package me.majrly.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import me.majrly.database.params.Parameter;
import me.majrly.database.statements.Query;
import me.majrly.database.statements.Statement;

import java.sql.*;
import java.util.Map;
import java.util.Optional;

/**
 * Database API
 *
 * @author Majrly
 * @since 1.0.0
 */
public class Database {

    // Variables
    @Getter private String name;
    @Getter private String hostname;
    @Getter private String username;
    @Getter private String password;
    @Getter private String database;
    @Getter private String type;

    @Getter private int port = 3306;

    @Getter private HikariDataSource source;
    @Getter private HikariConfig config = new HikariConfig();

    /**
     * Database API
     *
     * @param type     The type of database instance
     * @param driver   The driver for the database
     * @param name     The name of this database instance
     * @param hostname The ip to use when connecting
     * @param username The username to authenticate as
     * @param password The password to authenticate yourself
     * @param database The name of the database to switch to
     * @param port     The port to use when connecting
     * @since 1.0.0
     */
    public Database(String type, String driver, String name, String hostname, String username, String password, String database, int port, HikariConfig config) {
        this.config = config;
        this.name = name;
        this.config.setDriverClassName(driver);
        this.config.setJdbcUrl("jdbc:" + (this.type = type) + "://" + (this.hostname = hostname) + ":" + (this.port = port) + "/" + (this.database = database));
        this.config.setUsername(this.username = username);
        this.config.setPassword(this.password = password);
        this.source = new HikariDataSource(config);
    }

    /**
     * Database API
     *
     * @param name     The name of this database instance
     * @param hostname The ip to use when connecting
     * @param username The username to authenticate as
     * @param password The password to authenticate yourself
     * @param database The name of the database to switch to
     * @param port     The port to use when connecting
     * @since 1.0.0
     */
    public Database(String name, String hostname, String username, String password, String database, int port, HikariConfig config) {
        this("mysql", "com.mysql.jdbc.Driver", name, hostname, username, password, database, port, config);
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
     * Sends a query to the database
     *
     * @param statement The statement to send the database
     * @return Either the int of an update, or the ResultSet of a query
     * @since 1.0.0
     */
    public Optional<?> send(Statement statement) {
        Optional<PreparedStatement> preparedStatement = prepare(statement);
        try {
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
            PreparedStatement preparedStatement = source.getConnection().prepareStatement(statement.getSql());
            for (Map.Entry<Integer, Parameter> parameter : statement.getParameters().entrySet()) {
                switch (parameter.getValue().getType()) {
                    case STRING:
                        preparedStatement.setString(parameter.getKey(), (String) parameter.getValue().getData());
                        break;
                    case INTEGER:
                        preparedStatement.setInt(parameter.getKey(), (Integer) parameter.getValue().getData());
                        break;
                    case DOUBLE:
                        preparedStatement.setDouble(parameter.getKey(), (Double) parameter.getValue().getData());
                        break;
                    case LONG:
                        preparedStatement.setLong(parameter.getKey(), (Long) parameter.getValue().getData());
                        break;
                    case BLOB:
                        preparedStatement.setBlob(parameter.getKey(), (Blob) parameter.getValue().getData());
                        break;
                    case FLOAT:
                        preparedStatement.setFloat(parameter.getKey(), (Float) parameter.getValue().getData());
                        break;
                    case BOOLEAN:
                        preparedStatement.setBoolean(parameter.getKey(), (Boolean) parameter.getValue().getData());
                        break;
                    case DATE:
                        preparedStatement.setDate(parameter.getKey(), (Date) parameter.getValue().getData());
                        break;
                    default:
                        preparedStatement.setObject(parameter.getKey(), parameter.getValue().getData());
                        break;
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
        PreparedStatement preparedStatement = null;
        try {
            return Optional.of(preparedStatement = source.getConnection().prepareStatement(sql));
        } catch (SQLException exception) {
            exception.printStackTrace();
            return Optional.empty();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement = source.getConnection().prepareStatement(sql);
                }
            } catch (SQLException e) {
                // Couldn't close connection
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the connection of MySQL
     *
     * @return The optional value of {@link Connection}
     * @since 1.0.0
     */
    public Optional<Connection> getConnection() {
        return Optional.empty();
    }

    /**
     * Closes the database
     *
     * @since 1.0.0
     */
    public void close() {
        source.close();
    }

    /**
     * Database options used for {@link Database}
     *
     * @author Majrly
     * @since 1.0.0
     */
    public static class DatabaseOptions {

        // Variables
        private HikariConfig config = new HikariConfig();

        private String name;
        private String hostname = "127.0.0.1";
        private String username = "root";
        private String password;
        private String database;
        private String type;
        private String driver;

        private int port = 3306;

        /**
         * Set a key/value in the HikariConfig
         *
         * @param key   The key you want to set a value to
         * @param value The value you want to set
         * @since 1.0.0
         */
        public DatabaseOptions set(String key, String value) {
            config.addDataSourceProperty(key, value);
            return this;
        }

        /**
         * Set the hostname / port to connect
         *
         * @param hostname The hostname of the database
         * @param port     The port of the database
         * @return This object
         * @since 1.0.0
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
         * @since 1.0.0
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
         * @since 1.0.0
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
         * @since 1.0.0
         */
        public DatabaseOptions name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Set the type of sql database
         *
         * @param type The type of the SQL server
         * @return This object
         * @since 1.0.0
         */
        public DatabaseOptions type(String type) {
            this.type = type;
            return this;
        }

        /**
         * Set the drvier for the sql database
         *
         * @param type The driver of the SQL server
         * @return This object
         * @since 1.0.0
         */
        public DatabaseOptions driver(String type) {
            this.driver = type;
            return this;
        }

        /**
         * Build this class
         *
         * @return The database object
         * @since 1.0.0
         */
        public Database build() {
            return new Database(type, driver, name, hostname, username, password, database, port, config);
        }
    }
}