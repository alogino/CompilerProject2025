package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMBinaryInstructionDValToReg;
import fr.ensimag.arm.pseudocode.ARMDVal;
import fr.ensimag.arm.pseudocode.ARMSPRegister;

public class ARM_VMOVF32 extends ARMBinaryInstructionDValToReg {

    ARM_VMOVF32(ARMSPRegister op1, ARMDVal op2) {
        super(op1, op2);
    }

    @Override
    public String getName() {
        return super.getName().replace("F32", ".F32");
    }

}
