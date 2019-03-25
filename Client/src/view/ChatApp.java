package view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.ClientInstance;

public class ChatApp extends Application {

  private Scene chatScene;
  private Stage window;
  private ClientInstance clientInstance;

  @Override
  public void start(Stage primaryStage) {
    clientInstance = null;
    this.window = primaryStage;
    Scene loginScene = new Scene(new LoginLayout(this), 600, 400);
    chatScene = new Scene(new ChatLayout(), 1280, 720);
    window.setScene(loginScene);
    window.setOnCloseRequest((e)->{
      if(clientInstance != null){
        clientInstance.disconnect();
      }
      System.exit(1);
    });
    window.show();
  }

  void switchToChat(ClientInstance clientInstance){
    this.clientInstance = clientInstance;
    ((ChatLayout)chatScene.getRoot()).start(clientInstance);
    window.setScene(chatScene);
  }


  public static void main(String[] args){
    launch(args);
  }
}