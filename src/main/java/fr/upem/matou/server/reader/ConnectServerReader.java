package fr.upem.matou.server.reader;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.Optional;

/**
 * Reader for an incoming first connection.
 *
 * @author Damien Chesneau
 */
public class ConnectServerReader implements RequestReader<Optional<String>> {
    private final SocketChannel sc;
    private final ByteBuffer bb;
    private final ClientData clientData;

    private Reader<?> reader;
    private State state = State.PROCESS;
    private String value;

    public ConnectServerReader(SocketChannel sc, ByteBuffer bb, ClientData clientData) {
        this.clientData = Objects.requireNonNull(clientData);
        Objects.requireNonNull(sc);
        Objects.requireNonNull(bb);
        reader = new ShortReader(bb);
        this.sc = sc;
        this.bb = bb;
    }

    @Override
    public RequestReader<?> process() {
        State process = reader.process();
        if (State.FINISH.equals(process) && reader instanceof ShortReader) {
            ShortReader sr = (ShortReader) reader;
            Optional<Short> value = sr.value();
            if (value.isPresent()) {
                short lenOfLogin = value.get();
                reader = new StringReader(bb, lenOfLogin);
            }
        } else if (State.FINISH.equals(process) && reader instanceof StringReader) {
            value = ((StringReader) reader).value().orElseThrow(AssertionError::new);
            state = State.FINISH;
            return new OperationCodeReader(sc, bb, clientData);
        }
        return this;
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
