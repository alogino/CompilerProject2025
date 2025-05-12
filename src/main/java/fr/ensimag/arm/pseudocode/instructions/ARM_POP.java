package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMGPRegister;
import fr.ensimag.arm.pseudocode.ARMUnaryListInstruction;
import fr.ensimag.arm.pseudocode.ARMUnaryListOperand;

/**
 * @author gl13
 * @date 21/01/2025
 */
public class ARM_POP extends ARMUnaryListInstruction {

    public ARM_POP(ARMGPRegister op) {
        super(new ARMUnaryListOperand(op));
    }

}
