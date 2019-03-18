package result;

import java.util.ArrayList;
import java.util.List;

public class Result {

    private String cve;
    private String date;
    private List<ResultClass> diff;
    public List<ResultClass> getDiff() { return diff; }
    public void setDiff(List<ResultClass> diff) { this.diff = diff; }


    public Result() {
        diff = new ArrayList<>();
    }

    public void setCve(String cve) {
        this.cve = cve;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Result prune() {
        diff.removeIf(rc ->
                rc.getMethodsAdded().size()==0 &&
                rc.getMethodsEdited().size()==0 &&
                rc.getMethodsRemoved().size()==0 &&
                rc.getFieldsAdded().size()==0 &&
                rc.getFieldsRemoved().size()==0);
        return this;
    }
}
