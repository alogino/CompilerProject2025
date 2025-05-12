package fr.ensimag.arm.pseudocode;

/**
 * Base class for instructions with 2 operands, the first being a
 * DVal, and the second a Register.
 *
 * @author gl13
 * @date 14/01/2025
 */
public class ARMBinaryInstructionDValToReg extends ARMBinaryInstruction {

    public ARMBinaryInstructionDValToReg(ARMRegister op1, ARMDVal op2) {
        super(op1, op2);
    }
}
