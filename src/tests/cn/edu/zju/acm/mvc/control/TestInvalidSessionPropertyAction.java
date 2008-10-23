
package cn.edu.zju.acm.mvc.control;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.zju.acm.mvc.control.annotation.Cookie;
import cn.edu.zju.acm.mvc.control.annotation.Session;
import cn.edu.zju.acm.mvc.control.annotation.validator.IntValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.Required;

public class TestInvalidSessionPropertyAction extends TestActionBase {

    private Object objProp;

    private File[] fileArrayProp;

    private List<File> fileListProp;

    private Set<File> fileSetProp;

    private Map<Integer, String> mapProp;

    @Session
    @Required
    @IntValidator
    @Cookie
    public Object getObjProp() {
        return this.objProp;
    }

    @Session
    @Required
    @IntValidator
    @Cookie
    public void setObjProp(Object objProp) {
        this.objProp = objProp;
    }

    @Session
    @Required
    @IntValidator
    @Cookie
    public File[] getFileArrayProp() {
        return this.fileArrayProp;
    }

    @Session
    @Required
    @IntValidator
    @Cookie
    public void setFileArrayProp(File[] fileArrayProp) {
        this.fileArrayProp = fileArrayProp;
    }

    @Session
    @Required
    @IntValidator
    @Cookie
    public List<File> getFileListProp() {
        return this.fileListProp;
    }

    @Session
    @Required
    @IntValidator
    @Cookie
    public void setFileListProp(List<File> fileListProp) {
        this.fileListProp = fileListProp;
    }

    @Session
    @Required
    @IntValidator
    @Cookie
    public Set<File> getFileSetProp() {
        return this.fileSetProp;
    }

    @Session
    @Required
    @IntValidator
    @Cookie
    public void setFileSetProp(Set<File> fileSetProp) {
        this.fileSetProp = fileSetProp;
    }

    @Session
    @Required
    @IntValidator
    @Cookie
    public Map<Integer, String> getMapProp() {
        return this.mapProp;
    }

    @Session
    @Required
    @IntValidator
    @Cookie
    public void setMapProp(Map<Integer, String> mapProp) {
        this.mapProp = mapProp;
    }

}
