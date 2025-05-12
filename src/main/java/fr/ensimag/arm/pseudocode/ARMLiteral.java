package fr.ensimag.arm.pseudocode;

/**
 * Immediate operand containing a float value.
 * 
 * @author gl13
 * @date 14/01/2025
 */
public class ARMLiteral extends ARMDVal {
    private String entryName;

    public ARMLiteral(String entryName) {
        super();
        this.entryName = entryName;
    }

    @Override
    public String toString() {
        return "=" + entryName;
    }
}
