package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.WINT;
import fr.ensimag.deca.codegen.StackCount;
import fr.ensimag.arm.pseudocode.ARMGPRegister;
import fr.ensimag.arm.pseudocode.ARMImmediateInteger;
import fr.ensimag.arm.pseudocode.ARMRegister;
import fr.ensimag.arm.pseudocode.ARMRegisterOffset;
import fr.ensimag.arm.pseudocode.ARMLabel;
import fr.ensimag.arm.pseudocode.instructions.ARM_MOV;
import fr.ensimag.arm.pseudocode.instructions.ARM_PUSH;
import fr.ensimag.arm.pseudocode.instructions.ARM_POP;
import fr.ensimag.arm.pseudocode.instructions.ARM_BL;
import fr.ensimag.arm.pseudocode.instructions.ARM_LDR;
import fr.ensimag.arm.pseudocode.ARMLiteral;
import fr.ensimag.deca.codegen.ARMDataSection;

/**
 * Integer literal
 *
 * @author gl13
 * @date 01/01/2025
 */
public class IntLiteral extends AbstractExpr {
    public int getValue() {
        return value;
    }

    private int value;

    public IntLiteral(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntLiteral) {
            return this.value == ((IntLiteral) obj).value;
        }
        return false;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        this.setType(compiler.environmentType.INT);
        return compiler.environmentType.INT;
    }

    @Override
    public AbstractExpr verifyRValue(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass,
            Type expectedType) throws ContextualError {
        this.verifyExpr(compiler, localEnv, currentClass);

        if (expectedType.isFloat()) {
            // cast this rvalue to a float from an int
        } else if (!expectedType.sameType(this.getType())) {
            // contextual error
            String errorMessage = String.format("Cannot assign expression of type '%s' to a variable of type '%s'",
                    this.getType(), expectedType);
            throw new ContextualError(errorMessage, getLocation());
        }
        return this;
    }

    @Override
    protected void codeGenPrint(DecacCompiler compiler, boolean printHex) {
        compiler.addInstruction(new LOAD(new ImmediateInteger(value), Register.getR(1)));
        compiler.addInstruction(new WINT());
    }

    @Override
    protected void ARMCodeGenPrint(DecacCompiler compiler, boolean printHex) {
        // Account for incoming int printing
        ARMDataSection.setIntPrint();

        // Load int_format data into R0 (if used, push then pop)
        if (ARMRegister.isUsed(0)) {
            compiler.addARMInstruction(new ARM_PUSH(ARMRegister.R0));
        }

        // Load literal to be printed in R1 (if used, push then pop)
        if (ARMRegister.isUsed(1)) {
            compiler.addARMInstruction(new ARM_PUSH(ARMRegister.R1));
        }

        compiler.addARMInstruction(new ARM_LDR(ARMRegister.R0, new ARMLiteral("int_format")));
        compiler.addARMInstruction(new ARM_MOV(ARMRegister.R1, new ARMImmediateInteger(value)));

        // Call printf with BL instruction
        compiler.addARMInstruction(new ARM_BL(new ARMLabel("printf")));

        // Restore R0 and or R1 if need be
        if (ARMRegister.isUsed(1)) {
            compiler.addARMInstruction(new ARM_POP(ARMRegister.R1));
        }
        if (ARMRegister.isUsed(0)) {
            compiler.addARMInstruction(new ARM_POP(ARMRegister.R0));
        }
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        GPRegister unusedReg = Register.getUnusedR();
        if (unusedReg != null) {
            Register.setLastExprPos(new RegisterOffset(0, unusedReg));
            compiler.addInstruction(new LOAD(this.value, unusedReg));
        } else {
            Register.setLastExprPos(new RegisterOffset(0, Register.SP));
            compiler.addInstruction(new LOAD(this.value, Register.R1));
            compiler.addInstruction(new PUSH(Register.R1));
            StackCount.countPush();
        }
    }

    @Override
    protected void ARMCodeGenInst(DecacCompiler compiler) {
        ARMGPRegister unusedReg = ARMRegister.getUnusedR();
        if (unusedReg != null) {
            ARMRegister.setLastExprPos(new ARMRegisterOffset(0, unusedReg));
            compiler.addARMInstruction(new ARM_MOV(unusedReg, new ARMImmediateInteger(value)));
        } else {
            ARMRegister.setLastExprPos(new ARMRegisterOffset(0, ARMRegister.SP));
            compiler.addARMInstruction(new ARM_MOV(ARMRegister.R12, new ARMImmediateInteger(value)));
            compiler.addARMInstruction(new ARM_PUSH(ARMRegister.R12));
            StackCount.countPush();
        }
    }

    @Override
    String prettyPrintNode() {
        return "Int (" + getValue() + ")";
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(Integer.toString(value));
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

}
