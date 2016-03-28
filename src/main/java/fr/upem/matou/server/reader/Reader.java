package fr.upem.matou.server.reader;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * @author Damien Chesneau
 */
abstract class Reader<T> {
    private final int nbOctetToRead;
    private final ByteBuffer bb;

    private State state;
    private int nbOctetRead;
    private final byte[] data;

    Reader(int nbOctetToRead, ByteBuffer bb) {
        this.nbOctetToRead = nbOctetToRead;
        this.bb = Objects.requireNonNull(bb);
        reset();
        data = new byte[nbOctetToRead];
    }

    public abstract T value();

    public State process() {
        bb.flip();
        if (bb.remaining() >= nbOctetToRead) {
            for (int i = 0; i < nbOctetToRead; i++) {
                data[i] = bb.get();
                nbOctetRead++;
            }
            state = State.FINISH;
            bb.compact();
        } else {
            for (int i = nbOctetRead; i < bb.remaining(); i++) {
                data[i] = bb.get();
            }
            state = State.PROCESS;
        }
        if (nbOctetRead == nbOctetToRead) {
            state = State.FINISH;
        }
        return state;
    }

    private void reset() {
        state = State.PROCESS;
        nbOctetRead = 0;
    }

    public boolean isFinish() {
        return State.FINISH == state;
    }

    byte[] data() {
        return data;
    }

    State state() {
        return state;
    }
}
