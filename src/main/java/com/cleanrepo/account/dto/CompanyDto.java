package com.cleanrepo.account.dto;

import java.time.LocalDate;

public record CompanyDto(int companyId, LocalDate assignedDate, String companyName, int completedSurveys) {
}
