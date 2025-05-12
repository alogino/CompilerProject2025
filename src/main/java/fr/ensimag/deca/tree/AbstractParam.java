package fr.ensimag.deca.tree;

import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * Abstract class representing a method parameter in Deca.
 */
public abstract class AbstractParam extends Tree {

    /**
     * Get the type of the parameter.
     *
     * @return The parameter's type.
     */
    public abstract AbstractIdentifier getType();

    /**
     * Get the name of the parameter.
     *
     * @return The parameter's name.
     */
    public abstract String getName();

    /**
     * Decompile the parameter declaration.
     *
     * @param s The stream to write the decompiled parameter to.
     */
    @Override
    public abstract void decompile(IndentPrintStream s);
}
