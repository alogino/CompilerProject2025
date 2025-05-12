package fr.ensimag.arm.pseudocode;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * Base class for instructions with 3 operands.
 *
 * @author gl13
 * @date 14/01/2025
 */
public class ARMTernaryInstruction extends ARMInstruction {
    private ARMOperand operand1, operand2, operand3;

    public ARMOperand getOperand1() {
        return operand1;
    }

    public ARMOperand getOperand2() {
        return operand2;
    }

    public ARMOperand getOperand3() {
        return operand3;
    }

    @Override
    void displayOperands(PrintStream s) {
        s.print(" ");
        s.print(operand1);
        s.print(", ");
        s.print(operand2);
        s.print(", ");
        s.print(operand3);
    }

    protected ARMTernaryInstruction(ARMOperand op1, ARMOperand op2, ARMOperand op3) {
        Validate.notNull(op1);
        Validate.notNull(op2);
        Validate.notNull(op3);
        this.operand1 = op1;
        this.operand2 = op2;
        this.operand3 = op3;
    }
}
