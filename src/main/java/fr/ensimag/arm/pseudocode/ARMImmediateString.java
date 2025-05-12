package fr.ensimag.arm.pseudocode;

/**
 * Immediate operand representing a string.
 * 
 * @author gl13
 * @date 14/01/2025
 */
public class ARMImmediateString extends ARMOperand {
    private String value;

    public ARMImmediateString(String value) {
        super();
        this.value = value;
    }

    @Override
    public String toString() {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
