package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.arm.pseudocode.ARMDAddr;

/**
 * Definition associated to identifier in expressions.
 *
 * @author gl13
 * @date 01/01/2025
 */
public abstract class ExpDefinition extends Definition {

    public void setOperand(DAddr operand) {
        this.operand = operand;
    }

    public DAddr getOperand() {
        return operand;
    }

    private DAddr operand;

    public void setARMOperand(ARMDAddr ARMoperand) {
        this.ARMoperand = ARMoperand;
    }

    public ARMDAddr getARMOperand() {
        return ARMoperand;
    }

    private ARMDAddr ARMoperand;

    public ExpDefinition(Type type, Location location) {
        super(type, location);
    }

}
