package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMGPRegister;
import fr.ensimag.arm.pseudocode.ARMUnaryInstructionToReg;

/**
 * @author gl13
 * @date 21/01/2025
 */
public class ARM_BVS extends ARMUnaryInstructionToReg {

    public ARM_BVS(ARMGPRegister op) {
        super(op);
    }

}
