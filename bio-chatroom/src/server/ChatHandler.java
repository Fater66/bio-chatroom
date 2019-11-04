package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
//Runnable接口 方便开启新线程
public class ChatHandler implements Runnable {

    private ChatServer chatServer;
    private Socket socket;

    public ChatHandler(ChatServer chatServer,Socket socket)
    {
        this.chatServer = chatServer;
        this.socket = socket;
    }


    @Override
    public void run() {
        try {
            //存储 上线用户
            chatServer.addClients(socket);

            //读取发送的信息
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            String msg = null;
            while((msg = reader.readLine())!=null){
                String fwdMsg = "客户端["+socket.getPort()+"]："+msg + "\n";
                System.out.println(fwdMsg);
                //转发信息给别的客户端
                chatServer.forwardMessage(socket,fwdMsg);

                //检查是否退出
                if (chatServer.readyToQuit(msg)){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                chatServer.removeClients(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
