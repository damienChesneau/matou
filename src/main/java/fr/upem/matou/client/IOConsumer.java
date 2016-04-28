package fr.upem.matou.client;

import java.io.IOException;

/**
 * Allows to throw an exception with a consumer.
 * @author Damien Chesneau
 */
@FunctionalInterface
public interface IOConsumer<T> {
    void accept(T message) throws IOException;
}
