package com.civiclens.api.controller;

import com.civiclens.api.model.Grievance;
import com.civiclens.api.service.GrievanceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.civiclens.api.document.GrievanceDocument;

import java.util.List;

@RestController
@RequestMapping("/api/grievances")
public class GrievanceController {

    private final GrievanceService grievanceService;
    private final ObjectMapper objectMapper; // Add ObjectMapper for JSON conversion

    @Autowired
    public GrievanceController(GrievanceService grievanceService, ObjectMapper objectMapper) {
        this.grievanceService = grievanceService;
        this.objectMapper = objectMapper;
    }

    // This is the updated endpoint for submitting a grievance with an image
    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<Grievance> submitGrievance(
            @RequestPart("grievance") String grievanceJson,
            @RequestPart("image") MultipartFile imageFile
    ) throws JsonProcessingException {

        // Convert the grievance JSON string into a Grievance object
        Grievance grievance = objectMapper.readValue(grievanceJson, Grievance.class);

        // Call the service method with BOTH arguments
        Grievance createdGrievance = grievanceService.createGrievance(grievance, imageFile);

        return ResponseEntity.status(201).body(createdGrievance);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Grievance> getGrievanceById(@PathVariable Long id) {
        Grievance grievance = grievanceService.getGrievanceById(id);
        return ResponseEntity.ok(grievance);
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<Grievance> upvoteGrievance(@PathVariable Long id) {
        Grievance updatedGrievance = grievanceService.upvoteGrievance(id);
        return ResponseEntity.ok(updatedGrievance);
    }

    @GetMapping("/public")
    public ResponseEntity<List<Grievance>> getPublicDashboard() {
        List<Grievance> grievances = grievanceService.getPublicGrievances();
        return ResponseEntity.ok(grievances);
    }

    @GetMapping("/search")
    public ResponseEntity<List<GrievanceDocument>> searchGrievances(@RequestParam("q") String query) {
        return ResponseEntity.ok(grievanceService.searchGrievances(query));
    }
}
