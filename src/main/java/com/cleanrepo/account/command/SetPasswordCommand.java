package com.cleanrepo.account.command;

import com.cleanrepo.account.value_object.RawPassword;
import com.cleanrepo.account.value_object.UserEmail;
import com.cleanrepo.common.SelfValidating;
import jakarta.validation.Valid;
import lombok.Getter;

@Getter
public final class SetPasswordCommand extends SelfValidating<SetPasswordCommand> {
    @Valid
    private final UserEmail email;

    @Valid
    private final RawPassword password;

    private final String token;

    private SetPasswordCommand(Json json) {
        this.email = new UserEmail(json.email);
        this.password = new RawPassword(json.password);
        this.token = json.token;
        validateSelf();
    }

    public record Json(String email, String password, String token) {
        public SetPasswordCommand toCommand() {
            return new SetPasswordCommand(this);
        }
    }
}
