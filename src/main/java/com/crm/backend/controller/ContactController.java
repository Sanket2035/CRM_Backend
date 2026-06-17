package com.crm.backend.controller;

import com.crm.backend.model.Contacts;
import com.crm.backend.repository.ContactsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('Admin', 'Sales', 'Support')")
public class ContactController {
    @Autowired
    private ContactsRepository contactsRepository;

    @GetMapping
    public List<Contacts> getAllContacts() {
        return contactsRepository.findAll();
    }

    @PostMapping
    public Contacts createContact(@RequestBody Contacts contact) {
        return contactsRepository.save(contact);
    }

    @PutMapping("/{id}")
    public Contacts updateContact(@PathVariable Long id, @RequestBody Contacts contact) {
        return contactsRepository.findById(id)
                .map(existing -> {
                    existing.setName(contact.getName());
                    existing.setEmail(contact.getEmail());
                    existing.setPhone(contact.getPhone());
                    existing.setCompany(contact.getCompany());
                    existing.setStatus(contact.getStatus());
                    existing.setTags(contact.getTags());
                    return contactsRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Contact not found with id " + id));
    }

    @PostMapping("/{id}/activities")
    public ResponseEntity<Contacts> addActivity(@PathVariable Long id, @RequestBody Contacts.ActivityLog activity){
        return contactsRepository.findById(id).map(contacts -> {
            contacts.getActivities().add(0,activity);
            return ResponseEntity.ok(contactsRepository.save(contacts));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
