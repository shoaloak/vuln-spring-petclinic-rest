package org.springframework.samples.petclinic.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SqlInjectionDetector {

    private final Logger logger = LoggerFactory.getLogger(SqlInjectionDetector.class);

    private final ShutdownHandler shutdownHandler;

    @Autowired
    public SqlInjectionDetector(ShutdownHandler shutdownHandler) {
        this.shutdownHandler = shutdownHandler;
    }

    public void verifyInput(String input) {
        if (isSqlInjection(input)) {
            logger.info("SQL injection detected!");
            shutdownHandler.shutdown(0);
        }
    }

    public static boolean isSqlInjection(String input) {
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
}
