package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMGPRegister;
import fr.ensimag.arm.pseudocode.ARMBinaryInstructionDValToReg;
import fr.ensimag.arm.pseudocode.ARMDVal;

/**
 * @author gl13
 * @date 21/01/2025
 */
public class ARM_MOVT extends ARMBinaryInstructionDValToReg {

    public ARM_MOVT(ARMGPRegister op1, ARMDVal op2) {
        super(op1, op2);
    }

}
