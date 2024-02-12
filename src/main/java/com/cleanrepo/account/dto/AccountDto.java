package com.cleanrepo.account.dto;

import java.time.LocalDateTime;

public record AccountDto(int id, LocalDateTime createdDate, String fullName, String email, int completedSurveys) {
}
