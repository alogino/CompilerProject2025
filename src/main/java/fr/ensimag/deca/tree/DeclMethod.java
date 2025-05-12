package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.StackCount;
import fr.ensimag.deca.codegen.HelperInfo;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.IMAProgram;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.ERROR;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.WNL;
import fr.ensimag.ima.pseudocode.instructions.WSTR;

/**
 * Represents a method declaration in a class.
 */
public class DeclMethod extends AbstractDeclMethod {
    private final AbstractIdentifier returnIdentifier;
    private final AbstractIdentifier methodName;
    private final ListDeclParam params;
    private final AbstractMethodBody body;
    private MethodDefinition definition;

    public DeclMethod(AbstractIdentifier returnIdentifier, AbstractIdentifier methodName, ListDeclParam params,
            AbstractMethodBody body) {
        Validate.notNull(returnIdentifier);
        Validate.notNull(methodName);
        Validate.notNull(params);
        Validate.notNull(body);
        this.returnIdentifier = returnIdentifier;
        this.methodName = methodName;
        this.params = params;
        this.body = body;
    }

    @Override
    public void verifyClassBody(DecacCompiler compiler) throws ContextualError {
        throw new UnsupportedOperationException("to implement");

    }

    public AbstractIdentifier getReturnIdentifier() {
        return returnIdentifier;
    }

    public AbstractIdentifier getMethodName() {
        return methodName;
    }

    public ListDeclParam getParams() {
        return params;
    }

    public AbstractMethodBody getBody() {
        return body;
    }

    public MethodDefinition getMethodDefinition() {
        return definition;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        returnIdentifier.decompile(s);
        s.print(" ");
        methodName.decompile(s);
        s.print("(");
        params.decompile(s);
        s.print(")");
        body.decompile(s);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        if (returnIdentifier != null) {
            returnIdentifier.prettyPrint(s, prefix, false);
        }
        if (methodName != null) {
            methodName.prettyPrint(s, prefix, false);
        }
        if (params != null) {
            params.prettyPrint(s, prefix, false);
        }
        if (body != null) {
            body.prettyPrint(s, prefix, true);
        }
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        if (returnIdentifier != null) {
            returnIdentifier.iterChildren(f);
        }
        if (methodName != null) {
            methodName.iterChildren(f);
        }
        if (params != null) {
            params.iterChildren(f);
        }
        if (body != null) {
            body.iterChildren(f);
        }
    }

    @Override
    public MethodDefinition verifyDeclMethod(DecacCompiler compiler, ClassDefinition classDef)
            throws ContextualError {
        // if method is new (i.e not present in a parent class) simply add it
        // calling verifyType or verifyExp does not seem correct, they're pass 3
        // methods...
        // but we got all types in pass 2, so calling it seems fine. we try. we don't
        // handle the contextual error, seems to not be done like this
        EnvironmentExp parentEnvExp = classDef.getSuperClass().getMembers();

        EnvironmentExp methodEnvExp = new EnvironmentExp(classDef.getMembers());
        getBody().setMethodEnvExp(methodEnvExp);

        Type returnType = returnIdentifier.verifyType(compiler);
        Signature paramSign = params.verifyListDeclParam(compiler, methodEnvExp);
        paramSign.setReturnType(returnType);

        MethodDefinition methodDef = null;
        MethodDefinition parentMethod = null;
        try {
            // method is defined in parent class
            Symbol methodSym = compiler.createSymbol(methodName.getName().getName() + ".m");
            parentMethod = (MethodDefinition) parentEnvExp.get(methodSym, getLocation());

        } catch (ContextualError e) {
            // method is not defined in parent class
            classDef.incNumberOfMethods();
            methodDef = new MethodDefinition(returnType, getLocation(), paramSign,
                    classDef.getNumberOfMethods() - 1);
        }
        // If parentMethod exists (i.e we are trying to override an existing method)
        // we verify the validity of said override
        if (parentMethod != null) {
            if (!returnType.equals(parentMethod.getType())) {
                String errorMessage = String.format(
                        "%s method override is invalid: mismatch in return type, expected: '%s', got '%s'",
                        methodName.getName().getName(), parentMethod.getType().toString(), returnType.toString());
                throw new ContextualError(errorMessage, getLocation());
            }

            if (!paramSign.equals(parentMethod.getSignature())) {
                String errorMessage = String.format(
                        "%s method override is invalid: mismatch in signature",
                        methodName.getName().getName());
                throw new ContextualError(errorMessage, getLocation());
            }

            // valid method override
            methodDef = new MethodDefinition(returnType, getLocation(), paramSign,
                    parentMethod.getIndex());
        }

        // create the method's label
        methodDef.setLabel(new Label("code." +
                classDef.getType().getName().toString() + "." +
                methodName.getName().toString()));

        try {
            Symbol methodSym = compiler.createSymbol(methodName.getName().getName() + ".m");
            classDef.getMembers().declare(methodSym, methodDef);
        } catch (DoubleDefException e) {
            String errorMessage = String.format("Method %s is already defined in current environment.",
                    methodName.getName());
            throw new ContextualError(errorMessage, getLocation());
        }

        // set useful fields
        definition = methodDef;
        index = definition.getIndex();

        methodName.setDefinition(definition);
        methodName.setType(returnIdentifier.getType());

        // TODO : change this method back to void, use the definition field (only if
        // enough time)
        return methodDef;
    }

    @Override
    public void verifyMethodBody(DecacCompiler compiler, ClassDefinition currentClass)
            throws ContextualError {
        body.verifyMethodBody(compiler, currentClass, returnIdentifier.getType());
    }

    /**
     * Returns the number of local variables declared in the method body.
     * If the body is an instance of {@link MethodBody}, it retrieves the count of
     * variable declarations.
     *
     * @return Number of local variables, or 0 if none.
     */
    public int getNumberOfLocalVariables() {
        if (body instanceof MethodBody) {
            return ((MethodBody) body).getDecls().size();
        }
        return 0;
    }

    /**
     * Returns the number of parameters in the method.
     *
     * @return Number of parameters.
     */
    public int getNumberOfParams() {
        return params.size();
    }

    @Override
    public void codeGenMethodBody(DecacCompiler compiler) {
        IMAProgram block = new IMAProgram();
        compiler.setCurrentBlock(block);

        Label methodLabel = getMethodDefinition().getLabel();
        Label methodEndLabel = new Label(getMethodDefinition().getLabel().toString().replace("code", "end"));

        // set current method for information for the label needed for return statements
        HelperInfo.setCurrentMethod(this);

        // generate code for instructions, last instruction is return
        body.codeGenInst(compiler);

        // save any used R2 - R15 registers
        StackCount.genCodeSaveRegisters(compiler);

        // add stack overflow management and reset count
        StackCount.genCodeAllocateStack(compiler);

        // if method is non-void but terminates without a return
        if (!getMethodDefinition().getSignature().getReturnType().isVoid()) {
            compiler.addInstruction(
                    new WSTR(String.format("ERROR: %s method exited without a return statement.",
                            methodName.getName().getName())));
            compiler.addInstruction(new WNL());
            compiler.addInstruction(new ERROR());
        }

        // if method is void or terminates with a return
        compiler.addLabel(methodEndLabel);

        // restore any used R2 - R15 registers
        StackCount.genCodeRestoreRegisters(compiler);
        compiler.addInstruction(new RTS());

        // add method's label at the start of the block
        compiler.addLabelFirst(methodLabel);

        compiler.appendBlock(block);
    }

    @Override
    public void ARMCodeGenMethodBody(DecacCompiler compiler) {
        // TODO Auto-generated method stub

    }
}
