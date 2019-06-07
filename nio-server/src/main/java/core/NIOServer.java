package core;

import http.core.HttpRequest;
import http.core.HttpResponse;
import http.exception.HttpParseFailException;
import http.util.handler.HttpRequestUtils;
import http.util.handler.HttpUtils;
import service.Configuration;
import service.HttpContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

public class NIOServer {
    public static void main(String[] args) throws IOException {
        Selector serverSelector = Selector.open();
        Selector clientSelector = Selector.open();

        Configuration.getConfiguration();

        new Thread(() -> {
            try {
                // 对应IO编程中服务端启动
                ServerSocketChannel listenerChannel = ServerSocketChannel.open();
                listenerChannel.socket().bind(new InetSocketAddress(8080));
                listenerChannel.configureBlocking(false);
                listenerChannel.register(serverSelector, SelectionKey.OP_ACCEPT);

                while (true) {
                    // 监测是否有新的连接，这里的1指的是阻塞的时间为1ms
                    if (serverSelector.select(1) > 0) {
                        System.out.println("new connection");
                        Set<SelectionKey> set = serverSelector.selectedKeys();
                        Iterator<SelectionKey> keyIterator = set.iterator();

                        while (keyIterator.hasNext()) {
                            SelectionKey key = keyIterator.next();

                            if (key.isAcceptable()) {
                                try {
                                    // (1) 每来一个新连接，不需要创建一个线程，而是直接注册到clientSelector
                                    SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
                                    clientChannel.configureBlocking(false);
                                    clientChannel.register(clientSelector, SelectionKey.OP_READ);
                                } finally {
                                    keyIterator.remove();
                                }
                            }

                        }
                    }
                }
            } catch (IOException ignored) {
            }

        }).start();


        new Thread(() -> {
            try {
                while (true) {
                    // (2) 批量轮询是否有哪些连接有数据可读，这里的1指的是阻塞的时间为1ms
                    if (clientSelector.select(1) > 0) {
                        Set<SelectionKey> set = clientSelector.selectedKeys();
                        Iterator<SelectionKey> keyIterator = set.iterator();

                        while (keyIterator.hasNext()) {
                            SelectionKey key = keyIterator.next();
                            SocketChannel clientChannel = (SocketChannel) key.channel();

                            if (key.isReadable()) {
                                //判断socket链接是否断开，以防finally块出错
                                boolean closed = false;
                                try {
                                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                                    // (3) 读取数据以块为单位批量读取
                                    clientChannel.read(byteBuffer);
                                    byteBuffer.flip();

                                    System.out.println(new String(byteBuffer.array(), Charset.defaultCharset()));
                                    // 拿到客户端发来的请求
                                    HttpRequest httpRequest = new HttpRequest(new ByteArrayInputStream(byteBuffer.array()));

                                    try {
                                        // 处理请求
                                        HttpContext httpContext = new HttpContext();
                                        HttpResponse response = httpContext.processRequest(httpRequest);
                                        response.writeTo(clientChannel);

                                    } catch (Exception e) {
                                        //handle处理请求过程中抛出的异常
                                        e.printStackTrace();
                                    }

                                    //判断长短连接
                                    HttpUtils httpUtils = new HttpRequestUtils(httpRequest);
                                    //默认时间单位为S
                                    int timeout = 0, max = 0;
                                    LongConnectionContext lcContext = null;

                                    if (httpUtils.isLongConnection()) {
                                        Properties keepAlive = httpUtils.getLongConnectionDuration();
                                        timeout = Integer.parseInt(keepAlive.getProperty(HttpUtils.KEEP_ALIVE_TIMEOUT));
                                        max = Integer.parseInt(keepAlive.getProperty(HttpUtils.KEEP_ALIVE_MAX));

                                        lcContext = (LongConnectionContext) key.attachment();
                                        if (lcContext == null) {
                                            //  否则绑定一个计时器或者计数器
                                            lcContext = new LongConnectionContext();
                                            key.attach(lcContext);
                                        }

                                        lcContext.addRequestsServedCount();
                                    }

                                    //如果是短链接或者长链接到达要求，断开tcp链接
                                    if (!httpUtils.isLongConnection() ||
                                            lcContext.getRequestsServed() >= max ||
                                            lcContext.getInitTime() + timeout * 1000L < new Date().getTime()) {
                                        closed = true;
                                        clientChannel.close();
                                    }

                                } catch (HttpParseFailException ignored) {

                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    if (!closed) {
                                        keyIterator.remove();
                                        key.interestOps(SelectionKey.OP_READ);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IOException ignored) {
            }
        }).start();


    }
}