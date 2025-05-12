package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.deca.codegen.StackCount;

/**
 * Represents the unary "Not" operation in the Deca language.
 *
 * @author gl13
 * @date 01/01/2025
 */
public class Not extends AbstractUnaryExpr {

    public Not(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        // Verify the type of the operand
        Type operandType = getOperand().verifyExpr(compiler, localEnv, currentClass);

        // Check if the operand is of type boolean
        if (operandType.isBoolean()) {
            // Set the type of the expression to boolean
            this.setType(operandType);
            return operandType;
        } else {
            // Throw an error if the operand type is not boolean
            throw new ContextualError("Unary 'Not' is not compatible with type '"
                    + operandType.getName() + "'", this.getLocation());
        }
    }

    @Override
    protected String getOperatorName() {
        return "!";
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        getOperand().codeGenInst(compiler);
        DVal operandPos = Register.getLastExprPos();

        GPRegister tempRegister = Register.getUnusedR();

        if (tempRegister == null) {
            // No registers available: use the stack
            GPRegister stackRegister = Register.getR(1); // Use a fixed register for operations
            compiler.addInstruction(new LOAD(operandPos, stackRegister));

            // Push the value onto the stack
            compiler.addInstruction(new PUSH(stackRegister));
            StackCount.countPush(); // Track stack usage

            Label trueLabel = compiler.createLabel("not_true");
            Label endLabel = compiler.createLabel("not_end");

            // Pop the value back and perform the NOT operation
            compiler.addInstruction(new POP(stackRegister));
            StackCount.countPop();
            compiler.addInstruction(new BEQ(trueLabel));
            compiler.addInstruction(new LOAD(new ImmediateInteger(0), stackRegister));
            compiler.addInstruction(new BRA(endLabel));
            compiler.addLabel(trueLabel);
            compiler.addInstruction(new LOAD(new ImmediateInteger(1), stackRegister));
            compiler.addLabel(endLabel);

            // Update lastExprPos with the stack register using a RegisterOffset
            RegisterOffset stackRegisterOffset = new RegisterOffset(0, stackRegister);
            Register.setLastExprPos(stackRegisterOffset);
        } else {
            // Use the register directly
            compiler.addInstruction(new LOAD(operandPos, tempRegister));

            Label trueLabel = compiler.createLabel("not_true");
            Label endLabel = compiler.createLabel("not_end");

            // Perform the NOT operation
            compiler.addInstruction(new BEQ(trueLabel));
            compiler.addInstruction(new LOAD(new ImmediateInteger(0), tempRegister));
            compiler.addInstruction(new BRA(endLabel));
            compiler.addLabel(trueLabel);
            compiler.addInstruction(new LOAD(new ImmediateInteger(1), tempRegister));
            compiler.addLabel(endLabel);

            // Update lastExprPos with the temporary register
            RegisterOffset tempRegisterOffset = new RegisterOffset(0, tempRegister);
            Register.setLastExprPos(tempRegisterOffset);
            Register.setUnused(tempRegister);
        }
    }

}
