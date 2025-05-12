package fr.ensimag.deca.codegen;

import java.util.HashMap;

import fr.ensimag.deca.DecacCompiler;

public class ARMDataSection {
    private static HashMap<String, String> preLoadData = new HashMap<String, String>();
    private static HashMap<String, String> preLoadStringData = new HashMap<String, String>();

    private static void setDataSectionElem(String name, String content) {
        preLoadData.put(name, content);
    }

    private static void setStringDataSectionElem(String name, String content) {
        preLoadStringData.put(name, content);
    }

    private static int floatEntryCount = 0;

    public static String createFloatDataEntry(Float value) {
        String formattedEntryName = String.format("float_val.%d", floatEntryCount);
        floatEntryCount++;

        String formattedContent = String.format(".float %s", value.toString());

        setDataSectionElem(formattedEntryName, formattedContent);

        return formattedEntryName;
    }

    private static int stringEntryCount = 0;

    public static String createStringDataEntry(String string) {
        String formattedEntryName = String.format("string_val.%d", stringEntryCount);
        stringEntryCount++;

        String formattedContent = String.format(".string \"%s\"", string);

        setStringDataSectionElem(formattedEntryName, formattedContent);

        return formattedEntryName;
    }

    public static void codeGenDataSection(DecacCompiler compiler) {
        for (String name : preLoadData.keySet()) {
            compiler.addARMRawFirst(String.format("%s: %s", name, preLoadData.get(name)));
        }

        // Alignment for ints and floats, as strings mess up the alignment and
        // ints/floats expect 4 byte alignment
        compiler.addARMRawFirst(".balign 4");
        for (String name : preLoadStringData.keySet()) {
            compiler.addARMRawFirst(String.format("%s: %s", name, preLoadStringData.get(name)));
        }

        if (intPrint) {
            compiler.addARMRawFirst("int_format: .asciz \"%i\\n\"");
        }
        if (floatPrint) {
            compiler.addARMRawFirst("float_format: .asciz \"%f\\n\"");
        }
        if (stringPrint) {
            compiler.addARMRawFirst("string_format: .asciz \"%s\\n\"");
        }

        compiler.addARMRawFirst(".section .data");
        compiler.addARMRawFirst("\n");
    }

    private static Boolean intPrint = false;

    public static void setIntPrint() {
        intPrint = true;
    }

    private static Boolean floatPrint = false;

    public static void setFloatPrint() {
        floatPrint = true;
    }

    private static Boolean stringPrint = false;

    public static void setStringPrint() {
        stringPrint = true;
    }
}
