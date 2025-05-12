package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WFLOATX;
import fr.ensimag.deca.codegen.StackCount;
import fr.ensimag.arm.pseudocode.ARMSPRegister;
import fr.ensimag.arm.pseudocode.ARMLiteral;
import fr.ensimag.arm.pseudocode.ARMRegister;
import fr.ensimag.arm.pseudocode.ARMGPRegister;
import fr.ensimag.arm.pseudocode.ARMRegisterOffset;
import fr.ensimag.arm.pseudocode.ARMLabel;
import fr.ensimag.arm.pseudocode.instructions.ARM_VLDR;
import fr.ensimag.arm.pseudocode.instructions.ARM_VPUSH;
import fr.ensimag.arm.pseudocode.instructions.ARM_VCVTF64F32;
import fr.ensimag.arm.pseudocode.instructions.ARM_TVMOV;
import fr.ensimag.arm.pseudocode.instructions.ARM_PUSH;
import fr.ensimag.arm.pseudocode.instructions.ARM_POP;
import fr.ensimag.arm.pseudocode.instructions.ARM_LDR;
import fr.ensimag.arm.pseudocode.instructions.ARM_BL;

import fr.ensimag.deca.codegen.ARMDataSection;

/**
 * Single precision, floating-point literal
 *
 * @author gl13
 * @date 01/01/2025
 */
public class FloatLiteral extends AbstractExpr {

    public float getValue() {
        return value;
    }

    private float value;
    private boolean hex = false;

    public FloatLiteral(float value) {
        Validate.isTrue(!Float.isInfinite(value),
                "literal values cannot be infinite");
        Validate.isTrue(!Float.isNaN(value),
                "literal values cannot be NaN");
        this.value = value;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        this.setType(compiler.environmentType.FLOAT);
        return compiler.environmentType.FLOAT;
    }

    @Override
    protected void codeGenPrint(DecacCompiler compiler, boolean printHex) {
        compiler.addInstruction(new LOAD(new ImmediateFloat(value), Register.getR(1)));
        if (!printHex) {
            compiler.addInstruction(new WFLOAT());
        } else {
            compiler.addInstruction(new WFLOATX());
        }
    }

    @Override
    protected void ARMCodeGenPrint(DecacCompiler compiler, boolean printHex) {
        // Account for incoming int printing
        ARMDataSection.setFloatPrint();

        // Store R0, R2 and R3 if need be
        if (ARMRegister.isUsed(0)) {
            compiler.addARMInstruction(new ARM_PUSH(ARMRegister.R0));
        }
        if (ARMRegister.isUsed(2)) {
            compiler.addARMInstruction(new ARM_PUSH(ARMRegister.R2));
        }
        if (ARMRegister.isUsed(3)) {
            compiler.addARMInstruction(new ARM_PUSH(ARMRegister.R3));
        }

        // Load float_format data into R0 (if used, push then pop)
        compiler.addARMInstruction(new ARM_LDR(ARMRegister.R0, new ARMLiteral("float_format")));

        // Create an entry for the float in the data section, load it then put it in R2
        // and R3
        String floatDataEntry = ARMDataSection.createFloatDataEntry(value);
        compiler.addARMInstruction(new ARM_LDR(ARMRegister.R3, new ARMLiteral(floatDataEntry)));
        compiler.addARMInstruction(new ARM_VLDR(ARMRegister.S31, new ARMRegisterOffset(0, ARMRegister.R3)));
        compiler.addARMInstruction(new ARM_VCVTF64F32(ARMRegister.D15, ARMRegister.S31));
        compiler.addARMInstruction(new ARM_TVMOV(ARMRegister.R2, ARMRegister.R3, ARMRegister.D15));

        // Call printf with BL instruction
        compiler.addARMInstruction(new ARM_BL(new ARMLabel("printf")));

        // Restore R0, R2 and R3 if need be
        if (ARMRegister.isUsed(3)) {
            compiler.addARMInstruction(new ARM_POP(ARMRegister.R3));
        }
        if (ARMRegister.isUsed(2)) {
            compiler.addARMInstruction(new ARM_POP(ARMRegister.R2));
        }
        if (ARMRegister.isUsed(0)) {
            compiler.addARMInstruction(new ARM_POP(ARMRegister.R0));
        }
    }

    public void setHex(boolean hexFormat) {
        this.hex = hexFormat;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        GPRegister unusedReg = Register.getUnusedR();
        if (unusedReg != null) {
            Register.setLastExprPos(new RegisterOffset(0, unusedReg));
            compiler.addInstruction(new LOAD(new ImmediateFloat(value), unusedReg));
        } else {
            Register.setLastExprPos(new RegisterOffset(0, Register.SP));
            compiler.addInstruction(new LOAD(new ImmediateFloat(value), Register.R1));
            compiler.addInstruction(new PUSH(Register.R1));
            StackCount.countPush();
        }
    }

    @Override
    protected void ARMCodeGenInst(DecacCompiler compiler) {
        // Create entry in data section, load this same entry here as a literal
        String dataEntry = ARMDataSection.createFloatDataEntry(value);

        ARMGPRegister unusedReg = ARMRegister.getUnusedR();
        if (unusedReg != null) {
            ARMRegister.setLastExprPos(new ARMRegisterOffset(0, unusedReg));
            compiler.addARMInstruction(new ARM_LDR(unusedReg, new ARMLiteral(dataEntry)));
        } else {
            ARMRegister.setLastExprPos(new ARMRegisterOffset(0, ARMRegister.SP));
            compiler.addARMInstruction(new ARM_LDR(ARMRegister.R12, new ARMLiteral(dataEntry)));
            compiler.addARMInstruction(new ARM_PUSH(ARMRegister.R12));
            StackCount.countPush();
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        if (hex) {
            s.print(java.lang.Float.toHexString(value));
        } else {
            s.print(java.lang.Float.toString(value));
        }
    }

    @Override
    String prettyPrintNode() {
        return "Float (" + getValue() + ")";
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
