package service.statusReceiver;

import http.core.HttpResponse;
import http.util.HttpStatus;
import http.util.header.ResponseHeader;
import service.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.InvalidPathException;
import java.util.HashMap;
import java.util.Map;

public class MovPermHandler implements BaseHandler {
    /**
     * 状态码：301
     * <p>
     * 所请求的资源已被分配了一个新的永久URI，并且将来对此资源的引用应该使用返回的URI之一。
     * 具有链接编辑功能的客户端应该可能地自动将对Request-URI的引用重新链接到服务器返回的一个或多个新引用。
     * 除非另有说明，否则此响应是可缓存的。
     * <p>
     * 'Cache-Control':
     * no-cache，浏览器和缓存服务器都不应该缓存页面信息；
     * public，浏览器和缓存服务器都可以缓存页面信息；
     * no-store，请求和响应的信息都不应该被存储在对方的磁盘系统中；
     * must-revalidate，对于客户机的每次请求，代理服务器必须想服务器验证缓存是否过时
     * <p>
     * 新的永久URI应该由响应中的位置字段给出。除非请求方法是HEAD，
     * 否则响应的实体应该包含一个带有超链接到新的URI的短超文本注释。
     * <p>
     * 如果响应于GET或HEAD之外的请求接收到301状态码，用户代理不得自动重定向请求，
     * 除非用户可以确认，否则可能会更改发出请求的条件。
     *
     * @return :
     * location: 缓存的response路径
     * href: 重定向的新的uri
     */
    @Override
    public Map<String, Object> handleStatus(HttpResponse response) {
        try {
            Map<String, Object> ans = new HashMap<>();
            ans.put("status", HttpStatus.CODE_301);
            if (null != response.getHeader().getProperty(ResponseHeader.CACHE_CONTROLL) &&
                    !"no-cache".equals(response.getHeader()
                            .getProperty(ResponseHeader.CACHE_CONTROLL))) { //已指定不需要设置缓存
                ans.put("cache-location", FileUtils.cacheResponse(response, "/cache/"));
            }
            //重定向路径
            ans.put("Response Headers", response.getHeader().getHeaderText(Charset.forName("utf-8")));
            //不再修改,仍旧选用原有的uri
            ans.put("href", response.getHeader().getProperty(ResponseHeader.LOCATION));
            return ans;
        } catch (IOException e) {
            e.printStackTrace();
            throw new InvalidPathException("", "File already exist");
        }
    }
}
