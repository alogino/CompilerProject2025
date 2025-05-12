package fr.ensimag.arm.pseudocode;

/**
 * Operand of an ARM Instruction.
 *
 * @author gl13
 * @date 14/01/2025
 */
public class ARMUnaryListOperand extends ARMRegister {

    public ARMUnaryListOperand(ARMRegister op) {
        super(op.toString());
    }

    @Override
    public String toString() {
        // truncate ARM_ prefix from instruction names
        return "{" + super.toString() + "}";
    }
}
