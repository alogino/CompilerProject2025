package fr.ensimag.deca.tree;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.FLOAT;
import fr.ensimag.ima.pseudocode.instructions.INT;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.deca.codegen.StackCount;

/**
 * Represents a cast (explicit type conversion) in Deca, e.g., (float)(x).
 */
public class Cast extends AbstractUnaryExpr {
    private final AbstractIdentifier targetType;

    public Cast(AbstractIdentifier targetType, AbstractExpr expr) {
        super(expr);
        Validate.notNull(targetType, "Target type in Cast cannot be null");
        this.targetType = targetType;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {

        Type typeType = this.targetType.verifyType(compiler);
        AbstractExpr operand = getOperand();
        Type leftType = operand.verifyExpr(compiler, localEnv, currentClass);
        if (operand instanceof AbstractIdentifier) {
            AbstractIdentifier newOperand = (Identifier) operand;
            if (!newOperand.getVariableDefinition().isInitialized()) {
                String errorMessage = String.format(
                        "Variable '%s' must be initialized before cast",
                        newOperand.getName());
                throw new ContextualError(errorMessage, operand.getLocation());
            }

        }

        if (compiler.environmentType.isSubType(typeType, leftType)
                || compiler.environmentType.isSubType(leftType, typeType)) {
            setType(typeType);
            return typeType;
        } else {
            String errorMessage = String.format(
                    "Cannot cast to booleans");
            throw new ContextualError(errorMessage, operand.getLocation());
        }
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // NOTE : Only works for casting from int to float or float to int
        this.getOperand().codeGenInst(compiler);
        DVal loadTarget = Register.getLastExprPos();

        if (loadTarget.equals(Register.SP)) {
            compiler.addInstruction(new POP(Register.R1));
            StackCount.countPop();
            if (getType().isFloat()) {
                compiler.addInstruction(new FLOAT(Register.R1, Register.R1));
            } else if (getType().isInt()) {
                compiler.addInstruction(new INT(Register.R1, Register.R1));
            }
            compiler.addInstruction(new PUSH(Register.R1));
            StackCount.countPush();

        } else {
            if (getType().isFloat()) {
                compiler.addInstruction(new FLOAT(loadTarget, (GPRegister) loadTarget));
            } else if (getType().isInt()) {
                compiler.addInstruction(new INT(loadTarget, (GPRegister) loadTarget));
            }
        }
    }

    @Override
    protected void ARMCodeGenInst(DecacCompiler compiler) {
        // TODO Auto-generated method stub

    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        targetType.decompile(s);
        s.print(")(");
        getOperand().decompile(s);
        s.print(")");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        targetType.iter(f);
        getOperand().iter(f);
    }

    @Override
    protected String getOperatorName() {
        return "(" + targetType.getName() + ")";
    }

}
