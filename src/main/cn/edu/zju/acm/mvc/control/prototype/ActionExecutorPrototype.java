
package cn.edu.zju.acm.mvc.control.prototype;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import cn.edu.zju.acm.mvc.control.ActionProxy;
import cn.edu.zju.acm.mvc.control.InvalidResultException;

public class ActionExecutorPrototype extends ActionPrototype implements ActionProxy {

    private Logger logger = Logger.getLogger("ActionExecutorPrototype.class");

    public String execute(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Map cookieMap = new HashMap<String, Cookie>();
        for (Cookie cookie : req.getCookies()) {
            cookieMap.put(cookie.getName(), cookie);
        }
        HttpSession session = req.getSession();

        try {
            String value = req.getParameter("intProp");
            if (value != null) {
                this.setIntProp(Integer.parseInt(value));
            } else {
                Integer t = (Integer) session.getAttribute("intProp");
                if (t != null) {
                    this.setIntProp(t.intValue());
                }
            }
        } catch (NumberFormatException e) {
            logger.debug("Fail to set property intProp", e);
            this.addErrorMessage("intProp should be of type int");
        }

        try {
            String value = req.getParameter("longProp");
            if (value != null) {
                this.setLongProp(Long.parseLong(value));
            }
        } catch (NumberFormatException e) {
            logger.debug("Fail to set property longProp", e);
            this.addErrorMessage("intProp should be of type long");
        }

        try {
            String value = req.getParameter("integerProp");
            if (value != null) {
                this.setIntegerProp(new Integer(value));
            }
        } catch (NumberFormatException e) {
            logger.debug("Fail to set property integerProp", e);
            this.addErrorMessage("integerProp should be of type Integer");
        }
        {
            String value = req.getParameter("stringProp");
            if (value != null) {
                this.setStringProp(value);
            } else {
            }
        }
        try {
            String[] values = req.getParameterValues("intArrayProp");
            if (values != null) {
                int[] t = new int[values.length];
                for (int i = 0; i < values.length; ++i) {
                    t[i] = Integer.parseInt(values[i]);
                }
                this.setIntArrayProp(t);
            }
        } catch (NumberFormatException e) {
            logger.debug("Fail to set property intArrayProp", e);
            this.addErrorMessage("intProp should be of type int[]");
        }
        try {
            String[] values = req.getParameterValues("longArrayProp");
            if (values != null) {
                int len = values.length;
                long[] t = new long[len];
                for (int i = 0; i < len; ++i) {
                    t[i] = Long.parseLong(values[i]);
                }
                this.setLongArrayProp(t);
            }
        } catch (NumberFormatException e) {
            logger.debug("Fail to set property longArrayProp", e);
            this.addErrorMessage("longArrayProp should be of type long[]");
        }
        try {
            String[] values = req.getParameterValues("floatArrayProp");
            if (values != null) {
                float[] t = new float[values.length];
                for (int i = 0; i < values.length; ++i) {
                    t[i] = Float.parseFloat(values[i]);
                }
                this.setFloatArrayProp(t);
            }
        } catch (NumberFormatException e) {
            logger.debug("Fail to set property floatArrayProp", e);
            this.addErrorMessage("floatArrayProp should be of type float[]");
        }
        try {
            String[] values = req.getParameterValues("doubleArrayProp");
            if (values != null) {
                int len = values.length;
                double[] t = new double[len];
                for (int i = 0; i < len; ++i) {
                    t[i] = Double.parseDouble(values[i]);
                }
                this.setDoubleArrayProp(t);
            }
        } catch (NumberFormatException e) {
            logger.debug("Fail to set property doubleArrayProp", e);
            this.addErrorMessage("doubleArrayProp should be of type double[]");
        }
        try {
            String[] values = req.getParameterValues("StringArrayProp");
            if (values != null) {
                int len = values.length;
                String[] t = new String[len];
                for (int i = 0; i < len; ++i) {
                    t[i] = values[i];
                }
                this.setStringArrayProp(t);
            }
        } catch (NumberFormatException e) {
            logger.debug("Fail to set property StringArrayProp", e);
            this.addErrorMessage("StringArrayProp should be of type String[]");
        }
        try {
            String[] values = req.getParameterValues("intListProp");
            if (values != null) {
                int len = values.length;
                List<Integer> t = new ArrayList<Integer>(len);
                for (int i = 0; i < len; ++i) {
                    t.add(Integer.parseInt(values[i]));
                }
                this.setIntListProp(t);
            }
        } catch (NumberFormatException e) {
            logger.debug("Fail to set property intListProp", e);
            this.addErrorMessage("intProp should be of type List<int>");
        }

        try {
            String value = req.getParameter("dateProp");
            if (value != null) {
                SimpleDateFormat fmt = new SimpleDateFormat();
                fmt.setLenient(false);
                this.setDateProp(fmt.parse(value));
            }
        } catch (ParseException e) {
            logger.debug("Fail to set property dateProp", e);
            this.addErrorMessage("dateProp should be of type date");
        }

        String result = null;
        try {
            String resultName = super.execute();
            if ("success".equals(resultName)) {
                result = "/success.jsp";
            } else if ("failure".equals(resultName)) {
                result = "/failure.jsp";
            } else if ("input".equals(resultName)) {
                result = "/input.jsp";
            }
            if (result == null) {
                throw new InvalidResultException(resultName);
            }
            req.setAttribute("intArrayProp", this.getIntArrayProp());
            req.setAttribute("intProp", this.getIntProp());
            req.setAttribute("longProp", this.getLongProp());
            req.setAttribute("doubleProp", this.getDoubleProp());
            
        } catch (NullPointerException e) {
            result = "";
        } catch (Exception e) {
            result = "";
        }
        return result;
    }
}
