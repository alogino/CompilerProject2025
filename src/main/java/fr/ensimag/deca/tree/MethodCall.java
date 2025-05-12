package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.StackCount;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.SUBSP;

/**
 * Represents a method call like `object.method(args)`.
 */
public class MethodCall extends AbstractExpr {
    private final AbstractExpr object;
    private final AbstractIdentifier method;
    private final ListExpr args;

    public MethodCall(AbstractExpr object, AbstractIdentifier method, ListExpr args) {
        Validate.notNull(object, "Object in MethodCall cannot be null");
        Validate.notNull(method, "Method name in MethodCall cannot be null");
        Validate.notNull(args, "Arguments in MethodCall cannot be null");
        this.object = object;
        this.method = method;
        this.args = args;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type objectType = object.verifyExpr(compiler, localEnv, currentClass);
        if (!objectType.isClass()) {
            throw new ContextualError("Method call must be performed on an object of a class type.", getLocation());
        }
        ClassDefinition objectClassDef = (ClassDefinition) compiler.environmentType
                .defOfType(objectType.getName());

        if (objectClassDef == null) {
            throw new ContextualError("Class '" + objectType.getName() + "' not found.", getLocation());
        }
        Symbol methodSym = compiler.createSymbol(method.getName().getName() + ".m");
        MethodDefinition methodDef = (MethodDefinition) objectClassDef.getMembers().getAsMethod(methodSym,
                getLocation());

        if (methodDef == null) {
            throw new ContextualError(
                    "Method '" + method.getName() + "' does not exist in class '" + objectType.getName() + "'.",
                    getLocation());
        }

        Signature methodSignature = methodDef.getSignature();

        if (args.size() != methodSignature.size()) {
            throw new ContextualError("Incorrect number of arguments for method '" + method.getName() +
                    "'. Expected: " + methodSignature.size() + ", Found: " + args.size(), getLocation());
        }

        for (int i = 0; i < args.size(); i++) {
            Type argType = args.getList().get(i).verifyExpr(compiler, localEnv, currentClass);
            Type expectedType = methodSignature.paramNumber(i);

            // Validation des sous-types pour chaque argument
            if (!compiler.getEnvironmentType().isSubType(argType, expectedType)) {
                throw new ContextualError(
                        "Argument " + (i + 1) + " of method '" + method.getName() +
                                "' does not match the expected type. Expected: " + expectedType +
                                ", Found: " + argType,
                        getLocation());
            }
        }
        ClassDefinition classDef = (ClassDefinition) compiler.environmentType.defOfType(objectType.getName());
        // classDef.getMembers().showEnvKey();
        method.verifyMethod(compiler, classDef.getMembers(), classDef);

        setDefinition(methodDef);
        setType(methodSignature.getReturnType());

        return methodDef.getType();
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // TODO : handle case where no register is unused
        MethodDefinition methodDef = (MethodDefinition) method.getDefinition();
        int methodOffset = methodDef.getIndex() + 1;

        // reserve space in stack for all parameters, including implicit parameter (the
        // instance itself)
        compiler.addInstruction(new ADDSP(args.size() + 1));

        // get address of instance
        object.codeGenInst(compiler);
        DVal instanceAddrPos = Register.getLastExprPos();

        // update max param count if needed
        StackCount.setMaxParamCount(args.size());

        // push implicit param
        compiler.addInstruction(new STORE((GPRegister) instanceAddrPos, new RegisterOffset(0, Register.SP)));
        Register.setUnused((GPRegister) instanceAddrPos);

        for (int i = args.size() - 1; i >= 0; i--) {
            // evaluate expression passed as an argument
            args.getList().get(i).codeGenInst(compiler);

            // TODO : handle case where no register is unused
            if (!Register.getLastExprPos().equals(Register.SP)) {
                GPRegister loadTarget = (GPRegister) Register.getLastExprPos();
                compiler.addInstruction(
                        new STORE(loadTarget, new RegisterOffset(-1 - i, Register.SP)));
                Register.setUnused(loadTarget);
            } else {
                // TODO : handle case where no unused register is available
            }
        }

        // TODO : handle case where no register is unused (we NEED one)

        // load implicit param address
        GPRegister loadTarget = Register.getUnusedR();
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.SP), loadTarget));

        // manage null dereferencement
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.setPossibleNullDereferenceError();
            compiler.addInstruction(new CMP(new NullOperand(), loadTarget));
            compiler.addInstruction(new BEQ(new Label("null_dereference_error")));
        }

        // load VTable entry address
        compiler.addInstruction(new LOAD(new RegisterOffset(0, loadTarget), loadTarget));

        // method call
        compiler.addInstruction(new BSR(new RegisterOffset(methodOffset, loadTarget)));
        compiler.addInstruction(new SUBSP(args.size() + 1));

        // if the method returns non-void, set lastExprPos to R0. This should also be
        // done in Return node, but we do it here to take into account a call to the
        // Object's equal method
        if (!methodDef.getSignature().getReturnType().isVoid()) {
            Register.setLastExprPos(new RegisterOffset(0, Register.R0));
        }
        Register.setUnused(loadTarget);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        object.decompile(s);
        s.print(".");
        method.decompile(s);
        s.print("(");
        args.decompile(s);
        s.print(")");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        object.iter(f);
        method.iter(f);
        args.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        object.prettyPrint(s, prefix, false);
        method.prettyPrint(s, prefix, false);
        args.prettyPrint(s, prefix, true);
    }
}
