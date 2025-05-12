package fr.ensimag.arm.pseudocode;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * Instruction with a single operand.
 *
 * @author gl13
 * @date 14/01/2025
 */
public abstract class ARMUnaryInstruction extends ARMInstruction {
    private ARMOperand operand;

    @Override
    void displayOperands(PrintStream s) {
        s.print(" ");
        s.print(operand);
    }

    protected ARMUnaryInstruction(ARMOperand operand) {
        Validate.notNull(operand);
        this.operand = operand;
    }

    public ARMOperand getOperand() {
        return operand;
    }

}
