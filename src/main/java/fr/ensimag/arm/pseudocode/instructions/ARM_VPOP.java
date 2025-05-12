package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMSPRegister;
import fr.ensimag.arm.pseudocode.ARMDPRegister;
import fr.ensimag.arm.pseudocode.ARMUnaryInstructionToReg;

/**
 * @author gl13
 * @date 21/01/2025
 */
public class ARM_VPOP extends ARMUnaryInstructionToReg {

    public ARM_VPOP(ARMSPRegister op) {
        super(op);
    }

    public ARM_VPOP(ARMDPRegister op) {
        super(op);
    }

}
