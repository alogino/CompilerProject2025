package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * Base class for field declarations in a class.
 */
public abstract class AbstractDeclField extends AbstractDeclClassBody {

    /**
     * Verify the field declaration during the contextual verification phase.
     *
     * @param compiler     The Deca compiler instance.
     * @param currentClass The class in which this DeclField resides
     *
     */
    public abstract void verifyFieldBody(DecacCompiler compiler, ClassDefinition currentClass)
            throws ContextualError;

    /**
     * Verify the field declaration during pass 2 of contextual verrification phase.
     *
     * @param compiler     the Deca compiler instance.
     * @param currentClass the current class definition.
     */
    public abstract void verifyFieldType(DecacCompiler compiler, ClassDefinition currentClass)
            throws ContextualError;

    /**
     * Decompile the field declaration.
     *
     * @param s The stream to write the decompiled field to.
     */
    @Override
    public abstract void decompile(IndentPrintStream s);

    /**
     * Generate the assembly for a field declaration
     */
    public abstract void codeGenDeclField(DecacCompiler compiler);

    /**
     * Generate the ARM assembly for a field declaration
     */
    public abstract void ARMCodeGenDeclField(DecacCompiler compiler);

    /**
     * Generates assembly code for initialization of field declaration that was not
     * initialized
     */
    public abstract void codeGenDeclFieldDummy(DecacCompiler compiler);

    /**
     * Generates ARM assembly code for initialization of field declaration that was
     * not initialized
     */
    public abstract void ARMCodeGenDeclFieldDummy(DecacCompiler compiler);
}
