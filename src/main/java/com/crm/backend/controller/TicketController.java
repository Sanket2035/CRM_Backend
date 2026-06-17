package com.crm.backend.controller;

import com.crm.backend.model.Ticket;
import com.crm.backend.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('Admin', 'Support')")
public class TicketController {

    @Autowired
    private TicketRepository ticketRepository;

    @GetMapping
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @PostMapping
    public Ticket createTicket(@RequestBody Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ticket> updateTicket(@PathVariable long id, @RequestBody Ticket ticket){
        return ticketRepository.findById(id).map(existing ->{
            existing.setSubject(ticket.getSubject());
            existing.setCustomer(ticket.getCustomer());
            existing.setPriority(ticket.getPriority());
            existing.setStatus(ticket.getStatus());
            existing.setSlaHours(ticket.getSlaHours());
            existing.setAssignee(ticket.getAssignee());
            return ResponseEntity.ok(ticketRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }
}
