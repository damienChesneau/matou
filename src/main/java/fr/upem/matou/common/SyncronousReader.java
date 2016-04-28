package fr.upem.matou.common;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.function.Function;

/**
 * Represents all methods we need to read in syncrhonous method.
 *
 * @author Damien Chesneau
 */
public class SyncronousReader {
    /**
     * Read a long in the ByteBuffer or if it's not present read more in the SocketChannel.
     *
     * @param sc stream connection with target
     * @param bb ByteBuffer of datas
     * @return value
     * @throws IOException
     */
    public static long readFullyLong(SocketChannel sc, ByteBuffer bb) throws IOException {
        return readFully(sc, bb, Long.BYTES, ByteBuffer::getLong);
    }

    /**
     * Read a int in the ByteBuffer or if it's not present read more in the SocketChannel.
     *
     * @param sc stream connection with target
     * @param bb ByteBuffer of datas
     * @return value
     * @throws IOException
     */
    public static int readFullyInteger(SocketChannel sc, ByteBuffer bb) throws IOException {
        return readFully(sc, bb, Integer.BYTES, ByteBuffer::getInt);
    }

    /**
     * Read a short in the ByteBuffer or if it's not present read more in the SocketChannel.
     *
     * @param sc stream connection with target
     * @param bb ByteBuffer of datas
     * @return value
     * @throws IOException
     */
    public static short readFullyShort(SocketChannel sc, ByteBuffer bb) throws IOException {
        return readFully(sc, bb, Short.BYTES, ByteBuffer::getShort);
    }

    /**
     * Read a byte array in the ByteBuffer or if it's not present read more in the SocketChannel.
     *
     * @param sc stream connection with target
     * @param bb ByteBuffer of datas
     * @return value
     * @throws IOException
     */
    public static byte[] readFullyByteArray(SocketChannel sc, ByteBuffer bb, long bufferSize) throws IOException {
        return readFully(sc, bb, bufferSize, ByteBuffer::array);
    }

    /**
     * Read a byte in the ByteBuffer or if it's not present read more in the SocketChannel.
     *
     * @param sc stream connection with target
     * @param bb ByteBuffer of datas
     * @return value
     * @throws IOException
     */
    public static byte readFullyByte(SocketChannel sc, ByteBuffer bb) throws IOException {
        return readFully(sc, bb, Byte.BYTES, ByteBuffer::get);
    }

    private static <R> R readFully(SocketChannel sc, ByteBuffer bb, long nbByte, Function<ByteBuffer, R> retFunction) throws IOException {
        if (bb.remaining() < nbByte) {
            while (bb.remaining() < nbByte) {
                if (sc.read(bb) == -1) {
                    throw new IOException();
                } else {
                    bb.flip();
                }
            }
        }
        return retFunction.apply(bb);
    }

}
