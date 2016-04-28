package fr.upem.matou.server.reader;

import fr.upem.matou.common.Querys;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Represent the broadcast message.
 *
 * @author Damien Chesneau
 */
public class SendSrvMessageReader implements RequestReader<Optional<String>> {
    private final SocketChannel sc;
    private final ByteBuffer bb;
    private final ClientData clientData;
    private Reader<?> reader;
    private State state = State.PROCESS;
    private String value;

    public SendSrvMessageReader(SocketChannel sc, ByteBuffer bb, ClientData clientData) {
        Objects.requireNonNull(sc);
        Objects.requireNonNull(bb);
        this.clientData = Objects.requireNonNull(clientData);
        reader = new IntReader(bb);
        this.sc = sc;
        this.bb = bb;
    }

    @Override
    public RequestReader<?> process() {
        State process = reader.process();
        if (reader.isFinish() && reader instanceof IntReader) {
            IntReader sr = (IntReader) reader;
            OptionalInt value = sr.value();
            if (value.isPresent()) {
                int lenOfMessage = value.getAsInt();
                reader = new StringReader(bb, lenOfMessage);
            }
        } else if (State.FINISH.equals(process) && reader instanceof StringReader) {
            value = ((StringReader) reader).value().orElseThrow(AssertionError::new);
            state = State.FINISH;
            broadcastMessage(value);
            return new OperationCodeReader(sc, bb, clientData);
        }
        return this;
    }

    private void broadcastMessage(String message) {
        Collection<ClientData> clientDatas = clientData.server().getConnectedClients();
        String login = clientData.getPseudo();
        for (ClientData data : clientDatas) {
            ByteBuffer bb = Querys.encodeBroadcastMessage(login, message);
            data.addBufferToSend(bb);
        }
    }


    @Override
    public Optional<String> value() {
        return Optional.ofNullable(value);
    }

    @Override
    public boolean isFinish() {
        return State.FINISH.equals(state);
    }
}
