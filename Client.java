import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class represents a client in a Socket Programmed Tic Tac Toe Game.
 * @author ritviksingh
 * @version 1.0
 */
public class Client {

    Socket clientSock;
    Scanner inputScanner;
    PrintWriter outputWriter;
    char player;
    char opponent;

    JFrame frame;
    JButton[] cells;
    JMenuBar menuBar;
    JMenu control;
    JMenuItem exit;
    JMenu help;
    JMenuItem instruction;

    JLabel infoLabel;
    JTextField nameField;
    JButton submitButton;
    JPanel gamePanel;
    JPanel bottomPanel;

    JButton currentCell;

    /**
     * Constructor of Client class
     */
    public Client(){
        GUI gui = new GUI();
        gui.go();
    }

    /**
     * Assigns character of opponent based on character of player
     * @param piece
     * @return
     */
    public char assignOpponent(char piece){
        if(piece == 'O'){
            return 'X';
        } else{
            return 'O';
        }
    }

    /**
     *This method establishes a connection b/w the client and server & starts the receiving of messages from the server through the
     * Scanner object
     */
    public void initiateClient(){
        try{
            clientSock = new Socket("localhost",50000);
            inputScanner = new Scanner(clientSock.getInputStream());
            outputWriter = new PrintWriter(clientSock.getOutputStream(),true);
        }catch(Exception ex){
            System.out.println("Message:-\n"+ex.getMessage());
            System.out.println("Stack:-");
            ex.printStackTrace();
        }
        String message = inputScanner.nextLine();
        player = message.charAt(0);
        opponent = assignOpponent(player);

        while(true){
            if(inputScanner.hasNextLine()){
                getMessageFromServer(inputScanner.nextLine());
            }
        }
    }

    /**
     * This method takes the player character as parameter and returns corresponding color for player
     * @param piece
     * @return
     */
    public Color setColorOfPlayer(char piece){
        if(piece == 'X'){
            return Color.GREEN;
        } else{
            return Color.RED;
        }
    }

    /**
     * This method takes the  player character as parameter and returns corresponding color for opponent
     * @param piece
     * @return
     */
    public Color setColorOfOpponent(char piece){
        if(piece == 'X'){
            return Color.RED;
        } else{
            return Color.GREEN;
        }
    }

    /**
     * Sets the game board to be disabled.
     */
    public void disableGameBoard(){
        int i=0;
        while (i<9){
            cells[i++].setEnabled(false);
        }
    }
    /**
     * This method receives the messages from the server
     * makes moves accordingly.
     *
     * @param message
     */


    public void getMessageFromServer(String message){
        if(message.startsWith("Correct Move")){
            infoLabel.setText("Valid move, wait for your opponent.");
            Color colour = setColorOfPlayer(player);
            currentCell.setForeground(colour);
            currentCell.setText(player+"");
            currentCell.repaint();
        }
        else if(message.startsWith("Incorrect Move")){
            infoLabel.setText("Incorrect Move! Please try another move");
        }
        else if(message.startsWith("Opponent Moved")){
            int position = Integer.parseInt(message.substring(15));
            Color colour = setColorOfOpponent(player);
            cells[position].setForeground(colour);
            cells[position].setText(opponent+"");
            cells[position].repaint();
            infoLabel.setText("Your opponent has moved, now is your turn.");
        }
        else if(message.startsWith("Win")){
            JOptionPane.showMessageDialog(frame,"Congratulations! You Win!");
            infoLabel.setText("You Won! Game Ended!");
            disableGameBoard();
        }
        else if(message.startsWith("Draw")){
            JOptionPane.showMessageDialog(frame,"Draw.");
            infoLabel.setText("Draw! Game Ended!");
           disableGameBoard();
        }
        else if(message.startsWith("Loss")){
            JOptionPane.showMessageDialog(frame,"You Lose.");
            infoLabel.setText("You Lost! Game Ended!");
            disableGameBoard();
        }
        else if(message.startsWith("Opponent Left")){
            JOptionPane.showMessageDialog(frame, "Game ends. One of the players left");
            disableGameBoard();
        }
        else{
            infoLabel.setText(message);
        }
    }

    /**
     * Sets the game board to be enabled
     */
    public void enableGameBoard(){
        int i=0;
        while(i<9){
            cells[i++].setEnabled(true);
        }
    }

    /**
     * This inner class is used to set-up the GUI for the client thread.
     */

    private class GUI{
        public void go() {

            frame = new JFrame("Tic Tac Toe");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            menuBar = new JMenuBar();
            control = new JMenu("Control");
            exit = new JMenuItem("Exit");
            exit.addActionListener(new ExitListener());
            control.add(exit);
            menuBar.add(control);

            help = new JMenu("Help");
            instruction = new JMenuItem("Instruction");
            instruction.addActionListener(new InstructionListener());
            help.add(instruction);
            menuBar.add(help);
            frame.setJMenuBar(menuBar);


            infoLabel = new JLabel("Enter your name... ");
            frame.add(infoLabel, BorderLayout.NORTH);

            gamePanel = new JPanel();
            gamePanel.setLayout(new GridLayout(3,3,2,2));
            gamePanel.setBackground(Color.BLACK);
            cells = new JButton[9];
            int i=0;
            while(i<9){
                cells[i]= new JButton();
                cells[i].setBackground(Color.CYAN);
                cells[i].setFont(new Font("Helvetica",Font.BOLD,50));
                gamePanel.add(cells[i]);
                cells[i].addActionListener(new CellListener());
                i++;
            }
            frame.add(gamePanel,BorderLayout.CENTER);

            disableGameBoard();
            bottomPanel = new JPanel();
            bottomPanel.setLayout(new FlowLayout());
            submitButton = new JButton("Submit");
            submitButton.addActionListener(new SubmitListener());
            nameField = new JTextField(10);
            bottomPanel.add(nameField);
            bottomPanel.add(submitButton);
            frame.add(bottomPanel,BorderLayout.SOUTH);;


            frame.repaint();
            frame.setSize(400, 400);
            frame.setVisible(true);
        }
    }

    /**
     * Inner listener class for the cells[index] JButtons.
     */
    class CellListener implements ActionListener{
        public void actionPerformed(ActionEvent event){
            int index = -1;
            for(int i=0;i<9;i++){
                if(event.getSource().equals(cells[i])){
                    index=i;
                    break;
                }
            }
            currentCell = cells[index];
            outputWriter.println("PLAYED "+index);
        }
    }
    /**
     * Inner listener class for the exit JMenuItem.
     */
    public class ExitListener implements ActionListener{
        public void actionPerformed(ActionEvent event) {
            outputWriter.println("EXIT");
            System.exit(0);
        }
    }
    /**
     * Inner listener class for the instruction JMenuItem.
     */
    public class InstructionListener implements ActionListener{
        public void actionPerformed(ActionEvent event) {
            JOptionPane.showMessageDialog( frame , "Some information about the game:\n " +
                    "Criteria for a valid move: \n" +
                    "-The move is not occupied by any mark. \n" +
                    "-The move is made in the Player's turn. \n" +
                    "-The move is made within 3x3 board.\n" +
                    "The game would continue and switch among the opposite players until it reaches either one of the following conditions:\n" +
                    "-Player 1 wins.\n" +
                    "-Player 2 wins.\n" +
                    "-Draw.");
        }
    }

    /**
     *Inner listener class for the submit JButton.
     */
    public class SubmitListener implements ActionListener{
        public void actionPerformed(ActionEvent event) {
            String name = nameField.getText();
            frame.setTitle("Tic Tac Toe-Player: "+name);
            infoLabel.setText("WELCOME "+ name);
            enableGameBoard();
            nameField.setEnabled(false);
            submitButton.setEnabled(false);
        }
    }

    /**
     * This is the main method, used to create an instance of Client.
     * @param args
     */
    public static void main(String[] args) {
        Client client = new Client();
        client.initiateClient();
    }

}