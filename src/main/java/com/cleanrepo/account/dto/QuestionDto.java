package com.cleanrepo.account.dto;

import com.cleanrepo.question.Type;

import java.util.List;

public record QuestionDto(String question, List<AnswerDto> answers, String hint, Type type) {
}
