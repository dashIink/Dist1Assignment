/**
 * @author Qusay H. Mahmoud
 */

import java.io.*;
import java.net.*;
import java.sql.*;
import java.time.LocalTime;
import java.util.Arrays;

import static java.lang.Math.abs;

/*public class Task4Server {


    ServerSocket hi;
    Socket client;
    DataInputStream br;
    DataOutputStream dos;

 
   public static int add(int a, int b) {
      return a+b;
   }

   public static void main(String argv[]) throws Exception {
     new MathServer();
   }

   public Task4Server() throws Exception {
     hi = new ServerSocket(3500);
     System.out.println("Server Listening on port 3500....");
     client = hi.accept();
     br = new DataInputStream(client.getInputStream());
     dos = new DataOutputStream(client.getOutputStream());
  
     int x = br.readInt();
     System.out.println("I got: "+x);
     int y = br.readInt();
     System.out.println("I got: "+y);
     int total = 0;
     if (x > y){
         for (int i = 0; i <= abs(x-y); i++){
             System.out.println(y+i);
             total += y+i;
         }
     }
     else{
         for (int i = 0; i <= abs(x-y); i++){
             System.out.println(x+i);
             total += x+i;
         }
     }

     double Answer = (double) total /(abs(x-y)+1);


     System.out.println("I am sending the answer...");
     dos.writeBytes("the sum is: "+Answer+"\n");
   }
}*/

public class Task4Server extends Thread {
    static int connections = 0;
    private ServerSocket mathServer;

    public static void main(String argv[]) throws Exception {
        new Task4Server();
    }

    public Task4Server() throws Exception {
        mathServer = new ServerSocket(3500);
        System.out.println("Server listening on port 3500.");
        this.start();
    }

    @SuppressWarnings("unused")
    public void run() {
        DataOutputStream dos;
          // edit


        while (true) {
            try {
                System.out.println("Waiting for connections.");
                Socket client = mathServer.accept();
                dos = new DataOutputStream(client.getOutputStream());
                if (connections < 5) {
                    System.out.println("Accepted a connection from: " + client.getRemoteSocketAddress());
                    dos.writeBytes("accepted\n");
                    Connect c = new Connect(client);
                    connections++;
                }
                else{
                    System.out.println("Too many connections, closing " + client.getRemoteSocketAddress());
                    dos.writeBytes("Too many connections\n");
                    dos.writeBytes(java.time.LocalTime.now().toString()+"\n");
                    client.close();
                }
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
            br = new DataInputStream(client.getInputStream());

            dos = new DataOutputStream(client.getOutputStream());
        } catch (Exception e1) {
            try {
                client.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return;
        }

        this.start();
    }

    public void run() {
        try {
            String url = "jdbc:mysql://localhost:3306/distassignment1";  // edit
            String username = "root";  // edit
            String password = "root";
            try {
                Connection connection = DriverManager.getConnection(url, username, password);
                if (connection != null) {
                    System.out.println("Connected to the database!");

                    Statement statement = connection.createStatement();

                    System.out.println("Reading Credentials");

                    String x = br.readLine();
                    System.out.println("Reading Username as: "+x);
                    String y = br.readLine();
                    System.out.println("Reading Password as: "+y);
                    int count = 0;

                    ResultSet resultSet = statement.executeQuery("SELECT * FROM login WHERE username LIKE '"+x+"'");

                    while(resultSet.next()){
                        if (resultSet.getString("password").equals(y)){
                            dos.writeBytes("login successful\n");
                            int userID = resultSet.getInt("idLogin");
                            int recieverID = 0;
                            String input = br.readLine();

                            while(recieverID == 0) {
                                resultSet = statement.executeQuery("SELECT * FROM login WHERE username LIKE '" + input + "'");
                                if(resultSet.next()) {
                                    recieverID = resultSet.getInt("idLogin");
                                    dos.writeBytes("accepted\n");
                                    dos.flush();
                                }
                                else{
                                    dos.writeBytes("rejected\n");
                                    input = br.readLine();
                                }
                            }
                            System.out.println(input);
                            if (userID != recieverID) {
                                messages m = new messages(br, dos, resultSet, statement, userID, recieverID);
                            }
                            else{
                                resultSet = statement.executeQuery("SELECT * FROM messages WHERE Sender = "+recieverID+" AND Reciever = "+userID+"");
                                while(resultSet.next()){
                                    input = resultSet.getString("MessageContent");
                                    dos.writeBytes(input + "\n");
                                    dos.flush();
                                    System.out.println("Written Message: " + input);
                                }

                            }
                            input = br.readLine();
                            input = br.readLine();

                            while(!input.equals("exit")){
                                if (!input.equals("")) {
                                    count = statement.executeUpdate("INSERT INTO messages (Sender, Reciever ,MessageContent) VALUES (" + userID + ", " + recieverID + ", '" + input + "')");
                                }
                                 System.out.println(count);
                                 input = br.readLine();
                                 count = 0;
                                 System.out.println(input);
                            }
                            dos.writeBytes("disconnect\n");
                            dos.flush();


                            System.out.println("User has disconnected");
                            break;

                        }
                        else{
                            dos.writeBytes("login rejected\n");
                        }
                    }


                    client.close();
                    Task4Server.connections--;

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
            try {
                String input;
                int count = 0;
                int id = 0;
                while(true){
                    resultSet = statement.executeQuery("SELECT count(*) FROM messages WHERE Sender = "+recieverID+" AND Reciever = "+userID+"");
                    resultSet.next();
                    if (resultSet.getInt(1) > count){
                        count = resultSet.getInt(1);
                        resultSet = statement.executeQuery("SELECT * FROM messages WHERE Sender = "+recieverID+" AND Reciever = "+userID+" AND id > "+id+"");
                        while(resultSet.next()){
                            input = resultSet.getString("MessageContent");
                            dos.writeBytes(input + "\n");
                            dos.flush();
                            System.out.println("Written Message: " + input);
                            if (id < resultSet.getInt("id")){
                                id = resultSet.getInt("id");
                            }
                        }
                    }
                Thread.sleep(1000);


                }

            }
            catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}



