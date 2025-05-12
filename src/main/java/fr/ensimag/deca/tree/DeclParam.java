package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.deca.codegen.StackCount;

import java.io.PrintStream;

/**
 * Represents a single parameter declaration in a method.
 */
public class DeclParam extends AbstractDeclParam {
    private final AbstractIdentifier identifier;
    private final AbstractIdentifier paramName;

    public DeclParam(AbstractIdentifier identifier, AbstractIdentifier paramName) {
        if (identifier == null || paramName == null) {
            throw new IllegalArgumentException("Type and parameter name cannot be null");
        }
        this.identifier = identifier;
        this.paramName = paramName;
    }

    @Override
    public String getName() {
        return paramName.toString();
    }

    public AbstractIdentifier getType() {
        return identifier;
    }

    public AbstractIdentifier getParamName() {
        return paramName;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        identifier.decompile(s);
        s.print(" ");
        paramName.decompile(s);
    }

    private ParamDefinition paramDef;
    private Type paramType;

    public ParamDefinition getParamDefinition() {
        return paramDef;
    }

    public Type getParamType() {
        return paramType;
    }

    @Override
    protected Type verifyDeclParam(DecacCompiler compiler, EnvironmentExp methodEnvExp)
            throws ContextualError {
        paramType = identifier.verifyType(compiler);

        if (paramType.isVoid()) {
            throw new ContextualError("Invalid type for parameter declaration: " + paramType, this.getLocation());
        }

        paramDef = new ParamDefinition(paramType, getLocation());

        paramName.setDefinition(paramDef);
        paramName.setType(paramType);
        try {
            methodEnvExp.declare(paramName.getName(), paramName.getParamDefinition());
        } catch (DoubleDefException e) {
            String errorMessage = String.format("Identifier %s appears twice in method definition parameters",
                    paramName.getName());
            throw new ContextualError(errorMessage, getLocation());
        }

        return paramType;
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        if (identifier != null) {
            identifier.prettyPrint(s, prefix, false);
        }
        if (paramName != null) {
            paramName.prettyPrint(s, prefix, true);
        }
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        if (identifier != null) {
            identifier.iter(f);
        }
        if (paramName != null) {
            paramName.iter(f);
        }
    }

    @Override
    protected void codeGenDeclParam(DecacCompiler compiler) {
        // TODO : account for no register
        // generate code
        GPRegister loadTarget = Register.getUnusedR();
        compiler.addInstruction(new LOAD(paramDef.getOperand(), loadTarget));
        Register.setLastExprPos(new RegisterOffset(0, loadTarget));
    }

    @Override
    protected void ARMCodeGenDeclParam(DecacCompiler compiler) {
        // TODO Auto-generated method stub

    }
}
