package fr.upem.matou.client;

import fr.upem.matou.common.Message;
import fr.upem.matou.common.Query;
import fr.upem.matou.common.Querys;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Damien Chesneau
 */
public class Client implements AutoCloseable {
    private final String pseudo;
    private final InetSocketAddress server;
    private final SocketChannel sc;

    public static Client newInstance(String pseudo, InetSocketAddress server) throws IOException {
        SocketChannel sc = SocketChannel.open();
        return new Client(pseudo, server, sc);
    }

    private Client(String pseudo, InetSocketAddress server, SocketChannel sc) {
        this.sc = Objects.requireNonNull(sc);
        this.server = Objects.requireNonNull(server);
        this.pseudo = Objects.requireNonNull(pseudo);
    }


    public boolean connect(Consumer<Message> messages, Consumer<String> privateConnectionAsked, Consumer<PrivateConnectionChat> acceptedPrivateConn) throws IOException {
        Objects.requireNonNull(messages);
        Objects.requireNonNull(privateConnectionAsked);
        sc.connect(server);
        ByteBuffer byteBuffer = Querys.encodeServerConnect(pseudo);
        byteBuffer.flip();
        sc.write(byteBuffer);
        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES);
        sc.read(bb);
        byte b = Querys.decodeOperationCode(bb);
        if (b == Query.VALIDATE_CONNECTION.getOperationCode()) {
            new Thread(() -> {
                ByteBuffer bba = ByteBuffer.allocate(Byte.BYTES);
                try {
                    while (sc.read(bba) != -1) {
                        byte b1 = Querys.decodeOperationCode(bba);
                        if (b1 == Query.BROADCAST_CLIENTS_MESSAGE.getOperationCode()) {
                            Message mess = Querys.decodeBroadcastMessage(sc);
                            messages.accept(mess);
                        } else if (b1 == Query.ASK_SRV_PRIVATE_CON_RELAY_TARGET.getOperationCode()) {
                            String pseudoWishPrivateConn = Querys.decodeAskPrivateConnectionRelayToTarget(sc);
                            privateConnectionAsked.accept(pseudoWishPrivateConn);
                        } else if (b1 == Query.RESP_SRC_PRIVATE_CON_RELAY_CLIENT.getOperationCode()) {
                            ByteBuffer allocate = ByteBuffer.allocate(1024);
                            sc.read(allocate);//TODO reformat.
                            Querys.PrivateConnResponse privateConnResponse = Querys.decodeRelayResponseToServerPrivateConnAccepted(allocate);
                            PrivateConnectionChat privateConnectionSource = PrivateConnectionChat.newSource(privateConnResponse);
                            acceptedPrivateConn.accept(privateConnectionSource);
                        } else {
                            System.out.println("Incoming unknown request b=" + b1);
                            bba.clear();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            return true;
        } else if (b == Query.ERROR_CONNECTION.getOperationCode()) {
            return false;
        }
        return false;
    }

    public void sendMessage(String message) throws IOException {
        Objects.requireNonNull(message);
        ByteBuffer bb = Querys.encodeSendMessageToServer(message);
        bb.flip();
        sc.write(bb);
    }

    public void askPrivateConnection(String login) throws IOException {
        Objects.requireNonNull(login);
        ByteBuffer bb = Querys.encodeAskPrivateConnection(login);
        bb.flip();
        sc.write(bb);
    }


    public PrivateConnectionChat validatePrivateConnectionWith(String pseudo) throws IOException {
        long secureNumber = new SecureRandom().nextLong();
        PrivateConnectionChat initialize = PrivateConnectionChat.newTarget(secureNumber, pseudo);
        ServerSocketChannel ss = initialize.getServerSocketChannel();
        InetSocketAddress address = (InetSocketAddress) ss.getLocalAddress();
        int port = address.getPort();
        InetAddress inetAddress = address.getAddress();
        ByteBuffer bb = Querys.encodeResponseToServerPrivateConnAccepted(inetAddress, pseudo, port, secureNumber);
        bb.flip();
        sc.write(bb);
        return initialize;
    }

    public void refusePrivateConnectionWith() throws IOException {
        ByteBuffer bb = Querys.encodeResponseToServerPrivateConnRefused();
        bb.flip();
        sc.write(bb);
    }

    @Override
    public void close() throws IOException {
        sc.close();
    }
}
