package fr.ensimag.deca.codegen;

import java.util.HashSet;
import java.util.Set;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.TSTO;

/**
 * Helper class to generate overflow checking code
 * 
 * @author gl13
 * @date 16/01/2025
 */
public class StackCount {
    private static int varCount;
    private static int savedRegNum;
    private static int tempCount;
    private static int maxParamCount;

    private static Set<GPRegister> regsToSave = new HashSet<GPRegister>();

    private static int tempCountCandidate;
    private static int tempParamCandidate;

    public static int getVarCount() {
        return varCount;
    }

    public static int getSavedRegNum() {
        return savedRegNum;
    }

    public static int getTempCount() {
        return tempCount;
    }

    public static int getParamCount() {
        return maxParamCount;
    }

    public static int getAggregate() {
        return varCount + savedRegNum + tempCount + maxParamCount;
    }

    public static void addVarCount(int VTableSize) {
        varCount += VTableSize;
    }

    public static int incVarCount() {
        return ++varCount;
    }

    public static int incSavedRegNum() {
        return ++savedRegNum;
    }

    public static int incTempCount() {
        return ++tempCount;
    }

    public static void setMaxParamCount(int newParamCountCandidate) {
        if (newParamCountCandidate > maxParamCount) {
            maxParamCount = newParamCountCandidate;
        }
    }

    private static void resetCount() {
        varCount = 0;
        savedRegNum = 0;
        tempCount = 0;
        maxParamCount = 0;

        tempCountCandidate = 0;
    }

    public static void setUsedGPReg(GPRegister reg) {
        regsToSave.add(reg);
    }

    private static void resetRegs() {
        regsToSave.clear();
    }

    /**
     * Tracks PUSH instructions, updates {@link StackCount#tempCount} if
     * necessary
     */
    public static void countPush() {
        tempCountCandidate++;
        if (tempCountCandidate > tempCount) {
            tempCount++;
        }
    }

    /**
     * Tracks POP instructions
     */
    public static void countPop() {
        tempCountCandidate--;
    }

    /**
     * Generates assembly code for setting correct
     * {@link fr.ensimag.ima.pseudocode.Register.SP} value and checking for stack
     * overflow
     */
    public static void genCodeAllocateStack(DecacCompiler compiler) {
        // TODO : (check page 210) maxStackSize should be: number of saved registers at
        // block start + number of global variables (OK) + number of max temps used for
        // expression evaluation + max of number of parameters of called methods
        int maxStackSize = StackCount.getAggregate();

        int SPOffset = StackCount.getVarCount() + StackCount.getTempCount();

        compiler.addFirst(new ADDSP(SPOffset));
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.setPossibleStackOverflow();
            compiler.addFirst(new BOV(new Label("stack_overflow_error")));
            compiler.addFirst(new TSTO(maxStackSize), "Check for stack overflow");
        }

        resetCount();
    }

    /**
     * Generates assembly code for saving R2 - R15 registers that are
     * used in a block
     */
    public static void genCodeSaveRegisters(DecacCompiler compiler) {
        for (GPRegister reg : regsToSave) {
            compiler.addFirst(new PUSH(reg));
            incSavedRegNum();
        }

    }

    /**
     * Generates assembly code for restoring R2 - R15 registers that are
     * used in a block
     */
    public static void genCodeRestoreRegisters(DecacCompiler compiler) {
        for (GPRegister reg : regsToSave) {
            compiler.addInstruction(new POP(reg));
        }

        resetRegs();
    }
}
