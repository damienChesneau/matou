package fr.upem.matou.server.reader;

import fr.upem.matou.common.Querys;
import fr.upem.matou.server.Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author Damien Chesneau
 */
public class ClientData {
    private static final int BUFFER_SIZE = 1024;

    private final ByteBuffer in = ByteBuffer.allocateDirect(BUFFER_SIZE);
    private ByteBuffer out = ByteBuffer.allocateDirect(BUFFER_SIZE);
    private final ArrayBlockingQueue<ByteBuffer> messagesToSend = new ArrayBlockingQueue<>(10);
    private SelectionKey selectionKey;
    private final SocketChannel socketChannel;
    private final Server server;

    private String pseudo;
    private boolean closed;//TODO
    private RequestReader<?> requestReader;

    public ClientData(SelectionKey selectionKey, Server server) {
        this.server = Objects.requireNonNull(server);
        this.selectionKey = Objects.requireNonNull(selectionKey);
        SocketChannel sc = (SocketChannel) selectionKey.channel();
        requestReader = new OperationCodeReader(sc, in, this);
        this.socketChannel = sc;
    }

    public void doRead(SelectionKey key) throws IOException {
        selectionKey = key;
        int read = socketChannel.read(in);//TODO check read;
        RequestReader process = requestReader.process();
        if (requestReader.isFinish() && requestReader instanceof ConnectServerReader) {
            Optional<String> value = ((ConnectServerReader) requestReader).value();
            pseudo = value.orElseThrow(AssertionError::new);
            ByteBuffer bb = server.incomingClient(pseudo, this) ? Querys.encodeSuccessResponse() : Querys.encodeConnectionErrorResponse();
            messagesToSend.add(bb);
            selectionKey.interestOps(SelectionKey.OP_WRITE);
            in.clear();
            SocketChannel sc = (SocketChannel) selectionKey.channel();
            requestReader = new OperationCodeReader(sc, in, this);
        } else if (requestReader.isFinish() && (!(requestReader instanceof OperationCodeReader))) {
            requestReader = process;
        } else if (!process.isFinish() && in.limit() > 0) {
            key.interestOps(SelectionKey.OP_READ);
            requestReader = process;
            doRead(key);
        } else if (!messagesToSend.isEmpty()) {
            key.interestOps(SelectionKey.OP_WRITE);
            requestReader = process;
        }
    }

    public void doWrite(SelectionKey key) throws IOException {
        if (out.limit() > 0) {
            out = simpleWrite(out);
        } else if (messagesToSend.isEmpty()) {
            selectionKey.interestOps(SelectionKey.OP_READ);
        } else {
            ByteBuffer bb = messagesToSend.poll();
            out = simpleWrite(bb);
        }
    }

    private ByteBuffer simpleWrite(ByteBuffer bb) throws IOException {
        bb.flip();
        int write = socketChannel.write(bb);
        bb.position(write);
        return bb.slice();
    }

    public Server server() {
        return server;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void addBufferToSend(ByteBuffer bb) {
        Objects.requireNonNull(bb);
        messagesToSend.add(bb);
        selectionKey.interestOps(SelectionKey.OP_WRITE);
    }

    public void privateConAskRelay(String fromPseudo, String target) {
        if (!Objects.requireNonNull(target).equals(pseudo)) {
            return;
        }
        Objects.requireNonNull(fromPseudo);
        ByteBuffer byteBuffer = Querys.encodeAskPrivateConnectionRelayToTarget(fromPseudo);
        messagesToSend.add(byteBuffer);
        selectionKey.interestOps(SelectionKey.OP_WRITE);
    }
}
