package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WFLOATX;
import fr.ensimag.ima.pseudocode.instructions.WINT;
import fr.ensimag.deca.codegen.StackCount;
import fr.ensimag.deca.codegen.ARMDataSection;

import fr.ensimag.arm.pseudocode.instructions.ARM_POP;
import fr.ensimag.arm.pseudocode.instructions.ARM_PUSH;
import fr.ensimag.arm.pseudocode.instructions.ARM_LDR;
import fr.ensimag.arm.pseudocode.instructions.ARM_VLDR;
import fr.ensimag.arm.pseudocode.instructions.ARM_TVMOV;
import fr.ensimag.arm.pseudocode.instructions.ARM_VCVTF64F32;
import fr.ensimag.arm.pseudocode.instructions.ARM_MOV;
import fr.ensimag.arm.pseudocode.instructions.ARM_VMOV;
import fr.ensimag.arm.pseudocode.instructions.ARM_BL;
import fr.ensimag.arm.pseudocode.ARMRegister;
import fr.ensimag.arm.pseudocode.ARMRegisterOffset;
import fr.ensimag.arm.pseudocode.ARMDVal;
import fr.ensimag.arm.pseudocode.ARMLiteral;
import fr.ensimag.arm.pseudocode.ARMLabel;

/**
 * Expression, i.e. anything that has a value.
 *
 * @author gl13
 * @date 01/01/2025
 */
public abstract class AbstractExpr extends AbstractInst {
    /**
     * @return true if the expression does not correspond to any concrete token
     *         in the source code (and should be decompiled to the empty string).
     */
    boolean isImplicit() {
        return false;
    }

    /**
     * Get the type decoration associated to this expression (i.e. the type computed
     * by contextual verification).
     */
    public Type getType() {
        return type;
    }

    protected void setType(Type type) {
        Validate.notNull(type);
        this.type = type;
    }

    protected void setDefinition(Definition def) {
        Validate.notNull(def);
        this.definition = def;
    }

    private Type type;
    private Definition definition;

    @Override
    protected void checkDecoration() {
        if (getType() == null) {
            throw new DecacInternalError("Expression " + decompile() + " has no Type decoration");
        }
    }

    /**
     * Verify the expression for contextual error.
     * 
     * implements non-terminals "expr" and "lvalue"
     * of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler     (contains the "env_types" attribute)
     * @param localEnv
     *                     Environment in which the expression should be checked
     *                     (corresponds to the "env_exp" attribute)
     * @param currentClass
     *                     Definition of the class containing the expression
     *                     (corresponds to the "class" attribute)
     *                     is null in the main bloc.
     * @return the Type of the expression
     *         (corresponds to the "type" attribute)
     */
    public abstract Type verifyExpr(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError;

    /**
     * Verify the expression in right hand-side of (implicit) assignments
     * 
     * implements non-terminal "rvalue" of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler     contains the "env_types" attribute
     * @param localEnv     corresponds to the "env_exp" attribute
     * @param currentClass corresponds to the "class" attribute
     * @param expectedType corresponds to the "type1" attribute
     * @return this with an additional ConvFloat if needed...
     */
    public AbstractExpr verifyRValue(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass,
            Type expectedType)
            throws ContextualError {
        this.verifyExpr(compiler, localEnv, currentClass);
        if (!compiler.environmentType.isSubType(this.getType(), expectedType)) {
            // contextual error
            String errorMessage = String.format("Cannot assign expression of type '%s' to a variable of type '%s'",
                    this.getType(), expectedType);
            throw new ContextualError(errorMessage, getLocation());
        }
        return this;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        verifyExpr(compiler, localEnv, currentClass);
    }

    /**
     * Verify the expression as a condition, i.e. check that the type is
     * boolean.
     *
     * @param localEnv
     *                     Environment in which the condition should be checked.
     * @param currentClass
     *                     Definition of the class containing the expression, or
     *                     null in
     *                     the main program.
     */
    void verifyCondition(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        if (getType().equals(compiler.environmentType.BOOLEAN)) {
            return;
        } else {
            throw new ContextualError("Condition types is not valid, expected BOOLEAN", getLocation());
        }
    }

    /**
     * Generate code to print the expression
     *
     * @param compiler
     * @param printHex whether to print an int/float in hexadecimal format
     */
    protected void codeGenPrint(DecacCompiler compiler, boolean printHex) {
        codeGenInst(compiler);

        // in R1, we load the value of the expression if it's in a GPRegister, and POP
        // it from the stack otherwise
        DVal lastExprPos = Register.getLastExprPos();
        if (lastExprPos.equals(Register.SP)) {
            compiler.addInstruction(new POP(Register.R1));
            StackCount.countPop();
        } else {
            compiler.addInstruction(new LOAD(lastExprPos, Register.R1));
        }

        if (getType().isInt()) {
            compiler.addInstruction(new WINT());
        } else if (getType().isFloat()) {
            if (printHex) {
                compiler.addInstruction(new WFLOATX());
            } else {
                compiler.addInstruction(new WFLOAT());
            }
        }
    }

    protected void ARMCodeGenPrint(DecacCompiler compiler, boolean printHex) {
        // TODO:
        ARMCodeGenInst(compiler);

        // Save R1 if need be
        if (ARMRegister.isUsed(1)) {
            compiler.addARMInstruction(new ARM_PUSH(ARMRegister.R1));
        }

        ARMDVal lastExprPos = ARMRegister.getLastExprPos();
        if (lastExprPos.equals(ARMRegister.SP)) {
            compiler.addARMInstruction(new ARM_POP(ARMRegister.R1));
            StackCount.countPop();
        } else {
            compiler.addARMInstruction(new ARM_MOV(ARMRegister.R1, lastExprPos));
        }

        if (getType().isInt()) {
            // Account for incoming int printing
            ARMDataSection.setIntPrint();

            // Load int_format data into R0 (if used, push then pop)
            if (ARMRegister.isUsed(0)) {
                compiler.addARMInstruction(new ARM_PUSH(ARMRegister.R0));
            }

            compiler.addARMInstruction(new ARM_LDR(ARMRegister.R0, new ARMLiteral("int_format")));

            // Call printf with BL instruction
            compiler.addARMInstruction(new ARM_BL(new ARMLabel("printf")));

            if (ARMRegister.isUsed(0)) {
                compiler.addARMInstruction(new ARM_POP(ARMRegister.R0));
            }
        } else if (getType().isFloat()) {

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
            compiler.addARMInstruction(new ARM_VMOV(ARMRegister.S31, ARMRegister.R1));
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

        // Restore R1 if need be
        if (ARMRegister.isUsed(1)) {
            compiler.addARMInstruction(new ARM_POP(ARMRegister.R1));
        }
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
    }

    @Override
    protected void ARMCodeGenInst(DecacCompiler compiler) {
    }

    @Override
    protected void decompileInst(IndentPrintStream s) {
        decompile(s);
        s.print(";");
    }

    @Override
    protected void prettyPrintType(PrintStream s, String prefix) {
        Type t = getType();
        if (t != null) {
            s.print(prefix);
            s.print("type: ");
            s.print(t);
            s.println();
        }
    }
}
