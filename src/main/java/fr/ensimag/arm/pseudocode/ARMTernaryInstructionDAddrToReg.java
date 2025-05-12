package fr.ensimag.arm.pseudocode;

/**
 * Base class for instructions with 3 operands.
 *
 * @author gl13
 * @date 14/01/2025
 */
public class ARMTernaryInstructionDAddrToReg extends ARMTernaryInstruction {
    public ARMTernaryInstructionDAddrToReg(ARMGPRegister op1, ARMGPRegister op2, ARMDAddr op3) {
        super(op1, op2, op3);
    }
}
