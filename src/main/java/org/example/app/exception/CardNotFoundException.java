package org.example.app.exception;

public class CardNotFoundException extends Throwable {
    public CardNotFoundException() {
        super();
    }

    public CardNotFoundException(String message) {
        super(message);
    }

    public CardNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CardNotFoundException(Throwable cause) {
        super(cause);
    }
}
