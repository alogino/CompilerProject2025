package fr.ensimag.deca.tree;

import fr.ensimag.arm.pseudocode.*;
import fr.ensimag.arm.pseudocode.instructions.*;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.deca.codegen.StackCount;
import java.io.PrintStream;

import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.commons.lang.Validate;

/**
 * Represents a "while" loop instruction in the Deca language.
 *
 * @author gl13
 * @date 01/01/2025
 */
public class While extends AbstractInst {
    private AbstractExpr condition;
    private ListInst body;

    public AbstractExpr getCondition() {
        return condition;
    }

    public ListInst getBody() {
        return body;
    }

    /**
     * Constructs a "while" loop with a condition and a body.
     *
     * @param condition the condition expression (must not be null)
     * @param body      the list of instructions representing the body (must not be
     *                  null)
     * @throws IllegalArgumentException if the condition or body is null
     */
    public While(AbstractExpr condition, ListInst body) {
        Validate.notNull(condition);
        Validate.notNull(body);
        this.condition = condition;
        this.body = body;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        Label startLabel = compiler.createLabel("while_start");
        Label endLabel = compiler.createLabel("while_end");

        compiler.addLabel(startLabel);

        getCondition().codeGenInst(compiler);
        DVal conditionResult = Register.getLastExprPos();

        GPRegister tempRegister = Register.getUnusedR();

        if (tempRegister == null) {
            // No registers available: use the stack to evaluate the condition
            GPRegister stackRegister = Register.getR(1); // Use R1 as a fixed stack helper
            compiler.addInstruction(new LOAD(conditionResult, stackRegister));

            // Push the condition result onto the stack
            compiler.addInstruction(new PUSH(stackRegister));
            StackCount.countPush();

            // Test the condition by popping from the stack
            compiler.addInstruction(new POP(stackRegister));
            StackCount.countPop();
            compiler.addInstruction(new BEQ(endLabel));
        } else {
            // Use the temporary register for the condition
            compiler.addInstruction(new LOAD(conditionResult, tempRegister));
            compiler.addInstruction(new BEQ(endLabel));
        }

        getBody().codeGenListInst(compiler);

        compiler.addInstruction(new BRA(startLabel));

        compiler.addLabel(endLabel);

        if (tempRegister != null) {
            Register.setUnused(tempRegister);
        }
    }

    @Override
    protected void ARMCodeGenInst(DecacCompiler compiler) {
        ARMLabel startLabel = new ARMLabel("while_" + Integer.toHexString(compiler.createLabel("while").toString().hashCode()));
        ARMLabel endLabel = new ARMLabel("end_while_" + Integer.toHexString(compiler.createLabel("while").toString().hashCode()));

        compiler.addARMLabel(startLabel);

        condition.ARMCodeGenInst(compiler);

        ARMGPRegister conditionReg;
        if (ARMRegister.getLastExprPos() instanceof fr.ensimag.arm.pseudocode.ARMRegisterOffset) {
            compiler.addARMInstruction(new ARM_LDR(ARMRegister.R1, (ARMRegisterOffset)ARMRegister.getLastExprPos()));
            conditionReg = ARMRegister.R1;
        } else {
            conditionReg = (ARMGPRegister) ARMRegister.getLastExprPos();
        }

        compiler.addARMInstruction(new ARM_CMP(conditionReg, new ARMImmediateInteger(3)));

        compiler.addARMInstruction(new ARM_BGE(endLabel));

        if (!conditionReg.equals(ARMRegister.R1)) {
            ARMRegister.setUnused(conditionReg);
        }

        body.ARMCodeGenListInst(compiler);

        compiler.addARMInstruction(new ARM_B(startLabel));

        compiler.addARMLabel(endLabel);
    }
    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        Type condType = this.condition.verifyExpr(compiler, localEnv, currentClass);
        this.condition.setType(condType);
        this.condition.verifyCondition(compiler, localEnv, currentClass);
        this.body.verifyListInst(compiler, localEnv, currentClass, returnType);
    }

    /**
     * Decompiles the "while" loop into Deca source code.
     *
     * @param s the IndentPrintStream used for decompiling
     */
    @Override
    public void decompile(IndentPrintStream s) {
        s.print("while (");
        getCondition().decompile(s);
        s.println(") {");
        s.indent();
        getBody().decompile(s);
        s.unindent();
        s.print("}");
    }

    /**
     * Applies a function to all child nodes of the "while" loop.
     *
     * @param f the function to apply to the children
     */
    @Override
    protected void iterChildren(TreeFunction f) {
        condition.iter(f);
        body.iter(f);
    }

    /**
     * Prints the child nodes of the "while" loop for debugging purposes.
     *
     * @param s      the output stream
     * @param prefix the prefix used for indentation
     */
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        body.prettyPrint(s, prefix, true);
    }

}
