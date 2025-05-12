package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMGPRegister;
import fr.ensimag.arm.pseudocode.ARMDPRegister;
import fr.ensimag.arm.pseudocode.ARMTernaryInstructionDValToReg;

public class ARM_TVMOV extends ARMTernaryInstructionDValToReg {

    public ARM_TVMOV(ARMGPRegister op1, ARMGPRegister op2, ARMDPRegister op3) {
        super(op1, op2, op3);
    }

    @Override
    public String getName() {
        return super.getName().replace("TVMOV", "VMOV").replace("F32", ".F32");
    }

}
