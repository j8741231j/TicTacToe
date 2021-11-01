import javax.swing.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.*;//Socket在裡面
import java.util.Date;
import java.awt.*;
import javax.swing.JScrollPane;

public class GameServer extends JFrame implements InterfaceConstants {
    public static void main(String[] args)

    {
        GameServer frame=new GameServer();
    }
    public GameServer()
    {
        JTextArea GameLog=new JTextArea();
        JScrollPane ScrollPane=new JScrollPane(GameLog);
        getContentPane().add(ScrollPane,BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GameLog.setOpaque(true);
        GameLog.setForeground(Color.GREEN);
        GameLog.setBackground(Color.BLACK);
        setSize(500,300);
        setTitle("遊戲伺服器端");
        setVisible(true);
        try
        {
            ServerSocket serverSocket=new ServerSocket(9000);
            GameLog.append(new Date() +":伺服器從ServerSocket連接埠(port)9000"+"\n");//在GameLog(JTextArea)裡加入日期與字串
            int sessionNo=1;
            while (true)
            {
                GameLog.append(new Date()+"等待玩家來參加此session"+sessionNo+"\n");

                Socket player1= serverSocket.accept();//Socket是內建的，等待有人連進來，程式會在這等
                GameLog.append(new Date()+"玩家1參與了此session"+sessionNo+"\n");//等到有人進來了才加這行
                GameLog.append("玩家1的IP位置"+player1.getInetAddress().getHostAddress()+"\n");
                new DataOutputStream(player1.getOutputStream()).writeInt(PLAYER1);

                Socket player2= serverSocket.accept();//Socket是內建的
                GameLog.append(new Date()+"玩家2參與了此session"+sessionNo+"\n");
                GameLog.append("玩家2的IP位置"+player2.getInetAddress().getHostAddress()+"\n");
                new DataOutputStream(player2.getOutputStream()).writeInt(PLAYER2);

                GameLog.append(new Date()+"從此session啟動執行緒"+sessionNo+"\n");
                sessionNo++;
                HandleAsession thread=new HandleAsession(player1,player2);
                thread.start();
            }

        }
        catch (IOException e) {
            System.err.println(e);
        }
    }
}
