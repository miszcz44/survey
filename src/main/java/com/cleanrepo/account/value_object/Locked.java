package com.cleanrepo.account.value_object;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class Locked implements Serializable {

    @NotNull(message = "User locked property cannot be null")
    private boolean locked;

}