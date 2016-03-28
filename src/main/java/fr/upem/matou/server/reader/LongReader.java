package fr.upem.matou.server.reader;

import java.nio.ByteBuffer;
import java.util.OptionalLong;

/**
 * @author Damien Chesneau
 */
public class LongReader extends Reader<OptionalLong> {

    public LongReader(ByteBuffer bb) {
        super(Long.BYTES, bb);
    }

    public OptionalLong value() {
        if (State.FINISH.equals(state())) {
            long l = toLong(data());
            return OptionalLong.of(l);
        }
        return OptionalLong.empty();
    }

    private static long toLong(byte[] datas) {
        long value = 0;
        for (int data : datas) {
            value = (value << 8) + (data & 0xff);
        }
        return value;
    }
}
