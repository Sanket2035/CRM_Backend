package com.crm.backend.controller;

import com.crm.backend.model.Campaigns;
import com.crm.backend.repository.CampaignsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('Admin', 'Sales')")
public class CampaignController {

    @Autowired
    private CampaignsRepository campaignsRepository;

    @GetMapping
    public List<Campaigns> getAllCampaigns() {
        return campaignsRepository.findAll();
    }

    @PostMapping
    public Campaigns createCampaign(@RequestBody Campaigns campaign) {
        return campaignsRepository.save(campaign);
    }
}
