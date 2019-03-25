package view;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import model.ClientInstance;

import java.util.HashSet;
import java.util.Hashtable;

public class ChatLayout extends BorderPane {

  private ClientInstance clientInstance;
  private boolean updatingList;
  private boolean freshList;
  private Hashtable<String, ChatPane> chats;
  private VBox userList;
  private BorderPane layout;
  private ChatPane currentChat;


  ChatLayout() {
    layout = new BorderPane();
    chats = new Hashtable<>();
    userList = new VBox();
    //userList.setMinSize(400, 720);
    userList.setMaxSize(400, 720);
    Button updateButton = new Button("UpdateList");
    updateButton.setOnAction(e->updateList());
    VBox vbox = new VBox(updateButton, userList);
    vbox.setMinSize(200, 720);
    ScrollPane scrollPane = new ScrollPane(vbox);
    scrollPane.setMinSize(200, 720);
    Button exitButton = new Button("Logout and Exit");
    exitButton.setOnAction(e->{
      clientInstance.disconnect();
      System.exit(1);
    });
    layout.setLeft(new VBox(scrollPane, exitButton));
    getChildren().add(layout);
  }

  void start(ClientInstance clientInstance){
    this.clientInstance = clientInstance;
    clientInstance.setView(this);
    updateList();
  }

  public void displayMessage(String src, String message){
    Platform.runLater(()->{
      if(currentChat != null){
        currentChat.store();
      }
      if(chats.containsKey(src)){
        currentChat = chats.get(src);
      } else{
        updateList();
        String dest = "";
        for(Node n : userList.getChildren()){
          if(n instanceof UserButton){
            dest = ((UserButton) n).username;
          }
        }
        currentChat = new ChatPane(src, dest);
      }
      layout.setCenter(currentChat);
      currentChat.addMessage(message);
    });
  }

  private void updateList(){
    Thread t = new Thread(()->{
      if(updatingList){
        return;
      }
      updatingList = true;
      clientInstance.getMemberList();
      while(!freshList){
        try {
          Thread.sleep(120);
        } catch (InterruptedException ignored) {}
      }
      freshList = false;
      updatingList = false;
    });
    t.start();
  }

  public void refreshList(String[][] memberList){
    Platform.runLater(()->{
      userList.getChildren().clear();
      HashSet<String> ids = new HashSet<>();
      for(String[] user : memberList){
        userList.getChildren().add(new UserButton(user[0], user[1]));
        ids.add(user[1]);
      }
      for(String id : chats.keySet()){
        if(!ids.contains(id)){
          ids.add(id);
        }else {
          ids.remove(id);
        }
      }
      for(String id : ids){
        chats.remove(id);
      }
      freshList = true;
    });
  }

  // Inner classes
  private class UserButton extends Button{
    String username;
    String id;
    UserButton(String username, String id){
      super(username);
      this.username = username;
      this.id = id;
      setOnAction(e->clicked());
      this.setMinSize(200, 50);
    }

    void clicked(){
      if(currentChat != null){
        currentChat.store();
      }
      if(chats.containsKey(id)){
        currentChat = chats.get(id);
      } else {
        currentChat = new ChatPane(id, username);
      }
      layout.setCenter(currentChat);
    }
  }

  private class ChatPane extends VBox{
    String recipientId;
    TextField textField;
    VBox chat;
    String username;

    ChatPane(String recipientId, String username){
      super();
      this.username = username;
      this.recipientId = recipientId;
      chat = new VBox();
      ScrollPane scrollPane = new ScrollPane(chat);
      scrollPane.setMinSize(1000, 270);
      Label user = new Label(username);
      textField = new TextField();
      Button sendButton = new Button("Send");
      sendButton.setOnAction(e->sendMessage());
      HBox hbox = new HBox(textField, sendButton);
      this.getChildren().addAll(user, scrollPane, hbox);
    }

    void addMessage(String message){
      Label label = new Label(message);
      label.setTextAlignment(TextAlignment.LEFT);
      chat.getChildren().add(label);
    }

    void sendMessage(){
      String message = textField.getText();
      if(message.length() < 1){
        return;
      }
      clientInstance.sendMessage(message, recipientId);
      Label label = new Label(message);
      label.setTextAlignment(TextAlignment.RIGHT);
      chat.getChildren().add(label);
      textField.clear();
    }

    void store(){
      if(!chats.containsKey(recipientId)){
        chats.put(recipientId, this);
      }
    }
  }
}
