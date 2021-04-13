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
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

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
        menuTools.getItems().addAll(menuToolsBackgroundLight, menuToolsBackgroundDark);
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