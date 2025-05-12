package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

/**
 * Represents a list of method declarations in a class.
 */
public class ListDeclMethod extends TreeList<AbstractDeclMethod> {

    public void verifyListMethod(DecacCompiler compiler, ClassDefinition currentClass)
            throws ContextualError {
        // update number of methods of current class to be at least that of its parent
        currentClass.setNumberOfMethods(currentClass.getSuperClass().getNumberOfMethods());

        for (AbstractDeclMethod declMethod : getList()) {
            MethodDefinition methodDef = declMethod.verifyDeclMethod(compiler, currentClass);
            // trust
            Symbol methodSym = compiler.createSymbol(((DeclMethod) declMethod).getMethodName().getName().getName() + ".m");
            currentClass.getMembers().addOrUpdate(methodSym,
                    methodDef);
        }

    }

    public void verifyListMethodBody(DecacCompiler compiler, ClassDefinition currentClass)
            throws ContextualError {
        for (AbstractDeclMethod declMethod : getList()) {
            declMethod.verifyMethodBody(compiler, currentClass);
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclMethod method : getList()) {
            method.decompile(s);
        }
    }
}
