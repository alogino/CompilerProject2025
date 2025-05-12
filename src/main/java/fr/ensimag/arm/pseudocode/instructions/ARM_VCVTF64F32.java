package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMBinaryInstructionDValToReg;
import fr.ensimag.arm.pseudocode.ARMDPRegister;
import fr.ensimag.arm.pseudocode.ARMSPRegister;

/**
 * @author gl13
 * @date 22/01/2025
 */
public class ARM_VCVTF64F32 extends ARMBinaryInstructionDValToReg {

    public ARM_VCVTF64F32(ARMDPRegister op1, ARMSPRegister op2) {
        super(op1, op2);
    }

    @Override
    public String getName() {
        return super.getName().replace("F64", ".F64").replace("F32", ".F32");
    }

}
