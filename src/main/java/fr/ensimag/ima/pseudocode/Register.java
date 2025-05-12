package fr.ensimag.ima.pseudocode;

import java.util.HashMap;

import fr.ensimag.deca.CompilerOptions;
import fr.ensimag.deca.codegen.StackCount;
import fr.ensimag.deca.tree.AbstractExpr;

/**
 * Register operand (including special registers like SP).
 * 
 * @author Ensimag
 * @date 01/01/2025
 */
public class Register extends DVal {
    private String name;
    private static int NbRegisters = CompilerOptions.getNbRegisters();

    protected Register(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Global Base register
     */
    public static final Register GB = new Register("GB");
    /**
     * Local Base register
     */
    public static final Register LB = new Register("LB");
    /**
     * Stack Pointer
     */
    public static final Register SP = new Register("SP");
    /**
     * General Purpose Registers. Array is private because Java arrays cannot be
     * made immutable, use getR(i) to access it.
     */
    private static final GPRegister[] R = initRegisters();

    /**
     * General Purpose Registers
     */
    public static GPRegister getR(int i) {
        return R[i];
    }

    /**
     * Convenience shortcut for R[0]
     */
    public static final GPRegister R0 = R[0];
    /**
     * Convenience shortcut for R[1]
     */
    public static final GPRegister R1 = R[1];

    static private GPRegister[] initRegisters() {
        GPRegister[] res = new GPRegister[NbRegisters];
        for (int i = 0; i <= NbRegisters - 1; i++) {
            res[i] = new GPRegister("R" + i, i);
        }
        return res;
    }

    /**
     * Statuses for R2 - RNbRegisters - 1 registers (padded in the beginning by 2
     * for
     * convenience). true means a register is being used and should not be
     * overwritten, false means otherwise.
     */
    private static final Boolean[] registerStatuses = initRegisterStatuses();

    /**
     * Get whether a register is used or not
     *
     * @param i register to check
     * @return true if Ri is used, false otherwise
     */
    private static Boolean isUsed(int i) {
        assert i > 1;
        return registerStatuses[i];
    }

    /**
     * Set a register as used
     *
     * @param i register to set as used
     */
    public static void setUsed(int i) {
        assert i > 1;
        registerStatuses[i] = true;
        StackCount.setUsedGPReg(getR(i));
    }

    /**
     * Set a register as used
     *
     * @param R register to set as unused
     */
    public static void setUsed(GPRegister R) {
        registerStatuses[R.getNumber()] = true;
        StackCount.setUsedGPReg(R);
    }

    /**
     * Set a register as unused
     *
     * @param i register to set as unused
     */
    public static void setUnused(int i) {
        assert i > 1;
        registerStatuses[i] = false;
    }

    /**
     * Set a register as unused
     *
     * @param R register to set as unused
     */
    public static void setUnused(GPRegister R) {
        registerStatuses[R.getNumber()] = false;
    }

    /**
     * Get an unused register (prioritizes smallest numbered unused registers)
     *
     * @return unused register or null if all registers are used
     */
    public static GPRegister getUnusedR() {
        int i = 2;
        while (i < NbRegisters && isUsed(i)) {
            i++;
        }
        if (i >= NbRegisters) {
            return null;
        }

        setUsed(i);
        return getR(i);
    }

    static private Boolean[] initRegisterStatuses() {
        Boolean[] res = new Boolean[NbRegisters];
        res[0] = true;
        res[1] = true;
        for (int i = 2; i <= NbRegisters - 1; i++) {
            res[i] = false;
        }
        return res;
    }

    /** Store, for each GP register, the current AbstractExpr's value being held */
    static private HashMap<GPRegister, AbstractExpr> registerValues = initRegisterValues();

    /** Store the last expression's position */
    static private RegisterOffset lastExprPos = null;

    static private HashMap<GPRegister, AbstractExpr> initRegisterValues() {
        HashMap<GPRegister, AbstractExpr> ret = new HashMap<GPRegister, AbstractExpr>();

        for (int i = 0; i < NbRegisters - 1; i++) {
            ret.put(getR(i), null);
        }

        return ret;
    }

    /**
     * Get register containing expression's value, or null if no register contains
     * it
     *
     * @param expr the {@link AbstractExpr} whose value a register might contain
     * @return the first register containing the given {@link AbstractExpr}'s value,
     *         else null
     */
    public static GPRegister getRegisterOfExp(AbstractExpr expr) {
        for (GPRegister reg : registerValues.keySet()) {
            if (registerValues.get(reg).equals(expr)) {
                return reg;
            }
        }
        return null;
    }

    /**
     * Get the location of the latest computed expression's value
     *
     * @return the location of the latest computed expression's value
     */
    public static DVal getLastExprPos() {
        DVal loadTarget = lastExprPos.getOffset() == 0 ? lastExprPos.getRegister() : lastExprPos;
        return loadTarget;
    }

    /**
     * Set the location of the latest computed expression's value
     */
    public static void setLastExprPos(RegisterOffset exprPos) {
        lastExprPos = exprPos;
    }

    /**
     * Set a register as containing an expression's value
     *
     * @param reg  register the {@link AbstractExpr}'s value
     * @param expr the {@link AbstractExpr} itself
     */
    public static void setRegisterOfExp(GPRegister reg, AbstractExpr expr) {
        registerValues.put(reg, expr);

        // set the last expression's position
        setLastExprPos(new RegisterOffset(0, reg));
    }

}
