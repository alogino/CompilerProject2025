package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.StackCount;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;

import java.io.PrintStream;

/**
 * Represents a specific field declaration.
 */
public class DeclField extends AbstractDeclField {
    private final AbstractIdentifier typeIdentifier;
    private final AbstractIdentifier varIdentifier;
    private final AbstractInitialization initialization;
    private final Visibility visibility;

    public DeclField(AbstractIdentifier typeIdentifier, AbstractIdentifier varIdentifier,
            AbstractInitialization initialization, Visibility visibility) {
        this.typeIdentifier = typeIdentifier;
        this.varIdentifier = varIdentifier;
        this.initialization = initialization;
        this.visibility = visibility;
    }

    @Override
    public void verifyClassBody(DecacCompiler compiler) throws ContextualError {
        throw new UnsupportedOperationException("to implement");
    }

    @Override
    public void decompile(IndentPrintStream s) {
        if (visibility != null && visibility != Visibility.PUBLIC) {
            s.print(visibility.toString().toLowerCase() + " ");
        }
        typeIdentifier.decompile(s);
        s.print(" ");
        varIdentifier.decompile(s);
        initialization.decompile(s);
        s.println(";");
        // s.print(typeIdentifier.getName() + " " + varIdentifier + ";");

    }

    private FieldDefinition fieldDef;
    private Type fieldType;

    @Override
    public void verifyFieldType(DecacCompiler compiler, ClassDefinition currentClass) throws ContextualError {
        fieldType = typeIdentifier.verifyType(compiler);

        // Ensure the type is valid for a field declaration
        if (fieldType.isVoid()) {
            throw new ContextualError("Invalid type for field declaration: " + fieldType, this.getLocation());
        }

        currentClass.incNumberOfFields();
        fieldDef = new FieldDefinition(fieldType, getLocation(), visibility, currentClass,
                currentClass.getNumberOfFields());
        varIdentifier.setDefinition(fieldDef);
        varIdentifier.setType(fieldType);

        try {
            currentClass.getMembers().declare(varIdentifier.getName(), varIdentifier.getFieldDefinition());
        } catch (DoubleDefException e) {
            String errorMessage = String.format("The identifier '%s' is already in use in its context.",
                    varIdentifier.getName().getName());
            throw new ContextualError(errorMessage, getLocation());
        }

        // Set field as initialized or not
        if (initialization instanceof Initialization) {
            varIdentifier.getFieldDefinition().setInitializationStatus(true);
        }
    }

    @Override
    public void codeGenDeclField(DecacCompiler compiler) {
        // attribute local stack position of variable
        fieldDef.setOperand(new RegisterOffset(StackCount.incVarCount(), Register.GB));

        // generate the initialization code if field has been initialized
        if (initialization.isInitialized()) {
            codeGenDeclFieldInitialization(compiler);
        }
    }

    /**
     * Generates assembly code for initialization of a field declaration that was
     * initialied
     */
    private void codeGenDeclFieldInitialization(DecacCompiler compiler) {
        AbstractExpr fieldExpr = ((Initialization) initialization).getExpression();
        fieldExpr.codeGenInst(compiler);

        // TODO : adapt for POP and PUSH?

        DVal loadTarget = Register.getLastExprPos();
        compiler.addInstruction(new STORE((Register) loadTarget,
                new RegisterOffset(fieldDef.getIndex(), Register.R1)));
        Register.setUnused((GPRegister) loadTarget);
    }

    @Override
    public void codeGenDeclFieldDummy(DecacCompiler compiler) {
        // TODO : adapt for POP and PUSH?

        // initialize with 0 for ints, 0.0 for floats, and false for booleans
        DVal loadLiteral;
        if (fieldType.isInt() || fieldType.isBoolean()) {
            loadLiteral = new ImmediateInteger(0);
            compiler.addInstruction(new LOAD(loadLiteral, Register.R0));
        } else if (fieldType.isFloat()) {
            loadLiteral = new ImmediateFloat(0);
            compiler.addInstruction(new LOAD(loadLiteral, Register.R0));
        }
        // store the initialization
        if (fieldType.isInt() || fieldType.isFloat() || fieldType.isBoolean()) {
            compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(
                    fieldDef.getIndex(), Register.R1)));
        }
    }

    @Override
    public void ARMCodeGenDeclField(DecacCompiler compiler) {
        // TODO Auto-generated method stub

    }

    @Override
    public void ARMCodeGenDeclFieldDummy(DecacCompiler compiler) {
        // TODO Auto-generated method stub

    }

    @Override
    public void verifyFieldBody(DecacCompiler compiler, ClassDefinition currentClass)
            throws ContextualError {
        initialization.verifyInitialization(compiler, varIdentifier.getType(), currentClass.getMembers(), currentClass);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        typeIdentifier.prettyPrint(s, prefix, false);
        varIdentifier.prettyPrint(s, prefix, false);
        if (initialization != null) {
            initialization.prettyPrint(s, prefix, true);
        }
    }

    @Override
    String prettyPrintNode() {
        String var = super.prettyPrintNode();
        String visibilityString = visibility != null ? "[visibility=" + visibility.toString().toUpperCase() + "]" : "";
        return visibilityString + " " + var;
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        typeIdentifier.iter(f);
        varIdentifier.iter(f);
        initialization.iter(f);
    }
}
