package fr.upem.matou.server.reader;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Damien Chesneau
 */
public class AskPrivateConnReader implements RequestReader<Optional<String>> {
    private final SocketChannel sc;
    private final ByteBuffer bb;
    private final ClientData clientData;
    private Reader<?> reader;
    private State state = State.PROCESS;
    private String value;

    public AskPrivateConnReader(SocketChannel sc, ByteBuffer bb, ClientData clientData) {
        Objects.requireNonNull(sc);
        Objects.requireNonNull(bb);
        this.clientData = Objects.requireNonNull(clientData);
        reader = new ShortReader(bb);
        this.sc = sc;
        this.bb = bb;
    }

    @Override
    public RequestReader process() {
        State process = reader.process();
        if (reader.isFinish() && reader instanceof ShortReader) {
            ShortReader sr = (ShortReader) reader;
            Optional<Short> value = sr.value();
            if (value.isPresent()) {
                short lenOfMessage = value.get();
                reader = new StringReader(bb, lenOfMessage);
            }
        } else if (State.FINISH.equals(process) && reader instanceof StringReader) {
            value = ((StringReader) reader).value().orElseThrow(AssertionError::new);
            state = State.FINISH;
            sendToTarget(value.trim());
            return new OperationCodeReader(sc, bb, clientData);
        }
        return this;
    }

    private void sendToTarget(String loginTarget) {
        Collection<ClientData> clientDatas = this.clientData.server().getConnectedClients();
        String loginAsk = this.clientData.getPseudo();
        for (ClientData data : clientDatas) {
            data.privateConAskRelay(loginAsk, loginTarget);
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
