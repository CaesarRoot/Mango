package http.core;

import http.util.HttpMethod;
import http.util.header.RequestHeader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.*;

public class HttpRequestTest {
    private HttpRequest httpRequest;

    @Before
    @Test
    public void setUp() throws Exception {
        String request = "GET /search?hl=zh-CN&source=hp&q=domety&aq=f&oq= HTTP/1.1  \n" +
                "Accept: image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/vnd.ms-excel, application/vnd.ms-powerpoint, " +
                "application/msword, application/x-silverlight, application/x-shockwave-flash, */*  \n" +
                "Referer: <a href=\"http://www.google.cn/\">http://www.google.cn/</a>  \n" +
                "Accept-Language: zh-cn  \n" +
                "Accept-Encoding: gzip, deflate  \n" +
                "Content-Type: text/html; charset=utf-8  \n" +
                "User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; TheWorld)  \n" +
                "Host: <a href=\"http://www.google.cn\">www.google.cn</a>  \n" +
                "Connection: Keep-Alive  \n" +
                "Cookie: PREF=ID=80a06da87be9ae3c:U=f7167333e2c3b714:NW=1:TM=1261551909:LM=1261551917:S=ybYcq2wpfefs4V9g; " +
                "NID=31=ojj8d-IygaEtSxLgaJmqSjVhCspkviJrB6omjamNrSm8lZhKy_yMfO2M4QMRKcH1g0iQv9u-2hfBW7bUFwVh7pGaRUb0RnHcJU37y-" +
                "FxlRugatx63JLv7CWMD6UB_O_r  \n\n";
        InputStream is = new ByteArrayInputStream(request.getBytes());
        httpRequest = new HttpRequest(is);
        Assert.assertEquals(HttpMethod.GET, httpRequest.getMethod());
        Assert.assertEquals("/search?hl=zh-CN&source=hp&q=domety&aq=f&oq=", httpRequest.getUrl());
        Assert.assertEquals("HTTP/1.1", httpRequest.getVersion());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void setHeader() {
    }

    @Test
    public void getRequestBodyText() {
    }

    @Test
    public void getRequestBodyStream() {
    }

    @Test
    public void setRequestBody() {
    }

    @Test
    public void setRequestBody1() {
    }

    @Test
    public void writeTo() {
    }

    @Test
    public void toString1() {
    }

    @Test
    public void setMethod() {
    }

    @Test
    public void setUrl() {
    }

    @Test
    public void setVersion() {
    }

    @Test
    public void getMethod() {
    }

    @Test
    public void getUrl() {
    }

    @Test
    public void getVersion() {
    }

    @Test
    public void getHeader() {
    }
}