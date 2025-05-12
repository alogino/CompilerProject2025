package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;

/**
 * Abstract class for method parameters.
 */
public abstract class AbstractDeclParam extends AbstractParam {

    /**
     * Verify the parameter declaration in the context of the class' EnvironmentExp
     *
     * @param compiler     the compiler instance
     * @param methodEnvExp the method's EnvironmentExp
     */
    protected abstract Type verifyDeclParam(DecacCompiler compiler, EnvironmentExp methodEnvExp)
            throws ContextualError;

    protected abstract void codeGenDeclParam(DecacCompiler compiler);

    protected abstract void ARMCodeGenDeclParam(DecacCompiler compiler);
}
