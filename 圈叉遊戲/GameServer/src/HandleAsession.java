import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class HandleAsession extends Thread implements InterfaceConstants
{
    private Socket player1;
    private Socket player2;
    private char[][] cell=new char[3][3];
    private DataInputStream fromPlayer1;//來至玩家1的訊息接口
    private DataOutputStream toPlayer1;//傳給玩家1的訊息接口
    private DataInputStream fromPlayer2;//來至玩家2的訊息接口
    private DataOutputStream toPlayer2;//傳給玩家2的訊息接口

    public HandleAsession(Socket player1,Socket player2)
    {
        this.player1=player1;
        this.player2=player2;
        for(int i=0;i<3;i++)
        {
            for(int j=0;j<3;j++)
            {
                cell[i][j]=' ';
            }
        }
    }
    public void run()
    {
        try
        {
            fromPlayer1=new DataInputStream(player1.getInputStream());
            toPlayer1=new DataOutputStream(player1.getOutputStream());
            fromPlayer2=new DataInputStream(player2.getInputStream());
            toPlayer2=new DataOutputStream(player2.getOutputStream());
            toPlayer1.writeInt(1);//此處的1沒有任何意思，就是隨便傳東西給玩家1讓他知道有玩家2加入了

            while (true)
            {

                int row=fromPlayer1.readInt();
                int colum=fromPlayer1.readInt();
                cell[row][colum]='X';
                if(isWin('X'))
                {
                    toPlayer1.writeInt(PLAYER1_WIN);
                    toPlayer2.writeInt(PLAYER1_WIN);
                    sendMove(toPlayer2,row,colum);
                    break;
                }else if(isFull())
                    {
                        toPlayer1.writeInt(EQUAL);
                        toPlayer2.writeInt(EQUAL);
                        sendMove(toPlayer2,row,colum);
                        break;
                    }else {
                        toPlayer2.writeInt(CONTINUE);
                        sendMove(toPlayer2,row,colum);

                    }



                row=fromPlayer2.readInt();
                colum=fromPlayer2.readInt();

                cell[row][colum]='O';
                if(isWin('O'))
                {

                    toPlayer1.writeInt(PLAYER2_WIN);
                    toPlayer2.writeInt(1);
                    toPlayer2.writeInt(1);
                    toPlayer2.writeInt(PLAYER2_WIN);
                    sendMove(toPlayer1,row,colum);
                    break;
                }else{
                    toPlayer1.writeInt(CONTINUE);
                    sendMove(toPlayer1,row,colum);
                }

            }
        }
        catch (IOException e) {
            System.err.println(e);
        }
    }
    private void sendMove(DataOutputStream out,int row,int colum)throws IOException
    {
        out.writeInt(row);
        out.writeInt(colum);
    }
    private boolean isFull()//代表所有框框都被填了 遊戲就要判定結果了
    {
        for(int i=0;i<3;i++)
        {
            for(int j=0;j<3;j++)
            {
                if(cell[i][j]==' ')
                    return false;
            }
        }
        return true;
    }
    private boolean isWin(char token)
    {
        for(int i=0;i<3;i++)
        {
            if(cell[i][0]==token && cell[i][1]==token && cell[i][2]==token)
                return  true;
        }
        for(int j=0;j<3;j++)
        {
            if(cell[0][j]==token && cell[1][j]==token && cell[2][j]==token)
                return true;
        }
        if((cell[0][0]==token && cell[1][1]==token && cell[2][2]==token) || (cell[2][0]==token && cell[1][1]==token && cell[0][2]==token))
            return true;
        return false;
    }
}
