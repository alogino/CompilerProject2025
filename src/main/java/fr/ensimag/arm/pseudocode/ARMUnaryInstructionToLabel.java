package fr.ensimag.arm.pseudocode;

import java.io.PrintStream;

/**
 * Branch instruction using a label operand.
 *
 * @author gl13
 * @date 23/01/2025
 */
public abstract class ARMUnaryInstructionToLabel extends ARMInstruction {
    private ARMLabel operand;

    public ARMUnaryInstructionToLabel(ARMLabel operand) {
        this.operand = operand;
    }

    @Override
    void displayOperands(PrintStream s) {
        s.print(" " + operand);
    }

    @Override
    public String toString() {
        return getName() + " " + operand;
    }

    public abstract String getName();
}