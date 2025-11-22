package com.example.accesscontrol.dto;

import com.example.accesscontrol.model.AccessRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessRequestDetailDTO {

    private Long id;
    private String protocol;
    private String userEmail;
    private String userName;
    private String userDepartment;
    private List<String> moduleNames;
    private AccessRequestStatus status;
    private String justification;
    private boolean urgent;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String denialReason;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private List<HistoryEntryDTO> history;

    @Data
    @Builder
    public static class HistoryEntryDTO {
        private LocalDateTime eventDate;
        private String eventType;
        private String details;
    }
}
