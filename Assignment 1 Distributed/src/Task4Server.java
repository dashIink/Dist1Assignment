
import java.io.*;
import java.net.*;
import java.sql.*;


public class Task4Server extends Thread {

    private ServerSocket mathServer;

    public static void main(String argv[]) throws Exception {
        new Task4Server();
    }

    public Task4Server() throws Exception {
        //create a new server socket on port 3500
        mathServer = new ServerSocket(3500);
        System.out.println("Server listening on port 3500.");
        this.start();
    }

    @SuppressWarnings("unused")
    public void run() {
        DataOutputStream dos;

        //Run continuously when the server is on, always look for new connections
        while (true) {
            try {
                System.out.println("Waiting for connections.");
                //If a connection comes in, pass it to another thread. Create a new data output stream for it to use.
                Socket client = mathServer.accept();
                dos = new DataOutputStream(client.getOutputStream());
                System.out.println("Accepted a connection from: " + client.getRemoteSocketAddress());
                //write back to the Client that the connection has been accepted
                dos.writeBytes("accepted\n");
                //start the new thread
                Connect c = new Connect(client);

            } catch (Exception e) {
            }
        }
    }
}

class Connect extends Thread {

    private ServerSocket hi;
    private Socket client;
    private DataInputStream br;
    private DataOutputStream dos;

    public Connect() {
    }

    public Connect(Socket clientSocket) {

        client = clientSocket;
        try {
            //set the local values of buffered reader and data output stream to the ones passed by the main thread
            br = new DataInputStream(client.getInputStream());

            dos = new DataOutputStream(client.getOutputStream());
        } catch (Exception e1) {
            try {
                //If there is an error, close the connection
                client.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return;
        }
        //Start the thread
        this.start();
    }

    public void run() {
        try {
            //Using jdbc, we connect to our local database on the localhost, using port 3306
            String url = "jdbc:mysql://localhost:3306/distassignment1";
            String username = "root"; // The username and password are very simple, if running this as an example, then adjust them to the username and password
            String password = "root"; // that you set
            try {
                Connection connection = DriverManager.getConnection(url, username, password); // connect to the database
                if (connection != null) {
                    System.out.println("Connected to the database!");

                    Statement statement = connection.createStatement();

                    System.out.println("Reading Credentials");

                    String x = br.readLine();// read the username and password from the user
                    System.out.println("Reading Username as: "+x);
                    String y = br.readLine();
                    System.out.println("Reading Password as: "+y);
                    int count = 0; // count is a variable that will be used to count the number of resutls from SQL statemenets

                    ResultSet resultSet = statement.executeQuery("SELECT * FROM login WHERE username LIKE '"+x+"'");// using this, we look for users with the same username

                    while(resultSet.next()){
                        if (resultSet.getString("password").equals(y)){ //check if the password is matching
                            dos.writeBytes("login successful\n");//send to the user that the login was successful
                            int userID = resultSet.getInt("idLogin"); // track the user ID and recieverID
                            int recieverID = 0;
                            String input = br.readLine();// get the input from the user of what user they wish to talk to

                            while(recieverID == 0) {
                                resultSet = statement.executeQuery("SELECT * FROM login WHERE username LIKE '" + input + "'"); //Get Id's of users who have matching usernames
                                if(resultSet.next()) {
                                    recieverID = resultSet.getInt("idLogin");//If found, put recieverID into variable
                                    dos.writeBytes("accepted\n");//communicate to client that the user has been accepted
                                    dos.flush();
                                }
                                else{
                                    dos.writeBytes("rejected\n");//Cannot find a user, ask the user to re-enter a username
                                    input = br.readLine();
                                }
                            }
                            System.out.println(input);//This is the user we are connected to
                            if (userID != recieverID) {// If the username and recieverID are the same, this user is trying to use notes mode
                                messages m = new messages(br, dos, resultSet, statement, userID, recieverID); // Create another thread to receive messages from the other user
                            }
                            else{
                                resultSet = statement.executeQuery("SELECT * FROM messages WHERE Sender = "+recieverID+" AND Reciever = "+userID+""); // Send all messages to the user that they have sent to themselves
                                while(resultSet.next()){
                                    input = resultSet.getString("MessageContent"); // Writes the content of the message to the Client
                                    dos.writeBytes(input + "\n");
                                    dos.flush();
                                    System.out.println("Written Message: " + input);
                                }

                            }
                            input = br.readLine();// User sends some unknown data to the server, use these to clear the buffer
                            input = br.readLine();

                            while(!input.equals("exit")){//If the user types "exit" at any point, close their connection
                                if (!input.equals("")) {
                                    // enter the message content into the database, noting the ID's of each user
                                    count = statement.executeUpdate("INSERT INTO messages (Sender, Reciever ,MessageContent) VALUES (" + userID + ", " + recieverID + ", '" + input + "')");
                                }
                                 System.out.println(count);
                                 input = br.readLine();// wait for next value to be written
                                 count = 0;
                                 System.out.println(input);
                            }
                            dos.writeBytes("disconnect\n");// send to the user that the connection is being terminated
                            dos.flush();


                            System.out.println("User has disconnected");
                            break;// break out of the loop, close connection

                        }
                        else{
                            dos.writeBytes("login rejected\n");
                        }
                    }


                    client.close();

                } else {
                    System.out.println("Failed to connect to the database.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
        }
    }
    class messages extends Thread {

        private DataInputStream br;
        private DataOutputStream dos;
        private ResultSet resultSet;
        private Statement statement;
        private int userID;
        private int recieverID;
        public messages(DataInputStream br, DataOutputStream dos, ResultSet resultSet, Statement statement, int userID, int recieverID) {

            // set local values to those passed by constructor
            this.br = br;
            this.dos = dos;
            this.resultSet = resultSet;
            this.statement = statement;
            this.userID = userID;
            this.recieverID = recieverID;

            this.start();
        }

        public void run()
        {
            //The point of this thread is to act as a check for new messages, enabling live messaging
            try {
                String input;
                int count = 0;
                int id = 0; // keep the ID of the latest message
                while(true){
                    resultSet = statement.executeQuery("SELECT count(*) FROM messages WHERE Sender = "+recieverID+" AND Reciever = "+userID+"");// check to see if there are new message by counting them
                    resultSet.next();
                    if (resultSet.getInt(1) > count){// if the number of messages counted is more than the number that has been displayed so far, get the new messages
                        count = resultSet.getInt(1);
                        resultSet = statement.executeQuery("SELECT * FROM messages WHERE Sender = "+recieverID+" AND Reciever = "+userID+" AND id > "+id+""); // get all messages that are new
                        while(resultSet.next()){
                            input = resultSet.getString("MessageContent");
                            dos.writeBytes(input + "\n");// write the bytes of each message to the client
                            dos.flush();
                            System.out.println("Written Message: " + input);
                            if (id < resultSet.getInt("id")){
                                id = resultSet.getInt("id");// set the highest ID as the latest message
                            }
                        }
                    }
                Thread.sleep(1000);// only run once a second, as to not overload the server


                }

            }
            catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}



