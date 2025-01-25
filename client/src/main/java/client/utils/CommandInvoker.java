package client.utils;

import client.scenes.HomeScreenCtrl;
import javafx.scene.control.Alert;

import java.util.ResourceBundle;
import java.util.Stack;

public class CommandInvoker {
    private static CommandInvoker instance;
    private final Stack<Command> commandHistory = new Stack<>();

    /**
     * Default constructor for the CommandInvoker class
     */
    public CommandInvoker() {}

    /**
     * Ensures that only one instance of the class is created (Singleton)
     * @return CommandInvoker instance - used to manage command history
     */
    public static CommandInvoker getInstance() {
        if (instance == null) {
            instance = new CommandInvoker();
        }
        return instance;
    }

    /**
     *  Executes the given command and stores it in the command history stack
     * @param command - the command to be executed and recorded in the history stack
     *
     */
    public void executeCommand(Command command) {
        command.execute();
        commandHistory.push(command);
    }

    /**
     * Checks if the history of commands stack is empty or not.
     * If it is the latter, undoes the last action
     */
    public void undoLastCommand() {
        ResourceBundle bundle = HomeScreenCtrl.getBundle();
        if (!commandHistory.isEmpty()) {
            Command command = commandHistory.pop();
            command.undo();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(bundle.getString("UndoSuccessful"));
            alert.setHeaderText(bundle.getString("UndoSuccessfulM"));
            alert.setContentText(command.toString());
            alert.showAndWait();
        }else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(bundle.getString("UndoFail"));
            alert.setHeaderText(bundle.getString("UndoFailH"));
            alert.setContentText(bundle.getString("UndoFailM"));
            alert.showAndWait();
        }
    }
}
