package fr.upem.matou.server.reader;

import java.nio.ByteBuffer;
import java.util.OptionalInt;

/**
 * @author Damien Chesneau
 */
public class IntReader extends Reader<OptionalInt> {

    public IntReader(ByteBuffer bb) {
        super(Integer.BYTES, bb);
    }

    public OptionalInt value() {
        if (state().equals(State.FINISH)) {
            byte[] data = data();
            int b = toInt(data);
            return OptionalInt.of(b);
        }
        return OptionalInt.empty();
    }

    private static int toInt(byte[] bytes) {
        int ret = 0;
        for (int i = 0; i < 4 && i < bytes.length; i++) {
            ret <<= 8;
            ret |= (int) bytes[i] & 0xFF;
        }
        return ret;
    }

}
