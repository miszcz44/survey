package com.cleanrepo.account.dto;

import java.util.List;

public record AllSurveyCompaniesDto (String surveyName, List<CompanyDto> surveys) {
}
