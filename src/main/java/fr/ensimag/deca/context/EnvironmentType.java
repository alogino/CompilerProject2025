package fr.ensimag.deca.context;

import java.util.HashMap;
import java.util.Map;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

/**
 * Environment containing types. Initially contains predefined identifiers, more
 * classes can be added with declareClass().
 *
 * @author gl13
 * @date 01/01/2025
 */
public class EnvironmentType {

    public static class DoubleDefException extends Exception {
        private static final long serialVersionUID = -2733379901827316441L;

        public DoubleDefException(final String message) {
            super(message);
        }
    }

    public EnvironmentType(DecacCompiler compiler) {
        envTypes = new HashMap<Symbol, TypeDefinition>();

        Symbol intSymb = compiler.createSymbol("int");
        INT = new IntType(intSymb);
        envTypes.put(intSymb, new TypeDefinition(INT, Location.BUILTIN));

        Symbol floatSymb = compiler.createSymbol("float");
        FLOAT = new FloatType(floatSymb);
        envTypes.put(floatSymb, new TypeDefinition(FLOAT, Location.BUILTIN));

        Symbol voidSymb = compiler.createSymbol("void");
        VOID = new VoidType(voidSymb);
        envTypes.put(voidSymb, new TypeDefinition(VOID, Location.BUILTIN));

        Symbol booleanSymb = compiler.createSymbol("boolean");
        BOOLEAN = new BooleanType(booleanSymb);
        envTypes.put(booleanSymb, new TypeDefinition(BOOLEAN, Location.BUILTIN));

        Symbol stringSymb = compiler.createSymbol("string");
        STRING = new StringType(stringSymb);
        // not added to envTypes, it's not visible for the user.

        // class predefined types
        Symbol nullSymb = compiler.createSymbol("null");
        NULL = new NullType(nullSymb);

        Symbol objectSymb = compiler.createSymbol("Object");
        OBJECT = new ClassType(objectSymb, Location.BUILTIN, null);
        envTypes.put(objectSymb, OBJECT.getDefinition());

        // ----- add the equals method to object
        Signature objectEqualsSig = new Signature();
        objectEqualsSig.add(OBJECT);
        objectEqualsSig.setReturnType(BOOLEAN);

        // Object has a single method (equals)
        OBJECT.getDefinition().setNumberOfMethods(1);

        // Object has no fields
        OBJECT.getDefinition().setNumberOfFields(0);

        // Object VTable position
        OBJECT.getDefinition().setVBTablePos(new RegisterOffset(1, Register.GB));

        MethodDefinition objectEquals = new MethodDefinition(BOOLEAN, null, objectEqualsSig, 0);
        objectEquals.setLabel(new Label("code.Object.equals"));
        OBJECT.getDefinition().getMembers().addOrUpdate(compiler.createSymbol("equals.m"), objectEquals);
    }

    private final Map<Symbol, TypeDefinition> envTypes;

    public TypeDefinition defOfType(Symbol s) {
        return envTypes.get(s);
    }

    public final VoidType VOID;
    public final IntType INT;
    public final FloatType FLOAT;
    public final StringType STRING;
    public final BooleanType BOOLEAN;
    public final NullType NULL;

    public final ClassType OBJECT;

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
    public void declare(Symbol name, ClassDefinition def) throws DoubleDefException {
        if (envTypes.containsKey(name)) {
            throw new DoubleDefException("Symbol already used in the environment of expressions: " + name);
        }
        envTypes.put(name, def);
    }

    /**
     * Checks if type1 is a sub type of type 2
     *
     * @param type1
     *              Type of the class expected to be the child type
     * @param type2
     *              Type of the class expected to be the parent type
     * @return true if Type1 is a subtype of Type2
     */
    public boolean isSubType(Type type1, Type type2) {
        if (type1 == NULL) {
            return true;
        }
        if (type1.equals(type2)) {
            return true;
        }
        if (type1.isInt() && type2.isFloat()) {
            return true;
        } else if (type1.isClass() && type2.isClass()) {
            if (type1.equals(OBJECT)) {
                return false;
            } else {
                type1 = ((ClassType) type1).getDefinition().getSuperClass().getType();
                return isSubType(type1, type2);
            }
        } else {
            return false;
        }
    }

    /**
     * Checks if class1 is a subclass of class 2
     * @param classDef1 classDefintion expected to be the child class
     * @param classDef2 classDefintion expected to be the parent class
     * @return returns true if class1 is a subtype of class2
     */
    public boolean isSubClass(ClassDefinition classDef1, ClassDefinition classDef2) {
        if (classDef1 == null) {
            return true;
        }
        if (classDef1.getType().equals(classDef2.getType())) {
            return true;
        }
        if (classDef1.getType().isInt() && classDef2.getType().isFloat()) {
            return true;
        } else if (classDef1.getType().isClass() && classDef2.getType().isClass()) {
            if (classDef1.getType().equals(OBJECT)) {
                return false;
            } else {
                classDef1 = classDef1.getSuperClass();
                return isSubClass(classDef1, classDef2);
            }
        } else {
            return false;
        }
    }

}
