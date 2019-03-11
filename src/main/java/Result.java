import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;

public class Result {

    private List<String> methodsRemoved;
    private List<String> methodsAdded;
    private List<String> methodsEdited;

    private List<String> fieldsRemoved;
    private List<String> fieldsAdded;

    public Result() {
        methodsRemoved = new ArrayList<>();
        methodsAdded = new ArrayList<>();
        methodsEdited = new ArrayList<>();

        fieldsRemoved = new ArrayList<>();
        fieldsAdded = new ArrayList<>();
    }

    public List<String> getMethodsRemoved() {
        return methodsRemoved;
    }

    public void setMethodsRemoved(List<String> methodsRemoved) {
        this.methodsRemoved = methodsRemoved;
    }

    public List<String> getMethodsAdded() {
        return methodsAdded;
    }

    public void setMethodsAdded(List<String> methodsAdded) {
        this.methodsAdded = methodsAdded;
    }

    public List<String> getMethodsEdited() {
        return methodsEdited;
    }

    public void setMethodsEdited(List<String> methodsEdited) {
        this.methodsEdited = methodsEdited;
    }

    public List<String> getFieldsRemoved() {
        return fieldsRemoved;
    }

    public void setFieldsRemoved(List<String> fieldsRemoved) {
        this.fieldsRemoved = fieldsRemoved;
    }

    public List<String> getFieldsAdded() {
        return fieldsAdded;
    }

    public void setFieldsAdded(List<String> fieldsAdded) {
        this.fieldsAdded = fieldsAdded;
    }
}
