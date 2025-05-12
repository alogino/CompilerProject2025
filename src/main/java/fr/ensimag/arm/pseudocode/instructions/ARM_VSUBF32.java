package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMSPRegister;
import fr.ensimag.arm.pseudocode.ARMTernaryInstruction;

public class ARM_VSUBF32 extends ARMTernaryInstruction {

    public ARM_VSUBF32(ARMSPRegister op1, ARMSPRegister op2, ARMSPRegister op3) {
        super(op1, op2, op3);
    }

    @Override
    public String getName() {
        return super.getName().replace("F32", ".F32");
    }

}
