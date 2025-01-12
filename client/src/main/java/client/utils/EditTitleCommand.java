package client.utils;

import client.HomeScreen;
import client.scenes.HomeScreenCtrl;
import commons.Note;

import java.util.Stack;

public class EditTitleCommand implements Command {
    private final Note note;
    private final Stack<String> oldTitles = new Stack<>();
    private final String newTitle;
    private final HomeScreenCtrl controller;

    public EditTitleCommand(Note note,String oldTitle, String newTitle, HomeScreenCtrl controller) {
        this.note = note;
        this.oldTitles.push(oldTitle);
        this.newTitle = newTitle;
        this.controller = controller;
    }

    /**
     * Sets the title of the note to the new one
     */
    @Override
    public void execute() {
        note.setTitle(newTitle);
        controller.syncNoteWithServer(note);
    }

    /**
     * Restores the title of the note to the previously saved one
     */
    @Override
    public void undo() {
        note.setTitle(oldTitles.pop());
        controller.syncNoteWithServer(note);

    }
}
