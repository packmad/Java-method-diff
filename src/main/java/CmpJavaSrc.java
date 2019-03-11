import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import jdk.nashorn.internal.ir.Block;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CmpJavaSrc {
    private CompilationUnit cu1;
    private CompilationUnit cu2;

    private List<ClassOrInterfaceDeclaration> classes1;
    private List<ClassOrInterfaceDeclaration> classes2;

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
            packageName = pd1.toString().replace("package ", "").replace(";", "").replaceAll("\n", "");
        }
        this.mainClassFqdn = null;

        this.classes1 = new ArrayList<>();
        this.classes2 = new ArrayList<>();

        getClasses(cu1, classes1);
        getClasses(cu2, classes2);

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

    private void diffFields(List<FieldDeclaration> fields1, List<FieldDeclaration> fields2, List<String> res) {
        for (FieldDeclaration fd1 : fields1) {
            String fd1str = fd1.toString();
            boolean found = false;
            for (FieldDeclaration fd2 : fields2) {
                found |= fd1str.equals(fd2.toString());
            }
            if (!found) {
                res.add(String.format("%s %s", currentClass, fd1.getVariables().get(0).getName()));
            }
        }
    }


    private void cmpMethods(List<MethodDeclaration> methods1, List<MethodDeclaration> methods2, List<String> res) {
        for (MethodDeclaration md1 : methods1) {
            BlockStmt body1 = md1.getBody().orElse(null);
            if (body1 == null) continue;
            String md1str =  md1.getDeclarationAsString(true, true, true);
            for (MethodDeclaration md2 : methods2) {
                BlockStmt body2 = md2.getBody().orElse(null);
                if (body2 == null) continue;
                String md2str = md2.getDeclarationAsString(true, true, true);
                if (md1str.equals(md2str)) {
                    if (!body1.toString().equals(body2.toString())) {
                        res.add(String.format("!!! %s %s", currentClass, md1str));
                    }
                }
            }
        }

    }

     private void cmpConstructors(List<ConstructorDeclaration> constructors1, List<ConstructorDeclaration> constructors2, List<String> res) {
        for (ConstructorDeclaration md1 : constructors1) {
            BlockStmt body1 = md1.getBody();
            String md1str =  md1.getDeclarationAsString(true, true, true);
            for (ConstructorDeclaration md2 : constructors2) {
                BlockStmt body2 = md2.getBody();
                String md2str = md2.getDeclarationAsString(true, true, true);
                if (md1str.equals(md2str)) {
                    if (!body1.toString().equals(body2.toString())) {
                        res.add(String.format("!!! %s %s", currentClass, md1str));
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

    private void cmpClasses(ClassOrInterfaceDeclaration c1, ClassOrInterfaceDeclaration c2) {
        String c1fqdn = getFqdnClassname(c1);
        assert c1fqdn.equals(getFqdnClassname(c2));
        currentClass = c1fqdn;

        diffFields(c1.getFields(), c2.getFields(), result.getFieldsRemoved());
        diffFields(c2.getFields(), c1.getFields(), result.getFieldsAdded());

        List<String> methods1 = c1.getMethods().stream()
                .map(m -> m.getDeclarationAsString(true, true, true))
                .collect(Collectors.toList());
        List<String> methods2 = c2.getMethods().stream()
                .map(m -> m.getDeclarationAsString(true, true, true))
                .collect(Collectors.toList());
        diffListString(methods1, methods2, result.getMethodsRemoved());
        diffListString(methods2, methods1, result.getMethodsAdded());


        if (c1.isInterface()) return;

        List<String> constrs1 = c1.getConstructors().stream()
                .map(m -> m.getDeclarationAsString(true, true, true))
                .collect(Collectors.toList());
        List<String> constrs2 = c2.getConstructors().stream()
                .map(m -> m.getDeclarationAsString(true, true, true))
                .collect(Collectors.toList());
        diffListString(constrs1, constrs2, result.getMethodsRemoved());
        diffListString(constrs2, constrs1, result.getMethodsRemoved());

        cmpConstructors(c1.getConstructors(), c2.getConstructors(), result.getMethodsEdited());

        cmpMethods(c1.getMethods(), c2.getMethods(), result.getMethodsEdited());

    }

    public void compare() {
        for(ClassOrInterfaceDeclaration c1 : classes1) {
            String c1fqdn = getFqdnClassname(c1);
            for (ClassOrInterfaceDeclaration c2: classes2) {
                String c2fqdn = getFqdnClassname(c2);
                if (c1fqdn.equals(c2fqdn)) {
                    cmpClasses(c1, c2);
                }
            }
        }
    }

}
