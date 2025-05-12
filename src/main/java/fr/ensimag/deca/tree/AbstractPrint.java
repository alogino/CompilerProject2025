package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * Print statement (print, println, ...).
 *
 * @author gl13
 * @date 01/01/2025
 */
public abstract class AbstractPrint extends AbstractInst {

    private boolean printHex;
    private ListExpr arguments = new ListExpr();

    abstract String getSuffix();

    public AbstractPrint(boolean printHex, ListExpr arguments) {
        Validate.notNull(arguments);
        this.arguments = arguments;
        this.printHex = printHex;
    }

    public ListExpr getArguments() {
        return arguments;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType) throws ContextualError {
        getArguments().verifyListExpr(compiler, localEnv, currentClass, returnType);

        for (AbstractExpr expr : getArguments().getList()) {
            Type exprType = expr.getType();
            if (!exprType.isFloat() && !exprType.isInt() && !exprType.isString()) {
                String errorMessage = String.format(
                        "print%s%s: invalid argument type '%s', expected 'int', 'float', or 'string'",
                        this.getSuffix(), this.getPrintHex() ? "x" : "", expr.getType().toString());
                throw new ContextualError(errorMessage, expr.getLocation());
            }
        }
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        for (AbstractExpr a : getArguments().getList()) {
            a.codeGenPrint(compiler, getPrintHex());
        }
    }

    @Override
    protected void ARMCodeGenInst(DecacCompiler compiler) {
        for (AbstractExpr a : getArguments().getList()) {
            a.ARMCodeGenPrint(compiler, getPrintHex());
        }

    }

    private boolean getPrintHex() {
        return printHex;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("print" + getSuffix());
        if (getPrintHex()) {
            s.print("x");
        }
        s.print("(");
        for (AbstractExpr expr : getArguments().getList()) {
            if (getPrintHex() && expr instanceof FloatLiteral) {
                ((FloatLiteral) expr).setHex(true);
            }
        }
        getArguments().decompile(s);
        s.print(");");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        arguments.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        arguments.prettyPrint(s, prefix, true);
    }

}
