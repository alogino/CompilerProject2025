package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMGPRegister;
import fr.ensimag.arm.pseudocode.ARMUnaryInstructionToReg;

/**
 * @author gl13
 * @date 21/01/2025
 */
public class ARM_BNE extends ARMUnaryInstructionToReg {

    public ARM_BNE(ARMGPRegister op) {
        super(op);
    }

}
