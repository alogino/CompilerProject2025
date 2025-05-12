package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * Represents a list of field declarations in a class.
 */
public class ListDeclField extends TreeList<AbstractDeclField> {

    public void verifyListField(DecacCompiler compiler, ClassDefinition currentClass)
            throws ContextualError {
        // update number of fields of current class to be at least that of its parent
        currentClass.setNumberOfFields(currentClass.getSuperClass().getNumberOfFields());

        for (AbstractDeclField declField : getList()) {
            declField.verifyFieldType(compiler, currentClass);
        }
    }

    public void verifyListFieldBody(DecacCompiler compiler, ClassDefinition currentClass)
            throws ContextualError {
        for (AbstractDeclField declField : getList()) {
            declField.verifyFieldBody(compiler, currentClass);
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclField field : getList()) {
            field.decompile(s);
        }
    }
}
