package model;

import view.ChatLayout;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientInstance extends Thread {

  private static boolean TESTING = false;

  private static final int TICKS = 120;
  private String username;
  private String host;
  private int port;
  private Socket connSocket;
  private ObjectOutputStream outToServer;
  private ObjectInputStream inFromServer;

  private ChatLayout view;

  public ClientInstance(String host, int port, String username) {
    this.host = host;
    this.port = port;
    this.username = username;
  }

  public boolean connect() {
    try {
      connSocket = new Socket(host, port);
      inFromServer = new ObjectInputStream(connSocket.getInputStream());
      outToServer = new ObjectOutputStream(connSocket.getOutputStream());
      outToServer.writeObject(username);
      outToServer.flush();
      return true;
    } catch (IOException ignored) {
      closeStreams();
      return false;
    }
  }

  @Override
  public void run() {
    Thread serverReader = new Thread(this::readFromServer);
    serverReader.start();
    try {
      serverReader.join();
    } catch (InterruptedException ignored) {
    }
    closeStreams();
  }

  private void readFromServer() {
    while (true) {
      try {
        sleep(TICKS);
      } catch (InterruptedException ignored) {
      }
      try {
        Object[] message = (Object[]) inFromServer.readObject();
        parseMessage(message);
      } catch (IOException | ClassNotFoundException ignored) {
      }
    }
  }

  private void parseMessage(Object[] inFromServer) {
    String messageType = (String) inFromServer[0];
    switch (messageType) {
      case ("chat"):
        handleChat(inFromServer);
        break;
      case ("list"):
        refreshList(inFromServer);
        break;
      default:
        break;
    }
  }

  private void refreshList(Object[] inFromServer) {
    String[][] users = (String[][]) inFromServer[1];
    if (TESTING) {
      for (String[] user : users) {
        System.out.println(user[0] + " " + user[1]);
      }
    } else {
      view.refreshList(users);
    }
  }

  private void handleChat(Object[] inFromServer) {
    String src = (String) inFromServer[1];
    String message = (String) inFromServer[2];
    if (TESTING) {
      System.out.println(src + ": " + message);
    } else {
      view.displayMessage(src, message);
    }
  }

  public void sendMessage(String message, String dest) {
    Object[] request = {"chat", dest, message, 0};
    try {
      outToServer.writeObject(request);
      outToServer.flush();
    } catch (IOException ignored) {
    }
  }

  public void getMemberList() {
    Object[] request = {"list"};
    try {
      this.outToServer.writeObject(request);
      outToServer.flush();
    } catch (IOException ignored) {
    }
  }

  private void closeStreams() {
    if (inFromServer != null) {
      try {
        inFromServer.close();
      } catch (IOException ignored) {
      }
    }
    if (outToServer != null) {
      try {
        outToServer.close();
      } catch (IOException ignored) {
      }
    }
    if (connSocket != null) {
      try {
        connSocket.close();
      } catch (IOException ignored) {
      }
    }
  }

  public void disconnect(){
    try {
      outToServer.writeObject(new String[] {"quit"});
      outToServer.flush();
      outToServer.close();
    } catch (IOException ignored) {
    }
    closeStreams();
  }

  public void setView(ChatLayout view) {
    this.view = view;
  }

  // Testing method(s)
  public static void main(String[] args) {
    test();
  }

  private static void test() {
    TESTING = true;
    Scanner sc = new Scanner(System.in);
    System.out.println("Please enter the port number of the server");
    int port = sc.nextInt();
    System.out.println("Please enter your name");
    String name = sc.next();
    ClientInstance testInstance = new ClientInstance("localhost", port, name);
    if (!testInstance.connect()) {
      System.out.println("Could not connect");
      return;
    } else {
      System.out.println("Connected successfully!");
    }
    testInstance.start();
    while (true) {
      System.out.println("Enter function number\n1- chat\n2- list\n3- close");
      int func = sc.nextInt();
      switch (func) {
        case (1):
          chat_test(sc, testInstance);
          break;
        case (2):
          list_test(testInstance);
          break;
        case (3):
          System.exit(1);
      }
    }
  }

  private static void chat_test(Scanner sc, ClientInstance instance) {
    System.out.println("Enter recipient id.");
    String dest = sc.next();
    System.out.println("Enter message");
    String message = sc.next();
    instance.sendMessage(message, dest);
  }

  private static void list_test(ClientInstance instance) {
    instance.getMemberList();
  }
}
