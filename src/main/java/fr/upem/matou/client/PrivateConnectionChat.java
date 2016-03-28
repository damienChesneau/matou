package fr.upem.matou.client;

import fr.upem.matou.common.Query;
import fr.upem.matou.common.Querys;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Damien Chesneau
 */
public class PrivateConnectionChat {

    private final Thread reader = new Thread(this::reader);
    private final String pseudo;
    private final long secureNumber;
    private final ServerSocketChannel ss;
    private final Querys.PrivateConnResponse privateConResp;
    private final ArrayDeque<Path> files = new ArrayDeque<>(1);
    private Consumer<String> onMessage;
    private Consumer<Info> onInfo;
    private Function<String, Boolean> onFileRequestAsked;
    private SocketChannel socketChannel;
    private long fileSecureRandom = -1;

    private PrivateConnectionChat(Querys.PrivateConnResponse privateConResp, long secureNumber, String pseudo, ServerSocketChannel ss, SocketChannel socketChannel) {
        this.secureNumber = secureNumber;
        this.pseudo = pseudo;
        this.ss = ss;
        this.socketChannel = socketChannel;
        this.privateConResp = privateConResp;
    }

    static PrivateConnectionChat newSource(Querys.PrivateConnResponse privateConResp) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        return new PrivateConnectionChat(privateConResp, privateConResp.getSecureNumber().orElseThrow(AssertionError::new), privateConResp.getPseudo(), null, socketChannel);
    }

    static PrivateConnectionChat newTarget(long secureNumber, String pseudo) throws IOException {
        ServerSocketChannel ss = ServerSocketChannel.open();
        ss.bind(null);
        return new PrivateConnectionChat(null, secureNumber, pseudo, ss, null);
    }

    public void processAsSource(Consumer<String> onMessage, Consumer<Info> onInfo, Function<String, Boolean> onFileRequestAsked) throws IOException {
        fillParameters(onMessage, onInfo, onFileRequestAsked);
        int port = privateConResp.getPort().orElseThrow(AssertionError::new);
        InetAddress address = privateConResp.getAddress();
        socketChannel.connect(new InetSocketAddress(address, port));
        ByteBuffer byteBuffer = Querys.encodeFirstConnectionRequest(secureNumber);
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
        ByteBuffer bb = ByteBuffer.allocateDirect(1024);
        socketChannel.read(bb);
        if (!Querys.isDirectMessageAccepted(bb)) {
            onInfo.accept(Info.ILLEGAL_SECURE_NUMBER);
        }
        bb.clear();
        reader.start();
    }


    public void processAsTarget(Consumer<String> onMessage, Consumer<Info> onInfo, Function<String, Boolean> onFileRequestAsked) throws IOException {
        fillParameters(onMessage, onInfo, onFileRequestAsked);
        socketChannel = ss.accept();
        ByteBuffer bb = ByteBuffer.allocateDirect(1024);
        socketChannel.read(bb);
        long secureNumberReceived = Querys.decodeFirstConnectionRequest(bb);
        if (secureNumberReceived == secureNumber) {
            ByteBuffer byteBuffer = Querys.encodeClientAccepted();
            byteBuffer.flip();
            socketChannel.write(byteBuffer);
        } else {
            ByteBuffer byteBuffer = Querys.encodeClientRefused();
            byteBuffer.flip();
            socketChannel.write(byteBuffer);
            socketChannel.close();
            ss.close();
            return;
        }
        bb.clear();
        reader.start();
    }

    private void reader() {
        ByteBuffer bb = ByteBuffer.allocate(1024);
        try {
            while (!Thread.interrupted()) {
                socketChannel.read(bb);
                byte opCode = Querys.decodeOperationCode(bb);
                if (opCode == Query.MESSAGE_DIRECT_CLIENT.getOperationCode()) {
                    String message = Querys.decodeMessageDirectClient(bb);
                    if (onMessage != null) {
                        onMessage.accept(message);
                    }
                } else if (opCode == Query.CLIENT_FILE_REQUEST.getOperationCode()) {
                    String fileName = Querys.decodeFileRequest(bb);
                    if (onFileRequestAsked.apply(fileName)) {
                        if (fileSecureRandom == -1) {
                            fileSecureRandom = new SecureRandom().nextLong();
                        }
                        ServerSocketChannel serverSocketChannel = waitForFileTransfer(fileName);
                        InetSocketAddress localAddress = (InetSocketAddress) serverSocketChannel.getLocalAddress();
                        ByteBuffer byteBuffer1 = Querys.encodeAcceptFileRequestResponse(localAddress.getPort(), fileSecureRandom);//TODO
                        byteBuffer1.flip();
                        socketChannel.write(byteBuffer1);
                    } else {
                        ByteBuffer byteBuffer1 = Querys.encodeRefuseFileRequestResponse();
                        byteBuffer1.flip();
                        socketChannel.write(byteBuffer1);
                    }
                } else if (opCode == Query.CLIENT_FILE_REQUEST_RESPONSE.getOperationCode()) {
                    Querys.ResponseFileTransfer responseFileTransfer = Querys.decodeFileRequestResponse(bb);
                    if (responseFileTransfer.isAccept()) {
                        onInfo.accept(Info.CLIENT_ACCEPT_FILE);
                        sendFile(responseFileTransfer);
                    } else {
                        onInfo.accept(Info.CLIENT_REFUSE_FILE);
                    }
                } else {
                    System.out.println("Unknown cmd " + opCode);
                }
                bb.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ServerSocketChannel waitForFileTransfer(String fileName) throws IOException {
        ServerSocketChannel ss = ServerSocketChannel.open();
        ss.bind(null);
        Thread t = new Thread(() -> {
            try {
                SocketChannel accept = ss.accept();
                Querys.FileTransfer fileTransfer = Querys.decodeFileTransfer(accept);
                if (fileSecureRandom == fileTransfer.getSessionCode()) {
                    Files.write(Paths.get(fileName), fileTransfer.getData(), StandardOpenOption.CREATE_NEW);
                    onInfo.accept(Info.TRANSFER_COMPLETED);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.start();
        return ss;
    }

    private void sendFile(Querys.ResponseFileTransfer data) throws IOException {
        Path first = files.getLast();
        SocketChannel sc = SocketChannel.open();
        byte[] bytes = Files.readAllBytes(first);
        InetSocketAddress s = (InetSocketAddress) socketChannel.getRemoteAddress();
        sc.connect(new InetSocketAddress(s.getAddress(), data.getPort()));
        ByteBuffer bb = Querys.encodeFileTransfer(data.getSessionCode(), bytes);
        bb.flip();
        sc.write(bb);
    }

    private void fillParameters(Consumer<String> onMessage, Consumer<Info> onInfo, Function<String, Boolean> onFileRequestAsked) {
        this.onMessage = Objects.requireNonNull(onMessage);
        this.onInfo = Objects.requireNonNull(onInfo);
        this.onFileRequestAsked = Objects.requireNonNull(onFileRequestAsked);
    }

    public void sendMessage(String message) throws IOException {
        ByteBuffer byteBuffer = Querys.encodeMessageDirectClient(message);
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
    }

    public String getPseudo() {
        return pseudo;
    }

    public void sendRequestForFileTransfer(Path file) throws IOException {
        System.out.println(file.getFileName());
        ByteBuffer byteBuffer = Querys.encodeFileRequest(file.getFileName().toString());
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
        files.offer(file);
    }

    public ServerSocketChannel getServerSocketChannel() {
        return ss;
    }
}
