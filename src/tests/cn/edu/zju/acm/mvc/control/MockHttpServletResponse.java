
package cn.edu.zju.acm.mvc.control;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class MockHttpServletResponse implements HttpServletResponse {

    private List<Cookie> cookies = new ArrayList<Cookie>();

    private int error = 0;

    private String redirect = null;

    private MockServletOutputStream out = new MockServletOutputStream();

    private PrintWriter writer = new PrintWriter(out);

    public List<Cookie> getCookies() {
        return this.cookies;
    }

    public int getError() {
        return this.error;
    }

    public String getRedirect() {
        return this.redirect;
    }

    public byte[] getOutput() {
        return this.out.toByteArray();
    }

    public String getOutputAsString() {
        return new String(this.getOutput());
    }

    public void addCookie(Cookie cookie) {
        this.cookies.add(cookie);
    }

    public void addDateHeader(String arg0, long arg1) {
        // TODO Auto-generated method stub

    }

    public void addHeader(String arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    public void addIntHeader(String arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    public boolean containsHeader(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public String encodeRedirectURL(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public String encodeRedirectUrl(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public String encodeURL(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public String encodeUrl(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public void sendError(int sc) throws IOException {
        this.error = sc;
    }

    public void sendError(int sc, String msg) throws IOException {
        // TODO Auto-generated method stub

    }

    public void sendRedirect(String redirect) throws IOException {
        this.redirect = redirect;
    }

    public void setDateHeader(String arg0, long arg1) {
        // TODO Auto-generated method stub

    }

    public void setHeader(String arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    public void setIntHeader(String arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    public void setStatus(int arg0) {
        // TODO Auto-generated method stub

    }

    public void setStatus(int arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    public void flushBuffer() throws IOException {
        // TODO Auto-generated method stub

    }

    public int getBufferSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getCharacterEncoding() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getContentType() {
        // TODO Auto-generated method stub
        return null;
    }

    public Locale getLocale() {
        // TODO Auto-generated method stub
        return null;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return this.out;
    }

    public PrintWriter getWriter() throws IOException {
        return this.writer;
    }

    public boolean isCommitted() {
        // TODO Auto-generated method stub
        return false;
    }

    public void reset() {
        // TODO Auto-generated method stub

    }

    public void resetBuffer() {
        // TODO Auto-generated method stub

    }

    public void setBufferSize(int arg0) {
        // TODO Auto-generated method stub

    }

    public void setCharacterEncoding(String arg0) {
        // TODO Auto-generated method stub

    }

    public void setContentLength(int arg0) {
        // TODO Auto-generated method stub

    }

    public void setContentType(String arg0) {
        // TODO Auto-generated method stub

    }

    public void setLocale(Locale arg0) {
        // TODO Auto-generated method stub

    }

    private static class MockServletOutputStream extends ServletOutputStream {

        private ByteArrayOutputStream out = new ByteArrayOutputStream();

        @Override
        public void write(int b) throws IOException {
            this.out.write(b);
        }

        public byte[] toByteArray() {
            return this.out.toByteArray();
        }
    }
}
