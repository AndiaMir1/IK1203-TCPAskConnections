package tcpclient;
import java.io.*;
import java.net.Socket;

public class TCPClient {
    private boolean shutdown;
    private Integer timeout;
    private Integer limit;

    public TCPClient(boolean shutdown, Integer timeout, Integer limit) {
        this.shutdown = shutdown;
        this.timeout = timeout;
        this.limit = limit;
    }

    // ansluter till en server på hostname, port
    public byte[] askServer(String hostname, int port, byte[] bytesToServer) throws IOException {
        Socket socket = null;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            socket = new Socket(hostname, port); // vi skapar en socket för att ansluta till servern

            /* Skapar en inputstream för att läsa svaret från servern */
            InputStream in = socket.getInputStream();
            /* Vi skapar en OutputStream för att skicka data */
            OutputStream out = socket.getOutputStream();

            if (timeout != null) {
                socket.setSoTimeout(timeout);
            }

            out.write(bytesToServer); // Skicka data till servern
            out.flush(); // säkerställa att allt skickades

            if (shutdown) {
                socket.shutdownOutput();
            }

            byte[] tempBuff = new byte[2048]; // tillfällig buffert
            int bytesRead;
            int totalBytesRead = 0;
            while ((bytesRead = in.read(tempBuff)) != -1) {
                if (limit != null && totalBytesRead + bytesRead > limit) {
                    buffer.write(tempBuff, 0, limit - totalBytesRead);
                    break;
                } else {
                    buffer.write(tempBuff, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }
            }

            /* stänger anslutningen och returnerar svaret */
            in.close();
            out.close();
            socket.close();

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            throw e;
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
        return buffer.toByteArray();
    }

    // ansluter till servern som innan, men utan att skicka data till servern
    public byte[] askServer(String hostname, int port) throws IOException {
        return askServer(hostname, port, new byte[0]);
    }
}
