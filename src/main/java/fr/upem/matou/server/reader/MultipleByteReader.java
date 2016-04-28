package fr.upem.matou.server.reader;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Reader for an byte array.
 * @author Damien Chesneau
 */
public class MultipleByteReader extends Reader<Optional<List<Byte>>> {

    public MultipleByteReader(int size, ByteBuffer bb) {
        super(size, bb);
    }

    public Optional<List<Byte>> value() {
        if (State.FINISH.equals(state())) {
            byte[] bb = data();
            ArrayList<Byte> toReturn = new ArrayList<>();
            for (byte b : bb) {
                toReturn.add(b);
            }
            return Optional.of(toReturn);
        }
        return Optional.empty();
    }

}
