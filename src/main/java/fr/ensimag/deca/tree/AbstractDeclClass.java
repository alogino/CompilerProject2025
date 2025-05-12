package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Class declaration.
 *
 * @author gl13
 * @date 01/01/2025
 */
public abstract class AbstractDeclClass extends Tree {
    /**
     * Pass 1 of [SyntaxeContextuelle]. Verify that the class declaration is OK
     * without looking at its content.
     */
    protected abstract void verifyClass(DecacCompiler compiler)
            throws ContextualError;

    /**
     * Pass 2 of [SyntaxeContextuelle]. Verify that the class members (fields and
     * methods) are OK, without looking at method body and field initialization.
     */
    protected abstract void verifyClassMembers(DecacCompiler compiler)
            throws ContextualError;

    /**
     * Pass 3 of [SyntaxeContextuelle]. Verify that instructions and expressions
     * contained in the class are OK.
     */
    protected abstract void verifyClassBody(DecacCompiler compiler)
            throws ContextualError;

    /**
     * Generates the method and field tables for the class, ensuring proper
     * organization for runtime execution.
     *
     * @param compiler The compiler instance used to generate the tables.
     */
    public abstract void codeGenTable(DecacCompiler compiler);

    /**
     * Generates the method and field tables for the class, ensuring proper
     * organization for runtime execution.
     *
     * @param compiler The compiler instance used to generate the tables.
     */
    public abstract void ARMCodeGenTable(DecacCompiler compiler);

    /**
     * Generates the assembly code for the class, including its methods and field
     * initializations, as well as its constructor.
     *
     * @param compiler The compiler instance used to generate the code.
     */
    public abstract void codeGenClass(DecacCompiler compiler);

    /**
     * Generates the assembly code for the class, including its methods and field
     * initializations, as well as its constructor.
     *
     * @param compiler The compiler instance used to generate the code.
     */
    public abstract void ARMCodeGenClass(DecacCompiler compiler);

    /**
     * Generate the assembly code for the class initialization
     *
     * @param compiler The compiler instance used to generate the code.
     */
    public abstract void codeGenClassInit(DecacCompiler compiler);

    /**
     * Generate the assembly code for the class initialization
     *
     * @param compiler The compiler instance used to generate the code.
     */
    public abstract void ARMCodeGenClassInit(DecacCompiler compiler);
}
