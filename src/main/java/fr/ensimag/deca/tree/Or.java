package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.deca.codegen.StackCount;

/**
 * Represents the logical "||" operation in the Deca language.
 *
 * @author gl13
 * @date 01/01/2025
 */
public class Or extends AbstractOpBool {

    public Or(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "||";
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        Label trueLabel = compiler.createLabel("or_true");
        Label endLabel = compiler.createLabel("or_end");

        getLeftOperand().codeGenInst(compiler);
        DVal leftOperandPos = Register.getLastExprPos();
        GPRegister tempRegister = Register.getUnusedR();

        if (tempRegister == null) {
            // No registers available: use a fixed register for stack operations
            GPRegister stackRegister = Register.getR(1); // Use R1 as a fixed temporary register
            compiler.addInstruction(new LOAD(leftOperandPos, stackRegister));

            compiler.addInstruction(new PUSH(stackRegister));
            StackCount.countPush();
        } else {
            compiler.addInstruction(new LOAD(leftOperandPos, tempRegister));
        }

        compiler.addInstruction(new BNE(trueLabel));

        getRightOperand().codeGenInst(compiler);
        DVal rightOperandPos = Register.getLastExprPos();

        if (tempRegister == null) {
            // Pop the left operand from the stack
            GPRegister stackRegister = Register.getR(1); // Use a fixed register for popping
            compiler.addInstruction(new POP(stackRegister));
            StackCount.countPop();
            compiler.addInstruction(new LOAD(rightOperandPos, stackRegister));
            tempRegister = stackRegister; // Use the popped register as temp
        } else {
            // Load the right operand into the temporary register
            compiler.addInstruction(new LOAD(rightOperandPos, tempRegister));
        }

        // Short-circuit to true if the right operand is true
        compiler.addInstruction(new BNE(trueLabel));

        compiler.addInstruction(new LOAD(new ImmediateInteger(0), tempRegister));
        compiler.addInstruction(new BRA(endLabel));

        // True case: set result to 1
        compiler.addLabel(trueLabel);
        compiler.addInstruction(new LOAD(new ImmediateInteger(1), tempRegister));

        compiler.addLabel(endLabel);

        if (tempRegister != null) {
            Register.setLastExprPos(new RegisterOffset(0, tempRegister));
            Register.setUnused(tempRegister);
        }
    }

    @Override
    protected void ARMCodeGenInst(DecacCompiler compiler) {
        // TODO Auto-generated method stub

    }
}
