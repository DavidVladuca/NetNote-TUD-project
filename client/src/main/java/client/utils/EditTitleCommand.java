package client.utils;

import client.HomeScreen;
import client.scenes.HomeScreenCtrl;
import commons.Note;

public class EditTitleCommand implements Command {
    private final Note note;
    private final String oldTitle;
    private final String newTitle;
    private final HomeScreenCtrl controller;

    public EditTitleCommand(Note note, String newTitle, HomeScreenCtrl controller) {
        this.note = note;
        this.oldTitle = note.getTitle(); // Store the old title
        this.newTitle = newTitle;       // Store the new title
        this.controller = controller;
    }

    /**
     * Sets the title of the note to the new one
     */
    @Override
    public void execute() {
        note.setTitle(newTitle);
    }

    /**
     * Restores the title of the note to the previously saved one
     */
    @Override
    public void undo() {
        note.setTitle(oldTitle);
        controller.syncNoteWithServer(note);
        System.out.println("------------------- " + oldTitle + " ------------------");
    }
}
