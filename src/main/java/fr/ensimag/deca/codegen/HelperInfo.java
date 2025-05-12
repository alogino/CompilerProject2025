package fr.ensimag.deca.codegen;

import fr.ensimag.deca.tree.DeclMethod;

/**
 * Helper class maintaining some information used during code generation
 * 
 * @author gl13
 * @date 16/01/2025
 */
public class HelperInfo {
    private static DeclMethod currentMethod;

    public static DeclMethod getCurrentMethod() {
        return currentMethod;
    }

    public static void setCurrentMethod(DeclMethod method) {
        currentMethod = method;
    }
}
