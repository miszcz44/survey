package com.cleanrepo.account.dto;

import com.cleanrepo.question.Type;

public record VerifiedDto(String question, String modelAnswer, String answerContent, boolean isAnswerCorrect, Type type) {
}
