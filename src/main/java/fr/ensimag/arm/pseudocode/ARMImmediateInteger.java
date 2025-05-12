package fr.ensimag.arm.pseudocode;

/**
 * Immediate operand representing an integer.
 * 
 * @author gl13
 * @date 14/01/2025
 */
public class ARMImmediateInteger extends ARMDVal {
    private int value;

    public ARMImmediateInteger(int value) {
        super();
        this.value = value;
    }

    @Override
    public String toString() {
        return "#" + value;
    }
}
