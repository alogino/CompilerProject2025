package fr.ensimag.deca.tree;

import fr.ensimag.arm.pseudocode.*;
import fr.ensimag.arm.pseudocode.instructions.ARM_B;
import fr.ensimag.arm.pseudocode.instructions.ARM_BEQ;
import fr.ensimag.arm.pseudocode.instructions.ARM_CMP;
import fr.ensimag.arm.pseudocode.instructions.ARM_POP;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.deca.codegen.StackCount;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

/**
 * Full if/else if/else statement.
 *
 * @author gl13
 * @date 01/01/2025
 */
public class IfThenElse extends AbstractInst {

    private static int ifClauseCount = -1;
    private final AbstractExpr condition;
    private final ListInst thenBranch;
    private ListInst elseBranch;
    // private List<IfThenElse> elseIfBranches = new ArrayList<>();

    public IfThenElse(AbstractExpr condition, ListInst thenBranch, ListInst elseBranch) {
        Validate.notNull(condition);
        Validate.notNull(thenBranch);
        Validate.notNull(elseBranch);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public void addElseIfBranch(IfThenElse elseif) {
        this.elseBranch.add(elseif);
    }

    public void setElseBranch(ListInst elseBranch) {
        this.elseBranch = elseBranch;
    }

    public ListInst getElseBranch() {
        return elseBranch;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        Type condType = this.condition.verifyExpr(compiler, localEnv, currentClass);
        this.condition.setType(condType);
        this.condition.verifyCondition(compiler, localEnv, currentClass);
        // for (IfThenElse elseIf : elseIfBranches) {
        // elseIf.verifyInst(compiler, localEnv, currentClass, returnType);
        // }
        this.thenBranch.verifyListInst(compiler, localEnv, currentClass, returnType);
        this.elseBranch.verifyListInst(compiler, localEnv, currentClass, returnType);
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // update numbering for labels
        ifClauseCount++;

        // ------------ Generate branch conditions
        // if condition
        Label ifLabel = new Label(String.format("if.%h", ifClauseCount));

        condition.codeGenInst(compiler);
        DVal ifConditionResult = Register.getLastExprPos();

        if (ifConditionResult.equals(Register.SP)) {
            compiler.addInstruction(new POP(Register.R1));
            StackCount.countPop();
            compiler.addInstruction(new CMP(new ImmediateInteger(1), Register.R1));
        } else {
            compiler.addInstruction(new CMP(new ImmediateInteger(1), (GPRegister) ifConditionResult));
            Register.setUnused((GPRegister) ifConditionResult);
        }

        compiler.addInstruction(new BEQ(ifLabel));

        // else (conditionless)
        Label elseLabel = new Label(String.format("else.%h", ifClauseCount));
        compiler.addInstruction(new BRA(elseLabel));

        // ------------ Generate instructions for branches

        // end_if label, every branch jumps to this label after all its instructions are
        // executed
        Label endIfLabel = new Label(String.format("end_if.%h", ifClauseCount));

        // labels and their instructions

        // if instructions
        compiler.addLabel(ifLabel);
        thenBranch.codeGenListInst(compiler);
        compiler.addInstruction(new BRA(endIfLabel));

        // else instructions
        compiler.addLabel(elseLabel);
        elseBranch.codeGenListInst(compiler);
        compiler.addInstruction(new BRA(endIfLabel));

        compiler.addLabel(endIfLabel);
    }

    @Override
    protected void ARMCodeGenInst(DecacCompiler compiler) {
        ifClauseCount++;

        ARMLabel thenLabel = new ARMLabel("if." + Integer.toHexString(ifClauseCount));
        ARMLabel elseLabel = new ARMLabel("else." + Integer.toHexString(ifClauseCount));
        ARMLabel endIfLabel = new ARMLabel("end_if." + Integer.toHexString(ifClauseCount));

        condition.ARMCodeGenInst(compiler);
        ARMGPRegister conditionReg;

        if (ARMRegister.getLastExprPos() instanceof fr.ensimag.arm.pseudocode.ARMRegisterOffset) {
            compiler.addARMInstruction(new ARM_POP(ARMRegister.R1));
            StackCount.countPop();
            conditionReg = ARMRegister.R1;
        } else {
            conditionReg = (ARMGPRegister) ARMRegister.getLastExprPos();
        }

        compiler.addARMInstruction(new ARM_CMP(conditionReg, new ARMImmediateInteger(0)));
        compiler.addARMInstruction(new ARM_BEQ(elseLabel));

        if (!(conditionReg.equals(ARMRegister.R1))) {
            ARMRegister.setUnused(conditionReg);
        }

        thenBranch.ARMCodeGenListInst(compiler);
        compiler.addARMInstruction(new ARM_B(endIfLabel));

        compiler.addARMLabel(elseLabel);
        elseBranch.ARMCodeGenListInst(compiler);

        compiler.addARMLabel(endIfLabel);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("if (");
        condition.decompile(s);
        s.println(") {");
        s.indent();
        thenBranch.decompile(s);
        s.unindent();
        s.println("}");

        // for (IfThenElse elseif : elseIfBranches) {
        // s.print("else if (");
        // elseif.condition.decompile(s);
        // s.println(") {");
        // s.indent();
        // elseif.thenBranch.decompile(s);
        // s.unindent();
        // s.println("}");
        // }

        if (elseBranch != null) {
            s.println("else {");
            s.indent();
            elseBranch.decompile(s);
            s.unindent();
            s.println("}");
        }
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        condition.iter(f);
        thenBranch.iter(f);
        elseBranch.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        thenBranch.prettyPrint(s, prefix, false);
        // modifying prettyprint
        // for (IfThenElse elseif : elseIfBranches) {
        // elseif.prettyPrintChildren(s, prefix);
        // }
        elseBranch.prettyPrint(s, prefix, true);
    }
}
