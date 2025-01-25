package client.utils;

import client.scenes.HomeScreenCtrl;
import commons.Note;
import javafx.application.Platform;

import java.util.Stack;

public class EditBodyCommand implements Command {
    private final Note note;
    private final Stack<String> oldBodies = new Stack<>();
    private final String newBody;
    public final HomeScreenCtrl controller;

    /**
     * Constructor for the EditBodyCommand class
     * @param note - (Note) note provided
     * @param oldBody - (Stack<String>) the old titles that were before execution
     * @param newBody - (String) the new body after execution
     * @param controller - (HomeScreenCtrl) the HomeScreenCtrl
     *
     */
    public EditBodyCommand(Note note, String oldBody, String newBody, HomeScreenCtrl controller) {
        this.note = note;
        this.oldBodies.push(oldBody);
        this.newBody = newBody;
        this.controller = controller;
    }

    /**
     * Sets the body of the note to the new one
     */
    @Override
    public void execute() {
        note.setBody(newBody);
        controller.syncNoteWithServer(note);
    }

    /**
     * Restores the body of the note to the previously saved one
     */
    @Override
    public void undo() {
        note.setBody(oldBodies.pop());
        controller.syncNoteWithServer(note);
    }
}
