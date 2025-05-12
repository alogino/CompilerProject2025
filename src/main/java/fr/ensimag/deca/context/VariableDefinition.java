package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.Location;

/**
 * Definition of a variable.
 *
 * @author gl13
 * @date 01/01/2025
 */
public class VariableDefinition extends ExpDefinition {
    public VariableDefinition(Type type, Location location) {
        super(type, location);
    }

    @Override
    public String getNature() {
        return "variable";
    }

    // useful for verifying variables initializations before printing (e.g.)
    private boolean initialized = false;

    public void setInitializationStatus(boolean status) {
        this.initialized = status;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    @Override
    public boolean isExpression() {
        return true;
    }
}
