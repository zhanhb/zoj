
package cn.edu.zju.acm.mvc.control;

public class InvalidResultException extends Exception {

    public InvalidResultException(String result) {
        super("Invalid result " + result);
    }
}
