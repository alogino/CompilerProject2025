package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.HelperInfo;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

/**
 *
 * @author gl13
 * @date 01/01/2025
 */
public class Return extends AbstractInst {
    private AbstractExpr ret;

    public Return(AbstractExpr ret) {
        Validate.notNull(ret);
        this.ret = ret;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // evaluate rExp
        ret.codeGenInst(compiler);

        // load rExp in R0
        compiler.addInstruction(new LOAD(Register.getLastExprPos(), Register.R0));
        Register.setLastExprPos(new RegisterOffset(0, Register.R0));

        // exit without error, we are returning
        Label methodEndLabel = new Label(
                HelperInfo.getCurrentMethod().getMethodDefinition().getLabel().toString().replace("code", "end"));
        compiler.addInstruction(new BRA(methodEndLabel));
    }

    @Override
    protected void ARMCodeGenInst(DecacCompiler compiler) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType) throws ContextualError {
        ret.verifyRValue(compiler, localEnv, currentClass, returnType);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("return ");
        ret.decompile(s);
        s.print(";");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        ret.iterChildren(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        ret.prettyPrint(s, prefix, true);
    }

}
