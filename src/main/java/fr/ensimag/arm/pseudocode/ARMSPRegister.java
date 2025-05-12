package fr.ensimag.arm.pseudocode;

/**
 * General Purpose Register operand (R0, R1, ... R15).
 * 
 * @author gl13
 * @date 14/01/2025
 */
public class ARMSPRegister extends ARMRegister {
    /**
     * @return the number of the register, e.g. 12 for R12.
     */
    public int getNumber() {
        return number;
    }

    private int number;

    ARMSPRegister(String name, int number) {
        super(name);
        this.number = number;
    }
}
