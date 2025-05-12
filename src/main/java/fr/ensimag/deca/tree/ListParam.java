package fr.ensimag.deca.tree;

import fr.ensimag.deca.tools.IndentPrintStream;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a list of method parameters.
 */
public class ListParam extends TreeList<AbstractParam> {

    /**
     * Add a parameter to the list.
     *
     * @param param The parameter to add.
     */
    public void add(AbstractParam param) {

        super.add(param);
    }

    @Override
    public void decompile(IndentPrintStream s) {
            boolean first = true;
            for (AbstractParam param : getList()) {
                if (!first) {
                    s.print(", ");
                }
                param.decompile(s);
                first = true;
            }
    }
}
