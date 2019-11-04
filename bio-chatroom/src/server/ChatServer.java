package server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private final String QUIT = "quit";
    private int DEFAULT_PORT = 8888;

    private ExecutorService executorService;
    private ServerSocket serverSocket;
    private Map<Integer, Writer> connectedClients;

    public ChatServer(){
        //线程池中有10个线程 = 支持10 个用户
        executorService = Executors.newFixedThreadPool(10);
        connectedClients = new HashMap<>();
    }
    //synchronized 保证同一时间内只有一个线程添加 —— 线程安全性
    public synchronized void addClients(Socket socket) throws IOException {
        if(socket!= null)
        {
            int port = socket.getPort();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())
            );
            connectedClients.put(port,writer);
            System.out.println("客户端["+port+"]已连接");
        }
    }

    public synchronized void removeClients(Socket socket) throws IOException {
        if (socket!=null)
        {
            int port = socket.getPort();
            if (connectedClients.containsKey(port))
            {
                connectedClients.get(port).close();
            }
            connectedClients.remove(port);
            System.out.println("客户端["+port+"]已断开连接");
        }
    }

    public synchronized void forwardMessage(Socket socket,String fwdMsg) throws IOException {
        for (Integer id : connectedClients.keySet())
        {
            if (!id.equals(socket.getPort())){
                Writer writer = connectedClients.get(id);
                writer.write(fwdMsg);
                writer.flush();//?
            }
        }
    }

    public boolean readyToQuit(String msg){
        return QUIT.equals(msg);
    }

    public synchronized void close(){
        if (serverSocket != null){
            try {
                serverSocket.close();
                System.out.println("服务器已关闭");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //服务器主要逻辑
    public void start(){
        //绑定监督端口 类似之前练习
        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("服务器已启动，监听端口["+DEFAULT_PORT+"]");

            while (true){
                //等待客户端连接
                Socket socket = serverSocket.accept();
                //创建handler 线程
//              BIO模型：  new Thread(new ChatHandler(this,socket)).start();
                //伪异步IO(利用线程池启动)⬇
                executorService.execute(new ChatHandler(this,socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close();
        }
    }

    public static void main(String[] args){
        ChatServer chatServer = new ChatServer();
        chatServer.start();
    }
}
