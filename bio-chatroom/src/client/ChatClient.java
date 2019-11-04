package client;

import java.io.*;
import java.net.Socket;

public class ChatClient {

    private final String DEFAULT_SERVER_HOST = "127.0.0.1";
    private final int DEFAULT_SERVER_PORT = 8888;
    private final String QUIT = "quit";

    private Socket socket ;
    private BufferedReader reader;
    private BufferedWriter writer;

    public ChatClient(){

    }

    //发送消息至服务器
    public void send(String msg) throws IOException {
        if(!socket.isOutputShutdown()){
            writer.write(msg+"\n");
            writer.flush();
        }
    }
    //从服务器接受消息
    public String receive() throws IOException {
        String msg = null;
        if (!socket.isInputShutdown()){
            msg = reader.readLine();
        }
        return msg;
    }

    //检查用户是否退出
    public boolean readyToQuit(String msg){
        return QUIT.equals(msg);
    }

    public void close(){
        if (writer != null){
            try {
                System.out.println("客户端socket已关闭");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void start(){
        //创建socket对象
        try {
            socket = new Socket(DEFAULT_SERVER_HOST,DEFAULT_SERVER_PORT);
            //创建IO flow
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())
            );

            //UserInputHandler处理用户输入
            new Thread(new UserInputHandler(this)).start();
            //读取服务器转发的各种信息
            String msg = null;
            while ((msg = receive())!=null){
                System.out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close();
        }
    }

    public static void main(String[] args){
        ChatClient chatClient = new ChatClient();
        chatClient.start();
    }
}
