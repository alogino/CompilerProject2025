package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.instructions.HALT;
import fr.ensimag.arm.pseudocode.ARMGPRegister;
import fr.ensimag.arm.pseudocode.ARMImmediateInteger;
import fr.ensimag.arm.pseudocode.ARMLabel;
import fr.ensimag.arm.pseudocode.ARMRegister;
import fr.ensimag.arm.pseudocode.instructions.ARM_MOV;

import fr.ensimag.deca.codegen.ARMDataSection;

/**
 * Deca complete program (class definition plus main block)
 *
 * @author gl13
 * @date 01/01/2025
 */
public class Program extends AbstractProgram {
    private static final Logger LOG = Logger.getLogger(Program.class);

    /**
     * Constructs a new Program instance.
     *
     * @param classes the list of class declarations (must not be null)
     * @param main    the main block of the program (must not be null)
     */
    public Program(ListDeclClass classes, AbstractMain main) {
        Validate.notNull(classes);
        Validate.notNull(main);
        this.classes = classes;
        this.main = main;
    }

    public ListDeclClass getClasses() {
        return classes;
    }

    public AbstractMain getMain() {
        return main;
    }

    private ListDeclClass classes;
    private AbstractMain main;

    /**
     * Verifies the program's correctness in the context of the Deca language.
     *
     * <p>
     * This method checks the validity of the main block and its declarations.
     * </p>
     *
     * @param compiler the Deca compiler
     * @throws ContextualError if the program is not contextually valid
     */
    @Override
    public void verifyProgram(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify program: start");

        classes.verifyListClass(compiler);
        main.verifyMain(compiler);

        LOG.debug("verify program: end");
    }

    /**
     * Generates assembly code for the Deca program.
     *
     * <p>
     * This method generates code for the main block and appends a HALT instruction.
     * </p>
     *
     * @param compiler the Deca compiler
     */
    @Override
    public void codeGenProgram(DecacCompiler compiler) {
        // use default program
        compiler.addComment("VTABLE");
        classes.codeGenListClassTable(compiler);

        compiler.addComment("Main program");
        main.codeGenMain(compiler);
        compiler.addInstruction(new HALT());

        // switch current block to method block each time
        classes.codeGenListClassMethods(compiler);

        // Generate code for errors in initial block
        compiler.genCodeErrors();
    }

    /**
     * Generates ARM assembly code for the Deca program.
     *
     * @param compiler the Deca compiler
     */
    @Override
    public void ARMCodeGenProgram(DecacCompiler compiler) {

        main.ARMCodeGenMain(compiler);

        // Generate halting instructions
        ARMCodeGenHalt(compiler);

        // Generate main section
        ARMCodeGenMainLabel(compiler);

        // Generate .data section
        ARMDataSection.codeGenDataSection(compiler);

        // Generate header, specifying useful information
        ARMCodeGenHeader(compiler);
    }

    private void ARMCodeGenHeader(DecacCompiler compiler) {
        compiler.addARMRawFirst(".arm");
        compiler.addARMRawFirst(".arch armv7-a");
        compiler.addARMRawFirst(".syntax unified");
    }

    private void ARMCodeGenMainLabel(DecacCompiler compiler) {
        compiler.addARMRawFirst("\n");
        compiler.addARMFirst(new ARM_MOV(ARMRegister.SB, ARMRegister.SP));
        compiler.addARMFirst(new ARM_MOV(ARMRegister.FP, ARMRegister.SP));
        compiler.addARMLabelFirst(new ARMLabel("main"));
        compiler.addARMRawFirst(".section .text");
        compiler.addARMRawFirst(".global main");
        compiler.addARMRawFirst("\n");
    }

    private void ARMCodeGenHalt(DecacCompiler compiler) {
        compiler.addARMRaw("\n");
        compiler.addARMInstruction(new ARM_MOV(ARMGPRegister.getR(7), new ARMImmediateInteger(1)));
        compiler.addARMInstruction(new ARM_MOV(ARMGPRegister.getR(0), new ARMImmediateInteger(0)));
        compiler.addARMRaw("        SVC #0");
    }

    /**
     * Decompiles the program into Deca source code.
     *
     * @param s the stream to write the decompiled code
     */
    @Override
    public void decompile(IndentPrintStream s) {
        getClasses().decompile(s);
        getMain().decompile(s);
    }

    /**
     * Applies a function to all child nodes of the program.
     *
     * @param f the function to apply to each child
     */
    @Override
    protected void iterChildren(TreeFunction f) {
        classes.iter(f);
        main.iter(f);
    }

    /**
     * Pretty-prints the child nodes of the program for debugging purposes.
     *
     * @param s      the stream to write the output
     * @param prefix the prefix for indentation
     */
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        classes.prettyPrint(s, prefix, false);
        main.prettyPrint(s, prefix, true);
    }
}
