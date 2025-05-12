package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

import java.io.PrintStream;

/**
 * Represents the 'this' keyword in Deca, referring to the current instance.
 */
public class This extends AbstractExpr {

    public This() {
        // nothing
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        if (currentClass == null) {
            throw new ContextualError("'this' can only be used within a class context.", getLocation());
        }
        setType(currentClass.getType());
        return currentClass.getType();

    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // `this`, meaning the current instance of the class, is always at -2(LB)

        // TODO : handle case where no available registers

        GPRegister loadTarget = Register.getUnusedR();
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), loadTarget));
        Register.setLastExprPos(new RegisterOffset(0, loadTarget));
    }

    /**
     * Outputs the "this" keyword to the given stream.
     *
     * @param s the stream used for output
     */
    @Override
    public void decompile(IndentPrintStream s) {
        s.print("this");
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
