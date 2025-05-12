package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.SEQ;
import fr.ensimag.ima.pseudocode.instructions.SGE;
import fr.ensimag.ima.pseudocode.instructions.SGT;
import fr.ensimag.ima.pseudocode.instructions.SLE;
import fr.ensimag.ima.pseudocode.instructions.SLT;
import fr.ensimag.ima.pseudocode.instructions.SNE;
import fr.ensimag.deca.codegen.StackCount;

/**
 *
 * @author gl13
 * @date 01/01/2025
 */
public abstract class AbstractOpCmp extends AbstractBinaryExpr {

    public AbstractOpCmp(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type leftType = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type rightType = getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        // **Comparison for numeric types (int, float)**
        if (leftType.isInt() && rightType.isFloat()) {
            setLeftOperand(new ConvFloat(getLeftOperand()));
            this.setType(compiler.environmentType.BOOLEAN);
            return compiler.environmentType.BOOLEAN;
        } else if (leftType.isFloat() && rightType.isInt()) {
            setRightOperand(new ConvFloat(getRightOperand()));
            this.setType(compiler.environmentType.BOOLEAN);
            return compiler.environmentType.BOOLEAN;
        } else if ((leftType.isFloat() && rightType.isFloat()) || (leftType.isInt() && rightType.isInt())) {
            // setRightOperand(new ConvFloat(getRightOperand()));
            this.setType(compiler.environmentType.BOOLEAN);
            return compiler.environmentType.BOOLEAN;
        }

        // **Comparison for boolean types (only '==' and '!=')**
        else if (leftType.isBoolean() && rightType.isBoolean()) {
            String operator = this.getOperatorName();
            if (!operator.equals("==") && !operator.equals("!=")) {
                {
                    throw new ContextualError(
                            "Invalid operator '" + operator +
                                    "' for boolean operands. Only '==' and '!=' are allowed",
                            this.getLocation());
                }
            }
            this.setType(compiler.environmentType.BOOLEAN);
            return compiler.environmentType.BOOLEAN;
        }

        // **Comparison for class types or null - not yet tackled **
        else if ((leftType.isClass() || leftType.isNull()) &&
                (rightType.isClass() || rightType.isNull())) {
            this.setType(compiler.environmentType.BOOLEAN);
            return compiler.environmentType.BOOLEAN;
        } else {
            // **Throw error for incompatible types**
            throw new ContextualError(
                    "Incompatible types for comparison operator '" + this.getOperatorName() +
                            "' between '" + leftType + "' and '" + rightType + "'",
                    this.getLocation());
        }
    }

    protected void codeGenInst(DecacCompiler compiler) {
        getLeftOperand().codeGenInst(compiler);
        DVal leftOperandPos = Register.getLastExprPos();
        if (!leftOperandPos.equals(Register.SP)) {
            Register.setUsed((GPRegister) leftOperandPos);
        }

        getRightOperand().codeGenInst(compiler);
        DVal rightOperandPos = Register.getLastExprPos();
        if (!leftOperandPos.equals(Register.SP)) {
            Register.setUnused((GPRegister) leftOperandPos);
        }

        if (rightOperandPos.equals(Register.SP) && leftOperandPos.equals(Register.SP)) {
            // if both operands are stored in the stack as temorary variables
            compiler.addInstruction(new POP(Register.R1));
            StackCount.countPop();

            compiler.addInstruction(new POP(Register.R0));
            StackCount.countPop();

            compiler.addInstruction(new CMP(Register.R1, Register.R0));
            compiler.addInstruction(opCmpInstruction(Register.R1));

            compiler.addInstruction(new PUSH(Register.R1));
            StackCount.countPush();
            Register.setLastExprPos(new RegisterOffset(0, Register.SP));

        } else if (leftOperandPos.equals(Register.SP) && !rightOperandPos.equals(Register.SP)) {
            compiler.addInstruction(new POP(Register.R1));
            StackCount.countPop();

            compiler.addInstruction(new CMP(rightOperandPos, Register.R1));
            compiler.addInstruction(opCmpInstruction(Register.R1));

            compiler.addInstruction(new PUSH(Register.R1));
            StackCount.countPush();
            Register.setLastExprPos(new RegisterOffset(0, Register.SP));

            Register.setUnused((GPRegister) leftOperandPos);
        } else if (leftOperandPos.equals(Register.SP) && !rightOperandPos.equals(Register.SP)) {
            // leftOperand is stored in the stack as a temporary variable and rightOperand
            // is stored in a GPRegister, this should never happen!

            compiler.addInstruction(new POP(Register.R1));
            StackCount.countPop();

            compiler.addInstruction(opCmpInstruction(Register.R1));
            compiler.addInstruction(new PUSH(Register.R1));
            StackCount.countPush();
            Register.setLastExprPos(new RegisterOffset(0, Register.SP));
        } else {
            // if both operands are stored in GPRegisters
            compiler.addInstruction(new CMP(rightOperandPos, (GPRegister) leftOperandPos));
            compiler.addInstruction(opCmpInstruction((GPRegister) rightOperandPos));
            Register.setLastExprPos(new RegisterOffset(0, (GPRegister) rightOperandPos));

            Register.setUnused((GPRegister) leftOperandPos);
        }
    }

    @Override
    protected void ARMCodeGenInst(DecacCompiler compiler) {
        // TODO Auto-generated method stub

    }

    private Instruction opCmpInstruction(GPRegister storeTarget) {
        String op = getOperatorName();

        if (op.equals("==")) {
            return new SEQ(storeTarget);
        } else if (op.equals("!=")) {
            return new SNE(storeTarget);
        } else if (op.equals(">")) {
            return new SGT(storeTarget);
        } else if (op.equals(">=")) {
            return new SGE(storeTarget);
        } else if (op.equals("<")) {
            return new SLT(storeTarget);
        } else if (op.equals("<=")) {
            return new SLE(storeTarget);
        } else {
            System.err.println("Undefined binary arithmetic operation, this should NEVER happen\n");
            System.exit(1);
            return null;
        }
    }
}
