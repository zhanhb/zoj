
package cn.edu.zju.acm.mvc.control;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestUnsupportedInputPropertyAction extends TestActionBase {

    private Object objProp;

    private File[] fileArrayProp;

    private List<File> fileListProp;

    private Set<File> fileSetProp;

    private Map<Integer, String> mapProp;

    private boolean[] booleanArrayProp;

    public Object getObjProp() {
        return this.objProp;
    }

    public void setObjProp(Object objProp) {
        this.objProp = objProp;
    }

    public File[] getFileArrayProp() {
        return this.fileArrayProp;
    }

    public void setFileArrayProp(File[] fileArrayProp) {
        this.fileArrayProp = fileArrayProp;
    }

    public List<File> getFileListProp() {
        return this.fileListProp;
    }

    public void setFileListProp(List<File> fileListProp) {
        this.fileListProp = fileListProp;
    }

    public Set<File> getFileSetProp() {
        return this.fileSetProp;
    }

    public void setFileSetProp(Set<File> fileSetProp) {
        this.fileSetProp = fileSetProp;
    }

    public Map<Integer, String> getMapProp() {
        return this.mapProp;
    }

    public void setMapProp(Map<Integer, String> mapProp) {
        this.mapProp = mapProp;
    }

    public boolean[] getBooleanArrayProp() {
        return this.booleanArrayProp;
    }

    public void setBooleanArrayProp(boolean[] booleanArrayProp) {
        this.booleanArrayProp = booleanArrayProp;
    }

}
