package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMGPRegister;
import fr.ensimag.arm.pseudocode.ARMLabel;
import fr.ensimag.arm.pseudocode.ARMUnaryInstructionToLabel;
import fr.ensimag.arm.pseudocode.ARMUnaryInstructionToReg;

/**
 * Unconditional branch instruction for ARM.
 */

public class ARM_B extends ARMUnaryInstructionToLabel {
    public ARM_B(ARMLabel label) {
        super(label);
    }

    @Override
    public String getName() {
        return "b";
    }
}