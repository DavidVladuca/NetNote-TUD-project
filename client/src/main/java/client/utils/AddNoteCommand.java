package client.utils;

import client.scenes.HomeScreenCtrl;
import commons.Note;

public class AddNoteCommand implements Command {
    private final HomeScreenCtrl controller;
    private final Note note;
    private boolean executedSuccessfully = false;


    public AddNoteCommand(HomeScreenCtrl controller, Note note) {
        this.controller = controller;
        this.note = note;
    }

    /**
     * Executes the addCommand from HomeScreenCtrl
     */
    @Override
    public void execute() {
        try {
            // Generate a unique title and add the note
            controller.addCommand(note);
            executedSuccessfully = true; // Mark successful execution
        } catch (Exception e) {
            System.err.println("Error during execute: " + e.getMessage());
        }
    }

    /**
     * Deletes the added note - Undoes operation
     */
        @Override
    public void undo() {
            if (executedSuccessfully) {
                try {
                    // Delete the added note
                    controller.deleteCommand(note.getNoteId());
                    System.out.println(note.getNoteId()+"________");
                } catch (Exception e) {
                    System.err.println("Error during undo: " + e.getMessage());
                }
            } else {
                System.err.println("Undo was not successful");
            }
    }
}
