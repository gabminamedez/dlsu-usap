package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.w3c.dom.Text;

import java.awt.event.ActionEvent;
import java.io.IOException;



/***
 *  Main controller for the initial window asking for the address and port.
 *
 * @author Roxas, Ronell John
 * @author Ang, Charlene
 */
public class Controller {

    @FXML
    Button btnClose;

    @FXML
    TextField tfAddress;

    @FXML
    TextField tfPort;

    @FXML
    TextField tfUsername;

    private Client client = null;

    /***
     * Closes the window
     */
    public void closeStage() {
        Stage stage = (Stage) btnClose.getScene().getWindow();

        stage.close();
    }

    /***
     * Sets default values for ip address and port.
     * Used when clicked on the setDefault button.
     */
    public void setDefaults() {
        tfAddress.setText("localhost");
        tfPort.setText("8000");
    }

    /***
     * Switches scene of the window. Initializes content of the chat window.
     *
     * @param fxml scene to change to (UI)
     * @throws IOException When Parent fails to load FXML
     */
    private void switchScene(String fxml) throws IOException {
        //Open new server stage/scene
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(fxml));
        Parent chatScreen = loader.load();
        Scene scene = new Scene(chatScreen);
        Stage appStage = (Stage) btnClose.getScene().getWindow();

        //send client object to next window
        chatScreen csController = loader.getController();
        csController.initialize(client);

        //fix views then show scene
        appStage.setScene(scene);
        appStage.centerOnScreen();
        appStage.show();
    }

    /***
     * Connects to the server given the ip address and port.
     */
    public void connect() {
        Alert message = new Alert(Alert.AlertType.NONE, "Connecting to Server please wait...", ButtonType.CANCEL);
        if(checkInput())
            try {
                client = new Client(tfAddress.getText(), Integer.parseInt(tfPort.getText()), tfUsername.getText());
                message.show();
                client.joinServer();
                switchScene("ChatScreen.fxml");
            }
            catch (IOException e) {
                Alert errAlert = new Alert(Alert.AlertType.ERROR, "Error connecting to server.", ButtonType.OK);
                errAlert.show();
            }
            finally {
                message.close();
            }
    }

    /***
     * Check if the text fields are not empty.
     * @return boolean value if text fields are not empty
     */
    public boolean checkInput() {
        boolean pass = true;
        if(tfAddress.getText().isBlank()) {
            tfAddress.setStyle("-fx-text-box-border: #B22222; -fx-focus-color: #B22222;");
            pass = false;
        }
        else
            tfAddress.setStyle("");
        if(tfPort.getText().isBlank()) {
            tfPort.setStyle("-fx-text-box-border: #B22222; -fx-focus-color: #B22222;");
            pass = false;
        }
        else
            try {
                Integer.parseInt(tfPort.getText());
                tfPort.setStyle("");
            }
            catch (NumberFormatException e) {
                pass = false;
            }

        if(tfUsername.getText().isBlank()) {
            tfUsername.setStyle("-fx-text-box-border: #B22222; -fx-focus-color: #B22222;");
            pass = false;
        }
        else
            tfUsername.setStyle("");


        return pass;
    }
}
