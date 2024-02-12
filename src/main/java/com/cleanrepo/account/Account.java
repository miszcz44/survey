package com.cleanrepo.account;

import com.cleanrepo.account.command.HostRegisterCommand;
import com.cleanrepo.account.command.RegisterCommand;
import com.cleanrepo.account.command.UserRegisterCommand;
import com.cleanrepo.account.value_object.*;
import com.cleanrepo.survey.SubmittedSurvey;
import com.cleanrepo.survey.Survey;
import com.cleanrepo.survey.SurveyWithChosenQuestions;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "account")
@Getter(AccessLevel.PACKAGE)
@Setter(AccessLevel.PACKAGE)
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int accountId;

    @Valid @Embedded
    private UserEmail email;

    @Valid @Embedded
    private Username name;

    @Valid @Embedded
    private EncodedPassword encodedPassword;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Role must not be null")
    private UserRole role;

    @Valid @Embedded
    private Locked locked;

    @ManyToOne
    private Account admin;

    @ManyToOne
    private Account host;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id")
    private List<Survey> surveys = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id")
    private List<SubmittedSurvey> submittedSurveys = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id")
    private List<SurveyWithChosenQuestions> hostSurveys = new ArrayList<>();

    @Column(name = "confirmation_token")
    private String confirmationToken;

    private LocalDateTime createdAt;


    public Account(RegisterCommand registerCommand, String encodedPassword) {
        email = registerCommand.getEmail();
        this.encodedPassword = new EncodedPassword(encodedPassword);
        role = UserRole.ROLE_ADMIN; // jeśli użytkownik rejestruje się sam, to jest adminem
        locked = new Locked(false);
        createdAt = LocalDateTime.now();
    }
    public Account(HostRegisterCommand registerCommand) {
        email = registerCommand.getEmail();
        name = registerCommand.getName();
        role = UserRole.ROLE_HOST; // jeśli użytkownik jest rejestrowany przez admina, to jest userem
        locked = new Locked(false);
        createdAt = LocalDateTime.now();
    }

    public Account(UserRegisterCommand registerCommand) {
        email = registerCommand.getEmail();
        name = registerCommand.getName();
        role = registerCommand.getRole();
        locked = new Locked(false);
        createdAt = LocalDateTime.now();
    }
}
