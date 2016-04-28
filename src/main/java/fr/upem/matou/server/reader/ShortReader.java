package fr.upem.matou.server.reader;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * Reader for just a short.
 * @author Damien Chesneau
 */
public class ShortReader extends Reader<Optional<Short>> {

    public ShortReader(ByteBuffer bb) {
        super(Short.BYTES, bb);
    }

    public Optional<Short> value() {
        if (State.FINISH.equals(state())) {
            byte[] data = data();
            short val = (short) (((data[0] & 0xFF) << 8) | ((data[1] & 0xFF)));
            return Optional.of(val);
        }
        return Optional.empty();
    }

}
