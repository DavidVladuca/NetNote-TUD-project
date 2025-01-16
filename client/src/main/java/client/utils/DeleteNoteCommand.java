package client.utils;

import client.scenes.HomeScreenCtrl;
import commons.Note;

public class DeleteNoteCommand implements Command {
    private final HomeScreenCtrl controller;
    private final Note note;
    private boolean executedSuccessfully = false;

    /**
     * The constructor for the DeleteNoteCommand class
     * @param controller - the HomeScreenController provided
     * @param note - Note provided
     */
    public DeleteNoteCommand(HomeScreenCtrl controller, Note note) {
        this.controller = controller;
        this.note = note;
    }

    /**
     * Executes the deleteCommand from the HomeScreenCtrl
     */
    @Override
    public void execute() {
        try {
            // Remove the note from the UI and server
            controller.deleteCommand(note.getNoteId());
            executedSuccessfully = true;
        } catch (Exception e) {
            System.err.println("Error during execute: " + e.getMessage());
            executedSuccessfully = false;
        }
    }

    /**
     * Restores the deleted note - Undoes operation
     */
    @Override
    public void undo() {
        if (executedSuccessfully) {
            try {
                // Re-add the deleted note
                controller.addCommand(note);
            } catch (Exception e) {
                System.err.println("Error during undo: " + e.getMessage());
            }
        } else {
            System.err.println("Undo was not successful");
        }
    }
}
