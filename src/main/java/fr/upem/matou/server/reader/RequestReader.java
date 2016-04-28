package fr.upem.matou.server.reader;

/**
 * Represents methods for an operation reader.
 *
 * @author Damien Chesneau
 */
public interface RequestReader<R> {
    RequestReader<?> process();

    R value();

    boolean isFinish();
}
