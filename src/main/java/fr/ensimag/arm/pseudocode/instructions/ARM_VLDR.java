package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMBinaryInstructionDValToReg;
import fr.ensimag.arm.pseudocode.ARMSPRegister;
import fr.ensimag.arm.pseudocode.ARMDAddr;
import fr.ensimag.arm.pseudocode.ARMLiteral;

/**
 * @author gl13
 * @date 21/01/2025
 */
public class ARM_VLDR extends ARMBinaryInstructionDValToReg {

    public ARM_VLDR(ARMSPRegister op1, ARMDAddr op2) {
        super(op1, op2);
    }

    public ARM_VLDR(ARMSPRegister op1, ARMLiteral op2) {
        super(op1, op2);
    }

}
