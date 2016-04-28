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

    /**
     * Create a new instance of a client.
     * @param pseudo
     * @param server adress of the server
     * @return the client instance
     * @throws IOException
     */
    public static Client newInstance(String pseudo, InetSocketAddress server) throws IOException {
        SocketChannel sc = SocketChannel.open();
        return new Client(pseudo, server, sc);
    }

    private Client(String pseudo, InetSocketAddress server, SocketChannel sc) {
        this.sc = Objects.requireNonNull(sc);
        this.server = Objects.requireNonNull(server);
        this.pseudo = Objects.requireNonNull(pseudo);
    }

    /**
     * Connect to the target server.
     * @param messages A consumer of received messages.
     * @param privateConnectionAsked A consumer of new private connection asks.
     * @param acceptedPrivateConn A consumer of your accepted private connection.
     * @return
     * @throws IOException
     */
    public boolean connect(Consumer<Message> messages, Consumer<String> privateConnectionAsked, Consumer<PrivateConnectionChat> acceptedPrivateConn) throws IOException {
        Objects.requireNonNull(messages);
        Objects.requireNonNull(privateConnectionAsked);
        sc.connect(server);
        ByteBuffer byteBuffer = Querys.encodeServerConnect(pseudo);
        byteBuffer.flip();
        sc.write(byteBuffer);
        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES);
        sc.read(bb);
        byte b = Querys.decodeOperationCodeSynchronous(sc, bb);
        if (b == Query.VALIDATE_CONNECTION.getOperationCode()) {
            new Thread(new ClientRequestReader(messages, privateConnectionAsked, acceptedPrivateConn, sc)).start();
            return true;
        } else if (b == Query.ERROR_CONNECTION.getOperationCode()) {
            return false;
        }
        return false;
    }

    /**
     * Send a message in broadcast mode.
     * @param message The string message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        Objects.requireNonNull(message);
        ByteBuffer bb = Querys.encodeSendMessageToServer(message);
        bb.flip();
        sc.write(bb);
    }

    /**
     * Ask an other user of the chat to have a private connection.
     * @param login the target
     * @throws IOException
     */
    public void askPrivateConnection(String login) throws IOException {
        Objects.requireNonNull(login);
        ByteBuffer bb = Querys.encodeAskPrivateConnection(login);
        bb.flip();
        sc.write(bb);
    }

    /**
     * Validate the requested private connection.
     * @param pseudo of the target.
     * @return an privateConnectionChat instance
     * @throws IOException
     */
    public PrivateConnectionChat validatePrivateConnectionWith(String pseudo) throws IOException {
        long secureNumber = new SecureRandom().nextLong();
        PrivateConnectionChat initialize = PrivateConnectionChat.newTarget(secureNumber, pseudo);
        ServerSocketChannel ss = initialize.getServerSocketChannel();
        InetSocketAddress address = (InetSocketAddress) ss.getLocalAddress();
        int port = address.getPort();
        byte[] address1 = InetAddress.getLocalHost().getAddress();
        ByteBuffer bb = Querys.encodeResponseToServerPrivateConnAccepted(address1, pseudo, port, secureNumber);
        bb.flip();
        sc.write(bb);
        return initialize;
    }

    /**
     * Refuse a private connection.
     * @throws IOException
     */
    public void refusePrivateConnectionWith() throws IOException {
        ByteBuffer bb = Querys.encodeResponseToServerPrivateConnRefused();
        bb.flip();
        sc.write(bb);
    }

    /**
     * Close the chat.
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        sc.close();
    }
}
