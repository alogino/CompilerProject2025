package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMRegister;
import fr.ensimag.arm.pseudocode.ARMUnaryListInstruction;
import fr.ensimag.arm.pseudocode.ARMUnaryListOperand;

/**
 * @author gl13
 * @date 21/01/2025
 */
public class ARM_PUSH extends ARMUnaryListInstruction {

    public ARM_PUSH(ARMRegister op1) {
        super(new ARMUnaryListOperand(op1));
    }

}
