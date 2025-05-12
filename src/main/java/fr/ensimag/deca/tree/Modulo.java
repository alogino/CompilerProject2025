package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;

/**
 *
 * @author gl13
 * @date 01/01/2025
 */
public class Modulo extends AbstractOpArith {

    public Modulo(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type leftType = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type rightType = getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        // Ensure both operands are integers
        if (leftType.isInt() && rightType.isInt()) {
            compiler.setPossibleOverflow();
            this.setType(compiler.environmentType.INT); // Set the result type to int
            return compiler.environmentType.INT;
        } else {
            // Throw an error if either operand is not an int
            throw new ContextualError(
                    "Operands for modulus operator '%' must both be integers, but got: "
                            + leftType + " and " + rightType,
                    this.getLocation());
        }
    }

    @Override
    protected String getOperatorName() {
        return "%";
    }
}
