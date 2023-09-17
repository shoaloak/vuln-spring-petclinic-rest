package org.springframework.samples.petclinic.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ShutdownHandler implements ApplicationContextAware {

    private ConfigurableApplicationContext context;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        this.context = (ConfigurableApplicationContext) context;
    }

    public void terminate(int exitCode) {
        validateExitCode(exitCode);
        context.close();
        System.exit(exitCode);
    }

    private void validateExitCode(int exitCode) {
        if (exitCode < 0 || exitCode > 255) {
            throw new IllegalArgumentException("Exit code must be between 0 and 255.");
        }
    }
}
