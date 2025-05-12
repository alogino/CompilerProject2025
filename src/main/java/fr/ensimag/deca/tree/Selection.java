package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.deca.tree.Visibility;;

/**
 * Represents a selection like `object.attribute` or `object.method` without
 * arguments.
 */
public class Selection extends AbstractLValue {
    private final AbstractExpr object;
    private final AbstractIdentifier field;

    /**
     * Creates a new Selection instance.
     *
     * @param object the object being accessed (must not be null)
     * @param field  the field or method being accessed (must not be null)
     * @throws IllegalArgumentException if object or field is null
     */
    public Selection(AbstractExpr object, AbstractIdentifier field) {
        Validate.notNull(object, "Object in Selection cannot be null");
        Validate.notNull(field, "Field in Selection cannot be null");
        this.object = object;
        this.field = field;
    }

    public Definition getDefinition() {
        return this.definition;
    }

    protected void setDefinition(Definition def) {
        Validate.notNull(def);
        this.definition = def;
    }

    public AbstractIdentifier getField() {
        return field;
    }

    private Definition definition;

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        // Verifying the type of object we selct from
        Type objectType = object.verifyExpr(compiler, localEnv, currentClass);
        if (!objectType.isClass()) {
            throw new ContextualError("Selection must be performed on an object of a class type.", getLocation());
        }

        // Verifying the object's class' definition
        ClassDefinition objectClassDef = (ClassDefinition) compiler.environmentType
                .defOfType(objectType.getName());
        if (objectClassDef == null) {
            throw new ContextualError("Class '" + objectType.getName() + "' is not defined.", getLocation());
        }

        try {
            field.verifyExpr(compiler, objectClassDef.getMembers(), currentClass);
        } catch (ContextualError e) {
            throw new ContextualError("Field or method '" + field.getName() + "' does not exist in class '" +
                    objectType.getName() + "'.", getLocation());
        }

        if (field.getFieldDefinition().getVisibility().equals(Visibility.PROTECTED)
                && (!compiler.environmentType.isSubClass(currentClass, objectClassDef)
                        || !compiler.environmentType.isSubType(objectType, currentClass.getType()))) {
            String errorMessage = "Field '" + field.getName().getName()
                    + "' is protected and cannot be accessed outside its class or subclasses ";
            throw new ContextualError(errorMessage, getLocation());
        }
        // Vérification des restrictions d'accès et récupération du type
        Type fieldType = field.getDefinition().getType();
        setType(fieldType);
        setDefinition(field.getDefinition());
        return fieldType;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        object.codeGenInst(compiler);

        // TODO : handle case where there are no available registers

        DVal loadTarget = Register.getLastExprPos();
        DVal fieldPos = new RegisterOffset(field.getFieldDefinition().getIndex(), (GPRegister) loadTarget);

        // handle null dereference
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.setPossibleNullDereferenceError();
            compiler.addInstruction(new CMP(new NullOperand(), (GPRegister) loadTarget));
            compiler.addInstruction(new BEQ(new Label("null_dereference_error")),
                    "Handle null dereference");
        }

        // load field's value
        compiler.addInstruction(new LOAD(fieldPos, (GPRegister) loadTarget));
    }

    /**
     * Decompiles the selection expression into Deca source code.
     *
     * @param s the stream to write the decompiled code
     */
    @Override
    public void decompile(IndentPrintStream s) {
        object.decompile(s);
        s.print(".");
        field.decompile(s);
    }

    /**
     * Iterates over the child nodes of this selection.
     *
     * @param f the function to apply to each child
     */
    @Override
    protected void iterChildren(TreeFunction f) {
        object.iter(f);
        field.iter(f);
    }

    /**
     * Pretty-prints the child nodes of this selection for debugging.
     *
     * @param s      the stream to write the output
     * @param prefix the prefix for indentation
     */
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        object.prettyPrint(s, prefix, false);
        field.prettyPrint(s, prefix, true);
    }
}
