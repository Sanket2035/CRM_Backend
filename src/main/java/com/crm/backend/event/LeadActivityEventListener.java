package com.crm.backend.event;

import com.crm.backend.model.Activity;
import com.crm.backend.model.Leads;
import com.crm.backend.repository.LeadsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@Component
@EnableAsync
public class LeadActivityEventListener {

    @Autowired
    private LeadsRepository leadRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Async
    @EventListener
    public void handleLeadActivityEvent(LeadActivityEvent event) {
        leadRepository.findById(event.getLeadId()).ifPresent(lead -> {
            // Decoupled scoring logic
            int baseScore = 30;
            String stage = lead.getStage() != null ? lead.getStage().toLowerCase() : "new";
            switch (stage) {
                case "won": baseScore = 100; break;
                case "proposal": baseScore = 90; break;
                case "qualified": baseScore = 80; break;
                case "contacted": baseScore = 55; break;
                default: baseScore = 30; break;
            }

            // Value boost: up to 10 points
            if (lead.getDealValue() != null) {
                int valueBoost = (int) Math.min(10, lead.getDealValue() / 10000);
                baseScore += valueBoost;
            }

            lead.setScore(Math.min(100, baseScore));
            Leads savedLead = leadRepository.save(lead);

            // Notify websocket client instantly
            messagingTemplate.convertAndSend("/topic/activities",
                    Activity.builder()
                            .type("sales")
                            .text("Event Engine: Lead " + savedLead.getName() + " score auto-updated to " + savedLead.getScore() + "%")
                            .time("Just now")
                            .build()
            );
        });
    }
}
