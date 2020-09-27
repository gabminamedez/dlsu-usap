package sample;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.xml.crypto.Data;
import java.io.*;
import java.util.Optional;

/***
 * Main controller for the chat screen window.
 *
 * @author Roxas, Ronell John
 * @author Ang, Charlene
 */
public class chatScreen {

    @FXML
    Button btnSend;

    @FXML
    Button btnFile;

    @FXML
    MenuItem menuQuit;

    @FXML
    MenuItem menuDisconnect;

    @FXML
    Text textTitle;

    @FXML
    Text textPortNum;

    @FXML
    TextFlow tflowChats;

    @FXML
    TextField tfChat;

    @FXML
    Text textTalk;

    @FXML
    ScrollPane spScroll;



    private Client client = null;

    /***
     * Initializes content of the chat window given a client.
     *
     * @param client to get data to update the chat window.
     */
    public void initialize(Client client) {
        this.client = client;
        textTitle.setText(this.client.getUsername());
        textPortNum.setText(this.client.getServerAddress() + ":" + this.client.getServerPort());

        //Listener for scroll
        tflowChats.getChildren().addListener(
                (new ListChangeListener<Node>() {
                    @Override
                    public void onChanged(Change<? extends Node> change) {
                        tflowChats.layout();
                        spScroll.layout();
                        if(tflowChats.getChildren().size() > 12)
                            tflowChats.prefHeightProperty().setValue(tflowChats.getPrefHeight() + 20.9609375);
                        spScroll.setVmax(spScroll.getVmax() + 1);
                    };
                })
        );
        spScroll.vvalueProperty().bind(tflowChats.heightProperty());

        listenUpdate();
    }

    /***
     * Thread that listens to client for User Interface updates
     */
    private void listenUpdate() {
        Runnable listen = new Runnable() {
            @Override
            public void run() {
                while (!client.isDisconnect()) {
                    if(client.getChatEnable()) {
                        tfChat.setDisable(false);
                        btnFile.setDisable(false);
                    }
                    else {
                        tfChat.setDisable(true);
                        btnFile.setDisable(true);
                    }
                    if(client.getUpdate() != null) {
                        Message print = client.getUpdate();
                        if(print.getMessage() != null)
                            appendChat(print.getSource(), print.getMessage());
                        else {
                            appendChat(print.getSource(), "Sent a file [" + print.getFile().getName() + "](" + print.getFile().length() + " bytes)");
                            showConfirmation(print);
                        }
                        client.updated();
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Thread uiListener = new Thread(listen);
        uiListener.setDaemon(true);
        uiListener.start();
    }

    /***
     * Closes chat window and disconnects client from the server.
     */
    public void closeStage() {
        disconnect();
        Stage stage = (Stage) btnSend.getScene().getWindow();
        stage.close();
    }

    /***
     * Switches to a different window.
     *
     * @param fxml UI to switch window to.
     * @throws IOException When Parent fails to load FXML
     */
    private void switchScene(String fxml) throws IOException {
        //Open new server stage/scene
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(fxml));
        Parent chatScreen = loader.load();
        Scene scene = new Scene(chatScreen);
        Stage appStage = (Stage) btnSend.getScene().getWindow();

        //fix views then show scene
        appStage.setScene(scene);
        appStage.centerOnScreen();
        appStage.show();
    }

    /***
     * Closes chat window and disconnects client from the server.
     */
    public void leaveChat(){
        disconnect();
        try {
            switchScene("sample.fxml");
        }
        catch (IOException e) {
            Alert err = new Alert(Alert.AlertType.ERROR, "Error returning to connect window.", ButtonType.OK);
            err.show();
        }
    }

    /***
     * Disconnects client from the server.
     */
    public void disconnect() {
        try {
            client.disconnect();
        }
        catch (IOException e) {
            Alert err = new Alert(Alert.AlertType.ERROR, "Error disconnecting from server.", ButtonType.OK);
            err.show();
        }
    }

    /***
     * Method used to chat from text field to chat text Area (TextFlow).
     * Sends the chat to the server.
     */
    public void chat() {
        if(!tfChat.getText().isBlank()) {
            Message chat = new Message(client.getUsername(), tfChat.getText());
            client.sendChat(chat);
            appendChat(chat.getSource(), chat.getMessage());
            tfChat.setText("");
        }
        tfChat.requestFocus();
    }

    /***
     * Append chat text area (TextFlow)
     * @param user name of user who sent the chat
     * @param message chat message.
     */
    private void appendChat(String user, String message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Text text = new Text(user + ": " + message + "\n");
                if(user.equals("[SERVER]"))
                    text.setFill(Color.GREEN);
                else if(user.equals(client.getUsername()))
                    text.setFill(Color.rgb(56, 56, 56));
                tflowChats.getChildren().add(text);
            }

        });
    }

    /***
     * Used when sending a file.
     * Shows directory window to choose file to send.
     */
    public void sendFile() {
        //Get file to send
        Stage stage =(Stage) tflowChats.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if(file != null) {
            appendChat(client.getUsername(), "Sent file [" + file.getName() + "](" + file.length() + " bytes)");
            client.sendFile(file);
        }
    }

    /***
     * Confirmation dialog box when accepting a file sent by the other client
     * @param print message of the other client which includes a file.
     */
    private void showConfirmation(Message print) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert accept = new Alert(Alert.AlertType.CONFIRMATION, print.getSource() + " sent a file [" + print.getFile().getName() + "](" + print.getFile().length() + " bytes) Accept?", ButtonType.YES, ButtonType.NO);
                accept.showAndWait();

                Message message = null;
                if(accept.getResult() == ButtonType.YES) {
                    message = new Message(client.getUsername(), "Accepted file [" + print.getFile().getName() + "]");
                    Stage stage =(Stage) tflowChats.getScene().getWindow();
                    DirectoryChooser dChooser = new DirectoryChooser();
                    File path = dChooser.showDialog(stage);

                    try {
                        //Filename Dialog
                        TextInputDialog dialog = new TextInputDialog(print.getFile().getName());
                        dialog.setTitle("Save file");
                        dialog.setHeaderText("Save file options");
                        dialog.setContentText("Filename (with file type):");

                        //set filename
                        Optional<String> result = dialog.showAndWait();
                        //if filename not set use current filename
                        String filename = result.isPresent() ? result.get() : print.getFile().getName();

                        FileInputStream fiStream = new FileInputStream(print.getFile());
                        FileOutputStream foStream = new FileOutputStream(path.getPath() + "/" + filename);
                        foStream.write(fiStream.readAllBytes());

                        fiStream.close();
                        foStream.close();
                        Alert pass = new Alert(Alert.AlertType.INFORMATION, "File " + filename + "(" + print.getFile().length() + " bytes) saved at " + path + ".");
                        pass.show();
                    } catch (IOException e) {
                        Alert err = new Alert(Alert.AlertType.ERROR, "Error downloading file. Try again.", ButtonType.OK);
                        err.show();
                    }


                }
                if(accept.getResult() == ButtonType.NO)
                    message = new Message(client.getUsername(), "Declined file [" + print.getFile().getName() + "]");

                message.setFile(print.getFile());
                client.sendChat(message);
                appendChat(message.getSource(), message.getMessage());
            }
        });
    }
}
