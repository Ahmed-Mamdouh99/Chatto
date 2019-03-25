package view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.ClientInstance;

class LoginLayout extends StackPane {
  private ChatApp app;
  private TextField usernameField;
  private TextField portField;
  private Label statusLabel;

  LoginLayout(ChatApp app){
    super();
    setUpView();
    this.app = app;
  }

  private void setUpView(){
    VBox vbox = new VBox();
    usernameField = new TextField();
    usernameField.setMaxWidth(400);
    Label usernameLabel = new Label("Username", usernameField);
    portField = new TextField();
    portField.setMaxWidth(100);
    Label portLabel = new Label("Port number", portField);
    Button loginButton = new Button("Login");
    loginButton.setOnAction(e->login());
    statusLabel = new Label();
    vbox.getChildren().addAll(usernameLabel, usernameField, portLabel, portField, loginButton, statusLabel);
    getChildren().add(vbox);
  }

  private void login(){
    String username = usernameField.getText();
    if(username.length() < 3){
      statusLabel.setText("Your username has to be at least 3 characters long");
      return;
    }
    try{
      int portNumber = Integer.parseInt(portField.getText());
      ClientInstance newInstance = new ClientInstance("localhost", portNumber, username);
      if(!newInstance.connect()){
        statusLabel.setText("Could not connect\nPlease enter a valid port number <3000 or 3001>");
        return;
      }
      newInstance.start();
      statusLabel.setText("");
      statusLabel.setVisible(false);
      app.switchToChat(newInstance);
    } catch (Exception ignored){
      statusLabel.setText("Please enter a valid port number <3000 or 3001>");
    }
  }
}
