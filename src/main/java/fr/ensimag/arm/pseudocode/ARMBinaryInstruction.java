package fr.ensimag.arm.pseudocode;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * Base class for instructions with 2 operands.
 *
 * @author gl13
 * @date 14/01/2025
 */
public class ARMBinaryInstruction extends ARMInstruction {
    private ARMOperand operand1, operand2;

    public ARMOperand getOperand1() {
        return operand1;
    }

    public ARMOperand getOperand2() {
        return operand2;
    }

    @Override
    void displayOperands(PrintStream s) {
        s.print(" ");
        s.print(operand1);
        s.print(", ");
        s.print(operand2);
    }

    protected ARMBinaryInstruction(ARMOperand op1, ARMOperand op2) {
        Validate.notNull(op1);
        Validate.notNull(op2);
        this.operand1 = op1;
        this.operand2 = op2;
    }
}
