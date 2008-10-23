
package cn.edu.zju.acm.mvc.control;

import java.util.List;
import java.util.Map;

public class OutputPropertyAction extends TestActionBase {

    private Object objProp;

    private Object[] objArrayProp;

    private List<String> listProp;

    private Map<String, Integer> mapProp;

    public Object getObjProp() {
        return this.objProp;
    }

    public void setObjProp(Object objProp) {
        this.objProp = objProp;
    }

    public Object[] getObjArrayProp() {
        return this.objArrayProp;
    }

    public void setObjArrayProp(Object[] objArrayProp) {
        this.objArrayProp = objArrayProp;
    }

    public List<String> getListProp() {
        return this.listProp;
    }

    public void setListProp(List<String> listProp) {
        this.listProp = listProp;
    }

    public Map<String, Integer> getMapProp() {
        return this.mapProp;
    }

    public void setMapProp(Map<String, Integer> mapProp) {
        this.mapProp = mapProp;
    }

}
