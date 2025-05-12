package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMLabel;
import fr.ensimag.arm.pseudocode.ARMUnaryInstructionToLabel;

/**
 * Branch if Less or Equal instruction for ARM.
 */
public class ARM_BLE extends ARMUnaryInstructionToLabel {
    public ARM_BLE(ARMLabel label) {
        super(label);
    }

    @Override
    public String getName() {
        return "ble";
    }
}