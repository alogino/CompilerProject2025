package fr.ensimag.arm.pseudocode;

/**
 * Operand of an ARM Instruction.
 *
 * @author gl13
 * @date 14/01/2025
 */
public abstract class ARMOperand {
    @Override
    public String toString() {
        // truncate ARM_ prefix from instruction names
        return super.toString();
    }
}
