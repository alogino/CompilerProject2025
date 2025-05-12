package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMBinaryInstructionDValToReg;
import fr.ensimag.arm.pseudocode.ARMSPRegister;
import fr.ensimag.arm.pseudocode.ARMDVal;

/**
 * @author gl13
 * @date 21/01/2025
 */
public class ARM_VCMPF32 extends ARMBinaryInstructionDValToReg {

    public ARM_VCMPF32(ARMSPRegister op1, ARMDVal op2) {
        super(op1, op2);
    }

    @Override
    public String getName() {
        return super.getName().replace("F32", ".F32");
    }

}
