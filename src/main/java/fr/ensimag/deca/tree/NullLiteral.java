package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * Represents the 'null' literal in Deca.
 */
public class NullLiteral extends AbstractExpr {

    public NullLiteral() {
        // nothing
    }


    // TO DO : pour la partie avec Objet
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
                           ClassDefinition currentClass) throws ContextualError {
        this.setType(compiler.environmentType.NULL);
        return compiler.environmentType.NULL;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("null");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // nothing
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // nothing
    }
}
