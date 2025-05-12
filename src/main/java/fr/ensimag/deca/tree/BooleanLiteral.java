package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.arm.pseudocode.*;
import fr.ensimag.arm.pseudocode.instructions.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.deca.codegen.StackCount;

/**
 *
 * @author gl13
 * @date 01/01/2025
 */
public class BooleanLiteral extends AbstractExpr {

    private boolean value;

    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        this.setType(compiler.environmentType.BOOLEAN);
        return compiler.environmentType.BOOLEAN;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        DVal loadValue;
        if (this.value) {
            loadValue = new ImmediateInteger(1);
        } else {
            loadValue = new ImmediateInteger(0);
        }

        GPRegister unusedReg = Register.getUnusedR();
        if (unusedReg != null) {
            Register.setLastExprPos(new RegisterOffset(0, unusedReg));
            compiler.addInstruction(new LOAD(loadValue, unusedReg));
        } else {
            Register.setLastExprPos(new RegisterOffset(0, Register.SP));
            compiler.addInstruction(new LOAD(loadValue, Register.R1));
            compiler.addInstruction(new PUSH(Register.R1));
            StackCount.countPush();
        }
    }

    @Override
    protected void ARMCodeGenInst(DecacCompiler compiler) {
        if (ARMRegister.getLastExprPos() == null) {
            ARMRegister.setLastExprPos(new ARMRegisterOffset(0, ARMRegister.R0));
        }

        ARMImmediateInteger loadValue = new ARMImmediateInteger(value ? 1 : 0);

        ARMGPRegister unusedReg = ARMRegister.getUnusedR();
        if (unusedReg != null) {
            ARMRegister.setLastExprPos(new ARMRegisterOffset(0, unusedReg));
            compiler.addARMInstruction(new ARM_MOV(unusedReg, loadValue));
        } else {
            ARMRegister.setLastExprPos(new ARMRegisterOffset(0, ARMRegister.SP));
            compiler.addARMInstruction(new ARM_MOV(ARMRegister.R12, loadValue));
            compiler.addARMInstruction(new ARM_PUSH(ARMRegister.R12));
            StackCount.countPush();
        }
    }

    @Override
    protected void ARMCodeGenPrint(DecacCompiler compiler, boolean printHex) {
        if (ARMRegister.isUsed(0)) {
            compiler.addARMInstruction(new ARM_PUSH(ARMRegister.R0));
        }

        compiler.addARMInstruction(new ARM_LDR(ARMRegister.R0,
                new ARMLiteral(value ? "str_true" : "str_false")));

        compiler.addARMInstruction(new ARM_BL(new ARMLabel("printf")));

        if (ARMRegister.isUsed(0)) {
            compiler.addARMInstruction(new ARM_POP(ARMRegister.R0));
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(Boolean.toString(value));
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    String prettyPrintNode() {
        return "BooleanLiteral (" + value + ")";
    }

}
