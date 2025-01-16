package client.utils;

public interface Command {
    /**
     * Executes the command object
     */
    void execute();

    /**
     * Reverts the last execute() action
     */
    void undo();
}
