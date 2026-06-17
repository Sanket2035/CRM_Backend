package com.crm.backend.controller;


import com.crm.backend.model.Activity;
import com.crm.backend.repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('Admin', 'Sales', 'Support')")
public class ActivityController {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }

    @PostMapping
    public Activity createActivity(@RequestBody Activity activity) {
        Activity saved = activityRepository.save(activity);
        messagingTemplate.convertAndSend("/topic/activities", saved);
        return saved;
    }
}
