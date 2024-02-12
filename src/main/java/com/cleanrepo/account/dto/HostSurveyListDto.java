package com.cleanrepo.account.dto;

import java.time.LocalDate;

public record HostSurveyListDto(int id, LocalDate createdDate, String fullName, String email, int completedSurveys) {
}
