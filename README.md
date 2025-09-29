# Short context about this project
This project was developed as part of the TU Delft **Collaborative Software Engineering Project (CSEP)** course by a team of 5 over 10 weeks. We built **NetNote**, a distributed JavaFX note-taking application with a Spring Boot backend. We had weekly meetings to discuss milestones and get feedback from a teaching assistant (TA), and we overcame numerous challenges. In the end, we delivered a functional and aesthetically pleasing product on time. The following sections are from the `README` we built for the course.


# CSEP Template Project

To run the project from the command line, you either need to have [Maven](https://maven.apache.org/install.html) installed on your local system (`mvn`) or you need to use the Maven wrapper (`mvnw`). You can then execute

	mvn clean install

to package and install the artifacts for the three subprojects. Afterwards, you can run ...

	cd server
	mvn spring-boot:run

to start the server or ...

	cd client
	mvn javafx:run

to run the client. Please note that the server needs to be running, before you can start the client.

<img width="1002" height="634" alt="image" src="https://github.com/user-attachments/assets/a91c26ea-98ee-4abe-b4f5-2b8cb9d192e8" />


# Tutorial for the TAs :-)

You start the application as described above. When the application starts app, you can start by creating a new note, with the + button in the bottom left corner.

Then you can display all the available keyboard shortcuts when clicking on the ? button in the top left corner. Then, you can follow the instructions below.

PLEASE NOTE - shortcuts are not available if you are focused in a text field, you can reset focus by clicking ESC - this will focus to the search field, and then ESC again, this will reset focus on the list view

## Let's start with the basic requirements:

•  **To host my notes on a server, so I can use my notes from multiple clients.** - The notes are hosted on the localhost:8080 server, on the api/notes/ endpoint to be precise

•  **To see all existing notes on the server, so I can browse the available information.** - these notes are available on the list view - the black bar on the left, where new notes will appear when creating a note, the notes are persistent throughout restarts

•  **To create new notes, so I can persist information for later use.** - you can add a new note by clicking on the '+' button in the bottom left corner, the note titles are always unique for a new note with the (1), (2), ... added to the title if the note with that title already exists

•  **To add titles to notes, so I can organize my information.** - You can edit the title of the note by clicking on the title field in the middle of the screen. The title only gets saved when you lose focus of this title field

•  **To change note titles, so I can keep title and content in sync.** - Again, the same instructions as before, you can change the title by clicking on the title field in the middle of the interface.

•  **To delete notes, so I can remove unneeded information.** - You can easily delete a note with the '-' button in the bottom left corner, or with the keyboard shortcut, the note gets deleted from the database

•  **To sync every change in my notes automatically with the server, so I do not need to manually save.** - the titles are synced every time the user loses focus of the title field and if of course something changes, the content is synced only every second if something is changed to prevent server overload

•  **To write note contents as free text, so I am not hindered by any structural requirements.** - You can write the content of the note in the content field under the title field

•  **To manually refresh my client view, so I see new information on the server.** - you can refresh with the F5 shortcut, or with the refresh button in the bottom left corner of the application

•  **To search for keywords, so I can find notes with matching titles or content** - We implemented two types of searching - search within a collection and a search within a note. Let's start with the main - **search within the collection** (located at the top-center of the app). When you search some text, the results automatically show up. For the **note search** (located on top of the note view itself) you just type letters and it will automatically highlight matches, you can move between the matches with the arrows, next to the search bar

•  **To be able to use Markdown formatting in my notes, so I can write structured notes.** - The markdown is automatically shown when editing the title/content of the note. Implemented with the commonmark library.

•  **To see a rendered version of the Markdown note, so I can see a formatted version of my note.** - As described above.

•  **To see an automatic update of the rendered view, so I see my changes reflected in real time.** - Again, all described in the markdown part.

•  **To be prevented from using a duplicate note title, so note content is clear.**
(Note titles should be unique per collection, like a filename has to be unique in a folder.) - When you edit a title, and this title already exists in a collection, you will receive a warning that this title already exists and the title is restored back to the previous one.

## Multi-collection

## Embedded files

•	**To embed files into notes (e.g images), so I can add relevant media to my descriptions.** - Files can be uploaded to a note using the upload button on the bottom of the application. When a file is selected, it is displayed in the listview and uploaded to the server

•	**To rename file names, so I can give them more descriptive names without re-upload.** - On double-click of files in the listview, they can be renamed. Duplicate/empty file names and altered file extensions are not accepted.

•	**To delete files, so I can remove irrelevant information.** A file can be deleted by selecting a file on the listview and clicking the delete icon (minus symbol by listview)

•	**To delete all embedded files when I delete notes, so the server does not accumulate irrelevant files.** When a note is deleted, files are deleted also, as reflected when accessing the database.

•	**To refer to embedded images in my Markdown, so I can see images in my preview.** To display embedded images, the user must write ![alt text](filename.jpeg) as an example

•	**To download files when I click on them, so I can use the files locally on my computer.** By clicking the download button and typing in the file name, the file is successfully downloaded.

## Interconnected content

•	**To use tags in the form of "#foo" in my notes, so I can organize information and make it easier to find.** - By typing in the main text box using this format, the Markdown Display will process the text as a tag (providing a visual indicator) and assign it as a tag for the current note. The tag will also be added to the database. Deleting or editing a tag will also delete or update the tag in the database.

•	**To use [[other note]] to refer to other notes in the same collection by title, so I can link related notes.** - By typing in the main text box using this format, the Markdown Display will process the text as a link to another note (providing a visual indicator). 

•	**To see a visual difference in the preview if a referenced note does not exist, so I can spot typos.** - If the referenced note is not present in the current list of notes in the collection (filtered by tag or search function), the Markdown Display will visually highlight the issue, making it clear that the reference is invalid. 

•	**To click on the link to another note, so it is easy to switch to the other note.** - If the link to the referenced note is valid, clicking on it will switch the current view to the referenced note.  

•	**To click on a tag, so I can filter all notes to those who have the tag.** -Clicking on a tag in the Markdown Display will add it to a filtering box (located above the note's title box) and filter the notes so only those with the selected tag are visible.

•	**To select multiple tags, so I can filter notes with multiple criteria.** - Users can add multiple tags to the filtering box, which will filter the notes to show only those with **all** the selected tags. Tags in the filtering box are displayed as choice boxes. Users can replace a tag in the filtering box with another available tag, dynamically adjusting the filter. If no notes match the selected combination of tags, an alert will appear, and the change will be reverted.

•	**To clear all selected tags, so I can easily go back to seeing all notes.** - A "Clear Tags" button is provided on the right side of the filtering box. Clicking this button will remove all tags from the filtering box, cancel all filters, and display all notes.

## Automated change synchronization

•  We haven't implemented this

## Live Language Switch

•	**To see a language indicator, so I know which language is currently configured in my client at a glance.** - The language indicator is the flag in the combo box in the top right corner of the application

•	**To see a flag icon as the language indicator, so I do not have to read additional text.** - As described above

•	**To see all available languages through clicking the indicator, so I can find my preferred one easily.** - When you click on the combo box, all the available languages, described with flags, will show up. From the top - we implemented English, Spanish, Dutch, and Gibberish :D

•	**To persist my language choice through restarts, so I do not have to pick a language each time** - the language choice is persistent with a simple text file and Reader/Writer

The languages were implemented with Java internalization

In addition to labels, we also translated all alerts, and pop ups that show up in certain situations.

