package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMGPRegister;
import fr.ensimag.arm.pseudocode.ARMLabel;
import fr.ensimag.arm.pseudocode.ARMUnaryInstructionToLabel;
import fr.ensimag.arm.pseudocode.ARMUnaryInstructionToReg;

/**
 * @author gl13
 * @date 21/01/2025
 */


/**
 * Branch if equal instruction for ARM.
 */
public class ARM_BEQ extends ARMUnaryInstructionToLabel {
    public ARM_BEQ(ARMLabel label) {
        super(label);
    }

    @Override
    public String getName() {
        return "beq";
    }
}
