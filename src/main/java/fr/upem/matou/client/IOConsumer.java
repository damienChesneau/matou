package fr.upem.matou.client;

import java.io.IOException;

/**
 * @author Damien Chesneau
 */
@FunctionalInterface
public interface IOConsumer<T> {
    void accept(T message) throws IOException;
}
