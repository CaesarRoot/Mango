package util;

import lombok.Synchronized;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 保存socket实例的类
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/6/3
 */
public class SocketHolder {
    //socket池，保存着客户端曾经创建过的socket实例
    private static List<Socket> socketPool = new ArrayList<>();
    //心跳包代号(随便设的)
    private static final int HEART_BEAT_CODE = 0xFF;

    static {
        new Thread(() -> {
            while (true) {

                new Thread(() -> {
                    Socket s;

                    Iterator<Socket> iterator = socketPool.iterator();
                    while (iterator.hasNext()) {
                        s = iterator.next();
                        //判断socket链接是否连通，如果不连通则从内存中删除
                        try {
                            s.sendUrgentData(HEART_BEAT_CODE);
                        } catch (IOException e) {
                            e.printStackTrace();
                            iterator.remove();
                        }
                    }

                    System.out.println("Current size of socket pool is " + socketPool.size());
                }).start();

                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }).start();
    }


    /**
     * 查询socket池中是否已有相应地址和端口的socket的实例，如没有则创建并 加入池中
     *
     * @param ipAddress 目标ip地址
     * @param port      目标端口
     * @return ip地址和端口对应的socket实例
     */
    @Synchronized
    public static Socket of(String ipAddress, int port) throws IOException {
        Socket socket = socketPool.stream()
                .filter(s -> s.getInetAddress().getHostAddress().equals(ipAddress) && s.getPort() == port)
                .findFirst().orElse(null);
        if (socket != null) {
            try {
                socket.sendUrgentData(HEART_BEAT_CODE);
            } catch (IOException e) {
                e.printStackTrace();
                socketPool.remove(socket);
                socket = null;
            }
        }

        if (socket == null) {
            socket = new Socket(ipAddress, port);
            System.out.println("port = " + socket.getLocalPort());
            socketPool.add(socket);
        }

        return socket;
    }


}
