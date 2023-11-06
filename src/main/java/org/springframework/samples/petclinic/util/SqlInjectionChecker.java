package org.springframework.samples.petclinic.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.samples.petclinic.model.PreparedStatementParameter;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.Objects;
import java.util.Set;

@Component
public class SqlInjectionChecker {
    @Value("${feature.typeEscape}")
    private boolean typeEscape;
    private final Logger logger = LoggerFactory.getLogger(SqlInjectionChecker.class);
    private final ShutdownHandler shutdownHandler;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SqlInjectionChecker(ShutdownHandler shutdownHandler, JdbcTemplate jdbcTemplate) {
        this.shutdownHandler = shutdownHandler;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void verify(boolean... results) {
        for (boolean result : results) {
            if (result) {
                logger.info("SQL injection detected!");
                shutdownHandler.shutdown(0);
            }
        }
    }

    /* This method checks if the input contains any of the common SQL injection keywords.
     * It is not a foolproof method, hence we prefer the PreparedStatement method below.
     */
    @Deprecated
    public boolean detectByKeywords(String input) {
        // Define a list of common SQL injection keywords
        String[] sqlKeywords = {"SELECT", "INSERT", "UPDATE", "DELETE", "UNION", "DROP", "ALTER", "EXEC", "TRUNCATE",
                                "OR"};

        // Convert input to uppercase and remove spaces to make detection case-insensitive
        String sanitizedInput = input.toUpperCase().replaceAll("\\s", "");

        // Check if any of the SQL keywords are present in the input
        for (String keyword : sqlKeywords) {
            if (sanitizedInput.contains(keyword)) {
                return true; // Potential SQL injection detected
            }
        }

        return false; // No SQL injection detected
    }

    /**
     * This method checks for SQLi by comparing the query with the escaped query.
     * @param query     The query that is expected to be executed
     * @param statement The query with the parameters replaced by '?'
     * @param parameters    The parameters to be inserted into the query
     * @return          True if the query is different from the escaped query, false otherwise
     */
    public boolean detectByPreparedStatement(String query, String statement, Set<PreparedStatementParameter> parameters) {
        try {
            String escapedQuery = createEscapedQuery(statement, parameters);
            return !query.equals(escapedQuery);
        } catch (SQLException e) {
            logger.error("Error while checking for SQL injection", e);

            if (e instanceof SQLSyntaxErrorException) {
                String sqlState = e.getSQLState();
                if (sqlState != null && sqlState.equals("22018")) {
                    // This SQLState usually indicates an incompatible data type in conversion
                    // Handle the exception accordingly
                    System.out.println("Incompatible data type in conversion error");
                    return true;
                } else {
                    // Other SQLSyntaxErrorException handling
                    System.out.println("Other SQLSyntaxErrorException handling");
                }
            } else {
                // Other SQLException handling
                System.out.println("Other SQLException handling");
            }
        }
        return false;
    }

    /**
     * This method extracts the escaped SQL query from the PreparedStatement.
     * This only works if the JDBC driver implements toString() in a way that
     * includes the SQL query, e.g., Postgres or MySQL.
     * @param preparedStatement The PreparedStatement to extract the escaped SQL query from
     * @return                  The escaped SQL query
     */
    public String extractEscapedQuery(PreparedStatement preparedStatement) {
        // Use reflection to access the delegate field, i.e., real PreparedStatement
//            Field field = preparedStatement.getClass().getDeclaredField("delegate");
//            field.setAccessible(true);
//            PreparedStatement unwrappedPreparedStatement = (PreparedStatement) field.get(preparedStatement);
        // This doesn't work somehow, accesses the wrong class

        String escapedQuery = preparedStatement.toString();
        return escapedQuery.substring(escapedQuery.lastIndexOf(':') + 2);
    }

    private String createEscapedQuery(String sql, Set<PreparedStatementParameter> parameters) throws SQLException {
        // Get a connection from the DataSource and also close it when done
        try (Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {

            // Implement the PreparedStatementCreator interface
            PreparedStatementCreator preparedStatementCreator = connection1 -> {
                // Create the PreparedStatement with the given SQL
                PreparedStatement preparedStatement = connection1.prepareStatement(sql);

                int i = 1;
                // Set the parameters in the PreparedStatement
                for (PreparedStatementParameter param : parameters) {
                    Object value;
                    if (this.typeEscape) {
                        // Dynamically cast the parameter to the original type
                        if (param.type().equals(Integer.class)) {
                            value = Integer.parseInt((String) param.value());
                        } else {
                            value = param.type().cast(param.value());
                        }
                    } else {
                        // "just" set the parameter as an Object
                        value = param.value();
                    }
                    preparedStatement.setObject(i++, value);
                }
                return preparedStatement;
            };

            PreparedStatement ps = preparedStatementCreator.createPreparedStatement(connection);
            // Make sure to extract the query before the connection is closed.
            return extractEscapedQuery(ps);
        }
    }
}
