package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * List of expressions (eg list of parameters).
 *
 * @author gl13
 * @date 01/01/2025
 */
public class ListExpr extends TreeList<AbstractExpr> {

    public void verifyListExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType) throws ContextualError {
        for (AbstractExpr expr : getList()) {
            expr.setType(expr.verifyExpr(compiler, localEnv, currentClass));
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        boolean first = true;
        for (AbstractExpr expr : getList()) {
            if (!first) {
                s.print(", ");
            }
            expr.decompile(s);
            first = false;
        }

    }
}
