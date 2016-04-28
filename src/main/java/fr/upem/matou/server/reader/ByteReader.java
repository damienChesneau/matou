package fr.upem.matou.server.reader;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * Reader for just a byte.
 * @author Damien Chesneau
 */
public class ByteReader extends Reader<Optional<Byte>> {

    public ByteReader(ByteBuffer bb) {
        super(Byte.BYTES, bb);
    }

    public Optional<Byte> value() {
        if (State.FINISH.equals(state())) {
            byte[] bb = data();
            byte b = bb[0];
            return Optional.of(b);
        }
        return Optional.empty();
    }

}
