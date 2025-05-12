package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.StackCount;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.VoidType;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Line;

/**
 * @author gl13
 * @date 01/01/2025
 */
public class Main extends AbstractMain {
    private static final Logger LOG = Logger.getLogger(Main.class);

    private ListDeclVar declVariables;
    private ListInst insts;

    public Main(ListDeclVar declVariables,
            ListInst insts) {
        Validate.notNull(declVariables);
        Validate.notNull(insts);
        this.declVariables = declVariables;
        this.insts = insts;
    }

    @Override
    protected void verifyMain(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify Main: start");

        // EnvironmentExp parentEnv =
        // compiler.getEnvTypes().OBJECT.getDefinition().getMembers();
        EnvironmentExp localEnv = new EnvironmentExp(null);

        declVariables.verifyListDeclVariable(compiler, localEnv, null);
        insts.verifyListInst(compiler, localEnv, null, new VoidType(null));

        LOG.debug("verify Main: end");
    }

    @Override
    protected void codeGenMain(DecacCompiler compiler) {
        if (!declVariables.isEmpty()) {
            compiler.addComment("Beginning of variable declarations:");
            declVariables.codeGenListDeclVar(compiler);

            // notify compiler that a stack overflow error can occur
            compiler.setPossibleStackOverflow();

            // generate code for stack overflow checking
            StackCount.genCodeAllocateStack(compiler);
            compiler.addFirst(new Line("Main program stack allocation"));
        } else {
            compiler.setPossibleStackOverflow();
            StackCount.genCodeAllocateStack(compiler);
        }

        if (!insts.isEmpty()) {
            compiler.addComment("Beginning of main instructions:");
            insts.codeGenListInst(compiler);
        }
    }

    @Override
    protected void ARMCodeGenMain(DecacCompiler compiler) {
        // TODO Auto-generated method stub
        if (!declVariables.isEmpty()) {
            compiler.addComment("Beginning of variable declarations:");
            declVariables.ARMCodeGenListDeclVar(compiler);

            // notify compiler that a stack overflow error can occur
            // compiler.setPossibleStackOverflow();

            // generate code for stack overflow checking
            // StackCount.genCodeAllocateStack(compiler);
            // compiler.addFirst(new Line("Main program stack allocation"));
        } else {
            // compiler.setPossibleStackOverflow();
            // StackCount.genCodeAllocateStack(compiler);
        }

        if (!insts.isEmpty()) {
            compiler.addComment("Beginning of main instructions:");
            insts.ARMCodeGenListInst(compiler);
        }

    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.println("{");
        s.indent();
        declVariables.decompile(s);
        insts.decompile(s);
        s.unindent();
        s.println("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        declVariables.iter(f);
        insts.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        declVariables.prettyPrint(s, prefix, false);
        insts.prettyPrint(s, prefix, true);
    }
}
