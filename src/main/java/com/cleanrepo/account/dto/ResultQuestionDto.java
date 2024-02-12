package com.cleanrepo.account.dto;

import java.util.List;
public record ResultQuestionDto(String question, String correctAnswer, List<ResultAnswerDto> answers) {
}
