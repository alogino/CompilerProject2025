package fr.ensimag.arm.pseudocode;

/**
 * Immediate operand containing a float value.
 * 
 * @author gl13
 * @date 14/01/2025
 */
public class ARMImmediateFloat extends ARMDVal {
    private float value;

    public ARMImmediateFloat(float value) {
        super();
        this.value = value;
    }

    @Override
    public String toString() {
        return "#" + Float.toHexString(value);
    }
}
