# java-chat-app
This is a one-on-one Java chat application that allows clients to send and receive messages and files through a server.

### Authors: Gabriel Minamedez, Carlo Santos

## Project Directory Guide
1. `Server.java` is the Java file for the lone server of the chat application.
2. **Client1** is the directory for the first client.
- `Client.java` is the Java file for the particular client. It shall have the same executable code as the file in the other Client directory.
3. **Client2** is the directory for the second client.
- `Client.java` is the Java file for the particular client. It shall have the same executable code as the file in the other Client directory.

## Instructions for local machine use:
1. Once you have downloaded this repository, `cd` to the folder of this project.
2. While in the folder, enter `javac Server.java` in the command line interface (CLI) to compile the server file.
3. Now, enter `java Server` in the CLI. The set IP address (**localhost**) and port number (**8080**) should now be displayed on the CLI.
4. While the CLI in which the server is still running, open two new CLI's. The two new CLI's should `cd` until the directories **Client1** and Client2**, respectively, are met.
5. For both CLI's, enter `javac Client.java` to compile the two client files.
6. Again, for both CLI's, enter `java Client` to run the two client files. In your desktop, two GUI window should now appear.
7. The GUI windows will ask for three things: **IP address**, **port number**, and **username**. Input accordingly to the details indicated in the server's CLI. Username at your own discretion, as long as it's not empty. The GUI will keep asking if: (1) The inputted IP address and port number are wrong, or (2) There are empty areas.
8. If all details are complete and correct, the application automatically redirects you to the chatroom. You may now start using **De La Salle Usap**!

## Project Notes
1. This is in partial fulfillment of the course **Introduction to Computer Networks (CSNETWK)**.