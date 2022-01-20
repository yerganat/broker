package kz.salyqtez.broker.exception;

public class IdMismatchException extends RuntimeException {

    public IdMismatchException() {

    }
    public IdMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
    // ...
}
