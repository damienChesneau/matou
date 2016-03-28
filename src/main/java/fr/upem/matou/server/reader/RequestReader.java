package fr.upem.matou.server.reader;

/**
 * @author Damien Chesneau
 */
public interface RequestReader<R> {
    RequestReader process();

    R value();

    boolean isFinish();
}
