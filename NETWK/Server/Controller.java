package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;



/***
 * Main controller for the initial User Interface where the user inputs the server's
 * IP Address(localhost) and port.
 *
 * @author Roxas, Ronell John
 * @author Ang, Charlene
 */

public class Controller {

    Server server;

    @FXML
    Button btnClose;

    @FXML
    TextField tfAddress;

    @FXML
    TextField tfPort;

    /***
     * Closes the current stage/window. This is used when the close button is pressed
     * or when switching scenes to Server window.
     */
    public void closeStage() {
        Stage stage = (Stage) btnClose.getScene().getWindow();

        stage.close();
    }

    /***
     * Sets default values for the text fields.
     * Used when clicked on the setDefault button
     */
    public void setDefaults() {
        tfAddress.setText("localhost");
        tfPort.setText("8000");
    }

    /***
     * Creates the Server stage/window and switches to the scene.
     * This method gets the controller of the Server window (next window) and
     * initializes the contents for that window.
     *
     * @throws IOException On fail to load Parent of next window.
     */
    private void createServerStage() throws IOException {
        FXMLLoader loader = new FXMLLoader();                                   //Open new server stage/scene
        loader.setLocation(getClass().getResource("startServer.fxml"));
        Parent startServer = loader.load();                                     //Load FXML to the next window
        Scene scene = new Scene(startServer);                                   //Assign next window to new scene
        Stage appStage = (Stage) btnClose.getScene().getWindow();               //Get current stage

        //change Server UI texts
        StartServer ssController = loader.getController();                      //Gets the controller for the next window
        ssController.initialize(server);                                        //Initialize values for next window

        //fix views then show scene
        appStage.setScene(scene);
        appStage.centerOnScreen();
        appStage.show();

    }

    /***
     * This creates the server given an IP address and port.
     * This is used when clicking on the connect button
     */
    public void connect() {
        if(checkInput()) {
            try {
                server = new Server(tfAddress.getText(), Integer.parseInt(tfPort.getText()));
                createServerStage();
            } catch (IOException e) {
                Alert errAlert = new Alert(Alert.AlertType.ERROR, "Unable to create Server Window.", ButtonType.OK);
                errAlert.show();

                e.printStackTrace();
            }
        }
    }

    /***
     * Checks if text fields are not empty
     * @return If both text fields are not empty
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

        return pass;
    }
}
