package com.cleanrepo.account.dto;

import java.time.LocalDate;

public record AdminSurveyListDto(int id, LocalDate createdAt, String name, int completedSurveys) {
}
