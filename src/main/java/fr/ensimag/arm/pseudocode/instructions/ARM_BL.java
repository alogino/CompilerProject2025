package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMUnaryInstruction;
import fr.ensimag.arm.pseudocode.ARMLabel;
import fr.ensimag.arm.pseudocode.ARMLabelOperand;

/**
 * @author gl13
 * @date 21/01/2025
 */
public class ARM_BL extends ARMUnaryInstruction {

    public ARM_BL(ARMLabelOperand op) {
        super(op);
    }

    public ARM_BL(ARMLabel op) {
        super(new ARMLabelOperand(op));
    }

}
