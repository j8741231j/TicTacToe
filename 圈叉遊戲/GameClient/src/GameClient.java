import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class BackgroundPanel extends JPanel {//繼承面板
    private Image image = null;
    public BackgroundPanel(Image image) {
        this.image = image;
    }
    // 固定背景圖片，允許這個JPanel可以在圖片上新增其他元件
    protected void paintComponent(Graphics g) {
        g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(),this);//畫出跟BackgroundPanel一樣大的圖
    }
}

public class GameClient extends JApplet implements InterfaceConstants
{
    private boolean myTurn=false;
    private char myToken=' ';
    private char otherToken=' ';
    private Cell[][] cell=new Cell[3][3];
    //private int[][] Round=new int[4][2];//創立給每回合存取的空陣列
    private JLabel JTitle=new JLabel();
    private JLabel JStatus=new JLabel();
    private int rowSelect;
    private int columnSelect;
    private DataInputStream fromServer;
    private DataOutputStream toServer;
    private boolean continueToPlay=true;
    private boolean waiting=true;
    private boolean isStandAlone=false;
    private String host="127.0.1.1";
    public void init()
    {
        Image image=new ImageIcon("D:\\JAVA專案區\\Game\\TicTacToe\\GameClient\\src\\LR.png").getImage();
        JPanel Illustration=new BackgroundPanel(image);                          //創立含有背景的面板
        Illustration.setPreferredSize(new Dimension(105,this.getHeight()));//設定面板的寬高
        JPanel Illustration2=new BackgroundPanel(image);
        Illustration2.setPreferredSize(new Dimension(105, this.getHeight()));

        Image image2=new ImageIcon("D:\\JAVA專案區\\Game\\TicTacToe\\GameClient\\src\\M.png").getImage();
        JPanel PCell=new BackgroundPanel(image2);
        PCell.setLayout(new GridLayout(3,3,0,0));//將此面板分割成9等分

        for(int i=0;i<3;i++)
        {
            for (int j=0;j<3;j++)
            {
                PCell.add(cell[i][j]=new Cell(i,j));
            }
        }
        PCell.setBorder(new LineBorder(Color.GRAY,2));

        Illustration.setBorder(new LineBorder(Color.BLACK,0));//Illustration跟Illustration2是存一樣的圖片
        Illustration2.setBorder(new LineBorder(Color.BLACK,0));

        JTitle.setHorizontalAlignment(JLabel.CENTER);
        JTitle.setFont(new Font("標楷體",Font.BOLD,36));  //"標楷體"是真的標楷體 不是單純取名子，Font.BOLD是粗體字
        //JTitle.setBorder(new LineBorder(Color.BLACK,2));
        JTitle.setOpaque(true);                                     //將此物建設成不透明，未來才可以加背景色
        JStatus.setBorder(new LineBorder(Color.BLACK,2));
        JStatus.setOpaque(true);
        JStatus.setFont(new Font("新細明體",Font.BOLD,16));
        JStatus.setBackground(Color.BLACK);
        JStatus.setForeground(Color.GREEN);
        this.getContentPane().add(Illustration,BorderLayout.EAST);
        this.getContentPane().add(Illustration2,BorderLayout.WEST);
        this.getContentPane().add(JTitle,BorderLayout.NORTH);
        this.getContentPane().add(PCell,BorderLayout.CENTER);
        this.getContentPane().add(JStatus,BorderLayout.SOUTH);
        connectToServer();
    }
    private void connectToServer()
    {
        try
        {
            Socket socket;
            if(isStandAlone)
                socket=new Socket(host,9000);
            else
                socket=new Socket(getCodeBase().getHost(),9000);//??????

            fromServer=new DataInputStream(socket.getInputStream());
            toServer=new DataOutputStream(socket.getOutputStream());
            /*System.out.println(socket);
            System.out.println(socket.getInetAddress().getHostAddress());*/

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e);
        }
        Thread thread=new Thread(this::run);//※※
        thread.start();
    }
    public void run()
    {
        try
        {

            int player=fromServer.readInt();//接收GameServer的第37行，即"new DataOutputStream(player1.getOutputStream()).writeInt(PLAYER1);"
            if(player==PLAYER1)
            {
               myToken='X';
               otherToken='O';
               JTitle.setText("玩家1,使用'X'");
               JTitle.setBackground(Color.PINK);
               JTitle.setBorder(new LineBorder(Color.MAGENTA,2));
               JStatus.setText("兩人開局,等待玩家2入場");
               fromServer.readInt();//接收Server端裡的HandleAsession的37行，那個隨便傳來的1(代表有玩家2加入了)
               JStatus.setText("玩家2加入,玩家1先下手");
               myTurn=true;

            }
            else if(player==PLAYER2)
            {
                myToken='O';
                otherToken='X';
                JTitle.setText("玩家2,使用'O'");
                JTitle.setBackground(Color.CYAN);
                JTitle.setBorder(new LineBorder(Color.BLUE,2));
                JStatus.setText("遊戲開始,玩家1先下手");

            }

            while(continueToPlay)//一開始是true
            {

                if (player == PLAYER1)
                {
                    waitForPlayerAction();//程式暫停1秒
                    sendMove();//傳下手位置過去
                    receiveInfoFromServer();//接收戰局結果或接收對方下手位置
                }
                else if (player == PLAYER2)
                    {
                        receiveInfoFromServer();//接收對方下手位置或接收戰局結果
                        waitForPlayerAction();
                        sendMove();//傳下手位置過去
                    }

            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void waitForPlayerAction() throws InterruptedException{
        while(waiting)
        {
            Thread.sleep(100);
        }
        waiting=true;
    }
    private void sendMove() throws IOException{


        toServer.writeInt(rowSelect);//把滑鼠事件紀錄的行列傳出去(沒設定初值但滑鼠事件一定會先觸發)
        toServer.writeInt(columnSelect);

    }
    private void receiveInfoFromServer() throws IOException{




        int status=fromServer.readInt();//接收戰局結果
        if(status==PLAYER1_WIN)
        {
          continueToPlay=false;
          if(myToken=='X')
              JStatus.setText("哈哈，想跟我比，做夢吧!");
          else if(myToken=='O')
          {
              receiveMove();
              JStatus.setText("我大意了阿，這次就算我輸了");
          }
        }else if(status==PLAYER2_WIN)
        {
            continueToPlay=false;
            if(myToken=='O')
                JStatus.setText("怎麼樣，我贏了吧!");
            else if(myToken=='X')
            {
                receiveMove();
                JStatus.setText("可惡!我竟然輸了");
            }
        }else if(status==EQUAL)//由Server端isFull()成立後所傳過來的值
        {
            continueToPlay=false;
            JStatus.setText("遊戲結束，平手");
            if(myToken=='O')
                receiveMove();
        }else if(status==CONTINUE)//如果沒有結果，即比賽繼續
        {

            receiveMove();//接收對方下手位置並在自己的視窗上畫下他的標誌
            JStatus.setText("換我了");
            myTurn=true;
        }
    }
    private void receiveMove() throws IOException
    {
        int row=fromServer.readInt();
        int column=fromServer.readInt();
        cell[row][column].setToken(otherToken);//畫下標誌
    }



    public class Cell extends JLabel implements MouseListener
    {
        private int row;
        private int column;
        private char token=' ';
        public Cell(int row,int column)
        {
            this.row=row;
            this.column=column;
            setBorder(new LineBorder(Color.BLACK,2));
            addMouseListener(this);
        }
       /* public char getToken()
        {
            return token;
        }*/
        public void setToken(char c)
        {
            token=c;
            repaint();//重繪
        }
        protected void paintComponent(Graphics g)//此方法會在repaint()被呼叫時自動執行
        {
            super.paintComponent(g);
            if(token=='X')
            {
                Graphics2D g2 = (Graphics2D)g;
                g2.setStroke(new BasicStroke(6));
                g2.setColor(Color.RED);
                g2.drawLine(10,10,getWidth()-10,getHeight()-10);
                g2.drawLine(getWidth()-10,10,10,getHeight()-10);

            }else if(token=='O')
            {
                Graphics2D g2 = (Graphics2D)g;
                g2.setStroke(new BasicStroke(6));
                g2.setColor(Color.BLUE);
                g2.drawOval(10,10,getWidth()-20,getHeight()-20);
            }

        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if(token==' ' && myTurn)
            {
                setToken(myToken);//更改標誌並重繪(由對手標誌改為我方標誌)
                myTurn=false;
                rowSelect=row;//紀錄列
                columnSelect=column;//紀錄行
                JStatus.setText("等待另一位玩家移動");
                waiting=false;
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }
    public static void main(String[] args)
    {
        JFrame frame=new JFrame("遊戲客戶端");
        GameClient applet=new GameClient();//宣告了一個物件 叫applet
        applet.isStandAlone=true;
        if(args.length==1)
            applet.host=args[0];
        frame.getContentPane().add(applet,BorderLayout.CENTER);
        applet.init();//呼叫init方法
        applet.start();//用start呼叫run方法
        //applet.run();
        frame.setSize(655,540);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
