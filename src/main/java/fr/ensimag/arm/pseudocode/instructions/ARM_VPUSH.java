package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMSPRegister;
import fr.ensimag.arm.pseudocode.ARMDPRegister;
import fr.ensimag.arm.pseudocode.ARMUnaryInstruction;

/**
 * @author gl13
 * @date 21/01/2025
 */
public class ARM_VPUSH extends ARMUnaryInstruction {

    public ARM_VPUSH(ARMSPRegister op1) {
        super(op1);
    }

    public ARM_VPUSH(ARMDPRegister op1) {
        super(op1);
    }

}
