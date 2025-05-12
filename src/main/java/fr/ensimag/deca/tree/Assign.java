package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.deca.codegen.StackCount;
import fr.ensimag.arm.pseudocode.ARMDAddr;
import fr.ensimag.arm.pseudocode.ARMDVal;
import fr.ensimag.arm.pseudocode.ARMRegister;
import fr.ensimag.arm.pseudocode.ARMGPRegister;
import fr.ensimag.arm.pseudocode.instructions.ARM_POP;
import fr.ensimag.arm.pseudocode.instructions.ARM_STR;

/**
 * Assignment, i.e. lvalue = expr.
 *
 * @author gl13
 * @date 01/01/2025
 */
public class Assign extends AbstractBinaryExpr {

    @Override
    public AbstractLValue getLeftOperand() {
        // The cast succeeds by construction, as the leftOperand has been set
        // as an AbstractLValue by the constructor.
        return (AbstractLValue) super.getLeftOperand();
    }

    public Assign(AbstractLValue leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        // NOTE : VERY IMPORTANT!! leftOperand is a NEW identifier (in the sense that it
        // was created for verifyInst), we must make it correspond to an EXISTING
        // variable. We simply take the name of leftOperand, search for a definition
        // matching it in localEnv, and assign it as leftOperand's definition. NOTHING
        // ELSE. (we check for the existence of a symbol of this name of course)
        getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Definition lVar = getLeftOperand().getDefinition();
        // Set the assign type after all is processed
        this.setType(getLeftOperand().getType());

        // NOTE : AGAIN VERY IMPORTANT!! rightOperand can be a variable or an expression
        // we simply call verifyRValue, which is properly defined for all

        AbstractExpr rExp = getRightOperand()
                .verifyRValue(compiler, localEnv, currentClass, lVar.getType());

        // add ConvFloat node if needed
        if (lVar.getType().isFloat() && rExp.getType().isInt()) {
            // Add ConvFloat node
            this.setRightOperand(new ConvFloat(this.getRightOperand()));
        } else if (rExp.getType() == compiler.environmentType.NULL) {
            this.setRightOperand(rExp);
        } else if (!lVar.getType().sameType(rExp.getType())) {
            String errorMessage = String.format(
                    "Cannot assign expression of type '%s' to a variable of type '%s'",
                    rExp.getType(), lVar.getType());
            throw new ContextualError(errorMessage, this.getLocation());
        }

        lVar.setInitializationStatus(true);
        return lVar.getType();
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        DAddr lOperand = null;

        boolean isField = false;
        int fieldOffset = 0;
        if (getLeftOperand() instanceof AbstractIdentifier) {
            Identifier lValue = (Identifier) getLeftOperand();
            if (lValue.getDefinition().isParam()) {
                lOperand = new RegisterOffset(-2 - lValue.getParamDefinition().getIndex(), Register.LB);
            } else if (lValue.getDefinition().isField()) {
                isField = true;
                fieldOffset = lValue.getFieldDefinition().getIndex();
                lOperand = new RegisterOffset(-2, Register.LB);
            } else {
                lOperand = lValue.getVariableDefinition().getOperand();
            }
        } else if (getLeftOperand() instanceof Selection) {
            isField = true;
            Selection selectionLOperand = (Selection) getLeftOperand();
            AbstractIdentifier fieldIdentifier = selectionLOperand.getField();
            fieldOffset = fieldIdentifier.getFieldDefinition().getIndex();
            lOperand = new RegisterOffset(-2, Register.LB);
        }

        AbstractExpr rValue = getRightOperand();

        // compute right expression
        rValue.codeGenInst(compiler);

        // get right expression location
        DVal lastExprPos = Register.getLastExprPos();
        if (!lastExprPos.equals(Register.SP)) {
            Register.setUsed((GPRegister) lastExprPos);
        }

        if (lastExprPos.equals(Register.SP)) {
            // if expression to be assigned is stored in a temporary variable on the stack
            compiler.addInstruction(new POP(Register.R1));
            StackCount.countPop();
            compiler.addInstruction(new STORE(Register.R1, lOperand));
        } else {
            // if expression to be assigned is stored in a GPRegister
            GPRegister loadTarget = (GPRegister) lastExprPos;

            // handle case where a field is being assigned to
            if (isField) {
                GPRegister unusedReg = Register.getUnusedR();
                compiler.addInstruction(new LOAD(lOperand, unusedReg));
                compiler.addInstruction(new STORE(loadTarget, new RegisterOffset(fieldOffset, unusedReg)));
                Register.setUnused(unusedReg);
                Register.setUnused(loadTarget);
                if (!lastExprPos.equals(Register.SP)) {
                    Register.setUnused((GPRegister) lastExprPos);
                }
                return;
            }

            compiler.addInstruction(new STORE(loadTarget, lOperand));
            Register.setUnused(loadTarget);
        }

    }

    @Override
    protected void ARMCodeGenInst(DecacCompiler compiler) {
        // TODO : handle assign for objects (i.e fields and params)
        ARMDAddr lOperand = null;

        // boolean isField = false;
        // int fieldOffset = 0;
        if (getLeftOperand() instanceof AbstractIdentifier) {
            Identifier lValue = (Identifier) getLeftOperand();
            if (lValue.getDefinition().isParam()) {
                // lOperand = new RegisterOffset(-2 - lValue.getParamDefinition().getIndex(),
                // Register.LB);
            } else if (lValue.getDefinition().isField()) {
                // isField = true;
                // fieldOffset = lValue.getFieldDefinition().getIndex();
                // lOperand = new RegisterOffset(-2, Register.LB);
            } else {
                lOperand = lValue.getVariableDefinition().getARMOperand();
            }
        } else if (getLeftOperand() instanceof Selection) {
            // isField = true;
            // Selection selectionLOperand = (Selection) getLeftOperand();
            // AbstractIdentifier fieldIdentifier = selectionLOperand.getField();
            // fieldOffset = fieldIdentifier.getFieldDefinition().getIndex();
            // lOperand = new RegisterOffset(-2, Register.LB);
        }

        AbstractExpr rValue = getRightOperand();

        // compute right expression
        rValue.ARMCodeGenInst(compiler);

        // get right expression location
        ARMDVal lastExprPos = ARMRegister.getLastExprPos();
        if (!lastExprPos.equals(ARMRegister.SP)) {
            ARMRegister.setUsed((ARMGPRegister) lastExprPos);
        }

        if (lastExprPos.equals(ARMRegister.SP)) {
            // if expression to be assigned is stored in a temporary variable on the stack
            compiler.addARMInstruction(new ARM_POP(ARMRegister.R1));
            StackCount.countPop();
            compiler.addARMInstruction(new ARM_STR(ARMRegister.R1, lOperand));
        } else {
            // if expression to be assigned is stored in a GPRegister
            ARMGPRegister loadTarget = (ARMGPRegister) lastExprPos;

            // handle case where a field is being assigned to
            // if (isField) {
            // GPRegister unusedReg = Register.getUnusedR();
            // compiler.addInstruction(new LOAD(lOperand, unusedReg));
            // compiler.addInstruction(new STORE(loadTarget, new RegisterOffset(fieldOffset,
            // unusedReg)));
            // Register.setUnused(unusedReg);
            // Register.setUnused(loadTarget);
            // if (!lastExprPos.equals(Register.SP)) {
            // Register.setUnused((GPRegister) lastExprPos);
            // }
            // return;
            // }

            compiler.addARMInstruction(new ARM_STR(loadTarget, lOperand));
            ARMRegister.setUnused(loadTarget);
        }
    }

    @Override
    protected String getOperatorName() {
        return "=";
    }

}
