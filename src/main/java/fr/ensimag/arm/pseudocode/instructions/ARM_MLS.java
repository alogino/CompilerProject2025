package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMGPRegister;
import fr.ensimag.arm.pseudocode.ARMQuaternaryInstruction;

public class ARM_MLS extends ARMQuaternaryInstruction {

    public ARM_MLS(ARMGPRegister op1, ARMGPRegister op2, ARMGPRegister op3, ARMGPRegister op4) {
        super(op1, op2, op3, op4);
    }

}
