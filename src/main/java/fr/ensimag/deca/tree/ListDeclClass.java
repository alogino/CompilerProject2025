package fr.ensimag.deca.tree;

import org.apache.log4j.Logger;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.StackCount;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.TSTO;

/**
 *
 * @author gl13
 * @date 01/01/2025
 */
public class ListDeclClass extends TreeList<AbstractDeclClass> {
    private static final Logger LOG = Logger.getLogger(ListDeclClass.class);

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclClass c : getList()) {
            c.decompile(s);
            s.println();
        }
    }

    /**
     * Pass 1 of [SyntaxeContextuelle]
     */
    void verifyListClass(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify listClass: start");
        for (AbstractDeclClass decl : getList()) {
            decl.verifyClass(compiler);
        }
        verifyListClassMembers(compiler);
        LOG.debug("verify listClass: end");
    }

    /**
     * Pass 2 of [SyntaxeContextuelle]
     */
    public void verifyListClassMembers(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify listClassMemebers: start");
        for (AbstractDeclClass decl : getList()) {
            decl.verifyClassMembers(compiler);
        }
        LOG.debug("verify listClassMemebers: end");
        verifyListClassBody(compiler);
    }

    /**
     * Pass 3 of [SyntaxeContextuelle]
     */
    public void verifyListClassBody(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify listClassBody: start");
        for (AbstractDeclClass decl : getList()) {
            decl.verifyClassBody(compiler);
        }
        LOG.debug("verify listClassBody: end");
    }

    /**
     * Generate VTable entry for all classes
     */
    public void codeGenListClassTable(DecacCompiler compiler) {
        if (getList().isEmpty()) {
            return;
        }
        // Generate VTable entry for internal Object class
        compiler.addComment("VTABLE of class Object");
        compiler.addInstruction(new LOAD(new NullOperand(), Register.R0));
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(1, Register.GB)));
        compiler.addInstruction(new LOAD(new LabelOperand(new Label("code.Object.equals")), Register.R0));
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(2, Register.GB)));

        // Generate VTable entries for user-defined classes
        for (AbstractDeclClass decl : getList()) {
            decl.codeGenTable(compiler);
        }

        // add VTable size to StakCount to account for it for ADDSP
        StackCount.addVarCount(DeclClass.getVTableSize());
    }

    /**
     * Generate code for methods for all classes
     */
    public void codeGenListClassMethods(DecacCompiler compiler) {
        // Generate the code for the equals method of Object class
        if (getList().isEmpty()) {
            return;
        }
        compiler.addComment("--- Generating methods for class Object ---");
        compiler.addLabel(new Label("code.Object.equals"));
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new TSTO(1));
            compiler.addInstruction(new BOV(new Label("stack_overflow_error")));
        }
        compiler.addInstruction(new ADDSP(1));

        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R0));
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.R0), Register.R0));

        compiler.addInstruction(new LOAD(new RegisterOffset(-3, Register.LB), Register.R1));
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.R1), Register.R1));

        compiler.addInstruction(new CMP(Register.R0, Register.R1));
        compiler.addInstruction(new BEQ(new Label("if.Object.equals")));
        compiler.addInstruction(new LOAD(0, Register.R0));
        compiler.addInstruction(new BSR(new Label("end_if.Object.equals")));
        compiler.addLabel(new Label("if.Object.equals"));
        compiler.addInstruction(new LOAD(1, Register.R0));
        compiler.addLabel(new Label("end_if.Object.equals"));
        compiler.addInstruction(new RTS());

        for (AbstractDeclClass decl : getList()) {
            decl.codeGenClass(compiler);
        }
    }
}
