package client.utils;

import client.scenes.HomeScreenCtrl;
import commons.Note;

public class EditBodyCommand implements Command {
    private final Note note;
    private final String oldBody;
    private final String newBody;
    public final HomeScreenCtrl controller;

    /**
     * Constructor for the EditBodyCommand class
     * @param note - note provided
     * @param oldBody - the old body represented as a String
     * @param newBody - the new body represented as a String
     * @param controller - the HomeScreenCtrl
     *
     */
    public EditBodyCommand(Note note, String oldBody, String newBody, HomeScreenCtrl controller) {
        this.note = note;
        this.oldBody = oldBody; // Store the old body
        this.newBody = newBody; // Store the new body
        this.controller = controller;
    }

    /**
     * Sets the body of the note to the new one
     */
    @Override
    public void execute() {
        note.setBody(newBody);
    }

    /**
     * Restores the body of the note to the previously saved one
     */
    @Override
    public void undo() {
        note.setBody(oldBody);
        controller.syncNoteWithServer(note);
    }
}
