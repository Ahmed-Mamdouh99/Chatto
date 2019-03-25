import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.UUID;

public class ServerInstance {
  //private static boolean testing = false;
  private ServerSocket clientListener;
  private ServerSocket serverListener;
  private Hashtable<String, ClientHandler> clients;
  private ServerConnection otherServer;

  private ServerInstance(ServerSocket clientListener, ServerSocket serverListener, Integer serverPort) throws IOException {
    this.clientListener = clientListener;
    this.serverListener = serverListener;
    this.clients = new Hashtable<>();
    if (serverPort != null) {
      otherServer = new ServerConnection(serverPort);
    }
  }

  private void listen() throws InterruptedException {
    Thread clientListener = new Thread(this::acceptClients);
    Thread serverListener = new Thread(this::acceptServers);
    clientListener.start();
    serverListener.start();
    serverListener.join();
    clientListener.join();
  }

  void sendMessage(String src, String dest, String message, int ttl) throws IOException {
    if (clients.containsKey(dest)) {
      clients.get(dest).sendChat(src, message);
    } else if (ttl-- > 0 && otherServer != null) {
      otherServer.sendMessage(src, dest, message, ttl);
    }
  }

  String[][] list(boolean prop) throws InterruptedException, IOException, ClassNotFoundException {
    String[][] res;
    if (prop && otherServer != null) {
      String[][] arr1 = otherServer.getList();
      res = new String[arr1.length + clients.size()][];
      if (res.length - clients.size() > 0) {
        System.arraycopy(arr1, 0, res, clients.size(), arr1.length);
      }
    } else {
      res = new String[clients.size()][];
    }
    int index = 0;
    for (ClientHandler client : clients.values()) {
      res[index++] = new String[]{client.getUsername(), client.getUuid()};
    }
    return res;
  }

  private void acceptClients() {
    Socket connSocket;
    while (true) {
      try {
        Thread.sleep(120);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      try {
        connSocket = clientListener.accept();
        String id = UUID.randomUUID().toString();
        ClientHandler newHandler = new ClientHandler(id, connSocket, this);
        newHandler.start();
        clients.put(id, newHandler);
      } catch (IOException ignored) {
      }
    }
  }

  private void acceptServers() {
    Socket connSocket = null;
    while (true) {
      try {
        Thread.sleep(120);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      try {
        connSocket = serverListener.accept();
        ServerHandler newHandler = new ServerHandler(connSocket, this);
        newHandler.start();
      } catch (IOException ignored) {
      }
      if(connSocket != null){
        // Register the other server
        try {
          otherServer = new ServerConnection(5001);
        } catch (IOException ignored) {
        }
      }
    }
  }

  void removeClient(String id) {
    clients.remove(id);
  }

  public static void main(String[] args) {
    runServer();
    //testRun();
  }

  private static void runServer(){
    try{
      runServer1();
    } catch (InterruptedException | IOException ignored) {
      try{
        runServer2();
      } catch (InterruptedException | IOException ignored2) {
      }
    }
  }

  private static void runServer1() throws IOException, InterruptedException {
    ServerSocket clientSocket = new ServerSocket(3000);
    ServerSocket serverSocket = new ServerSocket(5000);
    ServerInstance instance = new ServerInstance(clientSocket, serverSocket, null);
    instance.listen();
  }

  private static void runServer2() throws IOException, InterruptedException {
    ServerSocket clientSocket = new ServerSocket(3001);
    ServerSocket serverSocket = new ServerSocket(5001);
    ServerInstance instance = new ServerInstance(clientSocket, serverSocket, 5000);
    instance.listen();
  }

  private static void testRun(){
    System.out.println("Enter server number <1 or 2>");
    Scanner sc = new Scanner(System.in);
    int serverNumber = sc.nextInt();
    sc.close();
    if(serverNumber == 1){
      try {
        runServer1();
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
    } else if (serverNumber == 2){
      try {
        runServer2();
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
