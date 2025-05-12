package fr.ensimag.deca;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

/**
 * Main class for the command-line Deca compiler.
 * Handles the compilation process for one or multiple Deca source files,
 * with support for parallel compilation.
 *
 * @author gl13
 * @date 01/01/2025
 */
public class DecacMain {
    private static Logger LOG = Logger.getLogger(DecacMain.class);

    /**
     * Main method for the Deca compiler.
     * Processes command line arguments and launches compilation.
     * Supports sequential or parallel compilation based on options.
     *
     * @param args Command line arguments
     *             Usage: decac [[-p | -v] [-n] [-r X] [-d]* [-P] [-w] <fichier deca>...] | [-b]
     */
    public static void main(String[] args) {
        // example log4j message.
        LOG.info("Decac compiler started");
        boolean error = false;
        final CompilerOptions options = new CompilerOptions();
        try {
            options.parseArgs(args);
        } catch (CLIException e) {
            System.err.println("Error during option parsing:\n"
                    + e.getMessage());
            options.displayUsage();
            System.exit(1);
        }
        if (options.getPrintBanner()) {
            System.out.println("                                         :                               ");
            System.out.println("                                        ;;                               ");
            System.out.println("                                       / |                               ");
            System.out.println("                                      /  |                               ");
            System.out.println("                                    .'   :                               ");
            System.out.println("                                 .-'     '                               ");
            System.out.println("                             _.-'       /                                ");
            System.out.println("                         .-\"*\"          /                                _ ");
            System.out.println("                      .-'            .'                            _.-*?'");
            System.out.println("                    .'             .'                           .-\"  .'    __");
            System.out.println("                  .'      ,     .-'                           .-+.  .' _.-*\".'");
            System.out.println("                 /        \\  .-'         _.--**\"\"**-.     .-'  _.y-:-\"   .'    ");
            System.out.println("                :          `+.       .*\"\"*.          `.  :-. -.     \\  .'  ");
            System.out.println("                ;        .--*\"\"*--. / __   `  _.--.    \\ |$| -.`   -.;/ _.-+.");
            System.out.println("                :      .'          :*\"  \"*..*\"          y`-' $|      ;*\"  _(    ");
            System.out.println("                 \\    /      +----/ / .'.-'---+  .-._.+' `.  -'_.--. :- \"_(");
            System.out.println("                  `*-:       |    \\/\\/\\/      | /)     `     .'___   ' \"_(");
            System.out.println("   __________        ;    `._|                | \\  )`      .'.'   `./_\" ( ");
            System.out.println("  /\\____;;___\\       :      \\|    ProjetGL    | (`._..--**\" : .-    ; `\"' ");
            System.out.println(" | /         /        \\      |                |  `----**\"T\"\" \" `+.  |     ( (");
            System.out.println(" `. ())oo() .          `.    |    équipe      |         '     .'    :      ) )");
            System.out.println("  |\\(%()*^^()^\\      _.-*\"*- |      13        |            / /      '   ........");
            System.out.println(" %| |-%-------|  .-*\" _      |                |      __..-'\\       /    |      |]");
            System.out.println("% \\ | %  ))   |   \"+,'___..--|                |--**\"\"       `-.__.'     \\      /");
            System.out.println("%  \\|%________|     \"\"       +----------------+                          `----'");
            

            System.exit(0);
        }
        if (options.getSourceFiles().isEmpty()) {
            options.displayUsage();
            System.exit(0);
        }
        if (options.getParallel()) {
            // A FAIRE : instancier DecacCompiler pour chaque fichier à
            // compiler, et lancer l'exécution des méthodes compile() de chaque
            // instance en parallèle. Il est conseillé d'utiliser
            // java.util.concurrent de la bibliothèque standard Java.

            ArrayList<File> sourceFiles = (ArrayList<File>) options.getSourceFiles();

            int nbThreads = Integer.min(Runtime.getRuntime().availableProcessors(), sourceFiles.size());
            ExecutorService executor = Executors.newFixedThreadPool(nbThreads);
            List<Future<Boolean>> futures = new ArrayList<>();

            for (File sourceFile : sourceFiles) {
                Future<Boolean> future = executor.submit(() -> {
                    DecacCompiler compiler = new DecacCompiler(options, sourceFile);
                    synchronized (System.out) {
                        return compiler.compile();
                    }
                });
                futures.add(future);
            }

            // We wait for all compilations
            try {
                for (Future<Boolean> future : futures) {
                    if (future.get()) {
                        error = true;
                    }
                }
            } catch (Exception e) {
                LOG.error("Error during parallel compilation: " + e.getMessage());
                error = true;
            } finally {
                executor.shutdown();
            }

        } else {
            for (File source : options.getSourceFiles()) {
                DecacCompiler compiler = new DecacCompiler(options, source);
                if (compiler.compile()) {
                    error = true;
                }
            }
        }
        System.exit(error ? 1 : 0);
    }
}