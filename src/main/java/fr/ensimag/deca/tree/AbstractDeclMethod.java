package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * Base class for method declarations in a class.
 */
public abstract class AbstractDeclMethod extends AbstractDeclClassBody {

    /**
     * Verify the Method declaration during the contextual verification phase.
     *
     * @param compiler The Deca compiler instance.
     * @param classDef the class definition for the class in which this method is
     *                 defined
     * @throws ContextualError if there is a problem with the methode declaration.
     */
    public abstract MethodDefinition verifyDeclMethod(DecacCompiler compiler, ClassDefinition classDef)
            throws ContextualError;

    /**
     * Verify the Method declaration during the contextual verification phase.
     *
     * @param compiler     The Deca compiler instance.
     * @param currentClass The class in which this DeclMethod resides
     *
     */
    public abstract void verifyMethodBody(DecacCompiler compiler, ClassDefinition currentClass)
            throws ContextualError;

    /**
     * Decompile the methode declaration.
     *
     * @param s The stream to write the decompiled methode to.
     */
    @Override
    public abstract void decompile(IndentPrintStream s);

    public int index;

    public int getMethodIndex() {
        return index;
    }

    /**
     * Generates the code for the method body:
     * - Allocates space for method parameters.
     * - Generates instructions for the method body.
     * - Handles method return by loading the return value into R0 and adding the
     * RTS instruction.
     *
     * @param compiler The compiler used to generate the code.
     */
    public abstract void codeGenMethodBody(DecacCompiler compiler);

    public abstract void ARMCodeGenMethodBody(DecacCompiler compiler);
}
