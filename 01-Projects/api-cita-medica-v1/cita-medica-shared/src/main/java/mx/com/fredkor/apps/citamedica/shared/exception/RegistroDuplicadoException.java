package mx.com.fredkor.apps.citamedica.shared.exception;

/**
 * Indica que se intentó registrar un recurso cuyos datos únicos
 * ya existen en el sistema.
 */
public class RegistroDuplicadoException extends RuntimeException {

    public RegistroDuplicadoException(String message) {
        super(message);
    }

    public RegistroDuplicadoException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}