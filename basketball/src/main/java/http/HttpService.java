package http;

import http.core.HttpRequest;
import http.core.HttpResponse;
import http.util.HttpStatus;
import http.util.header.ResponseHeader;
import service.StatusHandler;
import util.SocketHolder;
import view.MessageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 提供http相关处理方法的服务类
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/6/6
 */
public class HttpService {

    private static HttpService instance = null;
    //originalUri: targetUri
    private Map<String, String> redirectMap = new HashMap<>();

    private HttpService() {

    }

    public static HttpService getInstance() {
        if (instance == null)
            instance = new HttpService();
        return instance;
    }

    public HttpResponse sendRequest(HttpRequest request, String ip, int port) throws IOException {
        Socket s;
        try {
            s = SocketHolder.of(ip, port);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("socket链接建立失败");
        }

        HttpResponse response = new HttpResponse();
        try {
            String url = request.getUrl();
            String originalUri = getOriginalUri(url);

            //查询重定向表
            if (redirectMap.keySet().contains(originalUri)) {
                request.setUrl(redirectMap.get(originalUri) + url.substring(url.indexOf("?")));
            }

            request.writeTo(s.getOutputStream());
//             在此处阻塞
            response = getResponseFromSocket(s);
            displayResponse(response);

            //重定向
            if (response.getStatus() == HttpStatus.CODE_301 || response.getStatus() == HttpStatus.CODE_302) {
                originalUri = getOriginalUri(url);
                String targetUri = response.getHeader().getProperty(ResponseHeader.LOCATION);
                //更新重定向表
                if (response.getStatus() == HttpStatus.CODE_301) {
                    redirectMap.put(originalUri, targetUri);
                }
                request.setUrl(targetUri + getParamString(url));
                request.writeTo(s.getOutputStream());
                response = getResponseFromSocket(s);
                displayResponse(response);
            }


            return response;
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("请求失败");
        }
    }

    private String getOriginalUri(String url) {
        if (url.contains("?"))
            return url.substring(0, url.indexOf("?"));
        return url;
    }

    private String getParamString(String url) {
        if (url.contains("?"))
            return url.substring(url.indexOf("?"));
        return "";
    }

    private String resMap2String(Map<String, ?> res) {
        StringBuilder builder = new StringBuilder();
        res.forEach((k, v) -> builder.append(k).append(" : ").append(v.toString()).append("\n"));
        return builder.toString();
    }

    private void displayResponse(HttpResponse response) {
        MessageView.display("response", resMap2String(StatusHandler.handle(response)));
    }

    private HttpResponse getResponseFromSocket(Socket s) throws IOException {
        InputStream in = s.getInputStream();
        while (true) {
            //如果一直为0客户端会卡死
            if (in.available() == 0)
                continue;
            HttpResponse response = new HttpResponse(in);
            return response;
        }

    }


}
