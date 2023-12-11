package org.springframework.samples.petclinic.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * @author Axel Koolhaas
 * A utility class for shutting down a Spring Boot application.
 * <a href="https://github.com/spring-projects/spring-boot/blob/main/spring-boot-project/spring-boot-actuator/src/main/java/org/springframework/boot/actuate/context/ShutdownEndpoint.java">Based on actuator</a>
 */
@Component
public class ShutdownHandler implements ApplicationContextAware {

    private ConfigurableApplicationContext context;
    private int exitCode = 0;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        if (context instanceof ConfigurableApplicationContext configurableContext) {
            this.context = configurableContext;
        }
    }

    public void shutdown(int exitCode) {
        setExitCode(exitCode);
        Thread thread = new Thread(this::performShutdown);
        thread.setContextClassLoader(getClass().getClassLoader());
        thread.start();
    }

    private void setExitCode(int exitCode) {
        if (exitCode < 0 || exitCode > 255) {
            throw new IllegalArgumentException("Exit code must be between 0 and 255.");
        }
        this.exitCode = exitCode;
    }

    private void performShutdown() {
        // Delay to allow clients to receive HTTP response
        try {
            Thread.sleep(500L);
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        // Perform actual shutdown
        this.context.close();
        // (Force) kill the JVM to set the exit code
        System.exit(this.exitCode);
    }
}
