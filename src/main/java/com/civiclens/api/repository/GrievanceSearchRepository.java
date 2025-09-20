package com.civiclens.api.repository;

import com.civiclens.api.document.GrievanceDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

@Profile("!prod")
public interface GrievanceSearchRepository extends ElasticsearchRepository<GrievanceDocument, Long>{
    // Spring Data will create the complex search query based on this method name
    List<GrievanceDocument> findByTitleContainingOrDescriptionContaining(String title, String description);
}
