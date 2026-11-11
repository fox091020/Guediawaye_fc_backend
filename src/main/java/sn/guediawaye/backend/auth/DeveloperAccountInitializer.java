package sn.guediawaye.backend.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DeveloperAccountInitializer implements ApplicationRunner {

    private final AdminAccountService accountService;
    private final String initialPassword;

    public DeveloperAccountInitializer(
            AdminAccountService accountService,
            @Value("${app.auth.developer-initial-password:}") String initialPassword) {
        this.accountService = accountService;
        this.initialPassword = initialPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        accountService.initializeDeveloper("bmd", initialPassword);
    }
}
