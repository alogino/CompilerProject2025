package fr.ensimag.arm.pseudocode;

import org.apache.commons.lang.Validate;

/**
 * Representation of a label in ARM code. The same structure is used for label
 * declaration (e.g. foo: instruction) or use (e.g. ADD foo).
 *
 * @author gl13
 * @date 14/01/2025
 */
public class ARMLabel extends ARMOperand {

    @Override
    public String toString() {
        return name;
    }

    public ARMLabel(String name) {
        super();
        Validate.isTrue(name.length() <= 1024, "Label name too long, not supported by IMA");
        // Validate.isTrue(name.matches("^[_][a-zA-Z][a-zA-Z0-9_.]*$"), "Invalid label
        // name " + name);
        this.name = name;
    }

    private String name;
}
