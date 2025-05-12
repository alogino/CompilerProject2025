package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMLabel;
import fr.ensimag.arm.pseudocode.ARMUnaryInstructionToLabel;

/**
 * Branch if Greater or Equal instruction for ARM.
 */
public class ARM_BGE extends ARMUnaryInstructionToLabel {
    public ARM_BGE(ARMLabel label) {
        super(label);
    }

    @Override
    public String getName() {
        return "bge";
    }
}