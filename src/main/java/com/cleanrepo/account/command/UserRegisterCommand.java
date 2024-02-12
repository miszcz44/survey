package com.cleanrepo.account.command;

import com.cleanrepo.account.value_object.UserEmail;
import com.cleanrepo.account.value_object.UserRole;
import com.cleanrepo.account.value_object.Username;
import com.cleanrepo.common.SelfValidating;
import jakarta.validation.Valid;
import lombok.Getter;

@Getter
public class UserRegisterCommand extends SelfValidating<UserRegisterCommand> {
    @Valid
    private final UserEmail email;

    @Valid
    private final Username name;

    @Valid
    private final UserRole role;
    private UserRegisterCommand(Json json) {
        this.email = new UserEmail(json.email);
        this.name = new Username(json.name);
        this.role = json.role;
        validateSelf();
    }

    public record Json(String email, String name, UserRole role) {
        public UserRegisterCommand toCommand() {
            return new UserRegisterCommand(this);
        }
    }
}
