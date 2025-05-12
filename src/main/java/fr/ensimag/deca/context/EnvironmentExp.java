package fr.ensimag.deca.context;

import java.util.HashMap;
import java.util.Map;

import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.Location;

/**
 * Dictionary associating identifier's ExpDefinition to their names.
 *
 * This is actually a linked list of dictionaries: each EnvironmentExp has a
 * pointer to a parentEnvironment, corresponding to superblock (eg superclass).
 *
 * The dictionary at the head of this list thus corresponds to the "current"
 * block (eg class).
 *
 * Searching a definition (through method get) is done in the "current"
 * dictionary and in the parentEnvironment if it fails.
 *
 * Insertion (through method declare) is always done in the "current"
 * dictionary.
 *
 * @author gl13
 * @date 01/01/2025
 */
public class EnvironmentExp {
    public EnvironmentExp parentEnvironment;

    private final Map<Symbol, ExpDefinition> envExpr;

    public EnvironmentExp(EnvironmentExp parentEnvironment) {
        this.parentEnvironment = parentEnvironment;

        // Expression's environment associated with predefined types;
        envExpr = new HashMap<Symbol, ExpDefinition>();
    }

    public static class DoubleDefException extends Exception {
        private static final long serialVersionUID = -2733379901827316441L;

        public DoubleDefException(final String message) {
            super(message);
        }
    }

    /**
     * Return the definition of the symbol in the environment
     *
     * @throws ContextualError if the symbol does not exist in the current context
     */
    public ExpDefinition get(Symbol key, Location location) throws ContextualError {
        ExpDefinition def = envExpr.get(key);
        if (def == null){
        }
        // search in parent dictionary in case of failure
        if (def == null && parentEnvironment != null) {
            def = parentEnvironment.get(key, location);
        }
        if (def == null) {
            String errorMessage = String.format("'%s' does not exist in current context of expressions", key.getName());
            throw new ContextualError(errorMessage, location);
        }
        return def;
    }

    /**
     * Add or update the definition associated to a symbol
     *
     * @param sym Symbol for which to add or update
     */
    public void addOrUpdate(Symbol sym, MethodDefinition def) {
        envExpr.put(sym, def);
    }

    /**
     * Return the method definition of a certain index
     */
    public MethodDefinition getMethodOfIndex(int index) {
        for (Symbol sym : envExpr.keySet()) {
            ExpDefinition currDef = envExpr.get(sym);
            if (currDef.isMethod()) {
                MethodDefinition method = (MethodDefinition) currDef;
                if (method.getIndex() == index) {
                    return method;
                }
            }
        }
        if (parentEnvironment != null) {
            return parentEnvironment.getMethodOfIndex(index);
        } else {
            return null;
        }
    }

    /**
     * Return the definition of the symbol for a field in the environment
     *
     * @throws ContextualError if the symbol does not exist in the current context
     */
    public ExpDefinition getAsField(Symbol key, Location location) throws ContextualError {
        ExpDefinition def = envExpr.get(key);
        // search in parent dictionary in case of failure
        if (def == null && parentEnvironment == null) {
            String errorMessage = String.format("Field '%s' does not exist in current context of expressions", key.getName());
            throw new ContextualError(errorMessage, location);
        }

        if (def == null && parentEnvironment != null)  {
            def = parentEnvironment.getAsField(key, location);
        }

        if (!def.isField() && parentEnvironment != null) {
            def = parentEnvironment.getAsField(key, location);
        }

        return def;
    }

    /**
     * Return the definition of the symbol for a method in the environment
     *
     * @throws ContextualError if the symbol does not exist in the current context
     */
    public ExpDefinition getAsMethod(Symbol key, Location location) throws ContextualError {
        ExpDefinition def = envExpr.get(key);
        // search in parent dictionary in case of failure
        if (def == null && parentEnvironment == null) {
            String errorMessage = String.format("Method '%s' does not exist in current context of expressions", key.getName().substring(0, key.getName().length()-2));
            throw new ContextualError(errorMessage, location);
        }

        if (def == null && parentEnvironment != null) {
            def = parentEnvironment.getAsMethod(key, location);
        }
        
        if (!def.isMethod() && parentEnvironment != null) {
            def = parentEnvironment.getAsMethod(key, location);
        }

        return def;
    }

    /**
     * Add the definition def associated to the symbol name in the environment.
     * 
     * Adding a symbol which is already defined in the environment,
     * - throws DoubleDefException if the symbol is in the "current" dictionary
     * - or, hides the previous declaration otherwise.
     *
     * @param name
     *             Name of the symbol to define
     * @param def
     *             Definition of the symbol
     * @throws DoubleDefException
     *                            if the symbol is already defined at the "current"
     *                            dictionary
     */
    public void declare(Symbol name, ExpDefinition def) throws DoubleDefException {
        if (envExpr.containsKey(name)) {
            throw new DoubleDefException("Symbol already used in the environment of expressions: " + name);
        }
        envExpr.put(name, def);
    }


    public void showEnvKey(){
        for (Symbol key : envExpr.keySet()) {
            System.out.println(key.toString()+" : "+envExpr.get(key).toString());
        }
    }
}
