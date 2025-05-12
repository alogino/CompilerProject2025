package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.arm.pseudocode.ARMDVal;
import fr.ensimag.arm.pseudocode.ARMLabel;
import fr.ensimag.arm.pseudocode.ARMLiteral;
import fr.ensimag.arm.pseudocode.ARMRegister;
import fr.ensimag.arm.pseudocode.ARMRegisterOffset;
import fr.ensimag.arm.pseudocode.instructions.ARM_BL;
import fr.ensimag.arm.pseudocode.instructions.ARM_LDR;
import fr.ensimag.arm.pseudocode.instructions.ARM_POP;
import fr.ensimag.arm.pseudocode.instructions.ARM_PUSH;
import fr.ensimag.arm.pseudocode.instructions.ARM_TVMOV;
import fr.ensimag.arm.pseudocode.instructions.ARM_VCVTF64F32;
import fr.ensimag.arm.pseudocode.instructions.ARM_VLDR;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.ARMDataSection;
import fr.ensimag.deca.codegen.StackCount;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WFLOATX;
import fr.ensimag.ima.pseudocode.instructions.WINT;

/**
 * Deca Identifier
 *
 * @author gl13
 * @date 01/01/2025
 */
public class Identifier extends AbstractIdentifier {

    @Override
    protected void checkDecoration() {
        if (getDefinition() == null) {
            throw new DecacInternalError("Identifier " + this.getName() + " has no attached Definition");
        }
    }

    @Override
    public Definition getDefinition() {
        return definition;
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * ClassDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *                            if the definition is not a class definition.
     */
    @Override
    public ClassDefinition getClassDefinition() {
        try {
            return (ClassDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a class identifier, you can't call getClassDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * MethodDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *                            if the definition is not a method definition.
     */
    @Override
    public MethodDefinition getMethodDefinition() {
        try {
            return (MethodDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a method identifier, you can't call getMethodDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * FieldDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *                            if the definition is not a field definition.
     */
    @Override
    public FieldDefinition getFieldDefinition() {
        try {
            return (FieldDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a field identifier, you can't call getFieldDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * VariableDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *                            if the definition is not a field definition.
     */
    @Override
    public VariableDefinition getVariableDefinition() {
        try {
            return (VariableDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a variable identifier, you can't call getVariableDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * ExpDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *                            if the definition is not a field definition.
     */
    @Override
    public ExpDefinition getExpDefinition() {
        try {
            return (ExpDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a Exp identifier, you can't call getExpDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * ExpDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *                            if the definition is not a field definition.
     */
    @Override
    public ParamDefinition getParamDefinition() {
        try {
            return (ParamDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a Param identifier, you can't call getParamDefinition on it");
        }
    }

    @Override
    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    @Override
    public Symbol getName() {
        return name;
    }

    private Symbol name;

    public Identifier(Symbol name) {
        Validate.notNull(name);
        this.name = name;
    }

    /**
     * Implements contextual verifcation in the case the identifier is used as
     * expression (not a type)
     */
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        if (this.name == null) {
            throw new ContextualError("The identifier's name cannot be null.", getLocation());
        }

        // when identifier is field, we need more information (similar to selection)

        setDefinition(localEnv.get(getName(), getLocation()));
        setType(getDefinition().getType());

        if (definition.isField()) {
            FieldDefinition bruh = (FieldDefinition) definition;
        }

        return getType();
    }

    /**
     * Implements contextual verifcation in the case the identifier is used as
     * method (not a type nor an expression)
     */
    public Type verifyMethod(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        if (this.name == null) {
            throw new ContextualError("The identifier's name cannot be null.", getLocation());
        }
        Symbol methodSymb = compiler.createSymbol(getName().getName() + ".m");

        setDefinition(localEnv.get(methodSymb, getLocation()));
        setType(getDefinition().getType());

        // else {
        // Definition memberDef = currentClass.getMembers().get(name, getLocation());
        // Type memberType = memberDef.getType();
        // setDefinition(memberDef);
        // setType(memberType);
        // }

        return getType();
    }

    /**
     * Implements non-terminal "type" of [SyntaxeContextuelle] in the 3 passes
     * 
     * @param compiler contains "env_types" attribute
     */
    @Override
    public Type verifyType(DecacCompiler compiler) throws ContextualError {

        // Assert that the name of the identifier is not null
        if (this.name == null) {
            throw new ContextualError("The identifier's name cannot be null.", getLocation());
        }

        // Check if the type is defined in the compiler's environment
        if (compiler.environmentType.defOfType(this.name) == null) {
            throw new ContextualError("The type '" + this.name + "' is not defined in the environment of types",
                    getLocation());
        }

        // Retrieve the type definition
        Definition typeDef = compiler.environmentType.defOfType(name);
        if (typeDef == null) {
            throw new ContextualError("The type definition for '" + this.name + "' is invalid or incomplete.",
                    getLocation());
        }
        Type type = typeDef.getType();

        // set and return the type
        setType(type);
        setDefinition(typeDef);
        return type;
    }

    @Override
    public AbstractExpr verifyRValue(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass,
            Type expectedType) throws ContextualError {
        // set this identifier's definition to the declared identifier with same name's
        // definition
        this.verifyExpr(compiler, localEnv, currentClass);

        // type check
        if (this.getType().isInt() && expectedType.isFloat()) {
            // "cast" this rvalue to a float from an int
        } else if (!this.getType().sameType(expectedType)) {
            // contextual error
            String errorMessage = String.format("Cannot assign expression of type '%s' to a variable of type '%s'",
                    this.getType(), expectedType);
            throw new ContextualError(errorMessage, getLocation());
        }
        return this;
    }

    @Override
    protected void codeGenPrint(DecacCompiler compiler, boolean printHex) {
        // in R1, we load the value of the expression
        DVal loadTarget;
        if (getDefinition().isParam()) {
            loadTarget = new RegisterOffset(-2 - getParamDefinition().getIndex(), Register.LB);
        } else if (getDefinition().isField()) {
            loadTarget = Register.getUnusedR();

            compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), (GPRegister) loadTarget));

            DVal fieldPos = new RegisterOffset(getFieldDefinition().getIndex(), (GPRegister) loadTarget);
            compiler.addInstruction(new LOAD(fieldPos, (GPRegister) loadTarget));
            Register.setLastExprPos(new RegisterOffset(0, (GPRegister) loadTarget));
        } else {
            loadTarget = getVariableDefinition().getOperand();
        }
        compiler.addInstruction(new LOAD(loadTarget, Register.R1));

        // in case of a ConvFloat
        Register.setLastExprPos(new RegisterOffset(0, Register.R1));

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

    @Override
    protected void ARMCodeGenPrint(DecacCompiler compiler, boolean printHex) {
        // TODO : manage objects if time

        ARMDVal loadTarget = null;
        if (getDefinition().isParam()) {
            // TODO
        } else if (getDefinition().isField()) {
            // TODO
        } else {
            loadTarget = getVariableDefinition().getARMOperand();
        }

        // Print in case of int
        if (getType().isInt()) {
            ARMDataSection.setIntPrint();

            // Store R0 and R1 if need be
            if (ARMRegister.isUsed(0)) {
                compiler.addARMInstruction(new ARM_PUSH(ARMRegister.R0));
            }
            if (ARMRegister.isUsed(1)) {
                compiler.addARMInstruction(new ARM_PUSH(ARMRegister.R1));
            }

            compiler.addARMInstruction(new ARM_LDR(ARMRegister.R0, new ARMLiteral("int_format")));
            compiler.addARMInstruction(new ARM_LDR(ARMRegister.R1, loadTarget));
            compiler.addARMInstruction(new ARM_BL(new ARMLabel("printf")));

            // Restore R0 and R1 if need be
            if (ARMRegister.isUsed(0)) {
                compiler.addARMInstruction(new ARM_POP(ARMRegister.R0));
            }
            if (ARMRegister.isUsed(1)) {
                compiler.addARMInstruction(new ARM_POP(ARMRegister.R1));
            }
        } else if (getType().isFloat()) {
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

            compiler.addARMInstruction(new ARM_LDR(ARMRegister.R0, new ARMLiteral("float_format")));
            compiler.addARMInstruction(new ARM_LDR(ARMRegister.R3, loadTarget));
            compiler.addARMInstruction(new ARM_VLDR(ARMRegister.S31, new ARMRegisterOffset(0, ARMRegister.R3)));
            compiler.addARMInstruction(new ARM_VCVTF64F32(ARMRegister.D15, ARMRegister.S31));
            compiler.addARMInstruction(new ARM_TVMOV(ARMRegister.R2, ARMRegister.R3, ARMRegister.D15));
            compiler.addARMInstruction(new ARM_BL(new ARMLabel("printf")));

            // Restore R0, R2 and R3 if need be
            if (ARMRegister.isUsed(0)) {
                compiler.addARMInstruction(new ARM_POP(ARMRegister.R0));
            }
            if (ARMRegister.isUsed(2)) {
                compiler.addARMInstruction(new ARM_POP(ARMRegister.R2));
            }
            if (ARMRegister.isUsed(3)) {
                compiler.addARMInstruction(new ARM_POP(ARMRegister.R3));
            }
        }
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        if (getDefinition().isField()) {
            codeGenInstField(compiler);
            return;
        }

        GPRegister unusedReg = Register.getUnusedR();

        DVal loadTarget;
        if (getDefinition().isParam()) {
            loadTarget = new RegisterOffset(-2 - getParamDefinition().getIndex(), Register.LB);
        } else if (getDefinition().isField()) {
            loadTarget = getFieldDefinition().getOperand();
        } else {
            loadTarget = getVariableDefinition().getOperand();
        }

        if (unusedReg != null) {
            // try to use an unused GPRegister
            Register.setLastExprPos(new RegisterOffset(0, unusedReg));
            compiler.addInstruction(new LOAD(loadTarget, unusedReg));
        } else {
            // if no GPRegister is unused, push to stack as a temp variable
            Register.setLastExprPos(new RegisterOffset(0, Register.SP));
            compiler.addInstruction(new LOAD(loadTarget, Register.R1));
            compiler.addInstruction(new PUSH(Register.R1));
            StackCount.countPush();
        }
    }

    private void codeGenInstField(DecacCompiler compiler) {
        GPRegister loadTarget = Register.getUnusedR();

        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), loadTarget));

        DVal fieldPos = new RegisterOffset(getFieldDefinition().getIndex(), (GPRegister) loadTarget);
        compiler.addInstruction(new LOAD(fieldPos, loadTarget));
        Register.setLastExprPos(new RegisterOffset(0, loadTarget));
    }

    private Definition definition;

    @Override
    protected void ARMCodeGenInst(DecacCompiler compiler) {
        // TODO Auto-generated method stub

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
    public void decompile(IndentPrintStream s) {
        s.print(name.toString());
    }

    @Override
    String prettyPrintNode() {
        if (getDefinition() != null && getLocation().getFilename().equals("<builtin>")) {
            return "[builtin] Identifier (" + getName() + ")";
        }
        return "Identifier (" + getName() + ")";
    }

    @Override
    protected void prettyPrintType(PrintStream s, String prefix) {
        // System.out.println("pretty print identifier called");
        Definition d = getDefinition();
        if (d != null) {
            s.print(prefix);
            s.print("definition: ");
            s.print(d);
            s.println();
        }
    }

}
