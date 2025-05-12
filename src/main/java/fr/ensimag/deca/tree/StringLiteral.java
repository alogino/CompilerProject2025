package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.arm.pseudocode.ARMLabel;
import fr.ensimag.arm.pseudocode.ARMLiteral;
import fr.ensimag.arm.pseudocode.ARMRegister;
import fr.ensimag.arm.pseudocode.instructions.ARM_BL;
import fr.ensimag.arm.pseudocode.instructions.ARM_LDR;
import fr.ensimag.arm.pseudocode.instructions.ARM_MOV;
import fr.ensimag.arm.pseudocode.instructions.ARM_POP;
import fr.ensimag.arm.pseudocode.instructions.ARM_PUSH;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.ARMDataSection;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.ImmediateString;
import fr.ensimag.ima.pseudocode.instructions.WSTR;

/**
 * String literal
 *
 * @author gl13
 * @date 01/01/2025
 */
public class StringLiteral extends AbstractStringLiteral {

    @Override
    public String getValue() {
        return value;
    }

    private String value;

    /**
     * Constructor for a string literal.
     *
     * @param value the string value (must not be null)
     */
    public StringLiteral(String value) {
        Validate.notNull(value);
        this.value = value;
    }

    /**
     * Verifies that the expression is well-typed.
     *
     * @param compiler     the Deca compiler
     * @param localEnv     the local environment
     * @param currentClass the current class definition
     * @return the type of the string literal
     * @throws ContextualError if the context is invalid
     */
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {

        this.setType(compiler.environmentType.STRING);
        Type type = this.getType();
        if (type == compiler.environmentType.STRING) {
            return compiler.environmentType.STRING;
        } else {
            throw new ContextualError("Expected StringLiteral, got " + type.getName().toString(), getLocation());
        }
    }

    /**
     * Generates code to print the string literal.
     *
     * @param compiler the Deca compiler
     * @param printHex whether to print in hexadecimal format
     */
    @Override
    protected void codeGenPrint(DecacCompiler compiler, boolean printHex) {
        compiler.addInstruction(new WSTR(new ImmediateString(value)));
    }

    @Override
    protected void ARMCodeGenPrint(DecacCompiler compiler, boolean printHex) {
        // Account for incoming int printing
        ARMDataSection.setStringPrint();

        // Load int_format data into R0 (if used, push then pop)
        if (ARMRegister.isUsed(0)) {
            compiler.addARMInstruction(new ARM_PUSH(ARMRegister.R0));
        }
        // Load literal to be printed in R1 (if used, push then pop)
        if (ARMRegister.isUsed(1)) {
            compiler.addARMInstruction(new ARM_PUSH(ARMRegister.R1));
        }

        String stringDataEntry = ARMDataSection.createStringDataEntry(value);
        compiler.addARMInstruction(new ARM_LDR(ARMRegister.R0, new ARMLiteral("string_format")));
        compiler.addARMInstruction(new ARM_LDR(ARMRegister.R1, new ARMLiteral(stringDataEntry)));

        // Call printf with BL instruction
        compiler.addARMInstruction(new ARM_BL(new ARMLabel("printf")));

        // Restore R0 and or R1 if need be
        if (ARMRegister.isUsed(1)) {
            compiler.addARMInstruction(new ARM_POP(ARMRegister.R1));
        }
        if (ARMRegister.isUsed(0)) {
            compiler.addARMInstruction(new ARM_POP(ARMRegister.R0));
        }

    }

    /**
     * Decompiles the string literal into Deca source code.
     *
     * @param s the output stream
     */
    @Override
    public void decompile(IndentPrintStream s) {
        String escapedValue = value.replace("\\", "\\\\").replace("\"", "\\\"");
        s.print("\"" + escapedValue + "\"");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    /**
     * Returns a string representation of the node.
     *
     * @return a string representing the node
     */
    @Override
    String prettyPrintNode() {
        return "StringLiteral (" + value + ")";
    }

}
