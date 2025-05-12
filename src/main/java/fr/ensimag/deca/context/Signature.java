package fr.ensimag.deca.context;

import java.util.ArrayList;
import java.util.List;

/**
 * Signature of a method (i.e. list of arguments)
 *
 * @author gl13
 * @date 01/01/2025
 */
public class Signature {
    List<Type> args = new ArrayList<Type>();
    Type returnType;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Signature) {
            Signature objSig = (Signature) obj;
            return this.size() == objSig.size() && this.args.equals(objSig.args)
                    && this.returnType.equals(objSig.getReturnType());
        }
        return false;
    }

    public void add(Type t) {
        args.add(t);
    }

    public Type paramNumber(int n) {
        return args.get(n);
    }

    public int size() {
        return args.size();
    }

    public Type getReturnType() {
        return returnType;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

}
