package com.crm.backend.event;

import org.springframework.context.ApplicationEvent;

public class LeadActivityEvent extends ApplicationEvent {
    private final Long leadId;

    public LeadActivityEvent(Object source, Long leadId) {
        super(source);
        this.leadId = leadId;
    }

    public Long getLeadId() {
        return leadId;
    }
}

