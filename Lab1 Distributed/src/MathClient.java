/**
 * @author Qusay H. Mahmoud
 */

import java.io.*;
import java.net.*;

public class MathClient {
   public static void main(String argv[]) throws Exception {
     Socket echo;
     DataInputStream br;
     DataOutputStream dos;

     echo = new Socket("localhost", 3500);
     br = new DataInputStream(echo.getInputStream());
     dos = new DataOutputStream(echo.getOutputStream());
     int x = 7, y = 3;
     dos.writeInt(x);
     dos.flush();
     dos.writeInt(y);
     dos.flush();
     String str = br.readLine();
     System.out.println("I got: "+str);
   }
}
