import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class ServerConnection {
  private ObjectInputStream inFromServer;
  private ObjectOutputStream outToServer;

  ServerConnection(int port) throws IOException {
    Socket connSocket = new Socket("localhost", port);
    inFromServer = new ObjectInputStream(connSocket.getInputStream());
    outToServer = new ObjectOutputStream(connSocket.getOutputStream());
  }

  void sendMessage(String src, String dest, String message, int ttl) throws IOException {
    outToServer.writeObject(new Object[]{"chat", src, dest, message, ttl});
    outToServer.flush();
  }

  synchronized String[][] getList() throws IOException, InterruptedException, ClassNotFoundException {
    outToServer.writeObject(new Object[] {"list"});
    outToServer.flush();
    while (true) {
      try{
        String[][] res = (String[][]) inFromServer.readObject();
        return res;
      } catch (IOException ignored){
      }
      Thread.sleep(120);
    }
  }
}
