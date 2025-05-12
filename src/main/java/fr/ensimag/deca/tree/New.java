package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.NEW;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.STORE;

/**
 * Represents the `new` keyword for object instantiation in Deca.
 */
public class New extends AbstractExpr {
    private final AbstractIdentifier className;

    public New(AbstractIdentifier className) {
        this.className = className;
    }

    public AbstractIdentifier getClassName() {
        return className;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type clType = className.verifyType(compiler);
        setType(clType);
        return clType;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // allocate memory for new object in the heao
        String comment = String.format("allocation of new instance of %s", className.toString().toString());

        // TODO : handle case where no register is unused

        // allocate memory for new instance of the class
        GPRegister loadTarget = Register.getUnusedR();
        int allocSize = className.getClassDefinition().getNumberOfFields() + 1;
        compiler.addInstruction(new NEW(new ImmediateInteger(allocSize), loadTarget), comment);

        // handle full_heap error
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new BOV(new Label("full_heap_error")));
            compiler.setPossibleFullHeapError();
        }

        Register.setLastExprPos(new RegisterOffset(0, loadTarget));

        compiler.addInstruction(new LEA(className.getClassDefinition().getVBTableOffset(), Register.R0));
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(0, loadTarget)));

        // jump to init.<ClassName>
        Label classInitLabel = new Label(
                String.format("init.%s", className.getClassDefinition().getType().getName().getName()));
        compiler.addInstruction(new PUSH(loadTarget));
        compiler.addInstruction(new BSR(new LabelOperand(classInitLabel)));
        compiler.addInstruction(new POP(loadTarget));
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("new ");
        className.decompile(s);
        s.print("()");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        className.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        className.prettyPrint(s, prefix, true);
    }
}
