package fr.ensimag.arm.pseudocode;

/**
 * Operand representing a register indirection with offset, e.g. 42(R3).
 *
 * @author gl13
 * @date 14/01/2025
 */
public class ARMRegisterOffset extends ARMDAddr {
    public int getOffset() {
        return offset;
    }

    public ARMRegister getRegister() {
        return register;
    }

    private final int offset;
    private final ARMRegister register;

    public ARMRegisterOffset(int offset, ARMRegister register) {
        super();
        this.offset = offset;
        this.register = register;
    }

    @Override
    public String toString() {
        if (offset == 0) {
            return "[" + register + "]";
        } else {
            return "[" + register + ", #" + offset + "]";
        }
    }
}
