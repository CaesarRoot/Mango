package http.core;

import lombok.Getter;
import lombok.Setter;

import java.io.*;

/**
 * HTTP报文实体类
 *
 * @author steve
 */
public class HttpBody {
    /**
     * 媒体类型
     */
    @Getter
    private MediaType mediaType;

    /**
     * 实体部分
     */
    @Getter
    @Setter
    private InputStream content;

    HttpBody(String contentType) throws Exception {
        this(contentType, "");
    }

    HttpBody(String contentType, String content) throws Exception {
        if (contentType.isEmpty()) throw new Exception("contentType不能为空！");

        this.mediaType = new MediaType(contentType);
        this.content = new ByteArrayInputStream(content.getBytes(this.mediaType.getCharset()));
    }

    HttpBody(String contentType, InputStream content) throws Exception {
        if (contentType.isEmpty()) throw new Exception("contentType不能为空！");

        this.mediaType = new MediaType(contentType);
        this.content = content;
    }

    /**
     * 获取HTTP报文实体的文本内容
     *
     * @return HTTP报文实体文本内容
     */
    String getTextContent() {
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(this.content));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
