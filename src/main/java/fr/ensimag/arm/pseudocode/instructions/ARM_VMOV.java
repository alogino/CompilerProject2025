package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMBinaryInstructionDValToReg;
import fr.ensimag.arm.pseudocode.ARMGPRegister;
import fr.ensimag.arm.pseudocode.ARMSPRegister;

public class ARM_VMOV extends ARMBinaryInstructionDValToReg {

    public ARM_VMOV(ARMSPRegister op1, ARMGPRegister op2) {
        super(op1, op2);
    }

    public ARM_VMOV(ARMGPRegister op1, ARMSPRegister op2) {
        super(op1, op2);
    }

}
