import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import result.Result;
import result.ResultClass;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CmpJavaSrc {
    private CompilationUnit cu1;
    private CompilationUnit cu2;

    private List<ClassOrInterfaceDeclaration> classes1;
    private List<ClassOrInterfaceDeclaration> classes2;

     private Set<String> imports;

    private String currentClass;
    private String packageName;
    private String mainClassFqdn;

    private Result result; public Result getResult() { return result; }


    public CmpJavaSrc(File source1, File source2) throws FileNotFoundException {
        this.cu1 = StaticJavaParser.parse(source1);
        this.cu2 = StaticJavaParser.parse(source2);

        PackageDeclaration pd1 = cu1.getPackageDeclaration().orElse(null);
        PackageDeclaration pd2 = cu2.getPackageDeclaration().orElse(null);

        if ((pd1 == null && pd2 != null) || (pd1 != null && pd2 == null)) {
            throw new UnsupportedOperationException();
        }

        if (pd1 != null && pd2 != null) {
            String pack1 = pd1.toString().replace("package ", "").replace(";", "").replaceAll("\n", "");
            String pack2 = pd2.toString().replace("package ", "").replace(";", "").replaceAll("\n", "");
            if (pack1.equals(pack2)) {
                packageName = pack1;
            }
            else throw new UnsupportedOperationException();
        }
        this.mainClassFqdn = null;

        this.classes1 = new ArrayList<>();
        this.classes2 = new ArrayList<>();
        getClasses(cu1, classes1);
        getClasses(cu2, classes2);


        imports = new HashSet<>();
        for (ImportDeclaration i : cu1.getImports()) {
            imports.add(i.getName().toString());
        }
        for (ImportDeclaration i : cu2.getImports()) {
            imports.add(i.getName().toString());
        }

        result = new Result();
    }


    private static void getClasses(Node n, List<ClassOrInterfaceDeclaration> classesList) {
        for (Node child : n.getChildNodes()) {
            if (child instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration c = (ClassOrInterfaceDeclaration) child;
                classesList.add(c);
                getClasses(c, classesList);
            }
        }
    }

    /*
    private void diffListString(List<String> strs1, List<String> strs2, List<String> res) {
        for (String s1 : strs1) {
            boolean found = false;
            for (String s2 : strs2) {
                found |= s1.equals(s2);
            }
            if (!found) {
                res.add(String.format("%s %s",  currentClass, s1));
            }
        }
    }
    */

    private void diffConstructors(List<ConstructorDeclaration> cdl1, List<ConstructorDeclaration> cdl2, List<String> res) {
        for (ConstructorDeclaration cd1: cdl1) {
            String md1str = cd1.getDeclarationAsString(true, true, false);
            boolean found = false;
            for (ConstructorDeclaration cd2: cdl2) {
                String md2str = cd2.getDeclarationAsString(true, true, false);
                found |= md1str.equals(md2str);
            }
            if (!found) {
                res.add(java2DalvikConstructor(currentClass, cd1));
            }
        }
    }

    private void diffMethods(List<MethodDeclaration> mdl1, List<MethodDeclaration> mdl2, List<String> res) {
        for (MethodDeclaration md1: mdl1) {
            String md1str = md1.getDeclarationAsString(true, true, false);
            boolean found = false;
            for (MethodDeclaration md2: mdl2) {
                String md2str = md2.getDeclarationAsString(true, true, false);
                found |= md1str.equals(md2str);
            }
            if (!found) {
                res.add(java2DalvikMethod(currentClass, md1));
            }
        }
    }

    private String java2dalvikClass(String clazz) {
        return String.format("L%s;", clazz.replaceAll("\\.", "/"));
    }

    private String java2dalvikClassWithImports(String clazz) {
        if (clazz.contains(".")) {
            return java2dalvikClass(clazz);
        }
        switch (clazz) {
            case "Boolean":
            case "Byte":
            case "Character":
            case "Double":
            case "Float":
            case "Integer":
            case "Long":
            case "Number":
            case "Short":
            case "String":
            case "Void":
                return java2dalvikClass(String.format("java.lang.%s", clazz));
        }
        String impClass = null;
        for (String i : imports) {
            String[] parts = i.split("\\.");
            String s = parts[parts.length - 1];
            if (s.equals(clazz)) {
                impClass = i;
                break;
            }
        }
        if (impClass == null) {
            for (String i : imports) {
                int pos = i.indexOf(clazz);
                if (pos > 0) {
                   impClass = String.format("%s$%s", i.substring(0, pos-1), clazz);
                }
            }
        }
        if (impClass == null) {
            impClass = String.format("%s.%s", packageName, clazz);
        }
        return java2dalvikClass(impClass);
    }


    private void java2dalvikModifiers(StringBuilder sb, NodeList<Modifier> modifiers, boolean isInit) {
        sb.append(" [access_flags=");
        if (modifiers.size() > 0) {
            for (Modifier m : modifiers) {
                sb.append(m.toString().replaceAll(" ", ""));
                sb.append(' ');
            }
            sb.setLength(sb.length() - 1);
        }
        if (isInit) {
            sb.append("constructor");
        }
        sb.append(']');
    }


    private String java2dalvikField(String clazz, FieldDeclaration fd) {
        StringBuilder sb = new StringBuilder();
        sb.append(java2dalvikClassWithImports(clazz));
        sb.append("->");
        sb.append(fd.getVariables().get(0).getName());
        sb.append(' ');
        sb.append(java2dalvikType(fd.getCommonType().asString()));
        java2dalvikModifiers(sb, fd.getModifiers(), false);
        return sb.toString();
    }


    private String java2dalvikType(String type) {
        switch (type) {
            case "boolean":
                return "Z";
            case "byte":
                return "B";
            case "short":
                return "S";
            case "char":
                return "C";
            case "int":
                return "I";
            case "long":
                return "J";
            case "float":
                return "F";
            case "double":
                return "D";
            case "void":
                return "V";
        }
        return java2dalvikClassWithImports(type);
    }

    private String java2DalvikMethod(String clazz, MethodDeclaration md) {
        StringBuilder sb = new StringBuilder();
        sb.append(java2dalvikClassWithImports(clazz));
        sb.append("->");
        sb.append(md.getName().asString());
        sb.append('(');
        for (Parameter p : md.getParameters()) {
            sb.append(java2dalvikType(p.getType().asString()));
        }
        sb.append(')');
        sb.append(java2dalvikType(md.getType().asString()));

        ClassOrInterfaceDeclaration cid = ((ClassOrInterfaceDeclaration) md.getParentNode().orElse(null));
        if (cid != null && cid.isInterface()) {
            sb.append(" [access_flags=public abstract]");
        }
        else {
            java2dalvikModifiers(sb, md.getModifiers(), false);
        }
        return sb.toString();
    }


    private String java2DalvikConstructor(String clazz, ConstructorDeclaration cd) {
        StringBuilder sb = new StringBuilder();
        sb.append(java2dalvikClassWithImports(clazz));
        sb.append("-><init>");
        for (Parameter p : cd.getParameters()) {
            sb.append(java2dalvikType(p.getType().asString()));
        }
        sb.append(")V");
        java2dalvikModifiers(sb, cd.getModifiers(), true);
        return sb.toString();
    }


    private void diffFields(List<FieldDeclaration> fields1, List<FieldDeclaration> fields2, List<String> res) {
        for (FieldDeclaration fd1 : fields1) {
            String fd1str = fd1.toString();
            boolean found = false;
            for (FieldDeclaration fd2 : fields2) {
                found |= fd1str.equals(fd2.toString());
            }
            if (!found) {
                res.add(java2dalvikField(currentClass, fd1));
            }
        }
    }


    private void cmpMethods(List<MethodDeclaration> methods1, List<MethodDeclaration> methods2, List<String> res) {
        for (MethodDeclaration md1 : methods1) {
            BlockStmt body1 = md1.getBody().orElse(null);
            if (body1 == null) continue;
            String md1str =  md1.getDeclarationAsString(true, true, false);
            for (MethodDeclaration md2 : methods2) {
                BlockStmt body2 = md2.getBody().orElse(null);
                if (body2 == null) continue;
                String md2str = md2.getDeclarationAsString(true, true, false);
                if (md1str.equals(md2str)) {
                    if (!body1.toString().equals(body2.toString())) {
                        //res.add(String.format("%s %s ~ %s", currentClass, md1str, java2DalvikMethod(currentClass, md1)));
                        res.add(java2DalvikMethod(currentClass, md1));
                    }
                }
            }
        }

    }


    private void cmpConstructors(List<ConstructorDeclaration> constructors1, List<ConstructorDeclaration> constructors2, List<String> res) {
        for (ConstructorDeclaration md1 : constructors1) {
            BlockStmt body1 = md1.getBody();
            String md1str =  md1.getDeclarationAsString(true, true, false);
            for (ConstructorDeclaration md2 : constructors2) {
                BlockStmt body2 = md2.getBody();
                String md2str = md2.getDeclarationAsString(true, true, false);
                if (md1str.equals(md2str)) {
                    if (!body1.toString().equals(body2.toString())) {
                        //res.add(String.format("%s %s", currentClass, md1str));
                        res.add(java2DalvikConstructor(currentClass, md1));
                    }
                }
            }
        }

    }


    private String getFqdnClassname(ClassOrInterfaceDeclaration coi) {
        //res.add("*" + coi.getName());
        Node pn = coi.getParentNode().orElse(null);
        if (pn instanceof CompilationUnit) {
            CompilationUnit cu = (CompilationUnit) pn;
            PackageDeclaration pd = cu.getPackageDeclaration().orElse(null);
            if (pd == null) {
                return coi.getName().toString();
            }
            return String.format("%s.%s", pd.getName().toString(), coi.getName());
        }
        if (pn instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration pnCoi = (ClassOrInterfaceDeclaration) pn;
            return String.format("%s$%s", getFqdnClassname(pnCoi), coi.getName());
        }

        return null;
    }


    private ResultClass cmpClasses(ClassOrInterfaceDeclaration c1, ClassOrInterfaceDeclaration c2) {
        String c1fqdn = getFqdnClassname(c1);
        assert c1fqdn.equals(getFqdnClassname(c2));
        currentClass = c1fqdn;
        ResultClass resultClass = new ResultClass();
        resultClass.setPackageClassname(java2dalvikClass(c1fqdn));

        diffFields(c1.getFields(), c2.getFields(), resultClass.getFieldsRemoved());
        diffFields(c2.getFields(), c1.getFields(), resultClass.getFieldsAdded());

        diffMethods(c1.getMethods(), c2.getMethods(), resultClass.getMethodsRemoved());
        diffMethods(c2.getMethods(), c1.getMethods(), resultClass.getMethodsAdded());

        if (c1.isInterface()) return resultClass;

        diffConstructors(c1.getConstructors(), c2.getConstructors(), resultClass.getMethodsRemoved());
        diffConstructors(c2.getConstructors(), c1.getConstructors(), resultClass.getMethodsAdded());

        cmpConstructors(c1.getConstructors(), c2.getConstructors(), resultClass.getMethodsEdited());

        cmpMethods(c1.getMethods(), c2.getMethods(), resultClass.getMethodsEdited());
        return resultClass;
    }


    public CmpJavaSrc compare() {
        for(ClassOrInterfaceDeclaration c1 : classes1) {
            String c1fqdn = getFqdnClassname(c1);
            for (ClassOrInterfaceDeclaration c2: classes2) {
                String c2fqdn = getFqdnClassname(c2);
                if (c1fqdn.equals(c2fqdn)) {
                    ResultClass rc = cmpClasses(c1, c2);
                    this.result.getDiff().add(rc);
                }
            }
        }
        return this;
    }

}
