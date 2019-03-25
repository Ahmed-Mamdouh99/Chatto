import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerHandler extends Thread {
  private ServerInstance server;
  private ObjectOutputStream outToServer;
  private ObjectInputStream inFromServer;

  ServerHandler(Socket connSocket, ServerInstance server) throws IOException {
    this.server = server;
    this.outToServer = new ObjectOutputStream(connSocket.getOutputStream());
    this.inFromServer = new ObjectInputStream(connSocket.getInputStream());
  }

  @Override
  public void run() {
    while (true) {
      try {
        sleep(120);
      } catch (InterruptedException ignored) {
      }
      try {
        Object[] in = (Object[]) inFromServer.readObject();
        String function = (String) in[0];
        switch (function) {
          case ("chat"):
            sendMessage(in);
            break;
          case ("list"):
            listMembers();
            break;
          default:
        }
      } catch (IOException | ClassNotFoundException | InterruptedException ignored) {
      }
    }
  }

  private void listMembers() throws InterruptedException, IOException, ClassNotFoundException {
    String[][] memberList = server.list(false);
    outToServer.writeObject(memberList);
    outToServer.flush();
  }

  private void sendMessage(Object[] in) throws IOException {
    String src = (String) in[1];
    String dest = (String) in[2];
    String message = (String) in[3];
    int ttl = (int) in[4];
    server.sendMessage(src, dest, message, ttl);
  }

}
