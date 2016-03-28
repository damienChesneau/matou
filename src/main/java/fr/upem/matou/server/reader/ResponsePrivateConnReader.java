package fr.upem.matou.server.reader;

import fr.upem.matou.common.Querys;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Damien Chesneau
 */
public class ResponsePrivateConnReader implements RequestReader<Optional<String>> {
    private final SocketChannel sc;
    private final ByteBuffer bb;
    private final ClientData clientData;
    private Reader<?> reader;
    private State state = State.PROCESS;
    private String value;

    public ResponsePrivateConnReader(SocketChannel sc, ByteBuffer bb, ClientData clientData) {
        Objects.requireNonNull(sc);
        Objects.requireNonNull(bb);

        this.clientData = Objects.requireNonNull(clientData);
        reader = new ByteReader(bb);
        this.sc = sc;
        this.bb = bb;
    }

    private byte lenIp = -1;
    private boolean accept;
    private List<Byte> ipAddress;
    private int port = -1;
    private long secureNumber;

    @Override
    public RequestReader process() {
        State process = reader.process();
        if (reader.isFinish() && reader instanceof ByteReader) {
            ByteReader sr = (ByteReader) reader;
            Optional<Byte> value = sr.value();
            if (value.isPresent()) {
                byte lenOfMessage = value.get();
                if (!value.isPresent()) {
                    bb.clear();
                    return this;
                }
                if (lenIp == -1 && !accept) {
                    if (lenOfMessage == Byte.MAX_VALUE) {
                        reader = new ByteReader(bb);
                        accept = true;
                    } else if (lenOfMessage == Byte.MIN_VALUE) {
                        System.out.println("REFUSED TODO");
                        accept = false;
                    } else {
                        throw new AssertionError();
                    }
                } else if (lenIp == -1 && accept) {
                    ByteReader brLenIp = (ByteReader) reader;
                    Optional<Byte> lenIpOp = brLenIp.value();
                    lenIp = lenIpOp.orElseThrow(AssertionError::new);
                    reader = new MultipleByteReader(lenIp, bb);
                }
            }
        } else if (State.FINISH.equals(process) && reader instanceof MultipleByteReader) {
            ipAddress = ((MultipleByteReader) reader).value().orElseThrow(AssertionError::new);
            reader = new IntReader(bb);
        } else if (State.FINISH.equals(process) && reader instanceof IntReader) {
            port = ((IntReader) reader).value().orElseThrow(AssertionError::new);
            reader = new LongReader(bb);
        } else if (State.FINISH.equals(process) && reader instanceof LongReader) {
            secureNumber = ((LongReader) reader).value().orElseThrow(AssertionError::new);
            reader = new ShortReader(bb);
        } else if (State.FINISH.equals(process) && reader instanceof ShortReader) {
            short loginLen = ((ShortReader) reader).value().orElseThrow(AssertionError::new);
            reader = new StringReader(bb, loginLen);
        } else if (State.FINISH.equals(process) && reader instanceof StringReader) {
            String targetLogin = ((StringReader) reader).value().orElseThrow(AssertionError::new);
            state = State.FINISH;
            replayRequestToTarget(targetLogin);
            return new OperationCodeReader(sc, bb, clientData);
        }
        return this;
    }

    private void replayRequestToTarget(String targetPseudo) {
        byte[] ipAddress = new byte[this.ipAddress.size()];
        for (int i = 0; i < this.ipAddress.size(); i++) {
            ipAddress[i] = this.ipAddress.get(i);
        }
        ByteBuffer byteBuffer = Querys.encodeRelayResponseToServerPrivateConnAccepted(ipAddress, port, secureNumber, clientData.getPseudo());
        ClientData clientData = this.clientData.server().getClients().get(targetPseudo);
        if (clientData == null) {
            throw new IllegalArgumentException();
        }
        clientData.addBufferToSend(byteBuffer);
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
