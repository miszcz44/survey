package com.cleanrepo.account.dto;

import java.util.List;
public record SurveyDetailsDto(String surveyName, List<QuestionDetailsDto> questionList) {
}
