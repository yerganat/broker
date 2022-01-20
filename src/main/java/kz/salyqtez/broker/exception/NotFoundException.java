package kz.salyqtez.broker.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException() {

    }
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    // ...
}
