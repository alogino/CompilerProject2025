package fr.ensimag.deca;

import fr.ensimag.arm.pseudocode.ARMInstruction;
import fr.ensimag.arm.pseudocode.ARMLabel;
import fr.ensimag.arm.pseudocode.ARMLine;
import fr.ensimag.arm.pseudocode.ARMProgram;
import fr.ensimag.arm.pseudocode.AbstractARMLine;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.syntax.DecaLexer;
import fr.ensimag.deca.syntax.DecaParser;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.AbstractProgram;
import fr.ensimag.deca.tree.LocationException;
import fr.ensimag.ima.pseudocode.AbstractLine;
import fr.ensimag.ima.pseudocode.IMAProgram;
import fr.ensimag.ima.pseudocode.ImmediateString;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Line;
import fr.ensimag.ima.pseudocode.instructions.ERROR;
import fr.ensimag.ima.pseudocode.instructions.WNL;
import fr.ensimag.ima.pseudocode.instructions.WSTR;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.log4j.Logger;

/**
 * Decac compiler instance.
 *
 * This class is to be instantiated once per source file to be compiled. It
 * contains the meta-data used for compiling (source file name, compilation
 * options) and the necessary utilities for compilation (symbol tables, abstract
 * representation of target file, ...).
 *
 * It contains several objects specialized for different tasks. Delegate methods
 * are used to simplify the code of the caller (e.g. call
 * compiler.addInstruction() instead of compiler.getProgram().addInstruction()).
 *
 * @author gl13
 * @date 01/01/2025
 */
public class DecacCompiler {
    private static final Logger LOG = Logger.getLogger(DecacCompiler.class);
    private int labelCounter = 0;
    /**
     * Portable newline character.
     */
    private static final String nl = System.getProperty("line.separator", "\n");

    /**
     * Creates a new DecacCompiler instance.
     *
     * @param compilerOptions Compilation options (e.g. debug, optimization levels)
     * @param source          Source file to compile
     */
    public DecacCompiler(CompilerOptions compilerOptions, File source) {
        super();
        this.compilerOptions = compilerOptions;
        this.source = source;

    }

    public EnvironmentType getEnvTypes() {
        return environmentType;
    }

    /**
     * Source file associated with this compiler instance.
     */
    public File getSource() {
        return source;
    }

    /**
     * Compilation options (e.g. when to stop compilation, number of registers
     * to use, ...).
     */
    public CompilerOptions getCompilerOptions() {
        return compilerOptions;
    }

    /**
     * @see
     *      fr.ensimag.ima.pseudocode.IMAProgram#add(fr.ensimag.ima.pseudocode.AbstractLine)
     */
    public void add(AbstractLine line) {
        currentBlock.add(line);
    }

    /**
     * @see fr.ensimag.ima.pseudocode.IMAProgram#addComment(java.lang.String)
     */
    public void addComment(String comment) {
        currentBlock.addComment(comment);
    }

    /**
     * @see
     *      fr.ensimag.ima.pseudocode.IMAProgram#addLabel(fr.ensimag.ima.pseudocode.Label)
     */
    public void addLabel(Label label) {
        currentBlock.addLabel(label);
    }

    /**
     * @see
     *      fr.ensimag.ima.pseudocode.IMAProgram#addLabel(fr.ensimag.ima.pseudocode.Label)
     */
    public void addLabelFirst(Label label) {
        currentBlock.addFirst(new Line(label));
    }

    /**
     * @see
     *      fr.ensimag.ima.pseudocode.IMAProgram#addInstruction(fr.ensimag.ima.pseudocode.Instruction)
     */
    public void addInstruction(Instruction instruction) {
        currentBlock.addInstruction(instruction);
    }

    /**
     * @see
     *      fr.ensimag.ima.pseudocode.IMAcurrentBlock#addInstruction(fr.ensimag.ima.pseudocode.Instruction,
     *      java.lang.String)
     */
    public void addInstruction(Instruction instruction, String comment) {
        currentBlock.addInstruction(instruction, comment);
    }

    /**
     * @see
     *      fr.ensimag.ima.pseudocode.IMAProgram#addInstruction(fr.ensimag.ima.pseudocode.Instruction)
     */
    public void addFirst(Line line) {
        currentBlock.addFirst(line);
    }

    /**
     * @see
     *      fr.ensimag.ima.pseudocode.IMAProgram#addInstruction(fr.ensimag.ima.pseudocode.Instruction)
     */
    public void addFirst(Instruction instruction) {
        currentBlock.addFirst(instruction);
    }

    /**
     * @see
     *      fr.ensimag.ima.pseudocode.IMAProgram#addInstruction(fr.ensimag.ima.pseudocode.Instruction,
     *      java.lang.String)
     */
    public void addFirst(Instruction instruction, String comment) {
        currentBlock.addFirst(instruction, comment);
    }

    // ---------- ARM code gen util methods

    /**
     * @see
     *      fr.ensimag.arm.pseudocode.ARMProgram#add(fr.ensimag.arm.pseudocode.AbstractARMLine)
     */
    public void addARM(AbstractARMLine line) {
        currentARMBlock.add(line);
    }

    /**
     * @see
     *      fr.ensimag.arm.pseudocode.ARMProgram#add(fr.ensimag.arm.pseudocode.AbstractARMLine)
     */
    public void addARMRaw(String rawLine) {
        currentARMBlock.add(rawLine, true);
    }

    /**
     * @see
     *      fr.ensimag.arm.pseudocode.ARMProgram#add(fr.ensimag.arm.pseudocode.AbstractARMLine)
     */
    public void addARMRawFirst(String rawLine) {
        currentARMBlock.addFirst(rawLine, true);
    }

    /**
     * @see fr.ensimag.arm.pseudocode.ARMProgram#addComment(java.lang.String)
     */
    public void addARMComment(String comment) {
        currentARMBlock.addComment(comment);
    }

    /**
     * @see
     *      fr.ensimag.arm.pseudocode.ARMProgram#addLabel(fr.ensimag.arm.pseudocode.ARMLabel)
     */
    public void addARMLabel(ARMLabel label) {
        currentARMBlock.addLabel(label);
    }

    /**
     * @see
     *      fr.ensimag.arm.pseudocode.ARMProgram#addLabel(fr.ensimag.arm.pseudocode.ARMLabel)
     */
    public void addARMLabelFirst(ARMLabel label) {
        currentARMBlock.addFirst(new ARMLine(label));
    }

    /**
     * @see
     *      fr.ensimag.arm.pseudocode.ARMProgram#addARMInstruction(fr.ensimag.arm.pseudocode.ARMInstruction)
     */
    public void addARMInstruction(ARMInstruction instruction) {
        currentARMBlock.addARMInstruction(instruction);
    }

    /**
     * @see
     *      fr.ensimag.arm.pseudocode.ARMProgram#addARMInstruction(fr.ensimag.arm.pseudocode.ARMInstruction,
     *      java.lang.String)
     */
    public void addARMInstruction(ARMInstruction instruction, String comment) {
        currentARMBlock.addARMInstruction(instruction, comment);
    }

    /**
     * @see
     *      fr.ensimag.arm.pseudocode.ARMProgram#addARMInstruction(fr.ensimag.arm.pseudocode.ARMInstruction)
     */
    public void addARMFirst(ARMLine line) {
        currentARMBlock.addFirst(line);
    }

    /**
     * @see
     *      fr.ensimag.arm.pseudocode.ARMProgram#addARMInstruction(fr.ensimag.arm.pseudocode.ARMInstruction)
     */
    public void addARMFirst(ARMInstruction instruction) {
        currentARMBlock.addFirst(instruction);
    }

    /**
     * @see
     *      fr.ensimag.arm.pseudocode.ARMProgram#addARMInstruction(fr.ensimag.arm.pseudocode.ARMInstruction,
     *      java.lang.String)
     */
    public void addARMFirst(ARMInstruction instruction, String comment) {
        currentARMBlock.addFirst(instruction, comment);
    }

    /** represents whether an execution error can occur */
    private boolean possibleError = false;

    /**
     * Generates assembly code for all errors that could occur on execution of
     * compiled code
     */
    public void genCodeErrors() {
        if (getCompilerOptions().getArm()) {
            resetCurrentARMBlockToDefault();
        } else {
            resetCurrentBlockToDefault();
        }

        if (!possibleError) {
            return;
        }

        addComment("Errors:");
        genCodeIOError();

        // -n option
        if (getCompilerOptions().getNoCheck()) {
            return;
        }

        genCodeStackOverflowError();
        genCodeOverflowError();
        genCodeNullDereferenceError();
        genCodeFullHeapError();
    }

    /** represents whether a stack overflow error is possible */
    private boolean possibleStackOverflow = false;

    /**
     * Notifies the compiler that a stack overflow error can occur
     */
    public void setPossibleStackOverflow() {
        this.possibleStackOverflow = true;
        this.possibleError = true;
    }

    /**
     * Generates assembly code for notifying of stack overflow error. Does so only
     * if there is a need for stack overflow error checking
     */
    private void genCodeStackOverflowError() {
        // do not generate stack overflow error code block if it isn't necessary
        if (!this.possibleStackOverflow) {
            return;
        }

        addLabel(new Label("stack_overflow_error"));
        addInstruction(
                new WSTR(new ImmediateString(
                        "ERROR: Stack overflow detected, terminating program execution with error.")),
                "Stack overflow error message");
        addInstruction(new WNL());
        addInstruction(new ERROR());
    }

    /** represents whether a stack overflow error is possible */
    private boolean possibleOverflow = false;

    /**
     * Notifies the compiler that a stack overflow error can occur
     */
    public void setPossibleOverflow() {
        this.possibleOverflow = true;
        this.possibleError = true;
    }

    /**
     * Generates assembly code for notifying of overflow error (on arithmetic
     * divide/multiply). Does so only if there is a need for overflow error checking
     */
    private void genCodeOverflowError() {
        // do not generate stack overflow error code block if it isn't necessary
        if (!this.possibleOverflow) {
            return;
        }

        addLabel(new Label("overflow_error"));
        addInstruction(
                new WSTR(new ImmediateString(
                        "ERROR: Overflow detected, terminating program execution with error.")),
                "Overflow error message");
        addInstruction(new WNL());
        addInstruction(new ERROR());
    }

    /** represents whether an IO error is possible */
    private boolean possibleIOError = false;

    /**
     * Notifies the compiler that an IO error can occur
     */
    public void setPossibleIOError() {
        this.possibleIOError = true;
        this.possibleError = true;
    }

    /**
     * Generates assembly code for notifying of IO error (on readInt or readFloat).
     * Does so only if there is a need for IO error checking
     */
    private void genCodeIOError() {
        // do not generate stack overflow error code block if it isn't necessary
        if (!this.possibleIOError) {
            return;
        }

        addLabel(new Label("IO_error"));
        addInstruction(
                new WSTR(new ImmediateString(
                        "ERROR: IO error detected, terminating program execution with error.")),
                "IO error message");
        addInstruction(new WNL());
        addInstruction(new ERROR());
    }

    /** represents whether a null dereference error is possible */
    private boolean possibleNullDereferenceError = false;

    /**
     * Notifies the compiler that an null dereference error can occur
     */
    public void setPossibleNullDereferenceError() {
        this.possibleNullDereferenceError = true;
        this.possibleError = true;
    }

    /**
     * Generates assembly code for notifying of IO error (on readInt or readFloat).
     * Does so only if there is a need for IO error checking
     */
    private void genCodeNullDereferenceError() {
        // do not generate stack overflow error code block if it isn't necessary
        if (!this.possibleNullDereferenceError) {
            return;
        }

        addLabel(new Label("null_dereference_error"));
        addInstruction(
                new WSTR(new ImmediateString(
                        "ERROR: null dereference detected, terminating program execution with error.")),
                "null dereference error message");
        addInstruction(new WNL());
        addInstruction(new ERROR());
    }

    /** represents whether a null dereference error is possible */
    private boolean possibleFullHeapError = false;

    /**
     * Notifies the compiler that an null dereference error can occur
     */
    public void setPossibleFullHeapError() {
        this.possibleFullHeapError = true;
        this.possibleError = true;
    }

    /**
     * Generates assembly code for notifying of IO error (on readInt or readFloat).
     * Does so only if there is a need for IO error checking
     */
    private void genCodeFullHeapError() {
        // do not generate stack overflow error code block if it isn't necessary
        if (!this.possibleFullHeapError) {
            return;
        }

        addLabel(new Label("full_heap_error"));
        addInstruction(
                new WSTR(new ImmediateString(
                        "ERROR: heap is full, cannot allocate, terminating program execution with error.")),
                "null dereference error message");
        addInstruction(new WNL());
        addInstruction(new ERROR());
    }

    /**
     * @see
     *      fr.ensimag.ima.pseudocode.IMAProgram#display()
     */
    public String displayIMAProgram() {
        return program.display();
    }

    private final CompilerOptions compilerOptions;
    private final File source;

    /**
     * The main program. Every instruction generated will eventually end up here.
     */
    private final IMAProgram program = new IMAProgram();

    public void appendBlock(IMAProgram block) {
        program.append(block);
    }

    private IMAProgram currentBlock = program;

    public IMAProgram getCurrentBlock() {
        return currentBlock;
    }

    public void setCurrentBlock(IMAProgram currentBlock) {
        this.currentBlock = currentBlock;
    }

    private void resetCurrentBlockToDefault() {
        currentBlock = program;
    }

    /**
     * The main program. Every instruction generated will eventually end up here.
     */
    private final ARMProgram ARMProgram = new ARMProgram();

    public void appendARMBlock(ARMProgram block) {
        ARMProgram.append(block);
    }

    private ARMProgram currentARMBlock = ARMProgram;

    public ARMProgram getCurrentARMBlock() {
        return currentARMBlock;
    }

    public void setCurrentARMBlock(ARMProgram currentARMBlock) {
        this.currentARMBlock = currentARMBlock;
    }

    private void resetCurrentARMBlockToDefault() {
        currentARMBlock = ARMProgram;
    }

    /** The global environment for types (and the symbolTable) */
    // j'ai inversé les 2 lignes
    public final SymbolTable symbolTable = new SymbolTable();
    public final EnvironmentType environmentType = new EnvironmentType(this);

    /**
     * Creates a new symbol or returns the existing one.
     * Used during lexical analysis to maintain a unique instance
     * for each identifier.
     *
     * @param name Name of the symbol to create/find
     * @return The unique Symbol instance for this name
     */
    public Symbol createSymbol(String name) {
        return symbolTable.create(name);
    }

    /**
     * Run the compiler (parse source file, generate code)
     *
     * @return true on error
     */
    public boolean compile() {
        String assemblyFileExt;
        if (getCompilerOptions().getArm()) {
            assemblyFileExt = ".s";
        } else {
            assemblyFileExt = ".ass";
        }

        String sourceFile = source.getAbsolutePath();
        String destFile = sourceFile.substring(0, sourceFile.lastIndexOf(".")) + assemblyFileExt;

        PrintStream err = System.err;
        PrintStream out = System.out;
        LOG.debug("Compiling file " + sourceFile + " to assembly file " + destFile);
        try {
            return doCompile(sourceFile, destFile, out, err);
        } catch (LocationException e) {
            e.display(err);
            return true;
        } catch (DecacFatalError e) {
            err.println(e.getMessage());
            return true;
        } catch (StackOverflowError e) {
            LOG.debug("stack overflow", e);
            err.println("Stack overflow while compiling file " + sourceFile + ".");
            return true;
        } catch (Exception e) {
            LOG.fatal("Exception raised while compiling file " + sourceFile
                    + ":", e);
            err.println("Internal compiler error while compiling file " + sourceFile + ", sorry.");
            // NOTE : print stacktrace on internal compiler error
            e.printStackTrace();
            return true;
        } catch (AssertionError e) {
            LOG.fatal("Assertion failed while compiling file " + sourceFile
                    + ":", e);
            err.println("Internal compiler error while compiling file " + sourceFile + ", sorry.");
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Internal function responsible for compiling (lexer, verification, and code
     * generation).
     *
     * @param sourceName the source file to compile
     * @param destName   the destination file to generate
     * @param out        stream for standard output (e.g., decac -p)
     * @param err        stream for error messages
     * @return true if an error occurred, false otherwise
     * @throws DecacFatalError   if a critical error prevents compilation
     * @throws LocationException if a syntax or semantic error is detected
     */
    private boolean doCompile(String sourceName, String destName, PrintStream out, PrintStream err)
            throws DecacFatalError, LocationException {
        // Step 1: Lexing and parsing
        AbstractProgram prog = doLexingAndParsing(sourceName, err);
        if (prog == null) {
            LOG.info("Parsing failed");
            return true;
        }

        // Step 2: Handle -p (print) option
        if (compilerOptions.getParse()) {
            prog.decompile(out);
            return false;
        }

        // Step 3: Contextual verification
        prog.verifyProgram(this);
        assert (prog.checkAllDecorations());

        // Step 4: Handle -v (verify only) option
        if (compilerOptions.getVerify()) {
            return false;
        }

        // Step 6: Generate main program

        if (getCompilerOptions().getArm()) {
            prog.ARMCodeGenProgram(this);
        } else {
            addComment("Start of the main program");
            prog.codeGenProgram(this);
            addComment("End of the main program");
        }

        LOG.debug("Generated assembly code:\n" + program.display());
        LOG.info("Output file: " + destName);

        // Step 7: Write to the destination file
        try (FileOutputStream fstream = new FileOutputStream(destName)) {
            LOG.info("Writing assembly file...");
            if (getCompilerOptions().getArm()) {
                ARMProgram.display(new PrintStream(fstream));
            } else {
                program.display(new PrintStream(fstream));
            }
        } catch (IOException e) {
            throw new DecacFatalError("Failed to write to output file: " + e.getLocalizedMessage());
        }

        LOG.info("Compilation of " + sourceName + " completed successfully.");
        return false;
    }

    public EnvironmentType getEnvironmentType() {
        return environmentType;
    }

    /**
     * Build and call the lexer and parser to build the primitive abstract
     * syntax tree.
     *
     * @param sourceName Name of the file to parse
     * @param err        Stream to send error messages to
     * @return the abstract syntax tree
     * @throws DecacFatalError    When an error prevented opening the source file
     * @throws DecacInternalError When an inconsistency was detected in the
     *                            compiler.
     * @throws LocationException  When a compilation error (incorrect program)
     *                            occurs.
     */
    protected AbstractProgram doLexingAndParsing(String sourceName, PrintStream err)
            throws DecacFatalError, DecacInternalError {
        DecaLexer lex;
        try {
            lex = new DecaLexer(CharStreams.fromFileName(sourceName));
        } catch (IOException ex) {
            throw new DecacFatalError("Failed to open input file: " + ex.getLocalizedMessage());
        }
        lex.setDecacCompiler(this);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        DecaParser parser = new DecaParser(tokens);
        parser.setDecacCompiler(this);
        return parser.parseProgramAndManageErrors(err);
    }

    /**
     * Crée une étiquette unique avec un préfixe donné.
     *
     * @param prefix Le préfixe de l'étiquette.
     * @return Une nouvelle étiquette unique.
     */
    public Label createLabel(String prefix) {
        labelCounter++;
        return new Label(prefix + "_" + labelCounter);
    }

}
