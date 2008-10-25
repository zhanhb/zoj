
package cn.edu.zju.acm.mvc.control.annotation;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.zju.acm.mvc.control.TestActionBase;
import cn.edu.zju.acm.mvc.control.annotation.Session;

public class TestSessionPropertyAction extends TestActionBase {

    private Object objProp;

    private File[] fileArrayProp;

    private List<File> fileListProp;

    private Set<File> fileSetProp;

    private Map<Integer, String> mapProp;

    @Session
    public Object getObjProp() {
        return this.objProp;
    }

    @Session
    public void setObjProp(Object objProp) {
        this.objProp = objProp;
    }

    @Session
    public File[] getFileArrayProp() {
        return this.fileArrayProp;
    }

    @Session
    public void setFileArrayProp(File[] fileArrayProp) {
        this.fileArrayProp = fileArrayProp;
    }

    @Session
    public List<File> getFileListProp() {
        return this.fileListProp;
    }

    @Session
    public void setFileListProp(List<File> fileListProp) {
        this.fileListProp = fileListProp;
    }

    @Session
    public Set<File> getFileSetProp() {
        return this.fileSetProp;
    }

    @Session
    public void setFileSetProp(Set<File> fileSetProp) {
        this.fileSetProp = fileSetProp;
    }

    @Session
    public Map<Integer, String> getMapProp() {
        return this.mapProp;
    }

    @Session
    public void setMapProp(Map<Integer, String> mapProp) {
        this.mapProp = mapProp;
    }

}
