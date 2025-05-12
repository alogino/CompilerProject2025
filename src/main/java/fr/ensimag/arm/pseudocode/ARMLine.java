package fr.ensimag.arm.pseudocode;

import java.io.PrintStream;

/**
 * Line of code in an ARM program.
 *
 * @author gl13
 * @date 14/01/2025
 */
public class ARMLine extends AbstractARMLine {
    public ARMLine(ARMLabel label, ARMInstruction instruction, String comment) {
        super();
        checkComment(comment);
        this.label = label;
        this.instruction = instruction;
        this.comment = comment;
    }

    public ARMLine(ARMInstruction instruction) {
        super();
        this.instruction = instruction;
    }

    public ARMLine(String comment) {
        super();
        checkComment(comment);
        this.comment = comment;
    }

    public ARMLine(String raw1, Boolean rawPrint) {
        super();
        this.rawString = raw1;
    }

    public ARMLine(ARMLabel label) {
        super();
        this.label = label;
    }

    private void checkComment(final String s) {
        if (s == null) {
            return;
        }
        if (s.contains("\n")) {
            throw new ARMInternalError("Comment '" + s + "'contains newline character");
        }
        if (s.contains("\r")) {
            throw new ARMInternalError("Comment '" + s + "'contains carriage return character");
        }
    }

    private ARMInstruction instruction;
    private String comment;
    private ARMLabel label;
    private String rawString;

    @Override
    void display(PrintStream s) {
        boolean tab = false;
        if (label != null) {
            s.print(label);
            s.print(":");
            tab = true;
        }
        if (instruction != null) {
            s.print("\t");
            instruction.display(s);
            tab = true;
        }
        if (comment != null) {
            if (tab) {
                s.print("\t");
            }
            s.print("// " + comment);
        }
        if (rawString != null) {
            s.print(rawString);
        }
        s.println();
    }

    public void setARMInstruction(ARMInstruction instruction) {
        this.instruction = instruction;
    }

    public ARMInstruction getARMInstruction() {
        return instruction;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setARMLabel(ARMLabel label) {
        this.label = label;
    }

    public ARMLabel getARMLabel() {
        return label;
    }
}
