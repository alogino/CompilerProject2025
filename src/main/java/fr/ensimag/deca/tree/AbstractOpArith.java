package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.ADD;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.DIV;
import fr.ensimag.ima.pseudocode.instructions.MUL;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.QUO;
import fr.ensimag.ima.pseudocode.instructions.REM;
import fr.ensimag.ima.pseudocode.instructions.SUB;
import fr.ensimag.arm.pseudocode.ARMRegister;
import fr.ensimag.arm.pseudocode.ARMRegisterOffset;
import fr.ensimag.arm.pseudocode.ARMSPRegister;
import fr.ensimag.arm.pseudocode.ARMGPRegister;
import fr.ensimag.arm.pseudocode.ARMLabel;
import fr.ensimag.arm.pseudocode.ARMDVal;
import fr.ensimag.arm.pseudocode.instructions.ARM_ADD;
import fr.ensimag.arm.pseudocode.instructions.ARM_MUL;
import fr.ensimag.arm.pseudocode.instructions.ARM_POP;
import fr.ensimag.arm.pseudocode.instructions.ARM_PUSH;
import fr.ensimag.arm.pseudocode.instructions.ARM_SUB;
import fr.ensimag.arm.pseudocode.instructions.ARM_VADDF32;
import fr.ensimag.arm.pseudocode.instructions.ARM_VSUBF32;
import fr.ensimag.arm.pseudocode.instructions.ARM_VMULF32;
import fr.ensimag.arm.pseudocode.instructions.ARM_VDIVF32;
import fr.ensimag.arm.pseudocode.instructions.ARM_VLDR;
import fr.ensimag.arm.pseudocode.instructions.ARM_VMOV;
import fr.ensimag.arm.pseudocode.instructions.ARM_MOV;
import fr.ensimag.arm.pseudocode.instructions.ARM_MLS;
import fr.ensimag.arm.pseudocode.instructions.ARM_BL;
import fr.ensimag.deca.codegen.StackCount;

/**
 * Arithmetic binary operations (+, -, /, ...)
 * 
 * @author gl13
 * @date 01/01/2025
 */
public abstract class AbstractOpArith extends AbstractBinaryExpr {

    public AbstractOpArith(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type leftType = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type rightType = getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        // Check type compatibility between the left and right operands
        if (leftType.isInt() && rightType.isInt()) {
            this.setType(compiler.environmentType.INT);
            return compiler.environmentType.INT;
        } else if (leftType.isFloat() && rightType.isInt()) {
            compiler.setPossibleOverflow();
            this.setType(compiler.environmentType.FLOAT);
            setRightOperand(new ConvFloat(getRightOperand()));
            return compiler.environmentType.FLOAT;
        } else if (leftType.isInt() && rightType.isFloat()) {
            compiler.setPossibleOverflow();
            setLeftOperand(new ConvFloat(getLeftOperand()));
            this.setType(compiler.environmentType.FLOAT);
            return compiler.environmentType.FLOAT;
        } else if (leftType.isFloat() && rightType.isFloat()) {
            compiler.setPossibleOverflow();
            this.setType(compiler.environmentType.FLOAT);
            return compiler.environmentType.FLOAT;
        } else {
            throw new ContextualError("Incompatible types for binary operation: "
                    + leftType + " and " + rightType, this.getLocation());
        }
    }

    @Override
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

            opArithInstruction(Register.R1, Register.R0, compiler);
            compiler.addInstruction(new PUSH(Register.R1));
            StackCount.countPush();
            Register.setLastExprPos(new RegisterOffset(0, Register.SP));

        } else if (rightOperandPos.equals(Register.SP)) {
            // if rightOperand is stored in the stack as a temporary variable
            compiler.addInstruction(new POP(Register.R1));
            StackCount.countPop();

            opArithInstruction(Register.R1, (GPRegister) leftOperandPos, compiler);
            compiler.addInstruction(new PUSH(Register.R1));
            StackCount.countPush();
            Register.setLastExprPos(new RegisterOffset(0, Register.SP));

            Register.setUnused((GPRegister) leftOperandPos);
        } else if (leftOperandPos.equals(Register.SP) && !rightOperandPos.equals(Register.SP)) {
            compiler.addInstruction(new POP(Register.R1));
            StackCount.countPop();

            opArithInstruction((GPRegister) rightOperandPos, Register.R1, compiler);
            compiler.addInstruction(new PUSH(Register.R1));
            StackCount.countPush();
            Register.setLastExprPos(new RegisterOffset(0, Register.SP));
        } else {
            // if both operands are stored in GPRegisters
            opArithInstruction(rightOperandPos, (GPRegister) leftOperandPos, compiler);
            Register.setLastExprPos(new RegisterOffset(0, (GPRegister) leftOperandPos));

            Register.setUnused((GPRegister) rightOperandPos);
        }
    }

    private void opArithInstruction(DVal leftDVal, GPRegister rightGPReg, DecacCompiler compiler) {
        String op = getOperatorName();

        if (op.equals("+")) {
            compiler.addInstruction(new ADD(leftDVal, rightGPReg));
            if (this.getType().isFloat() && !compiler.getCompilerOptions().getNoCheck()) {
                compiler.addInstruction(new BOV(new Label("overflow_error")));
            }
        } else if (op.equals("-")) {
            compiler.addInstruction(new SUB(leftDVal, rightGPReg));
            if (this.getType().isFloat() && !compiler.getCompilerOptions().getNoCheck()) {
                compiler.addInstruction(new BOV(new Label("overflow_error")));
            }
        } else if (op.equals("*")) {
            compiler.addInstruction(new MUL(leftDVal, rightGPReg));
            if (this.getType().isFloat() && !compiler.getCompilerOptions().getNoCheck()) {
                compiler.addInstruction(new BOV(new Label("overflow_error")));
            }
        } else if (op.equals("/")) {
            // division between two ints
            if (getLeftOperand().getType().isInt() && getRightOperand().getType().isInt()) {
                compiler.addInstruction(new QUO(leftDVal, rightGPReg));
            } else {
                // division where one of the operands is a float
                compiler.addInstruction(new DIV(leftDVal, rightGPReg));
            }
            if (!compiler.getCompilerOptions().getNoCheck()) {
                compiler.addInstruction(new BOV(new Label("overflow_error")));
            }

        } else if (op.equals("%")) {
            compiler.addInstruction(new REM(leftDVal, rightGPReg));
            if (!compiler.getCompilerOptions().getNoCheck()) {
                compiler.addInstruction(new BOV(new Label("overflow_error")));
            }
        } else {
            System.err.println("Undefined binary arithmetic operation, this should NEVER happen\n");
            System.exit(1);
        }

    }

    @Override
    protected void ARMCodeGenInst(DecacCompiler compiler) {
        // TODO Auto-generated method stub
        getLeftOperand().ARMCodeGenInst(compiler);
        ARMDVal leftOperandPos = ARMRegister.getLastExprPos();
        if (!leftOperandPos.equals(ARMRegister.SP)) {
            if (leftOperandPos instanceof ARMSPRegister) {
                ARMRegister.VFPSsetUsed((ARMSPRegister) leftOperandPos);
            } else if (leftOperandPos instanceof ARMGPRegister) {
                ARMRegister.setUsed((ARMGPRegister) leftOperandPos);
            }
        }

        getRightOperand().ARMCodeGenInst(compiler);
        ARMDVal rightOperandPos = ARMRegister.getLastExprPos();
        if (!leftOperandPos.equals(ARMRegister.SP)) {
            if (leftOperandPos instanceof ARMSPRegister) {
                ARMRegister.VFPSsetUnused((ARMSPRegister) leftOperandPos);
            } else if (leftOperandPos instanceof ARMGPRegister) {
                ARMRegister.setUnused((ARMGPRegister) leftOperandPos);
            }
        }

        if (rightOperandPos.equals(ARMRegister.SP) && leftOperandPos.equals(ARMRegister.SP)) {
            // if both operands are stored in the stack as temorary variables

        } else if (rightOperandPos.equals(ARMRegister.SP)) {
            // if rightOperand is stored in the stack as a temporary variable

        } else if (leftOperandPos.equals(ARMRegister.SP) && !rightOperandPos.equals(ARMRegister.SP)) {

        } else {
            // if both operands are stored in GPRegisters
            opArithARMInstruction((ARMRegister) leftOperandPos, rightOperandPos, compiler);
            ARMRegister.setLastExprPos(new ARMRegisterOffset(0, (ARMRegister) leftOperandPos));

            if (rightOperandPos instanceof ARMGPRegister) {
                ARMRegister.setUnused((ARMGPRegister) rightOperandPos);
            } else if (rightOperandPos instanceof ARMSPRegister) {
                ARMRegister.VFPSsetUnused((ARMSPRegister) rightOperandPos);
            }
        }

    }

    private void opArithARMInstruction(ARMRegister leftReg, ARMDVal rightDVal, DecacCompiler compiler) {
        String op = getOperatorName();

        if (op.equals("+")) {
            if (getType().isInt()) {
                compiler.addARMInstruction(new ARM_ADD((ARMGPRegister) leftReg, (ARMGPRegister) leftReg, rightDVal));
            } else if (getType().isFloat()) {
                // put values in s registers, compute, put back in r register
                ARMSPRegister leftSReg = ARMRegister.VFPgetUnusedS();
                ARMSPRegister rightSReg = ARMRegister.VFPgetUnusedS();
                compiler.addARMInstruction(new ARM_VLDR(leftSReg, new ARMRegisterOffset(0, leftReg)));
                compiler.addARMInstruction(
                        new ARM_VLDR(rightSReg, new ARMRegisterOffset(0, (ARMGPRegister) rightDVal)));

                compiler.addARMInstruction(new ARM_VADDF32(leftSReg, leftSReg, rightSReg));

                // set result in the corresponding r register
                compiler.addARMInstruction(new ARM_VMOV((ARMGPRegister) leftReg, leftSReg));

                // set s registers as unused
                ARMRegister.VFPSsetUnused(leftSReg);
                ARMRegister.VFPSsetUnused(rightSReg);
            }

        } else if (op.equals("-")) {
            if (getType().isInt()) {
                compiler.addARMInstruction(new ARM_SUB((ARMGPRegister) leftReg, (ARMGPRegister) leftReg, rightDVal));
            } else if (getType().isFloat()) {
                // put values in s registers, compute, put back in r register
                ARMSPRegister leftSReg = ARMRegister.VFPgetUnusedS();
                ARMSPRegister rightSReg = ARMRegister.VFPgetUnusedS();
                compiler.addARMInstruction(new ARM_VLDR(leftSReg, new ARMRegisterOffset(0, leftReg)));
                compiler.addARMInstruction(
                        new ARM_VLDR(rightSReg, new ARMRegisterOffset(0, (ARMGPRegister) rightDVal)));

                compiler.addARMInstruction(new ARM_VSUBF32(leftSReg, leftSReg, rightSReg));

                // set result in the corresponding r register
                compiler.addARMInstruction(new ARM_VMOV((ARMGPRegister) leftReg, leftSReg));

                // set s registers as unused
                ARMRegister.VFPSsetUnused(leftSReg);
                ARMRegister.VFPSsetUnused(rightSReg);
            }

        } else if (op.equals("*")) {
            if (getType().isInt()) {
                compiler.addARMInstruction(new ARM_MUL((ARMGPRegister) leftReg, (ARMGPRegister) leftReg, rightDVal));
            } else if (getType().isFloat()) {
                // put values in s registers, compute, put back in r register
                ARMSPRegister leftSReg = ARMRegister.VFPgetUnusedS();
                ARMSPRegister rightSReg = ARMRegister.VFPgetUnusedS();
                compiler.addARMInstruction(new ARM_VLDR(leftSReg, new ARMRegisterOffset(0, leftReg)));
                compiler.addARMInstruction(
                        new ARM_VLDR(rightSReg, new ARMRegisterOffset(0, (ARMGPRegister) rightDVal)));

                compiler.addARMInstruction(new ARM_VMULF32(leftSReg, leftSReg, rightSReg));

                // set result in the corresponding r register
                compiler.addARMInstruction(new ARM_VMOV((ARMGPRegister) leftReg, leftSReg));

                // set s registers as unused
                ARMRegister.VFPSsetUnused(leftSReg);
                ARMRegister.VFPSsetUnused(rightSReg);
            }

        } else if (op.equals("/")) {
            if (getType().isInt()) {
                // implements integer division using the libc (this does not work if leftReg is
                // in R1 and rightDVal is in R0, let's hope this never happens haha...)

                // If rightDVal is in R0
                boolean movedLeftReg = false;
                boolean movedRightDVal = false;

                // Move leftReg to R0
                if (!leftReg.equals(ARMRegister.R0)) {
                    // manage case where rightDVal is in R0, in which case we store it in R12
                    if (rightDVal.equals(ARMRegister.R0)) {
                        movedRightDVal = true;
                        compiler.addARMInstruction(new ARM_MOV(ARMRegister.R12, ARMRegister.R0));
                        compiler.addARMInstruction(new ARM_MOV(ARMRegister.R0, leftReg));
                    } else {
                        // save R0 if needed
                        if (ARMRegister.isUsed(0)) {
                            compiler.addARMInstruction(new ARM_PUSH(ARMRegister.R0));
                        }

                        compiler.addARMInstruction(new ARM_MOV(ARMRegister.R0, leftReg));

                    }
                }

                // Move rightDVal to R1
                if (!rightDVal.equals(ARMRegister.R1)) {
                    // manage case where leftReg is in R1, in which case we store it in R12
                    if (rightDVal.equals(ARMRegister.R1)) {
                        movedLeftReg = true;
                        compiler.addARMInstruction(new ARM_MOV(ARMRegister.R12, ARMRegister.R1));
                        compiler.addARMInstruction(new ARM_MOV(ARMRegister.R1, rightDVal));
                    } else {
                        // save R1 if needed
                        if (ARMRegister.isUsed(1)) {
                            compiler.addARMInstruction(new ARM_PUSH(ARMRegister.R1));
                        }

                        compiler.addARMInstruction(new ARM_MOV(ARMRegister.R1, rightDVal));
                    }
                }

                compiler.addARMInstruction(new ARM_BL(new ARMLabel("__aeabi_idiv")));
                if (!leftReg.equals(ARMRegister.R0)) {
                    compiler.addARMInstruction(new ARM_MOV(leftReg, ARMRegister.R0));
                }

                if (movedRightDVal) {
                    compiler.addARMInstruction(new ARM_MOV(ARMRegister.R0, ARMRegister.R12));
                } else if (ARMRegister.isUsed(0) && !leftReg.equals(ARMRegister.R0)) {
                    // restore R0 if needed
                    compiler.addARMInstruction(new ARM_POP(ARMRegister.R0));
                }
                if (movedLeftReg) {
                    compiler.addARMInstruction(new ARM_MOV(ARMRegister.R1, ARMRegister.R12));
                } else if (ARMRegister.isUsed(1) && !rightDVal.equals(ARMRegister.R1)) {
                    // restore R1 if needed
                    compiler.addARMInstruction(new ARM_POP(ARMRegister.R1));
                }
            } else if (getType().isFloat()) {
                // put values in s registers, compute, put back in r register
                ARMSPRegister leftSReg = ARMRegister.VFPgetUnusedS();
                ARMSPRegister rightSReg = ARMRegister.VFPgetUnusedS();
                compiler.addARMInstruction(new ARM_VLDR(leftSReg, new ARMRegisterOffset(0, leftReg)));
                compiler.addARMInstruction(
                        new ARM_VLDR(rightSReg, new ARMRegisterOffset(0, (ARMGPRegister) rightDVal)));

                compiler.addARMInstruction(new ARM_VDIVF32(leftSReg, leftSReg, rightSReg));

                // set result in the corresponding r register
                compiler.addARMInstruction(new ARM_VMOV((ARMGPRegister) leftReg, leftSReg));

                // set s registers as unused
                ARMRegister.VFPSsetUnused(leftSReg);
                ARMRegister.VFPSsetUnused(rightSReg);
            }

        } else if (op.equals("%")) {
            // implements integer modulo using the libc for the division (this does not work
            // if leftReg is in R1 and rightDVal is in R0, let's hope this never happens
            // haha...)

            // If rightDVal is in R0
            boolean movedLeftReg = false;
            boolean movedRightDVal = false;

            ARMGPRegister leftRegStore = null;

            if (!leftReg.equals(ARMRegister.R0) && !rightDVal.equals(ARMRegister.R0)) {
                leftRegStore = ARMRegister.getUnusedSavedR();
                compiler.addARMInstruction(new ARM_MOV(leftRegStore, leftReg));
            }

            // Move leftReg to R0
            if (!leftReg.equals(ARMRegister.R0)) {
                // manage case where rightDVal is in R0, in which case we store it in R12
                if (rightDVal.equals(ARMRegister.R0)) {
                    movedRightDVal = true;
                    compiler.addARMInstruction(new ARM_MOV(ARMRegister.R12, ARMRegister.R0));
                    compiler.addARMInstruction(new ARM_MOV(ARMRegister.R0, leftReg));
                } else {
                    // save R0 if needed
                    if (ARMRegister.isUsed(0)) {
                        compiler.addARMInstruction(new ARM_PUSH(ARMRegister.R0));
                    }

                    compiler.addARMInstruction(new ARM_MOV(ARMRegister.R0, leftReg));

                }
            } else {
                leftRegStore = ARMRegister.getUnusedSavedR();
                compiler.addARMInstruction(new ARM_MOV(leftRegStore, leftReg));
            }

            // Move rightDVal to R1
            if (!rightDVal.equals(ARMRegister.R1)) {
                // manage case where leftReg is in R1, in which case we store it in R12
                if (rightDVal.equals(ARMRegister.R1)) {
                    movedLeftReg = true;
                    compiler.addARMInstruction(new ARM_MOV(ARMRegister.R12, ARMRegister.R1));
                    compiler.addARMInstruction(new ARM_MOV(ARMRegister.R1, rightDVal));
                } else {
                    // save R1 if needed
                    if (ARMRegister.isUsed(1)) {
                        compiler.addARMInstruction(new ARM_PUSH(ARMRegister.R1));
                    }

                    compiler.addARMInstruction(new ARM_MOV(ARMRegister.R1, rightDVal));
                }
            }

            compiler.addARMInstruction(new ARM_BL(new ARMLabel("__aeabi_idiv")));

            // leftReg - div*rightDVal
            if (!leftReg.equals(ARMRegister.R0) && !rightDVal.equals(ARMRegister.R0)) {
                compiler.addARMInstruction(
                        new ARM_MLS((ARMGPRegister) leftReg, ARMRegister.R0, (ARMGPRegister) rightDVal,
                                leftRegStore));
                ARMRegister.setUnused(leftRegStore);
            } else if (leftReg.equals(ARMRegister.R0)) {
                compiler.addARMInstruction(
                        new ARM_MLS((ARMGPRegister) leftReg, ARMRegister.R0, (ARMGPRegister) rightDVal,
                                leftRegStore));
                ARMRegister.setUnused(leftRegStore);
            } else if (rightDVal.equals(ARMRegister.R0)) {
                compiler.addARMInstruction(
                        new ARM_MLS((ARMGPRegister) leftReg, ARMRegister.R0, ARMRegister.R12,
                                (ARMGPRegister) leftReg));
            }

            if (movedRightDVal) {
                compiler.addARMInstruction(new ARM_MOV(ARMRegister.R0, ARMRegister.R12));
            } else if (ARMRegister.isUsed(0) && !leftReg.equals(ARMRegister.R0)) {
                // restore R0 if needed
                compiler.addARMInstruction(new ARM_POP(ARMRegister.R0));
            }
            if (movedLeftReg) {
                compiler.addARMInstruction(new ARM_MOV(ARMRegister.R1, ARMRegister.R12));
            } else if (ARMRegister.isUsed(1) && !rightDVal.equals(ARMRegister.R1)) {
                // restore R1 if needed
                compiler.addARMInstruction(new ARM_POP(ARMRegister.R1));
            }

            if (!leftReg.equals(ARMRegister.R0)) {
                compiler.addARMInstruction(new ARM_MOV(leftReg, ARMRegister.R0));
            }

        } else {
            System.err.println("Undefined binary arithmetic operation, this should NEVER happen\n");
            System.exit(1);
        }

    }

}
