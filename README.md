How to install and run the app

1. Open on an IDE, such as intellij, the one I used
2. Load the "Assignment 1 Distributed" folder into the projects folder of the IDE
Inside this folder is a couple of things that might need attending too, depending on your setup
3. First, a dump of the database files I used. I used MySQL, and as such, this folder comes with an adapter to MySQL located in the lib folder. This should be connected to the project, but if not, add both files to the list of module dependacies in your project
4. Import the schema dump into your database, this should result in a database that is Identical to mine.
5. Check the users table, if no data exists, make a username and password directly in SQL, this app does not yet have a registration feature.
6. check the connection between the database and the program. The username, password, and potentially port in this program may need adjusting according to what they are set to in your MySQL .
8. Run the server file first, client will error if no server is detected.
9. Run an instance of the client, the two should connect
10. If both programs are running correctly, the client should be asking for a password. Procceed as per the tests
