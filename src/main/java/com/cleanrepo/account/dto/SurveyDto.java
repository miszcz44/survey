package com.cleanrepo.account.dto;

import java.util.List;
public record SurveyDto(String name, List<QuestionDto> questionList) {
}
