package fr.ensimag.arm.pseudocode;

import java.io.PrintStream;

/**
 * ARM instruction.
 *
 * @author gl13
 * @date 14/01/2025
 */
public abstract class ARMInstruction {

    public String getName() {
        return this.getClass().getSimpleName().substring(4);
    }

    abstract void displayOperands(PrintStream s);

    void display(PrintStream s) {
        s.print(getName());
        displayOperands(s);
    }
}
