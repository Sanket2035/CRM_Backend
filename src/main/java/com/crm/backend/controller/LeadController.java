package com.crm.backend.controller;

import com.crm.backend.event.LeadActivityEvent;
import com.crm.backend.model.Leads;
import com.crm.backend.repository.LeadsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('Admin', 'Sales')")
public class LeadController {

    @Autowired
    private LeadsRepository leadsRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @GetMapping
    public List<Leads> getAllLeads() {
        return leadsRepository.findAll();
    }

    @PostMapping
    public Leads createLead(@RequestBody Leads lead) {
        return leadsRepository.save(lead);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Leads> updateLeads(@PathVariable Long id, @RequestBody Leads leads){
        return leadsRepository.findById(id).map(lead ->{
            lead.setName(leads.getName());
            lead.setCompany(leads.getCompany());
            lead.setEmail(leads.getEmail());
            lead.setDealValue(leads.getDealValue());
            lead.setStage(leads.getStage());
            lead.setPhone(leads.getPhone());

            Leads saved = leadsRepository.save(lead);
            eventPublisher.publishEvent(new LeadActivityEvent(this, saved.getId()));
            return ResponseEntity.ok(saved);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLead(@PathVariable Long id){
        return leadsRepository.findById(id).map(leads -> {
            leadsRepository.delete(leads);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
