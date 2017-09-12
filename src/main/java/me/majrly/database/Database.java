package me.majrly.database;

import me.majrly.database.params.Parameter;
import me.majrly.database.statements.Query;
import me.majrly.database.statements.Statement;

import java.sql.*;
import java.util.Map;
import java.util.Optional;

/**
 * Official Database API
 *
 * @author Majrly
 * @since 1.0.0
 */
public class Database {

    // Variables
    private String name;
    private String hostname;
    private String username;
    private String password;
    private String database;

    private int port = 3306;
    private int timeout = 60;

    private Connection connection;

    /**
     * Official Database API
     *
     * @param name     The name of this database instance
     * @param hostname The ip to use when connecting
     * @param username The username to authenticate as
     * @param password The password to authenticate yourself
     * @param database The name of the database to switch to
     * @param port     The port to use when connecting
     * @since 1.0.0
     */
    public Database(String name, String hostname, String username, String password, String database, int port) {
        this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.database = database;
        this.port = port;
    }

    /**
     * Official Database API
     *
     * @param name     The name of this database instance
     * @param hostname The ip to use when connecting
     * @param username The username to authenticate as
     * @param password The password to authenticate yourself
     * @param database The name of the database to switch to
     * @param port     The port to use when connecting
     * @param timeout  The sql connection timeout in seconds
     * @since 1.0.0
     */
    public Database(String name, String hostname, String username, String password, String database, int port, int timeout) {
        this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.database = database;
        this.port = port;
    }

    /**
     * Get the database options class
     *
     * @return A reference to {@link DatabaseOptions}
     * @since 1.0.0
     */
    public static DatabaseOptions options() {
        return new DatabaseOptions();
    }

    /**
     * Connects to the database
     *
     * @return Whether it connected or not
     * @since 1.0.0
     */
    public boolean connect() {
        try {
            if (connection != null && !connection.isClosed()) {
                return true;
            }
            connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database + "?autoReconnect=true&connectTimeout=" + timeout, username, password);
            return true;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    /**
     * Sends a query to the database
     *
     * @param statement The statement to send the database
     * @return Either the int of an update, or the ResultSet of a query
     * @since 1.0.0
     */
    public Optional<?> send(Statement statement) {
        try {
            Optional<PreparedStatement> preparedStatement = prepare(statement);
            if (!preparedStatement.isPresent()) return Optional.empty();
            if (statement instanceof Query) {
                return Optional.of(preparedStatement.get().executeQuery());
            } else {
                return Optional.of(preparedStatement.get().executeUpdate());
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Prepare a statement
     *
     * @param statement The statement with parameters you wish to prepare
     * @return The optional value of {@link PreparedStatement}
     * @since 1.0.0
     */
    public Optional<PreparedStatement> prepare(Statement statement) {
        try {
            if (connect()) return Optional.empty();
            PreparedStatement preparedStatement = connection.prepareStatement(statement.getSQL());
            for (Map.Entry<Integer, Parameter> parameter : statement.getParameters().entrySet()) {
                switch (parameter.getValue().getType()) {
                    case STRING:
                        preparedStatement.setString(parameter.getKey(), (String) parameter.getValue().getData());
                    case INTEGER:
                        preparedStatement.setInt(parameter.getKey(), (Integer) parameter.getValue().getData());
                    case DOUBLE:
                        preparedStatement.setDouble(parameter.getKey(), (Double) parameter.getValue().getData());
                    case LONG:
                        preparedStatement.setLong(parameter.getKey(), (Long) parameter.getValue().getData());
                    case BLOB:
                        preparedStatement.setBlob(parameter.getKey(), (Blob) parameter.getValue().getData());
                    case FLOAT:
                        preparedStatement.setFloat(parameter.getKey(), (Float) parameter.getValue().getData());
                    case BOOLEAN:
                        preparedStatement.setBoolean(parameter.getKey(), (Boolean) parameter.getValue().getData());
                    case DATE:
                        preparedStatement.setDate(parameter.getKey(), (Date) parameter.getValue().getData());
                }
            }
            return Optional.of(preparedStatement);
        } catch (SQLException exception) {
            exception.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Prepare a statement
     *
     * @param sql The statement you want to prepare
     * @return The optional value of {@link PreparedStatement}
     * @since 1.0.0
     */
    public Optional<PreparedStatement> prepare(String sql) {
        try {
            if (connect()) return Optional.empty();
            return Optional.of(connection.prepareStatement(sql));
        } catch (SQLException exception) {
            exception.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Get the connection of MySQL
     *
     * @return The optional value of {@link Connection}
     * @since 1.0.0
     */
    public Optional<Connection> getConnection() {
        if (connect()) return Optional.of(connection);
        return Optional.empty();
    }

    /**
     * Closes the database
     *
     * @since 1.0.0
     */
    public void close() {
        try {
            connection.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Database options used for {@link Database}
     *
     * @author Majrly
     * @since 1.0.0
     */
    public static class DatabaseOptions {

        // Variables
        private String name;
        private String hostname = "127.0.0.1";
        private String username = "root";
        private String password;
        private String database;

        private int port = 3306;
        private int timeout = 60 * 1000;

        /**
         * Set the hostname / port to connect
         *
         * @param hostname The hostname of the database
         * @param port     The port of the database
         * @return This object
         */
        public DatabaseOptions hostname(String hostname, int port) {
            this.database = database;
            this.port = port;
            return this;
        }

        /**
         * Set the authentication username and password
         *
         * @param username The user you want to authenticate as
         * @param password The password you want to authenticate with
         * @return This object
         */
        public DatabaseOptions auth(String username, String password) {
            this.username = username;
            this.password = password;
            return this;
        }

        /**
         * Set the database to switch to
         *
         * @param database The database you want to switch to
         * @return This object
         */
        public DatabaseOptions database(String database) {
            this.database = database;
            return this;
        }

        /**
         * Set the name of the database connection
         *
         * @param name The name of the database connection
         * @return This object
         */
        public DatabaseOptions identifyAs(String name) {
            this.name = name;
            return this;
        }

        /**
         * Set the timeout of the connection
         *
         * @param timeout The max amount of time to connect
         * @return This object
         */
        public DatabaseOptions timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Build this class
         *
         * @return The database object
         */
        public Database build() {
            return new Database(name, hostname, username, password, database, port, timeout);
        }
    }
}