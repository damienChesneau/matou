package fr.upem.matou.client;

import fr.upem.matou.common.Message;
import fr.upem.matou.common.Query;
import fr.upem.matou.common.Querys;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.function.Consumer;

public class ClientRequestReader implements Runnable {
    private static final int BUFFER_SIZE = 1024;
    private final Consumer<Message> messages;
    private final Consumer<String> privateConnectionAsked;
    private final Consumer<PrivateConnectionChat> acceptedPrivateConn;
    private final SocketChannel sc;

    /**
     *
     * @param messages a consumer of broadcast message.
     * @param privateConnectionAsked a consumer of private connection asked.
     * @param acceptedPrivateConn a consumer of private connection accepted by the target.
     * @param sc the stream.
     */
    public ClientRequestReader(Consumer<Message> messages, Consumer<String> privateConnectionAsked, Consumer<PrivateConnectionChat> acceptedPrivateConn, SocketChannel sc) {
        this.sc = Objects.requireNonNull(sc);
        this.messages = Objects.requireNonNull(messages);
        this.privateConnectionAsked = Objects.requireNonNull(privateConnectionAsked);
        this.acceptedPrivateConn = Objects.requireNonNull(acceptedPrivateConn);
    }

    @Override
    public void run() {
        ByteBuffer bba = ByteBuffer.allocate(BUFFER_SIZE);
        try {
            while (sc.read(bba) != -1) {
                byte b1 = Querys.decodeOperationCodeSynchronous(sc, bba);
                if (b1 == Query.BROADCAST_CLIENTS_MESSAGE.getOperationCode()) {
                    Message mess = Querys.decodeBroadcastMessage(sc, bba);
                    messages.accept(mess);
                    bba.compact();
                } else if (b1 == Query.ASK_SRV_PRIVATE_CON_RELAY_TARGET.getOperationCode()) {
                    String pseudoWishPrivateConn = Querys.decodeAskPrivateConnectionRelayToTarget(sc, bba);
                    bba.compact();
                    privateConnectionAsked.accept(pseudoWishPrivateConn);
                } else if (b1 == Query.RESP_SRC_PRIVATE_CON_RELAY_CLIENT.getOperationCode()) {
                    Querys.PrivateConnResponse privateConnResponse = Querys.decodeRelayResponseToServerPrivateConnAccepted(sc, bba);
                    PrivateConnectionChat privateConnectionSource = PrivateConnectionChat.newSource(privateConnResponse);
                    bba.compact();
                    acceptedPrivateConn.accept(privateConnectionSource);
                } else {
                    System.out.println("Incoming unknown request b=" + b1);
                    bba.clear();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
