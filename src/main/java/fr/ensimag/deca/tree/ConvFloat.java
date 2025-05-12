package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.FLOAT;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.deca.codegen.StackCount;

/**
 * Conversion of an int into a float. Used for implicit conversions.
 * 
 * @author gl13
 * @date 01/01/2025
 */
public class ConvFloat extends AbstractUnaryExpr {
    public ConvFloat(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) {
        return compiler.environmentType.FLOAT;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        this.getOperand().codeGenInst(compiler);
        DVal loadTarget = Register.getLastExprPos();

        if (loadTarget.equals(Register.SP)) {
            compiler.addInstruction(new POP(Register.R1));
            StackCount.countPop();
            compiler.addInstruction(new FLOAT(Register.R1, Register.R1));
            compiler.addInstruction(new PUSH(Register.R1));
            StackCount.countPush();

        } else {
            compiler.addInstruction(new FLOAT(loadTarget, (GPRegister) loadTarget));
        }
    }

    @Override
    protected String getOperatorName() {
        return "/* conv float */";
    }

    @Override
    protected void checkDecoration() {
        // ConvFloat has no decorations
    }

}
