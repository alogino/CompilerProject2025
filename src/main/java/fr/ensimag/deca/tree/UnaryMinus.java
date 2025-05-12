package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.OPP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.deca.codegen.StackCount;

/**
 * @author gl13
 * @date 01/01/2025
 */
public class UnaryMinus extends AbstractUnaryExpr {

    public UnaryMinus(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type operandType = getOperand().verifyExpr(compiler, localEnv, currentClass);

        // Check if the operand is of type int or float
        if (operandType.isInt() || operandType.isFloat()) {
            // Set the type of the expression to the type of the operand
            this.setType(operandType);
            return operandType;
        } else {
            // Throw an error if the operand type is not compatible
            throw new ContextualError("Unary minus is not compatible with type '"
                    + operandType.getName() + "'", this.getLocation());
        }
    }

    @Override
    protected String getOperatorName() {
        return "-";
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        getOperand().codeGenInst(compiler);
        DVal operandPos = Register.getLastExprPos();

        if (operandPos.equals(Register.SP)) {
            GPRegister fixedRegister = Register.R1; // Fixed register for stack manipulation
            compiler.addInstruction(new LOAD(operandPos, fixedRegister));

            compiler.addInstruction(new OPP(fixedRegister, fixedRegister));

            compiler.addInstruction(new PUSH(fixedRegister));
            StackCount.countPush();
        } else {
            compiler.addInstruction(new OPP((GPRegister) operandPos, (GPRegister) operandPos));
        }
    }

}
