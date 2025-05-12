package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMBinaryInstructionDValToReg;
import fr.ensimag.arm.pseudocode.ARMSPRegister;

/**
 * @author gl13
 * @date 22/01/2025
 */
public class ARM_VCVTF32S32 extends ARMBinaryInstructionDValToReg {

    ARM_VCVTF32S32(ARMSPRegister op1, ARMSPRegister op2) {
        super(op1, op2);
    }

    @Override
    public String getName() {
        return super.getName().replace("F32", ".F32").replace("S32", ".S32");
    }

}
