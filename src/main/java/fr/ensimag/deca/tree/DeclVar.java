package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.StackCount;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.STORE;

import fr.ensimag.arm.pseudocode.ARMRegisterOffset;
import fr.ensimag.arm.pseudocode.ARMDVal;
import fr.ensimag.arm.pseudocode.ARMDAddr;
import fr.ensimag.arm.pseudocode.ARMRegister;
import fr.ensimag.arm.pseudocode.ARMSPRegister;

import fr.ensimag.arm.pseudocode.instructions.ARM_STR;
import fr.ensimag.arm.pseudocode.instructions.ARM_VSTR;

/**
 * @author gl13
 * @date 01/01/2025
 */
public class DeclVar extends AbstractDeclVar {

    final private AbstractIdentifier type;
    final private AbstractIdentifier varName;
    final private AbstractInitialization initialization;

    public DeclVar(AbstractIdentifier type, AbstractIdentifier varName, AbstractInitialization initialization) {
        Validate.notNull(type);
        Validate.notNull(varName);
        Validate.notNull(initialization);
        this.type = type;
        this.varName = varName;
        this.initialization = initialization;
    }

    private VariableDefinition varDef = null;

    @Override
    protected void verifyDeclVar(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        // Step 1: Verify the type of the variable (e.g., 'int', 'float')
        Type t1 = type.verifyType(compiler);

        // Ensure the type is valid for a variable declaration
        if (t1.isVoid()) {
            throw new ContextualError("Invalid type for variable declaration: " + t1, this.getLocation());
        }

        // Decorate the type node with a TypeDefinition
        type.setDefinition(compiler.environmentType.defOfType(t1.getName()));

        // No previous decalaration -> Create a VariableDefinition for this variable and
        // associate it with the name
        this.varDef = new VariableDefinition(t1, varName.getLocation());
        varName.setType(t1);
        varName.setDefinition(this.varDef);

        // Declare the variable in the local environment
        try {
            localEnv.declare(varName.getName(), this.varDef);
        } catch (EnvironmentExp.DoubleDefException e) {
            throw new ContextualError(
                    "Variable '" + varName.getName() + "' is already defined in the environement of expressions",
                    varName.getLocation());
        }

        // verify variable name is valid
        varName.verifyExpr(compiler, localEnv, currentClass);

        // Verify the initialization expression (both Init and NoInit)
        initialization.verifyInitialization(compiler, t1, localEnv, currentClass);
        if (initialization instanceof Initialization) {
            varName.getVariableDefinition().setInitializationStatus(true);
        }
    }

    @Override
    public void codeGenDeclVar(DecacCompiler compiler) {
        // attribute global stack position of variable
        this.varDef.setOperand(new RegisterOffset(StackCount.incVarCount(), Register.GB));

        // generate the initialization code if variable has been initialized
        if (this.initialization.isInitialized()) {
            codeGenDeclVarInitialization(compiler);
        }
    }

    /**
     * Generates assembly code for initialization of variable declaration
     */
    private void codeGenDeclVarInitialization(DecacCompiler compiler) {
        AbstractExpr varExpr = ((Initialization) this.initialization).getExpression();
        varExpr.codeGenInst(compiler);

        // TODO : adapt for POP and PUSH

        DVal loadTarget = Register.getLastExprPos();
        compiler.addInstruction(new STORE((Register) loadTarget, (DAddr) varDef.getOperand()));
        Register.setUnused((GPRegister) loadTarget);
    }

    @Override
    public void ARMCodeGenDeclVar(DecacCompiler compiler) {
        // attribute global stack position of variable
        this.varDef.setARMOperand(new ARMRegisterOffset(StackCount.incVarCount() * 4,
                ARMRegister.SB));

        // generate the initialization code if variable has been initialized
        if (this.initialization.isInitialized()) {
            ARMCodeGenDeclVarInitialization(compiler);
        }
    }

    private void ARMCodeGenDeclVarInitialization(DecacCompiler compiler) {
        AbstractExpr varExpr = ((Initialization) this.initialization).getExpression();
        varExpr.ARMCodeGenInst(compiler);

        ARMDVal loadTarget = ARMRegister.getLastExprPos();
        compiler.addARMInstruction(new ARM_STR((ARMRegister) loadTarget, (ARMDAddr) varDef.getARMOperand()));
        ARMRegister.setUnused((ARMRegister) loadTarget);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        this.type.decompile(s);
        s.print(" ");
        this.varName.decompile(s);
        if (this.initialization != null) {
            this.initialization.decompile(s);
        }
        s.print(";");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        varName.iter(f);
        initialization.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        varName.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }
}
