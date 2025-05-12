package fr.ensimag.arm.pseudocode;

import java.io.PrintStream;

public class ARMLabelInstruction extends ARMInstruction {
    private ARMLabel label;

    public ARMLabelInstruction(ARMLabel label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label + ":";
    }

    @Override
    void displayOperands(PrintStream s) {
        if (label != null) {
            s.print(label);
        }
    }
}