package fr.ensimag.arm.pseudocode;

/**
 * Internal error related to IMA code. Should never happen.
 * 
 * @author gl13
 * @date 14/01/2025
 */
public class ARMInternalError extends RuntimeException {
    public ARMInternalError(String message, Throwable cause) {
        super(message, cause);
    }

    public ARMInternalError(String message) {
        super(message);
    }

    private static final long serialVersionUID = 2492165456035733778L;

}
