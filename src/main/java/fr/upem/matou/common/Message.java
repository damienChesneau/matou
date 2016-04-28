package fr.upem.matou.common;

import java.util.Objects;

/**
 * Represents a message with the login and her message.
 * @author Damien Chesneau
 */
public class Message {
    private final String login;
    private final String message;

    /**
     *
     * @param login of client in String.
     * @param message in String.
     */
    public Message(String login, String message) {
        this.login = Objects.requireNonNull(login);
        this.message = Objects.requireNonNull(message);
    }

    public String getLogin() {
        return login;
    }

    public String getMessage() {
        return message;
    }
}
