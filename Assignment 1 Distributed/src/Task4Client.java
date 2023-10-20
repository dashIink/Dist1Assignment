

import java.io.*;
import java.net.*;
import java.sql.ResultSet;
import java.sql.Statement;

import static java.lang.Integer.parseInt;
import static java.lang.Thread.sleep;

public class Task4Client {
   public static void main(String argv[]) throws Exception {
     Socket echo;
     DataInputStream br;
     DataOutputStream dos;

     echo = new Socket("localhost", 3500);// get the socket of the localhost
     br = new DataInputStream(echo.getInputStream());// create the new buffered reader

     String str = br.readLine();
     if (str.equals("accepted")) {// only procceed if the server accepts the connection


       dos = new DataOutputStream(echo.getOutputStream());//create new dos

       BufferedReader stdIn =
               new BufferedReader(
                       new InputStreamReader(System.in));

       try {
         System.out.println("Please enter your username");
         String x = stdIn.readLine();// Read username
         x = x + "\n";// add \n to the string to identify the end of the line, server keeps looking for input otherwise
         System.out.println("Please enter your password");
         String y = stdIn.readLine();
         y = y + "\n";
         dos.writeBytes(x);//write username and password to the server
         dos.flush();
         dos.writeBytes(y);
         dos.flush();
         str = br.readLine();
         if (str.equals("login successful")){
           System.out.println("Login Successful");// If successful in the login, procceed

           System.out.println("Please enter the name of the user you wish to message");
           String input = stdIn.readLine();
           input = input + "\n";
           dos.writeBytes(input);// Write to the server of what user we want to communicate with
           dos.flush();
           str = br.readLine();
           while(str.equals("rejected")){// if no user is found, try again
             System.out.println("No Name found");
             System.out.println("Please enter the name of the user you wish to message");
             input = stdIn.readLine();
             input = input + "\n";
             dos.writeBytes(input);
             dos.flush();
             str = br.readLine();
           }
           if (input.equals(x)){// if the users are equal to each other, enter notes mode
             System.out.println("Using notes mode");
           }
             messages m = new messages(br, dos);// we can still enable this thread as it does not recieve any input




            while(!input.equals("exit")){// should the user enter "exit" quit the program
              input = input+"\n";//get message and send it to the server
              dos.writeBytes(input);
              dos.flush();
              input = stdIn.readLine();//read the next input
            }
           input = input+"\n";//write to the server for the final time that we are disconnecting
           dos.writeBytes(input);
           dos.flush();
           str = br.readLine();
           if (str.equals("disconnect")) {// Server confirms disconnect
             System.out.println("Disconnected");
           }
          //m.interrupt();


         }
         else{
            System.out.println("Username or Password is not correct");
         }
       } catch (Exception e) {
         System.out.println("Connection Terminated");
       }
     }
     else{
       str+=" "+ br.readLine();
       System.out.print(str);
     }

   }
  static class messages extends Thread {
    //Create a new thread to recieve messages if any messages are present
    private DataInputStream br;
    private DataOutputStream dos;
    public messages(DataInputStream br, DataOutputStream dos) {

      this.br = br;
      this.dos = dos;

      this.start();
    }

    public void run()
    {
      try {
        String input;
        while(true) {
            //read new line from buffer, this will wait for any new input so it does not need a thread.sleep
            input = br.readLine();

            if(input == null){//if the server sends a null message, the server thread has stopped, stop this thread as well
              currentThread().stop();
            }
            else{
              System.out.println(input);//display message
            }
        }
      }
      catch (Exception e) {
        //System.out.println(e);
      }
    }
  }
}
