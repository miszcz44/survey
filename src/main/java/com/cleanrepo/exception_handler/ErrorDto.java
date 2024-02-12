package com.cleanrepo.exception_handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
record ErrorDto (

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String field,
    String message

) { }
