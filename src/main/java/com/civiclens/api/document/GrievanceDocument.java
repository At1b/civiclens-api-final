package com.civiclens.api.document;

import com.civiclens.api.model.Grievance;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Profile("!prod")
@Data
@NoArgsConstructor
@Document(indexName = "grievances") // This is the name of the "table" in Elasticsearch
public class GrievanceDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text) // Analyzed for full-text search
    private String title;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword) // Matched exactly
    private String status;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Integer)
    private int votes;

    // A handy constructor to convert from our JPA entity to this document
    public GrievanceDocument(Grievance grievance) {
        this.id = grievance.getId();
        this.title = grievance.getTitle();
        this.description = grievance.getDescription();
        this.status = grievance.getStatus();
        this.category = grievance.getCategory();
        this.votes = grievance.getVotes();
    }
}
