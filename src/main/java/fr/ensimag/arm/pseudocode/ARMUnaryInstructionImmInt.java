package fr.ensimag.arm.pseudocode;

/**
 *
 * @author gl13
 * @date 14/01/2025
 */
public abstract class ARMUnaryInstructionImmInt extends ARMUnaryInstruction {

    protected ARMUnaryInstructionImmInt(ARMImmediateInteger operand) {
        super(operand);
    }

    protected ARMUnaryInstructionImmInt(int i) {
        super(new ARMImmediateInteger(i));
    }

}
