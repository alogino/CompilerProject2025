package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.StackCount;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentType.DoubleDefException;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.IMAProgram;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.Line;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.SUBSP;

/**
 * Declaration of a class (<code>class name extends superClass {members}<code>).
 * 
 * @author gl13
 * @date 01/01/2025
 */
public class DeclClass extends AbstractDeclClass {
    private final AbstractIdentifier name;
    private final AbstractIdentifier superClass;
    private final ListDeclField fields;
    private final ListDeclMethod methods;

    public DeclClass(AbstractIdentifier name, AbstractIdentifier superClass,
            ListDeclField fields, ListDeclMethod methods) {
        this.name = name;
        this.superClass = superClass;
        this.fields = fields;
        this.methods = methods;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("class ");
        name.decompile(s);
        if (superClass != null) {
            s.print(" extends ");
            superClass.decompile(s);
        }
        s.println(" {");
        s.indent();
        fields.decompile(s);
        methods.decompile(s);
        s.unindent();
        s.println("}");
    }

    /**
     * verifies the contextual syntax of the class - step 1 (name + hierarchy)
     */
    @Override
    protected void verifyClass(DecacCompiler compiler) throws ContextualError {
        // Create type of this class, handles invalid parent class error
        ClassDefinition superClassDef = (ClassDefinition) compiler.environmentType.defOfType(superClass.getName());

        ClassType classType = new ClassType(name.getName(), getLocation(),
                superClassDef);

        // Make class definition
        ClassDefinition classDef = new ClassDefinition(classType, getLocation(), superClassDef);

        // Define with class definition, handle case where class of same name has
        // already been defined
        try {
            compiler.environmentType.declare(name.getName(), classDef);
        } catch (DoubleDefException e) {
            String errorMessage = String.format("Class %s is already defined", name.getName().getName());
            throw new ContextualError(errorMessage, getLocation());
        }

        name.setDefinition(classDef);
        // set superclass definition
        superClass.setDefinition(superClassDef);
    }

    @Override
    protected void verifyClassMembers(DecacCompiler compiler)
            throws ContextualError {
        fields.verifyListField(compiler, name.getClassDefinition());
        methods.verifyListMethod(compiler, name.getClassDefinition());
    }

    @Override
    protected void verifyClassBody(DecacCompiler compiler) throws ContextualError {
        fields.verifyListFieldBody(compiler, name.getClassDefinition());
        methods.verifyListMethodBody(compiler, name.getClassDefinition());
    }

    /** Represents the current offset for the GB register in the VTable */
    private static int currentGBOffset = 2;

    public static int getVTableSize() {
        return currentGBOffset;
    }

    /**
     * Get the next GB offset to be used in creation of VTable
     * 
     * @return the next value of the GB offset for the VTable creation
     */
    private static int getNextGBOffset() {
        return ++currentGBOffset;
    }

    @Override
    public void codeGenTable(DecacCompiler compiler) {
        ClassDefinition classDef = name.getClassDefinition();

        compiler.addComment(String.format("VTABLE of class %s", name.getName().getName()));

        // Step 1: Store the address of the superclass's method table
        RegisterOffset superClassOffset = classDef.getSuperClass().getVBTableOffset();
        compiler.addInstruction(new LEA(superClassOffset, Register.R0));

        // Step 2: Store the address in the current class's method table and set the
        // current class's VBTableOffset
        RegisterOffset classOffset = new RegisterOffset(getNextGBOffset(), Register.GB);
        compiler.addInstruction(new STORE(Register.R0, classOffset));
        classDef.setVBTablePos(classOffset);

        // Step 3: Process the methods of the current class
        for (int methodIndex = 0; methodIndex < classDef.getNumberOfMethods(); methodIndex++) {
            MethodDefinition methodOfIndex = classDef.getMembers().getMethodOfIndex(methodIndex);

            // Get the method's label
            Label methodLabel = methodOfIndex.getLabel();

            // Load the method's address into R0
            compiler.addInstruction(new LOAD(new LabelOperand(methodLabel), Register.R0));

            // Store the method's address in the table at its respective index
            RegisterOffset methodOffset = new RegisterOffset(getNextGBOffset(), Register.GB);
            compiler.addInstruction(new STORE(Register.R0, methodOffset));
        }
    }

    /**
     * Generates the code for a class' initialization
     */
    public void codeGenClassInit(DecacCompiler compiler) {
        IMAProgram block = new IMAProgram();
        compiler.setCurrentBlock(block);

        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1));

        // dummy initialization of fields specifically defined in this class
        for (AbstractDeclField declField : fields.getList()) {
            declField.codeGenDeclFieldDummy(compiler);
        }

        // initialization of inherited fields
        if (!superClass.getName().getName().equals("Object") && superClass != null) {
            Label superClassInitLabel = new Label(
                    String.format("init.%s", superClass.getName().getName()));
            compiler.addInstruction(new PUSH(Register.R1));
            compiler.addInstruction(new BSR(new LabelOperand(superClassInitLabel)));
            compiler.addInstruction(new SUBSP(1));
        }

        // true initialization of fields specifically defined in this class
        for (AbstractDeclField declField : fields.getList()) {
            declField.codeGenDeclField(compiler);
        }

        // generate code for saving and restoring any used R2 - R15 registers
        StackCount.genCodeSaveRegisters(compiler);
        StackCount.genCodeRestoreRegisters(compiler);

        // add stack overflow management and reset count
        StackCount.genCodeAllocateStack(compiler);
        compiler.addFirst(new Line(String.format("init.%s program stack allocation", name.getName().getName())));

        // add init.<ClassName> label at the start of the block
        compiler.addLabelFirst(new Label(String.format("init.%s", name.getName().getName())));

        // return from initialization
        compiler.addInstruction(new RTS());

        // append block to program
        compiler.appendBlock(block);
    }

    @Override
    public void codeGenClass(DecacCompiler compiler) {
        codeGenMethods(compiler);
    }

    /**
     * Generates the code for all methods in a class.
     * 
     * @param compiler The Decac compiler.
     */
    private void codeGenMethods(DecacCompiler compiler) {
        compiler.addComment("--- Generating methods for class " + name.getName() + " ---");

        // generate init.<ClassName> here
        codeGenClassInit(compiler);

        // generate methods
        for (AbstractDeclMethod abstractMethod : methods.getList()) {
            abstractMethod.codeGenMethodBody(compiler);
        }
    }

    @Override
    public void ARMCodeGenTable(DecacCompiler compiler) {
        // TODO Auto-generated method stub

    }

    @Override
    public void ARMCodeGenClassInit(DecacCompiler compiler) {
        // TODO Auto-generated method stub

    }

    @Override
    public void ARMCodeGenClass(DecacCompiler compiler) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        name.prettyPrint(s, prefix, false);
        if (superClass != null) {
            superClass.prettyPrint(s, prefix, false);
        }
        fields.prettyPrint(s, prefix, false);
        methods.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        name.iter(f);
        if (superClass != null) {
            superClass.iter(f);
        }
        fields.iter(f);
        methods.iter(f);
    }

}
