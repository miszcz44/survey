package com.cleanrepo.account.command;

import com.cleanrepo.account.value_object.RawPassword;
import com.cleanrepo.account.value_object.UserEmail;
import com.cleanrepo.common.SelfValidating;
import jakarta.validation.Valid;
import lombok.Getter;

@Getter
public final class RegisterCommand extends SelfValidating<RegisterCommand> {

    @Valid private final UserEmail email;
    @Valid private final RawPassword password;

    private RegisterCommand(Json json) {
        this.email = new UserEmail(json.email);
        this.password = new RawPassword(json.password);
        validateSelf();
    }

    public record Json(String email, String password) {
        public RegisterCommand toCommand() {
            return new RegisterCommand(this);
        }
    }
}
