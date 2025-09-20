package com.civiclens.api.service;

import com.civiclens.api.document.GrievanceDocument;
import com.civiclens.api.model.Grievance;
import com.civiclens.api.repository.GrievanceRepository;
import com.civiclens.api.repository.GrievanceSearchRepository;
import com.civiclens.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import com.civiclens.api.model.User;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.civiclens.api.dto.StatusUpdateMessage;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Service
public class GrievanceService {

    private final GrievanceRepository grievanceRepository;
    private final UserRepository userRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private AiCategorizationService aiCategorizationService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate; // Inject the template
    @Autowired
    private EmailService emailService;
    @Autowired(required = false) // Set to not required, so the app can start without it
    private GrievanceSearchRepository grievanceSearchRepository;

    @Autowired // Spring's dependency injection to provide instances of the repositories
    public GrievanceService(GrievanceRepository grievanceRepository, UserRepository userRepository) {
        this.grievanceRepository = grievanceRepository;
        this.userRepository = userRepository;
    }

    // Grievance categories here
    private final List<String> GRIEVANCE_CATEGORIES = List.of(
            "Pothole", "Garbage", "Broken Streetlight", "Water Leakage", "Sewage Overflow", "Stray Animals"
    );

//    public Grievance createGrievance(Grievance grievance) {
//        // In a real app, you get the user from the security context
//        // For now, let's assume a user with ID 1 exists for testing
//        var user = userRepository.findById(1L).orElseThrow(() -> new RuntimeException("User not found"));
//        grievance.setSubmittedBy(user);
//        return grievanceRepository.save(grievance);
//    }
// Modify your createGrievance method (the one without the file for now)

//    public Grievance createGrievance(Grievance grievance) {
//        // Get the currently authenticated user principal
//        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//
//        // Set this user as the one who submitted the grievance
//        grievance.setSubmittedBy(currentUser);
//
//        // Now save the grievance
//        return grievanceRepository.save(grievance);
//    }

    public Grievance createGrievance(Grievance grievance, MultipartFile imageFile) {
        // 1. Get the currently authenticated user
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 2. Upload the file to S3 and get its public URL
        String imageUrl = fileStorageService.uploadFile(imageFile);

        // 3. Call the AI service to get the category
        String category = aiCategorizationService.categorizeGrievance(grievance.getDescription(), GRIEVANCE_CATEGORIES)
                .block();

        // 4. Prepare the complete grievance object
        grievance.setSubmittedBy(currentUser);
        grievance.setImageUrl(imageUrl);
        grievance.setCategory(category);

        // 5. Save the grievance to the database
        Grievance savedGrievance = grievanceRepository.save(grievance);

        // --- ELASTICSEARCH SYNC ---
        // 6. Save the same grievance to Elasticsearch
        if (grievanceSearchRepository != null) {
            grievanceSearchRepository.save(new GrievanceDocument(savedGrievance));
        }
        // --- END SYNC ---

        // --- GAMIFICATION LOGIC GOES HERE ---
        // This code only runs if the grievance was saved successfully.

        // 7. Award points to the user
        currentUser.setPoints(currentUser.getPoints() + 10);
        userRepository.save(currentUser); // Save the updated user

        // --- GAMIFICATION LOGIC ENDS HERE ---

        // 8. Return the successfully saved grievance
        return savedGrievance;
    }

    public Grievance getGrievanceById(Long id) {
        return grievanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grievance with ID " + id + " not found"));
    }

    // NEW METHOD to update status
    public Grievance updateGrievanceStatus(Long grievanceId, String newStatus) {
        // 1. Find the grievance in the database
        Grievance grievance = grievanceRepository.findById(grievanceId)
                .orElseThrow(() -> new RuntimeException("Grievance not found!"));

        // 2. Update its status and save it
        grievance.setStatus(newStatus);
        Grievance updatedGrievance = grievanceRepository.save(grievance);

        // 3. Create the notification message
        String message = "The status of your grievance '" + grievance.getTitle() + "' has been updated to " + newStatus;
        StatusUpdateMessage statusUpdate = new StatusUpdateMessage(grievanceId, newStatus, message);

        // 4. Send the message over WebSocket to a specific "topic"
        // Anyone subscribed to "/topic/grievance/1" will receive this message.
        messagingTemplate.convertAndSend("/topic/grievance/" + grievanceId, statusUpdate);

        // --- EMAIL NOTIFICATION LOGIC STARTS HERE ---

        // 5. If the new status is "Resolved", send an email
        if ("Resolved".equalsIgnoreCase(newStatus)) {
            String to = grievance.getSubmittedBy().getEmail();
            String subject = "Your Grievance Has Been Resolved! [ID: " + grievance.getId() + "]";
            String text = "Dear Citizen,\n\nWe are pleased to inform you that your grievance regarding '"
                    + grievance.getTitle() + "' has been resolved.\n\nThank you for using CivicLens.";

            emailService.sendSimpleMessage(to, subject, text);
        }

        // --- EMAIL NOTIFICATION LOGIC ENDS HERE ---

        // 6. --- ELASTICSEARCH SYNC ---
        // Also update the document in Elasticsearch
        if (grievanceSearchRepository != null) {
            grievanceSearchRepository.save(new GrievanceDocument(updatedGrievance));
        }
        // --- END SYNC ---

        return updatedGrievance;
    }

    public Grievance upvoteGrievance(Long grievanceId) {
        // 1. Find the grievance by its ID
        Grievance grievance = grievanceRepository.findById(grievanceId)
                .orElseThrow(() -> new RuntimeException("Grievance not found with id: " + grievanceId));

        // 2. Increment the vote count
        grievance.setVotes(grievance.getVotes() + 1);

        // 3. Save the updated grievance back to the database
        return grievanceRepository.save(grievance);
    }

    public List<Grievance> getPublicGrievances() {
        return grievanceRepository.findAllByOrderByVotesDesc();
    }

    @Profile("!prod")
    public List<GrievanceDocument> searchGrievances(String query) {
        return grievanceSearchRepository.findByTitleContainingOrDescriptionContaining(query, query);
    }
}
