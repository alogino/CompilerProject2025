package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMBinaryInstructionDAddrToReg;
import fr.ensimag.arm.pseudocode.ARMDAddr;
import fr.ensimag.arm.pseudocode.ARMRegister;

/**
 * @author gl13
 * @date 21/01/2025
 */
public class ARM_STR extends ARMBinaryInstructionDAddrToReg {

    public ARM_STR(ARMRegister op1, ARMDAddr op2) {
        super(op1, op2);
    }

}
