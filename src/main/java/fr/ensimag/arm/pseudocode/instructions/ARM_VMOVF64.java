package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMBinaryInstructionDValToReg;
import fr.ensimag.arm.pseudocode.ARMDVal;
import fr.ensimag.arm.pseudocode.ARMDPRegister;

public class ARM_VMOVF64 extends ARMBinaryInstructionDValToReg {

    ARM_VMOVF64(ARMDPRegister op1, ARMDVal op2) {
        super(op1, op2);
    }

    @Override
    public String getName() {
        return super.getName().replace("F64", ".F64");
    }

}
