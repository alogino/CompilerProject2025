package fr.ensimag.arm.pseudocode;

import java.io.PrintStream;

/**
 * Instruction without operand.
 *
 * @author gl13
 * @date 14/01/2025
 */
public abstract class ARMNullaryInstruction extends ARMInstruction {
    @Override
    void displayOperands(PrintStream s) {
        // no operand
    }
}
