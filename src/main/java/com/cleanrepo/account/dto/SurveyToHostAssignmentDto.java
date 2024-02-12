package com.cleanrepo.account.dto;

import java.util.List;
public record SurveyToHostAssignmentDto(Integer companyId, List<Integer> questionIds, Integer surveyId)  {
}
