package fr.ensimag.arm.pseudocode;

/**
 * Base class for instructions with 3 operands.
 *
 * @author gl13
 * @date 14/01/2025
 */
public class ARMTernaryInstructionDValToReg extends ARMTernaryInstruction {
    public ARMTernaryInstructionDValToReg(ARMRegister op1, ARMRegister op2, ARMDVal op3) {
        super(op1, op2, op3);
    }
}
