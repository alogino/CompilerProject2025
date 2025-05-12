package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.WINT;
import fr.ensimag.ima.pseudocode.instructions.WNL;

/**
 * Represents an instanceof expression in Deca.
 */
public class InstanceOf extends AbstractExpr {
    private final AbstractExpr expr;
    private final AbstractIdentifier targetIdentifier;

    public InstanceOf(AbstractExpr expr, AbstractIdentifier targetIdentifier) {
        this.expr = expr;
        this.targetIdentifier = targetIdentifier;
    }

    public AbstractExpr getExpr() {
        return expr;
    }

    public AbstractIdentifier getTargetType() {
        return targetIdentifier;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type targetType = targetIdentifier.verifyType(compiler);
        Type exprType = expr.verifyExpr(compiler, localEnv, currentClass);
        if (!targetType.isClass() || !(exprType.isClass() || exprType.isNull())) {
            throw new ContextualError("Invalid operands for 'instanceof' binary operator", getLocation());
        }
        setType(compiler.environmentType.BOOLEAN);
        return compiler.environmentType.BOOLEAN;
    }

    private static int instanceOfCount = 0;

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        instanceOfCount++;
        Label checkSuperClassLabel = new Label(String.format("loop_instanceof.%d", instanceOfCount));
        Label notInstanceOfLabel = new Label(String.format("not_instanceof.%d", instanceOfCount));
        Label instanceOfLabel = new Label(String.format("is_instanceof.%d", instanceOfCount));
        Label endLabel = new Label(String.format("end_instanceof.%d", instanceOfCount));

        // instance address (verified correct, trust)
        expr.codeGenInst(compiler);
        compiler.addInstruction(new LOAD(Register.getLastExprPos(), Register.R1));
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.R1), Register.R1));

        // target VTable entry address
        RegisterOffset targetVTEntry = targetIdentifier.getClassDefinition().getVBTableOffset();
        compiler.addInstruction(new LEA(targetVTEntry, Register.R0));
        compiler.addInstruction(new CMP(Register.R0, Register.R1));
        compiler.addInstruction(new BEQ(instanceOfLabel));

        // while loop
        compiler.addLabel(checkSuperClassLabel);
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.R1), Register.R1));

        compiler.addInstruction(new CMP(new NullOperand(), Register.R1));
        compiler.addInstruction(new BEQ(notInstanceOfLabel));

        compiler.addInstruction(new CMP(Register.R0, Register.R1));
        compiler.addInstruction(new BEQ(instanceOfLabel));

        compiler.addInstruction(new BRA(checkSuperClassLabel));

        compiler.addLabel(instanceOfLabel);
        compiler.addInstruction(new LOAD(new ImmediateInteger(1), Register.R0));
        compiler.addInstruction(new BRA(endLabel));

        compiler.addLabel(notInstanceOfLabel);
        compiler.addInstruction(new LOAD(new ImmediateInteger(0), Register.R0));

        compiler.addLabel(endLabel);
        Register.setLastExprPos(new RegisterOffset(0, Register.R0));
    }

    @Override
    public void decompile(IndentPrintStream s) {
        expr.decompile(s);
        s.print(" instanceof ");
        s.print(targetIdentifier.getName().toString());
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expr.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expr.prettyPrint(s, prefix, false);
        targetIdentifier.prettyPrint(s, prefix, true);
    }
}
