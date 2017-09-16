Yet Another Database API
-----

Usage
-----
- Download this repository and compile the source code
- (Optional) Then maven shade the Jar you compiled in!

Connect to a MySQL database via HikariCP (Default):
```JAVA
Database database = Database.options()
    .set("idleTimeout", "600000") // Set Hikari configurations
    .identifyAs("name") // The name of the database
    .hostname("127.0.0.1", 3306) // The address / port of the MySQL database
    .auth("root", "password") // Authenticate yourself
    .build(); // Build and retrieve the Database instance
```
...More connection types coming soon!

Send a query to the MySQL database:
```JAVA
Database database = DatabaseManager.getDatabase("name"); // Get the Database instance you created
Query query = new Query("SELECT * FROM `users` WHERE `id`=?"); // Create a MySQL Query statement
query.setString(id.toString()); // Escape the UUID
database.send(query); // Send the Query
```
...or send an Update:
```JAVA
Database database = DatabaseManager.getDatabase("name"); // Get the Database instance you created
Update update = new Update("UPDATE `users` SET `coins`=? WHERE `id`=?"); // Create a MySQL Update statement
update.setInteger(10200); // Escape the amount of coins
update.setString(id.toString()); // Escape the UUID
database.send(update); // Send the update
```
...or prepare your own statement:
```JAVA
Database database = DatabaseManager.getDatabase("name"); // Get the Database instance you created
Statement statement = new Statement("INSERT INTO `users`(`id`, `coins`) VALUES(?, ?)");
statement.setString(...);
database.prepare(statement);
```

That's it! I'm working on adding more features.
