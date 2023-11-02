package org.springframework.samples.petclinic.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.samples.petclinic.model.PreparedStatementData;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.Map;
import java.util.Objects;

@Component
public class SqlInjectionChecker {

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
     * @param preparedSql The query with the parameters replaced by '?'
     * @param preparedStatementData    The parameters to be inserted into the query
     * @return          True if the query is different from the escaped query, false otherwise
     */
    public boolean detectByPreparedStatement(String query, String preparedSql, PreparedStatementData preparedStatementData) {
        try {
            String escapedQuery = createPreparedStatement(preparedSql, preparedStatementData);
            // HikariProxyPreparedStatement@1054499273 wrapping org.hsqldb.jdbc.JDBCPreparedStatement@18ab83cb[sql=[SELECT id, first_name, last_name FROM vets WHERE id= :id], parameters=[[1]]]
            // TODO: HSSQLDB does not implement toString() in a way that includes the SQL query
            // Or maybe it's because of HikariCP?
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

    // stuff necessary for the escaped SQL query extraction

    /**
     * This method extracts the escaped SQL query from the PreparedStatement.
     * This only works if the JDBC driver implements toString() in a way that
     * includes the SQL query, e.g., Postgres or MySQL.
     * @param preparedStatement The PreparedStatement to extract the escaped SQL query from
     * @return                  The escaped SQL query
     */
    public String extractEscapedQuery(PreparedStatement preparedStatement) {
        return preparedStatement.toString();
    }

    private String createPreparedStatement(String sql, PreparedStatementData preparedStatementData) throws SQLException {
        // Get a connection from the DataSource and also close it when done
        try (Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {

            // Implement the PreparedStatementCreator interface
            PreparedStatementCreator preparedStatementCreator = connection1 -> {
                // Create the PreparedStatement with the given SQL
                PreparedStatement preparedStatement = connection1.prepareStatement(sql);

                // Set the parameters in the PreparedStatement
                int i = 1;
                for (Map.Entry<String, Map.Entry<Class<?>, Object>> param : preparedStatementData.getParametersWithTypes().entrySet()) {
                    // Dynamically cast the parameter to the original type
                    Class<?> theType = param.getValue().getKey();
                    Object value;
                    if (theType.equals(Integer.class)) {
                        value = Integer.parseInt((String) param.getValue().getValue());
                    } else {
                        value = theType.cast(param.getValue().getValue());
                    }

                    preparedStatement.setObject(i++, value);
                }
                return preparedStatement;
            };

            return extractEscapedQuery(preparedStatementCreator.createPreparedStatement(connection));
        }
    }
}
