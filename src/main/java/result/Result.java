package result;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Result {

    private List<ResultClass> resultClasses;
    public List<ResultClass> getResultClasses() { return resultClasses; }
    public void setResultClasses(List<ResultClass> resultClasses) { this.resultClasses = resultClasses; }


    public Result() {
        resultClasses = new ArrayList<>();
    }


    public void convertToDalvik() {}
}
