package org.penpal.email.scheduler;

import org.penpal.email.service.EmailThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledReminderTask {
    @Autowired
    private EmailThreadService emailThreadService;

    // Run every Sunday at 1 am GMT
    @Scheduled(cron = "0 0 1 * * SUN", zone = "GMT")
    public void remindInactiveStudents() {
        emailThreadService.remindInactiveStudents();
    }
}
