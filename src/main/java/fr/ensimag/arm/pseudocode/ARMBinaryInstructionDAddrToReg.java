package fr.ensimag.arm.pseudocode;

/**
 * Base class for instructions with 2 operands, the first being a
 * DAddr, and the second a Register.
 *
 * @author gl13
 * @date 14/01/2025
 */
public class ARMBinaryInstructionDAddrToReg extends ARMBinaryInstructionDValToReg {

    public ARMBinaryInstructionDAddrToReg(ARMRegister op1, ARMDAddr op2) {
        super(op1, op2);
    }

}
