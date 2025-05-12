package fr.ensimag.arm.pseudocode;

import fr.ensimag.deca.CompilerOptions;
import fr.ensimag.arm.pseudocode.ARMGPRegister;

/**
 * Register operand (including special registers like SP).
 *
 * @author gl13
 * @date 14/01/2025
 */
public class ARMRegister extends ARMDVal {
    private String name;
    private static int NbRegisters = CompilerOptions.getNbRegisters();

    protected ARMRegister(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Static Base register (equivalent to GB for ima) -> R9
     */
    public static final ARMRegister SB = new ARMRegister("sb");
    /**
     * Frame Pointer register (equivalent to LB for ima) -> R11
     */
    public static final ARMRegister FP = new ARMRegister("fp");
    /**
     * Stack Pointer
     */
    public static final ARMRegister SP = new ARMRegister("sp");
    /**
     * Stack Limit
     */
    public static final ARMRegister SL = new ARMRegister("sl");
    /**
     * ARMv7 (32 bits)
     *
     * General Purpose Registers. Array is private because Java arrays cannot be
     * made immutable, use getR(i) to access it.
     *
     * Cannot use following registers as General Purpose Registers:
     * - R9 : SB register
     * - R10 : SL register (Stack Limit, used for stack overflow)
     * - R11 : FP register
     * - R13 : SP register
     * - R14 : LR register (Holds the return adress for subroutine calls)
     * - R15 : PC register
     *
     * Scratch register: R12
     *
     * General Purpose Registers
     * - R0 : Argument/Return
     * - R1 -> R3 : Argument
     * - R4 -> R8 : Callee-saved (preserved across calls)
     */
    private static final ARMGPRegister[] R = initRegisters();

    /**
     * VFP s0 - s31 registers for ARMv7
     *
     * Floating point single precision registers. Available for any ARM CPU
     * that contains a VFP (Vector Floating Point unit).
     */
    private static final ARMSPRegister[] S = initSRegisters();

    /**
     * VFP d0 - d15 registers for ARMv7
     *
     * Floating point double precision registers. Available for any ARM CPU
     * that contains a VFP (Vector Floating Point unit).
     */
    private static final ARMDPRegister[] D = initDRegisters();

    /**
     * Convenience shortcut for R[0]
     */
    public static final ARMGPRegister R0 = R[0];
    /**
     * Convenience shortcut for R[1]
     */
    public static final ARMGPRegister R1 = R[1];
    /**
     * Convenience shortcut for R[2]
     */
    public static final ARMGPRegister R2 = R[2];
    /**
     * Convenience shortcut for R[3]
     */
    public static final ARMGPRegister R3 = R[3];
    /**
     * Convenience shortcut for R[12] (scratch register)
     */
    public static final ARMGPRegister R12 = R[12];

    static {
        lastExprPos = new ARMRegisterOffset(0, R0);
    }

    private static ARMRegisterOffset lastExprPos = new ARMRegisterOffset(0, R0);

    /**
     * Set the location of the latest computed expression's value
     */
    public static void setLastExprPos(ARMRegisterOffset exprPos) {
        lastExprPos = exprPos != null ? exprPos : new ARMRegisterOffset(0, R0);
    }

    /**
     * Get the location of the latest computed expression's value
     */
    public static ARMDVal getLastExprPos() {
        if (lastExprPos == null) {
            return R0;
        }
        return lastExprPos.getOffset() == 0 ? lastExprPos.getRegister() : lastExprPos;
    }

    /**
     * General Purpose Registers
     */
    public static ARMGPRegister getR(int i) {
        if (i >= 9 && i != 12) {
            System.err.printf("Using R%h, which is not supposed to"
                    + "be used as a general purpose register.\n", i);
        }
        return R[i];
    }

    /**
     * VFP unit Registers
     */
    public static ARMSPRegister VFPgetS(int i) {
        return S[i];
    }

    /**
     * VFP unit Registers
     */
    public static ARMDPRegister VFPgetD(int i) {
        return D[i];
    }

    static private ARMGPRegister[] initRegisters() {
        ARMGPRegister[] res = new ARMGPRegister[NbRegisters];
        for (int i = 0; i <= NbRegisters - 1; i++) {
            res[i] = new ARMGPRegister("r" + i, i);
        }
        return res;
    }

    /**
     * Convenience shortcut for S31 (scratch register)
     */
    public static final ARMSPRegister S31 = S[31];

    static private ARMSPRegister[] initSRegisters() {
        ARMSPRegister[] res = new ARMSPRegister[32];
        for (int i = 0; i <= 31; i++) {
            res[i] = new ARMSPRegister("s" + i, i);
        }
        return res;
    }

    /**
     * Convenience shortcut for D15 (scratch register)
     */
    public static final ARMDPRegister D15 = D[15];

    static private ARMDPRegister[] initDRegisters() {
        ARMDPRegister[] res = new ARMDPRegister[16];
        for (int i = 0; i <= 15; i++) {
            res[i] = new ARMDPRegister("d" + i, i);
        }
        return res;
    }

    /**
     * Statuses for R0 - min(NBRegisters - 1, 8) registers (padded in the beginning
     * by 2 for convenience). true means a register is being used and should not be
     * overwritten, false means otherwise.
     */
    private static final Boolean[] registerStatuses = initRegisterStatuses();

    private static final Boolean[] VFPSregisterStatuses = initVFPSRegisterStatuses();

    private static final Boolean[] VFPDregisterStatuses = initVFPDRegisterStatuses();

    /**
     * Get whether a register is used or not
     *
     * @param i register to check
     * @return true if Ri is used, false otherwise
     */
    public static Boolean isUsed(int i) {
        return registerStatuses[i];
    }

    /**
     * Set a register as used
     *
     * @param i register to set as used
     */
    public static void setUsed(int i) {
        registerStatuses[i] = true;
    }

    /**
     * Set a register as used
     *
     * @param i register to set as used
     */
    public static void setUsed(ARMGPRegister R) {
        registerStatuses[R.getNumber()] = true;
    }

    /**
     * Set a register as unused
     *
     * @param i register to set as unused
     */
    public static void setUnused(int i) {
        registerStatuses[i] = false;
    }

    /**
     * Set a register as unused
     *
     * @param R register to set as unused
     */
    public static void setUnused(ARMRegister Reg) {
        if (Reg instanceof ARMGPRegister) {
            ARMGPRegister R = (ARMGPRegister) Reg;
            registerStatuses[R.getNumber()] = false;
        } else if (Reg instanceof ARMSPRegister) {
            ARMSPRegister S = (ARMSPRegister) Reg;
            VFPSregisterStatuses[S.getNumber()] = false;
        } else {
            ARMDPRegister D = (ARMDPRegister) Reg;
            VFPDregisterStatuses[D.getNumber()] = false;
        }
    }

    /**
     * Get an unused register (prioritizes smallest numbered unused registers)
     *
     * @return unused register or null if all registers are used
     */
    public static ARMGPRegister getUnusedR() {
        int i = 0;
        while (i < Integer.min(NbRegisters, 8) && isUsed(i)) {
            i++;
        }
        if (i >= NbRegisters) {
            if (!isUsed(12)) {
                setUsed(12);
                return getR(12);
            }
            return null;
        }

        setUsed(i);
        return getR(i);
    }

    public static ARMGPRegister getUnusedSavedR() {
        int i = 4;
        while (i < Integer.min(NbRegisters, 8) && isUsed(i)) {
            i++;
        }
        if (i >= NbRegisters) {
            if (!isUsed(12)) {
                setUsed(12);
                return getR(12);
            }
            return null;
        }

        setUsed(i);
        return getR(i);
    }

    /**
     * Get whether a VFP single precision register is used or not
     *
     * @param i register to check
     * @return true if Ri is used, false otherwise
     */
    private static Boolean VFPSisUsed(int i) {
        return VFPSregisterStatuses[i];
    }

    /**
     * Set a VFP single precision register as used
     *
     * @param i register to set as used
     */
    private static void VFPSsetUsed(int i) {
        VFPSregisterStatuses[i] = true;
    }

    /**
     * Set a VFP single precision register as used
     *
     * @param i register to set as used
     */
    public static void VFPSsetUsed(ARMSPRegister S) {
        VFPSregisterStatuses[S.getNumber()] = true;
    }

    /**
     * Set a VFP single precision register as unused
     *
     * @param i register to set as unused
     */
    public static void VFPSsetUnused(int i) {
        VFPSregisterStatuses[i] = false;
    }

    /**
     * Set a VFP single precision register as unused
     *
     * @param S register to set as unused
     */
    public static void VFPSsetUnused(ARMSPRegister S) {
        VFPSregisterStatuses[S.getNumber()] = false;
    }

    /**
     * Get an unused VFP single precision register (prioritizes smallest numbered
     * unused registers). S31 is used as a scratch register.
     *
     * @return unused register or null if all registers are used
     */
    public static ARMSPRegister VFPgetUnusedS() {
        int i = 0;
        while (i < 30 && VFPSisUsed(i)) {
            i++;
        }
        if (i >= 31) {
            return null;
        }

        VFPSsetUsed(i);
        return VFPgetS(i);
    }

    /**
     * Get whether a double precision register is used or not
     *
     * @param i register to check
     * @return true if Di is used, false otherwise
     */
    private static Boolean VFPDisUsed(int i) {
        return VFPDregisterStatuses[i];
    }

    /**
     * Set a VFP double precision register as used
     *
     * @param i register to set as used
     */
    private static void VFPDsetUsed(int i) {
        VFPDregisterStatuses[i] = true;
    }

    /**
     * Set a VFP double precision register as unused
     *
     * @param i register to set as unused
     */
    public static void VFPDsetUnused(int i) {
        VFPDregisterStatuses[i] = false;
    }

    /**
     * Set a VFP double precision register as unused
     *
     * @param D register to set as unused
     */
    public static void VFPDsetUnused(ARMSPRegister D) {
        VFPDregisterStatuses[D.getNumber()] = false;
    }

    /**
     * Get an unused VFP double precision register (prioritizes smallest numbered
     * unused registers). D15 is used as a scratch register.
     *
     * @return unused register or null if all registers are used
     */
    public static ARMDPRegister VFPgetUnusedD() {
        int i = 0;
        while (i < 14 && VFPDisUsed(i)) {
            i++;
        }
        if (i >= 15) {
            return null;
        }

        VFPDsetUsed(i);
        return VFPgetD(i);
    }

    static private Boolean[] initRegisterStatuses() {
        Boolean[] res = new Boolean[NbRegisters];
        for (int i = 0; i <= NbRegisters - 1; i++) {
            res[i] = false;
        }
        return res;
    }

    static private Boolean[] initVFPSRegisterStatuses() {
        Boolean[] res = new Boolean[32];
        for (int i = 0; i <= 31; i++) {
            res[i] = false;
        }
        return res;
    }

    static private Boolean[] initVFPDRegisterStatuses() {
        Boolean[] res = new Boolean[16];
        for (int i = 0; i <= 15; i++) {
            res[i] = false;
        }
        return res;
    }

}
