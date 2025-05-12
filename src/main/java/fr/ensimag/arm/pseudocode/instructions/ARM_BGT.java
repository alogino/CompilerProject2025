package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMLabel;
import fr.ensimag.arm.pseudocode.ARMUnaryInstructionToLabel;

/**
 * Branch if Greater Than instruction for ARM.
 */
public class ARM_BGT extends ARMUnaryInstructionToLabel {
    public ARM_BGT(ARMLabel label) {
        super(label);
    }

    @Override
    public String getName() {
        return "bgt";
    }
}