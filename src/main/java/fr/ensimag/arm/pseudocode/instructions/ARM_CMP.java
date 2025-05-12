package fr.ensimag.arm.pseudocode.instructions;

import fr.ensimag.arm.pseudocode.ARMBinaryInstructionDValToReg;
import fr.ensimag.arm.pseudocode.ARMGPRegister;
import fr.ensimag.arm.pseudocode.ARMDVal;

import java.io.PrintStream;

/**
 * @author gl13
 * @date 21/01/2025
 */

public class ARM_CMP extends ARMBinaryInstructionDValToReg {
    private ARMGPRegister op1;
    private ARMDVal op2;

    public ARM_CMP(ARMGPRegister op1, ARMDVal op2) {
        super(op1, op2);
        this.op1 = op1;
        this.op2 = op2;
    }

    @Override
    public String toString() {
        return "CMP " + op1 + ", " + op2;
    }
    void displayOperands(PrintStream s) {
        s.print(op1 + ", " + op2);
    }
}