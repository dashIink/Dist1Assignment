import java.net.*;
import java.io.*;
 
public class EchoServer {
    public static void main(String[] args) {
         
        if (args.length != 1) {
            System.err.println("Usage: java EchoServer <port number>");
            System.exit(1);
        }
         
        int portNumber = Integer.parseInt(args[0]);
         
        try {
            ServerSocket serverSocket =
                new ServerSocket(Integer.parseInt(args[0]));
            Socket clientSocket = serverSocket.accept();     
            PrintWriter out =
                new PrintWriter(clientSocket.getOutputStream(), true);                   
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;

            String userInput;
            InetAddress IP = clientSocket.getInetAddress();
            String ipClient = IP.toString();

            int ClientPort = serverSocket.getLocalPort();
            System.out.println(ipClient + " " + ClientPort);

            while ((inputLine = in.readLine()) != null) {
                out.println(ipClient);
                out.println(clientSocket);
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}