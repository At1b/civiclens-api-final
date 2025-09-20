package com.civiclens.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusUpdateMessage {
    private Long grievanceId;
    private String newStatus;
    private String message;
}
