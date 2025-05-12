package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * Represents a list of method parameters.
 */
public class ListDeclParam extends TreeList<AbstractDeclParam> {

    /**
     * Add a parameter to the list.
     *
     * @param param The parameter to add.
     */
    public void add(AbstractDeclParam param) {

        super.add(param);
    }

    public Signature verifyListDeclParam(DecacCompiler compiler, EnvironmentExp methodEnvExp) throws ContextualError {
        Signature paramListSig = new Signature();
        int index = 1;
        for (AbstractDeclParam param : getList()) {
            paramListSig.add(param.verifyDeclParam(compiler, methodEnvExp));

            ParamDefinition paramDef = ((DeclParam) param).getParamDefinition();
            paramDef.setIndex(index++);
        }
        return paramListSig;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        boolean comma = false;
        for (AbstractParam param : getList()) {
            if (comma) {
                s.print(", ");
            }
            param.decompile(s);
            comma = true;
        }
    }
}
