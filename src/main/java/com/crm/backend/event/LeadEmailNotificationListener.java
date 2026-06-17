package com.crm.backend.event;

import com.crm.backend.repository.LeadsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class LeadEmailNotificationListener {

    @Autowired
    private LeadsRepository leadRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Async
    @EventListener
    public void handleLeadEmailNotification(LeadActivityEvent event) {
        leadRepository.findById(event.getLeadId()).ifPresent(lead -> {
            if ("qualified".equalsIgnoreCase(lead.getStage())) {
                try {
                    // Create a simple text-based email structure
                    SimpleMailMessage message = new SimpleMailMessage();

                    // Optional: Matches your properties setup configuration
                    message.setFrom("sanketks200305@gmail.com");
                    message.setTo(lead.getEmail());
                    message.setSubject("Welcome to ApexCRM - Dynamic Qualification Completed");

                    String emailBody = String.format(
                            "Dear %s,\n\n" +
                                    "We are excited to share that your business, %s, " +
                                    "has met all qualifications standards. A sales representative will connect soon.\n\n" +
                                    "Best Regards,\n" +
                                    "ApexCRM System Engine",
                            lead.getName(), lead.getCompany()
                    );
                    message.setText(emailBody);

                    // Execute actual outbound SMTP transport delivery
                    mailSender.send(message);

                } catch (MailException ex) {
                    // Log the failure to prevent breaking the background execution thread
                    System.err.println("Failed to send qualification email to " + lead.getEmail() + " : " + ex.getMessage());
                }
            }
        });
    }
}

