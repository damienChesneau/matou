package fr.upem.matou.common;

import java.util.Objects;

public class Message {
    private final String login;
    private final String message;

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
