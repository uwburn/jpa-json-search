package it.mgt.uti.jpajsonsearch;

public class JpaJsonSearchException extends RuntimeException {

    public JpaJsonSearchException(String message) {
        super(message);
    }

    public JpaJsonSearchException(Throwable cause) {
        super(cause);
    }

    public JpaJsonSearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
