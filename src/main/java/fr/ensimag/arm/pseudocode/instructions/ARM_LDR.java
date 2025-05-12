package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMBinaryInstructionDValToReg;
import fr.ensimag.arm.pseudocode.ARMGPRegister;
import fr.ensimag.arm.pseudocode.ARMDVal;
import fr.ensimag.arm.pseudocode.ARMLiteral;

/**
 * @author gl13
 * @date 21/01/2025
 */
public class ARM_LDR extends ARMBinaryInstructionDValToReg {

    public ARM_LDR(ARMGPRegister op1, ARMDVal op2) {
        super(op1, op2);
    }

    public ARM_LDR(ARMGPRegister op1, ARMLiteral op2) {
        super(op1, op2);
    }

}
