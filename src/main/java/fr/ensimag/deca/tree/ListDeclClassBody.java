package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;


/**
 * Represents a list of class body declarations (fields and methods).
 */
public class ListDeclClassBody extends TreeList<AbstractDeclClassBody> {

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclClassBody decl : getList()) {
            decl.decompile(s);
        }
    }

    private ListDeclField fields = new ListDeclField();
    private ListDeclMethod methods = new ListDeclMethod();

    public void setFields(ListDeclField fields) {
        this.fields = fields;
    }

    public ListDeclField getFields() {
        return fields;
    }

    public void setMethods(ListDeclMethod methods) {
        this.methods = methods;
    }

    public ListDeclMethod getMethods() {
        return methods;
    }
    /**
     * Verifies all the declarations in the class body.
     *
     * @param compiler the compiler
     * @throws ContextualError if a contextual error is encountered
     */
    public void verifyClassBody(DecacCompiler compiler) throws ContextualError {
        for (AbstractDeclClassBody decl : getList()) {
            decl.verifyClassBody(compiler);
        }
    }
}
