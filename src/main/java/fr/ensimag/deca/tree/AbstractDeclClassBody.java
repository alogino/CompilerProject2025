package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;


import java.io.PrintStream;

/**
 * Common interface for all class body declarations (methods and fields).
 */
public abstract class AbstractDeclClassBody extends Tree {
    public abstract void verifyClassBody(DecacCompiler compiler) throws ContextualError;
}
