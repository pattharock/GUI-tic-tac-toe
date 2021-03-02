import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class is used to Represent a Server in a Socket Programmed GUI Tic-Tac-Toe game
 * @author ritviksingh
 * @version 1.0
 */


public class Server {
    private Player [] board = new Player[9];
    Player currentPlayer;

    /**
     * This class is the Runnable class(i.e. implements the Runnable interface for multi-threading)
     */
    public class Player implements Runnable{
        Socket connectorSocket;
        Scanner inputScanner;
        PrintWriter outputWriter;

        char piece;
        Player opponent;

        /**
         * this is the constructor for the class Player
         * @param connectorSocket
         * @param piece
         */
        public Player(char piece,Socket connectorSocket){
            this.connectorSocket=connectorSocket;
            this.piece=piece;
        }

        /**
         *Overridden method of the Runnable Interface required for multi-threading.
         */

        public void run(){
            try{
                initiateNetworking();
                getMessageFromClient();
            }catch(Exception ex){
                System.out.println("Message:-\n"+ex.getMessage());
                System.out.println("Stack:-");
                ex.printStackTrace();
            }
            if(opponent!=null && opponent.outputWriter!=null){
                opponent.outputWriter.println("Opponent Left");
            }
            try{
                connectorSocket.close();
            }catch (Exception ex){
                System.out.println("Message:-\n"+ex.getMessage());
                System.out.println("Stack:-");
                ex.printStackTrace();
            }

        }

        /**
         * Receives message from client and
         * performs suitable actions.
         */
        private void getMessageFromClient(){
            while(inputScanner.hasNextLine()){
                String message = inputScanner.nextLine();
                if(message.startsWith("EXIT")){
                    return;
                }
                else if(message.startsWith("PLAYED")){
                   sendMessageToClient(Integer.parseInt(message.substring(7)));
                }
            }
        }

        /**
         * Receives input from client, updates the board, writes back
         * info to the clients
         * @param position
         */
        private void sendMessageToClient(int position){
            try{
                isValidMove(position,this);
                outputWriter.println("Correct Move");
                opponent.outputWriter.println("Opponent Moved "+position);
                if(checkWin()){
                    outputWriter.println("Win");
                    opponent.outputWriter.println("Loss");
                }
                else if(checkDraw()){
                    outputWriter.println("Draw");
                    opponent.outputWriter.println("Draw");
                }
            }catch (IllegalStateException ex){
                outputWriter.println(ex.getMessage());
            }
        }

        /**
         * this method checks for the winning condition of the game
         * @return
         */
        public boolean checkWin(){
            if(checkVerticalWin()){
                return true;
            } else if(checkHorizontalWin()){
                return true;
            } else if((board[0]!=null && board[0].equals(board[4]) &&board[4].equals(board[8]))){
                return true;
            } else if((board[2]!=null && board[2].equals(board[4]) &&board[2].equals(board[6]))){
                return true;
            } else{
                return false;
            }
        }

        /**
         * Checks for win conditions in rows
         * @return
         */
        public boolean checkHorizontalWin(){
            for(int i=0;i<3;i+=3){
                if(board[i]!=null && board[i].equals(board[i+1]) &&board[i+1].equals(board[i+2])){
                    return true;
                }
            }
            return false;
        }

        /**
         * Checks for win conditions in columns
         * @return
         */
        public boolean checkVerticalWin(){
            for(int i=0;i<3;i++){
                if(board[i]!=null && board[i].equals(board[i+3]) &&board[i+3].equals(board[i+6])){
                    return true;
                }
            }
            return false;
        }
        /**
         * this checks if the board is full or not
         * @return
         */
        public boolean checkDraw(){
            int i=0;
            while(i<9){
                if(board[i++]==null){
                    return false;
                }
            }
            return true;
        }

        /**
         * this method checks if the user move is a valid one or not
         *
         * @param location
         * @param player
         */
        public void isValidMove(int location,Player player){
            synchronized (this) {
                if (player != currentPlayer) {
                    throw new IllegalStateException("Opponents turn! Please wait for you turn");
                } else if (player.opponent == null) {
                    throw new IllegalStateException("No Opponent Yet! Wait for opponent.");
                } else if (board[location] != null) {
                    throw new IllegalStateException("Block Occupied! Move Again.");
                }

                board[location] = currentPlayer;
                currentPlayer = currentPlayer.opponent;
            }
        }

        /**
         * This function helps in switching players based on character piece
         * @param piece
         */
        public void switchPlayers(char piece){
            if(piece == 'X'){
                currentPlayer = this;
            } else {
                opponent = currentPlayer;
                opponent.opponent = this;
            }
        }

        /**
         * This method helps initiate networking
         */
        private void initiateNetworking() {
            try{
                inputScanner= new Scanner(connectorSocket.getInputStream());
                outputWriter = new PrintWriter(connectorSocket.getOutputStream(),true);
            }
            catch (Exception ex){
                System.out.println("Message:-\n"+ex.getMessage());
                System.out.println("Stack:-");
                ex.printStackTrace();
            }
            outputWriter.println(piece);
            switchPlayers(piece);
        }
    }

    /**
     * This is the main method of the server, which is used to create 2 threads of the client class and network the same with the running server.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception{
        try (ServerSocket listener = new ServerSocket(50000))
        {
            System.out.println("Server is running...");
            ExecutorService pool = Executors.newFixedThreadPool(2000);
            while(true){
                try{
                    Server server = new Server();
                    pool.execute(server.new Player('X',listener.accept()));
                    pool.execute(server.new Player('O',listener.accept()));
                }catch (Exception ex){
                    System.out.println("Message:-\n"+ex.getMessage());
                    System.out.println("Stack:-");
                    ex.printStackTrace();
                }
            }
        }
    }
}