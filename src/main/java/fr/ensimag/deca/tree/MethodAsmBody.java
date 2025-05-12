package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Line;

import java.io.PrintStream;

/**
 * Represents a method body containing assembly code.
 */
public class MethodAsmBody extends AbstractMethodBody {
    private final String assemblyCode;

    public MethodAsmBody(String assemblyCode) {
        this.assemblyCode = assemblyCode;
    }

    public String getAssemblyCode() {
        return assemblyCode;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("asm(");
        s.print(assemblyCode);
        s.print(");");
    }

    @Override
    public void codeGenInst(DecacCompiler compiler) {
        compiler.addComment("Start of assembly code in MethodAsmBody");

        String[] assemblyLines = assemblyCode.split("\n");
        for (String line : assemblyLines) {
            if (!line.trim().isEmpty()) {
                compiler.addComment("ASM: " + line.trim());
            }
        }

        compiler.addComment("End of assembly code in MethodAsmBody");
    }

    @Override
    public void ARMCodeGenInst(DecacCompiler compiler) {
        // TODO Auto-generated method stub

    }

    @Override
    public void verifyMethodBody(DecacCompiler compiler, ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        if (assemblyCode == null || assemblyCode.isEmpty()) {
            throw new ContextualError("Assembly code in MethodAsmBody cannot be null or empty", getLocation());
        }
        compiler.addComment("Verified assembly code body");
    }

    @Override
    public void setMethodEnvExp(EnvironmentExp methodEnvExp) {
        // Do nothing
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // No children
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // No children
    }
}
