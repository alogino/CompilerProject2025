package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.VariableDefinition;

/**
 *
 * @author gl13
 * @date 01/01/2025
 */
public abstract class AbstractOpBool extends AbstractBinaryExpr {

    public AbstractOpBool(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type leftType = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type rightType = getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        // Check if both operands are of boolean type
        if (leftType.isBoolean() && rightType.isBoolean()) {
            // Check if the operator is valid for boolean binary operations
            String operator = this.getOperatorName();
            if (operator.equals("&&") || operator.equals("||")) {
                // Set the type of the operation to boolean
                this.setType(compiler.environmentType.BOOLEAN);
                return compiler.environmentType.BOOLEAN;
            } else {
                // Invalid operator for boolean types
                throw new ContextualError(
                        "Invalid operator '" + operator + "' for boolean operands ",
                        this.getLocation());
            }
        } else {
            // Incompatible operand types
            throw new ContextualError(
                    "Incompatible types for boolean binary operator '" + this.getOperatorName() +
                            "' between '" + leftType + "' and '" + rightType + "'",
                    this.getLocation());
        }
    }

    protected abstract void codeGenInst(DecacCompiler compiler);

    protected abstract void ARMCodeGenInst(DecacCompiler compiler);
}
