package fr.ensimag.arm.pseudocode;

import org.apache.commons.lang.Validate;

/**
 * Label used as operand
 *
 * @author gl13
 * @date 14/01/2025
 */
public class ARMLabelOperand extends ARMDVal {
    public ARMLabel getLabel() {
        return label;
    }

    private ARMLabel label;

    public ARMLabelOperand(ARMLabel label) {
        super();
        Validate.notNull(label);
        this.label = label;
    }

    @Override
    public String toString() {
        return label.toString();
    }

}
