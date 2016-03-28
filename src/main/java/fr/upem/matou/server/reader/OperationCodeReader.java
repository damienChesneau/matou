package fr.upem.matou.server.reader;

import fr.upem.matou.common.Query;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Damien Chesneau
 */
public class OperationCodeReader implements RequestReader<Optional<Byte>> {

    private final ByteReader byteReader;
    private final SocketChannel sc;
    private final ByteBuffer bb;
    private final ClientData clientData;

    private State state = State.PROCESS;

    public OperationCodeReader(SocketChannel sc, ByteBuffer bb, ClientData clientData) {
        this.clientData = Objects.requireNonNull(clientData);
        Objects.requireNonNull(sc);
        Objects.requireNonNull(bb);
        byteReader = new ByteReader(bb);
        this.sc = sc;
        this.bb = bb;
    }

    @Override
    public RequestReader process() {
        State processState = byteReader.process();
        if (State.FINISH.equals(processState)) {
            Optional<Byte> opValue = byteReader.value();
            if (opValue.isPresent()) {
                byte value = opValue.get();
                if (Query.CONNECT_SERVER.getOperationCode() == value) {
                    state = State.FINISH;
                    return new ConnectServerReader(sc, bb, clientData);
                } else if (Query.SEND_SRV_MESSAGE.getOperationCode() == value) {
                    state = State.FINISH;
                    return new SendSrvMessageReader(sc, bb, clientData);
                } else if (Query.ASK_SRV_PRIVATE_CON.getOperationCode() == value) {
                    state = State.FINISH;
                    return new AskPrivateConnReader(sc, bb, clientData);
                } else if (Query.RESP_SRV_PRIVATE_CON.getOperationCode() == value) {
                    state = State.FINISH;
                    return new ResponsePrivateConnReader(sc, bb, clientData);
                }
            }
        }
        return this;
    }

    @Override
    public Optional<Byte> value() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFinish() {
        return State.FINISH.equals(state);
    }

}
