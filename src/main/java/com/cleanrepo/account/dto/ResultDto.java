package com.cleanrepo.account.dto;

import java.util.List;
public record ResultDto(Integer score, List<ResultQuestionDto> questions) {
}
