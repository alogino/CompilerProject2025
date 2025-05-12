package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.Type;

import java.io.PrintStream;

/**
 * Represents the body of a method.
 */
public class MethodBody extends AbstractMethodBody {
    private final ListDeclVar decls;
    private final ListInst insts;
    private EnvironmentExp methodEnvExp;

    public MethodBody(ListDeclVar decls, ListInst insts) {
        this.decls = decls;
        this.insts = insts;
    }

    public ListDeclVar getDecls() {
        return decls;
    }

    public ListInst getInsts() {
        return insts;
    }

    public void setMethodEnvExp(EnvironmentExp methodEnvExp) {
        this.methodEnvExp = methodEnvExp;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.println("{");
        s.indent();
        decls.decompile(s);
        insts.decompile(s);
        s.unindent();
        s.println("}");
    }

    @Override
    public void verifyMethodBody(DecacCompiler compiler, ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        decls.verifyListDeclVariable(compiler, methodEnvExp, currentClass);
        insts.verifyListInst(compiler, methodEnvExp, currentClass, returnType);
    }

    @Override
    public void codeGenInst(DecacCompiler compiler) {
        compiler.addComment("Start of method body");
        decls.codeGenListDeclVar(compiler);
        insts.codeGenListInst(compiler);
        compiler.addComment("End of method body");
    }

    @Override
    public void ARMCodeGenInst(DecacCompiler compiler) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        decls.prettyPrint(s, prefix, false);
        insts.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        decls.iter(f);
        insts.iter(f);
    }
}
