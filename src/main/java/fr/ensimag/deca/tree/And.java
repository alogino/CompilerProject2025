package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.deca.codegen.StackCount;

/**
 * Represents the logical "&&" operation in the Deca language.
 *
 * @author gl13
 * @date 01/01/2025
 */
public class And extends AbstractOpBool {

    public And(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "&&";
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        Label falseLabel = compiler.createLabel("and_false");
        Label endLabel = compiler.createLabel("and_end");

        getLeftOperand().codeGenInst(compiler);
        DVal leftOperandPos = Register.getLastExprPos();
        GPRegister tempRegister = Register.getUnusedR();

        if (tempRegister == null) {
            // No registers available: load the left operand into a fixed temporary register
            GPRegister stackRegister = Register.getR(1); // Use R1 as a fixed temporary register
            compiler.addInstruction(new LOAD(leftOperandPos, stackRegister));

            // Push the left operand onto the stack
            compiler.addInstruction(new PUSH(stackRegister));
            StackCount.countPush(); // Track stack usage
        } else {
            // Load the left operand into the temporary register
            compiler.addInstruction(new LOAD(leftOperandPos, tempRegister));
        }

        // Branch to falseLabel if the left operand is false
        compiler.addInstruction(new BEQ(falseLabel));

        getRightOperand().codeGenInst(compiler);
        DVal rightOperandPos = Register.getLastExprPos();

        if (tempRegister == null) {
            // Pop the left operand back from the stack
            GPRegister stackRegister = Register.getR(1); // Use a fixed register for popping
            compiler.addInstruction(new POP(stackRegister));
            StackCount.countPop(); // Track stack usage
            compiler.addInstruction(new LOAD(rightOperandPos, stackRegister));
            tempRegister = stackRegister; // Use the popped register as temp
        } else {
            // Load the right operand into the temporary register
            compiler.addInstruction(new LOAD(rightOperandPos, tempRegister));
        }
        compiler.addInstruction(new BEQ(falseLabel));

        // Both operands are true; branch to endLabel
        compiler.addInstruction(new BRA(endLabel));

        // False case: set result to 0
        compiler.addLabel(falseLabel);
        compiler.addInstruction(new LOAD(new ImmediateInteger(0), tempRegister));

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
