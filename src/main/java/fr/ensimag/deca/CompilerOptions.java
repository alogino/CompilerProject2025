package fr.ensimag.deca;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * User-specified options influencing the compilation.
 *
 * @author gl13
 * @date 01/01/2025
 */
public class CompilerOptions {
    public static final int QUIET = 0;
    public static final int INFO = 1;
    public static final int DEBUG = 2;
    public static final int TRACE = 3;

    public boolean getParse() {
        return parse;
    }

    public boolean getVerify() {
        return verify;
    }

    public boolean getNoCheck() {
        return noCheck;
    }

    public int getDebug() {
        return debug;
    }

    public boolean getParallel() {
        return parallel;
    }

    public boolean getPrintBanner() {
        return printBanner;
    }

    public static int getNbRegisters() {
        return NbRegisters;
    }

    public boolean getWarnings() {
        return warnings;
    }

    public List<File> getSourceFiles() {
        return new ArrayList<File>(sourceFiles);
    }

    private boolean arm = false;

    public boolean getArm() {
        return arm;
    }

    private int debug = QUIET;
    private boolean parallel = false;
    private boolean printBanner = false;
    private boolean parse = false;
    private boolean verify = false;
    private boolean noCheck = false;
    private boolean warnings = false;
    private static final int registerLimitMax = 16;
    private static final int registerLimitMin = 4;
    private static int NbRegisters = registerLimitMax;
    private HashSet<File> sourceFiles = new HashSet<File>();

    /**
     * Parse the command line arguments and configure the compiler options.
     *
     * @param args Array of command line arguments
     * @throws CLIException if the arguments are invalid
     */
    public void parseArgs(String[] args) throws CLIException {
        // no other argument can be passed with the -b argument
        for (String arg : args) {
            if (arg.equals("-b") && args.length > 1) {
                throw new CLIException("Banner option -b cannot be used with other options");
            }
        }

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-b":
                    if (args.length > 1) {
                        throw new CLIException("Banner option -b cannot be used with other options");
                    }
                    printBanner = true;
                    break;
                case "-p":
                    if (verify) {
                        throw new CLIException("Options -p and -v are incompatible.");
                    }
                    parse = true;
                    break;
                case "-v":
                    if (parse) {
                        throw new CLIException("Options -p and -v are incompatible.");
                    }
                    verify = true;
                    break;
                case "-n":
                    noCheck = true;
                    break;
                case "-r":
                    if (i + 1 < args.length) {
                        try {
                            NbRegisters = Integer.parseInt(args[++i]);
                            if (NbRegisters < registerLimitMin || NbRegisters > registerLimitMax) {
                                throw new CLIException("Register limit must be between 4 and 16.");
                            }
                        } catch (NumberFormatException e) {
                            throw new CLIException("Invalid argument for -r, need a number.");
                        }
                    } else {
                        throw new CLIException("Missing argument for -r.");
                    }
                    break;
                case "-d":
                    if (debug < TRACE) { // Repeat the option several times to have more traces.
                        debug++;
                    }
                    break;
                case "-P":
                    parallel = true;
                    break;
                case "-w":
                    warnings = true;
                    break;
                case "-arm":
                    arm = true;
                    break;
                default:
                    if (args[i].startsWith("-")) {
                        throw new CLIException("Invalid option: " + args[i]);
                    } else {
                        addSourceFile(args[i]);
                    }
            }
        }

        if (sourceFiles.isEmpty() && !printBanner) {
            throw new CLIException("No source files.");
        }

        Logger logger = Logger.getRootLogger();

        // map command-line debug option to log4j's level.
        switch (getDebug()) {
            case QUIET:
                logger.setLevel(Level.OFF);
                break; // keep default
            case INFO:
                logger.setLevel(Level.INFO);
                break;
            case DEBUG:
                logger.setLevel(Level.DEBUG);
                break;
            case TRACE:
                logger.setLevel(Level.TRACE);
                break;
            default:
                logger.setLevel(Level.ALL);
                break;
        }
        logger.info("Application-wide trace level set to " + logger.getLevel());

        boolean assertsEnabled = false;
        assert assertsEnabled = true; // Intentional side effect!!!
        if (assertsEnabled) {
            logger.info("Java assertions enabled");
        } else {
            logger.info("Java assertions disabled");
        }
    }

    /**
     * Display usage information for the Deca compiler.
     * Shows all available command line options and their descriptions.
     */
    protected void displayUsage() {
        System.out.println("Expected : decac [[-p | -v] [-n] [-r X] <source files>...] | [-b]\n");
        System.out.println("Options:");
        System.out.println("-b     (banner)        Displays a banner showing the team name.");
        System.out.println(
                "-p     (parse)         Stops decac after the tree building step, and displays the decompilation of the latter.");
        System.out.println(
                "-v     (verification)  Stop decac after the check step (produces no output if there is no error)");
        System.out.println(
                "-n     (no check)      Removes the runtime tests specified in points 11.1 and 11.3 of Deca semantics..");
        System.out.println("-r <X> (registers)     Limit registers to R0 ... R{X-1}, with 4 <= X <= 16.");
        System.out.println(
                "-d     (debug)         Enables debug traces. Repeat the option several times to have more traces.");
        System.out.println(
                "-P     (parallel)      If there are several source files, launch the compilation of the files in parallel (to speed up the compilation).");
        System.out.println(
                "-w     (warnings)      Show warnings during compilation.");
        System.out.println(
                "-arm   (ARM)           Enable ARM architecture-specific extensions.");
        System.out.println(" <source files>        One or more Deca source files to compile.");
    }

    /**
     * Add a source file to the list of files to compile.
     *
     * @param filename Name of the file to add
     * @throws CLIException if the file is invalid or doesn't exist
     */
    private void addSourceFile(String filename) throws CLIException {
        File sourceFile = new File(filename);
        if (!filename.endsWith(".deca")) {
            throw new CLIException("Source file must have .deca extension: " + filename);
        }
        if (!sourceFile.exists()) {
            throw new CLIException("Source file does not exist: " + filename);
        }
        if (!sourceFile.isFile()) {
            throw new CLIException("Source file is not a regular file: " + filename);
        }
        sourceFiles.add(sourceFile);
    }
}
