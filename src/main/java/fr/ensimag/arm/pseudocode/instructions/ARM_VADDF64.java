package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMDPRegister;
import fr.ensimag.arm.pseudocode.ARMTernaryInstruction;

public class ARM_VADDF64 extends ARMTernaryInstruction {

    ARM_VADDF64(ARMDPRegister op1, ARMDPRegister op2, ARMDPRegister op3) {
        super(op1, op2, op3);
    }

    @Override
    public String getName() {
        return super.getName().replace("F64", ".F64");
    }

}
