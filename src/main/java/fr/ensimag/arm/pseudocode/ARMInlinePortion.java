package fr.ensimag.arm.pseudocode;

import java.io.PrintStream;

/**
 * Portion of ARM assembly code to be dumped verbatim into the
 * generated code.
 *
 * @author gl13
 * @date 14/01/2025
 */
public class ARMInlinePortion extends AbstractARMLine {
    private final String asmCode;

    public ARMInlinePortion(String asmCode) {
        super();
        this.asmCode = asmCode;
    }

    @Override
    void display(PrintStream s) {
        s.println(asmCode);
    }

}
