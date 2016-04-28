package fr.upem.matou.server.reader;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Optional;

/**
 * Reader for just a String.
 * @author Damien Chesneau
 */
public class StringReader extends Reader<Optional<String>> {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private final int size;

    public StringReader(ByteBuffer bb, int size) {
        super(size, bb);
        this.size = size;
    }

    public Optional<String> value() {
        if (State.FINISH.equals(state())) {
            byte[] data = data();
            ByteBuffer bb = ByteBuffer.allocate(data.length);
            bb.put(data);
            String s = decodeString(bb, size);
            return Optional.of(s);
        }
        return Optional.empty();
    }

    private static String decodeString(ByteBuffer bb, int size) {
        bb.flip();
        ByteBuffer loginBb = ByteBuffer.allocate(size);
        for (int i = 0; i < size; i++) {
            loginBb.put(bb.get());
        }
        loginBb.flip();
        return UTF8.decode(loginBb).toString();
    }
}
