package fr.ensimag.deca;

public class TestCompilerOptions {
    public static void main(String[] args) {
        testBannerOption();
        testParseAndVerifyOptions();
        testDebugLevels();
        testRegisterLimit();
        testWarningsOption();
        testParallelOption();
        testNoCheckOption();
        testInvalidOptions();
        testSourceFiles();
    }

    // Test -b option
    public static void testBannerOption() {
        System.out.println("=== Test: Banner Option ===");
        CompilerOptions options = new CompilerOptions();
        try {
            options.parseArgs(new String[]{"-b"});
            System.out.println("Print banner: " + options.getPrintBanner());
            assert options.getPrintBanner();
        } catch (CLIException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Test -p and -v options incompatibility
    public static void testParseAndVerifyOptions() {
        System.out.println("=== Test: Parse and Verify Options ===");
        CompilerOptions options = new CompilerOptions();
        try {
            options.parseArgs(new String[]{"-p", "-v"});
        } catch (CLIException e) {
            System.out.println("Expected error: " + e.getMessage());
            assert e.getMessage().contains("Options -p and -v are incompatible.\n");
        }

        try {
            options.parseArgs(new String[]{"-p","src/test/deca/syntax/valid/provided/hello2.deca"});
            System.out.println("Parse: " + options.getParse());
            assert options.getParse();
        } catch (CLIException e) {
            System.err.println("Error: " + e.getMessage());
        }
        try {
            options.parseArgs(new String[]{"-v","src/test/deca/syntax/valid/provided/hello2.deca"});
            System.out.println("Verify: " + options.getVerify());
            assert options.getVerify();
        } catch (CLIException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Test -d option with multiple levels
    public static void testDebugLevels() {
        System.out.println("=== Test: Debug Levels ===");
        CompilerOptions options = new CompilerOptions();
        try {
            options.parseArgs(new String[]{"-d", "-d", "-d", "src/test/deca/syntax/valid/provided/hello2.deca"});
            System.out.println("Debug level: " + options.getDebug());
            assert options.getDebug() == 3;
        } catch (CLIException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Test -r option with valid and invalid values
    public static void testRegisterLimit() {
        System.out.println("=== Test: Register Limit ===");
        CompilerOptions options = new CompilerOptions();
        try {
            options.parseArgs(new String[]{"-r", "8","src/test/deca/syntax/valid/provided/hello2.deca"});
            System.out.println("NbRegisters: " + options.getNbRegisters());
            assert options.getNbRegisters() == 8;
        } catch (CLIException e) {
            System.err.println("Error: " + e.getMessage());
        }

        try {
            options.parseArgs(new String[]{"-r", "20"}); // Invalid value
        } catch (CLIException e) {
            System.out.println("Expected error: " + e.getMessage());
            assert e.getMessage().contains("Register limit must be between 4 and 16.");
        }
    }

    // Test -w option
    public static void testWarningsOption() {
        System.out.println("=== Test: Warnings Option ===");
        CompilerOptions options = new CompilerOptions();
        try {
            options.parseArgs(new String[]{"-w","src/test/deca/syntax/valid/provided/hello2.deca"});
            System.out.println("Warnings: " + options.getWarnings());
            assert options.getWarnings();
        } catch (CLIException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Test -P option
    public static void testParallelOption() {
        System.out.println("=== Test: Parallel Option ===");
        CompilerOptions options = new CompilerOptions();
        try {
            options.parseArgs(new String[]{"-P","src/test/deca/syntax/valid/provided/hello2.deca", "src/test/deca/syntax/valid/provided/hello.deca"});
            System.out.println("Parallel: " + options.getParallel());
            assert options.getParallel();
        } catch (CLIException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Test -n option
    public static void testNoCheckOption() {
        System.out.println("=== Test: No Check Option ===");
        CompilerOptions options = new CompilerOptions();
        try {
            options.parseArgs(new String[]{"-n", "src/test/deca/syntax/valid/provided/hello2.deca"});
            System.out.println("NoCheck: " + options.getNoCheck());
            assert options.getNoCheck();
        } catch (CLIException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Test invalid options
    public static void testInvalidOptions() {
        System.out.println("=== Test: Invalid Options ===");
        CompilerOptions options = new CompilerOptions();
        try {
            options.parseArgs(new String[]{"-x"});
        } catch (CLIException e) {
            System.out.println("Expected error: " + e.getMessage());
            assert e.getMessage().contains("Invalid option: -x");
        }
    }

    // Test valid and invalid source files
    public static void testSourceFiles() {
        System.out.println("=== Test: Source Files ===");
        CompilerOptions options = new CompilerOptions();
        try {
            options.parseArgs(new String[]{ "src/test/deca/syntax/valid/provided/hello2.deca", "src/test/deca/syntax/valid/provided/hello.deca"});
            System.out.println("Source files: " + options.getSourceFiles());
            assert options.getSourceFiles().size() == 2;
        } catch (CLIException e) {
            System.err.println("Error: " + e.getMessage());
        }

        try {
            options.parseArgs(new String[]{"invalid_file.txt"}); // Invalid file
        } catch (CLIException e) {
            System.out.println("Expected error: " + e.getMessage());
            assert e.getMessage().contains("Invalid source file");
        }
    }
}


