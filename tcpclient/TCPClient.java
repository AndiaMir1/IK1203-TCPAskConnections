package tcpclient;
import java.io.*;
import java.net.*;

public class TCPClient {
    private boolean shutdown;
    private Integer timeout;
    private Integer limit;

    public TCPClient(boolean shutdown, Integer timeout, Integer limit) {
        this.shutdown = shutdown;
        this.timeout = timeout;
        this.limit = limit;
    }

    // Ansluter till en server på hostname, port och skickar data till servern
    public byte[] askServer(String hostname, int port, byte[] bytesToServer) throws IOException {
        Socket socket = new Socket(hostname, port); // Vi skapar en socket för att ansluta till servern
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); // Kan skriva data till en byte-array
        InputStream inputStream = socket.getInputStream(); // Kan läsa data från servern
        OutputStream outputStream = socket.getOutputStream(); // Kan skicka data till servern

        if (bytesToServer != null) {
            outputStream.write(bytesToServer); // Skicka data till servern
            outputStream.flush(); // Säkerställa att allt skickades
        }

        if (shutdown) {
            socket.shutdownOutput(); // Stänger anslutningen i utgående riktning om shutdown är true
        }

        byte[] buffer = new byte[1024]; // Lagrar datan som kommer från servern i en byte-array
        int bytesRead;
        long startTime = System.currentTimeMillis();

        while (true) {
            long elapsedTime = System.currentTimeMillis() - startTime;
        
            if (timeout != null && elapsedTime > timeout) { 
                System.out.println("En timeout har brytits ut! Ingen respons inom " + timeout + " ms."); 
                break; // Timeout uppnådd, avsluta
            }
            
        
            // Kontrollera om det finns data att läsa
            if (inputStream.available() > 0) {  
                bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) break;  // Anslutningen stängdes av servern
        
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                startTime = System.currentTimeMillis(); // Uppdatera starttiden vid mottagning av data
        
                if (limit != null && byteArrayOutputStream.size() >= limit) {
                    break;  // Avsluta om vi har tagit emot tillräckligt med data
                }
            } else {
                try {
                    Thread.sleep(10); // Kortare väntetid för att reagera snabbare på timeout
                } catch (InterruptedException e) {
                    
                    Thread.currentThread().interrupt();
                    break;

                }
            }
        }
        

        socket.close(); // Stänger anslutningen
        return byteArrayOutputStream.toByteArray(); // Returnerar svaret från servern
    }

    // Ansluter till servern som innan, men utan att skicka data till servern
    public byte[] askServer(String hostname, int port) throws IOException {
        return askServer(hostname, port, new byte[0]);
    }
}
