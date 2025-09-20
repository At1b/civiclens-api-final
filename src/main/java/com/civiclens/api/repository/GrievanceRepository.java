package com.civiclens.api.repository;

import com.civiclens.api.model.Grievance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrievanceRepository extends JpaRepository<Grievance, Long> {

    // New method to find all grievances and order them by the 'votes' field in descending order
    List<Grievance> findAllByOrderByVotesDesc();
}
