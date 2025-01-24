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

•  **To sync every change in my notes automatically with the server, so I do not need to manually save.** - the titles are synced every time the user loses focus of the title field and if of course something changes, the content is synced every 5 seconds

•  **To write note contents as free text, so I am not hindered by any structural requirements.** - You can write the content of the note in the content field under the title field

•  **To manually refresh my client view, so I see new information on the server.** - you can refresh with the F5 shortcut, or with the refresh button in the bottom left corner of the application

•  **To search for keywords, so I can find notes with matching titles or content** - We implemented two types of searching - search within a collection and a search within a note. Let's start with the main - **search within the collection**. When you search something, the results automatically show up. For the **note search** you just type letters and it will automatically highlight matches, you can move between the matches with the arrows, next to the search bar

•  **To be able to use Markdown formatting in my notes, so I can write structured notes.** - The markdown is automatically shown when editing the title/content of the note. Implemented with the commonmark library.

•  **To see a rendered version of the Markdown note, so I can see a formatted version of my note.** - As described above.

•  **To see an automatic update of the rendered view, so I see my changes reflected in real time.** - Again, all described in the markdown part.

•  T**o be prevented from using a duplicate note title, so note content is clear.**
(Note titles should be unique per collection, like a filename has to be unique in a folder.) - When you edit a title, and this title already exists in a collection, you will receive a warning that this title already exists and the title is restored back to the previous one.

## Multi-collection

## Embedded files

## Interconnected content

## Automated change synchronization

•  We haven't implemented this

## Live Language Switch

•	**To see a language indicator, so I know which language is currently configured in my client at a glance.** - The language indicator is the flag in the combo box in the top right corner of the application

•	**To see a flag icon as the language indicator, so I do not have to read additional text.** - As described above

•	**To see all available languages through clicking the indicator, so I can find my preferred one easily.** - When you click on the combo box, all the available languages, described with flags, will show up. From the top - we implemented English, Spanish, Dutch, and Gibberish :D

•	**To persist my language choice through restarts, so I do not have to pick a language each time** - the language choice is persistent with a simple text file and Reader/Writer

The languages were implemented with Java internalization

In addition to labels, we also translated all alerts, and pop ups that show up in certain situations.

