package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import java.io.PrintStream;

/**
 * Abstract base class for method bodies.
 */
public abstract class AbstractMethodBody extends Tree {

    /**
     * Verify the method body in the given context.
     *
     * @param compiler     the compiler
     * @param currentClass the class in which this method's body resides
     */
    public abstract void verifyMethodBody(DecacCompiler compiler, ClassDefinition currentClass, Type returnType)
            throws ContextualError;

    /**
     * Generate the code for the method body.
     *
     * @param compiler the compiler
     */
    public void codeGenMethodBody(DecacCompiler compiler) {
        compiler.addComment("Start of method body");
        codeGenInst(compiler);
        compiler.addComment("End of method body");
    }

    @Override
    protected abstract void prettyPrintChildren(PrintStream s, String prefix);

    @Override
    protected abstract void iterChildren(TreeFunction f);

    public abstract void codeGenInst(DecacCompiler compiler);

    public abstract void ARMCodeGenInst(DecacCompiler compiler);

    public abstract void setMethodEnvExp(EnvironmentExp methodEnvExp);
}
