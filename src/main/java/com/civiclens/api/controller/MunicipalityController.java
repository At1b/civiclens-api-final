package com.civiclens.api.controller;

import com.civiclens.api.model.Grievance;
import com.civiclens.api.service.GrievanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/municipal")
@RequiredArgsConstructor
public class MunicipalityController {

    private final GrievanceService grievanceService;

    @PatchMapping("/grievances/{id}/status")
    public ResponseEntity<Grievance> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusPayload
    ) {
        String newStatus = statusPayload.get("status");
        Grievance updatedGrievance = grievanceService.updateGrievanceStatus(id, newStatus);
        return ResponseEntity.ok(updatedGrievance);
    }
}
