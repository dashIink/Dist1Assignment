/**
 * @author Qusay H. Mahmoud
 */

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

     echo = new Socket("localhost", 3500);
     br = new DataInputStream(echo.getInputStream());

     String str = br.readLine();
     if (str.equals("accepted")) {


       dos = new DataOutputStream(echo.getOutputStream());

       BufferedReader stdIn =
               new BufferedReader(
                       new InputStreamReader(System.in));

       try {
         System.out.println("Please enter your username");
         String x = stdIn.readLine();
         x = x + "\n";
         System.out.println("Please enter your password");
         String y = stdIn.readLine();
         y = y + "\n";
         dos.writeBytes(x);
         dos.flush();
         dos.writeBytes(y);
         dos.flush();
         str = br.readLine();
         if (str.equals("login successful")){
           System.out.println("Login Successful");

           System.out.println("Please enter the name of the user you wish to message");
           String input = stdIn.readLine();
           input = input + "\n";
           dos.writeBytes(input);
           dos.flush();
           str = br.readLine();
           while(str.equals("rejected")){
             System.out.println("No Name found");
             System.out.println("Please enter the name of the user you wish to message");
             input = stdIn.readLine();
             input = input + "\n";
             dos.writeBytes(input);
             dos.flush();
             str = br.readLine();
           }
           if (input.equals(x)){
             System.out.println("Using notes mode");
           }
             messages m = new messages(br, dos);




            while(!input.equals("exit")){
              input = input+"\n";
              dos.writeBytes(input);
              dos.flush();
              input = stdIn.readLine();
            }
           input = input+"\n";
           dos.writeBytes(input);
           dos.flush();
           str = br.readLine();
           if (str.equals("disconnect")) {
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
            input = br.readLine();

            if(input == null){
              currentThread().stop();
            }
            else{
              System.out.println(input);
            }
        }
      }
      catch (Exception e) {
        //System.out.println(e);
      }
    }
  }
}
