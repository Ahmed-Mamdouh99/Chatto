import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {
  private Socket connSocket;
  private ServerInstance server;
  private String uuid;
  private ObjectOutputStream outToUser;
  private ObjectInputStream inFromUser;
  private String username;


  ClientHandler(String uuid, Socket connSocket, ServerInstance server) throws IOException {
    this.connSocket = connSocket;
    this.uuid = uuid;
    this.server = server;
    outToUser = new ObjectOutputStream(connSocket.getOutputStream());
    inFromUser = new ObjectInputStream(connSocket.getInputStream());
    try {
      username = (String) inFromUser.readObject();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    while (true) {
      try {
        sleep(120);
      } catch (InterruptedException ignored) {
      }
      try {
        Object[] in = (Object[]) inFromUser.readObject();
        String function = (String) in[0];
        switch (function) {
          case ("chat"):
            chat(in);
            break;
          case ("list"):
            listMembers();
            break;
          case ("quit"):
            quit();
            return;
          default:
        }
      } catch (IOException | ClassNotFoundException | InterruptedException ignored) {
      }
    }
  }

  private void quit() {
    server.removeClient(uuid);
    try {
      outToUser.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      inFromUser.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      connSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void listMembers() throws InterruptedException, IOException, ClassNotFoundException {
    String[][] memberList = server.list(true);
    outToUser.writeObject(new Object[] {"list", memberList});
    outToUser.flush();
  }

  private void chat(Object[] in) throws IOException {
    server.sendMessage(uuid, (String) in[1], (String) in[2], (int) in[3]);
  }

  void sendChat(String src, String message) throws IOException {
    outToUser.writeObject(new String[]{"chat", src, message});
    outToUser.flush();
  }

  String getUuid() {
    return uuid;
  }

  String getUsername() {
    return username;
  }
}
