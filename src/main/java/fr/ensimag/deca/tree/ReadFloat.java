package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.RFLOAT;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.deca.codegen.StackCount;

/**
 *
 * @author gl13
 * @date 01/01/2025
 */
public class ReadFloat extends AbstractReadExpr {

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        this.setType(compiler.environmentType.FLOAT);
        return compiler.environmentType.FLOAT;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("readFloat()");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        GPRegister tempRegister = Register.getUnusedR();

        compiler.addInstruction(new RFLOAT());
        compiler.setPossibleIOError();

        compiler.addInstruction(new BOV(new Label("IO_error")));

        if (tempRegister == null) {
            compiler.addInstruction(new PUSH(Register.R1));
            StackCount.countPush();

            RegisterOffset tempRegisterOffset = new RegisterOffset(0, Register.SP);
            Register.setLastExprPos(tempRegisterOffset);
        } else {
            compiler.addInstruction(new LOAD(Register.R1, tempRegister));

            RegisterOffset tempRegisterOffset = new RegisterOffset(0, tempRegister);
            Register.setLastExprPos(tempRegisterOffset);

            Register.setUnused(tempRegister);
        }

    }

    @Override
    protected void ARMCodeGenInst(DecacCompiler compiler) {
        // TODO Auto-generated method stub

    }
}
