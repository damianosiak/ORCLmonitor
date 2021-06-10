package com.damianosiak;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.InetAddress;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * ORCLmonitor
 *
 * @author : Damian Osiak
 * Student number: 40843
 * AWSB Cieszyn
 * 04.2021
 *
 * Main ORCLmonitor application class
 * This class include: print app GUI and auxiliary methods
 * */
public class ORCLmonitor extends Application {
    Stage stageWidth = null;
    Connections connections = new Connections();
    private final String appName = "ORCLmonitor";
    private final String appVersion = "v0.0.1";
    private final String appCopyrightYear= String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
    private final String appAuthor = "Damian Osiak";
    private final String appAuthorEmail="damianosiak34@gmail.com";
    private final String appAuthorGithub="github.com/damianosiak";
    private final String appAuthorLinekdin="linkedin.com/in/damianosiak";
    String defaultPathToFile = System.getenv("APPDATA").replace("\\", "\\\\")+"\\\\ORCLmonitor\\\\ORCLmonitor.json";
    String currentPathToFile=defaultPathToFile.replace("\\\\", "\\");
    Boolean fileHasUnsavedChanges=false;
    String backgroundColorLight="#efeeef";String backgroundColorDark="#242424";
    Integer connectionTimeoutSeconds = 5;//default 5 seconds

    /**This method is responsible for application GUI*/
    @Override
    public void start(final Stage primaryStage){
        primaryStage.setMaximized(true);
        primaryStage.setTitle(appName + " | "+currentPathToFile);
        stageWidth=primaryStage;

        Group root = new Group();
        Scene scene = new Scene(root, 1600, 700, Paint.valueOf(backgroundColorLight));

        MenuBar menuBar = new MenuBar();
        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());

        Group mainBody = new Group();
        mainBody.setLayoutY(25);

        GridPane gridBody = new GridPane();
        gridBody.prefWidthProperty().bind(primaryStage.widthProperty());


        Menu menuFile = new Menu("File");

        MenuItem menuFileSave = new MenuItem("Save");
        menuFileSave.setMnemonicParsing(true);
        menuFileSave.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event){
                if(connections.exportConnectionListToFileAs(currentPathToFile)){
                    fileHasUnsavedChanges=false;
                    Alert fileSavedAlert = new Alert(Alert.AlertType.INFORMATION);
                    fileSavedAlert.setTitle("ORCLmonitor settings has been saved successfully");
                    fileSavedAlert.setHeaderText(null);
                    fileSavedAlert.setContentText("ORCLmonitor settings has been saved successfully");
                    fileSavedAlert.show();
                }else{
                    Alert fileSavedAlert = new Alert(Alert.AlertType.ERROR);
                    fileSavedAlert.setTitle("ORCLmonitor settings has not been saved");
                    fileSavedAlert.setHeaderText(null);
                    fileSavedAlert.setContentText("Error.\nORCLmonitor settings has not been saved!");
                    fileSavedAlert.show();
                }
            }
        });

        MenuItem menuFileSaveAs = new MenuItem("Save as..");
        menuFileSaveAs.setMnemonicParsing(true);
        menuFileSaveAs.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event){
                try{
                    FileChooser fileChooser = new FileChooser();
                    FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("JSON File (*.json)", "*.json");
                    fileChooser.getExtensionFilters().add(extensionFilter);
                    File file = fileChooser.showSaveDialog(primaryStage);
                    if(file!=null){
                        if(connections.exportConnectionListToFileAs(file.getPath())){
                            fileHasUnsavedChanges=false;
                            Alert fileSavedAlert = new Alert(Alert.AlertType.INFORMATION);
                            fileSavedAlert.setTitle("ORCLmonitor settings has been saved successfully");
                            fileSavedAlert.setHeaderText(null);
                            fileSavedAlert.setContentText("ORCLmonitor settings has been saved successfully");
                            fileSavedAlert.show();
                            currentPathToFile=file.getPath();
                            primaryStage.setTitle(appName +" | "+ currentPathToFile);
                        }else{
                            Alert fileSavedAlert = new Alert(Alert.AlertType.ERROR);
                            fileSavedAlert.setTitle("ORCLmonitor settings has not been saved");
                            fileSavedAlert.setHeaderText(null);
                            fileSavedAlert.setContentText("Error.\nORCLmonitor settings has not been saved!");
                            fileSavedAlert.show();
                        }
                    }
                }catch(Exception e){
                    Alert fileSavedAlert = new Alert(Alert.AlertType.ERROR);
                    fileSavedAlert.setTitle("ORCLmonitor settings has not been saved");
                    fileSavedAlert.setHeaderText(null);
                    fileSavedAlert.setContentText("Error.\nORCLmonitor settings has not been saved!");
                    fileSavedAlert.show();
                }
            }
        });

        MenuItem menuOpenFile = new MenuItem("Open..");
        menuOpenFile.setMnemonicParsing(true);
        menuOpenFile.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event){
                try{
                    FileChooser fileChooser = new FileChooser();
                    FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("JSON File (*.json)", "*.json");
                    fileChooser.getExtensionFilters().add(extensionFilter);
                    File file = fileChooser.showOpenDialog(primaryStage);
                    if(file!=null) {
                        if (connections.importConnectionListFromSpecificLocation(file.getPath())) {
                            fileHasUnsavedChanges=false;
                            mainBody.getChildren().clear();mainBody.getChildren().addAll(showCharts());
                            primaryStage.setTitle(appName+" | "+file.getName());
                            Alert fileOpenedAlert = new Alert(Alert.AlertType.INFORMATION);
                            fileOpenedAlert.setTitle("ORCLmonitor settings has been opened successfully");
                            fileOpenedAlert.setHeaderText(null);
                            fileOpenedAlert.setContentText("ORCLmonitor settings has been opened successfully");
                            fileOpenedAlert.show();
                        } else {
                            Alert fileOpenedAlert = new Alert(Alert.AlertType.ERROR);
                            fileOpenedAlert.setTitle("ORCLmonitor settings has not been saved");
                            fileOpenedAlert.setHeaderText(null);
                            fileOpenedAlert.setContentText("Error.\nORCLmonitor settings has not been opened!");
                            fileOpenedAlert.show();
                        }
                    }
                }catch(Exception e){
                    Alert fileOpenedAlert = new Alert(Alert.AlertType.ERROR);
                    fileOpenedAlert.setTitle("ORCLmonitor settings has not been opened");
                    fileOpenedAlert.setHeaderText(null);
                    fileOpenedAlert.setContentText("Error.\nORCLmonitor settings has not been opened!");
                    fileOpenedAlert.show();
                }
            }
        });

        MenuItem menuFileExit = new MenuItem("Exit");
        menuFileExit.setMnemonicParsing(true);
        menuFileExit.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event){
                Platform.exit();
            }
        });

        menuFile.getItems().addAll(menuFileSave, menuFileSaveAs, menuOpenFile, menuFileExit);
        menuBar.getMenus().add(menuFile);


        Menu menuConnections = new Menu("Connections");

        MenuItem menuConnectionsAddConnection = new MenuItem("Add connection");
        menuConnectionsAddConnection.setMnemonicParsing(true);
        menuConnectionsAddConnection.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                Dialog addConnectionDialog = new Dialog();
                addConnectionDialog.setTitle("Add connection");
                TextField connectionName = new TextField(); connectionName.setPromptText("Connection name");
                TextField connectionAddress = new TextField(); connectionAddress.setPromptText("Connection address");
                TextField connectionPort = new TextField(); connectionPort.setPromptText("Connection port");
                TextField connectionServiceNameOrSID = new TextField(); connectionServiceNameOrSID.setPromptText("Service name or SID");
                TextField connectionUser = new TextField(); connectionUser.setPromptText("User");
                PasswordField connectionPassword = new PasswordField(); connectionPassword.setPromptText("Password");
                TextField connectionRefreshRate = new TextField(); connectionRefreshRate.setPromptText("Refresh rate (seconds)");

                GridPane grid = new GridPane();
                grid.add(new Label("Connection Name: "), 0, 0); grid.add(connectionName, 1, 0);
                grid.add(new Label("Connection Address: "), 0, 1); grid.add(connectionAddress, 1, 1);
                grid.add(new Label("Connection Port: "), 0, 2); grid.add(connectionPort, 1, 2);
                grid.add(new Label("Connection Service Name or SID: "), 0, 3); grid.add(connectionServiceNameOrSID, 1, 3);
                grid.add(new Label("Connection User: "), 0, 4); grid.add(connectionUser, 1, 4);
                grid.add(new Label("Connection Password: "), 0, 5); grid.add(connectionPassword, 1, 5);
                grid.add(new Label("Connection Refresh Rate: "), 0, 6); grid.add(connectionRefreshRate, 1, 6);
                Button testConnectionButton = new Button("Test connection"); Label testConnectionButtonResult = new Label();
                testConnectionButton.setOnAction(new EventHandler<ActionEvent>(){
                    public void handle(ActionEvent event){
                        if(pingTestToServer(connectionAddress.getText())){
                            try {
                                if(connectedToDatabaseTest(connectionAddress.getText(), connectionPort.getText(), connectionServiceNameOrSID.getText(), connectionUser.getText(), connectionPassword.getText())){
                                    testConnectionButtonResult.setText("Connected");
                                    testConnectionButtonResult.setStyle("-fx-text-fill: GREEN; -fx-font-weight: bold;");
                                }else{
                                    testConnectionButtonResult.setText("Connection failed");
                                    testConnectionButtonResult.setStyle("-fx-text-fill: RED; -fx-font-weight: bold;");
                                }
                            } catch (SQLException exception) {
                                testConnectionButtonResult.setText("Connection failed (click)");
                                testConnectionButtonResult.setStyle("-fx-text-fill: RED; -fx-font-weight: bold;");
                                testConnectionButtonResult.setOnMouseClicked(new EventHandler<MouseEvent>(){
                                    public void handle(MouseEvent event){
                                        Alert connectionFailedDetailsDialog = new Alert(Alert.AlertType.ERROR);
                                        connectionFailedDetailsDialog.setTitle("Connection failed details");
                                        connectionFailedDetailsDialog.setContentText(String.valueOf(exception));
                                        connectionFailedDetailsDialog.showAndWait();
                                    }
                                });
                            }
                        }else{
                            testConnectionButtonResult.setText("Connection failed (click)");
                            testConnectionButtonResult.setStyle("-fx-text-fill: RED; -fx-font-weight: bold;");
                            testConnectionButtonResult.setOnMouseClicked(new EventHandler<MouseEvent>(){
                                public void handle(MouseEvent event){
                                    Alert connectionFailedDetailsDialog = new Alert(Alert.AlertType.ERROR);
                                    connectionFailedDetailsDialog.setTitle("Connection failed details");
                                    connectionFailedDetailsDialog.setContentText("Server "+connectionAddress.getText()+"\nis not reachable\n\n"+"Connection timed out ("+connectionTimeoutSeconds+" seconds ping limit)");
                                    connectionFailedDetailsDialog.showAndWait();
                                }
                            });
                        }

                    }
                });
                grid.add(testConnectionButton, 0, 7); grid.add(testConnectionButtonResult, 1, 7);
                addConnectionDialog.getDialogPane().setContent(grid);

                ButtonType buttonTypeOk = new ButtonType("Add connection", ButtonBar.ButtonData.OK_DONE);
                addConnectionDialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
                Button addConnectionButton = (Button) addConnectionDialog.getDialogPane().lookupButton(buttonTypeOk);
                addConnectionButton.setOnAction(new EventHandler<ActionEvent>(){
                    public void handle(ActionEvent event){
                        Connection connection = new Connection(connectionName.getText(), connectionAddress.getText(), connectionPort.getText(), connectionServiceNameOrSID.getText(), connectionUser.getText(), connectionPassword.getText(), connectionRefreshRate.getText());
                        if(connections.addConnectionToConnectionList(connection)){
                            fileHasUnsavedChanges=true;
                            mainBody.getChildren().clear();mainBody.getChildren().addAll(showCharts());
                            Alert addConnectionSuccessDialog = new Alert(Alert.AlertType.INFORMATION);
                            addConnectionSuccessDialog.setTitle("Connection added successfully");
                            addConnectionSuccessDialog.setHeaderText(null);
                            addConnectionSuccessDialog.setContentText("Connection \""+connection.getConnectionName()+"\" has been added successfully!");
                            addConnectionSuccessDialog.show();
                        }else{
                            Alert addConnectionErrorDialog = new Alert(Alert.AlertType.ERROR);
                            addConnectionErrorDialog.setTitle("Error. Connection has not been added");
                            addConnectionErrorDialog.setHeaderText(null);
                            addConnectionErrorDialog.setContentText("Error. Connection \""+connection.getConnectionName()+"\" has not been added!");
                            addConnectionErrorDialog.show();
                        }
                    }
                });

                BooleanBinding addConnectionPanelValidationIsInvalid = Bindings.createBooleanBinding(() -> addConnectionPanelValidationIsInvalid(connectionName.getText(), connectionAddress.getText(), connectionPort.getText(), connectionServiceNameOrSID.getText(), connectionUser.getText(), connectionRefreshRate.getText()), connectionName.textProperty(), connectionAddress.textProperty(), connectionPort.textProperty(), connectionServiceNameOrSID.textProperty(), connectionUser.textProperty(), connectionRefreshRate.textProperty());

                testConnectionButton.disableProperty().bind(addConnectionPanelValidationIsInvalid);
                addConnectionButton.disableProperty().bind(addConnectionPanelValidationIsInvalid);

                addConnectionDialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
                addConnectionDialog.showAndWait();
            }
        });


        MenuItem menuConnectionsConnectionList = new MenuItem("Connection list");
        menuConnectionsConnectionList.setMnemonicParsing(true);
        menuConnectionsConnectionList.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                Dialog connectionListDialog = new Dialog();
                connectionListDialog.setTitle("Connection list");
                GridPane grid = new GridPane(); grid.setVgap(10); grid.setHgap(10);
                ComboBox comboBoxConnectionsList = new ComboBox();
                comboBoxConnectionsList.setPromptText("Select connection");
                for(Connection actualConnectionFromConnectionList : connections.getConnectionList()){
                    comboBoxConnectionsList.getItems().add(actualConnectionFromConnectionList.getConnectionName());
                }
                HBox hboxConnectionList = new HBox(comboBoxConnectionsList);
                grid.add(hboxConnectionList, 0, 0);

                BooleanBinding selectConnectionValidation = Bindings.createBooleanBinding(() -> connectionInComboBoxIsNotSelected(comboBoxConnectionsList));

                if(selectConnectionValidation.get()){
                    comboBoxConnectionsList.setOnAction((comboBoxConnectionsListAction) -> {
                        connectionListDialog.setWidth(375);connectionListDialog.setHeight(450);
                        String selectedConnectionNameFromConnectionList = String.valueOf(comboBoxConnectionsList.getValue());

                        HBox hboxButtons = new HBox();
                        Button editConnectionButton = new Button("Save");
                        Button deleteConnectionButton = new Button("Delete");
                        hboxButtons.getChildren().addAll(editConnectionButton, deleteConnectionButton);
                        grid.add(hboxButtons, 1, 0);

                        TextField connectionName = new TextField(); connectionName.setPromptText("Connection name");
                        TextField connectionAddress = new TextField(); connectionAddress.setPromptText("Connection address");
                        TextField connectionPort = new TextField(); connectionPort.setPromptText("Connection port");
                        TextField connectionServiceNameOrSID = new TextField(); connectionServiceNameOrSID.setPromptText("Service name or SID");
                        TextField connectionUser = new TextField(); connectionUser.setPromptText("User");
                        PasswordField connectionPassword = new PasswordField(); connectionPassword.setPromptText("Password");
                        TextField connectionRefreshRate = new TextField(); connectionRefreshRate.setPromptText("Refresh rate (seconds)");

                        connectionName.setText(connections.getConnectionFromConnectionListByConnectionName(selectedConnectionNameFromConnectionList).getConnectionName());
                        connectionAddress.setText(connections.getConnectionFromConnectionListByConnectionName(selectedConnectionNameFromConnectionList).getConnectionAddress());
                        connectionPort.setText(connections.getConnectionFromConnectionListByConnectionName(selectedConnectionNameFromConnectionList).getConnectionPort());
                        connectionServiceNameOrSID.setText(connections.getConnectionFromConnectionListByConnectionName(selectedConnectionNameFromConnectionList).getConnectionServiceNameOrSID());
                        connectionUser.setText(connections.getConnectionFromConnectionListByConnectionName(selectedConnectionNameFromConnectionList).getConnectionUser());
                        connectionPassword.setText(connections.getConnectionFromConnectionListByConnectionName(selectedConnectionNameFromConnectionList).getConnectionPassword());
                        connectionRefreshRate.setText(connections.getConnectionFromConnectionListByConnectionName(selectedConnectionNameFromConnectionList).getConnectionRefreshRate());

                        grid.add(new Label("Connection Name: "), 0, 2); grid.add(connectionName, 1, 2);
                        grid.add(new Label("Connection Address: "), 0, 3); grid.add(connectionAddress, 1, 3);
                        grid.add(new Label("Connection Port: "), 0, 4); grid.add(connectionPort, 1, 4);
                        grid.add(new Label("Connection Service Name or SID: "), 0, 5); grid.add(connectionServiceNameOrSID, 1, 5);
                        grid.add(new Label("Connection User: "), 0, 6); grid.add(connectionUser, 1, 6);
                        grid.add(new Label("Connection Password: "), 0, 7); grid.add(connectionPassword, 1, 7);
                        grid.add(new Label("Connection Refresh Rate: "), 0, 8); grid.add(connectionRefreshRate, 1, 8);
                        Button testConnectionButton = new Button("Test connection"); Label testConnectionButtonResult = new Label();
                        testConnectionButton.setOnAction(new EventHandler<ActionEvent>(){
                            public void handle(ActionEvent event){
                                if(pingTestToServer(connectionAddress.getText())){
                                    try {
                                        if(connectedToDatabaseTest(connectionAddress.getText(), connectionPort.getText(), connectionServiceNameOrSID.getText(), connectionUser.getText(), connectionPassword.getText())){
                                            testConnectionButtonResult.setText("Connected");
                                            testConnectionButtonResult.setStyle("-fx-text-fill: GREEN; -fx-font-weight: bold;");
                                        }else{
                                            testConnectionButtonResult.setText("Connection failed");
                                            testConnectionButtonResult.setStyle("-fx-text-fill: RED; -fx-font-weight: bold;");
                                        }
                                    } catch (SQLException exception) {
                                        testConnectionButtonResult.setText("Connection failed (click)");
                                        testConnectionButtonResult.setStyle("-fx-text-fill: RED; -fx-font-weight: bold;");
                                        testConnectionButtonResult.setOnMouseClicked(new EventHandler<MouseEvent>(){
                                            public void handle(MouseEvent event){
                                                Alert connectionFailedDetailsDialog = new Alert(Alert.AlertType.ERROR);
                                                connectionFailedDetailsDialog.setTitle("Connection failed details");
                                                connectionFailedDetailsDialog.setContentText(String.valueOf(exception));
                                                connectionFailedDetailsDialog.showAndWait();
                                            }
                                        });
                                    }
                                }else{
                                    testConnectionButtonResult.setText("Connection failed (click)");
                                    testConnectionButtonResult.setStyle("-fx-text-fill: RED; -fx-font-weight: bold;");
                                    testConnectionButtonResult.setOnMouseClicked(new EventHandler<MouseEvent>(){
                                        public void handle(MouseEvent event){
                                            Alert connectionFailedDetailsDialog = new Alert(Alert.AlertType.ERROR);
                                            connectionFailedDetailsDialog.setTitle("Connection failed details");
                                            connectionFailedDetailsDialog.setContentText(String.valueOf("Server "+connectionAddress.getText()+"\nis not reachable\n\n"+"Connection timed out ("+connectionTimeoutSeconds+" seconds ping limit)"));
                                            connectionFailedDetailsDialog.showAndWait();
                                        }
                                    });
                                }

                            }
                        });
                        grid.add(testConnectionButton, 0, 9); grid.add(testConnectionButtonResult, 1, 9);

                        editConnectionButton.setOnAction(new EventHandler<ActionEvent>(){
                            public void handle(ActionEvent event){
                                Alert editConnectionButtonAlert = new Alert(Alert.AlertType.CONFIRMATION);
                                editConnectionButtonAlert.setTitle("Edit connection confirmation");
                                editConnectionButtonAlert.setHeaderText(null);
                                editConnectionButtonAlert.setContentText("Are you sure?");
                                editConnectionButtonAlert.getDialogPane().setMaxSize(2, 2);
                                editConnectionButtonAlert.showAndWait().ifPresent(response -> {
                                    if(response == ButtonType.OK){
                                        if(connections.editConnectionFromConnectionListByConnectionName(selectedConnectionNameFromConnectionList, connectionName.getText(), connectionAddress.getText(), connectionPort.getText(), connectionServiceNameOrSID.getText(), connectionUser.getText(), connectionPassword.getText(), connectionRefreshRate.getText())) {
                                            fileHasUnsavedChanges=true;
                                            mainBody.getChildren().clear();mainBody.getChildren().addAll(showCharts());
                                            connectionListDialog.close();
                                            Alert alertConnectionHasBeenEdited = new Alert(Alert.AlertType.INFORMATION);
                                            alertConnectionHasBeenEdited.setTitle("Connection has been edited");
                                            alertConnectionHasBeenEdited.setHeaderText(null);
                                            alertConnectionHasBeenEdited.setContentText("Connection \""+connectionName.getText()+"\" has been edited successfully!");
                                            menuConnectionsConnectionList.fire();
                                            alertConnectionHasBeenEdited.show();
                                        }else{
                                            connectionListDialog.close();
                                            Alert alertErrorConnectionHasNotBeenEdited = new Alert(Alert.AlertType.ERROR);
                                            alertErrorConnectionHasNotBeenEdited.setTitle("Error. Connection has not been edited");
                                            alertErrorConnectionHasNotBeenEdited.setHeaderText(null);
                                            alertErrorConnectionHasNotBeenEdited.setContentText("Connection \""+connectionName.getText()+"\" has not been edited!");
                                            menuConnectionsConnectionList.fire();
                                            alertErrorConnectionHasNotBeenEdited.show();
                                        }
                                    }else if(response == ButtonType.CANCEL){
                                    }
                                });
                            }
                        });

                        deleteConnectionButton.setOnAction(new EventHandler<ActionEvent>(){
                            public void handle(ActionEvent event){
                                Alert deleteConnectionButtonAlert = new Alert(Alert.AlertType.CONFIRMATION);
                                deleteConnectionButtonAlert.setTitle("Delete connection confirmation");
                                deleteConnectionButtonAlert.setHeaderText(null);
                                deleteConnectionButtonAlert.setContentText("Are you sure?");
                                deleteConnectionButtonAlert.getDialogPane().setMaxSize(2, 2);
                                deleteConnectionButtonAlert.showAndWait().ifPresent(response -> {
                                    if(response == ButtonType.OK){
                                        if(connections.deleteConnectionFromConnectionListByConnectionName(selectedConnectionNameFromConnectionList)) {
                                            fileHasUnsavedChanges=true;
                                            mainBody.getChildren().clear();mainBody.getChildren().addAll(showCharts());
                                            connectionListDialog.close();
                                            Alert alertConnectionHasBeenDeleted = new Alert(Alert.AlertType.INFORMATION);
                                            alertConnectionHasBeenDeleted.setTitle("Connection has been deleted");
                                            alertConnectionHasBeenDeleted.setHeaderText(null);
                                            alertConnectionHasBeenDeleted.setContentText("Connection \""+connectionName.getText()+"\" has been deleted successfully!");
                                            menuConnectionsConnectionList.fire();
                                            alertConnectionHasBeenDeleted.show();
                                        }else{
                                            connectionListDialog.close();
                                            Alert alertErrorConnectionHasNotBeenDeleted = new Alert(Alert.AlertType.ERROR);
                                            alertErrorConnectionHasNotBeenDeleted.setTitle("Error. Connection has not been deleted");
                                            alertErrorConnectionHasNotBeenDeleted.setHeaderText(null);
                                            alertErrorConnectionHasNotBeenDeleted.setContentText("Connection \""+connectionName.getText()+"\" has not been deleted!");
                                            menuConnectionsConnectionList.fire();
                                            alertErrorConnectionHasNotBeenDeleted.show();
                                        }
                                    }else if(response == ButtonType.CANCEL){
                                    }
                                });
                            }
                        });
                        BooleanBinding editConnectionPanelValidationIsInvalid = Bindings.createBooleanBinding(() -> addConnectionPanelValidationIsInvalid(connectionName.getText(), connectionAddress.getText(), connectionPort.getText(), connectionServiceNameOrSID.getText(), connectionUser.getText(), connectionRefreshRate.getText()), connectionName.textProperty(), connectionAddress.textProperty(), connectionPort.textProperty(), connectionServiceNameOrSID.textProperty(), connectionUser.textProperty(), connectionRefreshRate.textProperty());
                        testConnectionButton.disableProperty().bind(editConnectionPanelValidationIsInvalid);
                        editConnectionButton.disableProperty().bind(editConnectionPanelValidationIsInvalid);
                    });
                }

                connectionListDialog.getDialogPane().setContent(grid);
                connectionListDialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
                connectionListDialog.show();
            }
        });

        mainBody.getChildren().add(gridBody);

        MenuItem menuConnectionsRefresh = new MenuItem("Refresh");
        menuConnectionsRefresh.setMnemonicParsing(true);
        menuConnectionsRefresh.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event){
                mainBody.getChildren().clear();mainBody.getChildren().addAll(showCharts());
            }
        });


        menuConnections.getItems().addAll(menuConnectionsAddConnection, menuConnectionsConnectionList, menuConnectionsRefresh);
        menuBar.getMenus().add(menuConnections);


        Menu menuTools = new Menu("Tools");

        MenuItem menuToolsBackgroundLight = new MenuItem("Background light");
        menuToolsBackgroundLight.setMnemonicParsing(true);
        menuToolsBackgroundLight.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                scene.setFill(Paint.valueOf(backgroundColorLight));
                menuBar.setStyle("-fx-background-color: #ebebeb");
            }
        });

        MenuItem menuToolsBackgroundDark = new MenuItem("Background dark");
        menuToolsBackgroundDark.setMnemonicParsing(true);
        menuToolsBackgroundDark.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                scene.setFill(Paint.valueOf(backgroundColorDark));
                menuBar.setStyle("-fx-background-color: #616060");
            }
        });

        MenuItem menuToolsSetTimeoutLimit = new MenuItem("Set Timeout Limit");
        menuToolsSetTimeoutLimit.setMnemonicParsing(true);
        menuToolsSetTimeoutLimit.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event){
                Alert setTimeoutLimitAlert = new Alert(Alert.AlertType.INFORMATION);
                setTimeoutLimitAlert.setTitle("Set Timeout Limit");
                setTimeoutLimitAlert.setHeaderText(null);
                GridPane setTimeoutLimitAlertGridPane = new GridPane();
                Label setTimeoutLimitLabel = new Label("Timeout limit (seconds): ");
                TextField setTimeoutLimitTextField = new TextField(connectionTimeoutSeconds.toString());
                setTimeoutLimitTextField.setPromptText("(seconds)");

                setTimeoutLimitAlert.getButtonTypes().clear();
                ButtonType buttonTypeSave = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
                setTimeoutLimitAlert.getButtonTypes().setAll(buttonTypeSave, ButtonType.CANCEL);

                setTimeoutLimitAlertGridPane.add(setTimeoutLimitLabel,0,0);
                setTimeoutLimitAlertGridPane.add(setTimeoutLimitTextField,0,1);

                setTimeoutLimitAlert.getDialogPane().setContent(setTimeoutLimitAlertGridPane);
                Optional<ButtonType> result = setTimeoutLimitAlert.showAndWait();
                if(result.get()==buttonTypeSave){
                    try{
                        if(Integer.parseInt(setTimeoutLimitTextField.getText())>=1){
                            connectionTimeoutSeconds=Integer.parseInt(setTimeoutLimitTextField.getText());
                            Alert setTimeoutSuccess = new Alert(Alert.AlertType.INFORMATION);
                            setTimeoutSuccess.setTitle("Success");
                            setTimeoutSuccess.setHeaderText(null);
                            setTimeoutSuccess.setContentText("Timeout limit has been succesfully changed to "+connectionTimeoutSeconds+" seconds");
                            setTimeoutSuccess.show();
                        }else{
                            Alert setTimeoutError = new Alert(Alert.AlertType.ERROR);
                            setTimeoutError.setHeaderText("Error");
                            setTimeoutError.setHeaderText(null);
                            setTimeoutError.setContentText("Please enter correct number (>=1)");
                            setTimeoutError.showAndWait();
                            menuToolsSetTimeoutLimit.fire();
                        }
                    }catch(Exception e){
                        Alert setTimeoutError = new Alert(Alert.AlertType.ERROR);
                        setTimeoutError.setHeaderText("Error");
                        setTimeoutError.setHeaderText(null);
                        setTimeoutError.setContentText("Please enter correct number (>=1)");
                        setTimeoutError.showAndWait();
                        menuToolsSetTimeoutLimit.fire();
                    }
                }

            }
        });

        menuTools.getItems().addAll(menuToolsBackgroundLight, menuToolsBackgroundDark, menuToolsSetTimeoutLimit);
        menuBar.getMenus().add(menuTools);


        Menu menuHelp = new Menu("Help");

        MenuItem menuHelpAboutORCLmonitor = new MenuItem("About ORCLmonitor");
        menuHelpAboutORCLmonitor.setMnemonicParsing(true);
        menuHelpAboutORCLmonitor.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                Alert alertAbout = new Alert(Alert.AlertType.INFORMATION);
                alertAbout.setTitle("About "+appName);
                alertAbout.setHeaderText(null);
                Separator horizontalSeparator = new Separator();
                horizontalSeparator.setOrientation(Orientation.HORIZONTAL);
                TextFlow textFlow = new TextFlow(
                        new Text(
                                "ORCLmonitor application is meant for a real-time monitor of load (waits) Oracle Database.\n" +
                                        "Application for collecting data is using two free (non-license) system views: v$system_wait_class and v$sys_time_model.\n" +
                                        "Presented sets of data are including:\n" +
                                        "- Other: Waits which should not typically occur on a system (for example, 'wait for EMON to spawn')\n" +
                                        "- Cluster: Waits related to Real Application Cluster resources (for example, global cache resources such as 'gc cr block busy'\n" +
                                        "- Queueing\n" +
                                        "- Network: Waits related to network messaging (for example, 'SQL*Net more data to dblink')\n" +
                                        "- Administrative: Waits resulting from DBA commands that cause users to wait (for example, an index rebuild)\n" +
                                        "- Configuration: Waits caused by inadequate configuration of database or instance resources (for example, undersized log file sizes, shared pool size)\n" +
                                        "- Application: Waits resulting from user application code (for example, lock waits caused by row level locking or explicit lock commands)\n" +
                                        "- Commit: This wait class only comprises one wait event - wait for redo log write confirmation after a commit (that is, 'log file sync')\n" +
                                        "- Concurrency: Waits for internal database resources (for example, latches)\n" +
                                        "- System I/O: Waits for background process IO (for example, DBWR wait for 'db file parallel write')\n" +
                                        "- Scheduler: Resource Manager related waits (for example, 'resmgr: become active')\n" +
                                        "- User I/O: Waits for user IO (for example 'db file sequential read')\n" +
                                        "- CPU\n"
                        ),
                        horizontalSeparator,
                        new Text("\n\u00a9 "+appName+" - "+appCopyrightYear)
                );
                alertAbout.getDialogPane().contentProperty().set(textFlow);
                alertAbout.showAndWait();
            }
        });

        MenuItem menuHelpAuthor = new MenuItem("Author");
        menuHelpAuthor.setMnemonicParsing(true);
        menuHelpAuthor.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                Hyperlink linkGithub = new Hyperlink(appAuthorGithub);
                linkGithub.setOnAction(e-> getHostServices().showDocument("https://"+appAuthorGithub));
                Hyperlink linkLinkedin = new Hyperlink(appAuthorLinekdin);
                linkLinkedin.setOnAction(e-> getHostServices().showDocument("https://"+appAuthorLinekdin));
                Hyperlink linkMail = new Hyperlink(appAuthorEmail);
                linkMail.setOnAction(e-> getHostServices().showDocument("mailto:"+appAuthorLinekdin));

                TextFlow textFlow = new TextFlow(
                        new Text("Author "+appAuthor),
                        new Text("\nMail: "),
                        linkMail,
                        new Text("\nGitHub: "),
                        linkGithub,
                        new Text("\nLinkedIn: "),
                        linkLinkedin
                );

                Alert alertAuthor = new Alert(Alert.AlertType.INFORMATION);
                alertAuthor.setTitle("Author");
                alertAuthor.setHeaderText(null);
                alertAuthor.getDialogPane().contentProperty().set(textFlow);
                alertAuthor.showAndWait();
            }
        });
        menuHelp.getItems().addAll(menuHelpAboutORCLmonitor, menuHelpAuthor);
        menuBar.getMenus().add(menuHelp);



        //---POCZATEK - 15_05_2021 - rozszerzenie na potrzeby kursu PBL - Problem Based Learning


        Menu menuDatabase = new Menu("Database");

        MenuItem menuDatabaseItemDatabase = new MenuItem("Select Database");
        menuDatabaseItemDatabase.setMnemonicParsing(true);
        menuDatabaseItemDatabase.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                Pane menuDatabasePane = new Pane();
                Dialog connectionDatabaseListDialog = new Dialog();
                connectionDatabaseListDialog.setTitle("Database");
                GridPane grid = new GridPane(); grid.setVgap(10); grid.setHgap(10);
                ComboBox comboBoxConnectionsList = new ComboBox();
                comboBoxConnectionsList.setPromptText("Select connection");
                for(Connection actualConnectionFromConnectionList : connections.getConnectionList()){
                    comboBoxConnectionsList.getItems().add(actualConnectionFromConnectionList.getConnectionName());
                }
                HBox hboxConnectionList = new HBox(comboBoxConnectionsList);
                grid.add(hboxConnectionList, 0, 4);

                BooleanBinding selectConnectionValidation = Bindings.createBooleanBinding(() -> connectionInComboBoxIsNotSelected(comboBoxConnectionsList));

                Label connectionName = new Label();grid.add(connectionName,0,0);
                Label connectionNameText = new Label();grid.add(connectionNameText, 1, 0);

                Label connectionAddress = new Label();grid.add(connectionAddress,0,1);
                Label connectionAddressText = new Label();grid.add(connectionAddressText, 1, 1);

                Label connectionPort = new Label();grid.add(connectionPort,0,2);
                Label connectionPortText = new Label();grid.add(connectionPortText, 1, 2);

                Label connectionServiceNameOrSID = new Label();grid.add(connectionServiceNameOrSID,0,3);
                Label connectionServiceNameOrSIDText = new Label();grid.add(connectionServiceNameOrSIDText, 1, 3);


                connectionName.setText("Connection Name: ");
                connectionAddress.setText("Connection Address: ");
                connectionPort.setText("Connection Port: ");
                connectionServiceNameOrSID.setText("Connection Service Name or SID: ");



                /*Label instanceStatus = new Label();grid.add(instanceStatus,0,4);
                Label instanceStatusText = new Label();grid.add(instanceStatusText, 1, 4);
                instanceStatus.setText("Instance Status: ");

                Label databaseStatus = new Label();grid.add(databaseStatus,0,4);
                Label databaseStatusText = new Label();grid.add(databaseStatusText, 1, 4);
                databaseStatus.setText("Database Status: ");*/



                if(selectConnectionValidation.get()){
                    comboBoxConnectionsList.setOnAction((comboBoxConnectionsListAction) -> {
                        /*try{
                            grid.getChildren().remove(9,14);
                        }catch(Exception e){}*/

                        connectionDatabaseListDialog.setTitle("Database from connection: "+comboBoxConnectionsList.getValue().toString());
                        connectionDatabaseListDialog.setWidth(475);connectionDatabaseListDialog.setHeight(250);
                        connectionDatabaseListDialog.setX((primaryStage.getWidth()/2)-(connectionDatabaseListDialog.getWidth()/2));
                        //connectionDatabaseListDialog.setWidth(425);connectionDatabaseListDialog.setHeight(450);
                        String selectedConnectionNameFromConnectionList = String.valueOf(comboBoxConnectionsList.getValue());
                        //Label labelDatabaseName = new Label();labelDatabaseName.setText("Database name");
                        //Label labelDatabaseNameText = new Label();labelDatabaseNameText.setText("");labelDatabaseNameText.setText(connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID());

                        HBox hboxButtons = new HBox();
                        Button detailsButton = new Button("Details");
                        Button spfileButton = new Button("SPFILE");
                        Button tablespacesButton = new Button("Tablespaces");
                        //Button schemasButton = new Button("Schemas");
                        Button schedulerButton = new Button("Scheduler");
                        //hboxButtons.getChildren().addAll(detailsButton, spfileButton, tablespacesButton, schemasButton);
                        hboxButtons.getChildren().addAll(detailsButton, spfileButton, tablespacesButton, schedulerButton);
                        //grid.add(labelDatabaseName,0,0);grid.add(labelDatabaseNameText,1,0);
                        grid.add(hboxButtons, 1, 4);




                        connectionNameText.setText(connections.getConnectionFromConnectionListByConnectionName(selectedConnectionNameFromConnectionList).getConnectionName());
                        connectionAddressText.setText(connections.getConnectionFromConnectionListByConnectionName(selectedConnectionNameFromConnectionList).getConnectionAddress());
                        connectionPortText.setText(connections.getConnectionFromConnectionListByConnectionName(selectedConnectionNameFromConnectionList).getConnectionPort());
                        connectionServiceNameOrSIDText.setText(connections.getConnectionFromConnectionListByConnectionName(selectedConnectionNameFromConnectionList).getConnectionServiceNameOrSID());



                        detailsButton.setOnAction(new EventHandler<ActionEvent>(){
                            public void handle(ActionEvent event){
                                if(pingTestToServer(connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress())){
                                    try{
                                        String URL = "jdbc:oracle:thin:@"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPort()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID();
                                        java.sql.Connection connection = DriverManager.getConnection(URL, connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionUser(), connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPassword());
                                        if(!connection.isClosed()){
                                            Dialog databaseDetailsAlert = new Dialog();
                                            //Alert databaseDetailsAlert = new Alert(Alert.AlertType.INFORMATION);
                                            databaseDetailsAlert.setTitle("Database from connection: "+comboBoxConnectionsList.getValue().toString()+" | Details");
                                            //databaseDetailsAlert.setHeaderText(null);
                                            databaseDetailsAlert.setWidth(550);databaseDetailsAlert.setHeight(600);
                                            //databaseDetailsAlert.setWidth(600);databaseDetailsAlert.setHeight(600);
                                            ScrollPane databaseDetailsScrollPane = new ScrollPane();
                                            GridPane databaseDetailsGridPane = new GridPane();
                                            databaseDetailsGridPane.setMinWidth(525);databaseDetailsGridPane.setMinHeight(600);
                                            databaseDetailsScrollPane.setMinWidth(550);databaseDetailsScrollPane.setMinHeight(600);




                                            TextField detailsInstance = new TextField("v$instance");detailsInstance.setEditable(false);detailsInstance.setStyle("-fx-font-weight:bold;");detailsInstance.setBackground(null);databaseDetailsGridPane.add(detailsInstance,0,0,2,1);
                                            TextField detailsInstanceInstanceNumber = new TextField("INSTANCE_NUMBER");detailsInstanceInstanceNumber.setEditable(false);databaseDetailsGridPane.add(detailsInstanceInstanceNumber,0,1);detailsInstanceInstanceNumber.setMinWidth(250);
                                            TextField detailsInstanceInstanceName = new TextField("INSTANCE_NAME");detailsInstanceInstanceName.setEditable(false);databaseDetailsGridPane.add(detailsInstanceInstanceName,0,2);
                                            TextField detailsInstanceHostName = new TextField("HOST_NAME");detailsInstanceHostName.setEditable(false);databaseDetailsGridPane.add(detailsInstanceHostName,0,3);
                                            TextField detailsInstanceVersion = new TextField("VERSION");detailsInstanceVersion.setEditable(false);databaseDetailsGridPane.add(detailsInstanceVersion,0,4);
                                            TextField detailsInstanceStartupTime = new TextField("STARTUP_TIME");detailsInstanceStartupTime.setEditable(false);databaseDetailsGridPane.add(detailsInstanceStartupTime,0,5);
                                            TextField detailsInstanceStatus = new TextField("STATUS");detailsInstanceStatus.setEditable(false);databaseDetailsGridPane.add(detailsInstanceStatus,0,6);
                                            TextField detailsInstanceParallel = new TextField("PARALLEL");detailsInstanceParallel.setEditable(false);databaseDetailsGridPane.add(detailsInstanceParallel,0,7);
                                            TextField detailsInstanceThread = new TextField("THREAD#");detailsInstanceThread.setEditable(false);databaseDetailsGridPane.add(detailsInstanceThread,0,8);
                                            TextField detailsInstanceArchiver = new TextField("ARCHIVER");detailsInstanceArchiver.setEditable(false);databaseDetailsGridPane.add(detailsInstanceArchiver,0,9);
                                            TextField detailsInstanceLogSwitchWait = new TextField("LOG_SWITCH_WAIT");detailsInstanceLogSwitchWait.setEditable(false);databaseDetailsGridPane.add(detailsInstanceLogSwitchWait,0,10);
                                            TextField detailsInstanceLogins = new TextField("LOGINS");detailsInstanceLogins.setEditable(false);databaseDetailsGridPane.add(detailsInstanceLogins,0,11);
                                            TextField detailsInstanceShutdownPending = new TextField("SHUTDOWN_PENDING");detailsInstanceShutdownPending.setEditable(false);databaseDetailsGridPane.add(detailsInstanceShutdownPending,0,12);
                                            TextField detailsInstanceDatabaseStatus = new TextField("DATABASE_STATUS");detailsInstanceDatabaseStatus.setEditable(false);databaseDetailsGridPane.add(detailsInstanceDatabaseStatus,0,13);
                                            TextField detailsInstanceInstanceRole = new TextField("INSTANCE_ROLE");detailsInstanceInstanceRole.setEditable(false);databaseDetailsGridPane.add(detailsInstanceInstanceRole,0,14);
                                            TextField detailsInstanceActiveState = new TextField("ACTIVE_STATUS");detailsInstanceActiveState.setEditable(false);databaseDetailsGridPane.add(detailsInstanceActiveState,0,15);
                                            TextField detailsInstanceBlocked = new TextField("BLOCKED");detailsInstanceBlocked.setEditable(false);databaseDetailsGridPane.add(detailsInstanceBlocked,0,16);
                                            TextField detailsInstanceConId = new TextField("CON_ID");detailsInstanceConId.setEditable(false);databaseDetailsGridPane.add(detailsInstanceConId,0,17);
                                            TextField detailsInstanceInstanceMode = new TextField("INSTANCE_MODE");detailsInstanceInstanceMode.setEditable(false);databaseDetailsGridPane.add(detailsInstanceInstanceMode,0,18);
                                            TextField detailsInstanceEdition = new TextField("EDITION");detailsInstanceEdition.setEditable(false);databaseDetailsGridPane.add(detailsInstanceEdition,0,19);
                                            TextField detailsInstanceFamily = new TextField("FAMILY");detailsInstanceFamily.setEditable(false);databaseDetailsGridPane.add(detailsInstanceFamily,0,20);
                                            TextField detailsInstanceDatabaseType = new TextField("DATABASE_TYPE");detailsInstanceDatabaseType.setEditable(false);databaseDetailsGridPane.add(detailsInstanceDatabaseType,0,21);

                                            TextField detailsDatabase = new TextField("v$database");detailsDatabase.setEditable(false);detailsDatabase.setStyle("-fx-font-weight:bold;");detailsDatabase.setBackground(null);databaseDetailsGridPane.add(detailsDatabase,0,23,2,1);
                                            TextField detailsDatabaseDBID = new TextField("DBID");detailsDatabaseDBID.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseDBID,0,24);
                                            TextField detailsDatabaseName = new TextField("NAME");detailsDatabaseName.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseName,0,25);
                                            TextField detailsDatabaseCreated = new TextField("CREATED");detailsDatabaseCreated.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseCreated,0,26);
                                            TextField detailsDatabaseResetLogsChange = new TextField("RESETLOGS_CHANGE#");detailsDatabaseResetLogsChange.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseResetLogsChange,0,27);
                                            TextField detailsDatabaseResetLogsTime = new TextField("RESETLOGS_TIME");detailsDatabaseResetLogsTime.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseResetLogsTime,0,28);
                                            TextField detailsDatabasePriorResetLogsChange = new TextField("PRIOR_RESETLOGS_CHANGE#");detailsDatabasePriorResetLogsChange.setEditable(false);databaseDetailsGridPane.add(detailsDatabasePriorResetLogsChange,0,29);
                                            TextField detailsDatabasePriorResetLogsTime = new TextField("PRIOR_RESETLOGS_TIME");detailsDatabasePriorResetLogsTime.setEditable(false);databaseDetailsGridPane.add(detailsDatabasePriorResetLogsTime,0,30);
                                            TextField detailsDatabaseLogMode = new TextField("LOG_MODE");detailsDatabaseLogMode.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseLogMode,0,31);
                                            TextField detailsDatabaseCheckpointChange = new TextField("CHECKPOINT_CHANGE#");detailsDatabaseCheckpointChange.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseCheckpointChange,0,32);
                                            TextField detailsDatabaseArchiveChange = new TextField("ARCHIVE_CHANGE#");detailsDatabaseArchiveChange.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseArchiveChange,0,33);
                                            TextField detailsDatabaseControlFileType = new TextField("CONTROLFILE_TYPE");detailsDatabaseControlFileType.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseControlFileType,0,34);
                                            TextField detailsDatabaseControlFileCreated = new TextField("CONTROLFILE_CREATED");detailsDatabaseControlFileCreated.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseControlFileCreated,0,35);
                                            TextField detailsDatabaseControlFileSequence = new TextField("CONTROLFILE_SEQUENCE#");detailsDatabaseControlFileSequence.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseControlFileSequence,0,36);
                                            TextField detailsDatabaseControlFileChange = new TextField("CONTROLFILE_CHANGE#");detailsDatabaseControlFileChange.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseControlFileChange,0,37);
                                            TextField detailsDatabaseControlFileTime = new TextField("CONTROLFILE_TIME");detailsDatabaseControlFileTime.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseControlFileTime,0,38);
                                            TextField detailsDatabaseOpenResetLogs = new TextField("OPEN_RESETLOGS");detailsDatabaseOpenResetLogs.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseOpenResetLogs,0,39);
                                            TextField detailsDatabaseVersionTime = new TextField("VERSION_TIME");detailsDatabaseVersionTime.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseVersionTime,0,40);
                                            TextField detailsDatabaseOpenMode = new TextField("OPEN_MODE");detailsDatabaseOpenMode.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseOpenMode,0,41);
                                            TextField detailsDatabaseProtectionMode = new TextField("PROTECTION_MODE");detailsDatabaseProtectionMode.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseProtectionMode,0,42);
                                            TextField detailsDatabaseProtectionLevel = new TextField("PROTECTION_LEVEL");detailsDatabaseProtectionLevel.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseProtectionLevel,0,43);
                                            TextField detailsDatabaseRemoteArchive = new TextField("REMOTE_ARCHIVE");detailsDatabaseRemoteArchive.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseRemoteArchive,0,44);
                                            TextField detailsDatabaseActivation = new TextField("ACTIVATION#");detailsDatabaseActivation.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseActivation,0,45);
                                            TextField detailsDatabaseSwitchover = new TextField("SWITCHOVER#");detailsDatabaseSwitchover.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseSwitchover,0,46);
                                            TextField detailsDatabaseDatabaseRole = new TextField("DATABASE_ROLE");detailsDatabaseDatabaseRole.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseDatabaseRole,0,47);
                                            TextField detailsDatabaseArchivelogChange = new TextField("ARCHIVELOG_CHANGE#");detailsDatabaseArchivelogChange.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseArchivelogChange,0,48);
                                            TextField detailsDatabaseArchivelogCompression = new TextField("ARCHIVELOG_COMPRESSION");detailsDatabaseArchivelogCompression.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseArchivelogCompression,0,49);
                                            TextField detailsDatabaseSwitchoverStatus = new TextField("SWITCHOVER_STATUS");detailsDatabaseSwitchoverStatus.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseSwitchoverStatus,0,50);
                                            TextField detailsDatabaseDataGuardBroker = new TextField("DATAGUARD_BROKER");detailsDatabaseDataGuardBroker.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseDataGuardBroker,0,51);
                                            TextField detailsDatabaseGuardStatus = new TextField("GUARD_STATUS");detailsDatabaseGuardStatus.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseGuardStatus,0,52);
                                            TextField detailsDatabaseSupplementalLogDataMin = new TextField("SUPPLEMENTAL_LOG_DATA_MIN");detailsDatabaseSupplementalLogDataMin.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseSupplementalLogDataMin,0,53);
                                            TextField detailsDatabaseSupplementalLogDataPK   = new TextField("SUPPLEMENTAL_LOG_DATA_PK");detailsDatabaseSupplementalLogDataPK.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseSupplementalLogDataPK,0,54);
                                            TextField detailsDatabaseSupplementalLogDataUI = new TextField("SUPPLEMENTAL_LOG_DATA_UI");detailsDatabaseSupplementalLogDataUI.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseSupplementalLogDataUI,0,55);
                                            TextField detailsDatabaseForceLogging = new TextField("FORCE_LOGGING");detailsDatabaseForceLogging.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseForceLogging,0,56);
                                            TextField detailsDatabasePlatformId = new TextField("PLATFORM_ID");detailsDatabasePlatformId.setEditable(false);databaseDetailsGridPane.add(detailsDatabasePlatformId,0,57);
                                            TextField detailsDatabasePlatformName = new TextField("PLATFORM_NAME");detailsDatabasePlatformName.setEditable(false);databaseDetailsGridPane.add(detailsDatabasePlatformName,0,58);
                                            TextField detailsDatabaseRecoveryTargetIncarnation = new TextField("RECOVERY_TARGET_INCARNATION#");detailsDatabaseRecoveryTargetIncarnation.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseRecoveryTargetIncarnation,0,59);
                                            TextField detailsDatabaseLastOpenIncarnation = new TextField("LAST_OPEN_INCARNATION#");detailsDatabaseLastOpenIncarnation.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseLastOpenIncarnation,0,60);
                                            TextField detailsDatabaseCurrentSCN = new TextField("CURRENT_SCN");detailsDatabaseCurrentSCN.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseCurrentSCN,0,61);
                                            TextField detailsDatabaseFlashbackOn = new TextField("FLASHBACK_ON");detailsDatabaseFlashbackOn.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseFlashbackOn,0,62);
                                            TextField detailsDatabaseSupplementalLogDataFK = new TextField("SUPPLEMENTAL_LOG_DATA_FK");detailsDatabaseSupplementalLogDataFK.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseSupplementalLogDataFK,0,63);
                                            TextField detailsDatabaseSupplementalLogDataALL = new TextField("SUPPLEMENTAL_LOG_DATA_ALL");detailsDatabaseSupplementalLogDataALL.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseSupplementalLogDataALL,0,64);
                                            TextField detailsDatabaseDBUniqueName = new TextField("DB_UNIQUE_NAME");detailsDatabaseDBUniqueName.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseDBUniqueName,0,65);
                                            TextField detailsDatabaseStandbyBecamePrimarySCN = new TextField("STANDBY_BECAME_PRIMARY_SCN");detailsDatabaseStandbyBecamePrimarySCN.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseStandbyBecamePrimarySCN,0,66);
                                            TextField detailsDatabaseFSFailoverStatus = new TextField("FS_FAILOVER_STATUS");detailsDatabaseFSFailoverStatus.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseFSFailoverStatus,0,67);
                                            TextField detailsDatabaseFSFailoverCurrentTarget = new TextField("FS_FAILOVER_CURRENT_TARGET");detailsDatabaseFSFailoverCurrentTarget.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseFSFailoverCurrentTarget,0,68);
                                            TextField detailsDatabaseFSFailoverThreshold = new TextField("FS_FAILOVER_THRESHOLD");detailsDatabaseFSFailoverThreshold.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseFSFailoverThreshold,0,69);
                                            TextField detailsDatabaseFSFailoverObserverPresent = new TextField("FS_FAILOVER_OBSERVER_PRESENT");detailsDatabaseFSFailoverObserverPresent.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseFSFailoverObserverPresent,0,70);
                                            TextField detailsDatabaseFSFailoverObserverHost = new TextField("FS_FAILOVER_OBSERVER_HOST");detailsDatabaseFSFailoverObserverHost.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseFSFailoverObserverHost,0,71);
                                            TextField detailsDatabaseControlfileConverted = new TextField("CONTROLFILE_CONVERTED");detailsDatabaseControlfileConverted.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseControlfileConverted,0,72);
                                            TextField detailsDatabasePrimaryDBUniqueName = new TextField("PRIMARY_DB_UNIQUE_NAME");detailsDatabasePrimaryDBUniqueName.setEditable(false);databaseDetailsGridPane.add(detailsDatabasePrimaryDBUniqueName,0,73);
                                            TextField detailsDatabaseSupplementalLogDataPL = new TextField("SUPPLEMENTAL_LOG_DATA_PL");detailsDatabaseSupplementalLogDataPL.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseSupplementalLogDataPL,0,74);
                                            TextField detailsDatabaseMinRequiredCaptureChange = new TextField("MIN_REQUIRED_CAPTURE_CHANGE");detailsDatabaseMinRequiredCaptureChange.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseMinRequiredCaptureChange,0,75);
                                            TextField detailsDatabaseCDB = new TextField("CDB");detailsDatabaseCDB.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseCDB,0,76);
                                            TextField detailsDatabaseConId1 = new TextField("CON_ID_1");detailsDatabaseConId1.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseConId1,0,77);
                                            TextField detailsDatabasePendingRoleChangeTasks = new TextField("PENDING_ROLE_CHANGE_TASKS");detailsDatabasePendingRoleChangeTasks.setEditable(false);databaseDetailsGridPane.add(detailsDatabasePendingRoleChangeTasks,0,78);
                                            TextField detailsDatabaseConDbId = new TextField("CON_DBID");detailsDatabaseConDbId.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseConDbId,0,79);
                                            TextField detailsDatabaseForceFullDbCaching = new TextField("FORCE_FULL_DB_CACHING");detailsDatabaseForceFullDbCaching.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseForceFullDbCaching,0,80);


                                            //TextField detailsInstanceText = new TextField("v$instance");detailsInstanceText.setEditable(false);
                                            TextField detailsInstanceInstanceNumberText = new TextField();detailsInstanceInstanceNumberText.setEditable(false);databaseDetailsGridPane.add(detailsInstanceInstanceNumberText,1,1);
                                            TextField detailsInstanceInstanceNameText = new TextField();detailsInstanceInstanceNameText.setEditable(false);databaseDetailsGridPane.add(detailsInstanceInstanceNameText,1,2);
                                            TextField detailsInstanceHostNameText = new TextField();detailsInstanceHostNameText.setEditable(false);databaseDetailsGridPane.add(detailsInstanceHostNameText,1,3);
                                            TextField detailsInstanceVersionText = new TextField();detailsInstanceVersionText.setEditable(false);databaseDetailsGridPane.add(detailsInstanceVersionText,1,4);
                                            TextField detailsInstanceStartupTimeText = new TextField();detailsInstanceStartupTimeText.setEditable(false);databaseDetailsGridPane.add(detailsInstanceStartupTimeText,1,5);
                                            TextField detailsInstanceStatusText = new TextField();detailsInstanceStatusText.setEditable(false);databaseDetailsGridPane.add(detailsInstanceStatusText,1,6);
                                            TextField detailsInstanceParallelText = new TextField();detailsInstanceParallelText.setEditable(false);databaseDetailsGridPane.add(detailsInstanceParallelText,1,7);
                                            TextField detailsInstanceThreadText = new TextField();detailsInstanceThreadText.setEditable(false);databaseDetailsGridPane.add(detailsInstanceThreadText,1,8);
                                            TextField detailsInstanceArchiverText = new TextField();detailsInstanceArchiverText.setEditable(false);databaseDetailsGridPane.add(detailsInstanceArchiverText,1,9);
                                            TextField detailsInstanceLogSwitchWaitText = new TextField();detailsInstanceLogSwitchWaitText.setEditable(false);databaseDetailsGridPane.add(detailsInstanceLogSwitchWaitText,1,10);
                                            TextField detailsInstanceLoginsText = new TextField();detailsInstanceLoginsText.setEditable(false);databaseDetailsGridPane.add(detailsInstanceLoginsText,1,11);
                                            TextField detailsInstanceShutdownPendingText = new TextField();detailsInstanceShutdownPendingText.setEditable(false);databaseDetailsGridPane.add(detailsInstanceShutdownPendingText,1,12);
                                            TextField detailsInstanceDatabaseStatusText = new TextField();detailsInstanceDatabaseStatusText.setEditable(false);databaseDetailsGridPane.add(detailsInstanceDatabaseStatusText,1,13);
                                            TextField detailsInstanceInstanceRoleText = new TextField();detailsInstanceInstanceRoleText.setEditable(false);databaseDetailsGridPane.add(detailsInstanceInstanceRoleText,1,14);
                                            TextField detailsInstanceActiveStateText = new TextField();detailsInstanceActiveStateText.setEditable(false);databaseDetailsGridPane.add(detailsInstanceActiveStateText,1,15);
                                            TextField detailsInstanceBlockedText = new TextField();detailsInstanceBlockedText.setEditable(false);databaseDetailsGridPane.add(detailsInstanceBlockedText,1,16);
                                            TextField detailsInstanceConIdText = new TextField();detailsInstanceConIdText.setEditable(false);databaseDetailsGridPane.add(detailsInstanceConIdText,1,17);
                                            TextField detailsInstanceInstanceModeText = new TextField();detailsInstanceInstanceModeText.setEditable(false);databaseDetailsGridPane.add(detailsInstanceInstanceModeText,1,18);
                                            TextField detailsInstanceEditionText = new TextField();detailsInstanceEditionText.setEditable(false);databaseDetailsGridPane.add(detailsInstanceEditionText,1,19);
                                            TextField detailsInstanceFamilyText = new TextField();detailsInstanceFamilyText.setEditable(false);databaseDetailsGridPane.add(detailsInstanceFamilyText,1,20);
                                            TextField detailsInstanceDatabaseTypeText = new TextField();detailsInstanceDatabaseTypeText.setEditable(false);databaseDetailsGridPane.add(detailsInstanceDatabaseTypeText,1,21);

                                            //TextField detailsDatabaseText = new TextField("v$database");detailsDatabaseText.setEditable(false);
                                            TextField detailsDatabaseDBIDText = new TextField();detailsDatabaseDBIDText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseDBIDText,1,24);
                                            TextField detailsDatabaseNameText = new TextField();detailsDatabaseNameText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseNameText,1,25);
                                            TextField detailsDatabaseCreatedText = new TextField();detailsDatabaseCreatedText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseCreatedText,1,26);
                                            TextField detailsDatabaseResetLogsChangeText = new TextField();detailsDatabaseResetLogsChangeText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseResetLogsChangeText,1,27);
                                            TextField detailsDatabaseResetLogsTimeText = new TextField();detailsDatabaseResetLogsTimeText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseResetLogsTimeText,1,28);
                                            TextField detailsDatabasePriorResetLogsChangeText = new TextField();detailsDatabasePriorResetLogsChangeText.setEditable(false);databaseDetailsGridPane.add(detailsDatabasePriorResetLogsChangeText,1,29);
                                            TextField detailsDatabasePriorResetLogsTimeText = new TextField();detailsDatabasePriorResetLogsTimeText.setEditable(false);databaseDetailsGridPane.add(detailsDatabasePriorResetLogsTimeText,1,30);
                                            TextField detailsDatabaseLogModeText = new TextField();detailsDatabaseLogModeText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseLogModeText,1,31);
                                            TextField detailsDatabaseCheckpointChangeText = new TextField();detailsDatabaseCheckpointChangeText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseCheckpointChangeText,1,32);
                                            TextField detailsDatabaseArchiveChangeText = new TextField();detailsDatabaseArchiveChangeText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseArchiveChangeText,1,33);
                                            TextField detailsDatabaseControlFileTypeText = new TextField();detailsDatabaseControlFileTypeText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseControlFileTypeText,1,34);
                                            TextField detailsDatabaseControlFileCreatedText = new TextField();detailsDatabaseControlFileCreatedText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseControlFileCreatedText,1,35);
                                            TextField detailsDatabaseControlFileSequenceText = new TextField();detailsDatabaseControlFileSequenceText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseControlFileSequenceText,1,36);
                                            TextField detailsDatabaseControlFileChangeText = new TextField();detailsDatabaseControlFileChangeText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseControlFileChangeText,1,37);
                                            TextField detailsDatabaseControlFileTimeText = new TextField();detailsDatabaseControlFileTimeText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseControlFileTimeText,1,38);
                                            TextField detailsDatabaseOpenResetLogsText = new TextField();detailsDatabaseOpenResetLogsText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseOpenResetLogsText,1,39);
                                            TextField detailsDatabaseVersionTimeText = new TextField();detailsDatabaseVersionTimeText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseVersionTimeText,1,40);
                                            TextField detailsDatabaseOpenModeText = new TextField();detailsDatabaseOpenModeText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseOpenModeText,1,41);
                                            TextField detailsDatabaseProtectionModeText = new TextField();detailsDatabaseProtectionModeText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseProtectionModeText,1,42);
                                            TextField detailsDatabaseProtectionLevelText = new TextField();detailsDatabaseProtectionLevelText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseProtectionLevelText,1,43);
                                            TextField detailsDatabaseRemoteArchiveText = new TextField();detailsDatabaseRemoteArchiveText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseRemoteArchiveText,1,44);
                                            TextField detailsDatabaseActivationText = new TextField();detailsDatabaseActivationText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseActivationText,1,45);
                                            TextField detailsDatabaseSwitchoverText = new TextField();detailsDatabaseSwitchoverText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseSwitchoverText,1,46);
                                            TextField detailsDatabaseDatabaseRoleText = new TextField();detailsDatabaseDatabaseRoleText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseDatabaseRoleText,1,47);
                                            TextField detailsDatabaseArchivelogChangeText = new TextField();detailsDatabaseArchivelogChangeText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseArchivelogChangeText,1,48);
                                            TextField detailsDatabaseArchivelogCompressionText = new TextField();detailsDatabaseArchivelogCompressionText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseArchivelogCompressionText,1,49);
                                            TextField detailsDatabaseSwitchoverStatusText = new TextField();detailsDatabaseSwitchoverStatusText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseSwitchoverStatusText,1,50);
                                            TextField detailsDatabaseDataGuardBrokerText = new TextField();detailsDatabaseDataGuardBrokerText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseDataGuardBrokerText,1,51);
                                            TextField detailsDatabaseGuardStatusText = new TextField();detailsDatabaseGuardStatusText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseGuardStatusText,1,52);
                                            TextField detailsDatabaseSupplementalLogDataMinText = new TextField();detailsDatabaseSupplementalLogDataMinText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseSupplementalLogDataMinText,1,53);
                                            TextField detailsDatabaseSupplementalLogDataPKText   = new TextField();detailsDatabaseSupplementalLogDataPKText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseSupplementalLogDataPKText,1,54);
                                            TextField detailsDatabaseSupplementalLogDataUIText = new TextField();detailsDatabaseSupplementalLogDataUIText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseSupplementalLogDataUIText,1,55);
                                            TextField detailsDatabaseForceLoggingText = new TextField();detailsDatabaseForceLoggingText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseForceLoggingText,1,56);
                                            TextField detailsDatabasePlatformIdText = new TextField();detailsDatabasePlatformIdText.setEditable(false);databaseDetailsGridPane.add(detailsDatabasePlatformIdText,1,57);
                                            TextField detailsDatabasePlatformNameText = new TextField();detailsDatabasePlatformNameText.setEditable(false);databaseDetailsGridPane.add(detailsDatabasePlatformNameText,1,58);
                                            TextField detailsDatabaseRecoveryTargetIncarnationText = new TextField();detailsDatabaseRecoveryTargetIncarnationText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseRecoveryTargetIncarnationText,1,59);
                                            TextField detailsDatabaseLastOpenIncarnationText = new TextField();detailsDatabaseLastOpenIncarnationText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseLastOpenIncarnationText,1,60);
                                            TextField detailsDatabaseCurrentSCNText = new TextField();detailsDatabaseCurrentSCNText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseCurrentSCNText,1,61);
                                            TextField detailsDatabaseFlashbackOnText = new TextField();detailsDatabaseFlashbackOnText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseFlashbackOnText,1,62);
                                            TextField detailsDatabaseSupplementalLogDataFKText = new TextField();detailsDatabaseSupplementalLogDataFKText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseSupplementalLogDataFKText,1,63);
                                            TextField detailsDatabaseSupplementalLogDataALLText = new TextField();detailsDatabaseSupplementalLogDataALLText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseSupplementalLogDataALLText,1,64);
                                            TextField detailsDatabaseDBUniqueNameText = new TextField();detailsDatabaseDBUniqueNameText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseDBUniqueNameText,1,65);
                                            TextField detailsDatabaseStandbyBecamePrimarySCNText = new TextField();detailsDatabaseStandbyBecamePrimarySCNText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseStandbyBecamePrimarySCNText,1,66);
                                            TextField detailsDatabaseFSFailoverStatusText = new TextField();detailsDatabaseFSFailoverStatusText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseFSFailoverStatusText,1,67);
                                            TextField detailsDatabaseFSFailoverCurrentTargetText = new TextField();detailsDatabaseFSFailoverCurrentTargetText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseFSFailoverCurrentTargetText,1,68);
                                            TextField detailsDatabaseFSFailoverThresholdText = new TextField();detailsDatabaseFSFailoverThresholdText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseFSFailoverThresholdText,1,69);
                                            TextField detailsDatabaseFSFailoverObserverPresentText = new TextField();detailsDatabaseFSFailoverObserverPresentText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseFSFailoverObserverPresentText,1,70);
                                            TextField detailsDatabaseFSFailoverObserverHostText = new TextField();detailsDatabaseFSFailoverObserverHostText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseFSFailoverObserverHostText,1,71);
                                            TextField detailsDatabaseControlfileConvertedText = new TextField();detailsDatabaseControlfileConvertedText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseControlfileConvertedText,1,72);
                                            TextField detailsDatabasePrimaryDBUniqueNameText = new TextField();detailsDatabasePrimaryDBUniqueNameText.setEditable(false);databaseDetailsGridPane.add(detailsDatabasePrimaryDBUniqueNameText,1,73);
                                            TextField detailsDatabaseSupplementalLogDataPLText = new TextField();detailsDatabaseSupplementalLogDataPLText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseSupplementalLogDataPLText,1,74);
                                            TextField detailsDatabaseMinRequiredCaptureChangeText = new TextField();detailsDatabaseMinRequiredCaptureChangeText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseMinRequiredCaptureChangeText,1,75);
                                            TextField detailsDatabaseCDBText = new TextField();detailsDatabaseCDBText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseCDBText,1,76);
                                            TextField detailsDatabaseConId1Text = new TextField();detailsDatabaseConId1Text.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseConId1Text,1,77);
                                            TextField detailsDatabasePendingRoleChangeTasksText = new TextField();detailsDatabasePendingRoleChangeTasksText.setEditable(false);databaseDetailsGridPane.add(detailsDatabasePendingRoleChangeTasksText,1,78);
                                            TextField detailsDatabaseConDbIdText = new TextField();detailsDatabaseConDbIdText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseConDbIdText,1,79);
                                            TextField detailsDatabaseForceFullDbCachingText = new TextField();detailsDatabaseForceFullDbCachingText.setEditable(false);databaseDetailsGridPane.add(detailsDatabaseForceFullDbCachingText,1,80);detailsDatabaseForceFullDbCachingText.setMinWidth(250);


                                            Task<Void> getDatabaseDetails = new Task<Void>() {
                                                @Override
                                                protected Void call() throws Exception {
                                                    String query = "select * from v$instance, v$database";
                                                    String URL = "jdbc:oracle:thin:@"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPort()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID();
                                                    java.sql.Connection connection = DriverManager.getConnection(URL, connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionUser(), connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPassword());
                                                    try{
                                                        if (!connection.isClosed()) {
                                                            System.out.println("woooo");
                                                            Statement statement = connection.createStatement();
                                                            ResultSet resultSet = statement.executeQuery(query);

                                                            //Platform.runLater(()->{
                                                            //tutaj
                                                            //});


                                                            while(resultSet.next()){
                                                                System.out.println("Aaaa");
                                                                System.out.println(resultSet.getString("INSTANCE_NUMBER"));
                                                                detailsInstanceInstanceNumberText.setText(resultSet.getString("INSTANCE_NUMBER"));
                                                                detailsInstanceInstanceNumberText.setText(resultSet.getString("INSTANCE_NUMBER"));
                                                                detailsInstanceInstanceNameText.setText(resultSet.getString("INSTANCE_NAME"));
                                                                detailsInstanceHostNameText.setText(resultSet.getString("HOST_NAME"));
                                                                detailsInstanceVersionText.setText(resultSet.getString("VERSION"));
                                                                detailsInstanceStartupTimeText.setText(resultSet.getString("STARTUP_TIME"));
                                                                detailsInstanceStatusText.setText(resultSet.getString("STATUS"));
                                                                detailsInstanceParallelText.setText(resultSet.getString("PARALLEL"));
                                                                detailsInstanceThreadText.setText(resultSet.getString("THREAD#"));
                                                                detailsInstanceArchiverText.setText(resultSet.getString("ARCHIVER"));
                                                                detailsInstanceLogSwitchWaitText.setText(resultSet.getString("LOG_SWITCH_WAIT"));
                                                                detailsInstanceLoginsText.setText(resultSet.getString("LOGINS"));
                                                                detailsInstanceShutdownPendingText.setText(resultSet.getString("SHUTDOWN_PENDING"));
                                                                detailsInstanceDatabaseStatusText.setText(resultSet.getString("DATABASE_STATUS"));
                                                                detailsInstanceInstanceRoleText.setText(resultSet.getString("INSTANCE_ROLE"));
                                                                detailsInstanceActiveStateText.setText(resultSet.getString("ACTIVE_STATE"));
                                                                detailsInstanceBlockedText.setText(resultSet.getString("BLOCKED"));
                                                                detailsInstanceConIdText.setText(resultSet.getString("CON_ID"));
                                                                detailsInstanceInstanceModeText.setText(resultSet.getString("INSTANCE_MODE"));
                                                                detailsInstanceEditionText.setText(resultSet.getString("EDITION"));
                                                                detailsInstanceFamilyText.setText(resultSet.getString("FAMILY"));
                                                                detailsInstanceDatabaseTypeText.setText(resultSet.getString("DATABASE_TYPE"));

                                                                detailsDatabaseDBIDText.setText(resultSet.getString("DBID"));
                                                                detailsDatabaseNameText.setText(resultSet.getString("NAME"));
                                                                detailsDatabaseCreatedText.setText(resultSet.getString("CREATED"));
                                                                detailsDatabaseResetLogsChangeText.setText(resultSet.getString("RESETLOGS_CHANGE#"));
                                                                detailsDatabaseResetLogsTimeText.setText(resultSet.getString("RESETLOGS_TIME"));
                                                                detailsDatabasePriorResetLogsChangeText.setText(resultSet.getString("PRIOR_RESETLOGS_CHANGE#"));
                                                                detailsDatabasePriorResetLogsTimeText.setText(resultSet.getString("PRIOR_RESETLOGS_TIME"));
                                                                detailsDatabaseLogModeText.setText(resultSet.getString("LOG_MODE"));
                                                                detailsDatabaseCheckpointChangeText.setText(resultSet.getString("CHECKPOINT_CHANGE#"));
                                                                detailsDatabaseArchiveChangeText.setText(resultSet.getString("ARCHIVE_CHANGE#"));
                                                                detailsDatabaseControlFileTypeText.setText(resultSet.getString("CONTROLFILE_TYPE"));
                                                                detailsDatabaseControlFileCreatedText.setText(resultSet.getString("CONTROLFILE_CREATED"));
                                                                detailsDatabaseControlFileSequenceText.setText(resultSet.getString("CONTROLFILE_SEQUENCE#"));
                                                                detailsDatabaseControlFileChangeText.setText(resultSet.getString("CONTROLFILE_CHANGE#"));
                                                                detailsDatabaseControlFileTimeText.setText(resultSet.getString("CONTROLFILE_TIME"));
                                                                detailsDatabaseOpenResetLogsText.setText(resultSet.getString("OPEN_RESETLOGS"));
                                                                detailsDatabaseVersionTimeText.setText(resultSet.getString("VERSION_TIME"));
                                                                detailsDatabaseOpenModeText.setText(resultSet.getString("OPEN_MODE"));
                                                                detailsDatabaseProtectionModeText.setText(resultSet.getString("PROTECTION_MODE"));
                                                                detailsDatabaseProtectionLevelText.setText(resultSet.getString("PROTECTION_LEVEL"));
                                                                detailsDatabaseRemoteArchiveText.setText(resultSet.getString("REMOTE_ARCHIVE"));
                                                                detailsDatabaseActivationText.setText(resultSet.getString("ACTIVATION#"));
                                                                detailsDatabaseSwitchoverText.setText(resultSet.getString("SWITCHOVER#"));
                                                                detailsDatabaseDatabaseRoleText.setText(resultSet.getString("DATABASE_ROLE"));
                                                                detailsDatabaseArchivelogChangeText.setText(resultSet.getString("ARCHIVELOG_CHANGE#"));
                                                                detailsDatabaseArchivelogCompressionText.setText(resultSet.getString("ARCHIVELOG_COMPRESSION"));
                                                                detailsDatabaseSwitchoverStatusText.setText(resultSet.getString("SWITCHOVER_STATUS"));
                                                                detailsDatabaseDataGuardBrokerText.setText(resultSet.getString("DATAGUARD_BROKER"));
                                                                detailsDatabaseGuardStatusText.setText(resultSet.getString("GUARD_STATUS"));
                                                                detailsDatabaseSupplementalLogDataMinText.setText(resultSet.getString("SUPPLEMENTAL_LOG_DATA_MIN"));
                                                                detailsDatabaseSupplementalLogDataPKText.setText(resultSet.getString("SUPPLEMENTAL_LOG_DATA_PK"));
                                                                detailsDatabaseSupplementalLogDataUIText.setText(resultSet.getString("SUPPLEMENTAL_LOG_DATA_UI"));
                                                                detailsDatabaseForceLoggingText.setText(resultSet.getString("FORCE_LOGGING"));
                                                                detailsDatabasePlatformIdText.setText(resultSet.getString("PLATFORM_ID"));
                                                                detailsDatabasePlatformNameText.setText(resultSet.getString("PLATFORM_NAME"));
                                                                detailsDatabaseRecoveryTargetIncarnationText.setText(resultSet.getString("RECOVERY_TARGET_INCARNATION#"));
                                                                detailsDatabaseLastOpenIncarnationText.setText(resultSet.getString("LAST_OPEN_INCARNATION#"));
                                                                detailsDatabaseCurrentSCNText.setText(resultSet.getString("CURRENT_SCN"));
                                                                detailsDatabaseFlashbackOnText.setText(resultSet.getString("FLASHBACK_ON"));
                                                                detailsDatabaseSupplementalLogDataFKText.setText(resultSet.getString("SUPPLEMENTAL_LOG_DATA_FK"));
                                                                detailsDatabaseSupplementalLogDataALLText.setText(resultSet.getString("SUPPLEMENTAL_LOG_DATA_ALL"));
                                                                detailsDatabaseDBUniqueNameText.setText(resultSet.getString("DB_UNIQUE_NAME"));
                                                                detailsDatabaseStandbyBecamePrimarySCNText.setText(resultSet.getString("STANDBY_BECAME_PRIMARY_SCN"));
                                                                detailsDatabaseFSFailoverStatusText.setText(resultSet.getString("FS_FAILOVER_STATUS"));
                                                                detailsDatabaseFSFailoverCurrentTargetText.setText(resultSet.getString("FS_FAILOVER_CURRENT_TARGET"));
                                                                detailsDatabaseFSFailoverThresholdText.setText(resultSet.getString("FS_FAILOVER_THRESHOLD"));
                                                                detailsDatabaseFSFailoverObserverPresentText.setText(resultSet.getString("FS_FAILOVER_OBSERVER_PRESENT"));
                                                                detailsDatabaseFSFailoverObserverHostText.setText(resultSet.getString("FS_FAILOVER_OBSERVER_HOST"));
                                                                detailsDatabaseControlfileConvertedText.setText(resultSet.getString("CONTROLFILE_CONVERTED"));
                                                                detailsDatabasePrimaryDBUniqueNameText.setText(resultSet.getString("PRIMARY_DB_UNIQUE_NAME"));
                                                                detailsDatabaseSupplementalLogDataPLText.setText(resultSet.getString("SUPPLEMENTAL_LOG_DATA_PL"));
                                                                detailsDatabaseMinRequiredCaptureChangeText.setText(resultSet.getString("MIN_REQUIRED_CAPTURE_CHANGE#"));
                                                                detailsDatabaseCDBText.setText(resultSet.getString("CDB"));
                                                                detailsDatabaseConId1Text.setText(resultSet.getString("CON_ID"));
                                                                detailsDatabasePendingRoleChangeTasksText.setText(resultSet.getString("PENDING_ROLE_CHANGE_TASKS"));
                                                                detailsDatabaseConDbIdText.setText(resultSet.getString("CON_DBID"));
                                                                detailsDatabaseForceFullDbCachingText.setText(resultSet.getString("FORCE_FULL_DB_CACHING"));detailsDatabaseForceFullDbCachingText.setMinWidth(250);
                                                                Platform.runLater(()->{
                                                                    try{
                                                                        detailsInstanceInstanceNumberText.setText(resultSet.getString("INSTANCE_NUMBER"));
                                                                        detailsInstanceInstanceNameText.setText(resultSet.getString("INSTANCE_NAME"));
                                                                        detailsInstanceHostNameText.setText(resultSet.getString("HOST_NAME"));
                                                                        detailsInstanceVersionText.setText(resultSet.getString("VERSION"));
                                                                        detailsInstanceStartupTimeText.setText(resultSet.getString("STARTUP_TIME"));
                                                                        detailsInstanceStatusText.setText(resultSet.getString("STATUS"));
                                                                        detailsInstanceParallelText.setText(resultSet.getString("PARALLEL"));
                                                                        detailsInstanceThreadText.setText(resultSet.getString("THREAD#"));
                                                                        detailsInstanceArchiverText.setText(resultSet.getString("ARCHIVER"));
                                                                        detailsInstanceLogSwitchWaitText.setText(resultSet.getString("LOG_SWITCH_WAIT"));
                                                                        detailsInstanceLoginsText.setText(resultSet.getString("LOGINS"));
                                                                        detailsInstanceShutdownPendingText.setText(resultSet.getString("SHUTDOWN_PENDING"));
                                                                        detailsInstanceDatabaseStatusText.setText(resultSet.getString("DATABASE_STATUS"));
                                                                        detailsInstanceInstanceRoleText.setText(resultSet.getString("INSTANCE_ROLE"));
                                                                        detailsInstanceActiveStateText.setText(resultSet.getString("ACTIVE_STATUS"));
                                                                        detailsInstanceBlockedText.setText(resultSet.getString("BLOCKED"));
                                                                        detailsInstanceConIdText.setText(resultSet.getString("CON_ID"));
                                                                        detailsInstanceInstanceModeText.setText(resultSet.getString("INSTANCE_MODE"));
                                                                        detailsInstanceEditionText.setText(resultSet.getString("EDITION"));
                                                                        detailsInstanceFamilyText.setText(resultSet.getString("FAMILY"));
                                                                        detailsInstanceDatabaseTypeText.setText(resultSet.getString("DATABASE_TYPE"));

                                                                        detailsDatabaseDBIDText.setText(resultSet.getString("DBID"));
                                                                        detailsDatabaseNameText.setText(resultSet.getString("NAME"));
                                                                        detailsDatabaseCreatedText.setText(resultSet.getString("CREATED"));
                                                                        detailsDatabaseResetLogsChangeText.setText(resultSet.getString("RESETLOGS_CHANGE#"));
                                                                        detailsDatabaseResetLogsTimeText.setText(resultSet.getString("RESETLOGS_TIME"));
                                                                        detailsDatabasePriorResetLogsChangeText.setText(resultSet.getString("PRIOR_RESETLOGS_CHANGE#"));
                                                                        detailsDatabasePriorResetLogsTimeText.setText(resultSet.getString("PRIOR_RESETLOGS_TIME"));
                                                                        detailsDatabaseLogModeText.setText(resultSet.getString("LOG_MODE"));
                                                                        detailsDatabaseCheckpointChangeText.setText(resultSet.getString("CHECKPOINT_CHANGE#"));
                                                                        detailsDatabaseArchiveChangeText.setText(resultSet.getString("ARCHIVE_CHANGE#"));
                                                                        detailsDatabaseControlFileTypeText.setText(resultSet.getString("CONTROLFILE_TYPE"));
                                                                        detailsDatabaseControlFileCreatedText.setText(resultSet.getString("CONTROLFILE_CREATED"));
                                                                        detailsDatabaseControlFileSequenceText.setText(resultSet.getString("CONTROLFILE_SEQUENCE#"));
                                                                        detailsDatabaseControlFileChangeText.setText(resultSet.getString("CONTROLFILE_CHANGE#"));
                                                                        detailsDatabaseControlFileTimeText.setText(resultSet.getString("CONTROLFILE_TIME"));
                                                                        detailsDatabaseOpenResetLogsText.setText(resultSet.getString("OPEN_RESETLOGS"));
                                                                        detailsDatabaseVersionTimeText.setText(resultSet.getString("VERSION_TIME"));
                                                                        detailsDatabaseOpenModeText.setText(resultSet.getString("OPEN_MODE"));
                                                                        detailsDatabaseProtectionModeText.setText(resultSet.getString("PROTECTION_MODE"));
                                                                        detailsDatabaseProtectionLevelText.setText(resultSet.getString("PROTECTION_LEVEL"));
                                                                        detailsDatabaseRemoteArchiveText.setText(resultSet.getString("REMOTE_ARCHIVE"));
                                                                        detailsDatabaseActivationText.setText(resultSet.getString("ACTIVATION#"));
                                                                        detailsDatabaseSwitchoverText.setText(resultSet.getString("SWITCHOVER#"));
                                                                        detailsDatabaseDatabaseRoleText.setText(resultSet.getString("DATABASE_ROLE"));
                                                                        detailsDatabaseArchivelogChangeText.setText(resultSet.getString("ARCHIVELOG_CHANGE#"));
                                                                        detailsDatabaseArchivelogCompressionText.setText(resultSet.getString("ARCHIVELOG_COMPRESSION"));
                                                                        detailsDatabaseSwitchoverStatusText.setText(resultSet.getString("SWITCHOVER_STATUS"));
                                                                        detailsDatabaseDataGuardBrokerText.setText(resultSet.getString("DATAGUARD_BROKER"));
                                                                        detailsDatabaseGuardStatusText.setText(resultSet.getString("GUARD_STATUS"));
                                                                        detailsDatabaseSupplementalLogDataMinText.setText(resultSet.getString("SUPPLEMENTAL_LOG_DATA_MIN"));
                                                                        detailsDatabaseSupplementalLogDataPKText.setText(resultSet.getString("SUPPLEMENTAL_LOG_DATA_PK"));
                                                                        detailsDatabaseSupplementalLogDataUIText.setText(resultSet.getString("SUPPLEMENTAL_LOG_DATA_UI"));
                                                                        detailsDatabaseForceLoggingText.setText(resultSet.getString("FORCE_LOGGING"));
                                                                        detailsDatabasePlatformIdText.setText(resultSet.getString("PLATFORM_ID"));
                                                                        detailsDatabasePlatformNameText.setText(resultSet.getString("PLATFORM_NAME"));
                                                                        detailsDatabaseRecoveryTargetIncarnationText.setText(resultSet.getString("RECOVERY_TARGET_INCARNATION#"));
                                                                        detailsDatabaseLastOpenIncarnationText.setText(resultSet.getString("LAST_OPEN_INCARNATION#"));
                                                                        detailsDatabaseCurrentSCNText.setText(resultSet.getString("CURRENT_SCN"));
                                                                        detailsDatabaseFlashbackOnText.setText(resultSet.getString("FLASHBACK_ON"));
                                                                        detailsDatabaseSupplementalLogDataFKText.setText(resultSet.getString("SUPPLEMENTAL_LOG_DATA_FK"));
                                                                        detailsDatabaseSupplementalLogDataALLText.setText(resultSet.getString("SUPPLEMENTAL_LOG_DATA_ALL"));
                                                                        detailsDatabaseDBUniqueNameText.setText(resultSet.getString("DB_UNIQUE_NAME"));
                                                                        detailsDatabaseStandbyBecamePrimarySCNText.setText(resultSet.getString("STANDBY_BECAME_PRIMARY_SCN"));
                                                                        detailsDatabaseFSFailoverStatusText.setText(resultSet.getString("FS_FAILOVER_STATUS"));
                                                                        detailsDatabaseFSFailoverCurrentTargetText.setText(resultSet.getString("FS_FAILOVER_CURRENT_TARGET"));
                                                                        detailsDatabaseFSFailoverThresholdText.setText(resultSet.getString("FS_FAILOVER_THRESHOLD"));
                                                                        detailsDatabaseFSFailoverObserverPresentText.setText(resultSet.getString("FS_FAILOVER_OBSERVER_PRESENT"));
                                                                        detailsDatabaseFSFailoverObserverHostText.setText(resultSet.getString("FS_FAILOVER_OBSERVER_HOST"));
                                                                        detailsDatabaseControlfileConvertedText.setText(resultSet.getString("CONTROLFILE_CONVERTED"));
                                                                        detailsDatabasePrimaryDBUniqueNameText.setText(resultSet.getString("PRIMARY_DB_UNIQUE_NAME"));
                                                                        detailsDatabaseSupplementalLogDataPLText.setText(resultSet.getString("SUPPLEMENTAL_LOG_DATA_PL"));
                                                                        detailsDatabaseMinRequiredCaptureChangeText.setText(resultSet.getString("MIN_REQUIRED_CAPTURE_CHANGE"));
                                                                        detailsDatabaseCDBText.setText(resultSet.getString("CDB"));
                                                                        detailsDatabaseConId1Text.setText(resultSet.getString("CON_ID_1"));
                                                                        detailsDatabasePendingRoleChangeTasksText.setText(resultSet.getString("PENDING_ROLE_CHANGE_TASKS"));
                                                                        detailsDatabaseConDbIdText.setText(resultSet.getString("CON_DBID"));
                                                                        detailsDatabaseForceFullDbCachingText.setText(resultSet.getString("FORCE_FULL_DB_CACHING"));detailsDatabaseForceFullDbCachingText.setMinWidth(350);
                                                                    }catch(Exception e){System.out.println(e);}
                                                                });
                                                            }//ewtutaj
                                                        }}
                                                    catch(Exception e){

                                                    }
                                                    return null;
                                                }
                                            };
                                            Thread thGetDatabaseDetails = new Thread(getDatabaseDetails);
                                            thGetDatabaseDetails.setDaemon(true);
                                            thGetDatabaseDetails.start();
                                            //databaseDetailsAlert.getDialogPane().setContent(databaseDetailsGridPane);
                                            databaseDetailsGridPane.setHgap(5);databaseDetailsGridPane.setVgap(5);
                                            databaseDetailsScrollPane.setContent(databaseDetailsGridPane);
                                            databaseDetailsAlert.getDialogPane().setContent(databaseDetailsScrollPane);
                                            databaseDetailsAlert.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
                                            databaseDetailsAlert.show();
                                        }else{
                                            Alert notConnectedAlert = new Alert(Alert.AlertType.ERROR);
                                            notConnectedAlert.setTitle("Not connected to database!");
                                            notConnectedAlert.setHeaderText(null);
                                            notConnectedAlert.setContentText("Connection to database '"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPort()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID()+"' is not reachable");
                                            notConnectedAlert.show();
                                        }
                                    }catch(Exception e){
                                        Alert notConnectedAlert = new Alert(Alert.AlertType.ERROR);
                                        notConnectedAlert.setTitle("Not connected to database!");
                                        notConnectedAlert.setHeaderText(null);
                                        notConnectedAlert.setContentText("Connection to database '"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPort()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID()+"'\nis not reachable\n\n"+"Error details: \n"+e.toString());
                                        notConnectedAlert.show();
                                    }
                                }else{
                                    Alert notConnectedAlert = new Alert(Alert.AlertType.ERROR);
                                    notConnectedAlert.setTitle("Database connection timeout!");
                                    notConnectedAlert.setHeaderText(null);
                                    notConnectedAlert.setContentText("Connection to database '"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPort()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID()+"'\nis not reachable\n\n"+"Connection timed out ("+connectionTimeoutSeconds+" seconds ping limit)");
                                    notConnectedAlert.show();
                                }

                            }
                        });




                        spfileButton.setOnAction(new EventHandler<ActionEvent>(){
                            public void handle(ActionEvent event){
                                if(pingTestToServer(connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress())){
                                    try{
                                        String URL = "jdbc:oracle:thin:@"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPort()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID();
                                        java.sql.Connection connection = DriverManager.getConnection(URL, connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionUser(), connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPassword());
                                        if(!connection.isClosed()){
                                            Dialog databaseSpfileAlert = new Dialog();
                                            databaseSpfileAlert.setTitle("Database from connection: "+comboBoxConnectionsList.getValue().toString()+" | Parameters from spfile");
                                            databaseSpfileAlert.setWidth(800);databaseSpfileAlert.setHeight(600);
                                            ScrollPane databaseSpfileScrollPane = new ScrollPane();
                                            databaseSpfileScrollPane.setMinWidth(775);databaseSpfileScrollPane.setMinHeight(600);
                                            GridPane databaseSpfileGridPane = new GridPane();
                                            databaseSpfileGridPane.setMinWidth(750);databaseSpfileScrollPane.setMinHeight(600);
                                            TextField spfileParameter = new TextField("Current parameters from spfile ("+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPort()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID()+")");spfileParameter.setEditable(false);spfileParameter.setStyle("-fx-font-weight:bold;");spfileParameter.setBackground(null);databaseSpfileGridPane.add(spfileParameter,0,0,3,1);
                                            TextField spfileParameterName = new TextField("NAME");spfileParameterName.setEditable(false);spfileParameterName.setStyle("-fx-font-weight:bold;");spfileParameterName.setBackground(null);databaseSpfileGridPane.add(spfileParameterName,0,1);
                                            TextField spfileParameterValue = new TextField("VALUE");spfileParameterValue.setEditable(false);spfileParameterValue.setStyle("-fx-font-weight:bold;");spfileParameterValue.setBackground(null);databaseSpfileGridPane.add(spfileParameterValue,1,1);
                                            TextField spfileParameterDescription = new TextField("DESCRIPTION");spfileParameterDescription.setEditable(false);spfileParameterDescription.setStyle("-fx-font-weight:bold;");spfileParameterDescription.setBackground(null);databaseSpfileGridPane.add(spfileParameterDescription,2,1);

                                            Task<Void> getParametersFromSpfile = new Task<Void>() {
                                                @Override
                                                protected Void call() throws Exception {
                                                    String query = "select NAME,VALUE,DESCRIPTION from v$parameter ORDER BY NAME ASC";
                                                    String URL = "jdbc:oracle:thin:@"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPort()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID();
                                                    java.sql.Connection connection = DriverManager.getConnection(URL, connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionUser(), connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPassword());
                                                    try{
                                                        if(!connection.isClosed()) {

                                                            System.out.println("woooo");
                                                            Statement statement = connection.createStatement();
                                                            ResultSet resultSet = statement.executeQuery(query);
                                                            int i=2;
                                                            while(resultSet.next()){
                                                                TextField spfileNameColumn = new TextField(resultSet.getString("NAME"));spfileNameColumn.setEditable(false);
                                                                TextField spfileValueColumn = new TextField(resultSet.getString("VALUE"));spfileValueColumn.setEditable(false);
                                                                TextField spfileDescriptionColumn = new TextField(resultSet.getString("DESCRIPTION"));spfileDescriptionColumn.setEditable(false);spfileDescriptionColumn.setMinWidth(420);
                                                                databaseSpfileGridPane.add(spfileNameColumn,0,i);
                                                                databaseSpfileGridPane.add(spfileValueColumn,1,i);
                                                                databaseSpfileGridPane.add(spfileDescriptionColumn,2,i);
                                                                i++;
                                                            }

                                                            Platform.runLater(()->{
                                                                databaseSpfileScrollPane.setContent(databaseSpfileGridPane);
                                                            });
                                                        }
                                                    }catch(Exception e){}
                                                    return null;
                                                }
                                            };
                                            Thread thgetParametersFromSpfile = new Thread(getParametersFromSpfile);
                                            thgetParametersFromSpfile.setDaemon(true);
                                            thgetParametersFromSpfile.start();
                                            //databaseSpfileGridPane.setHgap(5);databaseSpfileGridPane.setVgap(5);
                                            //databaseSpfileScrollPane.setContent(databaseSpfileGridPane);
                                            databaseSpfileAlert.getDialogPane().setContent(databaseSpfileScrollPane);
                                            databaseSpfileAlert.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
                                            databaseSpfileAlert.show();
                                        }else{
                                            Alert notConnectedAlert = new Alert(Alert.AlertType.ERROR);
                                            notConnectedAlert.setTitle("Not connected to database!");
                                            notConnectedAlert.setHeaderText(null);
                                            notConnectedAlert.setContentText("Connection to database '"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPort()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID()+"' is not reachable");
                                            notConnectedAlert.show();
                                        }
                                    }catch(Exception e){
                                        Alert notConnectedAlert = new Alert(Alert.AlertType.ERROR);
                                        notConnectedAlert.setTitle("Not connected to database!");
                                        notConnectedAlert.setHeaderText(null);
                                        notConnectedAlert.setContentText("Connection to database '"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPort()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID()+"'\nis not reachable\n\n"+"Error details: \n"+e.toString());
                                        notConnectedAlert.show();
                                    }
                                }else{
                                    Alert notConnectedAlert = new Alert(Alert.AlertType.ERROR);
                                    notConnectedAlert.setTitle("Database connection timeout!");
                                    notConnectedAlert.setHeaderText(null);
                                    notConnectedAlert.setContentText("Connection to database '"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPort()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID()+"'\nis not reachable\n\n"+"Connection timed out ("+connectionTimeoutSeconds+" seconds ping limit)");
                                    notConnectedAlert.show();
                                }

                            }
                        });






                        tablespacesButton.setOnAction(new EventHandler<ActionEvent>(){
                            public void handle(ActionEvent event){
                                try{
                                    InetAddress inetAddress = InetAddress.getByName(connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress());
                                    if (inetAddress.isReachable(connectionTimeoutSeconds*1000)) {
                                        try{
                                            String URL = "jdbc:oracle:thin:@"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPort()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID();
                                            java.sql.Connection connection = DriverManager.getConnection(URL, connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionUser(), connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPassword());
                                            if(!connection.isClosed()){
                                                String querySelectAllTablespaces = "select TABLESPACE_NAME from DBA_TABLESPACES order by TABLESPACE_NAME ASC";
                                                Statement statement = connection.createStatement();
                                                ResultSet resultSetTablespacesList = statement.executeQuery(querySelectAllTablespaces);
                                                List<String> tablespacesNameList = new ArrayList<>();
                                                while(resultSetTablespacesList.next()){
                                                    tablespacesNameList.add(resultSetTablespacesList.getString("TABLESPACE_NAME"));
                                                }
                                                System.out.println(tablespacesNameList.toString());
                                                Dialog tablespaceDialog = new Dialog();
                                                tablespaceDialog.setTitle("Tablespace details");
                                                tablespaceDialog.setHeaderText(null);
                                                tablespaceDialog.setWidth(175);tablespaceDialog.setHeight(125);
                                                GridPane tablespaceDialogGridPane = new GridPane();
                                                tablespaceDialogGridPane.setPrefWidth(175);tablespaceDialogGridPane.setPrefHeight(125);

                                                Label selectTablespace = new Label("Select Tablespace:");
                                                tablespaceDialogGridPane.add(selectTablespace,0,0);
                                                ComboBox comboBoxTablespaceList = new ComboBox();
                                                comboBoxTablespaceList.setPromptText("Select tablespace");
                                                for(String currentTablespaceFromTablespacesNameList : tablespacesNameList)
                                                {
                                                    comboBoxTablespaceList.getItems().add(currentTablespaceFromTablespacesNameList);
                                                }
                                                tablespaceDialogGridPane.add(comboBoxTablespaceList,0,1);

                                                BooleanBinding selectTablespaceValidation = Bindings.createBooleanBinding(() -> tablespaceInComboBoxIsNotSelected(comboBoxTablespaceList));
                                                if(selectTablespaceValidation.get()){
                                                    comboBoxTablespaceList.setOnAction((comboBoxTablespaceListAction) -> {
                                                        tablespaceDialog.setWidth(1600);tablespaceDialog.setHeight(600);
                                                        tablespaceDialogGridPane.setPrefWidth(1600);tablespaceDialogGridPane.setPrefHeight(600);
                                                        ScrollPane tablespaceDetailsScrollPane = new ScrollPane();
                                                        GridPane tablespaceDetailsGridPane = new GridPane();
                                                        Label tablespaceDetails = new Label("Tablespace ["+comboBoxTablespaceList.getValue().toString()+"] details");
                                                        tablespaceDetails.setStyle("-fx-font-weight:bold;");
                                                        tablespaceDetailsGridPane.setMaxWidth(350);
                                                        tablespaceDetailsScrollPane.setMaxWidth(375);
                                                        tablespaceDetailsGridPane.setMinWidth(350);
                                                        tablespaceDetailsScrollPane.setMinWidth(375);
                                                        tablespaceDetailsGridPane.add(tablespaceDetails,0,0,2,1);


                                                        ScrollPane tablespaceDatafilesDetailsScrollPane = new ScrollPane();
                                                        GridPane tablespaceDatafilesDetailsGridPane = new GridPane();
                                                        tablespaceDetailsGridPane.setPrefWidth(1150);
                                                        tablespaceDetailsScrollPane.setPrefWidth(1150);
                                                        Label tablespaceDatafilesDetails = new Label("Tablespace ["+comboBoxTablespaceList.getValue().toString()+"] datafiles details");
                                                        tablespaceDatafilesDetails.setStyle("-fx-font-weight:bold;");
                                                        tablespaceDatafilesDetailsGridPane.add(tablespaceDatafilesDetails,0,0,12,1);
                                                        TextField FILE_NAME = new TextField("File Name");FILE_NAME.setEditable(false);FILE_NAME.setStyle("-fx-font-weight:bold;");tablespaceDatafilesDetailsGridPane.add(FILE_NAME,0,1);FILE_NAME.setPrefWidth(500);
                                                        TextField FILE_ID = new TextField("File ID");FILE_ID.setEditable(false);FILE_ID.setStyle("-fx-font-weight:bold;");tablespaceDatafilesDetailsGridPane.add(FILE_ID,1,1);FILE_ID.setMaxWidth(100);
                                                        TextField TOTAL_MB = new TextField("Total (MB)");TOTAL_MB.setEditable(false);TOTAL_MB.setStyle("-fx-font-weight:bold;");tablespaceDatafilesDetailsGridPane.add(TOTAL_MB,2,1);TOTAL_MB.setMaxWidth(100);
                                                        TextField USED_MB = new TextField("Used (MB)");USED_MB.setEditable(false);USED_MB.setStyle("-fx-font-weight:bold;");tablespaceDatafilesDetailsGridPane.add(USED_MB,3,1);USED_MB.setMaxWidth(100);
                                                        TextField FREE_MB = new TextField("Free (MB)");FREE_MB.setEditable(false);FREE_MB.setStyle("-fx-font-weight:bold;");tablespaceDatafilesDetailsGridPane.add(FREE_MB,4,1);FREE_MB.setMaxWidth(100);
                                                        TextField PCT_FREE = new TextField("Free (%)");PCT_FREE.setEditable(false);PCT_FREE.setStyle("-fx-font-weight:bold;");tablespaceDatafilesDetailsGridPane.add(PCT_FREE,5,1);PCT_FREE.setMaxWidth(100);
                                                        TextField AUTOEXTENSIBLE = new TextField("Autoextensible");AUTOEXTENSIBLE.setEditable(false);AUTOEXTENSIBLE.setStyle("-fx-font-weight:bold;");tablespaceDatafilesDetailsGridPane.add(AUTOEXTENSIBLE,6,1);AUTOEXTENSIBLE.setMaxWidth(100);
                                                        TextField MAX_MB = new TextField("Max. (MB)");MAX_MB.setEditable(false);MAX_MB.setStyle("-fx-font-weight:bold;");tablespaceDatafilesDetailsGridPane.add(MAX_MB,7,1);MAX_MB.setMaxWidth(100);
                                                        TextField BLOCKS = new TextField("Blocks");BLOCKS.setEditable(false);BLOCKS.setStyle("-fx-font-weight:bold;");tablespaceDatafilesDetailsGridPane.add(BLOCKS,8,1);BLOCKS.setMaxWidth(100);
                                                        TextField MAXBLOCKS = new TextField("Max. Blocks");MAXBLOCKS.setEditable(false);MAXBLOCKS.setStyle("-fx-font-weight:bold;");tablespaceDatafilesDetailsGridPane.add(MAXBLOCKS,9,1);MAXBLOCKS.setMaxWidth(100);
                                                        TextField STATUS = new TextField("Status");STATUS.setEditable(false);STATUS.setStyle("-fx-font-weight:bold;");tablespaceDatafilesDetailsGridPane.add(STATUS,10,1);STATUS.setMaxWidth(100);
                                                        TextField ONLINE_STATUS = new TextField("Online Status");ONLINE_STATUS.setEditable(false);ONLINE_STATUS.setStyle("-fx-font-weight:bold;");tablespaceDatafilesDetailsGridPane.add(ONLINE_STATUS,11,1);ONLINE_STATUS.setMaxWidth(100);
                                                        try{
                                                            String querySelectTablespaceDetails = "select * from DBA_TABLESPACES where TABLESPACE_NAME='"+comboBoxTablespaceList.getValue().toString()+"'";
                                                            ResultSet resultSetTablespaceDetails = statement.executeQuery(querySelectTablespaceDetails);
                                                            while(resultSetTablespaceDetails.next()){
                                                                TextField TABLESPACE_NAME = new TextField("TABLESPACE_NAME");TABLESPACE_NAME.setEditable(false);tablespaceDetailsGridPane.add(TABLESPACE_NAME,0,1);TABLESPACE_NAME.setPrefWidth(200);
                                                                TextField BLOCK_SIZE = new TextField("BLOCK_SIZE");BLOCK_SIZE.setEditable(false);tablespaceDetailsGridPane.add(BLOCK_SIZE,0,2);
                                                                TextField INITIAL_EXTENT = new TextField("INITIAL_EXTENT");INITIAL_EXTENT.setEditable(false);tablespaceDetailsGridPane.add(INITIAL_EXTENT,0,3);
                                                                TextField NEXT_EXTENT = new TextField("NEXT_EXTENT");NEXT_EXTENT.setEditable(false);tablespaceDetailsGridPane.add(NEXT_EXTENT,0,4);
                                                                TextField MIN_EXTENTS = new TextField("MIN_EXTENTS");MIN_EXTENTS.setEditable(false);tablespaceDetailsGridPane.add(MIN_EXTENTS,0,5);
                                                                TextField MAX_EXTENTS = new TextField("MAX_EXTENTS");MAX_EXTENTS.setEditable(false);tablespaceDetailsGridPane.add(MAX_EXTENTS,0,6);
                                                                TextField MAX_SIZE = new TextField("MAX_SIZE");MAX_SIZE.setEditable(false);tablespaceDetailsGridPane.add(MAX_SIZE,0,7);
                                                                TextField PCT_INCREASE = new TextField("PCT_INCREASE");PCT_INCREASE.setEditable(false);tablespaceDetailsGridPane.add(PCT_INCREASE,0,8);
                                                                TextField MIN_EXTLEN = new TextField("MIN_EXTLEN");MIN_EXTLEN.setEditable(false);tablespaceDetailsGridPane.add(MIN_EXTLEN,0,9);
                                                                TextField DF__STATUS = new TextField("STATUS");DF__STATUS.setEditable(false);tablespaceDetailsGridPane.add(DF__STATUS,0,10);
                                                                TextField CONTENTS = new TextField("CONTENTS");CONTENTS.setEditable(false);tablespaceDetailsGridPane.add(CONTENTS,0,11);
                                                                TextField LOGGING = new TextField("LOGGING");LOGGING.setEditable(false);tablespaceDetailsGridPane.add(LOGGING,0,12);
                                                                TextField FORCE_LOGGING = new TextField("FORCE_LOGGING");FORCE_LOGGING.setEditable(false);tablespaceDetailsGridPane.add(FORCE_LOGGING,0,13);
                                                                TextField EXTENT_MANAGEMENT = new TextField("EXTENT_MANAGEMENT");EXTENT_MANAGEMENT.setEditable(false);tablespaceDetailsGridPane.add(EXTENT_MANAGEMENT,0,14);
                                                                TextField ALLOCATION_TYPE = new TextField("ALLOCATION_TYPE");ALLOCATION_TYPE.setEditable(false);tablespaceDetailsGridPane.add(ALLOCATION_TYPE,0,15);
                                                                TextField PLUGGED_IN = new TextField("PLUGGED_IN");PLUGGED_IN.setEditable(false);tablespaceDetailsGridPane.add(PLUGGED_IN,0,16);
                                                                TextField SEGMENT_SPACE_MANAGEMENT = new TextField("SEGMENT_SPACE_MANAGEMENT");SEGMENT_SPACE_MANAGEMENT.setEditable(false);tablespaceDetailsGridPane.add(SEGMENT_SPACE_MANAGEMENT,0,17);
                                                                TextField DEF_TAB_COMPRESSION = new TextField("DEF_TAB_COMPRESSION");DEF_TAB_COMPRESSION.setEditable(false);tablespaceDetailsGridPane.add(DEF_TAB_COMPRESSION,0,18);
                                                                TextField RETENTION = new TextField("RETENTION");RETENTION.setEditable(false);tablespaceDetailsGridPane.add(RETENTION,0,19);
                                                                TextField BIGFILE = new TextField("BIGFILE");BIGFILE.setEditable(false);tablespaceDetailsGridPane.add(BIGFILE,0,20);
                                                                TextField PREDICATE_EVALUATION = new TextField("PREDICATE_EVALUATION");PREDICATE_EVALUATION.setEditable(false);tablespaceDetailsGridPane.add(PREDICATE_EVALUATION,0,21);
                                                                TextField ENCRYPTED = new TextField("ENCRYPTED");ENCRYPTED.setEditable(false);tablespaceDetailsGridPane.add(ENCRYPTED,0,22);
                                                                TextField COMPRESS_FOR = new TextField("COMPRESS_FOR");COMPRESS_FOR.setEditable(false);tablespaceDetailsGridPane.add(COMPRESS_FOR,0,23);
                                                                TextField DEF_INMEMORY = new TextField("DEF_INMEMORY");DEF_INMEMORY.setEditable(false);tablespaceDetailsGridPane.add(DEF_INMEMORY,0,24);
                                                                TextField DEF_INMEMORY_PRIORITY = new TextField("DEF_INMEMORY_PRIORITY");DEF_INMEMORY_PRIORITY.setEditable(false);tablespaceDetailsGridPane.add(DEF_INMEMORY_PRIORITY,0,25);
                                                                TextField DEF_INMEMORY_DISTRIBUTE = new TextField("DEF_INMEMORY_DISTRIBUTE");DEF_INMEMORY_DISTRIBUTE.setEditable(false);tablespaceDetailsGridPane.add(DEF_INMEMORY_DISTRIBUTE,0,26);
                                                                TextField DEF_INMEMORY_COMPRESSION = new TextField("DEF_INMEMORY_COMPRESSION");DEF_INMEMORY_COMPRESSION.setEditable(false);tablespaceDetailsGridPane.add(DEF_INMEMORY_COMPRESSION,0,27);
                                                                TextField DEF_INMEMORY_DUPLICATE = new TextField("DEF_INMEMORY_DUPLICATE");DEF_INMEMORY_DUPLICATE.setEditable(false);tablespaceDetailsGridPane.add(DEF_INMEMORY_DUPLICATE,0,28);
                                                                TextField SHARED = new TextField("SHARED");SHARED.setEditable(false);tablespaceDetailsGridPane.add(SHARED,0,29);
                                                                TextField DEF_INDEX_COMPRESSION = new TextField("DEF_INDEX_COMPRESSION");DEF_INDEX_COMPRESSION.setEditable(false);tablespaceDetailsGridPane.add(DEF_INDEX_COMPRESSION,0,30);
                                                                TextField INDEX_COMPRESS_FOR = new TextField("INDEX_COMPRESS_FOR");INDEX_COMPRESS_FOR.setEditable(false);tablespaceDetailsGridPane.add(INDEX_COMPRESS_FOR,0,31);
                                                                TextField DEF_CELLMEMORY = new TextField("DEF_CELLMEMORY");DEF_CELLMEMORY.setEditable(false);tablespaceDetailsGridPane.add(DEF_CELLMEMORY,0,32);
                                                                TextField DEF_INMEMORY_SERVICE = new TextField("DEF_INMEMORY_SERVICE");DEF_INMEMORY_SERVICE.setEditable(false);tablespaceDetailsGridPane.add(DEF_INMEMORY_SERVICE,0,33);
                                                                TextField DEF_INMEMORY_SERVICE_NAME = new TextField("DEF_INMEMORY_SERVICE_NAME");DEF_INMEMORY_SERVICE_NAME.setEditable(false);tablespaceDetailsGridPane.add(DEF_INMEMORY_SERVICE_NAME,0,34);
                                                                TextField LOST_WRITE_PROTECT = new TextField("LOST_WRITE_PROTECT");LOST_WRITE_PROTECT.setEditable(false);tablespaceDetailsGridPane.add(LOST_WRITE_PROTECT,0,35);
                                                                TextField CHUNK_TABLESPACE = new TextField("CHUNK_TABLESPACE");CHUNK_TABLESPACE.setEditable(false);tablespaceDetailsGridPane.add(CHUNK_TABLESPACE,0,36);

                                                                TextField TABLESPACE_NAME__TEXT = new TextField(resultSetTablespaceDetails.getString("TABLESPACE_NAME"));TABLESPACE_NAME__TEXT.setEditable(false);tablespaceDetailsGridPane.add(TABLESPACE_NAME__TEXT,1,1);TABLESPACE_NAME__TEXT.setPrefWidth(150);
                                                                TextField BLOCK_SIZE__TEXT = new TextField(resultSetTablespaceDetails.getString("BLOCK_SIZE"));BLOCK_SIZE__TEXT.setEditable(false);tablespaceDetailsGridPane.add(BLOCK_SIZE__TEXT,1,2);
                                                                TextField INITIAL_EXTENT__TEXT = new TextField(resultSetTablespaceDetails.getString("INITIAL_EXTENT"));INITIAL_EXTENT__TEXT.setEditable(false);tablespaceDetailsGridPane.add(INITIAL_EXTENT__TEXT,1,3);
                                                                TextField NEXT_EXTENT__TEXT = new TextField(resultSetTablespaceDetails.getString("NEXT_EXTENT"));NEXT_EXTENT__TEXT.setEditable(false);tablespaceDetailsGridPane.add(NEXT_EXTENT__TEXT,1,4);
                                                                TextField MIN_EXTENTS__TEXT = new TextField(resultSetTablespaceDetails.getString("MIN_EXTENTS"));MIN_EXTENTS__TEXT.setEditable(false);tablespaceDetailsGridPane.add(MIN_EXTENTS__TEXT,1,5);
                                                                TextField MAX_EXTENTS__TEXT = new TextField(resultSetTablespaceDetails.getString("MAX_EXTENTS"));MAX_EXTENTS__TEXT.setEditable(false);tablespaceDetailsGridPane.add(MAX_EXTENTS__TEXT,1,6);
                                                                TextField MAX_SIZE__TEXT = new TextField(resultSetTablespaceDetails.getString("MAX_SIZE"));MAX_SIZE__TEXT.setEditable(false);tablespaceDetailsGridPane.add(MAX_SIZE__TEXT,1,7);
                                                                TextField PCT_INCREASE__TEXT = new TextField(resultSetTablespaceDetails.getString("PCT_INCREASE"));PCT_INCREASE__TEXT.setEditable(false);tablespaceDetailsGridPane.add(PCT_INCREASE__TEXT,1,8);
                                                                TextField MIN_EXTLEN__TEXT = new TextField(resultSetTablespaceDetails.getString("MIN_EXTLEN"));MIN_EXTLEN__TEXT.setEditable(false);tablespaceDetailsGridPane.add(MIN_EXTLEN__TEXT,1,9);
                                                                TextField STATUS__TEXT = new TextField(resultSetTablespaceDetails.getString("STATUS"));STATUS__TEXT.setEditable(false);tablespaceDetailsGridPane.add(STATUS__TEXT,1,10);
                                                                TextField CONTENTS__TEXT = new TextField(resultSetTablespaceDetails.getString("CONTENTS"));CONTENTS__TEXT.setEditable(false);tablespaceDetailsGridPane.add(CONTENTS__TEXT,1,11);
                                                                TextField LOGGING__TEXT = new TextField(resultSetTablespaceDetails.getString("LOGGING"));LOGGING__TEXT.setEditable(false);tablespaceDetailsGridPane.add(LOGGING__TEXT,1,12);
                                                                TextField FORCE_LOGGING__TEXT = new TextField(resultSetTablespaceDetails.getString("FORCE_LOGGING"));FORCE_LOGGING__TEXT.setEditable(false);tablespaceDetailsGridPane.add(FORCE_LOGGING__TEXT,1,13);
                                                                TextField EXTENT_MANAGEMENT__TEXT = new TextField(resultSetTablespaceDetails.getString("EXTENT_MANAGEMENT"));EXTENT_MANAGEMENT__TEXT.setEditable(false);tablespaceDetailsGridPane.add(EXTENT_MANAGEMENT__TEXT,1,14);
                                                                TextField ALLOCATION_TYPE__TEXT = new TextField(resultSetTablespaceDetails.getString("ALLOCATION_TYPE"));ALLOCATION_TYPE__TEXT.setEditable(false);tablespaceDetailsGridPane.add(ALLOCATION_TYPE__TEXT,1,15);
                                                                TextField PLUGGED_IN__TEXT = new TextField(resultSetTablespaceDetails.getString("PLUGGED_IN"));PLUGGED_IN__TEXT.setEditable(false);tablespaceDetailsGridPane.add(PLUGGED_IN__TEXT,1,16);
                                                                TextField SEGMENT_SPACE_MANAGEMENT__TEXT = new TextField(resultSetTablespaceDetails.getString("SEGMENT_SPACE_MANAGEMENT"));SEGMENT_SPACE_MANAGEMENT__TEXT.setEditable(false);tablespaceDetailsGridPane.add(SEGMENT_SPACE_MANAGEMENT__TEXT,1,17);
                                                                TextField DEF_TAB_COMPRESSION__TEXT = new TextField(resultSetTablespaceDetails.getString("DEF_TAB_COMPRESSION"));DEF_TAB_COMPRESSION__TEXT.setEditable(false);tablespaceDetailsGridPane.add(DEF_TAB_COMPRESSION__TEXT,1,18);
                                                                TextField RETENTION__TEXT = new TextField(resultSetTablespaceDetails.getString("RETENTION"));RETENTION__TEXT.setEditable(false);tablespaceDetailsGridPane.add(RETENTION__TEXT,1,19);
                                                                TextField BIGFILE__TEXT = new TextField(resultSetTablespaceDetails.getString("BIGFILE"));BIGFILE__TEXT.setEditable(false);tablespaceDetailsGridPane.add(BIGFILE__TEXT,1,20);
                                                                TextField PREDICATE_EVALUATION__TEXT = new TextField(resultSetTablespaceDetails.getString("PREDICATE_EVALUATION"));PREDICATE_EVALUATION__TEXT.setEditable(false);tablespaceDetailsGridPane.add(PREDICATE_EVALUATION__TEXT,1,21);
                                                                TextField ENCRYPTED__TEXT = new TextField(resultSetTablespaceDetails.getString("ENCRYPTED"));ENCRYPTED__TEXT.setEditable(false);tablespaceDetailsGridPane.add(ENCRYPTED__TEXT,1,22);
                                                                TextField COMPRESS_FOR__TEXT = new TextField(resultSetTablespaceDetails.getString("COMPRESS_FOR"));COMPRESS_FOR__TEXT.setEditable(false);tablespaceDetailsGridPane.add(COMPRESS_FOR__TEXT,1,23);
                                                                TextField DEF_INMEMORY__TEXT = new TextField(resultSetTablespaceDetails.getString("DEF_INMEMORY"));DEF_INMEMORY__TEXT.setEditable(false);tablespaceDetailsGridPane.add(DEF_INMEMORY__TEXT,1,24);
                                                                TextField DEF_INMEMORY_PRIORITY__TEXT = new TextField(resultSetTablespaceDetails.getString("DEF_INMEMORY_PRIORITY"));DEF_INMEMORY_PRIORITY__TEXT.setEditable(false);tablespaceDetailsGridPane.add(DEF_INMEMORY_PRIORITY__TEXT,1,25);
                                                                TextField DEF_INMEMORY_DISTRIBUTE__TEXT = new TextField(resultSetTablespaceDetails.getString("DEF_INMEMORY_DISTRIBUTE"));DEF_INMEMORY_DISTRIBUTE__TEXT.setEditable(false);tablespaceDetailsGridPane.add(DEF_INMEMORY_DISTRIBUTE__TEXT,1,26);
                                                                TextField DEF_INMEMORY_COMPRESSION__TEXT = new TextField(resultSetTablespaceDetails.getString("DEF_INMEMORY_COMPRESSION"));DEF_INMEMORY_COMPRESSION__TEXT.setEditable(false);tablespaceDetailsGridPane.add(DEF_INMEMORY_COMPRESSION__TEXT,1,27);
                                                                TextField DEF_INMEMORY_DUPLICATE__TEXT = new TextField(resultSetTablespaceDetails.getString("DEF_INMEMORY_DUPLICATE"));DEF_INMEMORY_DUPLICATE__TEXT.setEditable(false);tablespaceDetailsGridPane.add(DEF_INMEMORY_DUPLICATE__TEXT,1,28);
                                                                TextField SHARED__TEXT = new TextField(resultSetTablespaceDetails.getString("SHARED"));SHARED__TEXT.setEditable(false);tablespaceDetailsGridPane.add(SHARED__TEXT,1,29);
                                                                TextField DEF_INDEX_COMPRESSION__TEXT = new TextField(resultSetTablespaceDetails.getString("DEF_INDEX_COMPRESSION"));DEF_INDEX_COMPRESSION__TEXT.setEditable(false);tablespaceDetailsGridPane.add(DEF_INDEX_COMPRESSION__TEXT,1,30);
                                                                TextField INDEX_COMPRESS_FOR__TEXT = new TextField(resultSetTablespaceDetails.getString("INDEX_COMPRESS_FOR"));INDEX_COMPRESS_FOR__TEXT.setEditable(false);tablespaceDetailsGridPane.add(INDEX_COMPRESS_FOR__TEXT,1,31);
                                                                TextField DEF_CELLMEMORY__TEXT = new TextField(resultSetTablespaceDetails.getString("DEF_CELLMEMORY"));DEF_CELLMEMORY__TEXT.setEditable(false);tablespaceDetailsGridPane.add(DEF_CELLMEMORY__TEXT,1,32);
                                                                TextField DEF_INMEMORY_SERVICE__TEXT = new TextField(resultSetTablespaceDetails.getString("DEF_INMEMORY_SERVICE"));DEF_INMEMORY_SERVICE__TEXT.setEditable(false);tablespaceDetailsGridPane.add(DEF_INMEMORY_SERVICE__TEXT,1,33);
                                                                TextField DEF_INMEMORY_SERVICE_NAME__TEXT = new TextField(resultSetTablespaceDetails.getString("DEF_INMEMORY_SERVICE_NAME"));DEF_INMEMORY_SERVICE_NAME__TEXT.setEditable(false);tablespaceDetailsGridPane.add(DEF_INMEMORY_SERVICE_NAME__TEXT,1,34);
                                                                TextField LOST_WRITE_PROTECT__TEXT = new TextField(resultSetTablespaceDetails.getString("LOST_WRITE_PROTECT"));LOST_WRITE_PROTECT__TEXT.setEditable(false);tablespaceDetailsGridPane.add(LOST_WRITE_PROTECT__TEXT,1,35);
                                                                TextField CHUNK_TABLESPACE__TEXT = new TextField(resultSetTablespaceDetails.getString("CHUNK_TABLESPACE"));CHUNK_TABLESPACE__TEXT.setEditable(false);tablespaceDetailsGridPane.add(CHUNK_TABLESPACE__TEXT,1,36);
                                                            }
                                                        }catch(Exception e){
                                                            Alert notConnectedAlert = new Alert(Alert.AlertType.ERROR);
                                                            notConnectedAlert.setTitle("Error!");
                                                            notConnectedAlert.setHeaderText(null);
                                                            notConnectedAlert.setContentText("Error details:\n"+e);
                                                            notConnectedAlert.show();
                                                        }


                                                        try{
                                                            String querySelectTablespaceDatafilesDetails = "select ddf.FILE_NAME as \"FILE_NAME\", ddf.FILE_ID as \"FILE_ID\", fs.tablespace_name as \"TABLESPACE_NAME\", df.totalspace \"TOTAL_MB\", (df.totalspace-fs.freespace) \"USED_MB\", fs.freespace \"FREE_MB\", round(100*(fs.freespace/df.totalspace),2) as \"PCT_FREE\", ddf.AUTOEXTENSIBLE as \"AUTOEXTENSIBLE\", round(ddf.MAXBYTES/1024/1024) as \"MAX_MB\", ddf.BLOCKS as \"BLOCKS\", ddf.MAXBLOCKS as \"MAXBLOCKS\", ddf.STATUS as \"STATUS\", ddf.ONLINE_STATUS as \"ONLINE_STATUS\" from (select tablespace_name, round(sum(bytes)/1024/1024,2) TotalSpace from dba_data_files group by tablespace_name ) df, (select tablespace_name, round(sum(bytes)/1024/1024,2) FreeSpace from dba_free_space group by tablespace_name ) fs, dba_data_files ddf where df.tablespace_name = fs.tablespace_name and ddf.tablespace_name=df.tablespace_name and ddf.tablespace_name='"+comboBoxTablespaceList.getValue().toString()+"'";
                                                            ResultSet resultSetTablespaceDatafilesDetails = statement.executeQuery(querySelectTablespaceDatafilesDetails);
                                                            int resultSetTablespaceDatafilesDetails__INDEX=2;
                                                            while(resultSetTablespaceDatafilesDetails.next()){
                                                                TextField FILE_NAME__TEXT = new TextField(resultSetTablespaceDatafilesDetails.getString("FILE_NAME"));FILE_NAME__TEXT.setEditable(false);tablespaceDatafilesDetailsGridPane.add(FILE_NAME__TEXT,0,resultSetTablespaceDatafilesDetails__INDEX);FILE_NAME__TEXT.setPrefWidth(500);
                                                                TextField FILE_ID__TEXT = new TextField(resultSetTablespaceDatafilesDetails.getString("FILE_ID"));FILE_ID__TEXT.setEditable(false);tablespaceDatafilesDetailsGridPane.add(FILE_ID__TEXT,1,resultSetTablespaceDatafilesDetails__INDEX);FILE_ID__TEXT.setMaxWidth(100);
                                                                TextField TOTAL_MB__TEXT = new TextField(resultSetTablespaceDatafilesDetails.getString("TOTAL_MB"));TOTAL_MB__TEXT.setEditable(false);tablespaceDatafilesDetailsGridPane.add(TOTAL_MB__TEXT,2,resultSetTablespaceDatafilesDetails__INDEX);TOTAL_MB__TEXT.setMaxWidth(100);
                                                                TextField USED_MB__TEXT = new TextField(resultSetTablespaceDatafilesDetails.getString("USED_MB"));USED_MB__TEXT.setEditable(false);tablespaceDatafilesDetailsGridPane.add(USED_MB__TEXT,3,resultSetTablespaceDatafilesDetails__INDEX);USED_MB__TEXT.setMaxWidth(100);
                                                                TextField FREE_MB__TEXT = new TextField(resultSetTablespaceDatafilesDetails.getString("FREE_MB"));FREE_MB__TEXT.setEditable(false);tablespaceDatafilesDetailsGridPane.add(FREE_MB__TEXT,4,resultSetTablespaceDatafilesDetails__INDEX);FREE_MB__TEXT.setMaxWidth(100);
                                                                TextField PCT_FREE__TEXT = new TextField(resultSetTablespaceDatafilesDetails.getString("PCT_FREE"));PCT_FREE__TEXT.setEditable(false);tablespaceDatafilesDetailsGridPane.add(PCT_FREE__TEXT,5,resultSetTablespaceDatafilesDetails__INDEX);PCT_FREE__TEXT.setMaxWidth(100);
                                                                TextField AUTOEXTENSIBLE__TEXT = new TextField(resultSetTablespaceDatafilesDetails.getString("Autoextensible"));AUTOEXTENSIBLE__TEXT.setEditable(false);tablespaceDatafilesDetailsGridPane.add(AUTOEXTENSIBLE__TEXT,6,resultSetTablespaceDatafilesDetails__INDEX);AUTOEXTENSIBLE__TEXT.setMaxWidth(100);
                                                                TextField MAX_MB__TEXT = new TextField(resultSetTablespaceDatafilesDetails.getString("MAX_MB"));MAX_MB__TEXT.setEditable(false);tablespaceDatafilesDetailsGridPane.add(MAX_MB__TEXT,7,resultSetTablespaceDatafilesDetails__INDEX);MAX_MB__TEXT.setMaxWidth(100);
                                                                TextField BLOCKS__TEXT = new TextField(resultSetTablespaceDatafilesDetails.getString("BLOCKS"));BLOCKS__TEXT.setEditable(false);tablespaceDatafilesDetailsGridPane.add(BLOCKS__TEXT,8,resultSetTablespaceDatafilesDetails__INDEX);BLOCKS__TEXT.setMaxWidth(100);
                                                                TextField MAXBLOCKS__TEXT = new TextField(resultSetTablespaceDatafilesDetails.getString("MAXBLOCKS"));MAXBLOCKS__TEXT.setEditable(false);tablespaceDatafilesDetailsGridPane.add(MAXBLOCKS__TEXT,9,resultSetTablespaceDatafilesDetails__INDEX);MAXBLOCKS__TEXT.setMaxWidth(100);
                                                                TextField STATUS__TEXT = new TextField(resultSetTablespaceDatafilesDetails.getString("STATUS"));STATUS__TEXT.setEditable(false);tablespaceDatafilesDetailsGridPane.add(STATUS__TEXT,10,resultSetTablespaceDatafilesDetails__INDEX);STATUS__TEXT.setMaxWidth(100);
                                                                TextField ONLINE_STATUS__TEXT = new TextField(resultSetTablespaceDatafilesDetails.getString("ONLINE_STATUS"));ONLINE_STATUS__TEXT.setEditable(false);tablespaceDatafilesDetailsGridPane.add(ONLINE_STATUS__TEXT,11,resultSetTablespaceDatafilesDetails__INDEX);ONLINE_STATUS__TEXT.setMaxWidth(100);
                                                                resultSetTablespaceDatafilesDetails__INDEX++;
                                                            }
                                                        }catch(Exception e){
                                                            Alert notConnectedAlert = new Alert(Alert.AlertType.ERROR);
                                                            notConnectedAlert.setTitle("Error!");
                                                            notConnectedAlert.setHeaderText(null);
                                                            notConnectedAlert.setContentText("Error details:\n"+e);
                                                            notConnectedAlert.show();
                                                        }


                                                        tablespaceDetailsScrollPane.setContent(tablespaceDetailsGridPane);
                                                        tablespaceDialogGridPane.add(tablespaceDetailsScrollPane,0,6);
                                                        //Label tablespaceDetailsEmptyCol = new Label("");tablespaceDialogGridPane.add(tablespaceDetailsEmptyCol,2,0);tablespaceDetailsEmptyCol.setPrefWidth(100);
                                                        tablespaceDatafilesDetailsScrollPane.setContent(tablespaceDatafilesDetailsGridPane);
                                                        tablespaceDialogGridPane.add(tablespaceDatafilesDetailsScrollPane,3,6);
                                                    });
                                                }
                                                tablespaceDialog.getDialogPane().setContent(tablespaceDialogGridPane);
                                                tablespaceDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
                                                tablespaceDialog.show();
                                            }else{
                                                Alert notConnectedAlert = new Alert(Alert.AlertType.ERROR);
                                                notConnectedAlert.setTitle("Not connected to database!");
                                                notConnectedAlert.setHeaderText(null);
                                                notConnectedAlert.setContentText("Connection to database '"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPort()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID()+"' is not reachable");
                                                notConnectedAlert.show();
                                            }
                                        }catch(Exception e){
                                            Alert notConnectedAlert = new Alert(Alert.AlertType.ERROR);
                                            notConnectedAlert.setTitle("Not connected to database!");
                                            notConnectedAlert.setHeaderText(null);
                                            notConnectedAlert.setContentText("Connection to database '"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPort()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID()+"'\nis not reachable\n\n"+"Error details: \n"+e.toString());
                                            notConnectedAlert.show();
                                        }
                                    }else{
                                        Alert notConnectedAlert = new Alert(Alert.AlertType.ERROR);
                                        notConnectedAlert.setTitle("Database connection timeout!");
                                        notConnectedAlert.setHeaderText(null);
                                        notConnectedAlert.setContentText("Connection to database '"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPort()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID()+"'\nis not reachable\n\n"+"Connection timed out ("+connectionTimeoutSeconds+" seconds ping limit)");
                                        notConnectedAlert.show();
                                    }
                                }catch(Exception e){
                                    Alert notConnectedAlert = new Alert(Alert.AlertType.ERROR);
                                    notConnectedAlert.setTitle("Not connected to database!");
                                    notConnectedAlert.setHeaderText(null);
                                    notConnectedAlert.setContentText("Connection to database '"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPort()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID()+"'\nis not reachable\n\n"+"Error details: \n"+e.toString());
                                    notConnectedAlert.show();
                                }
                            }
                        });







                        schedulerButton.setOnAction(new EventHandler<ActionEvent>(){
                            public void handle(ActionEvent event){
                                if(pingTestToServer(connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress())){
                                    try{
                                        String URL = "jdbc:oracle:thin:@"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPort()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID();
                                        java.sql.Connection connection = DriverManager.getConnection(URL, connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionUser(), connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPassword());
                                        if(!connection.isClosed()){
                                            Dialog dialogScheduler = new Dialog();
                                            dialogScheduler.setTitle(connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID()+" | Scheduler");
                                            dialogScheduler.setHeaderText(null);
                                            GridPane dialogSchedulerGridPane = new GridPane();
                                            //dialogSchedulerGridPane.setVgap(5);dialogSchedulerGridPane.setHgap(5);

                                            String querySelectAllSchemasWithScheduler = "select s.OWNER as \"OWNER\" from DBA_USERS u, ALL_SCHEDULER_JOBS s where u.USERNAME=s.OWNER group by s.OWNER order by s.OWNER ASC";
                                            Statement statement = connection.createStatement();
                                            ResultSet resultSetGetSchedulerOwners = statement.executeQuery(querySelectAllSchemasWithScheduler);

                                            List<String> schedulerOwnerNameList = new ArrayList<>();
                                            while(resultSetGetSchedulerOwners.next()){
                                                schedulerOwnerNameList.add(resultSetGetSchedulerOwners.getString("OWNER"));
                                            }

                                            Label labelSchedulerOwner = new Label("Select scheduler job owner");
                                            dialogSchedulerGridPane.add(labelSchedulerOwner,0,0);

                                            ComboBox comboBoxSchedulerOwnerList = new ComboBox();
                                            comboBoxSchedulerOwnerList.setPromptText("Select owner");

                                            //dialogScheduler.setWidth(250);
                                            dialogScheduler.setWidth(250);
                                            dialogScheduler.setHeight(250);
                                            dialogScheduler.setX((primaryStage.getWidth()/2)-dialogScheduler.getWidth()/2);
                                            dialogScheduler.setY((primaryStage.getHeight()/2)-dialogScheduler.getHeight()/2);

                                            comboBoxSchedulerOwnerList.getItems().add("<ALL OWNERS>");
                                            for(String currentSchedulerOwnerFromSchedulerOwnerNameList : schedulerOwnerNameList)
                                            {
                                                comboBoxSchedulerOwnerList.getItems().add(currentSchedulerOwnerFromSchedulerOwnerNameList);
                                            }

                                            dialogSchedulerGridPane.add(comboBoxSchedulerOwnerList,0,1);
                                            BooleanBinding selectSchedulerOwnerValidation = Bindings.createBooleanBinding(() -> schedulerOwnerInComboBoxIsNotSelected(comboBoxSchedulerOwnerList));
                                            if(selectSchedulerOwnerValidation.get()){
                                                comboBoxSchedulerOwnerList.setOnAction((comboBoxSchedulerOwnerListAction) -> {

                                                    dialogScheduler.setWidth(1660);
                                                    dialogScheduler.setHeight(900);
                                                    dialogScheduler.setX((primaryStage.getWidth()/2)-dialogScheduler.getWidth()/2);
                                                    dialogScheduler.setY((primaryStage.getHeight()/2)-dialogScheduler.getHeight()/2);
                                                    dialogSchedulerGridPane.add(new Label(""),0,2);

                                                    Pane paneLastRunningJobs = new Pane();
                                                    paneLastRunningJobs.setMinWidth(600);paneLastRunningJobs.setPrefWidth(600);
                                                    paneLastRunningJobs.setMinHeight(700);paneLastRunningJobs.setPrefHeight(700);
                                                    GridPane gridPaneLastRunningJobs = new GridPane();
                                                    Label labelLastRunningJobs = new Label("Last (50) running jobs:");labelLastRunningJobs.setStyle("-fx-font-size:15");
                                                    gridPaneLastRunningJobs.add(labelLastRunningJobs,0,0,6,1);
                                                    Label labelLastRunningJobs__LOG_ID = new Label("LOG_ID");labelLastRunningJobs__LOG_ID.setPrefWidth(80);labelLastRunningJobs__LOG_ID.setStyle("-fx-border-style:solid;-fx-border-color:black;-fx-border-width:1;-fx-alignment:center");
                                                    Label labelLastRunningJobs__OWNER = new Label("OWNER");labelLastRunningJobs__OWNER.setPrefWidth(100);labelLastRunningJobs__OWNER.setStyle("-fx-border-style:solid;-fx-border-color:black;-fx-border-width:1;-fx-alignment:center");
                                                    Label labelLastRunningJobs__JOB_NAME = new Label("JOB_NAME");labelLastRunningJobs__JOB_NAME.setPrefWidth(140);labelLastRunningJobs__JOB_NAME.setStyle("-fx-border-style:solid;-fx-border-color:black;-fx-border-width:1;-fx-alignment:center");
                                                    Label labelLastRunningJobs__STATUS = new Label("STATUS");labelLastRunningJobs__STATUS.setPrefWidth(80);labelLastRunningJobs__STATUS.setStyle("-fx-border-style:solid;-fx-border-color:black;-fx-border-width:1;-fx-alignment:center");
                                                    Label labelLastRunningJobs__ACTUAL_START_DATE = new Label("ACTUAL_START_DATE");labelLastRunningJobs__ACTUAL_START_DATE.setPrefWidth(170);labelLastRunningJobs__ACTUAL_START_DATE.setStyle("-fx-border-style:solid;-fx-border-color:black;-fx-border-width:1;-fx-alignment:center");
                                                    gridPaneLastRunningJobs.add(labelLastRunningJobs__LOG_ID,0,1);
                                                    gridPaneLastRunningJobs.add(labelLastRunningJobs__OWNER,1,1);
                                                    gridPaneLastRunningJobs.add(labelLastRunningJobs__JOB_NAME,2,1);
                                                    gridPaneLastRunningJobs.add(labelLastRunningJobs__STATUS,3,1);
                                                    gridPaneLastRunningJobs.add(labelLastRunningJobs__ACTUAL_START_DATE,5,1);
                                                    ScrollPane scrollPaneLastRunningJobs = new ScrollPane();
                                                    gridPaneLastRunningJobs.add(scrollPaneLastRunningJobs,0,2,6,1);
                                                    scrollPaneLastRunningJobs.setMinWidth(585);scrollPaneLastRunningJobs.setMinHeight(715);scrollPaneLastRunningJobs.setPrefHeight(715);
                                                    GridPane gridPaneInScrollPaneLastRunningJobs = new GridPane();
                                                    scrollPaneLastRunningJobs.setContent(gridPaneInScrollPaneLastRunningJobs);


                                                    if(comboBoxSchedulerOwnerList.getValue().toString()=="<ALL OWNERS>"){
                                                        //String querySelectLastRunningJobsByOwnerName = "select LOG_ID, OWNER, JOB_NAME, STATUS, ACTUAL_START_DATE from ALL_SCHEDULER_JOB_RUN_DETAILS where OWNER='"+comboBoxSchedulerOwnerList.getValue().toString()+"' ORDER BY LOG_ID DESC FETCH FIRST 50 ROWS ONLY";
                                                        String querySelectLastRunningJobsByOwnerName = "select * from ALL_SCHEDULER_JOB_RUN_DETAILS ORDER BY LOG_ID DESC FETCH FIRST 50 ROWS ONLY";
                                                        try{
                                                            ResultSet resultSetSelectLastRunningJobsByOwnerName = statement.executeQuery(querySelectLastRunningJobsByOwnerName);
                                                            Integer iLastRunningJobsIndex=0;
                                                            while(resultSetSelectLastRunningJobsByOwnerName.next()){
                                                                TextField LOG_ID__TEXT = new TextField(resultSetSelectLastRunningJobsByOwnerName.getString("LOG_ID"));LOG_ID__TEXT.setEditable(false);gridPaneInScrollPaneLastRunningJobs.add(LOG_ID__TEXT,0,iLastRunningJobsIndex);LOG_ID__TEXT.setMinWidth(80);LOG_ID__TEXT.setPrefWidth(80);LOG_ID__TEXT.setMaxWidth(80);LOG_ID__TEXT.setStyle("-fx-font-size:10");
                                                                LOG_ID__TEXT.setOnMouseEntered(new EventHandler<MouseEvent>(){
                                                                    public void handle(MouseEvent event){
                                                                        LOG_ID__TEXT.setStyle("-fx-cursor:hand;-fx-font-size:10;-fx-text-fill:blue;");
                                                                    }
                                                                });
                                                                LOG_ID__TEXT.setOnMouseExited(new EventHandler<MouseEvent>(){
                                                                    public void handle(MouseEvent event){
                                                                        LOG_ID__TEXT.setStyle("-fx-font-size:10;-fx-text-fill:#383434;");
                                                                    }
                                                                });
                                                                LOG_ID__TEXT.setOnMouseClicked(new EventHandler<MouseEvent>(){
                                                                    public void handle(MouseEvent event){
                                                                        Alert dialogJobLogDetails = new Alert(Alert.AlertType.INFORMATION);
                                                                        dialogJobLogDetails.setTitle("LOG_ID \""+LOG_ID__TEXT.getText()+"\" details");
                                                                        dialogJobLogDetails.setHeaderText(null);
                                                                        GridPane dialogJobLogDetailsGridPane = new GridPane();
                                                                        dialogJobLogDetailsGridPane.setVgap(5);dialogJobLogDetailsGridPane.setHgap(5);
                                                                        Label labelJobLogDetail = new Label("LOG_ID \""+LOG_ID__TEXT.getText()+"\" details");labelJobLogDetail.setStyle("-fx-font-weight:bold");dialogJobLogDetailsGridPane.add(labelJobLogDetail,0,0);
                                                                        dialogJobLogDetailsGridPane.add(new Label(""),0,1);
                                                                        String querySelectLogDetails = "select * from all_scheduler_job_run_details where log_id='"+LOG_ID__TEXT.getText()+"'";

                                                                        Label label__LOG_ID = new Label("LOG_ID");
                                                                        Label label__LOG_DATE = new Label("LOG_DATE");
                                                                        Label label__OWNER = new Label("OWNER");
                                                                        Label label__JOB_NAME = new Label("JOB_NAME");
                                                                        Label label__JOB_SUBNAME = new Label("JOB_SUBNAME");
                                                                        Label label__STATUS = new Label("STATUS");
                                                                        Label label__ERROR = new Label("ERROR#");
                                                                        Label label__REQ_START_DATE = new Label("REQ_START_DATE");
                                                                        Label label__ACTUAL_START_DATE = new Label("ACTUAL_START_DATE");
                                                                        Label label__RUN_DURATION = new Label("RUN_DURATION");
                                                                        Label label__INSTANCE_ID = new Label("INSTANCE_ID");
                                                                        Label label__SESSION_ID = new Label("SESSION_ID");
                                                                        Label label__SLAVE_PID = new Label("SLAVE_PID");
                                                                        Label label__CPU_USED = new Label("CPU_USED");
                                                                        Label label__CREDENTIAL_OWNER = new Label("CREDENTIAL_OWNER");
                                                                        Label label__CREDENTIAL_NAME = new Label("CREDENTIAL_NAME");
                                                                        Label label__DESTINATION_OWNER = new Label("DESTINATION_OWNER");
                                                                        Label label__DESTINATION = new Label("DESTINATION");
                                                                        Label label__ADDITIONAL_INFO = new Label("ADDITIONAL_INFO");
                                                                        Label label__ERRORS = new Label("ERRORS");
                                                                        Label label__OUTPUT = new Label("OUTPUT");
                                                                        //Label label__BINARY_ERRORS = new Label("BINARY_ERRORS");
                                                                        //Label label__BINARY_OUTPUT = new Label("BINARY_OUTPUT");

                                                                        try{
                                                                            ResultSet resultSetLogDetails = statement.executeQuery(querySelectLogDetails);
                                                                            while(resultSetLogDetails.next()){
                                                                                TextField textField__LOG_ID = new TextField(resultSetLogDetails.getString("LOG_ID"));textField__LOG_ID.setEditable(false);textField__LOG_ID.setMinWidth(300);
                                                                                TextField textField__LOG_DATE = new TextField(resultSetLogDetails.getString("LOG_DATE"));textField__LOG_DATE.setEditable(false);textField__LOG_DATE.setMinWidth(300);
                                                                                TextField textField__OWNER = new TextField(resultSetLogDetails.getString("OWNER"));textField__OWNER.setEditable(false);textField__LOG_DATE.setMinWidth(300);
                                                                                TextField textField__JOB_NAME = new TextField(resultSetLogDetails.getString("JOB_NAME"));textField__JOB_NAME.setEditable(false);textField__JOB_NAME.setMinWidth(300);
                                                                                TextField textField__JOB_SUBNAME = new TextField(resultSetLogDetails.getString("JOB_SUBNAME"));textField__JOB_SUBNAME.setEditable(false);textField__JOB_SUBNAME.setMinWidth(300);
                                                                                TextField textField__STATUS = new TextField(resultSetLogDetails.getString("STATUS"));textField__STATUS.setEditable(false);textField__STATUS.setMinWidth(300);
                                                                                TextField textField__ERROR = new TextField(resultSetLogDetails.getString("ERROR#"));textField__ERROR.setEditable(false);textField__ERROR.setMinWidth(300);
                                                                                TextField textField__REQ_START_DATE = new TextField(resultSetLogDetails.getString("REQ_START_DATE"));textField__REQ_START_DATE.setEditable(false);textField__REQ_START_DATE.setMinWidth(300);
                                                                                TextField textField__ACTUAL_START_DATE = new TextField(resultSetLogDetails.getString("ACTUAL_START_DATE"));textField__ACTUAL_START_DATE.setEditable(false);textField__ACTUAL_START_DATE.setMinWidth(300);
                                                                                TextField textField__RUN_DURATION = new TextField(resultSetLogDetails.getString("RUN_DURATION"));textField__RUN_DURATION.setEditable(false);textField__RUN_DURATION.setMinWidth(300);
                                                                                TextField textField__INSTANCE_ID = new TextField(resultSetLogDetails.getString("INSTANCE_ID"));textField__INSTANCE_ID.setEditable(false);textField__INSTANCE_ID.setMinWidth(300);
                                                                                TextField textField__SESSION_ID = new TextField(resultSetLogDetails.getString("SESSION_ID"));textField__SESSION_ID.setEditable(false);textField__SESSION_ID.setMinWidth(300);
                                                                                TextField textField__SLAVE_PID = new TextField(resultSetLogDetails.getString("SLAVE_PID"));textField__SLAVE_PID.setEditable(false);textField__SLAVE_PID.setMinWidth(300);
                                                                                TextField textField__CPU_USED = new TextField(resultSetLogDetails.getString("CPU_USED"));textField__CPU_USED.setEditable(false);textField__CPU_USED.setMinWidth(300);
                                                                                TextField textField__CREDENTIAL_OWNER = new TextField(resultSetLogDetails.getString("CREDENTIAL_OWNER"));textField__CREDENTIAL_OWNER.setEditable(false);textField__CREDENTIAL_OWNER.setMinWidth(300);
                                                                                TextField textField__CREDENTIAL_NAME = new TextField(resultSetLogDetails.getString("CREDENTIAL_NAME"));textField__CREDENTIAL_NAME.setEditable(false);textField__CREDENTIAL_NAME.setMinWidth(300);
                                                                                TextField textField__DESTINATION_OWNER = new TextField(resultSetLogDetails.getString("DESTINATION_OWNER"));textField__DESTINATION_OWNER.setEditable(false);textField__CREDENTIAL_NAME.setMinWidth(300);
                                                                                TextField textField__DESTINATION = new TextField(resultSetLogDetails.getString("DESTINATION"));textField__DESTINATION.setEditable(false);textField__DESTINATION.setMinWidth(300);
                                                                                TextField textField__ADDITIONAL_INFO = new TextField(resultSetLogDetails.getString("ADDITIONAL_INFO"));textField__ADDITIONAL_INFO.setEditable(false);textField__ADDITIONAL_INFO.setMinWidth(300);
                                                                                TextField textField__ERRORS = new TextField(resultSetLogDetails.getString("ERRORS"));textField__ERRORS.setEditable(false);textField__ERRORS.setMinWidth(300);
                                                                                TextField textField__OUTPUT = new TextField(resultSetLogDetails.getString("OUTPUT"));textField__OUTPUT.setEditable(false);textField__OUTPUT.setMinWidth(300);
                                                                                //TextField textField__BINARY_ERRORS = new TextField(resultSetLogDetails.getBinaryStream("BINARY_ERRORS").toString());textField__BINARY_ERRORS.setEditable(false);textField__BINARY_ERRORS.setMinWidth(300);
                                                                                //TextField textField__BINARY_OUTPUT = new TextField(resultSetLogDetails.getBinaryStream("BINARY_OUTPUT").toString());textField__BINARY_OUTPUT.setEditable(false);textField__BINARY_OUTPUT.setMinWidth(300);

                                                                                dialogJobLogDetailsGridPane.add(label__LOG_ID,0,2);dialogJobLogDetailsGridPane.add(textField__LOG_ID,1,2);
                                                                                dialogJobLogDetailsGridPane.add(label__LOG_DATE,0,3);dialogJobLogDetailsGridPane.add(textField__LOG_DATE,1,3);
                                                                                dialogJobLogDetailsGridPane.add(label__OWNER,0,4);dialogJobLogDetailsGridPane.add(textField__OWNER,1,4);
                                                                                dialogJobLogDetailsGridPane.add(label__JOB_NAME,0,5);dialogJobLogDetailsGridPane.add(textField__JOB_NAME,1,5);
                                                                                dialogJobLogDetailsGridPane.add(label__JOB_SUBNAME,0,6);dialogJobLogDetailsGridPane.add(textField__JOB_SUBNAME,1,6);
                                                                                dialogJobLogDetailsGridPane.add(label__STATUS,0,7);dialogJobLogDetailsGridPane.add(textField__STATUS,1,7);
                                                                                dialogJobLogDetailsGridPane.add(label__ERROR,0,8);dialogJobLogDetailsGridPane.add(textField__ERROR,1,8);
                                                                                dialogJobLogDetailsGridPane.add(label__REQ_START_DATE,0,9);dialogJobLogDetailsGridPane.add(textField__REQ_START_DATE,1,9);
                                                                                dialogJobLogDetailsGridPane.add(label__ACTUAL_START_DATE,0,10);dialogJobLogDetailsGridPane.add(textField__ACTUAL_START_DATE,1,10);
                                                                                dialogJobLogDetailsGridPane.add(label__RUN_DURATION,0,11);dialogJobLogDetailsGridPane.add(textField__RUN_DURATION,1,11);
                                                                                dialogJobLogDetailsGridPane.add(label__INSTANCE_ID,0,12);dialogJobLogDetailsGridPane.add(textField__INSTANCE_ID,1,12);
                                                                                dialogJobLogDetailsGridPane.add(label__SESSION_ID,0,13);dialogJobLogDetailsGridPane.add(textField__SESSION_ID,1,13);
                                                                                dialogJobLogDetailsGridPane.add(label__SLAVE_PID,0,14);dialogJobLogDetailsGridPane.add(textField__SLAVE_PID,1,14);
                                                                                dialogJobLogDetailsGridPane.add(label__CPU_USED,0,15);dialogJobLogDetailsGridPane.add(textField__CPU_USED,1,15);
                                                                                dialogJobLogDetailsGridPane.add(label__CREDENTIAL_OWNER,0,16);dialogJobLogDetailsGridPane.add(textField__CREDENTIAL_OWNER,1,16);
                                                                                dialogJobLogDetailsGridPane.add(label__CREDENTIAL_NAME,0,17);dialogJobLogDetailsGridPane.add(textField__CREDENTIAL_NAME,1,17);
                                                                                dialogJobLogDetailsGridPane.add(label__DESTINATION_OWNER,0,18);dialogJobLogDetailsGridPane.add(textField__DESTINATION_OWNER,1,18);
                                                                                dialogJobLogDetailsGridPane.add(label__DESTINATION,0,19);dialogJobLogDetailsGridPane.add(textField__DESTINATION,1,19);
                                                                                dialogJobLogDetailsGridPane.add(label__ADDITIONAL_INFO,0,20);dialogJobLogDetailsGridPane.add(textField__ADDITIONAL_INFO,1,20);
                                                                                dialogJobLogDetailsGridPane.add(label__ERRORS,0,21);dialogJobLogDetailsGridPane.add(textField__ERRORS,1,21);
                                                                                dialogJobLogDetailsGridPane.add(label__OUTPUT,0,22);dialogJobLogDetailsGridPane.add(textField__OUTPUT,1,22);
                                                                                //dialogJobLogDetailsGridPane.add(label__BINARY_ERRORS,0,23);dialogJobLogDetailsGridPane.add(textField__BINARY_ERRORS,1,23);
                                                                                //dialogJobLogDetailsGridPane.add(label__BINARY_OUTPUT,0,24);dialogJobLogDetailsGridPane.add(textField__BINARY_OUTPUT,1,24);

                                                                            }
                                                                        }catch(Exception e){System.out.println(e);}
                                                                        dialogJobLogDetails.getDialogPane().setContent(dialogJobLogDetailsGridPane);
                                                                        dialogJobLogDetails.show();
                                                                    }
                                                                });

                                                                TextField OWNER__TEXT = new TextField(resultSetSelectLastRunningJobsByOwnerName.getString("OWNER"));OWNER__TEXT.setEditable(false);gridPaneInScrollPaneLastRunningJobs.add(OWNER__TEXT,1,iLastRunningJobsIndex);OWNER__TEXT.setMinWidth(100);OWNER__TEXT.setPrefWidth(100);OWNER__TEXT.setMaxWidth(100);OWNER__TEXT.setStyle("-fx-font-size:10");
                                                                TextField JOB_NAME__TEXT = new TextField(resultSetSelectLastRunningJobsByOwnerName.getString("JOB_NAME"));JOB_NAME__TEXT.setEditable(false);gridPaneInScrollPaneLastRunningJobs.add(JOB_NAME__TEXT,2,iLastRunningJobsIndex);JOB_NAME__TEXT.setMinWidth(140);JOB_NAME__TEXT.setPrefWidth(140);JOB_NAME__TEXT.setMaxWidth(140);JOB_NAME__TEXT.setStyle("-fx-font-size:10");

                                                                //JOB_NAME__TEXT.onMouseClickedProperty()
                                                                JOB_NAME__TEXT.setOnMouseEntered(new EventHandler<MouseEvent>(){
                                                                    public void handle(MouseEvent event){
                                                                        JOB_NAME__TEXT.setStyle("-fx-cursor:hand;-fx-font-size:10;-fx-text-fill:blue;");
                                                                    }
                                                                });
                                                                JOB_NAME__TEXT.setOnMouseExited(new EventHandler<MouseEvent>(){
                                                                    public void handle(MouseEvent event){
                                                                        JOB_NAME__TEXT.setStyle("-fx-font-size:10;-fx-text-fill:#383434;");
                                                                    }
                                                                });
//
                                                                JOB_NAME__TEXT.setOnMouseClicked(new EventHandler<MouseEvent>(){
                                                                    public void handle(MouseEvent event){
                                                                        Alert dialogJobDetails = new Alert(Alert.AlertType.INFORMATION);
                                                                        dialogJobDetails.setWidth(900);dialogJobDetails.setHeight(650);
                                                                        dialogJobDetails.setTitle("Job \""+JOB_NAME__TEXT.getText()+"\" details");
                                                                        dialogJobDetails.setHeaderText(null);
                                                                        GridPane dialogJobDetailsGridPane = new GridPane();
                                                                        dialogJobDetailsGridPane.setVgap(5);dialogJobDetailsGridPane.setHgap(5);
                                                                        //dialogJobDetailsGridPane.add(new Label(""),0,0);
                                                                        //,0,1
                                                                        Label labelJobDetail = new Label("Job \""+JOB_NAME__TEXT.getText()+"\" details");labelJobDetail.setStyle("-fx-font-weight:bold");dialogJobDetailsGridPane.add(labelJobDetail,0,0);
                                                                        String querySelectJobDetails = "select * from all_scheduler_jobs where job_name='"+JOB_NAME__TEXT.getText()+"' and owner='"+OWNER__TEXT.getText()+"'";
                                                                        ScrollPane scrollPaneJobDetails = new ScrollPane();
                                                                        GridPane gridPaneInScrollPaneJobDetails = new GridPane();
                                                                        gridPaneInScrollPaneJobDetails.setVgap(5);gridPaneInScrollPaneJobDetails.setHgap(15);
                                                                        scrollPaneJobDetails.setContent(gridPaneInScrollPaneJobDetails);
                                                                        scrollPaneJobDetails.setMinHeight(600);scrollPaneJobDetails.setMaxHeight(600);scrollPaneJobDetails.setPrefHeight(600);
                                                                        scrollPaneJobDetails.setMinWidth(550);scrollPaneJobDetails.setMaxWidth(550);scrollPaneJobDetails.setPrefWidth(550);
                                                                        dialogJobDetailsGridPane.add(scrollPaneJobDetails,0,2);
                                                                        Label label__OWNER = new Label("LOG_ID");
                                                                        Label label__JOB_NAME = new Label("JOB_NAME");
                                                                        Label label__JOB_SUBNAME = new Label("JOB_SUBNAME");
                                                                        Label label__JOB_STYLE = new Label("JOB_STYLE");
                                                                        Label label__JOB_CREATOR = new Label("JOB_CREATOR");
                                                                        Label label__CLIENT_ID = new Label("CLIENT_ID");
                                                                        Label label__GLOBAL_UID = new Label("GLOBAL_UID");
                                                                        Label label__PROGRAM_OWNER = new Label("PROGRAM_OWNER");
                                                                        Label label__PROGRAM_NAME = new Label("PROGRAM_NAME");
                                                                        Label label__JOB_TYPE = new Label("JOB_TYPE");
                                                                        Label label__JOB_ACTION = new Label("JOB_ACTION");
                                                                        Label label__NUMBER_OF_ARGUMENTS = new Label("NUMBER_OF_ARGUMENTS");
                                                                        Label label__SCHEDULE_OWNER = new Label("SCHEDULE_OWNER");
                                                                        Label label__SCHEDULE_NAME = new Label("SCHEDULE_NAME");
                                                                        Label label__SCHEDULE_TYPE = new Label("SCHEDULE_TYPE");
                                                                        Label label__START_DATE = new Label("START_DATE");
                                                                        Label label__REPEAT_INTERVAL = new Label("REPEAT_INTERVAL");
                                                                        Label label__EVENT_QUEUE_OWNER = new Label("EVENT_QUEUE_OWNER");
                                                                        Label label__EVENT_QUEUE_NAME = new Label("EVENT_QUEUE_NAME");
                                                                        Label label__EVENT_QUEUE_AGENT = new Label("EVENT_QUEUE_AGENT");
                                                                        Label label__EVENT_CONDITION = new Label("EVENT_CONDITION");
                                                                        Label label__EVENT_RULE = new Label("EVENT_RULE");
                                                                        Label label__FILE_WATCHER_OWNER = new Label("FILE_WATCHER_OWNER");
                                                                        Label label__FILE_WATCHER_NAME = new Label("FILE_WATCHER_NAME");
                                                                        Label label__END_DATE = new Label("END_DATE");
                                                                        Label label__JOB_CLASS = new Label("JOB_CLASS");
                                                                        Label label__ENABLED = new Label("ENABLED");
                                                                        Label label__AUTO_DROP = new Label("AUTO_DROP");
                                                                        Label label__RESTART_ON_RECOVERY = new Label("RESTART_ON_RECOVERY");
                                                                        Label label__RESTART_ON_FAILURE = new Label("RESTART_ON_FAILURE");
                                                                        Label label__STATE = new Label("STATE");
                                                                        Label label__JOB_PRIORITY = new Label("JOB_PRIORITY");
                                                                        Label label__RUN_COUNT = new Label("RUN_COUNT");
                                                                        Label label__UPTIME_RUN_COUNT = new Label("UPTIME_RUN_COUNT");
                                                                        Label label__MAX_RUNS = new Label("MAX_RUNS");
                                                                        Label label__FAILURE_COUNT = new Label("FAILURE_COUNT");
                                                                        Label label__UPTIME_FAILURE_COUNT = new Label("UPTIME_FAILURE_COUNT");
                                                                        Label label__MAX_FAILURES = new Label("MAX_FAILURES");
                                                                        Label label__RETRY_COUNT = new Label("RETRY_COUNT");
                                                                        Label label__LAST_START_DATE = new Label("LAST_START_DATE");
                                                                        Label label__LAST_RUN_DURATION = new Label("LAST_RUN_DURATION");
                                                                        Label label__NEXT_RUN_DATE = new Label("NEXT_RUN_DATE");
                                                                        Label label__SCHEDULE_LIMIT = new Label("SCHEDULE_LIMIT");
                                                                        Label label__MAX_RUN_DURATION = new Label("MAX_RUN_DURATION");
                                                                        Label label__LOGGING_LEVEL = new Label("LOGGING_LEVEL");
                                                                        Label label__STORE_OUTPUT = new Label("STORE_OUTPUT");
                                                                        Label label__STOP_ON_WINDOW_CLOSE = new Label("STOP_ON_WINDOW_CLOSE");
                                                                        Label label__INSTANCE_STICKINESS = new Label("INSTANCE_STICKINESS");
                                                                        Label label__RAISE_EVENTS = new Label("RAISE_EVENTS");
                                                                        Label label__SYSTEM = new Label("SYSTEM");
                                                                        Label label__JOB_WEIGHT = new Label("JOB_WEIGHT");
                                                                        Label label__NLS_ENV = new Label("NLS_ENV");
                                                                        Label label__SOURCE = new Label("SOURCE");
                                                                        Label label__NUMBER_OF_DESTINATIONS = new Label("NUMBER_OF_DESTINATIONS");
                                                                        Label label__DESTINATION_OWNER = new Label("DESTINATION_OWNER");
                                                                        Label label__DESTINATION = new Label("DESTINATION");
                                                                        Label label__CREDENTIAL_OWNER = new Label("CREDENTIAL_OWNER");
                                                                        Label label__CREDENTIAL_NAME= new Label("CREDENTIAL_NAME");
                                                                        Label label__INSTANCE_ID= new Label("INSTANCE_ID");
                                                                        Label label__DEFERRED_DROP= new Label("DEFERRED_DROP");
                                                                        Label label__ALLOW_RUNS_IN_RESTRICTED_MODE= new Label("ALLOW_RUNS_IN_RESTRICTED_MODE");
                                                                        Label label__COMMENTS= new Label("COMMENTS");
                                                                        Label label__FLAGS= new Label("FLAGS");
                                                                        Label label__RESTARTABLE= new Label("RESTARTABLE");
                                                                        Label label__HAS_CONSTRAINTS= new Label("HAS_CONSTRAINTS");
                                                                        Label label__CONNECT_CREDENTIAL_OWNER= new Label("CONNECT_CREDENTIAL_OWNER");
                                                                        Label label__CONNECT_CREDENTIAL_NAME= new Label("CONNECT_CREDENTIAL_NAME");
                                                                        Label label__FAIL_ON_SCRIPT_ERROR= new Label("FAIL_ON_SCRIPT_ERROR");


                                                                        try{
                                                                            ResultSet resultSetLogDetails = statement.executeQuery(querySelectJobDetails);
                                                                            while(resultSetLogDetails.next()){
                                                                                TextField textField__LOG_ID = new TextField(resultSetLogDetails.getString("OWNER"));textField__LOG_ID.setEditable(false);textField__LOG_ID.setMinWidth(300);
                                                                                TextField textField__JOB_NAME = new TextField(resultSetLogDetails.getString("JOB_NAME"));textField__JOB_NAME.setEditable(false);textField__JOB_NAME.setMinWidth(300);
                                                                                TextField textField__JOB_SUBNAME = new TextField(resultSetLogDetails.getString("JOB_SUBNAME"));textField__JOB_SUBNAME.setEditable(false);textField__JOB_SUBNAME.setMinWidth(300);
                                                                                TextField textField__JOB_STYLE = new TextField(resultSetLogDetails.getString("JOB_STYLE"));textField__JOB_STYLE.setEditable(false);textField__JOB_STYLE.setMinWidth(300);
                                                                                TextField textField__JOB_CREATOR = new TextField(resultSetLogDetails.getString("JOB_CREATOR"));textField__JOB_CREATOR.setEditable(false);textField__JOB_CREATOR.setMinWidth(300);
                                                                                TextField textField__CLIENT_ID = new TextField(resultSetLogDetails.getString("CLIENT_ID"));textField__CLIENT_ID.setEditable(false);textField__CLIENT_ID.setMinWidth(300);
                                                                                TextField textField__GLOBAL_UID = new TextField(resultSetLogDetails.getString("GLOBAL_UID"));textField__GLOBAL_UID.setEditable(false);textField__GLOBAL_UID.setMinWidth(300);
                                                                                TextField textField__PROGRAM_OWNER = new TextField(resultSetLogDetails.getString("PROGRAM_OWNER"));textField__PROGRAM_OWNER.setEditable(false);textField__PROGRAM_OWNER.setMinWidth(300);
                                                                                TextField textField__PROGRAM_NAME = new TextField(resultSetLogDetails.getString("PROGRAM_NAME"));textField__PROGRAM_NAME.setEditable(false);textField__PROGRAM_NAME.setMinWidth(300);
                                                                                TextField textField__JOB_TYPE = new TextField(resultSetLogDetails.getString("JOB_TYPE"));textField__JOB_TYPE.setEditable(false);textField__JOB_TYPE.setMinWidth(300);
                                                                                TextField textField__JOB_ACTION = new TextField(resultSetLogDetails.getString("JOB_ACTION"));textField__JOB_ACTION.setEditable(false);textField__JOB_ACTION.setMinWidth(300);
                                                                                TextField textField__NUMBER_OF_ARGUMENTS = new TextField(resultSetLogDetails.getString("NUMBER_OF_ARGUMENTS"));textField__NUMBER_OF_ARGUMENTS.setEditable(false);textField__NUMBER_OF_ARGUMENTS.setMinWidth(300);
                                                                                TextField textField__SCHEDULE_OWNER = new TextField(resultSetLogDetails.getString("SCHEDULE_OWNER"));textField__SCHEDULE_OWNER.setEditable(false);textField__SCHEDULE_OWNER.setMinWidth(300);
                                                                                TextField textField__SCHEDULE_NAME = new TextField(resultSetLogDetails.getString("SCHEDULE_NAME"));textField__SCHEDULE_NAME.setEditable(false);textField__SCHEDULE_NAME.setMinWidth(300);
                                                                                TextField textField__SCHEDULE_TYPE = new TextField(resultSetLogDetails.getString("SCHEDULE_TYPE"));textField__SCHEDULE_TYPE.setEditable(false);textField__SCHEDULE_TYPE.setMinWidth(300);
                                                                                TextField textField__START_DATE = new TextField(resultSetLogDetails.getString("START_DATE"));textField__START_DATE.setEditable(false);textField__START_DATE.setMinWidth(300);
                                                                                TextField textField__REPEAT_INTERVAL = new TextField(resultSetLogDetails.getString("REPEAT_INTERVAL"));textField__REPEAT_INTERVAL.setEditable(false);textField__REPEAT_INTERVAL.setMinWidth(300);
                                                                                TextField textField__EVENT_QUEUE_OWNER = new TextField(resultSetLogDetails.getString("EVENT_QUEUE_OWNER"));textField__EVENT_QUEUE_OWNER.setEditable(false);textField__EVENT_QUEUE_OWNER.setMinWidth(300);
                                                                                TextField textField__EVENT_QUEUE_NAME = new TextField(resultSetLogDetails.getString("EVENT_QUEUE_NAME"));textField__EVENT_QUEUE_NAME.setEditable(false);textField__EVENT_QUEUE_NAME.setMinWidth(300);
                                                                                TextField textField__EVENT_QUEUE_AGENT = new TextField(resultSetLogDetails.getString("EVENT_QUEUE_AGENT"));textField__EVENT_QUEUE_AGENT.setEditable(false);textField__EVENT_QUEUE_AGENT.setMinWidth(300);
                                                                                TextField textField__EVENT_CONDITION = new TextField(resultSetLogDetails.getString("EVENT_CONDITION"));textField__EVENT_CONDITION.setEditable(false);textField__EVENT_CONDITION.setMinWidth(300);
                                                                                TextField textField__EVENT_RULE = new TextField(resultSetLogDetails.getString("EVENT_RULE"));textField__EVENT_RULE.setEditable(false);textField__EVENT_RULE.setMinWidth(300);
                                                                                TextField textField__FILE_WATCHER_OWNER = new TextField(resultSetLogDetails.getString("FILE_WATCHER_OWNER"));textField__FILE_WATCHER_OWNER.setEditable(false);textField__FILE_WATCHER_OWNER.setMinWidth(300);
                                                                                TextField textField__FILE_WATCHER_NAME = new TextField(resultSetLogDetails.getString("FILE_WATCHER_NAME"));textField__FILE_WATCHER_NAME.setEditable(false);textField__FILE_WATCHER_NAME.setMinWidth(300);
                                                                                TextField textField__END_DATE = new TextField(resultSetLogDetails.getString("END_DATE"));textField__END_DATE.setEditable(false);textField__END_DATE.setMinWidth(300);
                                                                                TextField textField__JOB_CLASS = new TextField(resultSetLogDetails.getString("JOB_CLASS"));textField__JOB_CLASS.setEditable(false);textField__JOB_CLASS.setMinWidth(300);
                                                                                TextField textField__ENABLED = new TextField(resultSetLogDetails.getString("ENABLED"));textField__ENABLED.setEditable(false);textField__ENABLED.setMinWidth(300);
                                                                                TextField textField__AUTO_DROP = new TextField(resultSetLogDetails.getString("AUTO_DROP"));textField__AUTO_DROP.setEditable(false);textField__AUTO_DROP.setMinWidth(300);
                                                                                TextField textField__RESTART_ON_RECOVERY = new TextField(resultSetLogDetails.getString("RESTART_ON_RECOVERY"));textField__RESTART_ON_RECOVERY.setEditable(false);textField__RESTART_ON_RECOVERY.setMinWidth(300);
                                                                                TextField textField__RESTART_ON_FAILURE = new TextField(resultSetLogDetails.getString("RESTART_ON_FAILURE"));textField__RESTART_ON_FAILURE.setEditable(false);textField__RESTART_ON_FAILURE.setMinWidth(300);
                                                                                TextField textField__STATE = new TextField(resultSetLogDetails.getString("STATE"));textField__STATE.setEditable(false);textField__STATE.setMinWidth(300);
                                                                                TextField textField__JOB_PRIORITY = new TextField(resultSetLogDetails.getString("JOB_PRIORITY"));textField__JOB_PRIORITY.setEditable(false);textField__JOB_PRIORITY.setMinWidth(300);
                                                                                TextField textField__RUN_COUNT = new TextField(resultSetLogDetails.getString("RUN_COUNT"));textField__RUN_COUNT.setEditable(false);textField__RUN_COUNT.setMinWidth(300);
                                                                                TextField textField__UPTIME_RUN_COUNT = new TextField(resultSetLogDetails.getString("UPTIME_RUN_COUNT"));textField__UPTIME_RUN_COUNT.setEditable(false);textField__UPTIME_RUN_COUNT.setMinWidth(300);
                                                                                TextField textField__MAX_RUNS = new TextField(resultSetLogDetails.getString("MAX_RUNS"));textField__MAX_RUNS.setEditable(false);textField__MAX_RUNS.setMinWidth(300);
                                                                                TextField textField__FAILURE_COUNT = new TextField(resultSetLogDetails.getString("FAILURE_COUNT"));textField__FAILURE_COUNT.setEditable(false);textField__FAILURE_COUNT.setMinWidth(300);
                                                                                TextField textField__UPTIME_FAILURE_COUNT = new TextField(resultSetLogDetails.getString("UPTIME_FAILURE_COUNT"));textField__UPTIME_FAILURE_COUNT.setEditable(false);textField__UPTIME_FAILURE_COUNT.setMinWidth(300);
                                                                                TextField textField__MAX_FAILURES = new TextField(resultSetLogDetails.getString("MAX_FAILURES"));textField__MAX_FAILURES.setEditable(false);textField__MAX_FAILURES.setMinWidth(300);
                                                                                TextField textField__RETRY_COUNT = new TextField(resultSetLogDetails.getString("RETRY_COUNT"));textField__RETRY_COUNT.setEditable(false);textField__RETRY_COUNT.setMinWidth(300);
                                                                                TextField textField__LAST_START_DATE = new TextField(resultSetLogDetails.getString("LAST_START_DATE"));textField__LAST_START_DATE.setEditable(false);textField__LAST_START_DATE.setMinWidth(300);
                                                                                TextField textField__LAST_RUN_DURATION = new TextField(resultSetLogDetails.getString("LAST_RUN_DURATION"));textField__LAST_RUN_DURATION.setEditable(false);textField__LAST_RUN_DURATION.setMinWidth(300);
                                                                                TextField textField__NEXT_RUN_DATE = new TextField(resultSetLogDetails.getString("NEXT_RUN_DATE"));textField__NEXT_RUN_DATE.setEditable(false);textField__NEXT_RUN_DATE.setMinWidth(300);
                                                                                TextField textField__SCHEDULE_LIMIT = new TextField(resultSetLogDetails.getString("SCHEDULE_LIMIT"));textField__SCHEDULE_LIMIT.setEditable(false);textField__SCHEDULE_LIMIT.setMinWidth(300);
                                                                                TextField textField__MAX_RUN_DURATION = new TextField(resultSetLogDetails.getString("MAX_RUN_DURATION"));textField__MAX_RUN_DURATION.setEditable(false);textField__MAX_RUN_DURATION.setMinWidth(300);
                                                                                TextField textField__LOGGING_LEVEL = new TextField(resultSetLogDetails.getString("LOGGING_LEVEL"));textField__LOGGING_LEVEL.setEditable(false);textField__LOGGING_LEVEL.setMinWidth(300);
                                                                                TextField textField__STORE_OUTPUT = new TextField(resultSetLogDetails.getString("STORE_OUTPUT"));textField__STORE_OUTPUT.setEditable(false);textField__STORE_OUTPUT.setMinWidth(300);
                                                                                TextField textField__STOP_ON_WINDOW_CLOSE = new TextField(resultSetLogDetails.getString("STOP_ON_WINDOW_CLOSE"));textField__STOP_ON_WINDOW_CLOSE.setEditable(false);textField__STOP_ON_WINDOW_CLOSE.setMinWidth(300);
                                                                                TextField textField__INSTANCE_STICKINESS = new TextField(resultSetLogDetails.getString("INSTANCE_STICKINESS"));textField__INSTANCE_STICKINESS.setEditable(false);textField__INSTANCE_STICKINESS.setMinWidth(300);
                                                                                TextField textField__RAISE_EVENTS = new TextField(resultSetLogDetails.getString("RAISE_EVENTS"));textField__RAISE_EVENTS.setEditable(false);textField__RAISE_EVENTS.setMinWidth(300);
                                                                                TextField textField__SYSTEM = new TextField(resultSetLogDetails.getString("SYSTEM"));textField__SYSTEM.setEditable(false);textField__SYSTEM.setMinWidth(300);
                                                                                TextField textField__JOB_WEIGHT = new TextField(resultSetLogDetails.getString("JOB_WEIGHT"));textField__JOB_WEIGHT.setEditable(false);textField__JOB_WEIGHT.setMinWidth(300);
                                                                                TextField textField__NLS_ENV = new TextField(resultSetLogDetails.getString("NLS_ENV"));textField__NLS_ENV.setEditable(false);textField__NLS_ENV.setMinWidth(300);
                                                                                TextField textField__SOURCE = new TextField(resultSetLogDetails.getString("SOURCE"));textField__SOURCE.setEditable(false);textField__SOURCE.setMinWidth(300);
                                                                                TextField textField__NUMBER_OF_DESTINATIONS = new TextField(resultSetLogDetails.getString("NUMBER_OF_DESTINATIONS"));textField__NUMBER_OF_DESTINATIONS.setEditable(false);textField__NUMBER_OF_DESTINATIONS.setMinWidth(300);
                                                                                TextField textField__DESTINATION_OWNER = new TextField(resultSetLogDetails.getString("DESTINATION_OWNER"));textField__DESTINATION_OWNER.setEditable(false);textField__DESTINATION_OWNER.setMinWidth(300);
                                                                                TextField textField__DESTINATION = new TextField(resultSetLogDetails.getString("DESTINATION"));textField__DESTINATION.setEditable(false);textField__DESTINATION.setMinWidth(300);
                                                                                TextField textField__CREDENTIAL_OWNER = new TextField(resultSetLogDetails.getString("CREDENTIAL_OWNER"));textField__CREDENTIAL_OWNER.setEditable(false);textField__CREDENTIAL_OWNER.setMinWidth(300);
                                                                                TextField textField__CREDENTIAL_NAME = new TextField(resultSetLogDetails.getString("CREDENTIAL_NAME"));textField__CREDENTIAL_NAME.setEditable(false);textField__CREDENTIAL_NAME.setMinWidth(300);
                                                                                TextField textField__INSTANCE_ID = new TextField(resultSetLogDetails.getString("INSTANCE_ID"));textField__INSTANCE_ID.setEditable(false);textField__INSTANCE_ID.setMinWidth(300);
                                                                                TextField textField__DEFERRED_DROP = new TextField(resultSetLogDetails.getString("DEFERRED_DROP"));textField__DEFERRED_DROP.setEditable(false);textField__DEFERRED_DROP.setMinWidth(300);
                                                                                TextField textField__ALLOW_RUNS_IN_RESTRICTED_MODE = new TextField(resultSetLogDetails.getString("ALLOW_RUNS_IN_RESTRICTED_MODE"));textField__ALLOW_RUNS_IN_RESTRICTED_MODE.setEditable(false);textField__ALLOW_RUNS_IN_RESTRICTED_MODE.setMinWidth(300);
                                                                                TextField textField__COMMENTS = new TextField(resultSetLogDetails.getString("COMMENTS"));textField__COMMENTS.setEditable(false);textField__COMMENTS.setMinWidth(300);
                                                                                TextField textField__FLAGS = new TextField(resultSetLogDetails.getString("FLAGS"));textField__FLAGS.setEditable(false);textField__FLAGS.setMinWidth(300);
                                                                                TextField textField__RESTARTABLE = new TextField(resultSetLogDetails.getString("RESTARTABLE"));textField__RESTARTABLE.setEditable(false);textField__RESTARTABLE.setMinWidth(300);
                                                                                TextField textField__HAS_CONSTRAINTS = new TextField(resultSetLogDetails.getString("HAS_CONSTRAINTS"));textField__HAS_CONSTRAINTS.setEditable(false);textField__HAS_CONSTRAINTS.setMinWidth(300);
                                                                                TextField textField__CONNECT_CREDENTIAL_OWNER = new TextField(resultSetLogDetails.getString("CONNECT_CREDENTIAL_OWNER"));textField__CONNECT_CREDENTIAL_OWNER.setEditable(false);textField__CONNECT_CREDENTIAL_OWNER.setMinWidth(300);
                                                                                TextField textField__CONNECT_CREDENTIAL_NAME = new TextField(resultSetLogDetails.getString("CONNECT_CREDENTIAL_NAME"));textField__CONNECT_CREDENTIAL_NAME.setEditable(false);textField__CONNECT_CREDENTIAL_NAME.setMinWidth(300);
                                                                                TextField textField__FAIL_ON_SCRIPT_ERROR = new TextField(resultSetLogDetails.getString("FAIL_ON_SCRIPT_ERROR"));textField__FAIL_ON_SCRIPT_ERROR.setEditable(false);textField__FAIL_ON_SCRIPT_ERROR.setMinWidth(300);


                                                                                gridPaneInScrollPaneJobDetails.add(label__OWNER,0,0);gridPaneInScrollPaneJobDetails.add(textField__LOG_ID,1,0);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_NAME,0,1);gridPaneInScrollPaneJobDetails.add(textField__JOB_NAME,1,1);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_SUBNAME,0,2);gridPaneInScrollPaneJobDetails.add(textField__JOB_SUBNAME,1,2);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_STYLE,0,3);gridPaneInScrollPaneJobDetails.add(textField__JOB_STYLE,1,3);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_CREATOR,0,4);gridPaneInScrollPaneJobDetails.add(textField__JOB_CREATOR,1,4);
                                                                                gridPaneInScrollPaneJobDetails.add(label__CLIENT_ID,0,5);gridPaneInScrollPaneJobDetails.add(textField__CLIENT_ID,1,5);
                                                                                gridPaneInScrollPaneJobDetails.add(label__GLOBAL_UID,0,6);gridPaneInScrollPaneJobDetails.add(textField__GLOBAL_UID,1,6);
                                                                                gridPaneInScrollPaneJobDetails.add(label__PROGRAM_OWNER,0,7);gridPaneInScrollPaneJobDetails.add(textField__PROGRAM_OWNER,1,7);
                                                                                gridPaneInScrollPaneJobDetails.add(label__PROGRAM_NAME,0,8);gridPaneInScrollPaneJobDetails.add(textField__PROGRAM_NAME,1,8);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_TYPE,0,9);gridPaneInScrollPaneJobDetails.add(textField__JOB_TYPE,1,9);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_ACTION,0,10);gridPaneInScrollPaneJobDetails.add(textField__JOB_ACTION,1,10);
                                                                                gridPaneInScrollPaneJobDetails.add(label__NUMBER_OF_ARGUMENTS,0,11);gridPaneInScrollPaneJobDetails.add(textField__NUMBER_OF_ARGUMENTS,1,11);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SCHEDULE_OWNER,0,12);gridPaneInScrollPaneJobDetails.add(textField__SCHEDULE_OWNER,1,12);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SCHEDULE_NAME,0,13);gridPaneInScrollPaneJobDetails.add(textField__SCHEDULE_NAME,1,13);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SCHEDULE_TYPE,0,14);gridPaneInScrollPaneJobDetails.add(textField__SCHEDULE_TYPE,1,14);
                                                                                gridPaneInScrollPaneJobDetails.add(label__START_DATE,0,15);gridPaneInScrollPaneJobDetails.add(textField__START_DATE,1,15);
                                                                                gridPaneInScrollPaneJobDetails.add(label__REPEAT_INTERVAL,0,16);gridPaneInScrollPaneJobDetails.add(textField__REPEAT_INTERVAL,1,16);
                                                                                gridPaneInScrollPaneJobDetails.add(label__EVENT_QUEUE_OWNER,0,17);gridPaneInScrollPaneJobDetails.add(textField__EVENT_QUEUE_OWNER,1,17);
                                                                                gridPaneInScrollPaneJobDetails.add(label__EVENT_QUEUE_NAME,0,18);gridPaneInScrollPaneJobDetails.add(textField__EVENT_QUEUE_NAME,1,18);
                                                                                gridPaneInScrollPaneJobDetails.add(label__EVENT_QUEUE_AGENT,0,19);gridPaneInScrollPaneJobDetails.add(textField__EVENT_QUEUE_AGENT,1,19);
                                                                                gridPaneInScrollPaneJobDetails.add(label__EVENT_CONDITION,0,20);gridPaneInScrollPaneJobDetails.add(textField__EVENT_CONDITION,1,20);
                                                                                gridPaneInScrollPaneJobDetails.add(label__EVENT_RULE,0,21);gridPaneInScrollPaneJobDetails.add(textField__EVENT_RULE,1,21);
                                                                                gridPaneInScrollPaneJobDetails.add(label__FILE_WATCHER_OWNER,0,22);gridPaneInScrollPaneJobDetails.add(textField__FILE_WATCHER_OWNER,1,22);
                                                                                gridPaneInScrollPaneJobDetails.add(label__FILE_WATCHER_NAME,0,23);gridPaneInScrollPaneJobDetails.add(textField__FILE_WATCHER_NAME,1,23);
                                                                                gridPaneInScrollPaneJobDetails.add(label__END_DATE,0,24);gridPaneInScrollPaneJobDetails.add(textField__END_DATE,1,24);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_CLASS,0,25);gridPaneInScrollPaneJobDetails.add(textField__JOB_CLASS,1,25);
                                                                                gridPaneInScrollPaneJobDetails.add(label__ENABLED,0,26);gridPaneInScrollPaneJobDetails.add(textField__ENABLED,1,26);
                                                                                gridPaneInScrollPaneJobDetails.add(label__AUTO_DROP,0,27);gridPaneInScrollPaneJobDetails.add(textField__AUTO_DROP,1,27);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RESTART_ON_RECOVERY,0,28);gridPaneInScrollPaneJobDetails.add(textField__RESTART_ON_RECOVERY,1,28);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RESTART_ON_FAILURE,0,29);gridPaneInScrollPaneJobDetails.add(textField__RESTART_ON_FAILURE,1,29);
                                                                                gridPaneInScrollPaneJobDetails.add(label__STATE,0,30);gridPaneInScrollPaneJobDetails.add(textField__STATE,1,30);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_PRIORITY,0,31);gridPaneInScrollPaneJobDetails.add(textField__JOB_PRIORITY,1,31);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RUN_COUNT,0,32);gridPaneInScrollPaneJobDetails.add(textField__RUN_COUNT,1,32);
                                                                                gridPaneInScrollPaneJobDetails.add(label__UPTIME_RUN_COUNT,0,33);gridPaneInScrollPaneJobDetails.add(textField__UPTIME_RUN_COUNT,1,33);
                                                                                gridPaneInScrollPaneJobDetails.add(label__MAX_RUNS,0,34);gridPaneInScrollPaneJobDetails.add(textField__MAX_RUNS,1,34);
                                                                                gridPaneInScrollPaneJobDetails.add(label__FAILURE_COUNT,0,35);gridPaneInScrollPaneJobDetails.add(textField__FAILURE_COUNT,1,35);
                                                                                gridPaneInScrollPaneJobDetails.add(label__UPTIME_FAILURE_COUNT,0,36);gridPaneInScrollPaneJobDetails.add(textField__UPTIME_FAILURE_COUNT,1,36);
                                                                                gridPaneInScrollPaneJobDetails.add(label__MAX_FAILURES,0,37);gridPaneInScrollPaneJobDetails.add(textField__MAX_FAILURES,1,37);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RETRY_COUNT,0,38);gridPaneInScrollPaneJobDetails.add(textField__RETRY_COUNT,1,38);
                                                                                gridPaneInScrollPaneJobDetails.add(label__LAST_START_DATE,0,39);gridPaneInScrollPaneJobDetails.add(textField__LAST_START_DATE,1,39);
                                                                                gridPaneInScrollPaneJobDetails.add(label__LAST_RUN_DURATION,0,40);gridPaneInScrollPaneJobDetails.add(textField__LAST_RUN_DURATION,1,40);
                                                                                gridPaneInScrollPaneJobDetails.add(label__NEXT_RUN_DATE,0,41);gridPaneInScrollPaneJobDetails.add(textField__NEXT_RUN_DATE,1,41);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SCHEDULE_LIMIT,0,42);gridPaneInScrollPaneJobDetails.add(textField__SCHEDULE_LIMIT,1,42);
                                                                                gridPaneInScrollPaneJobDetails.add(label__MAX_RUN_DURATION,0,43);gridPaneInScrollPaneJobDetails.add(textField__MAX_RUN_DURATION,1,43);
                                                                                gridPaneInScrollPaneJobDetails.add(label__LOGGING_LEVEL,0,44);gridPaneInScrollPaneJobDetails.add(textField__LOGGING_LEVEL,1,44);
                                                                                gridPaneInScrollPaneJobDetails.add(label__STORE_OUTPUT,0,45);gridPaneInScrollPaneJobDetails.add(textField__STORE_OUTPUT,1,45);
                                                                                gridPaneInScrollPaneJobDetails.add(label__STOP_ON_WINDOW_CLOSE,0,46);gridPaneInScrollPaneJobDetails.add(textField__STOP_ON_WINDOW_CLOSE,1,46);
                                                                                gridPaneInScrollPaneJobDetails.add(label__INSTANCE_STICKINESS,0,47);gridPaneInScrollPaneJobDetails.add(textField__INSTANCE_STICKINESS,1,47);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RAISE_EVENTS,0,48);gridPaneInScrollPaneJobDetails.add(textField__RAISE_EVENTS,1,48);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SYSTEM,0,49);gridPaneInScrollPaneJobDetails.add(textField__SYSTEM,1,49);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_WEIGHT,0,50);gridPaneInScrollPaneJobDetails.add(textField__JOB_WEIGHT,1,50);
                                                                                gridPaneInScrollPaneJobDetails.add(label__NLS_ENV,0,51);gridPaneInScrollPaneJobDetails.add(textField__NLS_ENV,1,51);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SOURCE,0,52);gridPaneInScrollPaneJobDetails.add(textField__SOURCE,1,52);
                                                                                gridPaneInScrollPaneJobDetails.add(label__NUMBER_OF_DESTINATIONS,0,53);gridPaneInScrollPaneJobDetails.add(textField__NUMBER_OF_DESTINATIONS,1,53);
                                                                                gridPaneInScrollPaneJobDetails.add(label__DESTINATION_OWNER,0,54);gridPaneInScrollPaneJobDetails.add(textField__DESTINATION_OWNER,1,54);
                                                                                gridPaneInScrollPaneJobDetails.add(label__DESTINATION,0,55);gridPaneInScrollPaneJobDetails.add(textField__DESTINATION,1,55);
                                                                                gridPaneInScrollPaneJobDetails.add(label__CREDENTIAL_OWNER,0,56);gridPaneInScrollPaneJobDetails.add(textField__CREDENTIAL_OWNER,1,56);
                                                                                gridPaneInScrollPaneJobDetails.add(label__CREDENTIAL_NAME,0,57);gridPaneInScrollPaneJobDetails.add(textField__CREDENTIAL_NAME,1,57);
                                                                                gridPaneInScrollPaneJobDetails.add(label__INSTANCE_ID,0,58);gridPaneInScrollPaneJobDetails.add(textField__INSTANCE_ID,1,58);
                                                                                gridPaneInScrollPaneJobDetails.add(label__DEFERRED_DROP,0,59);gridPaneInScrollPaneJobDetails.add(textField__DEFERRED_DROP,1,59);
                                                                                gridPaneInScrollPaneJobDetails.add(label__ALLOW_RUNS_IN_RESTRICTED_MODE,0,60);gridPaneInScrollPaneJobDetails.add(textField__ALLOW_RUNS_IN_RESTRICTED_MODE,1,60);
                                                                                gridPaneInScrollPaneJobDetails.add(label__COMMENTS,0,61);gridPaneInScrollPaneJobDetails.add(textField__COMMENTS,1,61);
                                                                                gridPaneInScrollPaneJobDetails.add(label__FLAGS,0,62);gridPaneInScrollPaneJobDetails.add(textField__FLAGS,1,62);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RESTARTABLE,0,63);gridPaneInScrollPaneJobDetails.add(textField__RESTARTABLE,1,63);
                                                                                gridPaneInScrollPaneJobDetails.add(label__HAS_CONSTRAINTS,0,64);gridPaneInScrollPaneJobDetails.add(textField__HAS_CONSTRAINTS,1,64);
                                                                                gridPaneInScrollPaneJobDetails.add(label__CONNECT_CREDENTIAL_OWNER,0,65);gridPaneInScrollPaneJobDetails.add(textField__CONNECT_CREDENTIAL_OWNER,1,65);
                                                                                gridPaneInScrollPaneJobDetails.add(label__CONNECT_CREDENTIAL_NAME,0,66);gridPaneInScrollPaneJobDetails.add(textField__CONNECT_CREDENTIAL_NAME,1,66);
                                                                                gridPaneInScrollPaneJobDetails.add(label__FAIL_ON_SCRIPT_ERROR,0,67);gridPaneInScrollPaneJobDetails.add(textField__FAIL_ON_SCRIPT_ERROR,1,67);


                                                                            }
                                                                        }catch(Exception e){System.out.println(e);}
                                                                        dialogJobDetails.getDialogPane().setContent(dialogJobDetailsGridPane);
                                                                        dialogJobDetails.show();
                                                                    }
                                                                });


                                                                TextField REQ_START_DATE__TEXT = new TextField(resultSetSelectLastRunningJobsByOwnerName.getString("STATUS"));REQ_START_DATE__TEXT.setEditable(false);gridPaneInScrollPaneLastRunningJobs.add(REQ_START_DATE__TEXT,3,iLastRunningJobsIndex);REQ_START_DATE__TEXT.setMinWidth(80);REQ_START_DATE__TEXT.setPrefWidth(80);REQ_START_DATE__TEXT.setMaxWidth(80);REQ_START_DATE__TEXT.setStyle("-fx-font-size:10");
                                                                TextField ACTUAL_START_DATE__TEXT = new TextField(resultSetSelectLastRunningJobsByOwnerName.getString("ACTUAL_START_DATE"));ACTUAL_START_DATE__TEXT.setEditable(false);gridPaneInScrollPaneLastRunningJobs.add(ACTUAL_START_DATE__TEXT,4,iLastRunningJobsIndex);ACTUAL_START_DATE__TEXT.setMinWidth(170);ACTUAL_START_DATE__TEXT.setPrefWidth(170);ACTUAL_START_DATE__TEXT.setMaxWidth(170);ACTUAL_START_DATE__TEXT.setStyle("-fx-font-size:10");
                                                                iLastRunningJobsIndex++;
                                                            }
                                                        }catch(Exception e){//blad
                                                            System.out.println(e);
                                                        }
                                                        paneLastRunningJobs.getChildren().add(gridPaneLastRunningJobs);

                                                        //
                                                        //GridPane gridPaneAllSchedulerJobs = new GridPane();
                                                        //dialogSchedulerGridPane.add(gridPaneAllSchedulerJobs,1,3);


                                                        Pane paneAllSchedulerJobs = new Pane();
                                                        paneAllSchedulerJobs.setMinWidth(1025);paneAllSchedulerJobs.setPrefWidth(1025);
                                                        paneAllSchedulerJobs.setMinHeight(700);paneAllSchedulerJobs.setPrefHeight(700);
                                                        GridPane gridPaneAllSchedulerJobs = new GridPane();
                                                        Label labelAllSchedulerJobs = new Label("All scheduler jobs:");labelAllSchedulerJobs.setStyle("-fx-font-size:15");
                                                        gridPaneAllSchedulerJobs.add(labelAllSchedulerJobs,0,0,6,1);
                                                        Label labelAllSchedulerJobs__OWNER = new Label("OWNER");labelAllSchedulerJobs__OWNER.setPrefWidth(100);labelAllSchedulerJobs__OWNER.setStyle("-fx-border-style:solid;-fx-border-color:black;-fx-border-width:1;-fx-alignment:center");
                                                        Label labelAllSchedulerJobs__JOB_NAME = new Label("JOB_NAME");labelAllSchedulerJobs__JOB_NAME.setPrefWidth(140);labelAllSchedulerJobs__JOB_NAME.setStyle("-fx-border-style:solid;-fx-border-color:black;-fx-border-width:1;-fx-alignment:center");
                                                        Label labelAllSchedulerJobs__ENABLED = new Label("ENABLED");labelAllSchedulerJobs__ENABLED.setPrefWidth(80);labelAllSchedulerJobs__ENABLED.setStyle("-fx-border-style:solid;-fx-border-color:black;-fx-border-width:1;-fx-alignment:center");
                                                        Label labelAllSchedulerJobs__STATE = new Label("STATE");labelAllSchedulerJobs__STATE.setPrefWidth(80);labelAllSchedulerJobs__STATE.setStyle("-fx-border-style:solid;-fx-border-color:black;-fx-border-width:1;-fx-alignment:center");
                                                        Label labelAllSchedulerJobs__JOB_TYPE = new Label("JOB_TYPE");labelAllSchedulerJobs__JOB_TYPE.setPrefWidth(110);labelAllSchedulerJobs__JOB_TYPE.setStyle("-fx-border-style:solid;-fx-border-color:black;-fx-border-width:1;-fx-alignment:center");
                                                        Label labelAllSchedulerJobs__REPEAT_INTERVAL = new Label("REPEAT_INTERVAL");labelAllSchedulerJobs__REPEAT_INTERVAL.setPrefWidth(160);labelAllSchedulerJobs__REPEAT_INTERVAL.setStyle("-fx-border-style:solid;-fx-border-color:black;-fx-border-width:1;-fx-alignment:center");
                                                        Label labelAllSchedulerJobs__LAST_START_DATE = new Label("LAST_START_DATE");labelAllSchedulerJobs__LAST_START_DATE.setPrefWidth(170);labelAllSchedulerJobs__LAST_START_DATE.setStyle("-fx-border-style:solid;-fx-border-color:black;-fx-border-width:1;-fx-alignment:center");
                                                        Label labelAllSchedulerJobs__NEXT_RUN_DATE = new Label("NEXT_RUN_DATE");labelAllSchedulerJobs__NEXT_RUN_DATE.setPrefWidth(170);labelAllSchedulerJobs__NEXT_RUN_DATE.setStyle("-fx-border-style:solid;-fx-border-color:black;-fx-border-width:1;-fx-alignment:center");
                                                        gridPaneAllSchedulerJobs.add(labelAllSchedulerJobs__OWNER,0,1);
                                                        gridPaneAllSchedulerJobs.add(labelAllSchedulerJobs__JOB_NAME,1,1);
                                                        gridPaneAllSchedulerJobs.add(labelAllSchedulerJobs__ENABLED,2,1);
                                                        gridPaneAllSchedulerJobs.add(labelAllSchedulerJobs__STATE,3,1);
                                                        gridPaneAllSchedulerJobs.add(labelAllSchedulerJobs__JOB_TYPE,4,1);
                                                        gridPaneAllSchedulerJobs.add(labelAllSchedulerJobs__REPEAT_INTERVAL,5,1);
                                                        gridPaneAllSchedulerJobs.add(labelAllSchedulerJobs__LAST_START_DATE,6,1);
                                                        gridPaneAllSchedulerJobs.add(labelAllSchedulerJobs__NEXT_RUN_DATE,7,1);
                                                        ScrollPane scrollPaneAllSchedulerJobs = new ScrollPane();
                                                        gridPaneAllSchedulerJobs.add(scrollPaneAllSchedulerJobs,0,2,8,1);
                                                        scrollPaneAllSchedulerJobs.setMinWidth(1025);scrollPaneAllSchedulerJobs.setPrefWidth(1025);scrollPaneAllSchedulerJobs.setMinHeight(715);scrollPaneAllSchedulerJobs.setPrefHeight(715);
                                                        GridPane gridPaneInScrollPaneAllSchedulerJobs = new GridPane();
                                                        scrollPaneAllSchedulerJobs.setContent(gridPaneInScrollPaneAllSchedulerJobs);
                                                        //String querySelectAllSchedulerJobsByOwnerName = "select LOG_ID, OWNER, JOB_NAME, STATUS, JOB_TYPE from ALL_SCHEDULER_JOBS where OWNER='"+comboBoxSchedulerOwnerList.getValue().toString()+"' ORDER BY JOB_NAME DESC";
                                                        String querySelectAllSchedulerJobsByOwnerName = "select * from ALL_SCHEDULER_JOBS ORDER BY JOB_NAME DESC";
                                                        try{
                                                            ResultSet resultSetSelectAllSchedulerJobsByOwnerName = statement.executeQuery(querySelectAllSchedulerJobsByOwnerName);
                                                            Integer iAllSchedulerJobsIndex=0;
                                                            while(resultSetSelectAllSchedulerJobsByOwnerName.next()){
                                                                TextField OWNER__TEXT = new TextField(resultSetSelectAllSchedulerJobsByOwnerName.getString("OWNER"));OWNER__TEXT.setEditable(false);gridPaneInScrollPaneAllSchedulerJobs.add(OWNER__TEXT,0,iAllSchedulerJobsIndex);OWNER__TEXT.setMinWidth(100);OWNER__TEXT.setPrefWidth(100);OWNER__TEXT.setMaxWidth(100);OWNER__TEXT.setStyle("-fx-font-size:10");
                                                                TextField JOB_NAME__TEXT = new TextField(resultSetSelectAllSchedulerJobsByOwnerName.getString("JOB_NAME"));JOB_NAME__TEXT.setEditable(false);gridPaneInScrollPaneAllSchedulerJobs.add(JOB_NAME__TEXT,1,iAllSchedulerJobsIndex);JOB_NAME__TEXT.setMinWidth(140);JOB_NAME__TEXT.setPrefWidth(140);JOB_NAME__TEXT.setMaxWidth(140);JOB_NAME__TEXT.setStyle("-fx-font-size:10");

                                                                //JOB_NAME__TEXT.onMouseClickedProperty()
                                                                JOB_NAME__TEXT.setOnMouseEntered(new EventHandler<MouseEvent>(){
                                                                    public void handle(MouseEvent event){
                                                                        JOB_NAME__TEXT.setStyle("-fx-cursor:hand;-fx-font-size:10;-fx-text-fill:blue;");
                                                                    }
                                                                });
                                                                JOB_NAME__TEXT.setOnMouseExited(new EventHandler<MouseEvent>(){
                                                                    public void handle(MouseEvent event){
                                                                        JOB_NAME__TEXT.setStyle("-fx-font-size:10;-fx-text-fill:#383434;");
                                                                    }
                                                                });
                                                                //
                                                                JOB_NAME__TEXT.setOnMouseClicked(new EventHandler<MouseEvent>(){
                                                                    public void handle(MouseEvent event){
                                                                        Alert dialogJobDetails = new Alert(Alert.AlertType.INFORMATION);
                                                                        dialogJobDetails.setWidth(900);dialogJobDetails.setHeight(650);
                                                                        dialogJobDetails.setTitle("Job \""+JOB_NAME__TEXT.getText()+"\" details");
                                                                        dialogJobDetails.setHeaderText(null);
                                                                        GridPane dialogJobDetailsGridPane = new GridPane();
                                                                        dialogJobDetailsGridPane.setVgap(5);dialogJobDetailsGridPane.setHgap(5);
                                                                        //dialogJobDetailsGridPane.add(new Label(""),0,0);
                                                                        //,0,1
                                                                        Label labelJobDetail = new Label("Job \""+JOB_NAME__TEXT.getText()+"\" details");labelJobDetail.setStyle("-fx-font-weight:bold");dialogJobDetailsGridPane.add(labelJobDetail,0,0);
                                                                        String querySelectJobDetails = "select * from all_scheduler_jobs where job_name='"+JOB_NAME__TEXT.getText()+"' and owner='"+OWNER__TEXT.getText()+"'";
                                                                        ScrollPane scrollPaneJobDetails = new ScrollPane();
                                                                        GridPane gridPaneInScrollPaneJobDetails = new GridPane();
                                                                        gridPaneInScrollPaneJobDetails.setVgap(5);gridPaneInScrollPaneJobDetails.setHgap(15);
                                                                        scrollPaneJobDetails.setContent(gridPaneInScrollPaneJobDetails);
                                                                        scrollPaneJobDetails.setMinHeight(600);scrollPaneJobDetails.setMaxHeight(600);scrollPaneJobDetails.setPrefHeight(600);
                                                                        scrollPaneJobDetails.setMinWidth(550);scrollPaneJobDetails.setMaxWidth(550);scrollPaneJobDetails.setPrefWidth(550);
                                                                        dialogJobDetailsGridPane.add(scrollPaneJobDetails,0,2);
                                                                        Label label__OWNER = new Label("LOG_ID");
                                                                        Label label__JOB_NAME = new Label("JOB_NAME");
                                                                        Label label__JOB_SUBNAME = new Label("JOB_SUBNAME");
                                                                        Label label__JOB_STYLE = new Label("JOB_STYLE");
                                                                        Label label__JOB_CREATOR = new Label("JOB_CREATOR");
                                                                        Label label__CLIENT_ID = new Label("CLIENT_ID");
                                                                        Label label__GLOBAL_UID = new Label("GLOBAL_UID");
                                                                        Label label__PROGRAM_OWNER = new Label("PROGRAM_OWNER");
                                                                        Label label__PROGRAM_NAME = new Label("PROGRAM_NAME");
                                                                        Label label__JOB_TYPE = new Label("JOB_TYPE");
                                                                        Label label__JOB_ACTION = new Label("JOB_ACTION");
                                                                        Label label__NUMBER_OF_ARGUMENTS = new Label("NUMBER_OF_ARGUMENTS");
                                                                        Label label__SCHEDULE_OWNER = new Label("SCHEDULE_OWNER");
                                                                        Label label__SCHEDULE_NAME = new Label("SCHEDULE_NAME");
                                                                        Label label__SCHEDULE_TYPE = new Label("SCHEDULE_TYPE");
                                                                        Label label__START_DATE = new Label("START_DATE");
                                                                        Label label__REPEAT_INTERVAL = new Label("REPEAT_INTERVAL");
                                                                        Label label__EVENT_QUEUE_OWNER = new Label("EVENT_QUEUE_OWNER");
                                                                        Label label__EVENT_QUEUE_NAME = new Label("EVENT_QUEUE_NAME");
                                                                        Label label__EVENT_QUEUE_AGENT = new Label("EVENT_QUEUE_AGENT");
                                                                        Label label__EVENT_CONDITION = new Label("EVENT_CONDITION");
                                                                        Label label__EVENT_RULE = new Label("EVENT_RULE");
                                                                        Label label__FILE_WATCHER_OWNER = new Label("FILE_WATCHER_OWNER");
                                                                        Label label__FILE_WATCHER_NAME = new Label("FILE_WATCHER_NAME");
                                                                        Label label__END_DATE = new Label("END_DATE");
                                                                        Label label__JOB_CLASS = new Label("JOB_CLASS");
                                                                        Label label__ENABLED = new Label("ENABLED");
                                                                        Label label__AUTO_DROP = new Label("AUTO_DROP");
                                                                        Label label__RESTART_ON_RECOVERY = new Label("RESTART_ON_RECOVERY");
                                                                        Label label__RESTART_ON_FAILURE = new Label("RESTART_ON_FAILURE");
                                                                        Label label__STATE = new Label("STATE");
                                                                        Label label__JOB_PRIORITY = new Label("JOB_PRIORITY");
                                                                        Label label__RUN_COUNT = new Label("RUN_COUNT");
                                                                        Label label__UPTIME_RUN_COUNT = new Label("UPTIME_RUN_COUNT");
                                                                        Label label__MAX_RUNS = new Label("MAX_RUNS");
                                                                        Label label__FAILURE_COUNT = new Label("FAILURE_COUNT");
                                                                        Label label__UPTIME_FAILURE_COUNT = new Label("UPTIME_FAILURE_COUNT");
                                                                        Label label__MAX_FAILURES = new Label("MAX_FAILURES");
                                                                        Label label__RETRY_COUNT = new Label("RETRY_COUNT");
                                                                        Label label__LAST_START_DATE = new Label("LAST_START_DATE");
                                                                        Label label__LAST_RUN_DURATION = new Label("LAST_RUN_DURATION");
                                                                        Label label__NEXT_RUN_DATE = new Label("NEXT_RUN_DATE");
                                                                        Label label__SCHEDULE_LIMIT = new Label("SCHEDULE_LIMIT");
                                                                        Label label__MAX_RUN_DURATION = new Label("MAX_RUN_DURATION");
                                                                        Label label__LOGGING_LEVEL = new Label("LOGGING_LEVEL");
                                                                        Label label__STORE_OUTPUT = new Label("STORE_OUTPUT");
                                                                        Label label__STOP_ON_WINDOW_CLOSE = new Label("STOP_ON_WINDOW_CLOSE");
                                                                        Label label__INSTANCE_STICKINESS = new Label("INSTANCE_STICKINESS");
                                                                        Label label__RAISE_EVENTS = new Label("RAISE_EVENTS");
                                                                        Label label__SYSTEM = new Label("SYSTEM");
                                                                        Label label__JOB_WEIGHT = new Label("JOB_WEIGHT");
                                                                        Label label__NLS_ENV = new Label("NLS_ENV");
                                                                        Label label__SOURCE = new Label("SOURCE");
                                                                        Label label__NUMBER_OF_DESTINATIONS = new Label("NUMBER_OF_DESTINATIONS");
                                                                        Label label__DESTINATION_OWNER = new Label("DESTINATION_OWNER");
                                                                        Label label__DESTINATION = new Label("DESTINATION");
                                                                        Label label__CREDENTIAL_OWNER = new Label("CREDENTIAL_OWNER");
                                                                        Label label__CREDENTIAL_NAME= new Label("CREDENTIAL_NAME");
                                                                        Label label__INSTANCE_ID= new Label("INSTANCE_ID");
                                                                        Label label__DEFERRED_DROP= new Label("DEFERRED_DROP");
                                                                        Label label__ALLOW_RUNS_IN_RESTRICTED_MODE= new Label("ALLOW_RUNS_IN_RESTRICTED_MODE");
                                                                        Label label__COMMENTS= new Label("COMMENTS");
                                                                        Label label__FLAGS= new Label("FLAGS");
                                                                        Label label__RESTARTABLE= new Label("RESTARTABLE");
                                                                        Label label__HAS_CONSTRAINTS= new Label("HAS_CONSTRAINTS");
                                                                        Label label__CONNECT_CREDENTIAL_OWNER= new Label("CONNECT_CREDENTIAL_OWNER");
                                                                        Label label__CONNECT_CREDENTIAL_NAME= new Label("CONNECT_CREDENTIAL_NAME");
                                                                        Label label__FAIL_ON_SCRIPT_ERROR= new Label("FAIL_ON_SCRIPT_ERROR");


                                                                        try{
                                                                            ResultSet resultSetLogDetails = statement.executeQuery(querySelectJobDetails);
                                                                            while(resultSetLogDetails.next()){
                                                                                TextField textField__LOG_ID = new TextField(resultSetLogDetails.getString("OWNER"));textField__LOG_ID.setEditable(false);textField__LOG_ID.setMinWidth(300);
                                                                                TextField textField__JOB_NAME = new TextField(resultSetLogDetails.getString("JOB_NAME"));textField__JOB_NAME.setEditable(false);textField__JOB_NAME.setMinWidth(300);
                                                                                TextField textField__JOB_SUBNAME = new TextField(resultSetLogDetails.getString("JOB_SUBNAME"));textField__JOB_SUBNAME.setEditable(false);textField__JOB_SUBNAME.setMinWidth(300);
                                                                                TextField textField__JOB_STYLE = new TextField(resultSetLogDetails.getString("JOB_STYLE"));textField__JOB_STYLE.setEditable(false);textField__JOB_STYLE.setMinWidth(300);
                                                                                TextField textField__JOB_CREATOR = new TextField(resultSetLogDetails.getString("JOB_CREATOR"));textField__JOB_CREATOR.setEditable(false);textField__JOB_CREATOR.setMinWidth(300);
                                                                                TextField textField__CLIENT_ID = new TextField(resultSetLogDetails.getString("CLIENT_ID"));textField__CLIENT_ID.setEditable(false);textField__CLIENT_ID.setMinWidth(300);
                                                                                TextField textField__GLOBAL_UID = new TextField(resultSetLogDetails.getString("GLOBAL_UID"));textField__GLOBAL_UID.setEditable(false);textField__GLOBAL_UID.setMinWidth(300);
                                                                                TextField textField__PROGRAM_OWNER = new TextField(resultSetLogDetails.getString("PROGRAM_OWNER"));textField__PROGRAM_OWNER.setEditable(false);textField__PROGRAM_OWNER.setMinWidth(300);
                                                                                TextField textField__PROGRAM_NAME = new TextField(resultSetLogDetails.getString("PROGRAM_NAME"));textField__PROGRAM_NAME.setEditable(false);textField__PROGRAM_NAME.setMinWidth(300);
                                                                                TextField textField__JOB_TYPE = new TextField(resultSetLogDetails.getString("JOB_TYPE"));textField__JOB_TYPE.setEditable(false);textField__JOB_TYPE.setMinWidth(300);
                                                                                TextField textField__JOB_ACTION = new TextField(resultSetLogDetails.getString("JOB_ACTION"));textField__JOB_ACTION.setEditable(false);textField__JOB_ACTION.setMinWidth(300);
                                                                                TextField textField__NUMBER_OF_ARGUMENTS = new TextField(resultSetLogDetails.getString("NUMBER_OF_ARGUMENTS"));textField__NUMBER_OF_ARGUMENTS.setEditable(false);textField__NUMBER_OF_ARGUMENTS.setMinWidth(300);
                                                                                TextField textField__SCHEDULE_OWNER = new TextField(resultSetLogDetails.getString("SCHEDULE_OWNER"));textField__SCHEDULE_OWNER.setEditable(false);textField__SCHEDULE_OWNER.setMinWidth(300);
                                                                                TextField textField__SCHEDULE_NAME = new TextField(resultSetLogDetails.getString("SCHEDULE_NAME"));textField__SCHEDULE_NAME.setEditable(false);textField__SCHEDULE_NAME.setMinWidth(300);
                                                                                TextField textField__SCHEDULE_TYPE = new TextField(resultSetLogDetails.getString("SCHEDULE_TYPE"));textField__SCHEDULE_TYPE.setEditable(false);textField__SCHEDULE_TYPE.setMinWidth(300);
                                                                                TextField textField__START_DATE = new TextField(resultSetLogDetails.getString("START_DATE"));textField__START_DATE.setEditable(false);textField__START_DATE.setMinWidth(300);
                                                                                TextField textField__REPEAT_INTERVAL = new TextField(resultSetLogDetails.getString("REPEAT_INTERVAL"));textField__REPEAT_INTERVAL.setEditable(false);textField__REPEAT_INTERVAL.setMinWidth(300);
                                                                                TextField textField__EVENT_QUEUE_OWNER = new TextField(resultSetLogDetails.getString("EVENT_QUEUE_OWNER"));textField__EVENT_QUEUE_OWNER.setEditable(false);textField__EVENT_QUEUE_OWNER.setMinWidth(300);
                                                                                TextField textField__EVENT_QUEUE_NAME = new TextField(resultSetLogDetails.getString("EVENT_QUEUE_NAME"));textField__EVENT_QUEUE_NAME.setEditable(false);textField__EVENT_QUEUE_NAME.setMinWidth(300);
                                                                                TextField textField__EVENT_QUEUE_AGENT = new TextField(resultSetLogDetails.getString("EVENT_QUEUE_AGENT"));textField__EVENT_QUEUE_AGENT.setEditable(false);textField__EVENT_QUEUE_AGENT.setMinWidth(300);
                                                                                TextField textField__EVENT_CONDITION = new TextField(resultSetLogDetails.getString("EVENT_CONDITION"));textField__EVENT_CONDITION.setEditable(false);textField__EVENT_CONDITION.setMinWidth(300);
                                                                                TextField textField__EVENT_RULE = new TextField(resultSetLogDetails.getString("EVENT_RULE"));textField__EVENT_RULE.setEditable(false);textField__EVENT_RULE.setMinWidth(300);
                                                                                TextField textField__FILE_WATCHER_OWNER = new TextField(resultSetLogDetails.getString("FILE_WATCHER_OWNER"));textField__FILE_WATCHER_OWNER.setEditable(false);textField__FILE_WATCHER_OWNER.setMinWidth(300);
                                                                                TextField textField__FILE_WATCHER_NAME = new TextField(resultSetLogDetails.getString("FILE_WATCHER_NAME"));textField__FILE_WATCHER_NAME.setEditable(false);textField__FILE_WATCHER_NAME.setMinWidth(300);
                                                                                TextField textField__END_DATE = new TextField(resultSetLogDetails.getString("END_DATE"));textField__END_DATE.setEditable(false);textField__END_DATE.setMinWidth(300);
                                                                                TextField textField__JOB_CLASS = new TextField(resultSetLogDetails.getString("JOB_CLASS"));textField__JOB_CLASS.setEditable(false);textField__JOB_CLASS.setMinWidth(300);
                                                                                TextField textField__ENABLED = new TextField(resultSetLogDetails.getString("ENABLED"));textField__ENABLED.setEditable(false);textField__ENABLED.setMinWidth(300);
                                                                                TextField textField__AUTO_DROP = new TextField(resultSetLogDetails.getString("AUTO_DROP"));textField__AUTO_DROP.setEditable(false);textField__AUTO_DROP.setMinWidth(300);
                                                                                TextField textField__RESTART_ON_RECOVERY = new TextField(resultSetLogDetails.getString("RESTART_ON_RECOVERY"));textField__RESTART_ON_RECOVERY.setEditable(false);textField__RESTART_ON_RECOVERY.setMinWidth(300);
                                                                                TextField textField__RESTART_ON_FAILURE = new TextField(resultSetLogDetails.getString("RESTART_ON_FAILURE"));textField__RESTART_ON_FAILURE.setEditable(false);textField__RESTART_ON_FAILURE.setMinWidth(300);
                                                                                TextField textField__STATE = new TextField(resultSetLogDetails.getString("STATE"));textField__STATE.setEditable(false);textField__STATE.setMinWidth(300);
                                                                                TextField textField__JOB_PRIORITY = new TextField(resultSetLogDetails.getString("JOB_PRIORITY"));textField__JOB_PRIORITY.setEditable(false);textField__JOB_PRIORITY.setMinWidth(300);
                                                                                TextField textField__RUN_COUNT = new TextField(resultSetLogDetails.getString("RUN_COUNT"));textField__RUN_COUNT.setEditable(false);textField__RUN_COUNT.setMinWidth(300);
                                                                                TextField textField__UPTIME_RUN_COUNT = new TextField(resultSetLogDetails.getString("UPTIME_RUN_COUNT"));textField__UPTIME_RUN_COUNT.setEditable(false);textField__UPTIME_RUN_COUNT.setMinWidth(300);
                                                                                TextField textField__MAX_RUNS = new TextField(resultSetLogDetails.getString("MAX_RUNS"));textField__MAX_RUNS.setEditable(false);textField__MAX_RUNS.setMinWidth(300);
                                                                                TextField textField__FAILURE_COUNT = new TextField(resultSetLogDetails.getString("FAILURE_COUNT"));textField__FAILURE_COUNT.setEditable(false);textField__FAILURE_COUNT.setMinWidth(300);
                                                                                TextField textField__UPTIME_FAILURE_COUNT = new TextField(resultSetLogDetails.getString("UPTIME_FAILURE_COUNT"));textField__UPTIME_FAILURE_COUNT.setEditable(false);textField__UPTIME_FAILURE_COUNT.setMinWidth(300);
                                                                                TextField textField__MAX_FAILURES = new TextField(resultSetLogDetails.getString("MAX_FAILURES"));textField__MAX_FAILURES.setEditable(false);textField__MAX_FAILURES.setMinWidth(300);
                                                                                TextField textField__RETRY_COUNT = new TextField(resultSetLogDetails.getString("RETRY_COUNT"));textField__RETRY_COUNT.setEditable(false);textField__RETRY_COUNT.setMinWidth(300);
                                                                                TextField textField__LAST_START_DATE = new TextField(resultSetLogDetails.getString("LAST_START_DATE"));textField__LAST_START_DATE.setEditable(false);textField__LAST_START_DATE.setMinWidth(300);
                                                                                TextField textField__LAST_RUN_DURATION = new TextField(resultSetLogDetails.getString("LAST_RUN_DURATION"));textField__LAST_RUN_DURATION.setEditable(false);textField__LAST_RUN_DURATION.setMinWidth(300);
                                                                                TextField textField__NEXT_RUN_DATE = new TextField(resultSetLogDetails.getString("NEXT_RUN_DATE"));textField__NEXT_RUN_DATE.setEditable(false);textField__NEXT_RUN_DATE.setMinWidth(300);
                                                                                TextField textField__SCHEDULE_LIMIT = new TextField(resultSetLogDetails.getString("SCHEDULE_LIMIT"));textField__SCHEDULE_LIMIT.setEditable(false);textField__SCHEDULE_LIMIT.setMinWidth(300);
                                                                                TextField textField__MAX_RUN_DURATION = new TextField(resultSetLogDetails.getString("MAX_RUN_DURATION"));textField__MAX_RUN_DURATION.setEditable(false);textField__MAX_RUN_DURATION.setMinWidth(300);
                                                                                TextField textField__LOGGING_LEVEL = new TextField(resultSetLogDetails.getString("LOGGING_LEVEL"));textField__LOGGING_LEVEL.setEditable(false);textField__LOGGING_LEVEL.setMinWidth(300);
                                                                                TextField textField__STORE_OUTPUT = new TextField(resultSetLogDetails.getString("STORE_OUTPUT"));textField__STORE_OUTPUT.setEditable(false);textField__STORE_OUTPUT.setMinWidth(300);
                                                                                TextField textField__STOP_ON_WINDOW_CLOSE = new TextField(resultSetLogDetails.getString("STOP_ON_WINDOW_CLOSE"));textField__STOP_ON_WINDOW_CLOSE.setEditable(false);textField__STOP_ON_WINDOW_CLOSE.setMinWidth(300);
                                                                                TextField textField__INSTANCE_STICKINESS = new TextField(resultSetLogDetails.getString("INSTANCE_STICKINESS"));textField__INSTANCE_STICKINESS.setEditable(false);textField__INSTANCE_STICKINESS.setMinWidth(300);
                                                                                TextField textField__RAISE_EVENTS = new TextField(resultSetLogDetails.getString("RAISE_EVENTS"));textField__RAISE_EVENTS.setEditable(false);textField__RAISE_EVENTS.setMinWidth(300);
                                                                                TextField textField__SYSTEM = new TextField(resultSetLogDetails.getString("SYSTEM"));textField__SYSTEM.setEditable(false);textField__SYSTEM.setMinWidth(300);
                                                                                TextField textField__JOB_WEIGHT = new TextField(resultSetLogDetails.getString("JOB_WEIGHT"));textField__JOB_WEIGHT.setEditable(false);textField__JOB_WEIGHT.setMinWidth(300);
                                                                                TextField textField__NLS_ENV = new TextField(resultSetLogDetails.getString("NLS_ENV"));textField__NLS_ENV.setEditable(false);textField__NLS_ENV.setMinWidth(300);
                                                                                TextField textField__SOURCE = new TextField(resultSetLogDetails.getString("SOURCE"));textField__SOURCE.setEditable(false);textField__SOURCE.setMinWidth(300);
                                                                                TextField textField__NUMBER_OF_DESTINATIONS = new TextField(resultSetLogDetails.getString("NUMBER_OF_DESTINATIONS"));textField__NUMBER_OF_DESTINATIONS.setEditable(false);textField__NUMBER_OF_DESTINATIONS.setMinWidth(300);
                                                                                TextField textField__DESTINATION_OWNER = new TextField(resultSetLogDetails.getString("DESTINATION_OWNER"));textField__DESTINATION_OWNER.setEditable(false);textField__DESTINATION_OWNER.setMinWidth(300);
                                                                                TextField textField__DESTINATION = new TextField(resultSetLogDetails.getString("DESTINATION"));textField__DESTINATION.setEditable(false);textField__DESTINATION.setMinWidth(300);
                                                                                TextField textField__CREDENTIAL_OWNER = new TextField(resultSetLogDetails.getString("CREDENTIAL_OWNER"));textField__CREDENTIAL_OWNER.setEditable(false);textField__CREDENTIAL_OWNER.setMinWidth(300);
                                                                                TextField textField__CREDENTIAL_NAME = new TextField(resultSetLogDetails.getString("CREDENTIAL_NAME"));textField__CREDENTIAL_NAME.setEditable(false);textField__CREDENTIAL_NAME.setMinWidth(300);
                                                                                TextField textField__INSTANCE_ID = new TextField(resultSetLogDetails.getString("INSTANCE_ID"));textField__INSTANCE_ID.setEditable(false);textField__INSTANCE_ID.setMinWidth(300);
                                                                                TextField textField__DEFERRED_DROP = new TextField(resultSetLogDetails.getString("DEFERRED_DROP"));textField__DEFERRED_DROP.setEditable(false);textField__DEFERRED_DROP.setMinWidth(300);
                                                                                TextField textField__ALLOW_RUNS_IN_RESTRICTED_MODE = new TextField(resultSetLogDetails.getString("ALLOW_RUNS_IN_RESTRICTED_MODE"));textField__ALLOW_RUNS_IN_RESTRICTED_MODE.setEditable(false);textField__ALLOW_RUNS_IN_RESTRICTED_MODE.setMinWidth(300);
                                                                                TextField textField__COMMENTS = new TextField(resultSetLogDetails.getString("COMMENTS"));textField__COMMENTS.setEditable(false);textField__COMMENTS.setMinWidth(300);
                                                                                TextField textField__FLAGS = new TextField(resultSetLogDetails.getString("FLAGS"));textField__FLAGS.setEditable(false);textField__FLAGS.setMinWidth(300);
                                                                                TextField textField__RESTARTABLE = new TextField(resultSetLogDetails.getString("RESTARTABLE"));textField__RESTARTABLE.setEditable(false);textField__RESTARTABLE.setMinWidth(300);
                                                                                TextField textField__HAS_CONSTRAINTS = new TextField(resultSetLogDetails.getString("HAS_CONSTRAINTS"));textField__HAS_CONSTRAINTS.setEditable(false);textField__HAS_CONSTRAINTS.setMinWidth(300);
                                                                                TextField textField__CONNECT_CREDENTIAL_OWNER = new TextField(resultSetLogDetails.getString("CONNECT_CREDENTIAL_OWNER"));textField__CONNECT_CREDENTIAL_OWNER.setEditable(false);textField__CONNECT_CREDENTIAL_OWNER.setMinWidth(300);
                                                                                TextField textField__CONNECT_CREDENTIAL_NAME = new TextField(resultSetLogDetails.getString("CONNECT_CREDENTIAL_NAME"));textField__CONNECT_CREDENTIAL_NAME.setEditable(false);textField__CONNECT_CREDENTIAL_NAME.setMinWidth(300);
                                                                                TextField textField__FAIL_ON_SCRIPT_ERROR = new TextField(resultSetLogDetails.getString("FAIL_ON_SCRIPT_ERROR"));textField__FAIL_ON_SCRIPT_ERROR.setEditable(false);textField__FAIL_ON_SCRIPT_ERROR.setMinWidth(300);


                                                                                gridPaneInScrollPaneJobDetails.add(label__OWNER,0,0);gridPaneInScrollPaneJobDetails.add(textField__LOG_ID,1,0);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_NAME,0,1);gridPaneInScrollPaneJobDetails.add(textField__JOB_NAME,1,1);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_SUBNAME,0,2);gridPaneInScrollPaneJobDetails.add(textField__JOB_SUBNAME,1,2);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_STYLE,0,3);gridPaneInScrollPaneJobDetails.add(textField__JOB_STYLE,1,3);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_CREATOR,0,4);gridPaneInScrollPaneJobDetails.add(textField__JOB_CREATOR,1,4);
                                                                                gridPaneInScrollPaneJobDetails.add(label__CLIENT_ID,0,5);gridPaneInScrollPaneJobDetails.add(textField__CLIENT_ID,1,5);
                                                                                gridPaneInScrollPaneJobDetails.add(label__GLOBAL_UID,0,6);gridPaneInScrollPaneJobDetails.add(textField__GLOBAL_UID,1,6);
                                                                                gridPaneInScrollPaneJobDetails.add(label__PROGRAM_OWNER,0,7);gridPaneInScrollPaneJobDetails.add(textField__PROGRAM_OWNER,1,7);
                                                                                gridPaneInScrollPaneJobDetails.add(label__PROGRAM_NAME,0,8);gridPaneInScrollPaneJobDetails.add(textField__PROGRAM_NAME,1,8);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_TYPE,0,9);gridPaneInScrollPaneJobDetails.add(textField__JOB_TYPE,1,9);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_ACTION,0,10);gridPaneInScrollPaneJobDetails.add(textField__JOB_ACTION,1,10);
                                                                                gridPaneInScrollPaneJobDetails.add(label__NUMBER_OF_ARGUMENTS,0,11);gridPaneInScrollPaneJobDetails.add(textField__NUMBER_OF_ARGUMENTS,1,11);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SCHEDULE_OWNER,0,12);gridPaneInScrollPaneJobDetails.add(textField__SCHEDULE_OWNER,1,12);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SCHEDULE_NAME,0,13);gridPaneInScrollPaneJobDetails.add(textField__SCHEDULE_NAME,1,13);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SCHEDULE_TYPE,0,14);gridPaneInScrollPaneJobDetails.add(textField__SCHEDULE_TYPE,1,14);
                                                                                gridPaneInScrollPaneJobDetails.add(label__START_DATE,0,15);gridPaneInScrollPaneJobDetails.add(textField__START_DATE,1,15);
                                                                                gridPaneInScrollPaneJobDetails.add(label__REPEAT_INTERVAL,0,16);gridPaneInScrollPaneJobDetails.add(textField__REPEAT_INTERVAL,1,16);
                                                                                gridPaneInScrollPaneJobDetails.add(label__EVENT_QUEUE_OWNER,0,17);gridPaneInScrollPaneJobDetails.add(textField__EVENT_QUEUE_OWNER,1,17);
                                                                                gridPaneInScrollPaneJobDetails.add(label__EVENT_QUEUE_NAME,0,18);gridPaneInScrollPaneJobDetails.add(textField__EVENT_QUEUE_NAME,1,18);
                                                                                gridPaneInScrollPaneJobDetails.add(label__EVENT_QUEUE_AGENT,0,19);gridPaneInScrollPaneJobDetails.add(textField__EVENT_QUEUE_AGENT,1,19);
                                                                                gridPaneInScrollPaneJobDetails.add(label__EVENT_CONDITION,0,20);gridPaneInScrollPaneJobDetails.add(textField__EVENT_CONDITION,1,20);
                                                                                gridPaneInScrollPaneJobDetails.add(label__EVENT_RULE,0,21);gridPaneInScrollPaneJobDetails.add(textField__EVENT_RULE,1,21);
                                                                                gridPaneInScrollPaneJobDetails.add(label__FILE_WATCHER_OWNER,0,22);gridPaneInScrollPaneJobDetails.add(textField__FILE_WATCHER_OWNER,1,22);
                                                                                gridPaneInScrollPaneJobDetails.add(label__FILE_WATCHER_NAME,0,23);gridPaneInScrollPaneJobDetails.add(textField__FILE_WATCHER_NAME,1,23);
                                                                                gridPaneInScrollPaneJobDetails.add(label__END_DATE,0,24);gridPaneInScrollPaneJobDetails.add(textField__END_DATE,1,24);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_CLASS,0,25);gridPaneInScrollPaneJobDetails.add(textField__JOB_CLASS,1,25);
                                                                                gridPaneInScrollPaneJobDetails.add(label__ENABLED,0,26);gridPaneInScrollPaneJobDetails.add(textField__ENABLED,1,26);
                                                                                gridPaneInScrollPaneJobDetails.add(label__AUTO_DROP,0,27);gridPaneInScrollPaneJobDetails.add(textField__AUTO_DROP,1,27);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RESTART_ON_RECOVERY,0,28);gridPaneInScrollPaneJobDetails.add(textField__RESTART_ON_RECOVERY,1,28);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RESTART_ON_FAILURE,0,29);gridPaneInScrollPaneJobDetails.add(textField__RESTART_ON_FAILURE,1,29);
                                                                                gridPaneInScrollPaneJobDetails.add(label__STATE,0,30);gridPaneInScrollPaneJobDetails.add(textField__STATE,1,30);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_PRIORITY,0,31);gridPaneInScrollPaneJobDetails.add(textField__JOB_PRIORITY,1,31);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RUN_COUNT,0,32);gridPaneInScrollPaneJobDetails.add(textField__RUN_COUNT,1,32);
                                                                                gridPaneInScrollPaneJobDetails.add(label__UPTIME_RUN_COUNT,0,33);gridPaneInScrollPaneJobDetails.add(textField__UPTIME_RUN_COUNT,1,33);
                                                                                gridPaneInScrollPaneJobDetails.add(label__MAX_RUNS,0,34);gridPaneInScrollPaneJobDetails.add(textField__MAX_RUNS,1,34);
                                                                                gridPaneInScrollPaneJobDetails.add(label__FAILURE_COUNT,0,35);gridPaneInScrollPaneJobDetails.add(textField__FAILURE_COUNT,1,35);
                                                                                gridPaneInScrollPaneJobDetails.add(label__UPTIME_FAILURE_COUNT,0,36);gridPaneInScrollPaneJobDetails.add(textField__UPTIME_FAILURE_COUNT,1,36);
                                                                                gridPaneInScrollPaneJobDetails.add(label__MAX_FAILURES,0,37);gridPaneInScrollPaneJobDetails.add(textField__MAX_FAILURES,1,37);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RETRY_COUNT,0,38);gridPaneInScrollPaneJobDetails.add(textField__RETRY_COUNT,1,38);
                                                                                gridPaneInScrollPaneJobDetails.add(label__LAST_START_DATE,0,39);gridPaneInScrollPaneJobDetails.add(textField__LAST_START_DATE,1,39);
                                                                                gridPaneInScrollPaneJobDetails.add(label__LAST_RUN_DURATION,0,40);gridPaneInScrollPaneJobDetails.add(textField__LAST_RUN_DURATION,1,40);
                                                                                gridPaneInScrollPaneJobDetails.add(label__NEXT_RUN_DATE,0,41);gridPaneInScrollPaneJobDetails.add(textField__NEXT_RUN_DATE,1,41);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SCHEDULE_LIMIT,0,42);gridPaneInScrollPaneJobDetails.add(textField__SCHEDULE_LIMIT,1,42);
                                                                                gridPaneInScrollPaneJobDetails.add(label__MAX_RUN_DURATION,0,43);gridPaneInScrollPaneJobDetails.add(textField__MAX_RUN_DURATION,1,43);
                                                                                gridPaneInScrollPaneJobDetails.add(label__LOGGING_LEVEL,0,44);gridPaneInScrollPaneJobDetails.add(textField__LOGGING_LEVEL,1,44);
                                                                                gridPaneInScrollPaneJobDetails.add(label__STORE_OUTPUT,0,45);gridPaneInScrollPaneJobDetails.add(textField__STORE_OUTPUT,1,45);
                                                                                gridPaneInScrollPaneJobDetails.add(label__STOP_ON_WINDOW_CLOSE,0,46);gridPaneInScrollPaneJobDetails.add(textField__STOP_ON_WINDOW_CLOSE,1,46);
                                                                                gridPaneInScrollPaneJobDetails.add(label__INSTANCE_STICKINESS,0,47);gridPaneInScrollPaneJobDetails.add(textField__INSTANCE_STICKINESS,1,47);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RAISE_EVENTS,0,48);gridPaneInScrollPaneJobDetails.add(textField__RAISE_EVENTS,1,48);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SYSTEM,0,49);gridPaneInScrollPaneJobDetails.add(textField__SYSTEM,1,49);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_WEIGHT,0,50);gridPaneInScrollPaneJobDetails.add(textField__JOB_WEIGHT,1,50);
                                                                                gridPaneInScrollPaneJobDetails.add(label__NLS_ENV,0,51);gridPaneInScrollPaneJobDetails.add(textField__NLS_ENV,1,51);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SOURCE,0,52);gridPaneInScrollPaneJobDetails.add(textField__SOURCE,1,52);
                                                                                gridPaneInScrollPaneJobDetails.add(label__NUMBER_OF_DESTINATIONS,0,53);gridPaneInScrollPaneJobDetails.add(textField__NUMBER_OF_DESTINATIONS,1,53);
                                                                                gridPaneInScrollPaneJobDetails.add(label__DESTINATION_OWNER,0,54);gridPaneInScrollPaneJobDetails.add(textField__DESTINATION_OWNER,1,54);
                                                                                gridPaneInScrollPaneJobDetails.add(label__DESTINATION,0,55);gridPaneInScrollPaneJobDetails.add(textField__DESTINATION,1,55);
                                                                                gridPaneInScrollPaneJobDetails.add(label__CREDENTIAL_OWNER,0,56);gridPaneInScrollPaneJobDetails.add(textField__CREDENTIAL_OWNER,1,56);
                                                                                gridPaneInScrollPaneJobDetails.add(label__CREDENTIAL_NAME,0,57);gridPaneInScrollPaneJobDetails.add(textField__CREDENTIAL_NAME,1,57);
                                                                                gridPaneInScrollPaneJobDetails.add(label__INSTANCE_ID,0,58);gridPaneInScrollPaneJobDetails.add(textField__INSTANCE_ID,1,58);
                                                                                gridPaneInScrollPaneJobDetails.add(label__DEFERRED_DROP,0,59);gridPaneInScrollPaneJobDetails.add(textField__DEFERRED_DROP,1,59);
                                                                                gridPaneInScrollPaneJobDetails.add(label__ALLOW_RUNS_IN_RESTRICTED_MODE,0,60);gridPaneInScrollPaneJobDetails.add(textField__ALLOW_RUNS_IN_RESTRICTED_MODE,1,60);
                                                                                gridPaneInScrollPaneJobDetails.add(label__COMMENTS,0,61);gridPaneInScrollPaneJobDetails.add(textField__COMMENTS,1,61);
                                                                                gridPaneInScrollPaneJobDetails.add(label__FLAGS,0,62);gridPaneInScrollPaneJobDetails.add(textField__FLAGS,1,62);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RESTARTABLE,0,63);gridPaneInScrollPaneJobDetails.add(textField__RESTARTABLE,1,63);
                                                                                gridPaneInScrollPaneJobDetails.add(label__HAS_CONSTRAINTS,0,64);gridPaneInScrollPaneJobDetails.add(textField__HAS_CONSTRAINTS,1,64);
                                                                                gridPaneInScrollPaneJobDetails.add(label__CONNECT_CREDENTIAL_OWNER,0,65);gridPaneInScrollPaneJobDetails.add(textField__CONNECT_CREDENTIAL_OWNER,1,65);
                                                                                gridPaneInScrollPaneJobDetails.add(label__CONNECT_CREDENTIAL_NAME,0,66);gridPaneInScrollPaneJobDetails.add(textField__CONNECT_CREDENTIAL_NAME,1,66);
                                                                                gridPaneInScrollPaneJobDetails.add(label__FAIL_ON_SCRIPT_ERROR,0,67);gridPaneInScrollPaneJobDetails.add(textField__FAIL_ON_SCRIPT_ERROR,1,67);


                                                                            }
                                                                        }catch(Exception e){System.out.println(e);}
                                                                        dialogJobDetails.getDialogPane().setContent(dialogJobDetailsGridPane);
                                                                        dialogJobDetails.show();
                                                                    }
                                                                });


                                                                TextField ENABLED__TEXT = new TextField(resultSetSelectAllSchedulerJobsByOwnerName.getString("ENABLED"));ENABLED__TEXT.setEditable(false);gridPaneInScrollPaneAllSchedulerJobs.add(ENABLED__TEXT,2,iAllSchedulerJobsIndex);ENABLED__TEXT.setMinWidth(80);ENABLED__TEXT.setPrefWidth(80);ENABLED__TEXT.setMaxWidth(80);ENABLED__TEXT.setStyle("-fx-font-size:10");
                                                                TextField STATE__TEXT = new TextField(resultSetSelectAllSchedulerJobsByOwnerName.getString("STATE"));STATE__TEXT.setEditable(false);gridPaneInScrollPaneAllSchedulerJobs.add(STATE__TEXT,3,iAllSchedulerJobsIndex);STATE__TEXT.setMinWidth(80);STATE__TEXT.setPrefWidth(80);STATE__TEXT.setMaxWidth(80);STATE__TEXT.setStyle("-fx-font-size:10");
                                                                TextField JOB_TYPE__TEXT = new TextField(resultSetSelectAllSchedulerJobsByOwnerName.getString("JOB_TYPE"));JOB_TYPE__TEXT.setEditable(false);gridPaneInScrollPaneAllSchedulerJobs.add(JOB_TYPE__TEXT,4,iAllSchedulerJobsIndex);JOB_TYPE__TEXT.setMinWidth(110);JOB_TYPE__TEXT.setPrefWidth(110);JOB_TYPE__TEXT.setMaxWidth(110);JOB_TYPE__TEXT.setStyle("-fx-font-size:10");
                                                                TextField REPEAT_INTERVAL__TEXT = new TextField(resultSetSelectAllSchedulerJobsByOwnerName.getString("REPEAT_INTERVAL"));REPEAT_INTERVAL__TEXT.setEditable(false);gridPaneInScrollPaneAllSchedulerJobs.add(REPEAT_INTERVAL__TEXT,5,iAllSchedulerJobsIndex);REPEAT_INTERVAL__TEXT.setMinWidth(160);REPEAT_INTERVAL__TEXT.setPrefWidth(160);REPEAT_INTERVAL__TEXT.setMaxWidth(160);REPEAT_INTERVAL__TEXT.setStyle("-fx-font-size:10");
                                                                TextField LAST_START_DATE__TEXT = new TextField(resultSetSelectAllSchedulerJobsByOwnerName.getString("LAST_START_DATE"));LAST_START_DATE__TEXT.setEditable(false);gridPaneInScrollPaneAllSchedulerJobs.add(LAST_START_DATE__TEXT,6,iAllSchedulerJobsIndex);LAST_START_DATE__TEXT.setMinWidth(170);LAST_START_DATE__TEXT.setPrefWidth(170);LAST_START_DATE__TEXT.setMaxWidth(170);LAST_START_DATE__TEXT.setStyle("-fx-font-size:10");
                                                                TextField NEXT_RUN_DATE__TEXT = new TextField(resultSetSelectAllSchedulerJobsByOwnerName.getString("NEXT_RUN_DATE"));NEXT_RUN_DATE__TEXT.setEditable(false);gridPaneInScrollPaneAllSchedulerJobs.add(NEXT_RUN_DATE__TEXT,7,iAllSchedulerJobsIndex);NEXT_RUN_DATE__TEXT.setMinWidth(170);NEXT_RUN_DATE__TEXT.setPrefWidth(170);NEXT_RUN_DATE__TEXT.setMaxWidth(170);NEXT_RUN_DATE__TEXT.setStyle("-fx-font-size:10");
                                                                iAllSchedulerJobsIndex++;
                                                            }
                                                        }catch(Exception e){//blad
                                                            System.out.println(e);
                                                        }
                                                        paneAllSchedulerJobs.getChildren().add(gridPaneAllSchedulerJobs);


                                                        dialogSchedulerGridPane.add(paneLastRunningJobs,0,3);
                                                        dialogSchedulerGridPane.add(paneAllSchedulerJobs,1,3);
                                                    }else{
                                                        //String querySelectLastRunningJobsByOwnerName = "select LOG_ID, OWNER, JOB_NAME, STATUS, ACTUAL_START_DATE from ALL_SCHEDULER_JOB_RUN_DETAILS where OWNER='"+comboBoxSchedulerOwnerList.getValue().toString()+"' ORDER BY LOG_ID DESC FETCH FIRST 50 ROWS ONLY";
                                                        String querySelectLastRunningJobsByOwnerName = "select * from ALL_SCHEDULER_JOB_RUN_DETAILS where OWNER='"+comboBoxSchedulerOwnerList.getValue().toString()+"' ORDER BY LOG_ID DESC FETCH FIRST 50 ROWS ONLY";
                                                        try{
                                                            ResultSet resultSetSelectLastRunningJobsByOwnerName = statement.executeQuery(querySelectLastRunningJobsByOwnerName);
                                                            Integer iLastRunningJobsIndex=0;
                                                            while(resultSetSelectLastRunningJobsByOwnerName.next()){
                                                                TextField LOG_ID__TEXT = new TextField(resultSetSelectLastRunningJobsByOwnerName.getString("LOG_ID"));LOG_ID__TEXT.setEditable(false);gridPaneInScrollPaneLastRunningJobs.add(LOG_ID__TEXT,0,iLastRunningJobsIndex);LOG_ID__TEXT.setMinWidth(80);LOG_ID__TEXT.setPrefWidth(80);LOG_ID__TEXT.setMaxWidth(80);LOG_ID__TEXT.setStyle("-fx-font-size:10");
                                                                LOG_ID__TEXT.setOnMouseEntered(new EventHandler<MouseEvent>(){
                                                                    public void handle(MouseEvent event){
                                                                        LOG_ID__TEXT.setStyle("-fx-cursor:hand;-fx-font-size:10;-fx-text-fill:blue;");
                                                                    }
                                                                });
                                                                LOG_ID__TEXT.setOnMouseExited(new EventHandler<MouseEvent>(){
                                                                    public void handle(MouseEvent event){
                                                                        LOG_ID__TEXT.setStyle("-fx-font-size:10;-fx-text-fill:#383434;");
                                                                    }
                                                                });
                                                                LOG_ID__TEXT.setOnMouseClicked(new EventHandler<MouseEvent>(){
                                                                    public void handle(MouseEvent event){
                                                                        Alert dialogJobLogDetails = new Alert(Alert.AlertType.INFORMATION);
                                                                        dialogJobLogDetails.setTitle("LOG_ID \""+LOG_ID__TEXT.getText()+"\" details");
                                                                        dialogJobLogDetails.setHeaderText(null);
                                                                        GridPane dialogJobLogDetailsGridPane = new GridPane();
                                                                        dialogJobLogDetailsGridPane.setVgap(5);dialogJobLogDetailsGridPane.setHgap(5);
                                                                        Label labelJobLogDetail = new Label("LOG_ID \""+LOG_ID__TEXT.getText()+"\" details");labelJobLogDetail.setStyle("-fx-font-weight:bold");dialogJobLogDetailsGridPane.add(labelJobLogDetail,0,0);
                                                                        dialogJobLogDetailsGridPane.add(new Label(""),0,1);
                                                                        String querySelectLogDetails = "select * from all_scheduler_job_run_details where log_id='"+LOG_ID__TEXT.getText()+"'";

                                                                        Label label__LOG_ID = new Label("LOG_ID");
                                                                        Label label__LOG_DATE = new Label("LOG_DATE");
                                                                        Label label__OWNER = new Label("OWNER");
                                                                        Label label__JOB_NAME = new Label("JOB_NAME");
                                                                        Label label__JOB_SUBNAME = new Label("JOB_SUBNAME");
                                                                        Label label__STATUS = new Label("STATUS");
                                                                        Label label__ERROR = new Label("ERROR#");
                                                                        Label label__REQ_START_DATE = new Label("REQ_START_DATE");
                                                                        Label label__ACTUAL_START_DATE = new Label("ACTUAL_START_DATE");
                                                                        Label label__RUN_DURATION = new Label("RUN_DURATION");
                                                                        Label label__INSTANCE_ID = new Label("INSTANCE_ID");
                                                                        Label label__SESSION_ID = new Label("SESSION_ID");
                                                                        Label label__SLAVE_PID = new Label("SLAVE_PID");
                                                                        Label label__CPU_USED = new Label("CPU_USED");
                                                                        Label label__CREDENTIAL_OWNER = new Label("CREDENTIAL_OWNER");
                                                                        Label label__CREDENTIAL_NAME = new Label("CREDENTIAL_NAME");
                                                                        Label label__DESTINATION_OWNER = new Label("DESTINATION_OWNER");
                                                                        Label label__DESTINATION = new Label("DESTINATION");
                                                                        Label label__ADDITIONAL_INFO = new Label("ADDITIONAL_INFO");
                                                                        Label label__ERRORS = new Label("ERRORS");
                                                                        Label label__OUTPUT = new Label("OUTPUT");
                                                                        //Label label__BINARY_ERRORS = new Label("BINARY_ERRORS");
                                                                        //Label label__BINARY_OUTPUT = new Label("BINARY_OUTPUT");

                                                                        try{
                                                                            ResultSet resultSetLogDetails = statement.executeQuery(querySelectLogDetails);
                                                                            while(resultSetLogDetails.next()){
                                                                                TextField textField__LOG_ID = new TextField(resultSetLogDetails.getString("LOG_ID"));textField__LOG_ID.setEditable(false);textField__LOG_ID.setMinWidth(300);
                                                                                TextField textField__LOG_DATE = new TextField(resultSetLogDetails.getString("LOG_DATE"));textField__LOG_DATE.setEditable(false);textField__LOG_DATE.setMinWidth(300);
                                                                                TextField textField__OWNER = new TextField(resultSetLogDetails.getString("OWNER"));textField__OWNER.setEditable(false);textField__LOG_DATE.setMinWidth(300);
                                                                                TextField textField__JOB_NAME = new TextField(resultSetLogDetails.getString("JOB_NAME"));textField__JOB_NAME.setEditable(false);textField__JOB_NAME.setMinWidth(300);
                                                                                TextField textField__JOB_SUBNAME = new TextField(resultSetLogDetails.getString("JOB_SUBNAME"));textField__JOB_SUBNAME.setEditable(false);textField__JOB_SUBNAME.setMinWidth(300);
                                                                                TextField textField__STATUS = new TextField(resultSetLogDetails.getString("STATUS"));textField__STATUS.setEditable(false);textField__STATUS.setMinWidth(300);
                                                                                TextField textField__ERROR = new TextField(resultSetLogDetails.getString("ERROR#"));textField__ERROR.setEditable(false);textField__ERROR.setMinWidth(300);
                                                                                TextField textField__REQ_START_DATE = new TextField(resultSetLogDetails.getString("REQ_START_DATE"));textField__REQ_START_DATE.setEditable(false);textField__REQ_START_DATE.setMinWidth(300);
                                                                                TextField textField__ACTUAL_START_DATE = new TextField(resultSetLogDetails.getString("ACTUAL_START_DATE"));textField__ACTUAL_START_DATE.setEditable(false);textField__ACTUAL_START_DATE.setMinWidth(300);
                                                                                TextField textField__RUN_DURATION = new TextField(resultSetLogDetails.getString("RUN_DURATION"));textField__RUN_DURATION.setEditable(false);textField__RUN_DURATION.setMinWidth(300);
                                                                                TextField textField__INSTANCE_ID = new TextField(resultSetLogDetails.getString("INSTANCE_ID"));textField__INSTANCE_ID.setEditable(false);textField__INSTANCE_ID.setMinWidth(300);
                                                                                TextField textField__SESSION_ID = new TextField(resultSetLogDetails.getString("SESSION_ID"));textField__SESSION_ID.setEditable(false);textField__SESSION_ID.setMinWidth(300);
                                                                                TextField textField__SLAVE_PID = new TextField(resultSetLogDetails.getString("SLAVE_PID"));textField__SLAVE_PID.setEditable(false);textField__SLAVE_PID.setMinWidth(300);
                                                                                TextField textField__CPU_USED = new TextField(resultSetLogDetails.getString("CPU_USED"));textField__CPU_USED.setEditable(false);textField__CPU_USED.setMinWidth(300);
                                                                                TextField textField__CREDENTIAL_OWNER = new TextField(resultSetLogDetails.getString("CREDENTIAL_OWNER"));textField__CREDENTIAL_OWNER.setEditable(false);textField__CREDENTIAL_OWNER.setMinWidth(300);
                                                                                TextField textField__CREDENTIAL_NAME = new TextField(resultSetLogDetails.getString("CREDENTIAL_NAME"));textField__CREDENTIAL_NAME.setEditable(false);textField__CREDENTIAL_NAME.setMinWidth(300);
                                                                                TextField textField__DESTINATION_OWNER = new TextField(resultSetLogDetails.getString("DESTINATION_OWNER"));textField__DESTINATION_OWNER.setEditable(false);textField__CREDENTIAL_NAME.setMinWidth(300);
                                                                                TextField textField__DESTINATION = new TextField(resultSetLogDetails.getString("DESTINATION"));textField__DESTINATION.setEditable(false);textField__DESTINATION.setMinWidth(300);
                                                                                TextField textField__ADDITIONAL_INFO = new TextField(resultSetLogDetails.getString("ADDITIONAL_INFO"));textField__ADDITIONAL_INFO.setEditable(false);textField__ADDITIONAL_INFO.setMinWidth(300);
                                                                                TextField textField__ERRORS = new TextField(resultSetLogDetails.getString("ERRORS"));textField__ERRORS.setEditable(false);textField__ERRORS.setMinWidth(300);
                                                                                TextField textField__OUTPUT = new TextField(resultSetLogDetails.getString("OUTPUT"));textField__OUTPUT.setEditable(false);textField__OUTPUT.setMinWidth(300);
                                                                                //TextField textField__BINARY_ERRORS = new TextField(resultSetLogDetails.getBinaryStream("BINARY_ERRORS").toString());textField__BINARY_ERRORS.setEditable(false);textField__BINARY_ERRORS.setMinWidth(300);
                                                                                //TextField textField__BINARY_OUTPUT = new TextField(resultSetLogDetails.getBinaryStream("BINARY_OUTPUT").toString());textField__BINARY_OUTPUT.setEditable(false);textField__BINARY_OUTPUT.setMinWidth(300);

                                                                                dialogJobLogDetailsGridPane.add(label__LOG_ID,0,2);dialogJobLogDetailsGridPane.add(textField__LOG_ID,1,2);
                                                                                dialogJobLogDetailsGridPane.add(label__LOG_DATE,0,3);dialogJobLogDetailsGridPane.add(textField__LOG_DATE,1,3);
                                                                                dialogJobLogDetailsGridPane.add(label__OWNER,0,4);dialogJobLogDetailsGridPane.add(textField__OWNER,1,4);
                                                                                dialogJobLogDetailsGridPane.add(label__JOB_NAME,0,5);dialogJobLogDetailsGridPane.add(textField__JOB_NAME,1,5);
                                                                                dialogJobLogDetailsGridPane.add(label__JOB_SUBNAME,0,6);dialogJobLogDetailsGridPane.add(textField__JOB_SUBNAME,1,6);
                                                                                dialogJobLogDetailsGridPane.add(label__STATUS,0,7);dialogJobLogDetailsGridPane.add(textField__STATUS,1,7);
                                                                                dialogJobLogDetailsGridPane.add(label__ERROR,0,8);dialogJobLogDetailsGridPane.add(textField__ERROR,1,8);
                                                                                dialogJobLogDetailsGridPane.add(label__REQ_START_DATE,0,9);dialogJobLogDetailsGridPane.add(textField__REQ_START_DATE,1,9);
                                                                                dialogJobLogDetailsGridPane.add(label__ACTUAL_START_DATE,0,10);dialogJobLogDetailsGridPane.add(textField__ACTUAL_START_DATE,1,10);
                                                                                dialogJobLogDetailsGridPane.add(label__RUN_DURATION,0,11);dialogJobLogDetailsGridPane.add(textField__RUN_DURATION,1,11);
                                                                                dialogJobLogDetailsGridPane.add(label__INSTANCE_ID,0,12);dialogJobLogDetailsGridPane.add(textField__INSTANCE_ID,1,12);
                                                                                dialogJobLogDetailsGridPane.add(label__SESSION_ID,0,13);dialogJobLogDetailsGridPane.add(textField__SESSION_ID,1,13);
                                                                                dialogJobLogDetailsGridPane.add(label__SLAVE_PID,0,14);dialogJobLogDetailsGridPane.add(textField__SLAVE_PID,1,14);
                                                                                dialogJobLogDetailsGridPane.add(label__CPU_USED,0,15);dialogJobLogDetailsGridPane.add(textField__CPU_USED,1,15);
                                                                                dialogJobLogDetailsGridPane.add(label__CREDENTIAL_OWNER,0,16);dialogJobLogDetailsGridPane.add(textField__CREDENTIAL_OWNER,1,16);
                                                                                dialogJobLogDetailsGridPane.add(label__CREDENTIAL_NAME,0,17);dialogJobLogDetailsGridPane.add(textField__CREDENTIAL_NAME,1,17);
                                                                                dialogJobLogDetailsGridPane.add(label__DESTINATION_OWNER,0,18);dialogJobLogDetailsGridPane.add(textField__DESTINATION_OWNER,1,18);
                                                                                dialogJobLogDetailsGridPane.add(label__DESTINATION,0,19);dialogJobLogDetailsGridPane.add(textField__DESTINATION,1,19);
                                                                                dialogJobLogDetailsGridPane.add(label__ADDITIONAL_INFO,0,20);dialogJobLogDetailsGridPane.add(textField__ADDITIONAL_INFO,1,20);
                                                                                dialogJobLogDetailsGridPane.add(label__ERRORS,0,21);dialogJobLogDetailsGridPane.add(textField__ERRORS,1,21);
                                                                                dialogJobLogDetailsGridPane.add(label__OUTPUT,0,22);dialogJobLogDetailsGridPane.add(textField__OUTPUT,1,22);
                                                                                //dialogJobLogDetailsGridPane.add(label__BINARY_ERRORS,0,23);dialogJobLogDetailsGridPane.add(textField__BINARY_ERRORS,1,23);
                                                                                //dialogJobLogDetailsGridPane.add(label__BINARY_OUTPUT,0,24);dialogJobLogDetailsGridPane.add(textField__BINARY_OUTPUT,1,24);

                                                                            }
                                                                        }catch(Exception e){System.out.println(e);}
                                                                        dialogJobLogDetails.getDialogPane().setContent(dialogJobLogDetailsGridPane);
                                                                        dialogJobLogDetails.show();
                                                                    }
                                                                });

                                                                TextField OWNER__TEXT = new TextField(resultSetSelectLastRunningJobsByOwnerName.getString("OWNER"));OWNER__TEXT.setEditable(false);gridPaneInScrollPaneLastRunningJobs.add(OWNER__TEXT,1,iLastRunningJobsIndex);OWNER__TEXT.setMinWidth(100);OWNER__TEXT.setPrefWidth(100);OWNER__TEXT.setMaxWidth(100);OWNER__TEXT.setStyle("-fx-font-size:10");
                                                                TextField JOB_NAME__TEXT = new TextField(resultSetSelectLastRunningJobsByOwnerName.getString("JOB_NAME"));JOB_NAME__TEXT.setEditable(false);gridPaneInScrollPaneLastRunningJobs.add(JOB_NAME__TEXT,2,iLastRunningJobsIndex);JOB_NAME__TEXT.setMinWidth(140);JOB_NAME__TEXT.setPrefWidth(140);JOB_NAME__TEXT.setMaxWidth(140);JOB_NAME__TEXT.setStyle("-fx-font-size:10");

                                                                //JOB_NAME__TEXT.onMouseClickedProperty()
                                                                JOB_NAME__TEXT.setOnMouseEntered(new EventHandler<MouseEvent>(){
                                                                    public void handle(MouseEvent event){
                                                                        JOB_NAME__TEXT.setStyle("-fx-cursor:hand;-fx-font-size:10;-fx-text-fill:blue;");
                                                                    }
                                                                });
                                                                JOB_NAME__TEXT.setOnMouseExited(new EventHandler<MouseEvent>(){
                                                                    public void handle(MouseEvent event){
                                                                        JOB_NAME__TEXT.setStyle("-fx-font-size:10;-fx-text-fill:#383434;");
                                                                    }
                                                                });
//
                                                                JOB_NAME__TEXT.setOnMouseClicked(new EventHandler<MouseEvent>(){
                                                                    public void handle(MouseEvent event){
                                                                        Alert dialogJobDetails = new Alert(Alert.AlertType.INFORMATION);
                                                                        dialogJobDetails.setWidth(900);dialogJobDetails.setHeight(650);
                                                                        dialogJobDetails.setTitle("Job \""+JOB_NAME__TEXT.getText()+"\" details");
                                                                        dialogJobDetails.setHeaderText(null);
                                                                        GridPane dialogJobDetailsGridPane = new GridPane();
                                                                        dialogJobDetailsGridPane.setVgap(5);dialogJobDetailsGridPane.setHgap(5);
                                                                        //dialogJobDetailsGridPane.add(new Label(""),0,0);
                                                                        //,0,1
                                                                        Label labelJobDetail = new Label("Job \""+JOB_NAME__TEXT.getText()+"\" details");labelJobDetail.setStyle("-fx-font-weight:bold");dialogJobDetailsGridPane.add(labelJobDetail,0,0);
                                                                        String querySelectJobDetails = "select * from all_scheduler_jobs where job_name='"+JOB_NAME__TEXT.getText()+"' and owner='"+OWNER__TEXT.getText()+"'";
                                                                        ScrollPane scrollPaneJobDetails = new ScrollPane();
                                                                        GridPane gridPaneInScrollPaneJobDetails = new GridPane();
                                                                        gridPaneInScrollPaneJobDetails.setVgap(5);gridPaneInScrollPaneJobDetails.setHgap(15);
                                                                        scrollPaneJobDetails.setContent(gridPaneInScrollPaneJobDetails);
                                                                        scrollPaneJobDetails.setMinHeight(600);scrollPaneJobDetails.setMaxHeight(600);scrollPaneJobDetails.setPrefHeight(600);
                                                                        scrollPaneJobDetails.setMinWidth(550);scrollPaneJobDetails.setMaxWidth(550);scrollPaneJobDetails.setPrefWidth(550);
                                                                        dialogJobDetailsGridPane.add(scrollPaneJobDetails,0,2);
                                                                        Label label__OWNER = new Label("LOG_ID");
                                                                        Label label__JOB_NAME = new Label("JOB_NAME");
                                                                        Label label__JOB_SUBNAME = new Label("JOB_SUBNAME");
                                                                        Label label__JOB_STYLE = new Label("JOB_STYLE");
                                                                        Label label__JOB_CREATOR = new Label("JOB_CREATOR");
                                                                        Label label__CLIENT_ID = new Label("CLIENT_ID");
                                                                        Label label__GLOBAL_UID = new Label("GLOBAL_UID");
                                                                        Label label__PROGRAM_OWNER = new Label("PROGRAM_OWNER");
                                                                        Label label__PROGRAM_NAME = new Label("PROGRAM_NAME");
                                                                        Label label__JOB_TYPE = new Label("JOB_TYPE");
                                                                        Label label__JOB_ACTION = new Label("JOB_ACTION");
                                                                        Label label__NUMBER_OF_ARGUMENTS = new Label("NUMBER_OF_ARGUMENTS");
                                                                        Label label__SCHEDULE_OWNER = new Label("SCHEDULE_OWNER");
                                                                        Label label__SCHEDULE_NAME = new Label("SCHEDULE_NAME");
                                                                        Label label__SCHEDULE_TYPE = new Label("SCHEDULE_TYPE");
                                                                        Label label__START_DATE = new Label("START_DATE");
                                                                        Label label__REPEAT_INTERVAL = new Label("REPEAT_INTERVAL");
                                                                        Label label__EVENT_QUEUE_OWNER = new Label("EVENT_QUEUE_OWNER");
                                                                        Label label__EVENT_QUEUE_NAME = new Label("EVENT_QUEUE_NAME");
                                                                        Label label__EVENT_QUEUE_AGENT = new Label("EVENT_QUEUE_AGENT");
                                                                        Label label__EVENT_CONDITION = new Label("EVENT_CONDITION");
                                                                        Label label__EVENT_RULE = new Label("EVENT_RULE");
                                                                        Label label__FILE_WATCHER_OWNER = new Label("FILE_WATCHER_OWNER");
                                                                        Label label__FILE_WATCHER_NAME = new Label("FILE_WATCHER_NAME");
                                                                        Label label__END_DATE = new Label("END_DATE");
                                                                        Label label__JOB_CLASS = new Label("JOB_CLASS");
                                                                        Label label__ENABLED = new Label("ENABLED");
                                                                        Label label__AUTO_DROP = new Label("AUTO_DROP");
                                                                        Label label__RESTART_ON_RECOVERY = new Label("RESTART_ON_RECOVERY");
                                                                        Label label__RESTART_ON_FAILURE = new Label("RESTART_ON_FAILURE");
                                                                        Label label__STATE = new Label("STATE");
                                                                        Label label__JOB_PRIORITY = new Label("JOB_PRIORITY");
                                                                        Label label__RUN_COUNT = new Label("RUN_COUNT");
                                                                        Label label__UPTIME_RUN_COUNT = new Label("UPTIME_RUN_COUNT");
                                                                        Label label__MAX_RUNS = new Label("MAX_RUNS");
                                                                        Label label__FAILURE_COUNT = new Label("FAILURE_COUNT");
                                                                        Label label__UPTIME_FAILURE_COUNT = new Label("UPTIME_FAILURE_COUNT");
                                                                        Label label__MAX_FAILURES = new Label("MAX_FAILURES");
                                                                        Label label__RETRY_COUNT = new Label("RETRY_COUNT");
                                                                        Label label__LAST_START_DATE = new Label("LAST_START_DATE");
                                                                        Label label__LAST_RUN_DURATION = new Label("LAST_RUN_DURATION");
                                                                        Label label__NEXT_RUN_DATE = new Label("NEXT_RUN_DATE");
                                                                        Label label__SCHEDULE_LIMIT = new Label("SCHEDULE_LIMIT");
                                                                        Label label__MAX_RUN_DURATION = new Label("MAX_RUN_DURATION");
                                                                        Label label__LOGGING_LEVEL = new Label("LOGGING_LEVEL");
                                                                        Label label__STORE_OUTPUT = new Label("STORE_OUTPUT");
                                                                        Label label__STOP_ON_WINDOW_CLOSE = new Label("STOP_ON_WINDOW_CLOSE");
                                                                        Label label__INSTANCE_STICKINESS = new Label("INSTANCE_STICKINESS");
                                                                        Label label__RAISE_EVENTS = new Label("RAISE_EVENTS");
                                                                        Label label__SYSTEM = new Label("SYSTEM");
                                                                        Label label__JOB_WEIGHT = new Label("JOB_WEIGHT");
                                                                        Label label__NLS_ENV = new Label("NLS_ENV");
                                                                        Label label__SOURCE = new Label("SOURCE");
                                                                        Label label__NUMBER_OF_DESTINATIONS = new Label("NUMBER_OF_DESTINATIONS");
                                                                        Label label__DESTINATION_OWNER = new Label("DESTINATION_OWNER");
                                                                        Label label__DESTINATION = new Label("DESTINATION");
                                                                        Label label__CREDENTIAL_OWNER = new Label("CREDENTIAL_OWNER");
                                                                        Label label__CREDENTIAL_NAME= new Label("CREDENTIAL_NAME");
                                                                        Label label__INSTANCE_ID= new Label("INSTANCE_ID");
                                                                        Label label__DEFERRED_DROP= new Label("DEFERRED_DROP");
                                                                        Label label__ALLOW_RUNS_IN_RESTRICTED_MODE= new Label("ALLOW_RUNS_IN_RESTRICTED_MODE");
                                                                        Label label__COMMENTS= new Label("COMMENTS");
                                                                        Label label__FLAGS= new Label("FLAGS");
                                                                        Label label__RESTARTABLE= new Label("RESTARTABLE");
                                                                        Label label__HAS_CONSTRAINTS= new Label("HAS_CONSTRAINTS");
                                                                        Label label__CONNECT_CREDENTIAL_OWNER= new Label("CONNECT_CREDENTIAL_OWNER");
                                                                        Label label__CONNECT_CREDENTIAL_NAME= new Label("CONNECT_CREDENTIAL_NAME");
                                                                        Label label__FAIL_ON_SCRIPT_ERROR= new Label("FAIL_ON_SCRIPT_ERROR");


                                                                        try{
                                                                            ResultSet resultSetLogDetails = statement.executeQuery(querySelectJobDetails);
                                                                            while(resultSetLogDetails.next()){
                                                                                TextField textField__LOG_ID = new TextField(resultSetLogDetails.getString("OWNER"));textField__LOG_ID.setEditable(false);textField__LOG_ID.setMinWidth(300);
                                                                                TextField textField__JOB_NAME = new TextField(resultSetLogDetails.getString("JOB_NAME"));textField__JOB_NAME.setEditable(false);textField__JOB_NAME.setMinWidth(300);
                                                                                TextField textField__JOB_SUBNAME = new TextField(resultSetLogDetails.getString("JOB_SUBNAME"));textField__JOB_SUBNAME.setEditable(false);textField__JOB_SUBNAME.setMinWidth(300);
                                                                                TextField textField__JOB_STYLE = new TextField(resultSetLogDetails.getString("JOB_STYLE"));textField__JOB_STYLE.setEditable(false);textField__JOB_STYLE.setMinWidth(300);
                                                                                TextField textField__JOB_CREATOR = new TextField(resultSetLogDetails.getString("JOB_CREATOR"));textField__JOB_CREATOR.setEditable(false);textField__JOB_CREATOR.setMinWidth(300);
                                                                                TextField textField__CLIENT_ID = new TextField(resultSetLogDetails.getString("CLIENT_ID"));textField__CLIENT_ID.setEditable(false);textField__CLIENT_ID.setMinWidth(300);
                                                                                TextField textField__GLOBAL_UID = new TextField(resultSetLogDetails.getString("GLOBAL_UID"));textField__GLOBAL_UID.setEditable(false);textField__GLOBAL_UID.setMinWidth(300);
                                                                                TextField textField__PROGRAM_OWNER = new TextField(resultSetLogDetails.getString("PROGRAM_OWNER"));textField__PROGRAM_OWNER.setEditable(false);textField__PROGRAM_OWNER.setMinWidth(300);
                                                                                TextField textField__PROGRAM_NAME = new TextField(resultSetLogDetails.getString("PROGRAM_NAME"));textField__PROGRAM_NAME.setEditable(false);textField__PROGRAM_NAME.setMinWidth(300);
                                                                                TextField textField__JOB_TYPE = new TextField(resultSetLogDetails.getString("JOB_TYPE"));textField__JOB_TYPE.setEditable(false);textField__JOB_TYPE.setMinWidth(300);
                                                                                TextField textField__JOB_ACTION = new TextField(resultSetLogDetails.getString("JOB_ACTION"));textField__JOB_ACTION.setEditable(false);textField__JOB_ACTION.setMinWidth(300);
                                                                                TextField textField__NUMBER_OF_ARGUMENTS = new TextField(resultSetLogDetails.getString("NUMBER_OF_ARGUMENTS"));textField__NUMBER_OF_ARGUMENTS.setEditable(false);textField__NUMBER_OF_ARGUMENTS.setMinWidth(300);
                                                                                TextField textField__SCHEDULE_OWNER = new TextField(resultSetLogDetails.getString("SCHEDULE_OWNER"));textField__SCHEDULE_OWNER.setEditable(false);textField__SCHEDULE_OWNER.setMinWidth(300);
                                                                                TextField textField__SCHEDULE_NAME = new TextField(resultSetLogDetails.getString("SCHEDULE_NAME"));textField__SCHEDULE_NAME.setEditable(false);textField__SCHEDULE_NAME.setMinWidth(300);
                                                                                TextField textField__SCHEDULE_TYPE = new TextField(resultSetLogDetails.getString("SCHEDULE_TYPE"));textField__SCHEDULE_TYPE.setEditable(false);textField__SCHEDULE_TYPE.setMinWidth(300);
                                                                                TextField textField__START_DATE = new TextField(resultSetLogDetails.getString("START_DATE"));textField__START_DATE.setEditable(false);textField__START_DATE.setMinWidth(300);
                                                                                TextField textField__REPEAT_INTERVAL = new TextField(resultSetLogDetails.getString("REPEAT_INTERVAL"));textField__REPEAT_INTERVAL.setEditable(false);textField__REPEAT_INTERVAL.setMinWidth(300);
                                                                                TextField textField__EVENT_QUEUE_OWNER = new TextField(resultSetLogDetails.getString("EVENT_QUEUE_OWNER"));textField__EVENT_QUEUE_OWNER.setEditable(false);textField__EVENT_QUEUE_OWNER.setMinWidth(300);
                                                                                TextField textField__EVENT_QUEUE_NAME = new TextField(resultSetLogDetails.getString("EVENT_QUEUE_NAME"));textField__EVENT_QUEUE_NAME.setEditable(false);textField__EVENT_QUEUE_NAME.setMinWidth(300);
                                                                                TextField textField__EVENT_QUEUE_AGENT = new TextField(resultSetLogDetails.getString("EVENT_QUEUE_AGENT"));textField__EVENT_QUEUE_AGENT.setEditable(false);textField__EVENT_QUEUE_AGENT.setMinWidth(300);
                                                                                TextField textField__EVENT_CONDITION = new TextField(resultSetLogDetails.getString("EVENT_CONDITION"));textField__EVENT_CONDITION.setEditable(false);textField__EVENT_CONDITION.setMinWidth(300);
                                                                                TextField textField__EVENT_RULE = new TextField(resultSetLogDetails.getString("EVENT_RULE"));textField__EVENT_RULE.setEditable(false);textField__EVENT_RULE.setMinWidth(300);
                                                                                TextField textField__FILE_WATCHER_OWNER = new TextField(resultSetLogDetails.getString("FILE_WATCHER_OWNER"));textField__FILE_WATCHER_OWNER.setEditable(false);textField__FILE_WATCHER_OWNER.setMinWidth(300);
                                                                                TextField textField__FILE_WATCHER_NAME = new TextField(resultSetLogDetails.getString("FILE_WATCHER_NAME"));textField__FILE_WATCHER_NAME.setEditable(false);textField__FILE_WATCHER_NAME.setMinWidth(300);
                                                                                TextField textField__END_DATE = new TextField(resultSetLogDetails.getString("END_DATE"));textField__END_DATE.setEditable(false);textField__END_DATE.setMinWidth(300);
                                                                                TextField textField__JOB_CLASS = new TextField(resultSetLogDetails.getString("JOB_CLASS"));textField__JOB_CLASS.setEditable(false);textField__JOB_CLASS.setMinWidth(300);
                                                                                TextField textField__ENABLED = new TextField(resultSetLogDetails.getString("ENABLED"));textField__ENABLED.setEditable(false);textField__ENABLED.setMinWidth(300);
                                                                                TextField textField__AUTO_DROP = new TextField(resultSetLogDetails.getString("AUTO_DROP"));textField__AUTO_DROP.setEditable(false);textField__AUTO_DROP.setMinWidth(300);
                                                                                TextField textField__RESTART_ON_RECOVERY = new TextField(resultSetLogDetails.getString("RESTART_ON_RECOVERY"));textField__RESTART_ON_RECOVERY.setEditable(false);textField__RESTART_ON_RECOVERY.setMinWidth(300);
                                                                                TextField textField__RESTART_ON_FAILURE = new TextField(resultSetLogDetails.getString("RESTART_ON_FAILURE"));textField__RESTART_ON_FAILURE.setEditable(false);textField__RESTART_ON_FAILURE.setMinWidth(300);
                                                                                TextField textField__STATE = new TextField(resultSetLogDetails.getString("STATE"));textField__STATE.setEditable(false);textField__STATE.setMinWidth(300);
                                                                                TextField textField__JOB_PRIORITY = new TextField(resultSetLogDetails.getString("JOB_PRIORITY"));textField__JOB_PRIORITY.setEditable(false);textField__JOB_PRIORITY.setMinWidth(300);
                                                                                TextField textField__RUN_COUNT = new TextField(resultSetLogDetails.getString("RUN_COUNT"));textField__RUN_COUNT.setEditable(false);textField__RUN_COUNT.setMinWidth(300);
                                                                                TextField textField__UPTIME_RUN_COUNT = new TextField(resultSetLogDetails.getString("UPTIME_RUN_COUNT"));textField__UPTIME_RUN_COUNT.setEditable(false);textField__UPTIME_RUN_COUNT.setMinWidth(300);
                                                                                TextField textField__MAX_RUNS = new TextField(resultSetLogDetails.getString("MAX_RUNS"));textField__MAX_RUNS.setEditable(false);textField__MAX_RUNS.setMinWidth(300);
                                                                                TextField textField__FAILURE_COUNT = new TextField(resultSetLogDetails.getString("FAILURE_COUNT"));textField__FAILURE_COUNT.setEditable(false);textField__FAILURE_COUNT.setMinWidth(300);
                                                                                TextField textField__UPTIME_FAILURE_COUNT = new TextField(resultSetLogDetails.getString("UPTIME_FAILURE_COUNT"));textField__UPTIME_FAILURE_COUNT.setEditable(false);textField__UPTIME_FAILURE_COUNT.setMinWidth(300);
                                                                                TextField textField__MAX_FAILURES = new TextField(resultSetLogDetails.getString("MAX_FAILURES"));textField__MAX_FAILURES.setEditable(false);textField__MAX_FAILURES.setMinWidth(300);
                                                                                TextField textField__RETRY_COUNT = new TextField(resultSetLogDetails.getString("RETRY_COUNT"));textField__RETRY_COUNT.setEditable(false);textField__RETRY_COUNT.setMinWidth(300);
                                                                                TextField textField__LAST_START_DATE = new TextField(resultSetLogDetails.getString("LAST_START_DATE"));textField__LAST_START_DATE.setEditable(false);textField__LAST_START_DATE.setMinWidth(300);
                                                                                TextField textField__LAST_RUN_DURATION = new TextField(resultSetLogDetails.getString("LAST_RUN_DURATION"));textField__LAST_RUN_DURATION.setEditable(false);textField__LAST_RUN_DURATION.setMinWidth(300);
                                                                                TextField textField__NEXT_RUN_DATE = new TextField(resultSetLogDetails.getString("NEXT_RUN_DATE"));textField__NEXT_RUN_DATE.setEditable(false);textField__NEXT_RUN_DATE.setMinWidth(300);
                                                                                TextField textField__SCHEDULE_LIMIT = new TextField(resultSetLogDetails.getString("SCHEDULE_LIMIT"));textField__SCHEDULE_LIMIT.setEditable(false);textField__SCHEDULE_LIMIT.setMinWidth(300);
                                                                                TextField textField__MAX_RUN_DURATION = new TextField(resultSetLogDetails.getString("MAX_RUN_DURATION"));textField__MAX_RUN_DURATION.setEditable(false);textField__MAX_RUN_DURATION.setMinWidth(300);
                                                                                TextField textField__LOGGING_LEVEL = new TextField(resultSetLogDetails.getString("LOGGING_LEVEL"));textField__LOGGING_LEVEL.setEditable(false);textField__LOGGING_LEVEL.setMinWidth(300);
                                                                                TextField textField__STORE_OUTPUT = new TextField(resultSetLogDetails.getString("STORE_OUTPUT"));textField__STORE_OUTPUT.setEditable(false);textField__STORE_OUTPUT.setMinWidth(300);
                                                                                TextField textField__STOP_ON_WINDOW_CLOSE = new TextField(resultSetLogDetails.getString("STOP_ON_WINDOW_CLOSE"));textField__STOP_ON_WINDOW_CLOSE.setEditable(false);textField__STOP_ON_WINDOW_CLOSE.setMinWidth(300);
                                                                                TextField textField__INSTANCE_STICKINESS = new TextField(resultSetLogDetails.getString("INSTANCE_STICKINESS"));textField__INSTANCE_STICKINESS.setEditable(false);textField__INSTANCE_STICKINESS.setMinWidth(300);
                                                                                TextField textField__RAISE_EVENTS = new TextField(resultSetLogDetails.getString("RAISE_EVENTS"));textField__RAISE_EVENTS.setEditable(false);textField__RAISE_EVENTS.setMinWidth(300);
                                                                                TextField textField__SYSTEM = new TextField(resultSetLogDetails.getString("SYSTEM"));textField__SYSTEM.setEditable(false);textField__SYSTEM.setMinWidth(300);
                                                                                TextField textField__JOB_WEIGHT = new TextField(resultSetLogDetails.getString("JOB_WEIGHT"));textField__JOB_WEIGHT.setEditable(false);textField__JOB_WEIGHT.setMinWidth(300);
                                                                                TextField textField__NLS_ENV = new TextField(resultSetLogDetails.getString("NLS_ENV"));textField__NLS_ENV.setEditable(false);textField__NLS_ENV.setMinWidth(300);
                                                                                TextField textField__SOURCE = new TextField(resultSetLogDetails.getString("SOURCE"));textField__SOURCE.setEditable(false);textField__SOURCE.setMinWidth(300);
                                                                                TextField textField__NUMBER_OF_DESTINATIONS = new TextField(resultSetLogDetails.getString("NUMBER_OF_DESTINATIONS"));textField__NUMBER_OF_DESTINATIONS.setEditable(false);textField__NUMBER_OF_DESTINATIONS.setMinWidth(300);
                                                                                TextField textField__DESTINATION_OWNER = new TextField(resultSetLogDetails.getString("DESTINATION_OWNER"));textField__DESTINATION_OWNER.setEditable(false);textField__DESTINATION_OWNER.setMinWidth(300);
                                                                                TextField textField__DESTINATION = new TextField(resultSetLogDetails.getString("DESTINATION"));textField__DESTINATION.setEditable(false);textField__DESTINATION.setMinWidth(300);
                                                                                TextField textField__CREDENTIAL_OWNER = new TextField(resultSetLogDetails.getString("CREDENTIAL_OWNER"));textField__CREDENTIAL_OWNER.setEditable(false);textField__CREDENTIAL_OWNER.setMinWidth(300);
                                                                                TextField textField__CREDENTIAL_NAME = new TextField(resultSetLogDetails.getString("CREDENTIAL_NAME"));textField__CREDENTIAL_NAME.setEditable(false);textField__CREDENTIAL_NAME.setMinWidth(300);
                                                                                TextField textField__INSTANCE_ID = new TextField(resultSetLogDetails.getString("INSTANCE_ID"));textField__INSTANCE_ID.setEditable(false);textField__INSTANCE_ID.setMinWidth(300);
                                                                                TextField textField__DEFERRED_DROP = new TextField(resultSetLogDetails.getString("DEFERRED_DROP"));textField__DEFERRED_DROP.setEditable(false);textField__DEFERRED_DROP.setMinWidth(300);
                                                                                TextField textField__ALLOW_RUNS_IN_RESTRICTED_MODE = new TextField(resultSetLogDetails.getString("ALLOW_RUNS_IN_RESTRICTED_MODE"));textField__ALLOW_RUNS_IN_RESTRICTED_MODE.setEditable(false);textField__ALLOW_RUNS_IN_RESTRICTED_MODE.setMinWidth(300);
                                                                                TextField textField__COMMENTS = new TextField(resultSetLogDetails.getString("COMMENTS"));textField__COMMENTS.setEditable(false);textField__COMMENTS.setMinWidth(300);
                                                                                TextField textField__FLAGS = new TextField(resultSetLogDetails.getString("FLAGS"));textField__FLAGS.setEditable(false);textField__FLAGS.setMinWidth(300);
                                                                                TextField textField__RESTARTABLE = new TextField(resultSetLogDetails.getString("RESTARTABLE"));textField__RESTARTABLE.setEditable(false);textField__RESTARTABLE.setMinWidth(300);
                                                                                TextField textField__HAS_CONSTRAINTS = new TextField(resultSetLogDetails.getString("HAS_CONSTRAINTS"));textField__HAS_CONSTRAINTS.setEditable(false);textField__HAS_CONSTRAINTS.setMinWidth(300);
                                                                                TextField textField__CONNECT_CREDENTIAL_OWNER = new TextField(resultSetLogDetails.getString("CONNECT_CREDENTIAL_OWNER"));textField__CONNECT_CREDENTIAL_OWNER.setEditable(false);textField__CONNECT_CREDENTIAL_OWNER.setMinWidth(300);
                                                                                TextField textField__CONNECT_CREDENTIAL_NAME = new TextField(resultSetLogDetails.getString("CONNECT_CREDENTIAL_NAME"));textField__CONNECT_CREDENTIAL_NAME.setEditable(false);textField__CONNECT_CREDENTIAL_NAME.setMinWidth(300);
                                                                                TextField textField__FAIL_ON_SCRIPT_ERROR = new TextField(resultSetLogDetails.getString("FAIL_ON_SCRIPT_ERROR"));textField__FAIL_ON_SCRIPT_ERROR.setEditable(false);textField__FAIL_ON_SCRIPT_ERROR.setMinWidth(300);


                                                                                gridPaneInScrollPaneJobDetails.add(label__OWNER,0,0);gridPaneInScrollPaneJobDetails.add(textField__LOG_ID,1,0);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_NAME,0,1);gridPaneInScrollPaneJobDetails.add(textField__JOB_NAME,1,1);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_SUBNAME,0,2);gridPaneInScrollPaneJobDetails.add(textField__JOB_SUBNAME,1,2);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_STYLE,0,3);gridPaneInScrollPaneJobDetails.add(textField__JOB_STYLE,1,3);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_CREATOR,0,4);gridPaneInScrollPaneJobDetails.add(textField__JOB_CREATOR,1,4);
                                                                                gridPaneInScrollPaneJobDetails.add(label__CLIENT_ID,0,5);gridPaneInScrollPaneJobDetails.add(textField__CLIENT_ID,1,5);
                                                                                gridPaneInScrollPaneJobDetails.add(label__GLOBAL_UID,0,6);gridPaneInScrollPaneJobDetails.add(textField__GLOBAL_UID,1,6);
                                                                                gridPaneInScrollPaneJobDetails.add(label__PROGRAM_OWNER,0,7);gridPaneInScrollPaneJobDetails.add(textField__PROGRAM_OWNER,1,7);
                                                                                gridPaneInScrollPaneJobDetails.add(label__PROGRAM_NAME,0,8);gridPaneInScrollPaneJobDetails.add(textField__PROGRAM_NAME,1,8);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_TYPE,0,9);gridPaneInScrollPaneJobDetails.add(textField__JOB_TYPE,1,9);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_ACTION,0,10);gridPaneInScrollPaneJobDetails.add(textField__JOB_ACTION,1,10);
                                                                                gridPaneInScrollPaneJobDetails.add(label__NUMBER_OF_ARGUMENTS,0,11);gridPaneInScrollPaneJobDetails.add(textField__NUMBER_OF_ARGUMENTS,1,11);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SCHEDULE_OWNER,0,12);gridPaneInScrollPaneJobDetails.add(textField__SCHEDULE_OWNER,1,12);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SCHEDULE_NAME,0,13);gridPaneInScrollPaneJobDetails.add(textField__SCHEDULE_NAME,1,13);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SCHEDULE_TYPE,0,14);gridPaneInScrollPaneJobDetails.add(textField__SCHEDULE_TYPE,1,14);
                                                                                gridPaneInScrollPaneJobDetails.add(label__START_DATE,0,15);gridPaneInScrollPaneJobDetails.add(textField__START_DATE,1,15);
                                                                                gridPaneInScrollPaneJobDetails.add(label__REPEAT_INTERVAL,0,16);gridPaneInScrollPaneJobDetails.add(textField__REPEAT_INTERVAL,1,16);
                                                                                gridPaneInScrollPaneJobDetails.add(label__EVENT_QUEUE_OWNER,0,17);gridPaneInScrollPaneJobDetails.add(textField__EVENT_QUEUE_OWNER,1,17);
                                                                                gridPaneInScrollPaneJobDetails.add(label__EVENT_QUEUE_NAME,0,18);gridPaneInScrollPaneJobDetails.add(textField__EVENT_QUEUE_NAME,1,18);
                                                                                gridPaneInScrollPaneJobDetails.add(label__EVENT_QUEUE_AGENT,0,19);gridPaneInScrollPaneJobDetails.add(textField__EVENT_QUEUE_AGENT,1,19);
                                                                                gridPaneInScrollPaneJobDetails.add(label__EVENT_CONDITION,0,20);gridPaneInScrollPaneJobDetails.add(textField__EVENT_CONDITION,1,20);
                                                                                gridPaneInScrollPaneJobDetails.add(label__EVENT_RULE,0,21);gridPaneInScrollPaneJobDetails.add(textField__EVENT_RULE,1,21);
                                                                                gridPaneInScrollPaneJobDetails.add(label__FILE_WATCHER_OWNER,0,22);gridPaneInScrollPaneJobDetails.add(textField__FILE_WATCHER_OWNER,1,22);
                                                                                gridPaneInScrollPaneJobDetails.add(label__FILE_WATCHER_NAME,0,23);gridPaneInScrollPaneJobDetails.add(textField__FILE_WATCHER_NAME,1,23);
                                                                                gridPaneInScrollPaneJobDetails.add(label__END_DATE,0,24);gridPaneInScrollPaneJobDetails.add(textField__END_DATE,1,24);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_CLASS,0,25);gridPaneInScrollPaneJobDetails.add(textField__JOB_CLASS,1,25);
                                                                                gridPaneInScrollPaneJobDetails.add(label__ENABLED,0,26);gridPaneInScrollPaneJobDetails.add(textField__ENABLED,1,26);
                                                                                gridPaneInScrollPaneJobDetails.add(label__AUTO_DROP,0,27);gridPaneInScrollPaneJobDetails.add(textField__AUTO_DROP,1,27);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RESTART_ON_RECOVERY,0,28);gridPaneInScrollPaneJobDetails.add(textField__RESTART_ON_RECOVERY,1,28);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RESTART_ON_FAILURE,0,29);gridPaneInScrollPaneJobDetails.add(textField__RESTART_ON_FAILURE,1,29);
                                                                                gridPaneInScrollPaneJobDetails.add(label__STATE,0,30);gridPaneInScrollPaneJobDetails.add(textField__STATE,1,30);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_PRIORITY,0,31);gridPaneInScrollPaneJobDetails.add(textField__JOB_PRIORITY,1,31);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RUN_COUNT,0,32);gridPaneInScrollPaneJobDetails.add(textField__RUN_COUNT,1,32);
                                                                                gridPaneInScrollPaneJobDetails.add(label__UPTIME_RUN_COUNT,0,33);gridPaneInScrollPaneJobDetails.add(textField__UPTIME_RUN_COUNT,1,33);
                                                                                gridPaneInScrollPaneJobDetails.add(label__MAX_RUNS,0,34);gridPaneInScrollPaneJobDetails.add(textField__MAX_RUNS,1,34);
                                                                                gridPaneInScrollPaneJobDetails.add(label__FAILURE_COUNT,0,35);gridPaneInScrollPaneJobDetails.add(textField__FAILURE_COUNT,1,35);
                                                                                gridPaneInScrollPaneJobDetails.add(label__UPTIME_FAILURE_COUNT,0,36);gridPaneInScrollPaneJobDetails.add(textField__UPTIME_FAILURE_COUNT,1,36);
                                                                                gridPaneInScrollPaneJobDetails.add(label__MAX_FAILURES,0,37);gridPaneInScrollPaneJobDetails.add(textField__MAX_FAILURES,1,37);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RETRY_COUNT,0,38);gridPaneInScrollPaneJobDetails.add(textField__RETRY_COUNT,1,38);
                                                                                gridPaneInScrollPaneJobDetails.add(label__LAST_START_DATE,0,39);gridPaneInScrollPaneJobDetails.add(textField__LAST_START_DATE,1,39);
                                                                                gridPaneInScrollPaneJobDetails.add(label__LAST_RUN_DURATION,0,40);gridPaneInScrollPaneJobDetails.add(textField__LAST_RUN_DURATION,1,40);
                                                                                gridPaneInScrollPaneJobDetails.add(label__NEXT_RUN_DATE,0,41);gridPaneInScrollPaneJobDetails.add(textField__NEXT_RUN_DATE,1,41);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SCHEDULE_LIMIT,0,42);gridPaneInScrollPaneJobDetails.add(textField__SCHEDULE_LIMIT,1,42);
                                                                                gridPaneInScrollPaneJobDetails.add(label__MAX_RUN_DURATION,0,43);gridPaneInScrollPaneJobDetails.add(textField__MAX_RUN_DURATION,1,43);
                                                                                gridPaneInScrollPaneJobDetails.add(label__LOGGING_LEVEL,0,44);gridPaneInScrollPaneJobDetails.add(textField__LOGGING_LEVEL,1,44);
                                                                                gridPaneInScrollPaneJobDetails.add(label__STORE_OUTPUT,0,45);gridPaneInScrollPaneJobDetails.add(textField__STORE_OUTPUT,1,45);
                                                                                gridPaneInScrollPaneJobDetails.add(label__STOP_ON_WINDOW_CLOSE,0,46);gridPaneInScrollPaneJobDetails.add(textField__STOP_ON_WINDOW_CLOSE,1,46);
                                                                                gridPaneInScrollPaneJobDetails.add(label__INSTANCE_STICKINESS,0,47);gridPaneInScrollPaneJobDetails.add(textField__INSTANCE_STICKINESS,1,47);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RAISE_EVENTS,0,48);gridPaneInScrollPaneJobDetails.add(textField__RAISE_EVENTS,1,48);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SYSTEM,0,49);gridPaneInScrollPaneJobDetails.add(textField__SYSTEM,1,49);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_WEIGHT,0,50);gridPaneInScrollPaneJobDetails.add(textField__JOB_WEIGHT,1,50);
                                                                                gridPaneInScrollPaneJobDetails.add(label__NLS_ENV,0,51);gridPaneInScrollPaneJobDetails.add(textField__NLS_ENV,1,51);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SOURCE,0,52);gridPaneInScrollPaneJobDetails.add(textField__SOURCE,1,52);
                                                                                gridPaneInScrollPaneJobDetails.add(label__NUMBER_OF_DESTINATIONS,0,53);gridPaneInScrollPaneJobDetails.add(textField__NUMBER_OF_DESTINATIONS,1,53);
                                                                                gridPaneInScrollPaneJobDetails.add(label__DESTINATION_OWNER,0,54);gridPaneInScrollPaneJobDetails.add(textField__DESTINATION_OWNER,1,54);
                                                                                gridPaneInScrollPaneJobDetails.add(label__DESTINATION,0,55);gridPaneInScrollPaneJobDetails.add(textField__DESTINATION,1,55);
                                                                                gridPaneInScrollPaneJobDetails.add(label__CREDENTIAL_OWNER,0,56);gridPaneInScrollPaneJobDetails.add(textField__CREDENTIAL_OWNER,1,56);
                                                                                gridPaneInScrollPaneJobDetails.add(label__CREDENTIAL_NAME,0,57);gridPaneInScrollPaneJobDetails.add(textField__CREDENTIAL_NAME,1,57);
                                                                                gridPaneInScrollPaneJobDetails.add(label__INSTANCE_ID,0,58);gridPaneInScrollPaneJobDetails.add(textField__INSTANCE_ID,1,58);
                                                                                gridPaneInScrollPaneJobDetails.add(label__DEFERRED_DROP,0,59);gridPaneInScrollPaneJobDetails.add(textField__DEFERRED_DROP,1,59);
                                                                                gridPaneInScrollPaneJobDetails.add(label__ALLOW_RUNS_IN_RESTRICTED_MODE,0,60);gridPaneInScrollPaneJobDetails.add(textField__ALLOW_RUNS_IN_RESTRICTED_MODE,1,60);
                                                                                gridPaneInScrollPaneJobDetails.add(label__COMMENTS,0,61);gridPaneInScrollPaneJobDetails.add(textField__COMMENTS,1,61);
                                                                                gridPaneInScrollPaneJobDetails.add(label__FLAGS,0,62);gridPaneInScrollPaneJobDetails.add(textField__FLAGS,1,62);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RESTARTABLE,0,63);gridPaneInScrollPaneJobDetails.add(textField__RESTARTABLE,1,63);
                                                                                gridPaneInScrollPaneJobDetails.add(label__HAS_CONSTRAINTS,0,64);gridPaneInScrollPaneJobDetails.add(textField__HAS_CONSTRAINTS,1,64);
                                                                                gridPaneInScrollPaneJobDetails.add(label__CONNECT_CREDENTIAL_OWNER,0,65);gridPaneInScrollPaneJobDetails.add(textField__CONNECT_CREDENTIAL_OWNER,1,65);
                                                                                gridPaneInScrollPaneJobDetails.add(label__CONNECT_CREDENTIAL_NAME,0,66);gridPaneInScrollPaneJobDetails.add(textField__CONNECT_CREDENTIAL_NAME,1,66);
                                                                                gridPaneInScrollPaneJobDetails.add(label__FAIL_ON_SCRIPT_ERROR,0,67);gridPaneInScrollPaneJobDetails.add(textField__FAIL_ON_SCRIPT_ERROR,1,67);


                                                                            }
                                                                        }catch(Exception e){System.out.println(e);}
                                                                        dialogJobDetails.getDialogPane().setContent(dialogJobDetailsGridPane);
                                                                        dialogJobDetails.show();
                                                                    }
                                                                });


                                                                TextField REQ_START_DATE__TEXT = new TextField(resultSetSelectLastRunningJobsByOwnerName.getString("STATUS"));REQ_START_DATE__TEXT.setEditable(false);gridPaneInScrollPaneLastRunningJobs.add(REQ_START_DATE__TEXT,3,iLastRunningJobsIndex);REQ_START_DATE__TEXT.setMinWidth(80);REQ_START_DATE__TEXT.setPrefWidth(80);REQ_START_DATE__TEXT.setMaxWidth(80);REQ_START_DATE__TEXT.setStyle("-fx-font-size:10");
                                                                TextField ACTUAL_START_DATE__TEXT = new TextField(resultSetSelectLastRunningJobsByOwnerName.getString("ACTUAL_START_DATE"));ACTUAL_START_DATE__TEXT.setEditable(false);gridPaneInScrollPaneLastRunningJobs.add(ACTUAL_START_DATE__TEXT,4,iLastRunningJobsIndex);ACTUAL_START_DATE__TEXT.setMinWidth(170);ACTUAL_START_DATE__TEXT.setPrefWidth(170);ACTUAL_START_DATE__TEXT.setMaxWidth(170);ACTUAL_START_DATE__TEXT.setStyle("-fx-font-size:10");
                                                                iLastRunningJobsIndex++;
                                                            }
                                                        }catch(Exception e){//blad
                                                            System.out.println(e);
                                                        }
                                                        paneLastRunningJobs.getChildren().add(gridPaneLastRunningJobs);

                                                        //
                                                        //GridPane gridPaneAllSchedulerJobs = new GridPane();
                                                        //dialogSchedulerGridPane.add(gridPaneAllSchedulerJobs,1,3);


                                                        Pane paneAllSchedulerJobs = new Pane();
                                                        paneAllSchedulerJobs.setMinWidth(1025);paneAllSchedulerJobs.setPrefWidth(1025);
                                                        paneAllSchedulerJobs.setMinHeight(700);paneAllSchedulerJobs.setPrefHeight(700);
                                                        GridPane gridPaneAllSchedulerJobs = new GridPane();
                                                        Label labelAllSchedulerJobs = new Label("All scheduler jobs:");labelAllSchedulerJobs.setStyle("-fx-font-size:15");
                                                        gridPaneAllSchedulerJobs.add(labelAllSchedulerJobs,0,0,6,1);
                                                        Label labelAllSchedulerJobs__OWNER = new Label("OWNER");labelAllSchedulerJobs__OWNER.setPrefWidth(100);labelAllSchedulerJobs__OWNER.setStyle("-fx-border-style:solid;-fx-border-color:black;-fx-border-width:1;-fx-alignment:center");
                                                        Label labelAllSchedulerJobs__JOB_NAME = new Label("JOB_NAME");labelAllSchedulerJobs__JOB_NAME.setPrefWidth(140);labelAllSchedulerJobs__JOB_NAME.setStyle("-fx-border-style:solid;-fx-border-color:black;-fx-border-width:1;-fx-alignment:center");
                                                        Label labelAllSchedulerJobs__ENABLED = new Label("ENABLED");labelAllSchedulerJobs__ENABLED.setPrefWidth(80);labelAllSchedulerJobs__ENABLED.setStyle("-fx-border-style:solid;-fx-border-color:black;-fx-border-width:1;-fx-alignment:center");
                                                        Label labelAllSchedulerJobs__STATE = new Label("STATE");labelAllSchedulerJobs__STATE.setPrefWidth(80);labelAllSchedulerJobs__STATE.setStyle("-fx-border-style:solid;-fx-border-color:black;-fx-border-width:1;-fx-alignment:center");
                                                        Label labelAllSchedulerJobs__JOB_TYPE = new Label("JOB_TYPE");labelAllSchedulerJobs__JOB_TYPE.setPrefWidth(110);labelAllSchedulerJobs__JOB_TYPE.setStyle("-fx-border-style:solid;-fx-border-color:black;-fx-border-width:1;-fx-alignment:center");
                                                        Label labelAllSchedulerJobs__REPEAT_INTERVAL = new Label("REPEAT_INTERVAL");labelAllSchedulerJobs__REPEAT_INTERVAL.setPrefWidth(160);labelAllSchedulerJobs__REPEAT_INTERVAL.setStyle("-fx-border-style:solid;-fx-border-color:black;-fx-border-width:1;-fx-alignment:center");
                                                        Label labelAllSchedulerJobs__LAST_START_DATE = new Label("LAST_START_DATE");labelAllSchedulerJobs__LAST_START_DATE.setPrefWidth(170);labelAllSchedulerJobs__LAST_START_DATE.setStyle("-fx-border-style:solid;-fx-border-color:black;-fx-border-width:1;-fx-alignment:center");
                                                        Label labelAllSchedulerJobs__NEXT_RUN_DATE = new Label("NEXT_RUN_DATE");labelAllSchedulerJobs__NEXT_RUN_DATE.setPrefWidth(170);labelAllSchedulerJobs__NEXT_RUN_DATE.setStyle("-fx-border-style:solid;-fx-border-color:black;-fx-border-width:1;-fx-alignment:center");
                                                        gridPaneAllSchedulerJobs.add(labelAllSchedulerJobs__OWNER,0,1);
                                                        gridPaneAllSchedulerJobs.add(labelAllSchedulerJobs__JOB_NAME,1,1);
                                                        gridPaneAllSchedulerJobs.add(labelAllSchedulerJobs__ENABLED,2,1);
                                                        gridPaneAllSchedulerJobs.add(labelAllSchedulerJobs__STATE,3,1);
                                                        gridPaneAllSchedulerJobs.add(labelAllSchedulerJobs__JOB_TYPE,4,1);
                                                        gridPaneAllSchedulerJobs.add(labelAllSchedulerJobs__REPEAT_INTERVAL,5,1);
                                                        gridPaneAllSchedulerJobs.add(labelAllSchedulerJobs__LAST_START_DATE,6,1);
                                                        gridPaneAllSchedulerJobs.add(labelAllSchedulerJobs__NEXT_RUN_DATE,7,1);
                                                        ScrollPane scrollPaneAllSchedulerJobs = new ScrollPane();
                                                        gridPaneAllSchedulerJobs.add(scrollPaneAllSchedulerJobs,0,2,8,1);
                                                        scrollPaneAllSchedulerJobs.setMinWidth(1025);scrollPaneAllSchedulerJobs.setPrefWidth(1025);scrollPaneAllSchedulerJobs.setMinHeight(715);scrollPaneAllSchedulerJobs.setPrefHeight(715);
                                                        GridPane gridPaneInScrollPaneAllSchedulerJobs = new GridPane();
                                                        scrollPaneAllSchedulerJobs.setContent(gridPaneInScrollPaneAllSchedulerJobs);
                                                        //String querySelectAllSchedulerJobsByOwnerName = "select LOG_ID, OWNER, JOB_NAME, STATUS, JOB_TYPE from ALL_SCHEDULER_JOBS where OWNER='"+comboBoxSchedulerOwnerList.getValue().toString()+"' ORDER BY JOB_NAME DESC";
                                                        String querySelectAllSchedulerJobsByOwnerName = "select * from ALL_SCHEDULER_JOBS where OWNER='"+comboBoxSchedulerOwnerList.getValue().toString()+"' ORDER BY JOB_NAME DESC";
                                                        try{
                                                            ResultSet resultSetSelectAllSchedulerJobsByOwnerName = statement.executeQuery(querySelectAllSchedulerJobsByOwnerName);
                                                            Integer iAllSchedulerJobsIndex=0;
                                                            while(resultSetSelectAllSchedulerJobsByOwnerName.next()){
                                                                TextField OWNER__TEXT = new TextField(resultSetSelectAllSchedulerJobsByOwnerName.getString("OWNER"));OWNER__TEXT.setEditable(false);gridPaneInScrollPaneAllSchedulerJobs.add(OWNER__TEXT,0,iAllSchedulerJobsIndex);OWNER__TEXT.setMinWidth(100);OWNER__TEXT.setPrefWidth(100);OWNER__TEXT.setMaxWidth(100);OWNER__TEXT.setStyle("-fx-font-size:10");
                                                                TextField JOB_NAME__TEXT = new TextField(resultSetSelectAllSchedulerJobsByOwnerName.getString("JOB_NAME"));JOB_NAME__TEXT.setEditable(false);gridPaneInScrollPaneAllSchedulerJobs.add(JOB_NAME__TEXT,1,iAllSchedulerJobsIndex);JOB_NAME__TEXT.setMinWidth(140);JOB_NAME__TEXT.setPrefWidth(140);JOB_NAME__TEXT.setMaxWidth(140);JOB_NAME__TEXT.setStyle("-fx-font-size:10");

                                                                //JOB_NAME__TEXT.onMouseClickedProperty()
                                                                JOB_NAME__TEXT.setOnMouseEntered(new EventHandler<MouseEvent>(){
                                                                    public void handle(MouseEvent event){
                                                                        JOB_NAME__TEXT.setStyle("-fx-cursor:hand;-fx-font-size:10;-fx-text-fill:blue;");
                                                                    }
                                                                });
                                                                JOB_NAME__TEXT.setOnMouseExited(new EventHandler<MouseEvent>(){
                                                                    public void handle(MouseEvent event){
                                                                        JOB_NAME__TEXT.setStyle("-fx-font-size:10;-fx-text-fill:#383434;");
                                                                    }
                                                                });
                                                                //
                                                                JOB_NAME__TEXT.setOnMouseClicked(new EventHandler<MouseEvent>(){
                                                                    public void handle(MouseEvent event){
                                                                        Alert dialogJobDetails = new Alert(Alert.AlertType.INFORMATION);
                                                                        dialogJobDetails.setWidth(900);dialogJobDetails.setHeight(650);
                                                                        dialogJobDetails.setTitle("Job \""+JOB_NAME__TEXT.getText()+"\" details");
                                                                        dialogJobDetails.setHeaderText(null);
                                                                        GridPane dialogJobDetailsGridPane = new GridPane();
                                                                        dialogJobDetailsGridPane.setVgap(5);dialogJobDetailsGridPane.setHgap(5);
                                                                        //dialogJobDetailsGridPane.add(new Label(""),0,0);
                                                                        //,0,1
                                                                        Label labelJobDetail = new Label("Job \""+JOB_NAME__TEXT.getText()+"\" details");labelJobDetail.setStyle("-fx-font-weight:bold");dialogJobDetailsGridPane.add(labelJobDetail,0,0);
                                                                        String querySelectJobDetails = "select * from all_scheduler_jobs where job_name='"+JOB_NAME__TEXT.getText()+"' and owner='"+OWNER__TEXT.getText()+"'";
                                                                        ScrollPane scrollPaneJobDetails = new ScrollPane();
                                                                        GridPane gridPaneInScrollPaneJobDetails = new GridPane();
                                                                        gridPaneInScrollPaneJobDetails.setVgap(5);gridPaneInScrollPaneJobDetails.setHgap(15);
                                                                        scrollPaneJobDetails.setContent(gridPaneInScrollPaneJobDetails);
                                                                        scrollPaneJobDetails.setMinHeight(600);scrollPaneJobDetails.setMaxHeight(600);scrollPaneJobDetails.setPrefHeight(600);
                                                                        scrollPaneJobDetails.setMinWidth(550);scrollPaneJobDetails.setMaxWidth(550);scrollPaneJobDetails.setPrefWidth(550);
                                                                        dialogJobDetailsGridPane.add(scrollPaneJobDetails,0,2);
                                                                        Label label__OWNER = new Label("LOG_ID");
                                                                        Label label__JOB_NAME = new Label("JOB_NAME");
                                                                        Label label__JOB_SUBNAME = new Label("JOB_SUBNAME");
                                                                        Label label__JOB_STYLE = new Label("JOB_STYLE");
                                                                        Label label__JOB_CREATOR = new Label("JOB_CREATOR");
                                                                        Label label__CLIENT_ID = new Label("CLIENT_ID");
                                                                        Label label__GLOBAL_UID = new Label("GLOBAL_UID");
                                                                        Label label__PROGRAM_OWNER = new Label("PROGRAM_OWNER");
                                                                        Label label__PROGRAM_NAME = new Label("PROGRAM_NAME");
                                                                        Label label__JOB_TYPE = new Label("JOB_TYPE");
                                                                        Label label__JOB_ACTION = new Label("JOB_ACTION");
                                                                        Label label__NUMBER_OF_ARGUMENTS = new Label("NUMBER_OF_ARGUMENTS");
                                                                        Label label__SCHEDULE_OWNER = new Label("SCHEDULE_OWNER");
                                                                        Label label__SCHEDULE_NAME = new Label("SCHEDULE_NAME");
                                                                        Label label__SCHEDULE_TYPE = new Label("SCHEDULE_TYPE");
                                                                        Label label__START_DATE = new Label("START_DATE");
                                                                        Label label__REPEAT_INTERVAL = new Label("REPEAT_INTERVAL");
                                                                        Label label__EVENT_QUEUE_OWNER = new Label("EVENT_QUEUE_OWNER");
                                                                        Label label__EVENT_QUEUE_NAME = new Label("EVENT_QUEUE_NAME");
                                                                        Label label__EVENT_QUEUE_AGENT = new Label("EVENT_QUEUE_AGENT");
                                                                        Label label__EVENT_CONDITION = new Label("EVENT_CONDITION");
                                                                        Label label__EVENT_RULE = new Label("EVENT_RULE");
                                                                        Label label__FILE_WATCHER_OWNER = new Label("FILE_WATCHER_OWNER");
                                                                        Label label__FILE_WATCHER_NAME = new Label("FILE_WATCHER_NAME");
                                                                        Label label__END_DATE = new Label("END_DATE");
                                                                        Label label__JOB_CLASS = new Label("JOB_CLASS");
                                                                        Label label__ENABLED = new Label("ENABLED");
                                                                        Label label__AUTO_DROP = new Label("AUTO_DROP");
                                                                        Label label__RESTART_ON_RECOVERY = new Label("RESTART_ON_RECOVERY");
                                                                        Label label__RESTART_ON_FAILURE = new Label("RESTART_ON_FAILURE");
                                                                        Label label__STATE = new Label("STATE");
                                                                        Label label__JOB_PRIORITY = new Label("JOB_PRIORITY");
                                                                        Label label__RUN_COUNT = new Label("RUN_COUNT");
                                                                        Label label__UPTIME_RUN_COUNT = new Label("UPTIME_RUN_COUNT");
                                                                        Label label__MAX_RUNS = new Label("MAX_RUNS");
                                                                        Label label__FAILURE_COUNT = new Label("FAILURE_COUNT");
                                                                        Label label__UPTIME_FAILURE_COUNT = new Label("UPTIME_FAILURE_COUNT");
                                                                        Label label__MAX_FAILURES = new Label("MAX_FAILURES");
                                                                        Label label__RETRY_COUNT = new Label("RETRY_COUNT");
                                                                        Label label__LAST_START_DATE = new Label("LAST_START_DATE");
                                                                        Label label__LAST_RUN_DURATION = new Label("LAST_RUN_DURATION");
                                                                        Label label__NEXT_RUN_DATE = new Label("NEXT_RUN_DATE");
                                                                        Label label__SCHEDULE_LIMIT = new Label("SCHEDULE_LIMIT");
                                                                        Label label__MAX_RUN_DURATION = new Label("MAX_RUN_DURATION");
                                                                        Label label__LOGGING_LEVEL = new Label("LOGGING_LEVEL");
                                                                        Label label__STORE_OUTPUT = new Label("STORE_OUTPUT");
                                                                        Label label__STOP_ON_WINDOW_CLOSE = new Label("STOP_ON_WINDOW_CLOSE");
                                                                        Label label__INSTANCE_STICKINESS = new Label("INSTANCE_STICKINESS");
                                                                        Label label__RAISE_EVENTS = new Label("RAISE_EVENTS");
                                                                        Label label__SYSTEM = new Label("SYSTEM");
                                                                        Label label__JOB_WEIGHT = new Label("JOB_WEIGHT");
                                                                        Label label__NLS_ENV = new Label("NLS_ENV");
                                                                        Label label__SOURCE = new Label("SOURCE");
                                                                        Label label__NUMBER_OF_DESTINATIONS = new Label("NUMBER_OF_DESTINATIONS");
                                                                        Label label__DESTINATION_OWNER = new Label("DESTINATION_OWNER");
                                                                        Label label__DESTINATION = new Label("DESTINATION");
                                                                        Label label__CREDENTIAL_OWNER = new Label("CREDENTIAL_OWNER");
                                                                        Label label__CREDENTIAL_NAME= new Label("CREDENTIAL_NAME");
                                                                        Label label__INSTANCE_ID= new Label("INSTANCE_ID");
                                                                        Label label__DEFERRED_DROP= new Label("DEFERRED_DROP");
                                                                        Label label__ALLOW_RUNS_IN_RESTRICTED_MODE= new Label("ALLOW_RUNS_IN_RESTRICTED_MODE");
                                                                        Label label__COMMENTS= new Label("COMMENTS");
                                                                        Label label__FLAGS= new Label("FLAGS");
                                                                        Label label__RESTARTABLE= new Label("RESTARTABLE");
                                                                        Label label__HAS_CONSTRAINTS= new Label("HAS_CONSTRAINTS");
                                                                        Label label__CONNECT_CREDENTIAL_OWNER= new Label("CONNECT_CREDENTIAL_OWNER");
                                                                        Label label__CONNECT_CREDENTIAL_NAME= new Label("CONNECT_CREDENTIAL_NAME");
                                                                        Label label__FAIL_ON_SCRIPT_ERROR= new Label("FAIL_ON_SCRIPT_ERROR");


                                                                        try{
                                                                            ResultSet resultSetLogDetails = statement.executeQuery(querySelectJobDetails);
                                                                            while(resultSetLogDetails.next()){
                                                                                TextField textField__LOG_ID = new TextField(resultSetLogDetails.getString("OWNER"));textField__LOG_ID.setEditable(false);textField__LOG_ID.setMinWidth(300);
                                                                                TextField textField__JOB_NAME = new TextField(resultSetLogDetails.getString("JOB_NAME"));textField__JOB_NAME.setEditable(false);textField__JOB_NAME.setMinWidth(300);
                                                                                TextField textField__JOB_SUBNAME = new TextField(resultSetLogDetails.getString("JOB_SUBNAME"));textField__JOB_SUBNAME.setEditable(false);textField__JOB_SUBNAME.setMinWidth(300);
                                                                                TextField textField__JOB_STYLE = new TextField(resultSetLogDetails.getString("JOB_STYLE"));textField__JOB_STYLE.setEditable(false);textField__JOB_STYLE.setMinWidth(300);
                                                                                TextField textField__JOB_CREATOR = new TextField(resultSetLogDetails.getString("JOB_CREATOR"));textField__JOB_CREATOR.setEditable(false);textField__JOB_CREATOR.setMinWidth(300);
                                                                                TextField textField__CLIENT_ID = new TextField(resultSetLogDetails.getString("CLIENT_ID"));textField__CLIENT_ID.setEditable(false);textField__CLIENT_ID.setMinWidth(300);
                                                                                TextField textField__GLOBAL_UID = new TextField(resultSetLogDetails.getString("GLOBAL_UID"));textField__GLOBAL_UID.setEditable(false);textField__GLOBAL_UID.setMinWidth(300);
                                                                                TextField textField__PROGRAM_OWNER = new TextField(resultSetLogDetails.getString("PROGRAM_OWNER"));textField__PROGRAM_OWNER.setEditable(false);textField__PROGRAM_OWNER.setMinWidth(300);
                                                                                TextField textField__PROGRAM_NAME = new TextField(resultSetLogDetails.getString("PROGRAM_NAME"));textField__PROGRAM_NAME.setEditable(false);textField__PROGRAM_NAME.setMinWidth(300);
                                                                                TextField textField__JOB_TYPE = new TextField(resultSetLogDetails.getString("JOB_TYPE"));textField__JOB_TYPE.setEditable(false);textField__JOB_TYPE.setMinWidth(300);
                                                                                TextField textField__JOB_ACTION = new TextField(resultSetLogDetails.getString("JOB_ACTION"));textField__JOB_ACTION.setEditable(false);textField__JOB_ACTION.setMinWidth(300);
                                                                                TextField textField__NUMBER_OF_ARGUMENTS = new TextField(resultSetLogDetails.getString("NUMBER_OF_ARGUMENTS"));textField__NUMBER_OF_ARGUMENTS.setEditable(false);textField__NUMBER_OF_ARGUMENTS.setMinWidth(300);
                                                                                TextField textField__SCHEDULE_OWNER = new TextField(resultSetLogDetails.getString("SCHEDULE_OWNER"));textField__SCHEDULE_OWNER.setEditable(false);textField__SCHEDULE_OWNER.setMinWidth(300);
                                                                                TextField textField__SCHEDULE_NAME = new TextField(resultSetLogDetails.getString("SCHEDULE_NAME"));textField__SCHEDULE_NAME.setEditable(false);textField__SCHEDULE_NAME.setMinWidth(300);
                                                                                TextField textField__SCHEDULE_TYPE = new TextField(resultSetLogDetails.getString("SCHEDULE_TYPE"));textField__SCHEDULE_TYPE.setEditable(false);textField__SCHEDULE_TYPE.setMinWidth(300);
                                                                                TextField textField__START_DATE = new TextField(resultSetLogDetails.getString("START_DATE"));textField__START_DATE.setEditable(false);textField__START_DATE.setMinWidth(300);
                                                                                TextField textField__REPEAT_INTERVAL = new TextField(resultSetLogDetails.getString("REPEAT_INTERVAL"));textField__REPEAT_INTERVAL.setEditable(false);textField__REPEAT_INTERVAL.setMinWidth(300);
                                                                                TextField textField__EVENT_QUEUE_OWNER = new TextField(resultSetLogDetails.getString("EVENT_QUEUE_OWNER"));textField__EVENT_QUEUE_OWNER.setEditable(false);textField__EVENT_QUEUE_OWNER.setMinWidth(300);
                                                                                TextField textField__EVENT_QUEUE_NAME = new TextField(resultSetLogDetails.getString("EVENT_QUEUE_NAME"));textField__EVENT_QUEUE_NAME.setEditable(false);textField__EVENT_QUEUE_NAME.setMinWidth(300);
                                                                                TextField textField__EVENT_QUEUE_AGENT = new TextField(resultSetLogDetails.getString("EVENT_QUEUE_AGENT"));textField__EVENT_QUEUE_AGENT.setEditable(false);textField__EVENT_QUEUE_AGENT.setMinWidth(300);
                                                                                TextField textField__EVENT_CONDITION = new TextField(resultSetLogDetails.getString("EVENT_CONDITION"));textField__EVENT_CONDITION.setEditable(false);textField__EVENT_CONDITION.setMinWidth(300);
                                                                                TextField textField__EVENT_RULE = new TextField(resultSetLogDetails.getString("EVENT_RULE"));textField__EVENT_RULE.setEditable(false);textField__EVENT_RULE.setMinWidth(300);
                                                                                TextField textField__FILE_WATCHER_OWNER = new TextField(resultSetLogDetails.getString("FILE_WATCHER_OWNER"));textField__FILE_WATCHER_OWNER.setEditable(false);textField__FILE_WATCHER_OWNER.setMinWidth(300);
                                                                                TextField textField__FILE_WATCHER_NAME = new TextField(resultSetLogDetails.getString("FILE_WATCHER_NAME"));textField__FILE_WATCHER_NAME.setEditable(false);textField__FILE_WATCHER_NAME.setMinWidth(300);
                                                                                TextField textField__END_DATE = new TextField(resultSetLogDetails.getString("END_DATE"));textField__END_DATE.setEditable(false);textField__END_DATE.setMinWidth(300);
                                                                                TextField textField__JOB_CLASS = new TextField(resultSetLogDetails.getString("JOB_CLASS"));textField__JOB_CLASS.setEditable(false);textField__JOB_CLASS.setMinWidth(300);
                                                                                TextField textField__ENABLED = new TextField(resultSetLogDetails.getString("ENABLED"));textField__ENABLED.setEditable(false);textField__ENABLED.setMinWidth(300);
                                                                                TextField textField__AUTO_DROP = new TextField(resultSetLogDetails.getString("AUTO_DROP"));textField__AUTO_DROP.setEditable(false);textField__AUTO_DROP.setMinWidth(300);
                                                                                TextField textField__RESTART_ON_RECOVERY = new TextField(resultSetLogDetails.getString("RESTART_ON_RECOVERY"));textField__RESTART_ON_RECOVERY.setEditable(false);textField__RESTART_ON_RECOVERY.setMinWidth(300);
                                                                                TextField textField__RESTART_ON_FAILURE = new TextField(resultSetLogDetails.getString("RESTART_ON_FAILURE"));textField__RESTART_ON_FAILURE.setEditable(false);textField__RESTART_ON_FAILURE.setMinWidth(300);
                                                                                TextField textField__STATE = new TextField(resultSetLogDetails.getString("STATE"));textField__STATE.setEditable(false);textField__STATE.setMinWidth(300);
                                                                                TextField textField__JOB_PRIORITY = new TextField(resultSetLogDetails.getString("JOB_PRIORITY"));textField__JOB_PRIORITY.setEditable(false);textField__JOB_PRIORITY.setMinWidth(300);
                                                                                TextField textField__RUN_COUNT = new TextField(resultSetLogDetails.getString("RUN_COUNT"));textField__RUN_COUNT.setEditable(false);textField__RUN_COUNT.setMinWidth(300);
                                                                                TextField textField__UPTIME_RUN_COUNT = new TextField(resultSetLogDetails.getString("UPTIME_RUN_COUNT"));textField__UPTIME_RUN_COUNT.setEditable(false);textField__UPTIME_RUN_COUNT.setMinWidth(300);
                                                                                TextField textField__MAX_RUNS = new TextField(resultSetLogDetails.getString("MAX_RUNS"));textField__MAX_RUNS.setEditable(false);textField__MAX_RUNS.setMinWidth(300);
                                                                                TextField textField__FAILURE_COUNT = new TextField(resultSetLogDetails.getString("FAILURE_COUNT"));textField__FAILURE_COUNT.setEditable(false);textField__FAILURE_COUNT.setMinWidth(300);
                                                                                TextField textField__UPTIME_FAILURE_COUNT = new TextField(resultSetLogDetails.getString("UPTIME_FAILURE_COUNT"));textField__UPTIME_FAILURE_COUNT.setEditable(false);textField__UPTIME_FAILURE_COUNT.setMinWidth(300);
                                                                                TextField textField__MAX_FAILURES = new TextField(resultSetLogDetails.getString("MAX_FAILURES"));textField__MAX_FAILURES.setEditable(false);textField__MAX_FAILURES.setMinWidth(300);
                                                                                TextField textField__RETRY_COUNT = new TextField(resultSetLogDetails.getString("RETRY_COUNT"));textField__RETRY_COUNT.setEditable(false);textField__RETRY_COUNT.setMinWidth(300);
                                                                                TextField textField__LAST_START_DATE = new TextField(resultSetLogDetails.getString("LAST_START_DATE"));textField__LAST_START_DATE.setEditable(false);textField__LAST_START_DATE.setMinWidth(300);
                                                                                TextField textField__LAST_RUN_DURATION = new TextField(resultSetLogDetails.getString("LAST_RUN_DURATION"));textField__LAST_RUN_DURATION.setEditable(false);textField__LAST_RUN_DURATION.setMinWidth(300);
                                                                                TextField textField__NEXT_RUN_DATE = new TextField(resultSetLogDetails.getString("NEXT_RUN_DATE"));textField__NEXT_RUN_DATE.setEditable(false);textField__NEXT_RUN_DATE.setMinWidth(300);
                                                                                TextField textField__SCHEDULE_LIMIT = new TextField(resultSetLogDetails.getString("SCHEDULE_LIMIT"));textField__SCHEDULE_LIMIT.setEditable(false);textField__SCHEDULE_LIMIT.setMinWidth(300);
                                                                                TextField textField__MAX_RUN_DURATION = new TextField(resultSetLogDetails.getString("MAX_RUN_DURATION"));textField__MAX_RUN_DURATION.setEditable(false);textField__MAX_RUN_DURATION.setMinWidth(300);
                                                                                TextField textField__LOGGING_LEVEL = new TextField(resultSetLogDetails.getString("LOGGING_LEVEL"));textField__LOGGING_LEVEL.setEditable(false);textField__LOGGING_LEVEL.setMinWidth(300);
                                                                                TextField textField__STORE_OUTPUT = new TextField(resultSetLogDetails.getString("STORE_OUTPUT"));textField__STORE_OUTPUT.setEditable(false);textField__STORE_OUTPUT.setMinWidth(300);
                                                                                TextField textField__STOP_ON_WINDOW_CLOSE = new TextField(resultSetLogDetails.getString("STOP_ON_WINDOW_CLOSE"));textField__STOP_ON_WINDOW_CLOSE.setEditable(false);textField__STOP_ON_WINDOW_CLOSE.setMinWidth(300);
                                                                                TextField textField__INSTANCE_STICKINESS = new TextField(resultSetLogDetails.getString("INSTANCE_STICKINESS"));textField__INSTANCE_STICKINESS.setEditable(false);textField__INSTANCE_STICKINESS.setMinWidth(300);
                                                                                TextField textField__RAISE_EVENTS = new TextField(resultSetLogDetails.getString("RAISE_EVENTS"));textField__RAISE_EVENTS.setEditable(false);textField__RAISE_EVENTS.setMinWidth(300);
                                                                                TextField textField__SYSTEM = new TextField(resultSetLogDetails.getString("SYSTEM"));textField__SYSTEM.setEditable(false);textField__SYSTEM.setMinWidth(300);
                                                                                TextField textField__JOB_WEIGHT = new TextField(resultSetLogDetails.getString("JOB_WEIGHT"));textField__JOB_WEIGHT.setEditable(false);textField__JOB_WEIGHT.setMinWidth(300);
                                                                                TextField textField__NLS_ENV = new TextField(resultSetLogDetails.getString("NLS_ENV"));textField__NLS_ENV.setEditable(false);textField__NLS_ENV.setMinWidth(300);
                                                                                TextField textField__SOURCE = new TextField(resultSetLogDetails.getString("SOURCE"));textField__SOURCE.setEditable(false);textField__SOURCE.setMinWidth(300);
                                                                                TextField textField__NUMBER_OF_DESTINATIONS = new TextField(resultSetLogDetails.getString("NUMBER_OF_DESTINATIONS"));textField__NUMBER_OF_DESTINATIONS.setEditable(false);textField__NUMBER_OF_DESTINATIONS.setMinWidth(300);
                                                                                TextField textField__DESTINATION_OWNER = new TextField(resultSetLogDetails.getString("DESTINATION_OWNER"));textField__DESTINATION_OWNER.setEditable(false);textField__DESTINATION_OWNER.setMinWidth(300);
                                                                                TextField textField__DESTINATION = new TextField(resultSetLogDetails.getString("DESTINATION"));textField__DESTINATION.setEditable(false);textField__DESTINATION.setMinWidth(300);
                                                                                TextField textField__CREDENTIAL_OWNER = new TextField(resultSetLogDetails.getString("CREDENTIAL_OWNER"));textField__CREDENTIAL_OWNER.setEditable(false);textField__CREDENTIAL_OWNER.setMinWidth(300);
                                                                                TextField textField__CREDENTIAL_NAME = new TextField(resultSetLogDetails.getString("CREDENTIAL_NAME"));textField__CREDENTIAL_NAME.setEditable(false);textField__CREDENTIAL_NAME.setMinWidth(300);
                                                                                TextField textField__INSTANCE_ID = new TextField(resultSetLogDetails.getString("INSTANCE_ID"));textField__INSTANCE_ID.setEditable(false);textField__INSTANCE_ID.setMinWidth(300);
                                                                                TextField textField__DEFERRED_DROP = new TextField(resultSetLogDetails.getString("DEFERRED_DROP"));textField__DEFERRED_DROP.setEditable(false);textField__DEFERRED_DROP.setMinWidth(300);
                                                                                TextField textField__ALLOW_RUNS_IN_RESTRICTED_MODE = new TextField(resultSetLogDetails.getString("ALLOW_RUNS_IN_RESTRICTED_MODE"));textField__ALLOW_RUNS_IN_RESTRICTED_MODE.setEditable(false);textField__ALLOW_RUNS_IN_RESTRICTED_MODE.setMinWidth(300);
                                                                                TextField textField__COMMENTS = new TextField(resultSetLogDetails.getString("COMMENTS"));textField__COMMENTS.setEditable(false);textField__COMMENTS.setMinWidth(300);
                                                                                TextField textField__FLAGS = new TextField(resultSetLogDetails.getString("FLAGS"));textField__FLAGS.setEditable(false);textField__FLAGS.setMinWidth(300);
                                                                                TextField textField__RESTARTABLE = new TextField(resultSetLogDetails.getString("RESTARTABLE"));textField__RESTARTABLE.setEditable(false);textField__RESTARTABLE.setMinWidth(300);
                                                                                TextField textField__HAS_CONSTRAINTS = new TextField(resultSetLogDetails.getString("HAS_CONSTRAINTS"));textField__HAS_CONSTRAINTS.setEditable(false);textField__HAS_CONSTRAINTS.setMinWidth(300);
                                                                                TextField textField__CONNECT_CREDENTIAL_OWNER = new TextField(resultSetLogDetails.getString("CONNECT_CREDENTIAL_OWNER"));textField__CONNECT_CREDENTIAL_OWNER.setEditable(false);textField__CONNECT_CREDENTIAL_OWNER.setMinWidth(300);
                                                                                TextField textField__CONNECT_CREDENTIAL_NAME = new TextField(resultSetLogDetails.getString("CONNECT_CREDENTIAL_NAME"));textField__CONNECT_CREDENTIAL_NAME.setEditable(false);textField__CONNECT_CREDENTIAL_NAME.setMinWidth(300);
                                                                                TextField textField__FAIL_ON_SCRIPT_ERROR = new TextField(resultSetLogDetails.getString("FAIL_ON_SCRIPT_ERROR"));textField__FAIL_ON_SCRIPT_ERROR.setEditable(false);textField__FAIL_ON_SCRIPT_ERROR.setMinWidth(300);


                                                                                gridPaneInScrollPaneJobDetails.add(label__OWNER,0,0);gridPaneInScrollPaneJobDetails.add(textField__LOG_ID,1,0);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_NAME,0,1);gridPaneInScrollPaneJobDetails.add(textField__JOB_NAME,1,1);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_SUBNAME,0,2);gridPaneInScrollPaneJobDetails.add(textField__JOB_SUBNAME,1,2);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_STYLE,0,3);gridPaneInScrollPaneJobDetails.add(textField__JOB_STYLE,1,3);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_CREATOR,0,4);gridPaneInScrollPaneJobDetails.add(textField__JOB_CREATOR,1,4);
                                                                                gridPaneInScrollPaneJobDetails.add(label__CLIENT_ID,0,5);gridPaneInScrollPaneJobDetails.add(textField__CLIENT_ID,1,5);
                                                                                gridPaneInScrollPaneJobDetails.add(label__GLOBAL_UID,0,6);gridPaneInScrollPaneJobDetails.add(textField__GLOBAL_UID,1,6);
                                                                                gridPaneInScrollPaneJobDetails.add(label__PROGRAM_OWNER,0,7);gridPaneInScrollPaneJobDetails.add(textField__PROGRAM_OWNER,1,7);
                                                                                gridPaneInScrollPaneJobDetails.add(label__PROGRAM_NAME,0,8);gridPaneInScrollPaneJobDetails.add(textField__PROGRAM_NAME,1,8);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_TYPE,0,9);gridPaneInScrollPaneJobDetails.add(textField__JOB_TYPE,1,9);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_ACTION,0,10);gridPaneInScrollPaneJobDetails.add(textField__JOB_ACTION,1,10);
                                                                                gridPaneInScrollPaneJobDetails.add(label__NUMBER_OF_ARGUMENTS,0,11);gridPaneInScrollPaneJobDetails.add(textField__NUMBER_OF_ARGUMENTS,1,11);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SCHEDULE_OWNER,0,12);gridPaneInScrollPaneJobDetails.add(textField__SCHEDULE_OWNER,1,12);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SCHEDULE_NAME,0,13);gridPaneInScrollPaneJobDetails.add(textField__SCHEDULE_NAME,1,13);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SCHEDULE_TYPE,0,14);gridPaneInScrollPaneJobDetails.add(textField__SCHEDULE_TYPE,1,14);
                                                                                gridPaneInScrollPaneJobDetails.add(label__START_DATE,0,15);gridPaneInScrollPaneJobDetails.add(textField__START_DATE,1,15);
                                                                                gridPaneInScrollPaneJobDetails.add(label__REPEAT_INTERVAL,0,16);gridPaneInScrollPaneJobDetails.add(textField__REPEAT_INTERVAL,1,16);
                                                                                gridPaneInScrollPaneJobDetails.add(label__EVENT_QUEUE_OWNER,0,17);gridPaneInScrollPaneJobDetails.add(textField__EVENT_QUEUE_OWNER,1,17);
                                                                                gridPaneInScrollPaneJobDetails.add(label__EVENT_QUEUE_NAME,0,18);gridPaneInScrollPaneJobDetails.add(textField__EVENT_QUEUE_NAME,1,18);
                                                                                gridPaneInScrollPaneJobDetails.add(label__EVENT_QUEUE_AGENT,0,19);gridPaneInScrollPaneJobDetails.add(textField__EVENT_QUEUE_AGENT,1,19);
                                                                                gridPaneInScrollPaneJobDetails.add(label__EVENT_CONDITION,0,20);gridPaneInScrollPaneJobDetails.add(textField__EVENT_CONDITION,1,20);
                                                                                gridPaneInScrollPaneJobDetails.add(label__EVENT_RULE,0,21);gridPaneInScrollPaneJobDetails.add(textField__EVENT_RULE,1,21);
                                                                                gridPaneInScrollPaneJobDetails.add(label__FILE_WATCHER_OWNER,0,22);gridPaneInScrollPaneJobDetails.add(textField__FILE_WATCHER_OWNER,1,22);
                                                                                gridPaneInScrollPaneJobDetails.add(label__FILE_WATCHER_NAME,0,23);gridPaneInScrollPaneJobDetails.add(textField__FILE_WATCHER_NAME,1,23);
                                                                                gridPaneInScrollPaneJobDetails.add(label__END_DATE,0,24);gridPaneInScrollPaneJobDetails.add(textField__END_DATE,1,24);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_CLASS,0,25);gridPaneInScrollPaneJobDetails.add(textField__JOB_CLASS,1,25);
                                                                                gridPaneInScrollPaneJobDetails.add(label__ENABLED,0,26);gridPaneInScrollPaneJobDetails.add(textField__ENABLED,1,26);
                                                                                gridPaneInScrollPaneJobDetails.add(label__AUTO_DROP,0,27);gridPaneInScrollPaneJobDetails.add(textField__AUTO_DROP,1,27);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RESTART_ON_RECOVERY,0,28);gridPaneInScrollPaneJobDetails.add(textField__RESTART_ON_RECOVERY,1,28);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RESTART_ON_FAILURE,0,29);gridPaneInScrollPaneJobDetails.add(textField__RESTART_ON_FAILURE,1,29);
                                                                                gridPaneInScrollPaneJobDetails.add(label__STATE,0,30);gridPaneInScrollPaneJobDetails.add(textField__STATE,1,30);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_PRIORITY,0,31);gridPaneInScrollPaneJobDetails.add(textField__JOB_PRIORITY,1,31);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RUN_COUNT,0,32);gridPaneInScrollPaneJobDetails.add(textField__RUN_COUNT,1,32);
                                                                                gridPaneInScrollPaneJobDetails.add(label__UPTIME_RUN_COUNT,0,33);gridPaneInScrollPaneJobDetails.add(textField__UPTIME_RUN_COUNT,1,33);
                                                                                gridPaneInScrollPaneJobDetails.add(label__MAX_RUNS,0,34);gridPaneInScrollPaneJobDetails.add(textField__MAX_RUNS,1,34);
                                                                                gridPaneInScrollPaneJobDetails.add(label__FAILURE_COUNT,0,35);gridPaneInScrollPaneJobDetails.add(textField__FAILURE_COUNT,1,35);
                                                                                gridPaneInScrollPaneJobDetails.add(label__UPTIME_FAILURE_COUNT,0,36);gridPaneInScrollPaneJobDetails.add(textField__UPTIME_FAILURE_COUNT,1,36);
                                                                                gridPaneInScrollPaneJobDetails.add(label__MAX_FAILURES,0,37);gridPaneInScrollPaneJobDetails.add(textField__MAX_FAILURES,1,37);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RETRY_COUNT,0,38);gridPaneInScrollPaneJobDetails.add(textField__RETRY_COUNT,1,38);
                                                                                gridPaneInScrollPaneJobDetails.add(label__LAST_START_DATE,0,39);gridPaneInScrollPaneJobDetails.add(textField__LAST_START_DATE,1,39);
                                                                                gridPaneInScrollPaneJobDetails.add(label__LAST_RUN_DURATION,0,40);gridPaneInScrollPaneJobDetails.add(textField__LAST_RUN_DURATION,1,40);
                                                                                gridPaneInScrollPaneJobDetails.add(label__NEXT_RUN_DATE,0,41);gridPaneInScrollPaneJobDetails.add(textField__NEXT_RUN_DATE,1,41);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SCHEDULE_LIMIT,0,42);gridPaneInScrollPaneJobDetails.add(textField__SCHEDULE_LIMIT,1,42);
                                                                                gridPaneInScrollPaneJobDetails.add(label__MAX_RUN_DURATION,0,43);gridPaneInScrollPaneJobDetails.add(textField__MAX_RUN_DURATION,1,43);
                                                                                gridPaneInScrollPaneJobDetails.add(label__LOGGING_LEVEL,0,44);gridPaneInScrollPaneJobDetails.add(textField__LOGGING_LEVEL,1,44);
                                                                                gridPaneInScrollPaneJobDetails.add(label__STORE_OUTPUT,0,45);gridPaneInScrollPaneJobDetails.add(textField__STORE_OUTPUT,1,45);
                                                                                gridPaneInScrollPaneJobDetails.add(label__STOP_ON_WINDOW_CLOSE,0,46);gridPaneInScrollPaneJobDetails.add(textField__STOP_ON_WINDOW_CLOSE,1,46);
                                                                                gridPaneInScrollPaneJobDetails.add(label__INSTANCE_STICKINESS,0,47);gridPaneInScrollPaneJobDetails.add(textField__INSTANCE_STICKINESS,1,47);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RAISE_EVENTS,0,48);gridPaneInScrollPaneJobDetails.add(textField__RAISE_EVENTS,1,48);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SYSTEM,0,49);gridPaneInScrollPaneJobDetails.add(textField__SYSTEM,1,49);
                                                                                gridPaneInScrollPaneJobDetails.add(label__JOB_WEIGHT,0,50);gridPaneInScrollPaneJobDetails.add(textField__JOB_WEIGHT,1,50);
                                                                                gridPaneInScrollPaneJobDetails.add(label__NLS_ENV,0,51);gridPaneInScrollPaneJobDetails.add(textField__NLS_ENV,1,51);
                                                                                gridPaneInScrollPaneJobDetails.add(label__SOURCE,0,52);gridPaneInScrollPaneJobDetails.add(textField__SOURCE,1,52);
                                                                                gridPaneInScrollPaneJobDetails.add(label__NUMBER_OF_DESTINATIONS,0,53);gridPaneInScrollPaneJobDetails.add(textField__NUMBER_OF_DESTINATIONS,1,53);
                                                                                gridPaneInScrollPaneJobDetails.add(label__DESTINATION_OWNER,0,54);gridPaneInScrollPaneJobDetails.add(textField__DESTINATION_OWNER,1,54);
                                                                                gridPaneInScrollPaneJobDetails.add(label__DESTINATION,0,55);gridPaneInScrollPaneJobDetails.add(textField__DESTINATION,1,55);
                                                                                gridPaneInScrollPaneJobDetails.add(label__CREDENTIAL_OWNER,0,56);gridPaneInScrollPaneJobDetails.add(textField__CREDENTIAL_OWNER,1,56);
                                                                                gridPaneInScrollPaneJobDetails.add(label__CREDENTIAL_NAME,0,57);gridPaneInScrollPaneJobDetails.add(textField__CREDENTIAL_NAME,1,57);
                                                                                gridPaneInScrollPaneJobDetails.add(label__INSTANCE_ID,0,58);gridPaneInScrollPaneJobDetails.add(textField__INSTANCE_ID,1,58);
                                                                                gridPaneInScrollPaneJobDetails.add(label__DEFERRED_DROP,0,59);gridPaneInScrollPaneJobDetails.add(textField__DEFERRED_DROP,1,59);
                                                                                gridPaneInScrollPaneJobDetails.add(label__ALLOW_RUNS_IN_RESTRICTED_MODE,0,60);gridPaneInScrollPaneJobDetails.add(textField__ALLOW_RUNS_IN_RESTRICTED_MODE,1,60);
                                                                                gridPaneInScrollPaneJobDetails.add(label__COMMENTS,0,61);gridPaneInScrollPaneJobDetails.add(textField__COMMENTS,1,61);
                                                                                gridPaneInScrollPaneJobDetails.add(label__FLAGS,0,62);gridPaneInScrollPaneJobDetails.add(textField__FLAGS,1,62);
                                                                                gridPaneInScrollPaneJobDetails.add(label__RESTARTABLE,0,63);gridPaneInScrollPaneJobDetails.add(textField__RESTARTABLE,1,63);
                                                                                gridPaneInScrollPaneJobDetails.add(label__HAS_CONSTRAINTS,0,64);gridPaneInScrollPaneJobDetails.add(textField__HAS_CONSTRAINTS,1,64);
                                                                                gridPaneInScrollPaneJobDetails.add(label__CONNECT_CREDENTIAL_OWNER,0,65);gridPaneInScrollPaneJobDetails.add(textField__CONNECT_CREDENTIAL_OWNER,1,65);
                                                                                gridPaneInScrollPaneJobDetails.add(label__CONNECT_CREDENTIAL_NAME,0,66);gridPaneInScrollPaneJobDetails.add(textField__CONNECT_CREDENTIAL_NAME,1,66);
                                                                                gridPaneInScrollPaneJobDetails.add(label__FAIL_ON_SCRIPT_ERROR,0,67);gridPaneInScrollPaneJobDetails.add(textField__FAIL_ON_SCRIPT_ERROR,1,67);


                                                                            }
                                                                        }catch(Exception e){System.out.println(e);}
                                                                        dialogJobDetails.getDialogPane().setContent(dialogJobDetailsGridPane);
                                                                        dialogJobDetails.show();
                                                                    }
                                                                });


                                                                TextField ENABLED__TEXT = new TextField(resultSetSelectAllSchedulerJobsByOwnerName.getString("ENABLED"));ENABLED__TEXT.setEditable(false);gridPaneInScrollPaneAllSchedulerJobs.add(ENABLED__TEXT,2,iAllSchedulerJobsIndex);ENABLED__TEXT.setMinWidth(80);ENABLED__TEXT.setPrefWidth(80);ENABLED__TEXT.setMaxWidth(80);ENABLED__TEXT.setStyle("-fx-font-size:10");
                                                                TextField STATE__TEXT = new TextField(resultSetSelectAllSchedulerJobsByOwnerName.getString("STATE"));STATE__TEXT.setEditable(false);gridPaneInScrollPaneAllSchedulerJobs.add(STATE__TEXT,3,iAllSchedulerJobsIndex);STATE__TEXT.setMinWidth(80);STATE__TEXT.setPrefWidth(80);STATE__TEXT.setMaxWidth(80);STATE__TEXT.setStyle("-fx-font-size:10");
                                                                TextField JOB_TYPE__TEXT = new TextField(resultSetSelectAllSchedulerJobsByOwnerName.getString("JOB_TYPE"));JOB_TYPE__TEXT.setEditable(false);gridPaneInScrollPaneAllSchedulerJobs.add(JOB_TYPE__TEXT,4,iAllSchedulerJobsIndex);JOB_TYPE__TEXT.setMinWidth(110);JOB_TYPE__TEXT.setPrefWidth(110);JOB_TYPE__TEXT.setMaxWidth(110);JOB_TYPE__TEXT.setStyle("-fx-font-size:10");
                                                                TextField REPEAT_INTERVAL__TEXT = new TextField(resultSetSelectAllSchedulerJobsByOwnerName.getString("REPEAT_INTERVAL"));REPEAT_INTERVAL__TEXT.setEditable(false);gridPaneInScrollPaneAllSchedulerJobs.add(REPEAT_INTERVAL__TEXT,5,iAllSchedulerJobsIndex);REPEAT_INTERVAL__TEXT.setMinWidth(160);REPEAT_INTERVAL__TEXT.setPrefWidth(160);REPEAT_INTERVAL__TEXT.setMaxWidth(160);REPEAT_INTERVAL__TEXT.setStyle("-fx-font-size:10");
                                                                TextField LAST_START_DATE__TEXT = new TextField(resultSetSelectAllSchedulerJobsByOwnerName.getString("LAST_START_DATE"));LAST_START_DATE__TEXT.setEditable(false);gridPaneInScrollPaneAllSchedulerJobs.add(LAST_START_DATE__TEXT,6,iAllSchedulerJobsIndex);LAST_START_DATE__TEXT.setMinWidth(170);LAST_START_DATE__TEXT.setPrefWidth(170);LAST_START_DATE__TEXT.setMaxWidth(170);LAST_START_DATE__TEXT.setStyle("-fx-font-size:10");
                                                                TextField NEXT_RUN_DATE__TEXT = new TextField(resultSetSelectAllSchedulerJobsByOwnerName.getString("NEXT_RUN_DATE"));NEXT_RUN_DATE__TEXT.setEditable(false);gridPaneInScrollPaneAllSchedulerJobs.add(NEXT_RUN_DATE__TEXT,7,iAllSchedulerJobsIndex);NEXT_RUN_DATE__TEXT.setMinWidth(170);NEXT_RUN_DATE__TEXT.setPrefWidth(170);NEXT_RUN_DATE__TEXT.setMaxWidth(170);NEXT_RUN_DATE__TEXT.setStyle("-fx-font-size:10");
                                                                iAllSchedulerJobsIndex++;
                                                            }
                                                        }catch(Exception e){//blad
                                                            System.out.println(e);
                                                        }
                                                        paneAllSchedulerJobs.getChildren().add(gridPaneAllSchedulerJobs);


                                                        dialogSchedulerGridPane.add(paneLastRunningJobs,0,3);
                                                        dialogSchedulerGridPane.add(paneAllSchedulerJobs,1,3);
                                                    }

                                                });
                                            }

                                            dialogScheduler.getDialogPane().setContent(dialogSchedulerGridPane);
                                            dialogScheduler.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
                                            dialogScheduler.show();
                                        }else{
                                            Alert notConnectedAlert = new Alert(Alert.AlertType.ERROR);
                                            notConnectedAlert.setTitle("Not connected to database!");
                                            notConnectedAlert.setHeaderText(null);
                                            notConnectedAlert.setContentText("Connection to database '"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPort()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID()+"' is not reachable");
                                            notConnectedAlert.show();
                                        }
                                    }catch(Exception e){
                                        Alert notConnectedAlert = new Alert(Alert.AlertType.ERROR);
                                        notConnectedAlert.setTitle("Not connected to database!");
                                        notConnectedAlert.setHeaderText(null);
                                        notConnectedAlert.setContentText("Connection to database '"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPort()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID()+"'\nis not reachable\n\n"+"Error details: \n"+e.toString());
                                        notConnectedAlert.show();
                                    }
                                }else{
                                    Alert notConnectedAlert = new Alert(Alert.AlertType.ERROR);
                                    notConnectedAlert.setTitle("Database connection timeout!");
                                    notConnectedAlert.setHeaderText(null);
                                    notConnectedAlert.setContentText("Connection to database '"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPort()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID()+"'\nis not reachable\n\n"+"Connection timed out ("+connectionTimeoutSeconds+" seconds ping limit)");
                                    notConnectedAlert.show();
                                }
                            }
                        });









                        /*spfileButton.setOnAction(new EventHandler<ActionEvent>(){
                            public void handle(ActionEvent event){
                                try{
                                    connectionDatabaseListDialog.setWidth(700);connectionDatabaseListDialog.setHeight(600);
                                    ScrollPane spfileScrollPane = new ScrollPane();
                                    GridPane spfileGridPane = new GridPane();

                                    Task<Void> getSpfile = new Task<Void>() {
                                        @Override
                                        protected Void call(){
                                            try{
                                                Label spfileNameColumnHeader = new Label("NAME");
                                                Label spfileValueColumnHeader = new Label("VALUE");
                                                Label spfileDescriptionColumnHeader = new Label("DESCRIPTION");
                                                String query = "select NAME, VALUE, DESCRIPTION from v$parameter order by NAME ASC";
                                                String URL = "jdbc:oracle:thin:@"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionAddress()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPort()+":"+connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionServiceNameOrSID();
                                                System.out.println(URL);
                                                java.sql.Connection connection = DriverManager.getConnection(URL, connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionUser(), connections.getConnectionFromConnectionListByConnectionName(comboBoxConnectionsList.getValue().toString()).getConnectionPassword());
                                                if (!connection.isClosed()) {
                                                    System.out.println("Connection successfully - show parameter");
                                                    Statement statement = connection.createStatement();
                                                    ResultSet resultSet = statement.executeQuery(query);
                                                    int i=0;
                                                    while(resultSet.next()){
                                                        TextField spfileNameColumn = new TextField(resultSet.getString("NAME"));spfileNameColumn.setEditable(false);
                                                        TextField spfileValueColumn = new TextField(resultSet.getString("VALUE"));spfileValueColumn.setEditable(false);
                                                        TextField spfileDescriptionColumn = new TextField(resultSet.getString("DESCRIPTION"));spfileDescriptionColumn.setEditable(false);spfileDescriptionColumn.setMinWidth(350);
                                                        spfileGridPane.add(spfileNameColumn,0,i);
                                                        spfileGridPane.add(spfileValueColumn,1,i);
                                                        spfileGridPane.add(spfileDescriptionColumn,2,i);
                                                        i++;
                                                    }
                                                    Platform.runLater(()->{
                                                        grid.add(spfileNameColumnHeader,0,6);
                                                        grid.add(spfileValueColumnHeader,1,6);
                                                        grid.add(spfileDescriptionColumnHeader,2,6);
                                                        spfileScrollPane.setContent(spfileGridPane);
                                                    });
                                                }
                                            }catch(Exception e){System.out.println(e);}
                                            return null;
                                        }
                                    };
                                    Thread thGetSpfile = new Thread(getSpfile);
                                    thGetSpfile.setDaemon(true);
                                    thGetSpfile.start();
                                    grid.add(spfileScrollPane,0,7,3,1);
                                }catch(Exception e){
                                    System.out.println(e);
                                }
                            }
                        });dzialajacy button z spfile*/


                        /*spfileButton.setOnAction(new EventHandler<ActionEvent>(){
                            public void handle(ActionEvent event){
                                try{

                                }catch(Exception e){
                                }
                            }
                        });*/





                    });
                }
                connectionDatabaseListDialog.getDialogPane().setContent(grid);
                connectionDatabaseListDialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
                connectionDatabaseListDialog.show();
            }
        });


        MenuItem menuDatabaseItemDatabaseInfo = new MenuItem("Information");
        menuDatabaseItemDatabaseInfo.setMnemonicParsing(true);
        menuDatabaseItemDatabaseInfo.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            }
        });

        menuDatabase.getItems().addAll(menuDatabaseItemDatabase, menuDatabaseItemDatabaseInfo);
        menuBar.getMenus().add(menuDatabase);


        //---KONIEC - 15_05_2021 - rozszerzenie na potrzeby kursu PBL - Problem Based Learning





        root.getChildren().add(mainBody);
        root.getChildren().add(menuBar);
        primaryStage.setScene(scene);
        primaryStage.show();
        connections.importConnectionListFromDefaultLocation();
        mainBody.getChildren().clear();mainBody.getChildren().addAll(showCharts());
    }


    /**This method is responsible for validating if data inserted into add/modify connection form are correct - if not, then form buttons are greyed (disabled)*/
    Boolean addConnectionPanelValidationIsInvalid(String connectionName, String connectionAddress, String connectionPort, String connectionServiceNameOrSid, String connectionUser, String connectionRefreshRate){
        try{
            Integer refreshRate=Integer.valueOf(connectionRefreshRate);
            if(connectionName.length()<=0 || connectionAddress.length()<=0 || connectionPort.length()<=0 || connectionServiceNameOrSid.length()<=0 || connectionUser.length()<=0 || refreshRate<=1){
                return true;
            }else{
                return false;
            }
        }catch(Exception e){
            return true;
        }
    }

    /**This method is responsible for checking if combobox in Connections List is empty*/
    public Boolean connectionInComboBoxIsNotSelected(ComboBox comboBox){
        if(comboBox.getValue()!=null){
            return false;
        }else{
            return true;
        }
    }

    /**This method is responsible for checking if combobox in Tablespace List is empty*/
    public Boolean tablespaceInComboBoxIsNotSelected(ComboBox comboBox){
        if(comboBox.getValue()!=null){
            return false;
        }else{
            return true;
        }
    }

    /**This method is responsible for checking if combobox in Scheduler Owner List is empty*/
    public Boolean schedulerOwnerInComboBoxIsNotSelected(ComboBox comboBox){
        if(comboBox.getValue()!=null){
            return false;
        }else{
            return true;
        }
    }

    /**This method is responsible for checking connection to database*/
    public Boolean connectedToDatabaseTest(String connectionAddress, String connectionPort, String connectionServiceNameOrSID, String connectionUser, String connectionPassword) throws SQLException, SQLException {
        String URL = "jdbc:oracle:thin:@"+connectionAddress+":"+connectionPort+":"+connectionServiceNameOrSID;
        java.sql.Connection connection = DriverManager.getConnection(URL, connectionUser, connectionPassword);
        if(!connection.isClosed()){
            connection.close();
            return true;
        }else{
            return false;
        }
    }

    /**This method is responsible for checking if server is pinging*/
    public Boolean pingTestToServer(String connectionAddress){
        try{
            InetAddress inetAddress = InetAddress.getByName(connectionAddress);
            if(inetAddress.isReachable(connectionTimeoutSeconds*1000)){
                return true;
            }else{
                return false;
            }
        }catch(Exception e){
            return false;
        }
    }

    /**This method runs application*/
    public static void main(String[] args){
        Application.launch();
    }


    /**This method is responsible for displaying charts with info about load database*/
    public GridPane showCharts(){
        int objectsInGrid=0;
        GridPane gridBody = new GridPane();
        for(Connection actualConnectionFromConnectionList : connections.getConnectionList()){
            Group chartsGroup = new Group();
            GridPane chartAndInfoGridPane = new GridPane();
            Label currentLoad = new Label(); currentLoad.setStyle("-fx-font-weight:BOLD;-fx-font-size:13;");
            Label connectionStatus = new Label();
            Group chartActualInfoAboutValuesGroup = new Group(); chartActualInfoAboutValuesGroup.setStyle("-fx-alignment: right;");
            GridPane chartActualInfoAboutValuesGrid = new GridPane(); chartActualInfoAboutValuesGrid.setStyle("-fx-font-size: 9");

            Rectangle valuesGroupRectangleValueOfOther = new Rectangle(15, 15, Paint.valueOf("rgba(248,110,170,1);"));
            Label valuesGroupLabelValueOfOther = new Label("Other: ");
            Label valuesGroupValueOfOther = new Label();
            chartActualInfoAboutValuesGrid.add(valuesGroupRectangleValueOfOther, 0, 0);
            chartActualInfoAboutValuesGrid.add(valuesGroupLabelValueOfOther, 1, 0);
            chartActualInfoAboutValuesGrid.add(valuesGroupValueOfOther, 2, 0);

            Rectangle valuesGroupRectangleValueOfCluster = new Rectangle(15, 15, Paint.valueOf("rgba(201,194,175,1);"));
            Label valuesGroupLabelValueOfCluster = new Label("Cluster: ");
            Label valuesGroupValueOfCluster = new Label();
            chartActualInfoAboutValuesGrid.add(valuesGroupRectangleValueOfCluster, 0, 1);
            chartActualInfoAboutValuesGrid.add(valuesGroupLabelValueOfCluster, 1, 1);
            chartActualInfoAboutValuesGrid.add(valuesGroupValueOfCluster, 2, 1);

            Rectangle valuesGroupRectangleValueOfQueueing = new Rectangle(15, 15, Paint.valueOf("rgba(194,183,155,1);"));
            Label valuesGroupLabelValueOfQueueing = new Label("Queueing: ");
            Label valuesGroupValueOfQueueing = new Label();
            chartActualInfoAboutValuesGrid.add(valuesGroupRectangleValueOfQueueing, 0, 2);
            chartActualInfoAboutValuesGrid.add(valuesGroupLabelValueOfQueueing, 1, 2);
            chartActualInfoAboutValuesGrid.add(valuesGroupValueOfQueueing, 2, 2);

            Rectangle valuesGroupRectangleValueOfNetwork = new Rectangle(15, 15, Paint.valueOf("rgba(159,147,113,1);"));
            Label valuesGroupLabelValueOfNetwork = new Label("Network: ");
            Label valuesGroupValueOfNetwork = new Label();
            chartActualInfoAboutValuesGrid.add(valuesGroupRectangleValueOfNetwork, 0, 3);
            chartActualInfoAboutValuesGrid.add(valuesGroupLabelValueOfNetwork, 1, 3);
            chartActualInfoAboutValuesGrid.add(valuesGroupValueOfNetwork, 2, 3);

            Rectangle valuesGroupRectangleValueOfAdministrative = new Rectangle(15, 15, Paint.valueOf("rgba(113,115,84,1);"));
            Label valuesGroupLabelValueOfAdministrative = new Label("Administrative: ");
            Label valuesGroupValueOfAdministrative = new Label();
            chartActualInfoAboutValuesGrid.add(valuesGroupRectangleValueOfAdministrative, 0, 4);
            chartActualInfoAboutValuesGrid.add(valuesGroupLabelValueOfAdministrative, 1, 4);
            chartActualInfoAboutValuesGrid.add(valuesGroupValueOfAdministrative, 2, 4);

            Rectangle valuesGroupRectangleValueOfConfiguration = new Rectangle(15, 15, Paint.valueOf("rgba(92,68,11,1);"));
            Label valuesGroupLabelValueOfConfiguration = new Label("Configuration: ");
            Label valuesGroupValueOfConfiguration = new Label();
            chartActualInfoAboutValuesGrid.add(valuesGroupRectangleValueOfConfiguration, 0, 5);
            chartActualInfoAboutValuesGrid.add(valuesGroupLabelValueOfConfiguration, 1, 5);
            chartActualInfoAboutValuesGrid.add(valuesGroupValueOfConfiguration, 2, 5);

            Rectangle valuesGroupRectangleValueOfApplication = new Rectangle(15, 15, Paint.valueOf("rgba(228,104,0,1);"));
            Label valuesGroupLabelValueOfApplication = new Label("Application: ");
            Label valuesGroupValueOfApplication = new Label();
            chartActualInfoAboutValuesGrid.add(valuesGroupRectangleValueOfApplication, 0, 6);
            chartActualInfoAboutValuesGrid.add(valuesGroupLabelValueOfApplication, 1, 6);
            chartActualInfoAboutValuesGrid.add(valuesGroupValueOfApplication, 2, 6);

            Rectangle valuesGroupRectangleValueOfCommit = new Rectangle(15, 15, Paint.valueOf("rgba(192,40,0,1);"));
            Label valuesGroupLabelValueOfCommit = new Label("Commit: ");
            Label valuesGroupValueOfCommit = new Label();
            chartActualInfoAboutValuesGrid.add(valuesGroupRectangleValueOfCommit, 0, 7);
            chartActualInfoAboutValuesGrid.add(valuesGroupLabelValueOfCommit, 1, 7);
            chartActualInfoAboutValuesGrid.add(valuesGroupValueOfCommit, 2, 7);

            Rectangle valuesGroupRectangleValueOfConcurrency = new Rectangle(15, 15, Paint.valueOf("rgba(139,26,0,1);"));
            Label valuesGroupLabelValueOfConcurrency = new Label("Concurrency: ");
            Label valuesGroupValueOfConcurrency = new Label();
            chartActualInfoAboutValuesGrid.add(valuesGroupRectangleValueOfConcurrency, 0, 8);
            chartActualInfoAboutValuesGrid.add(valuesGroupLabelValueOfConcurrency, 1, 8);
            chartActualInfoAboutValuesGrid.add(valuesGroupValueOfConcurrency, 2, 8);

            Rectangle valuesGroupRectangleValueOfSystemIO = new Rectangle(15, 15, Paint.valueOf("rgba(0,148,231,1);"));
            Label valuesGroupLabelValueOfSystemIO = new Label("System I/O: ");
            Label valuesGroupValueOfSystemIO = new Label();
            chartActualInfoAboutValuesGrid.add(valuesGroupRectangleValueOfSystemIO, 0, 9);
            chartActualInfoAboutValuesGrid.add(valuesGroupLabelValueOfSystemIO, 1, 9);
            chartActualInfoAboutValuesGrid.add(valuesGroupValueOfSystemIO, 2, 9);

            Rectangle valuesGroupRectangleValueOfScheduler = new Rectangle(15, 15, Paint.valueOf("rgba(0,74,231,1);"));
            Label valuesGroupLabelValueOfScheduler = new Label("Scheduler: ");
            Label valuesGroupValueOfScheduler = new Label();
            chartActualInfoAboutValuesGrid.add(valuesGroupRectangleValueOfScheduler, 0, 10);
            chartActualInfoAboutValuesGrid.add(valuesGroupLabelValueOfScheduler, 1, 10);
            chartActualInfoAboutValuesGrid.add(valuesGroupValueOfScheduler, 2, 10);

            Rectangle valuesGroupRectangleValueOfUserIO = new Rectangle(15, 15, Paint.valueOf("rgba(204,204,204,1);"));
            Label valuesGroupLabelValueOfUserIO = new Label("User I/O: ");
            Label valuesGroupValueOfUserIO = new Label();
            chartActualInfoAboutValuesGrid.add(valuesGroupRectangleValueOfUserIO, 0, 11);
            chartActualInfoAboutValuesGrid.add(valuesGroupLabelValueOfUserIO, 1, 11);
            chartActualInfoAboutValuesGrid.add(valuesGroupValueOfUserIO, 2, 11);

            Rectangle valuesGroupRectangleValueOfCPU = new Rectangle(15, 15, Paint.valueOf("rgba(0,204,0,1);"));
            Label valuesGroupLabelValueOfCPU = new Label("CPU: ");
            Label valuesGroupValueOfCPU = new Label();
            chartActualInfoAboutValuesGrid.add(valuesGroupRectangleValueOfCPU, 0, 12);
            chartActualInfoAboutValuesGrid.add(valuesGroupLabelValueOfCPU, 1, 12);
            chartActualInfoAboutValuesGrid.add(valuesGroupValueOfCPU, 2, 12);

            chartActualInfoAboutValuesGroup.getChildren().add(chartActualInfoAboutValuesGrid);

            /**This if create a grid 4x~ for organizing charts on screen*/
            if(objectsInGrid==0){
                gridBody.add(chartsGroup, 0, 0);
            }else{
                double dividing = (double)objectsInGrid/4;
                double fractional = dividing - (int)dividing;
                double integral = dividing - fractional;
                int col = 0;
                int row = 0;
                if(fractional==0.0){col=0;}else if(fractional==0.25){col=1;}else if(fractional==0.50){col=2;}else if(fractional==0.75){col=3;}
                row=(int)integral;
                gridBody.add(chartsGroup, col, row);
            }
            objectsInGrid++;


            final CategoryAxis xAxis = new CategoryAxis();
            xAxis.setCategories(FXCollections.<String>observableArrayList
                    (Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24")));
            NumberAxis yAxis = new NumberAxis();
            xAxis.setStartMargin(0);
            StackedAreaChart<String, Number> areaChart = new StackedAreaChart<>(xAxis, yAxis);
            XYChart.Series<String, Number> series1 = new XYChart.Series<>();
            XYChart.Series<String, Number> series2 = new XYChart.Series<>();
            XYChart.Series<String, Number> series3 = new XYChart.Series<>();
            XYChart.Series<String, Number> series4 = new XYChart.Series<>();
            XYChart.Series<String, Number> series5 = new XYChart.Series<>();
            XYChart.Series<String, Number> series6 = new XYChart.Series<>();
            XYChart.Series<String, Number> series7 = new XYChart.Series<>();
            XYChart.Series<String, Number> series8 = new XYChart.Series<>();
            XYChart.Series<String, Number> series9 = new XYChart.Series<>();
            XYChart.Series<String, Number> series10 = new XYChart.Series<>();
            XYChart.Series<String, Number> series11 = new XYChart.Series<>();
            XYChart.Series<String, Number> series12 = new XYChart.Series<>();
            XYChart.Series<String, Number> series13 = new XYChart.Series<>();

            areaChart.getData().add(series1);
            areaChart.getData().add(series2);
            areaChart.getData().add(series3);
            areaChart.getData().add(series4);
            areaChart.getData().add(series5);
            areaChart.getData().add(series6);
            areaChart.getData().add(series7);
            areaChart.getData().add(series8);
            areaChart.getData().add(series9);
            areaChart.getData().add(series10);
            areaChart.getData().add(series11);
            areaChart.getData().add(series12);
            areaChart.getData().add(series13);

            areaChart.getStylesheets().add(getClass().getResource("style3.css").toExternalForm());
            areaChart.setAnimated(false);
            areaChart.setLegendVisible(false);
            areaChart.getXAxis().setTickLabelsVisible(false);
            areaChart.getXAxis().setTickMarkVisible(false);
            areaChart.getXAxis().setOpacity(0);
            areaChart.setTitle(actualConnectionFromConnectionList.getConnectionName()+" <"+actualConnectionFromConnectionList.getConnectionServiceNameOrSID()+" | "+actualConnectionFromConnectionList.getConnectionAddress()+">");
            areaChart.setCreateSymbols(false);
            areaChart.setMaxHeight(200);


            /**This is responsible for specific background task - this task is connecting to database, gathering info about load and insert into chart*/
            // Background Task
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() {
                    if(pingTestToServer(actualConnectionFromConnectionList.getConnectionAddress())){


                        try{
                            String query = "select nvl((select time_waited/1E2 FROM v$system_wait_class where wait_class = 'Other'), 0) \"Other\",nvl((select time_waited/1E2 FROM v$system_wait_class where wait_class = 'Cluster'), 0) \"Cluster\",nvl((select time_waited/1E2 FROM v$system_wait_class where wait_class = 'Queueing'), 0) \"Queueing\",nvl((select time_waited/1E2 FROM v$system_wait_class where wait_class = 'Network'), 0) \"Network\",nvl((select time_waited/1E2 FROM v$system_wait_class where wait_class = 'Administrative'), 0) \"Administrative\",nvl((select time_waited/1E2 FROM v$system_wait_class where wait_class = 'Configuration'), 0) \"Configuration\",nvl((select time_waited/1E2 FROM v$system_wait_class where wait_class = 'Commit'), 0) \"Commit\",nvl((select time_waited/1E2 FROM v$system_wait_class where wait_class = 'Application'), 0) \"Application\",nvl((select time_waited/1E2 FROM v$system_wait_class where wait_class = 'Concurrency'), 0) \"Concurrency\",nvl((select time_waited/1E2 FROM v$system_wait_class where wait_class = 'System I/O'), 0) \"System I/O\",nvl((select time_waited/1E2 FROM v$system_wait_class where wait_class = 'User I/O'), 0) \"User I/O\",nvl((select time_waited/1E2 FROM v$system_wait_class where wait_class = 'Scheduler'), 0) \"Scheduler\",nvl((select sum(value)/1E6 from v$sys_time_model WHERE stat_name IN ('DB CPU','background cpu time')), 0) \"CPU\" from dual";
                            String URL = "jdbc:oracle:thin:@"+actualConnectionFromConnectionList.getConnectionAddress()+":"+actualConnectionFromConnectionList.getConnectionPort()+":"+actualConnectionFromConnectionList.getConnectionServiceNameOrSID();
                            java.sql.Connection connection = DriverManager.getConnection(URL, actualConnectionFromConnectionList.getConnectionUser(), actualConnectionFromConnectionList.getConnectionPassword());
                            if (!connection.isClosed()) {
                                System.out.println("Connection successfully");
                                Statement statement = connection.createStatement();
                                Double[] arrayOther = new Double[25];
                                Double[] arrayCluster = new Double[25];
                                Double[] arrayQueueing = new Double[25];
                                Double[] arrayNetwork = new Double[25];
                                Double[] arrayAdministrative = new Double[25];
                                Double[] arrayConfiguration = new Double[25];
                                Double[] arrayApplication = new Double[25];
                                Double[] arrayCommit = new Double[25];
                                Double[] arrayConcurrency = new Double[25];
                                Double[] arraySystemIO = new Double[25];
                                Double[] arrayScheduler = new Double[25];
                                Double[] arrayUser = new Double[25];
                                Double[] arrayCPU = new Double[25];

                                int i = 0;
                                while (true) {
                                    ResultSet resultSet = statement.executeQuery(query);
                                    Double vOtherA=0.0;Double vOtherB=0.0;
                                    Double vClusterA=0.0;Double vClusterB=0.0;
                                    Double vQueueingA=0.0;Double vQueueingB=0.0;
                                    Double vNetworkA=0.0;Double vNetworkB=0.0;
                                    Double vAdministrativeA=0.0;Double vAdministrativeB=0.0;
                                    Double vConfigurationA=0.0;Double vConfigurationB=0.0;
                                    Double vApplicationA=0.0;Double vApplicationB=0.0;
                                    Double vCommitA=0.0;Double vCommitB=0.0;
                                    Double vConcurrencyA=0.0;Double vConcurrencyB=0.0;
                                    Double vSystemA=0.0;Double vSystemB=0.0;
                                    Double vSchedulerA=0.0;Double vSchedulerB=0.0;
                                    Double vUserA=0.0;Double vUserB=0.0;
                                    Double vCPUA=0.0;Double vCPUB=0.0;
                                    while(resultSet.next()){
                                        vOtherA=resultSet.getDouble("Other");
                                        vClusterA=resultSet.getDouble("Cluster");
                                        vQueueingA=resultSet.getDouble("Queueing");
                                        vNetworkA=resultSet.getDouble("Network");
                                        vAdministrativeA=resultSet.getDouble("Administrative");
                                        vConfigurationA=resultSet.getDouble("Configuration");
                                        vApplicationA=resultSet.getDouble("Application");
                                        vCommitA=resultSet.getDouble("Commit");
                                        vConcurrencyA=resultSet.getDouble("Concurrency");
                                        vSystemA=resultSet.getDouble("System I/O");
                                        vSchedulerA=resultSet.getDouble("Scheduler");
                                        vUserA=resultSet.getDouble("User I/O");
                                        vCPUA=resultSet.getDouble("CPU");
                                    }
                                    Integer refreshTime = Integer.valueOf(actualConnectionFromConnectionList.getConnectionRefreshRate())*1000;
                                    Thread.sleep(refreshTime);
                                    resultSet = statement.executeQuery(query);
                                    while(resultSet.next()){
                                        vOtherB=resultSet.getDouble("Other");
                                        vClusterB=resultSet.getDouble("Cluster");
                                        vQueueingB=resultSet.getDouble("Queueing");
                                        vNetworkB=resultSet.getDouble("Network");
                                        vAdministrativeB=resultSet.getDouble("Administrative");
                                        vConfigurationB=resultSet.getDouble("Configuration");
                                        vApplicationB=resultSet.getDouble("Application");
                                        vCommitB=resultSet.getDouble("Commit");
                                        vConcurrencyB=resultSet.getDouble("Concurrency");
                                        vSystemB=resultSet.getDouble("System I/O");
                                        vSchedulerB=resultSet.getDouble("Scheduler");
                                        vUserB=resultSet.getDouble("User I/O");
                                        vCPUB=resultSet.getDouble("CPU");
                                    }
                                    Double Other = Math.round((vOtherB-vOtherA)*100.0)/100.0;
                                    Double Cluster = Math.round((vClusterB-vClusterA)*100.0)/100.0;
                                    Double Queueing = Math.round((vQueueingB-vQueueingA)*100.0)/100.0;
                                    Double Network = Math.round((vNetworkB-vNetworkA)*100.0)/100.0;
                                    Double Administrative = Math.round((vAdministrativeB-vAdministrativeA)*100.0)/100.0;
                                    Double Configuration = Math.round((vConfigurationB-vConfigurationA)*100.0)/100.0;
                                    Double Application = Math.round((vApplicationB-vApplicationA)*100.0)/100.0;
                                    Double Commit = Math.round((vCommitB-vCommitA)*100.0)/100.0;
                                    Double Concurrency = Math.round((vConcurrencyB-vConcurrencyA)*100.0)/100.0;
                                    Double SystemIO = Math.round((vSystemB-vSystemA)*100.0)/100.0;
                                    Double Scheduler = Math.round((vSchedulerB-vSchedulerA)*100.0)/100.0;
                                    Double User = Math.round((vUserB-vUserA)*100.0)/100.0;
                                    Double CPU = Math.round((vCPUB-vCPUA)*100.0)/100.0;
                                    System.out.println("Other: "+Other
                                            +", Cluster: "+Cluster
                                            +", Queueing: "+Queueing
                                            +", Network: "+Network
                                            +", Administrative: "+Administrative
                                            +", Configuration: "+Configuration
                                            +", Application: "+Application
                                            +", Commit: "+Commit
                                            +", Concurrency: "+Concurrency
                                            +", SystemIO: "+SystemIO
                                            +", Scheduler: "+Scheduler
                                            +", User: "+User
                                            +", CPU: "+CPU);

                                    Double dbCurrentLoad = Math.round((Other+Cluster+Queueing+Network+Administrative+Configuration+Application+Commit+Concurrency+SystemIO+Scheduler+User+CPU)*100.0)/100.0;

                                    Platform.runLater(() -> {
                                        currentLoad.setText("\n                                                                                              "+String.valueOf(dbCurrentLoad));
                                        valuesGroupValueOfOther.setText(String.valueOf(Other));
                                        valuesGroupValueOfCluster.setText(String.valueOf(Cluster));
                                        valuesGroupValueOfQueueing.setText(String.valueOf(Queueing));
                                        valuesGroupValueOfNetwork.setText(String.valueOf(Network));
                                        valuesGroupValueOfAdministrative.setText(String.valueOf(Administrative));
                                        valuesGroupValueOfConfiguration.setText(String.valueOf(Configuration));
                                        valuesGroupValueOfApplication.setText(String.valueOf(Application));
                                        valuesGroupValueOfCommit.setText(String.valueOf(Commit));
                                        valuesGroupValueOfConcurrency.setText(String.valueOf(Concurrency));
                                        valuesGroupValueOfSystemIO.setText(String.valueOf(SystemIO));
                                        valuesGroupValueOfScheduler.setText(String.valueOf(Scheduler));
                                        valuesGroupValueOfUserIO.setText(String.valueOf(User));
                                        valuesGroupValueOfCPU.setText(String.valueOf(CPU));
                                    });


                                    int tempI;
                                    if(i>=0 && i<25){
                                        arrayOther[i] = Other;
                                        arrayCluster[i] = Cluster;
                                        arrayQueueing[i] = Queueing;
                                        arrayNetwork[i] = Network;
                                        arrayAdministrative[i] = Administrative;
                                        arrayConfiguration[i] = Configuration;
                                        arrayApplication[i] = Application;
                                        arrayCommit[i] = Commit;
                                        arrayConcurrency[i] = Concurrency;
                                        arraySystemIO[i] = SystemIO;
                                        arrayScheduler[i] = Scheduler;
                                        arrayUser[i] = User;
                                        arrayCPU[i] = CPU;
                                        tempI=i;
                                        Platform.runLater(() -> {
                                            Date now = new Date();
                                            series1.getData().add(new XYChart.Data<>(String.valueOf(tempI), arrayCPU[tempI]));
                                            series2.getData().add(new XYChart.Data<>(String.valueOf(tempI), arrayUser[tempI]));
                                            series3.getData().add(new XYChart.Data<>(String.valueOf(tempI), arrayScheduler[tempI]));
                                            series4.getData().add(new XYChart.Data<>(String.valueOf(tempI), arraySystemIO[tempI]));
                                            series5.getData().add(new XYChart.Data<>(String.valueOf(tempI), arrayConcurrency[tempI]));
                                            series6.getData().add(new XYChart.Data<>(String.valueOf(tempI), arrayCommit[tempI]));
                                            series7.getData().add(new XYChart.Data<>(String.valueOf(tempI), arrayApplication[tempI]));
                                            series8.getData().add(new XYChart.Data<>(String.valueOf(tempI), arrayConfiguration[tempI]));
                                            series9.getData().add(new XYChart.Data<>(String.valueOf(tempI), arrayAdministrative[tempI]));
                                            series10.getData().add(new XYChart.Data<>(String.valueOf(tempI), arrayNetwork[tempI]));
                                            series11.getData().add(new XYChart.Data<>(String.valueOf(tempI), arrayQueueing[tempI]));
                                            series12.getData().add(new XYChart.Data<>(String.valueOf(tempI), arrayCluster[tempI]));
                                            series13.getData().add(new XYChart.Data<>(String.valueOf(tempI), arrayOther[tempI]));
                                        });
                                    } else if (i == 25) {
                                        arrayOther[0] = arrayOther[1];
                                        arrayOther[1] = arrayOther[2];
                                        arrayOther[2] = arrayOther[3];
                                        arrayOther[3] = arrayOther[4];
                                        arrayOther[4] = arrayOther[5];
                                        arrayOther[5] = arrayOther[6];
                                        arrayOther[6] = arrayOther[7];
                                        arrayOther[7] = arrayOther[8];
                                        arrayOther[8] = arrayOther[9];
                                        arrayOther[9] = arrayOther[10];
                                        arrayOther[10] = arrayOther[11];
                                        arrayOther[11] = arrayOther[12];
                                        arrayOther[12] = arrayOther[13];
                                        arrayOther[13] = arrayOther[14];
                                        arrayOther[14] = arrayOther[15];
                                        arrayOther[15] = arrayOther[16];
                                        arrayOther[16] = arrayOther[17];
                                        arrayOther[17] = arrayOther[18];
                                        arrayOther[18] = arrayOther[19];
                                        arrayOther[19] = arrayOther[20];
                                        arrayOther[20] = arrayOther[21];
                                        arrayOther[21] = arrayOther[22];
                                        arrayOther[22] = arrayOther[23];
                                        arrayOther[23] = arrayOther[24];
                                        arrayOther[24] = Other;

                                        arrayCluster[0] = arrayCluster[1];
                                        arrayCluster[1] = arrayCluster[2];
                                        arrayCluster[2] = arrayCluster[3];
                                        arrayCluster[3] = arrayCluster[4];
                                        arrayCluster[4] = arrayCluster[5];
                                        arrayCluster[5] = arrayCluster[6];
                                        arrayCluster[6] = arrayCluster[7];
                                        arrayCluster[7] = arrayCluster[8];
                                        arrayCluster[8] = arrayCluster[9];
                                        arrayCluster[9] = arrayCluster[10];
                                        arrayCluster[10] = arrayCluster[11];
                                        arrayCluster[11] = arrayCluster[12];
                                        arrayCluster[12] = arrayCluster[13];
                                        arrayCluster[13] = arrayCluster[14];
                                        arrayCluster[14] = arrayCluster[15];
                                        arrayCluster[15] = arrayCluster[16];
                                        arrayCluster[16] = arrayCluster[17];
                                        arrayCluster[17] = arrayCluster[18];
                                        arrayCluster[18] = arrayCluster[19];
                                        arrayCluster[19] = arrayCluster[20];
                                        arrayCluster[20] = arrayCluster[21];
                                        arrayCluster[21] = arrayCluster[22];
                                        arrayCluster[22] = arrayCluster[23];
                                        arrayCluster[23] = arrayCluster[24];
                                        arrayCluster[24] = Cluster;

                                        arrayQueueing[0] = arrayQueueing[1];
                                        arrayQueueing[1] = arrayQueueing[2];
                                        arrayQueueing[2] = arrayQueueing[3];
                                        arrayQueueing[3] = arrayQueueing[4];
                                        arrayQueueing[4] = arrayQueueing[5];
                                        arrayQueueing[5] = arrayQueueing[6];
                                        arrayQueueing[6] = arrayQueueing[7];
                                        arrayQueueing[7] = arrayQueueing[8];
                                        arrayQueueing[8] = arrayQueueing[9];
                                        arrayQueueing[9] = arrayQueueing[10];
                                        arrayQueueing[10] = arrayQueueing[11];
                                        arrayQueueing[11] = arrayQueueing[12];
                                        arrayQueueing[12] = arrayQueueing[13];
                                        arrayQueueing[13] = arrayQueueing[14];
                                        arrayQueueing[14] = arrayQueueing[15];
                                        arrayQueueing[15] = arrayQueueing[16];
                                        arrayQueueing[16] = arrayQueueing[17];
                                        arrayQueueing[17] = arrayQueueing[18];
                                        arrayQueueing[18] = arrayQueueing[19];
                                        arrayQueueing[19] = arrayQueueing[20];
                                        arrayQueueing[20] = arrayQueueing[21];
                                        arrayQueueing[21] = arrayQueueing[22];
                                        arrayQueueing[22] = arrayQueueing[23];
                                        arrayQueueing[23] = arrayQueueing[24];
                                        arrayQueueing[24] = Queueing;

                                        arrayNetwork[0] = arrayNetwork[1];
                                        arrayNetwork[1] = arrayNetwork[2];
                                        arrayNetwork[2] = arrayNetwork[3];
                                        arrayNetwork[3] = arrayNetwork[4];
                                        arrayNetwork[4] = arrayNetwork[5];
                                        arrayNetwork[5] = arrayNetwork[6];
                                        arrayNetwork[6] = arrayNetwork[7];
                                        arrayNetwork[7] = arrayNetwork[8];
                                        arrayNetwork[8] = arrayNetwork[9];
                                        arrayNetwork[9] = arrayNetwork[10];
                                        arrayNetwork[10] = arrayNetwork[11];
                                        arrayNetwork[11] = arrayNetwork[12];
                                        arrayNetwork[12] = arrayNetwork[13];
                                        arrayNetwork[13] = arrayNetwork[14];
                                        arrayNetwork[14] = arrayNetwork[15];
                                        arrayNetwork[15] = arrayNetwork[16];
                                        arrayNetwork[16] = arrayNetwork[17];
                                        arrayNetwork[17] = arrayNetwork[18];
                                        arrayNetwork[18] = arrayNetwork[19];
                                        arrayNetwork[19] = arrayNetwork[20];
                                        arrayNetwork[20] = arrayNetwork[21];
                                        arrayNetwork[21] = arrayNetwork[22];
                                        arrayNetwork[22] = arrayNetwork[23];
                                        arrayNetwork[23] = arrayNetwork[24];
                                        arrayNetwork[24] = Network;

                                        arrayAdministrative[0] = arrayAdministrative[1];
                                        arrayAdministrative[1] = arrayAdministrative[2];
                                        arrayAdministrative[2] = arrayAdministrative[3];
                                        arrayAdministrative[3] = arrayAdministrative[4];
                                        arrayAdministrative[4] = arrayAdministrative[5];
                                        arrayAdministrative[5] = arrayAdministrative[6];
                                        arrayAdministrative[6] = arrayAdministrative[7];
                                        arrayAdministrative[7] = arrayAdministrative[8];
                                        arrayAdministrative[8] = arrayAdministrative[9];
                                        arrayAdministrative[9] = arrayAdministrative[10];
                                        arrayAdministrative[10] = arrayAdministrative[11];
                                        arrayAdministrative[11] = arrayAdministrative[12];
                                        arrayAdministrative[12] = arrayAdministrative[13];
                                        arrayAdministrative[13] = arrayAdministrative[14];
                                        arrayAdministrative[14] = arrayAdministrative[15];
                                        arrayAdministrative[15] = arrayAdministrative[16];
                                        arrayAdministrative[16] = arrayAdministrative[17];
                                        arrayAdministrative[17] = arrayAdministrative[18];
                                        arrayAdministrative[18] = arrayAdministrative[19];
                                        arrayAdministrative[19] = arrayAdministrative[20];
                                        arrayAdministrative[20] = arrayAdministrative[21];
                                        arrayAdministrative[21] = arrayAdministrative[22];
                                        arrayAdministrative[22] = arrayAdministrative[23];
                                        arrayAdministrative[23] = arrayAdministrative[24];
                                        arrayAdministrative[24] = Administrative;

                                        arrayConfiguration[0] = arrayConfiguration[1];
                                        arrayConfiguration[1] = arrayConfiguration[2];
                                        arrayConfiguration[2] = arrayConfiguration[3];
                                        arrayConfiguration[3] = arrayConfiguration[4];
                                        arrayConfiguration[4] = arrayConfiguration[5];
                                        arrayConfiguration[5] = arrayConfiguration[6];
                                        arrayConfiguration[6] = arrayConfiguration[7];
                                        arrayConfiguration[7] = arrayConfiguration[8];
                                        arrayConfiguration[8] = arrayConfiguration[9];
                                        arrayConfiguration[9] = arrayConfiguration[10];
                                        arrayConfiguration[10] = arrayConfiguration[11];
                                        arrayConfiguration[11] = arrayConfiguration[12];
                                        arrayConfiguration[12] = arrayConfiguration[13];
                                        arrayConfiguration[13] = arrayConfiguration[14];
                                        arrayConfiguration[14] = arrayConfiguration[15];
                                        arrayConfiguration[15] = arrayConfiguration[16];
                                        arrayConfiguration[16] = arrayConfiguration[17];
                                        arrayConfiguration[17] = arrayConfiguration[18];
                                        arrayConfiguration[18] = arrayConfiguration[19];
                                        arrayConfiguration[19] = arrayConfiguration[20];
                                        arrayConfiguration[20] = arrayConfiguration[21];
                                        arrayConfiguration[21] = arrayConfiguration[22];
                                        arrayConfiguration[22] = arrayConfiguration[23];
                                        arrayConfiguration[23] = arrayConfiguration[24];
                                        arrayConfiguration[24] = Configuration;

                                        arrayApplication[0] = arrayApplication[1];
                                        arrayApplication[1] = arrayApplication[2];
                                        arrayApplication[2] = arrayApplication[3];
                                        arrayApplication[3] = arrayApplication[4];
                                        arrayApplication[4] = arrayApplication[5];
                                        arrayApplication[5] = arrayApplication[6];
                                        arrayApplication[6] = arrayApplication[7];
                                        arrayApplication[7] = arrayApplication[8];
                                        arrayApplication[8] = arrayApplication[9];
                                        arrayApplication[9] = arrayApplication[10];
                                        arrayApplication[10] = arrayApplication[11];
                                        arrayApplication[11] = arrayApplication[12];
                                        arrayApplication[12] = arrayApplication[13];
                                        arrayApplication[13] = arrayApplication[14];
                                        arrayApplication[14] = arrayApplication[15];
                                        arrayApplication[15] = arrayApplication[16];
                                        arrayApplication[16] = arrayApplication[17];
                                        arrayApplication[17] = arrayApplication[18];
                                        arrayApplication[18] = arrayApplication[19];
                                        arrayApplication[19] = arrayApplication[20];
                                        arrayApplication[20] = arrayApplication[21];
                                        arrayApplication[21] = arrayApplication[22];
                                        arrayApplication[22] = arrayApplication[23];
                                        arrayApplication[23] = arrayApplication[24];
                                        arrayApplication[24] = Application;

                                        arrayCommit[0] = arrayCommit[1];
                                        arrayCommit[1] = arrayCommit[2];
                                        arrayCommit[2] = arrayCommit[3];
                                        arrayCommit[3] = arrayCommit[4];
                                        arrayCommit[4] = arrayCommit[5];
                                        arrayCommit[5] = arrayCommit[6];
                                        arrayCommit[6] = arrayCommit[7];
                                        arrayCommit[7] = arrayCommit[8];
                                        arrayCommit[8] = arrayCommit[9];
                                        arrayCommit[9] = arrayCommit[10];
                                        arrayCommit[10] = arrayCommit[11];
                                        arrayCommit[11] = arrayCommit[12];
                                        arrayCommit[12] = arrayCommit[13];
                                        arrayCommit[13] = arrayCommit[14];
                                        arrayCommit[14] = arrayCommit[15];
                                        arrayCommit[15] = arrayCommit[16];
                                        arrayCommit[16] = arrayCommit[17];
                                        arrayCommit[17] = arrayCommit[18];
                                        arrayCommit[18] = arrayCommit[19];
                                        arrayCommit[19] = arrayCommit[20];
                                        arrayCommit[20] = arrayCommit[21];
                                        arrayCommit[21] = arrayCommit[22];
                                        arrayCommit[22] = arrayCommit[23];
                                        arrayCommit[23] = arrayCommit[24];
                                        arrayCommit[24] = Commit;

                                        arrayConcurrency[0] = arrayConcurrency[1];
                                        arrayConcurrency[1] = arrayConcurrency[2];
                                        arrayConcurrency[2] = arrayConcurrency[3];
                                        arrayConcurrency[3] = arrayConcurrency[4];
                                        arrayConcurrency[4] = arrayConcurrency[5];
                                        arrayConcurrency[5] = arrayConcurrency[6];
                                        arrayConcurrency[6] = arrayConcurrency[7];
                                        arrayConcurrency[7] = arrayConcurrency[8];
                                        arrayConcurrency[8] = arrayConcurrency[9];
                                        arrayConcurrency[9] = arrayConcurrency[10];
                                        arrayConcurrency[10] = arrayConcurrency[11];
                                        arrayConcurrency[11] = arrayConcurrency[12];
                                        arrayConcurrency[12] = arrayConcurrency[13];
                                        arrayConcurrency[13] = arrayConcurrency[14];
                                        arrayConcurrency[14] = arrayConcurrency[15];
                                        arrayConcurrency[15] = arrayConcurrency[16];
                                        arrayConcurrency[16] = arrayConcurrency[17];
                                        arrayConcurrency[17] = arrayConcurrency[18];
                                        arrayConcurrency[18] = arrayConcurrency[19];
                                        arrayConcurrency[19] = arrayConcurrency[20];
                                        arrayConcurrency[20] = arrayConcurrency[21];
                                        arrayConcurrency[21] = arrayConcurrency[22];
                                        arrayConcurrency[22] = arrayConcurrency[23];
                                        arrayConcurrency[23] = arrayConcurrency[24];
                                        arrayConcurrency[24] = Concurrency;

                                        arraySystemIO[0] = arraySystemIO[1];
                                        arraySystemIO[1] = arraySystemIO[2];
                                        arraySystemIO[2] = arraySystemIO[3];
                                        arraySystemIO[3] = arraySystemIO[4];
                                        arraySystemIO[4] = arraySystemIO[5];
                                        arraySystemIO[5] = arraySystemIO[6];
                                        arraySystemIO[6] = arraySystemIO[7];
                                        arraySystemIO[7] = arraySystemIO[8];
                                        arraySystemIO[8] = arraySystemIO[9];
                                        arraySystemIO[9] = arraySystemIO[10];
                                        arraySystemIO[10] = arraySystemIO[11];
                                        arraySystemIO[11] = arraySystemIO[12];
                                        arraySystemIO[12] = arraySystemIO[13];
                                        arraySystemIO[13] = arraySystemIO[14];
                                        arraySystemIO[14] = arraySystemIO[15];
                                        arraySystemIO[15] = arraySystemIO[16];
                                        arraySystemIO[16] = arraySystemIO[17];
                                        arraySystemIO[17] = arraySystemIO[18];
                                        arraySystemIO[18] = arraySystemIO[19];
                                        arraySystemIO[19] = arraySystemIO[20];
                                        arraySystemIO[20] = arraySystemIO[21];
                                        arraySystemIO[21] = arraySystemIO[22];
                                        arraySystemIO[22] = arraySystemIO[23];
                                        arraySystemIO[23] = arraySystemIO[24];
                                        arraySystemIO[24] = SystemIO;

                                        arrayScheduler[0] = arrayScheduler[1];
                                        arrayScheduler[1] = arrayScheduler[2];
                                        arrayScheduler[2] = arrayScheduler[3];
                                        arrayScheduler[3] = arrayScheduler[4];
                                        arrayScheduler[4] = arrayScheduler[5];
                                        arrayScheduler[5] = arrayScheduler[6];
                                        arrayScheduler[6] = arrayScheduler[7];
                                        arrayScheduler[7] = arrayScheduler[8];
                                        arrayScheduler[8] = arrayScheduler[9];
                                        arrayScheduler[9] = arrayScheduler[10];
                                        arrayScheduler[10] = arrayScheduler[11];
                                        arrayScheduler[11] = arrayScheduler[12];
                                        arrayScheduler[12] = arrayScheduler[13];
                                        arrayScheduler[13] = arrayScheduler[14];
                                        arrayScheduler[14] = arrayScheduler[15];
                                        arrayScheduler[15] = arrayScheduler[16];
                                        arrayScheduler[16] = arrayScheduler[17];
                                        arrayScheduler[17] = arrayScheduler[18];
                                        arrayScheduler[18] = arrayScheduler[19];
                                        arrayScheduler[19] = arrayScheduler[20];
                                        arrayScheduler[20] = arrayScheduler[21];
                                        arrayScheduler[21] = arrayScheduler[22];
                                        arrayScheduler[22] = arrayScheduler[23];
                                        arrayScheduler[23] = arrayScheduler[24];
                                        arrayScheduler[24] = Scheduler;

                                        arrayUser[0] = arrayUser[1];
                                        arrayUser[1] = arrayUser[2];
                                        arrayUser[2] = arrayUser[3];
                                        arrayUser[3] = arrayUser[4];
                                        arrayUser[4] = arrayUser[5];
                                        arrayUser[5] = arrayUser[6];
                                        arrayUser[6] = arrayUser[7];
                                        arrayUser[7] = arrayUser[8];
                                        arrayUser[8] = arrayUser[9];
                                        arrayUser[9] = arrayUser[10];
                                        arrayUser[10] = arrayUser[11];
                                        arrayUser[11] = arrayUser[12];
                                        arrayUser[12] = arrayUser[13];
                                        arrayUser[13] = arrayUser[14];
                                        arrayUser[14] = arrayUser[15];
                                        arrayUser[15] = arrayUser[16];
                                        arrayUser[16] = arrayUser[17];
                                        arrayUser[17] = arrayUser[18];
                                        arrayUser[18] = arrayUser[19];
                                        arrayUser[19] = arrayUser[20];
                                        arrayUser[20] = arrayUser[21];
                                        arrayUser[21] = arrayUser[22];
                                        arrayUser[22] = arrayUser[23];
                                        arrayUser[23] = arrayUser[24];
                                        arrayUser[24] = User;

                                        arrayCPU[0] = arrayCPU[1];
                                        arrayCPU[1] = arrayCPU[2];
                                        arrayCPU[2] = arrayCPU[3];
                                        arrayCPU[3] = arrayCPU[4];
                                        arrayCPU[4] = arrayCPU[5];
                                        arrayCPU[5] = arrayCPU[6];
                                        arrayCPU[6] = arrayCPU[7];
                                        arrayCPU[7] = arrayCPU[8];
                                        arrayCPU[8] = arrayCPU[9];
                                        arrayCPU[9] = arrayCPU[10];
                                        arrayCPU[10] = arrayCPU[11];
                                        arrayCPU[11] = arrayCPU[12];
                                        arrayCPU[12] = arrayCPU[13];
                                        arrayCPU[13] = arrayCPU[14];
                                        arrayCPU[14] = arrayCPU[15];
                                        arrayCPU[15] = arrayCPU[16];
                                        arrayCPU[16] = arrayCPU[17];
                                        arrayCPU[17] = arrayCPU[18];
                                        arrayCPU[18] = arrayCPU[19];
                                        arrayCPU[19] = arrayCPU[20];
                                        arrayCPU[20] = arrayCPU[21];
                                        arrayCPU[21] = arrayCPU[22];
                                        arrayCPU[22] = arrayCPU[23];
                                        arrayCPU[23] = arrayCPU[24];
                                        arrayCPU[24] = CPU;


                                        Platform.runLater(() -> {
                                            Date now = new Date();
                                            series1.getData().get(0).setYValue(arrayCPU[0]);
                                            series1.getData().get(1).setYValue(arrayCPU[1]);
                                            series1.getData().get(2).setYValue(arrayCPU[2]);
                                            series1.getData().get(3).setYValue(arrayCPU[3]);
                                            series1.getData().get(4).setYValue(arrayCPU[4]);
                                            series1.getData().get(5).setYValue(arrayCPU[5]);
                                            series1.getData().get(6).setYValue(arrayCPU[6]);
                                            series1.getData().get(7).setYValue(arrayCPU[7]);
                                            series1.getData().get(8).setYValue(arrayCPU[8]);
                                            series1.getData().get(9).setYValue(arrayCPU[9]);
                                            series1.getData().get(10).setYValue(arrayCPU[10]);
                                            series1.getData().get(11).setYValue(arrayCPU[11]);
                                            series1.getData().get(12).setYValue(arrayCPU[12]);
                                            series1.getData().get(13).setYValue(arrayCPU[13]);
                                            series1.getData().get(14).setYValue(arrayCPU[14]);
                                            series1.getData().get(15).setYValue(arrayCPU[15]);
                                            series1.getData().get(16).setYValue(arrayCPU[16]);
                                            series1.getData().get(17).setYValue(arrayCPU[17]);
                                            series1.getData().get(18).setYValue(arrayCPU[18]);
                                            series1.getData().get(19).setYValue(arrayCPU[19]);
                                            series1.getData().get(20).setYValue(arrayCPU[20]);
                                            series1.getData().get(21).setYValue(arrayCPU[21]);
                                            series1.getData().get(22).setYValue(arrayCPU[22]);
                                            series1.getData().get(23).setYValue(arrayCPU[23]);
                                            series1.getData().get(24).setYValue(arrayCPU[24]);

                                            series2.getData().get(0).setYValue(arrayUser[0]);
                                            series2.getData().get(1).setYValue(arrayUser[1]);
                                            series2.getData().get(2).setYValue(arrayUser[2]);
                                            series2.getData().get(3).setYValue(arrayUser[3]);
                                            series2.getData().get(4).setYValue(arrayUser[4]);
                                            series2.getData().get(5).setYValue(arrayUser[5]);
                                            series2.getData().get(6).setYValue(arrayUser[6]);
                                            series2.getData().get(7).setYValue(arrayUser[7]);
                                            series2.getData().get(8).setYValue(arrayUser[8]);
                                            series2.getData().get(9).setYValue(arrayUser[9]);
                                            series2.getData().get(10).setYValue(arrayUser[10]);
                                            series2.getData().get(11).setYValue(arrayUser[11]);
                                            series2.getData().get(12).setYValue(arrayUser[12]);
                                            series2.getData().get(13).setYValue(arrayUser[13]);
                                            series2.getData().get(14).setYValue(arrayUser[14]);
                                            series2.getData().get(15).setYValue(arrayUser[15]);
                                            series2.getData().get(16).setYValue(arrayUser[16]);
                                            series2.getData().get(17).setYValue(arrayUser[17]);
                                            series2.getData().get(18).setYValue(arrayUser[18]);
                                            series2.getData().get(19).setYValue(arrayUser[19]);
                                            series2.getData().get(20).setYValue(arrayUser[20]);
                                            series2.getData().get(21).setYValue(arrayUser[21]);
                                            series2.getData().get(22).setYValue(arrayUser[22]);
                                            series2.getData().get(23).setYValue(arrayUser[23]);
                                            series2.getData().get(24).setYValue(arrayUser[24]);

                                            series3.getData().get(0).setYValue(arrayScheduler[0]);
                                            series3.getData().get(1).setYValue(arrayScheduler[1]);
                                            series3.getData().get(2).setYValue(arrayScheduler[2]);
                                            series3.getData().get(3).setYValue(arrayScheduler[3]);
                                            series3.getData().get(4).setYValue(arrayScheduler[4]);
                                            series3.getData().get(5).setYValue(arrayScheduler[5]);
                                            series3.getData().get(6).setYValue(arrayScheduler[6]);
                                            series3.getData().get(7).setYValue(arrayScheduler[7]);
                                            series3.getData().get(8).setYValue(arrayScheduler[8]);
                                            series3.getData().get(9).setYValue(arrayScheduler[9]);
                                            series3.getData().get(10).setYValue(arrayScheduler[10]);
                                            series3.getData().get(11).setYValue(arrayScheduler[11]);
                                            series3.getData().get(12).setYValue(arrayScheduler[12]);
                                            series3.getData().get(13).setYValue(arrayScheduler[13]);
                                            series3.getData().get(14).setYValue(arrayScheduler[14]);
                                            series3.getData().get(15).setYValue(arrayScheduler[15]);
                                            series3.getData().get(16).setYValue(arrayScheduler[16]);
                                            series3.getData().get(17).setYValue(arrayScheduler[17]);
                                            series3.getData().get(18).setYValue(arrayScheduler[18]);
                                            series3.getData().get(19).setYValue(arrayScheduler[19]);
                                            series3.getData().get(20).setYValue(arrayScheduler[20]);
                                            series3.getData().get(21).setYValue(arrayScheduler[21]);
                                            series3.getData().get(22).setYValue(arrayScheduler[22]);
                                            series3.getData().get(23).setYValue(arrayScheduler[23]);
                                            series3.getData().get(24).setYValue(arrayScheduler[24]);

                                            series4.getData().get(0).setYValue(arraySystemIO[0]);
                                            series4.getData().get(1).setYValue(arraySystemIO[1]);
                                            series4.getData().get(2).setYValue(arraySystemIO[2]);
                                            series4.getData().get(3).setYValue(arraySystemIO[3]);
                                            series4.getData().get(4).setYValue(arraySystemIO[4]);
                                            series4.getData().get(5).setYValue(arraySystemIO[5]);
                                            series4.getData().get(6).setYValue(arraySystemIO[6]);
                                            series4.getData().get(7).setYValue(arraySystemIO[7]);
                                            series4.getData().get(8).setYValue(arraySystemIO[8]);
                                            series4.getData().get(9).setYValue(arraySystemIO[9]);
                                            series4.getData().get(10).setYValue(arraySystemIO[10]);
                                            series4.getData().get(11).setYValue(arraySystemIO[11]);
                                            series4.getData().get(12).setYValue(arraySystemIO[12]);
                                            series4.getData().get(13).setYValue(arraySystemIO[13]);
                                            series4.getData().get(14).setYValue(arraySystemIO[14]);
                                            series4.getData().get(15).setYValue(arraySystemIO[15]);
                                            series4.getData().get(16).setYValue(arraySystemIO[16]);
                                            series4.getData().get(17).setYValue(arraySystemIO[17]);
                                            series4.getData().get(18).setYValue(arraySystemIO[18]);
                                            series4.getData().get(19).setYValue(arraySystemIO[19]);
                                            series4.getData().get(20).setYValue(arraySystemIO[20]);
                                            series4.getData().get(21).setYValue(arraySystemIO[21]);
                                            series4.getData().get(22).setYValue(arraySystemIO[22]);
                                            series4.getData().get(23).setYValue(arraySystemIO[23]);
                                            series4.getData().get(24).setYValue(arraySystemIO[24]);

                                            series5.getData().get(0).setYValue(arrayConcurrency[0]);
                                            series5.getData().get(1).setYValue(arrayConcurrency[1]);
                                            series5.getData().get(2).setYValue(arrayConcurrency[2]);
                                            series5.getData().get(3).setYValue(arrayConcurrency[3]);
                                            series5.getData().get(4).setYValue(arrayConcurrency[4]);
                                            series5.getData().get(5).setYValue(arrayConcurrency[5]);
                                            series5.getData().get(6).setYValue(arrayConcurrency[6]);
                                            series5.getData().get(7).setYValue(arrayConcurrency[7]);
                                            series5.getData().get(8).setYValue(arrayConcurrency[8]);
                                            series5.getData().get(9).setYValue(arrayConcurrency[9]);
                                            series5.getData().get(10).setYValue(arrayConcurrency[10]);
                                            series5.getData().get(11).setYValue(arrayConcurrency[11]);
                                            series5.getData().get(12).setYValue(arrayConcurrency[12]);
                                            series5.getData().get(13).setYValue(arrayConcurrency[13]);
                                            series5.getData().get(14).setYValue(arrayConcurrency[14]);
                                            series5.getData().get(15).setYValue(arrayConcurrency[15]);
                                            series5.getData().get(16).setYValue(arrayConcurrency[16]);
                                            series5.getData().get(17).setYValue(arrayConcurrency[17]);
                                            series5.getData().get(18).setYValue(arrayConcurrency[18]);
                                            series5.getData().get(19).setYValue(arrayConcurrency[19]);
                                            series5.getData().get(20).setYValue(arrayConcurrency[20]);
                                            series5.getData().get(21).setYValue(arrayConcurrency[21]);
                                            series5.getData().get(22).setYValue(arrayConcurrency[22]);
                                            series5.getData().get(23).setYValue(arrayConcurrency[23]);
                                            series5.getData().get(24).setYValue(arrayConcurrency[24]);

                                            series6.getData().get(0).setYValue(arrayCommit[0]);
                                            series6.getData().get(1).setYValue(arrayCommit[1]);
                                            series6.getData().get(2).setYValue(arrayCommit[2]);
                                            series6.getData().get(3).setYValue(arrayCommit[3]);
                                            series6.getData().get(4).setYValue(arrayCommit[4]);
                                            series6.getData().get(5).setYValue(arrayCommit[5]);
                                            series6.getData().get(6).setYValue(arrayCommit[6]);
                                            series6.getData().get(7).setYValue(arrayCommit[7]);
                                            series6.getData().get(8).setYValue(arrayCommit[8]);
                                            series6.getData().get(9).setYValue(arrayCommit[9]);
                                            series6.getData().get(10).setYValue(arrayCommit[10]);
                                            series6.getData().get(11).setYValue(arrayCommit[11]);
                                            series6.getData().get(12).setYValue(arrayCommit[12]);
                                            series6.getData().get(13).setYValue(arrayCommit[13]);
                                            series6.getData().get(14).setYValue(arrayCommit[14]);
                                            series6.getData().get(15).setYValue(arrayCommit[15]);
                                            series6.getData().get(16).setYValue(arrayCommit[16]);
                                            series6.getData().get(17).setYValue(arrayCommit[17]);
                                            series6.getData().get(18).setYValue(arrayCommit[18]);
                                            series6.getData().get(19).setYValue(arrayCommit[19]);
                                            series6.getData().get(20).setYValue(arrayCommit[20]);
                                            series6.getData().get(21).setYValue(arrayCommit[21]);
                                            series6.getData().get(22).setYValue(arrayCommit[22]);
                                            series6.getData().get(23).setYValue(arrayCommit[23]);
                                            series6.getData().get(24).setYValue(arrayCommit[24]);

                                            series7.getData().get(0).setYValue(arrayApplication[0]);
                                            series7.getData().get(1).setYValue(arrayApplication[1]);
                                            series7.getData().get(2).setYValue(arrayApplication[2]);
                                            series7.getData().get(3).setYValue(arrayApplication[3]);
                                            series7.getData().get(4).setYValue(arrayApplication[4]);
                                            series7.getData().get(5).setYValue(arrayApplication[5]);
                                            series7.getData().get(6).setYValue(arrayApplication[6]);
                                            series7.getData().get(7).setYValue(arrayApplication[7]);
                                            series7.getData().get(8).setYValue(arrayApplication[8]);
                                            series7.getData().get(9).setYValue(arrayApplication[9]);
                                            series7.getData().get(10).setYValue(arrayApplication[10]);
                                            series7.getData().get(11).setYValue(arrayApplication[11]);
                                            series7.getData().get(12).setYValue(arrayApplication[12]);
                                            series7.getData().get(13).setYValue(arrayApplication[13]);
                                            series7.getData().get(14).setYValue(arrayApplication[14]);
                                            series7.getData().get(15).setYValue(arrayApplication[15]);
                                            series7.getData().get(16).setYValue(arrayApplication[16]);
                                            series7.getData().get(17).setYValue(arrayApplication[17]);
                                            series7.getData().get(18).setYValue(arrayApplication[18]);
                                            series7.getData().get(19).setYValue(arrayApplication[19]);
                                            series7.getData().get(20).setYValue(arrayApplication[20]);
                                            series7.getData().get(21).setYValue(arrayApplication[21]);
                                            series7.getData().get(22).setYValue(arrayApplication[22]);
                                            series7.getData().get(23).setYValue(arrayApplication[23]);
                                            series7.getData().get(24).setYValue(arrayApplication[24]);

                                            series8.getData().get(0).setYValue(arrayConfiguration[0]);
                                            series8.getData().get(1).setYValue(arrayConfiguration[1]);
                                            series8.getData().get(2).setYValue(arrayConfiguration[2]);
                                            series8.getData().get(3).setYValue(arrayConfiguration[3]);
                                            series8.getData().get(4).setYValue(arrayConfiguration[4]);
                                            series8.getData().get(5).setYValue(arrayConfiguration[5]);
                                            series8.getData().get(6).setYValue(arrayConfiguration[6]);
                                            series8.getData().get(7).setYValue(arrayConfiguration[7]);
                                            series8.getData().get(8).setYValue(arrayConfiguration[8]);
                                            series8.getData().get(9).setYValue(arrayConfiguration[9]);
                                            series8.getData().get(10).setYValue(arrayConfiguration[10]);
                                            series8.getData().get(11).setYValue(arrayConfiguration[11]);
                                            series8.getData().get(12).setYValue(arrayConfiguration[12]);
                                            series8.getData().get(13).setYValue(arrayConfiguration[13]);
                                            series8.getData().get(14).setYValue(arrayConfiguration[14]);
                                            series8.getData().get(15).setYValue(arrayConfiguration[15]);
                                            series8.getData().get(16).setYValue(arrayConfiguration[16]);
                                            series8.getData().get(17).setYValue(arrayConfiguration[17]);
                                            series8.getData().get(18).setYValue(arrayConfiguration[18]);
                                            series8.getData().get(19).setYValue(arrayConfiguration[19]);
                                            series8.getData().get(20).setYValue(arrayConfiguration[20]);
                                            series8.getData().get(21).setYValue(arrayConfiguration[21]);
                                            series8.getData().get(22).setYValue(arrayConfiguration[22]);
                                            series8.getData().get(23).setYValue(arrayConfiguration[23]);
                                            series8.getData().get(24).setYValue(arrayConfiguration[24]);

                                            series9.getData().get(0).setYValue(arrayAdministrative[0]);
                                            series9.getData().get(1).setYValue(arrayAdministrative[1]);
                                            series9.getData().get(2).setYValue(arrayAdministrative[2]);
                                            series9.getData().get(3).setYValue(arrayAdministrative[3]);
                                            series9.getData().get(4).setYValue(arrayAdministrative[4]);
                                            series9.getData().get(5).setYValue(arrayAdministrative[5]);
                                            series9.getData().get(6).setYValue(arrayAdministrative[6]);
                                            series9.getData().get(7).setYValue(arrayAdministrative[7]);
                                            series9.getData().get(8).setYValue(arrayAdministrative[8]);
                                            series9.getData().get(9).setYValue(arrayAdministrative[9]);
                                            series9.getData().get(10).setYValue(arrayAdministrative[10]);
                                            series9.getData().get(11).setYValue(arrayAdministrative[11]);
                                            series9.getData().get(12).setYValue(arrayAdministrative[12]);
                                            series9.getData().get(13).setYValue(arrayAdministrative[13]);
                                            series9.getData().get(14).setYValue(arrayAdministrative[14]);
                                            series9.getData().get(15).setYValue(arrayAdministrative[15]);
                                            series9.getData().get(16).setYValue(arrayAdministrative[16]);
                                            series9.getData().get(17).setYValue(arrayAdministrative[17]);
                                            series9.getData().get(18).setYValue(arrayAdministrative[18]);
                                            series9.getData().get(19).setYValue(arrayAdministrative[19]);
                                            series9.getData().get(20).setYValue(arrayAdministrative[20]);
                                            series9.getData().get(21).setYValue(arrayAdministrative[21]);
                                            series9.getData().get(22).setYValue(arrayAdministrative[22]);
                                            series9.getData().get(23).setYValue(arrayAdministrative[23]);
                                            series9.getData().get(24).setYValue(arrayAdministrative[24]);

                                            series10.getData().get(0).setYValue(arrayNetwork[0]);
                                            series10.getData().get(1).setYValue(arrayNetwork[1]);
                                            series10.getData().get(2).setYValue(arrayNetwork[2]);
                                            series10.getData().get(3).setYValue(arrayNetwork[3]);
                                            series10.getData().get(4).setYValue(arrayNetwork[4]);
                                            series10.getData().get(5).setYValue(arrayNetwork[5]);
                                            series10.getData().get(6).setYValue(arrayNetwork[6]);
                                            series10.getData().get(7).setYValue(arrayNetwork[7]);
                                            series10.getData().get(8).setYValue(arrayNetwork[8]);
                                            series10.getData().get(9).setYValue(arrayNetwork[9]);
                                            series10.getData().get(10).setYValue(arrayNetwork[10]);
                                            series10.getData().get(11).setYValue(arrayNetwork[11]);
                                            series10.getData().get(12).setYValue(arrayNetwork[12]);
                                            series10.getData().get(13).setYValue(arrayNetwork[13]);
                                            series10.getData().get(14).setYValue(arrayNetwork[14]);
                                            series10.getData().get(15).setYValue(arrayNetwork[15]);
                                            series10.getData().get(16).setYValue(arrayNetwork[16]);
                                            series10.getData().get(17).setYValue(arrayNetwork[17]);
                                            series10.getData().get(18).setYValue(arrayNetwork[18]);
                                            series10.getData().get(19).setYValue(arrayNetwork[19]);
                                            series10.getData().get(20).setYValue(arrayNetwork[20]);
                                            series10.getData().get(21).setYValue(arrayNetwork[21]);
                                            series10.getData().get(22).setYValue(arrayNetwork[22]);
                                            series10.getData().get(23).setYValue(arrayNetwork[23]);
                                            series10.getData().get(24).setYValue(arrayNetwork[24]);

                                            series11.getData().get(0).setYValue(arrayQueueing[0]);
                                            series11.getData().get(1).setYValue(arrayQueueing[1]);
                                            series11.getData().get(2).setYValue(arrayQueueing[2]);
                                            series11.getData().get(3).setYValue(arrayQueueing[3]);
                                            series11.getData().get(4).setYValue(arrayQueueing[4]);
                                            series11.getData().get(5).setYValue(arrayQueueing[5]);
                                            series11.getData().get(6).setYValue(arrayQueueing[6]);
                                            series11.getData().get(7).setYValue(arrayQueueing[7]);
                                            series11.getData().get(8).setYValue(arrayQueueing[8]);
                                            series11.getData().get(9).setYValue(arrayQueueing[9]);
                                            series11.getData().get(10).setYValue(arrayQueueing[10]);
                                            series11.getData().get(11).setYValue(arrayQueueing[11]);
                                            series11.getData().get(12).setYValue(arrayQueueing[12]);
                                            series11.getData().get(13).setYValue(arrayQueueing[13]);
                                            series11.getData().get(14).setYValue(arrayQueueing[14]);
                                            series11.getData().get(15).setYValue(arrayQueueing[15]);
                                            series11.getData().get(16).setYValue(arrayQueueing[16]);
                                            series11.getData().get(17).setYValue(arrayQueueing[17]);
                                            series11.getData().get(18).setYValue(arrayQueueing[18]);
                                            series11.getData().get(19).setYValue(arrayQueueing[19]);
                                            series11.getData().get(20).setYValue(arrayQueueing[20]);
                                            series11.getData().get(21).setYValue(arrayQueueing[21]);
                                            series11.getData().get(22).setYValue(arrayQueueing[22]);
                                            series11.getData().get(23).setYValue(arrayQueueing[23]);
                                            series11.getData().get(24).setYValue(arrayQueueing[24]);

                                            series12.getData().get(0).setYValue(arrayCluster[0]);
                                            series12.getData().get(1).setYValue(arrayCluster[1]);
                                            series12.getData().get(2).setYValue(arrayCluster[2]);
                                            series12.getData().get(3).setYValue(arrayCluster[3]);
                                            series12.getData().get(4).setYValue(arrayCluster[4]);
                                            series12.getData().get(5).setYValue(arrayCluster[5]);
                                            series12.getData().get(6).setYValue(arrayCluster[6]);
                                            series12.getData().get(7).setYValue(arrayCluster[7]);
                                            series12.getData().get(8).setYValue(arrayCluster[8]);
                                            series12.getData().get(9).setYValue(arrayCluster[9]);
                                            series12.getData().get(10).setYValue(arrayCluster[10]);
                                            series12.getData().get(11).setYValue(arrayCluster[11]);
                                            series12.getData().get(12).setYValue(arrayCluster[12]);
                                            series12.getData().get(13).setYValue(arrayCluster[13]);
                                            series12.getData().get(14).setYValue(arrayCluster[14]);
                                            series12.getData().get(15).setYValue(arrayCluster[15]);
                                            series12.getData().get(16).setYValue(arrayCluster[16]);
                                            series12.getData().get(17).setYValue(arrayCluster[17]);
                                            series12.getData().get(18).setYValue(arrayCluster[18]);
                                            series12.getData().get(19).setYValue(arrayCluster[19]);
                                            series12.getData().get(20).setYValue(arrayCluster[20]);
                                            series12.getData().get(21).setYValue(arrayCluster[21]);
                                            series12.getData().get(22).setYValue(arrayCluster[22]);
                                            series12.getData().get(23).setYValue(arrayCluster[23]);
                                            series12.getData().get(24).setYValue(arrayCluster[24]);

                                            series13.getData().get(0).setYValue(arrayOther[0]);
                                            series13.getData().get(1).setYValue(arrayOther[1]);
                                            series13.getData().get(2).setYValue(arrayOther[2]);
                                            series13.getData().get(3).setYValue(arrayOther[3]);
                                            series13.getData().get(4).setYValue(arrayOther[4]);
                                            series13.getData().get(5).setYValue(arrayOther[5]);
                                            series13.getData().get(6).setYValue(arrayOther[6]);
                                            series13.getData().get(7).setYValue(arrayOther[7]);
                                            series13.getData().get(8).setYValue(arrayOther[8]);
                                            series13.getData().get(9).setYValue(arrayOther[9]);
                                            series13.getData().get(10).setYValue(arrayOther[10]);
                                            series13.getData().get(11).setYValue(arrayOther[11]);
                                            series13.getData().get(12).setYValue(arrayOther[12]);
                                            series13.getData().get(13).setYValue(arrayOther[13]);
                                            series13.getData().get(14).setYValue(arrayOther[14]);
                                            series13.getData().get(15).setYValue(arrayOther[15]);
                                            series13.getData().get(16).setYValue(arrayOther[16]);
                                            series13.getData().get(17).setYValue(arrayOther[17]);
                                            series13.getData().get(18).setYValue(arrayOther[18]);
                                            series13.getData().get(19).setYValue(arrayOther[19]);
                                            series13.getData().get(20).setYValue(arrayOther[20]);
                                            series13.getData().get(21).setYValue(arrayOther[21]);
                                            series13.getData().get(22).setYValue(arrayOther[22]);
                                            series13.getData().get(23).setYValue(arrayOther[23]);
                                            series13.getData().get(24).setYValue(arrayOther[24]);
                                        });
                                    }
                                    if (i <= 24) {
                                        i++;
                                    }
                                }
                            }
                        }catch(Exception e){
                            Platform.runLater(() -> {
                                connectionStatus.setText("Offline");connectionStatus.setStyle("-fx-font-weight: BOLD;-fx-text-fill: RED");
                            });
                        }


                    }else{
                        Platform.runLater(() -> {
                            connectionStatus.setText("Offline");connectionStatus.setStyle("-fx-font-weight: BOLD;-fx-text-fill: RED");
                        });
                    }

                    return null;
                }
            };
            Thread th = new Thread(task);
            th.setDaemon(true);
            th.start();
            chartAndInfoGridPane.add(areaChart, 0, 0);
            chartAndInfoGridPane.add(chartActualInfoAboutValuesGroup, 1, 0);
            chartAndInfoGridPane.setMaxHeight(200);chartAndInfoGridPane.setMaxWidth(stageWidth.widthProperty().get()/4.08);chartAndInfoGridPane.setStyle("-fx-border-width: 1; -fx-border-color: WHITE; -fx-border-style: DASHED;-fx-background-color: WHITE;");
            chartsGroup.getChildren().addAll(chartAndInfoGridPane, currentLoad, connectionStatus);
        }
        return gridBody;
    }


    /**This method is responsible for exiting process - checks if app has unsaved changes - if yes, then display alert*/
    @Override
    public void stop(){
        System.out.println("Stage is closing");
        if(fileHasUnsavedChanges) {
            Alert exitAlert = new Alert(Alert.AlertType.CONFIRMATION);
            exitAlert.setTitle("Exit");
            exitAlert.setHeaderText(null);
            exitAlert.setContentText("Would you like to save current config?\n\nFile path:\n" + currentPathToFile);
            exitAlert.getDialogPane().getButtonTypes().clear();
            exitAlert.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
            exitAlert.showAndWait();
            if (exitAlert.getResult() == ButtonType.YES) {
                connections.exportConnectionListToFileAs(currentPathToFile);
            }
        }
    }
}