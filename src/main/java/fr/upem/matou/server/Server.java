package fr.upem.matou.server;

import fr.upem.matou.client.Client;
import fr.upem.matou.server.reader.ClientData;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * Manage server with asynchronous API.
 *
 * @author Damien Chesneau
 */
public class Server implements Closeable {

    private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;
    private final Set<SelectionKey> selectedKeys;
    private final HashMap<String, ClientData> clients = new HashMap<>();

    public static Server init(int port) throws IOException {
        if (port <= 1024) {
            throw new IllegalArgumentException();
        }
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        Selector selector = Selector.open();
        return new Server(serverSocketChannel, selector);
    }

    private Server(ServerSocketChannel serverSocketChannel, Selector selector) {
        this.serverSocketChannel = Objects.requireNonNull(serverSocketChannel);
        this.selector = Objects.requireNonNull(selector);
        this.selectedKeys = selector.selectedKeys();
    }

    /**
     * Start the server.
     *
     * @throws IOException
     */
    public void launch() throws IOException {
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        System.out.println("Server started on " + ((InetSocketAddress) serverSocketChannel.getLocalAddress()).getPort());
        while (!Thread.interrupted()) {
            selector.select();
            processSelectedKeys();
            selectedKeys.clear();
        }
    }

    private void processSelectedKeys() throws IOException {
        for (SelectionKey key : selectedKeys) {
            if (key.isValid() && key.isAcceptable()) {
                doAccept(key);//throw Io ex server error.
            }
            try {
                if (key.isValid() && key.isWritable()) {
                    doWrite(key);
                }
                if (key.isValid() && key.isReadable()) {
                    doRead(key);
                }
            } catch (IOException e) { // Client problem
                System.out.println("Client problem.");
                e.printStackTrace();
                key.cancel();
            }
        }
    }

    private void doAccept(SelectionKey key) throws IOException {
        // only the ServerSocketChannel is register in OP_ACCEPT
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel sc = ssc.accept();
        if (sc == null) return; // In case, the selector gave a bad hint
        sc.configureBlocking(false);
        SelectionKey register = sc.register(selector, SelectionKey.OP_READ);
        register.attach(new ClientData(register, this));
    }

    private void doRead(SelectionKey key) throws IOException {
        Object attachment = key.attachment();
        if (attachment instanceof ClientData) {
            ClientData client = (ClientData) attachment;
            client.doRead(key);
        }
    }

    private void doWrite(SelectionKey key) throws IOException {
        if (key.attachment() != null && key.attachment() instanceof ClientData) {
            ClientData clientData = (ClientData) key.attachment();
            clientData.doWrite(key);
        }
    }

    public boolean incomingClient(String login, ClientData clientData) {
        if (!clients.containsKey(login)) {
            clients.put(login, clientData);
            return true;
        }
        return false;
    }

    public Collection<ClientData> getConnectedClients() {
        return clients.values();
    }

    /**
     * get all clients using the server.
     *
     * @return map
     */
    public Map<String, ClientData> getClients() {
        return clients;
    }

    public void disconnetClient(String pseudo, ClientData value) {
        boolean remove = clients.remove(pseudo, value);
        System.out.println(remove);
    }

    @Override
    public void close() throws IOException {
        selectedKeys.clear();
        selector.close();
        serverSocketChannel.close();
    }
}
