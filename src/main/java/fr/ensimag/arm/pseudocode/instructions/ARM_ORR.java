package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMGPRegister;
import fr.ensimag.arm.pseudocode.ARMTernaryInstructionDValToReg;
import fr.ensimag.arm.pseudocode.ARMDVal;

/**
 * @author gl13
 * @date 21/01/2025
 */
public class ARM_ORR extends ARMTernaryInstructionDValToReg {

    public ARM_ORR(ARMGPRegister op1, ARMGPRegister op2, ARMDVal op3) {
        super(op1, op2, op3);
    }

}
