package com.cleanrepo.account;

import com.cleanrepo.account.command.HostRegisterCommand;
import com.cleanrepo.account.command.RegisterCommand;
import com.cleanrepo.account.command.SetPasswordCommand;
import com.cleanrepo.account.command.UserRegisterCommand;
import com.cleanrepo.account.dto.*;
import com.cleanrepo.account.exception.EmailTakenException;
import com.cleanrepo.account.value_object.UserRole;
import com.cleanrepo.account.value_object.Username;
import com.cleanrepo.question.Question;
import com.cleanrepo.question.QuestionRepository;
import com.cleanrepo.question.SubmittedQuestion;
import com.cleanrepo.question.Type;
import com.cleanrepo.survey.*;
import com.cleanrepo.answer.Answer;
import com.cleanrepo.auth.JwtService;
import com.cleanrepo.auth.exception.InvalidJwtException;
import com.cleanrepo.auth.exception.UserEmailNotFoundException;
import com.cleanrepo.auth.value_object.Jwt;
import com.cleanrepo.email.EmailService;
import com.cleanrepo.enums.ResponseStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@CrossOrigin(origins = {"http://localhost:5173"})
@RequiredArgsConstructor
class AccountController {

    private final AccountCommandHandler accountCommandHandler;
    private final JwtService jwtService;
    private final AccountRepository accountRepository;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final AccountService accountService;
    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final SurveyWithChosenQuestionsRepository surveyWithChosenQuestionsRepository;
    private final SubmittedSurveyRepository submittedSurveyRepository;

    @GetMapping("/test")
    public ResponseEntity<Object> test() {
        return ResponseEntity.status(200).body("TEST");
    }

    @PostMapping("/user/sign-in")
    ResponseEntity<Object> login(@RequestBody RegisterCommand.Json requestJson) {
        Optional<Account> account = accountRepository.findByEmail_Email(requestJson.email());
        if (account.isEmpty()) {
            return ResponseEntity.status(400)
                .body(new SignInResponse(ResponseStatus.BAD_REQUEST, "USER_NOT_REGISTERED", null, null));
        }
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(requestJson.email(),
                                                                                       requestJson.password()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new SignInResponse(ResponseStatus.UNAUTHORIZED, "WRONG_DATA", null, null));
        }
        if (account.get().getLocked().isLocked()) {
            return ResponseEntity.status(401).body(new SignInResponse(ResponseStatus.UNAUTHORIZED, "USER_LOCKED", null, null));
        }
        Optional<Jwt> jwt = jwtService.generateJwt(new AccountCredentials(account.get().getAccountId(),
                                                                          account.get().getEmail(),
                                                                          account.get().getEncodedPassword(),
                                                                          account.get().getRole(),
                                                                          account.get().getLocked()));
        String token = jwt.orElseThrow().jwt();
        return ResponseEntity.status(200)
            .body(new SignInResponse(ResponseStatus.ACCEPTED, "CORRECT_LOGIN_DATA", token, account.get().getRole()));
    }

    @PostMapping("/admin/create-host")
    ResponseEntity<Object> registerHost(@RequestHeader("Authorization") String bearerToken,
                                        @RequestBody HostRegisterCommand.Json requestJson) {
        var registerCommand = requestJson.toCommand();
        Account admin;
        try {
            admin = accountService.getAccountFromToken(bearerToken);
        } catch (InvalidJwtException e) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_JWT_TOKEN", null));
        }
        if (!UserRole.ROLE_ADMIN.equals(admin.getRole())) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_USER_ROLE", null));
        }
        Account account;
        try {
            account = accountCommandHandler.handleRegisterHost(registerCommand);
        } catch (EmailTakenException e) {
            return ResponseEntity.status(400).body(new SignUpResponse(ResponseStatus.BAD_REQUEST, "USER_ALREADY_ADDED", null));
        }
        Optional<Jwt> jwt = jwtService.generateJwt(new AccountCredentials(account.getAccountId(),
                                                                          account.getEmail(),
                                                                          account.getEncodedPassword(),
                                                                          account.getRole(),
                                                                          account.getLocked()));
        if (jwt.isEmpty()) {
            return ResponseEntity.status(400)
                .body(new SignUpResponse(ResponseStatus.BAD_REQUEST, "ERROR_WHILE_GENERATING_TOKEN", null));
        }
        UUID randomUUID = UUID.randomUUID();
        account.setConfirmationToken(randomUUID.toString().replaceAll("_", ""));
        String link = "http://url/set-password?token=" + randomUUID.toString().replaceAll("_", "") + "&email="
            + account.getEmail().getEmail();
        emailService.sendEmail(account.getEmail().getEmail(), "test@gmail.com", link);
        account.setAdmin(admin);
        accountRepository.save(account);
        return ResponseEntity.status(200).body(new SignUpResponse(ResponseStatus.ACCEPTED, "USER_SUCCESSFULLY_CREATED", null));
    }

    @PostMapping("/host/create-user")
    ResponseEntity<Object> registerUserOrVerifier(@RequestHeader("Authorization") String bearerToken,
                                                  @RequestBody UserRegisterCommand.Json requestJson) {
        var registerCommand = requestJson.toCommand();
        Account host;
        try {
            host = accountService.getAccountFromToken(bearerToken);
        } catch (InvalidJwtException e) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_JWT_TOKEN", null));
        }
        if (!UserRole.ROLE_HOST.equals(host.getRole())) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_USER_ROLE", null));
        }
        Account account;
        try {
            account = accountCommandHandler.handleRegisterUser(registerCommand);
        } catch (EmailTakenException e) {
            return ResponseEntity.status(400).body(new SignUpResponse(ResponseStatus.BAD_REQUEST, "USER_ALREADY_ADDED", null));
        }
        Optional<Jwt> jwt = jwtService.generateJwt(new AccountCredentials(account.getAccountId(),
                                                                          account.getEmail(),
                                                                          account.getEncodedPassword(),
                                                                          account.getRole(),
                                                                          account.getLocked()));
        if (jwt.isEmpty()) {
            return ResponseEntity.status(400)
                .body(new SignUpResponse(ResponseStatus.BAD_REQUEST, "ERROR_WHILE_GENERATING_TOKEN", null));
        }
        UUID randomUUID = UUID.randomUUID();
        account.setConfirmationToken(randomUUID.toString().replaceAll("_", ""));
        String link = "http://url/set-password?token=" + randomUUID.toString().replaceAll("_", "") + "&email="
            + account.getEmail().getEmail();
        emailService.sendEmail(account.getEmail().getEmail(), "test@gmail.com", link);
        account.setHost(host);
        accountRepository.save(account);
        return ResponseEntity.status(200).body(new SignUpResponse(ResponseStatus.ACCEPTED, "USER_SUCCESSFULLY_CREATED", null));
    }

    @PostMapping("/user/set-password")
    ResponseEntity<Object> setPassword(@RequestBody SetPasswordCommand.Json requestJson) {
        var registerCommand = requestJson.toCommand();
        Account account;
        try {
            account = accountCommandHandler.handleSetPassword(registerCommand);
        } catch (UserEmailNotFoundException e) {
            return ResponseEntity.status(400).body(new SignUpResponse(ResponseStatus.BAD_REQUEST, "INVALID_EMAIL", null));
        } catch (InvalidJwtException e) {
            return ResponseEntity.status(400).body(new SignUpResponse(ResponseStatus.BAD_REQUEST, "INVALID_CODE", null));
        }
        if (UserRole.ROLE_ADMIN.equals(account.getRole())) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_USER_ROLE", null));
        }
        Optional<Jwt> jwt = jwtService.generateJwt(new AccountCredentials(account.getAccountId(),
                                                                          account.getEmail(),
                                                                          account.getEncodedPassword(),
                                                                          account.getRole(),
                                                                          account.getLocked()));
        return jwt.<ResponseEntity<Object>>map(value -> ResponseEntity.status(200)
                .body(new SetPasswordResponse(ResponseStatus.ACCEPTED,
                        "PASSWORD_SUCCESSFULLY_SET",
                        value.jwt(),
                        account.getRole()))).orElseGet(() -> ResponseEntity.status(400)
                .body(new SignUpResponse(ResponseStatus.BAD_REQUEST, "ERROR_WHILE_GENERATING_TOKEN", null)));
    }

    @GetMapping("/host/get-user-list")
    ResponseEntity<Object> getHostUserList(@RequestHeader("Authorization") String bearerToken) {
        Account host;
        try {
            host = accountService.getAccountFromToken(bearerToken);
        } catch (InvalidJwtException | UserEmailNotFoundException e) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_JWT_TOKEN", null));
        }
        if (!host.getRole().equals(UserRole.ROLE_HOST)) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_USER_ROLE", null));
        }
        List<Account> accounts = accountRepository.findAllUsersConnectedToHostN1(host.getAccountId());
        return ResponseEntity.status(200)
            .body(accounts.stream()
                      .map(account -> new HostAccountDto(account.getAccountId(),
                                                         account.getCreatedAt(),
                                                         accountService.getOptionalName(account)
                                                             .map(Username::getUsername)
                                                             .orElse(null),
                                                         account.getEmail().getEmail(),
                                                         account.getSubmittedSurveys().size(),
                                                         account.getRole()))
                      .collect(Collectors.toList()));
    }

    @GetMapping("/admin/get-user-list")
    ResponseEntity<Object> getAdminUserList(@RequestHeader("Authorization") String bearerToken) {
        Account admin;
        try {
            admin = accountService.getAccountFromToken(bearerToken);
        } catch (InvalidJwtException | UserEmailNotFoundException e) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_JWT_TOKEN", null));
        }
        if (!UserRole.ROLE_ADMIN.equals(admin.getRole())) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_USER_ROLE", null));
        }
        List<Account> accounts = accountRepository.findAllUsersConnectedToAdminN1(admin.getAccountId());
        return ResponseEntity.status(200)
            .body(accounts.stream()
                      .map(account -> new AccountDto(account.getAccountId(),
                                                     account.getCreatedAt(),
                                                     accountService.getOptionalName(account)
                                                         .map(Username::getUsername)
                                                         .orElse(null),
                                                     account.getEmail().getEmail(),
                                                     account.getHostSurveys()
                                                             .stream()
                                                             .mapToInt(surveyWithChosenQuestions -> surveyWithChosenQuestions.getSubmittedSurveys().size())
                                                             .sum()))
                      .collect(Collectors.toList()));
    }

    @PostMapping("/admin/create-survey")
    ResponseEntity<Object> createSurvey(@RequestHeader("Authorization") String bearerToken, @RequestBody SurveyDataDto survey) {
        Account admin;
        try {
            admin = accountService.getAccountFromToken(bearerToken);
        } catch (InvalidJwtException e) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_JWT_TOKEN", null));
        }
        if (!UserRole.ROLE_ADMIN.equals(admin.getRole())) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_USER_ROLE", null));
        }
        List<String> questions = survey.surveyData().getQuestionList().stream().map(Question::getQuestion).toList();
        Set<String> set = new HashSet<>(questions);

        if (set.size() < questions.size()) {
            return ResponseEntity.status(400).body(new SignUpResponse(ResponseStatus.BAD_REQUEST, "DUPLICATE_QUESTIONS", null));
        }
        survey.surveyData().setAssignedDate(LocalDate.now());
        survey.surveyData().setStatus(Status.OPEN);
        surveyRepository.save(survey.surveyData());
        admin.getSurveys().add(survey.surveyData());
        System.out.println(admin.getSurveys().size());
        accountRepository.save(admin);
        System.out.println(admin.getSurveys().size());
        return ResponseEntity.status(200).body(new SignUpResponse(ResponseStatus.CREATED, "SURVEY_SUCCESSFULLY_CREATED", null));
    }

    @GetMapping("/host/get-survey-list")
    ResponseEntity<Object> getHostSurveys(@RequestHeader("Authorization") String bearerToken) {
        Account host;
        try {
            host = accountService.getAccountFromToken(bearerToken);
        } catch (InvalidJwtException e) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_JWT_TOKEN", null));
        }
        if (!UserRole.ROLE_HOST.equals(host.getRole())) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_USER_ROLE", null));
        }
        List<HostSurveyListDto> hostSurveyListDtos = new ArrayList<>();
        for (SurveyWithChosenQuestions surveyWithChosenQuestions : host.getHostSurveys()) {
            hostSurveyListDtos.add(new HostSurveyListDto(surveyWithChosenQuestions.getSurveyWithChosenQuestionsId(),
                                                         surveyWithChosenQuestions.getAssignedDate(),
                                                         surveyWithChosenQuestions.getSurvey().getName(),
                                                         host.getEmail().getEmail(),
                                                         surveyWithChosenQuestions.getSubmittedSurveys().size()));
        }
        return ResponseEntity.status(200).body(hostSurveyListDtos);
    }

    @GetMapping("/admin/get-survey-list")
    ResponseEntity<Object> getAdminSurveys(@RequestHeader("Authorization") String bearerToken) {
        Account admin;
        try {
            admin = accountService.getAccountFromToken(bearerToken);
        } catch (InvalidJwtException e) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_JWT_TOKEN", null));
        }
        if (!UserRole.ROLE_ADMIN.equals(admin.getRole())) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_USER_ROLE", null));
        }
        List<AdminSurveyListDto> adminSurveyListDtos = new ArrayList<>();
        for (Survey survey : admin.getSurveys()) {
            int size = 0;
            for (SurveyWithChosenQuestions surveyWithChosenQuestions : survey.getSurveyWithChosenQuestions()) {
                size += surveyWithChosenQuestions.getSubmittedSurveys().size();
            }
            adminSurveyListDtos.add(new AdminSurveyListDto(survey.getSurveyId(),
                                                           survey.getAssignedDate(),
                                                           survey.getName(),
                                                           size));
        }
        return ResponseEntity.status(200).body(adminSurveyListDtos);
    }

    @PostMapping("/admin/assign-survey-to-host")
    ResponseEntity<Object> assignSurveyToCompany(@RequestHeader("Authorization") String bearerToken,
                                                 @RequestBody SurveyToHostAssignmentDto assignment) {
        Account admin;
        try {
            admin = accountService.getAccountFromToken(bearerToken);
        } catch (InvalidJwtException e) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_JWT_TOKEN", null));
        }
        if (!UserRole.ROLE_ADMIN.equals(admin.getRole())) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_USER_ROLE", null));
        }
        Optional<Account> host = accountRepository.findById(assignment.companyId());
        if (host.isEmpty()) {
            return ResponseEntity.status(404).body(new SignUpResponse(ResponseStatus.NOT_FOUND, "COMPANY_NOT_FOUND", null));
        }
        for (SurveyWithChosenQuestions surveyWithChosenQuestions : host.get().getHostSurveys()) {
            if (surveyWithChosenQuestions.getSurvey().getSurveyId() == assignment.surveyId()) {
                return ResponseEntity.status(400)
                    .body(new SignUpResponse(ResponseStatus.BAD_REQUEST, "SURVEY_ALREADY_ASSIGNED", null));
            }
        }
        Optional<Survey> survey = surveyRepository.findById(assignment.surveyId());
        if (survey.isEmpty()) {
            return ResponseEntity.status(404).body(new SignUpResponse(ResponseStatus.NOT_FOUND, "SURVEY_NOT_FOUND", null));
        }
        List<Question> questions = questionRepository.findAllByIdN1(assignment.questionIds());
        SurveyWithChosenQuestions surveyWithChosenQuestions = new SurveyWithChosenQuestions(survey.get(),
                                                                                            LocalDate.now(),
                                                                                            questions);
        surveyWithChosenQuestions.setStatus(Status.OPEN);
        surveyWithChosenQuestions.setHost(host.get());
        host.get().getHostSurveys().add(surveyWithChosenQuestions);
        surveyWithChosenQuestionsRepository.save(surveyWithChosenQuestions);
        accountRepository.save(host.get());
        return ResponseEntity.status(200).body(new SignUpResponse(ResponseStatus.ACCEPTED, "SURVEY_SUCCESSFULLY_ASSIGNED", null));
    }

    @GetMapping("/admin/get-survey-list/{survey-id}")
    ResponseEntity<Object> getCompanyList(@RequestHeader("Authorization") String bearerToken,
                                          @PathVariable("survey-id") int surveyId) {
        Account admin;
        try {
            admin = accountService.getAccountFromToken(bearerToken);
        } catch (InvalidJwtException e) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_JWT_TOKEN", null));
        }
        if (!UserRole.ROLE_ADMIN.equals(admin.getRole())) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_USER_ROLE", null));
        }
        Optional<Survey> survey = surveyRepository.findById(surveyId);
        if (survey.isEmpty()) {
            return ResponseEntity.status(404).body(new SignUpResponse(ResponseStatus.NOT_FOUND, "SURVEY_NOT_FOUND", null));
        }
        List<SurveyWithChosenQuestions> surveys = surveyWithChosenQuestionsRepository.findAllByParentIdN1(surveyId);
        List<CompanyDto> companyDtos = new ArrayList<>();
        for (SurveyWithChosenQuestions surveyWithChosenQuestions : surveys) {
            companyDtos.add(new CompanyDto(surveyWithChosenQuestions.getHost().getAccountId(),
                                           surveyWithChosenQuestions.getAssignedDate(),
                                           accountService.getOptionalName(surveyWithChosenQuestions.getHost())
                                               .map(Username::getUsername)
                                               .orElse(null),
                                           surveyWithChosenQuestions.getHost().getSurveys().size()));
        }
        return ResponseEntity.status(200).body(new AllSurveyCompaniesDto(survey.get().getName(), companyDtos));
    }

    @PostMapping("/user/submit-survey/{survey-id}")
    ResponseEntity<Object> submitSurvey(@RequestHeader("Authorization") String bearerToken,
                                        @RequestBody SubmittedSurveyDto submittedSurvey,
                                        @PathVariable("survey-id") int surveyId) {
        Account user;
        try {
            user = accountService.getAccountFromToken(bearerToken);
        } catch (InvalidJwtException e) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_JWT_TOKEN", null));
        }
        if (!UserRole.ROLE_USER.equals(user.getRole())) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_USER_ROLE", null));
        }
        for (SubmittedSurvey survey : user.getSubmittedSurveys()) {
            if (survey.getSurvey().getSurveyWithChosenQuestionsId() == surveyId) {
                return ResponseEntity.status(400)
                    .body(new SignUpResponse(ResponseStatus.BAD_REQUEST, "ERROR_WHILE_SUBMITTING_SURVEY", null));
            }
        }
        Optional<SurveyWithChosenQuestions> surveyWithChosenQuestions = surveyWithChosenQuestionsRepository.findById(surveyId);
        if (surveyWithChosenQuestions.isEmpty()) {
            return ResponseEntity.status(404).body(new SignUpResponse(ResponseStatus.NOT_FOUND, "SURVEY_NOT_FOUND", null));
        }
        submittedSurvey.object()
            .setSurvey(surveyWithChosenQuestions.get());
        submittedSurvey.object().setSubmitter(user);
        boolean atLeastOneOpenQuestion = false;
        for (Question question : surveyWithChosenQuestions.get().getQuestionList()) {
            if (Type.OPEN.equals(question.getType())) {
                atLeastOneOpenQuestion = true;
            } else if (Type.ABCD.equals(question.getType())) {
                for (SubmittedQuestion submittedQuestion : submittedSurvey.object().getQuestionList()) {
                    if (submittedQuestion.getQuestion().equals(question.getQuestion())) {
                        if (submittedQuestion.getSelectedAnswerId() == question.getCorrectAnswerId()) {
                            submittedQuestion.setCorrect(true);
                        }
                        break;
                    }
                }
            }
        }
        submittedSurvey.object().setStatus(Status.CLOSED);
        if (atLeastOneOpenQuestion) {
            submittedSurvey.object().setStatus(Status.TO_BE_VERIFIED);
        }
        user.getSubmittedSurveys().add(submittedSurvey.object());
        submittedSurveyRepository.save(submittedSurvey.object());
        accountRepository.save(user);
        return ResponseEntity.status(200)
            .body(new SignUpResponse(ResponseStatus.ACCEPTED, "SURVEY_SUCCESSFULLY_SUBMITTED", null));
    }

    @GetMapping("/user/get-survey-details/{survey-id}")
    ResponseEntity<Object> getSurveyDetails(@RequestHeader("Authorization") String bearerToken,
                                            @PathVariable("survey-id") int surveyId) {
        Account user;
        try {
            user = accountService.getAccountFromToken(bearerToken);
        } catch (InvalidJwtException e) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_JWT_TOKEN", null));
        }
        if (!UserRole.ROLE_USER.equals(user.getRole())) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_USER_ROLE", null));
        }
        Optional<SurveyWithChosenQuestions> surveyWithChosenQuestions = surveyWithChosenQuestionsRepository.findById(surveyId);
        if (surveyWithChosenQuestions.isEmpty()) {
            return ResponseEntity.status(404).body(new SignUpResponse(ResponseStatus.NOT_FOUND, "SURVEY_NOT_FOUND", null));
        }
        List<QuestionDto> questionDtos = new ArrayList<>();
        for (Question question : surveyWithChosenQuestions.get().getQuestionList()) {
            List<AnswerDto> answerDtos = question.getAnswers()
                .stream()
                .map(answer -> new AnswerDto(answer.getAnswerId(), answer.getContent()))
                .collect(Collectors.toList());
            questionDtos.add(new QuestionDto(question.getQuestion(), answerDtos, question.getHint(), question.getType()));
        }
        return ResponseEntity.status(200)
            .body(new SurveyDto(surveyWithChosenQuestions.get().getSurvey().getName(), questionDtos));
    }

    @GetMapping("/user/get-survey-list")
    ResponseEntity<Object> getSurveyList(@RequestHeader("Authorization") String bearerToken) {
        Account user;
        try {
            user = accountService.getAccountFromToken(bearerToken);
        } catch (InvalidJwtException e) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_JWT_TOKEN", null));
        }
        if (!UserRole.ROLE_USER.equals(user.getRole()) && !UserRole.ROLE_VERIFIER.equals(user.getRole())) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_USER_ROLE", null));
        }
        List<SurveyForSurveyListDto> surveyForSurveyListDtos = new ArrayList<>();
        if (UserRole.ROLE_VERIFIER.equals(user.getRole())) {
            for (SurveyWithChosenQuestions surveyWithChosenQuestions : user.getHost().getHostSurveys()) {
                for (SubmittedSurvey survey : surveyWithChosenQuestions.getSubmittedSurveys()) {
                    if (survey.getVerifier() != null && user.getAccountId() == survey.getVerifier().getAccountId()) {
                        surveyForSurveyListDtos.add(new SurveyForSurveyListDto(survey.getSubmittedSurveyId(),
                                                                               survey.getSurvey().getSurvey().getName(),
                                                                               surveyWithChosenQuestions.getAssignedDate(),
                                                                               survey.getStatus()));
                        continue;
                    }
                    boolean atLeastOneOpenQuestionFlag = false;
                    for (Question question : surveyWithChosenQuestions.getQuestionList()) {
                        if (Type.OPEN.equals(question.getType())) {
                            atLeastOneOpenQuestionFlag = true;
                            break;
                        }
                    }
                    if (atLeastOneOpenQuestionFlag && Status.TO_BE_VERIFIED.equals(survey.getStatus())) {
                        surveyForSurveyListDtos.add(new SurveyForSurveyListDto(survey.getSubmittedSurveyId(),
                                                                               survey.getSurvey().getSurvey().getName(),
                                                                               surveyWithChosenQuestions.getAssignedDate(),
                                                                               survey.getStatus()));
                    }
                }
            }
        } else if (UserRole.ROLE_USER.equals(user.getRole())) {
            List<SurveyWithChosenQuestions> hostSurveys = user.getHost().getHostSurveys();
            for (SubmittedSurvey survey : user.getSubmittedSurveys()) {
                surveyForSurveyListDtos.add(new SurveyForSurveyListDto(survey.getSubmittedSurveyId(),
                                                                       survey.getSurvey().getSurvey().getName(),
                                                                       survey.getSurvey().getAssignedDate(),
                                                                       survey.getStatus()));
            }
            for (SurveyWithChosenQuestions survey : hostSurveys) {
                boolean flag = false;
                for (SubmittedSurvey submittedSurvey : user.getSubmittedSurveys()) {
                    if (submittedSurvey.getSurvey().getSurveyWithChosenQuestionsId() == survey.getSurveyWithChosenQuestionsId()) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    continue;
                }
                surveyForSurveyListDtos.add(new SurveyForSurveyListDto(survey.getSurveyWithChosenQuestionsId(),
                                                                       survey.getSurvey().getName(),
                                                                       survey.getAssignedDate(),
                                                                       survey.getStatus()));
            }
        } else {
            ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_USER_ROLE", null));
        }
        return ResponseEntity.status(200).body(surveyForSurveyListDtos);
    }

    @GetMapping("/admin/get-survey-results/{survey-id}/{company-id}")
    ResponseEntity<Object> getSurveyResults(@RequestHeader("Authorization") String bearerToken,
                                            @PathVariable("survey-id") int surveyId,
                                            @PathVariable("company-id") int hostId) {
        Account admin;
        try {
            admin = accountService.getAccountFromToken(bearerToken);
        } catch (InvalidJwtException e) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_JWT_TOKEN", null));
        }
        if (!UserRole.ROLE_ADMIN.equals(admin.getRole())) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_USER_ROLE", null));
        }
        Optional<SurveyWithChosenQuestions> surveyWithChosenQuestions = surveyWithChosenQuestionsRepository.findByParentId(
            surveyId, hostId);
        if (surveyWithChosenQuestions.isEmpty()) {
            ResponseEntity.status(404).body(new SignUpResponse(ResponseStatus.NOT_FOUND, "SURVEY_NOT_FOUND", null));
        }
        Optional<Account> host = accountRepository.findById(hostId);
        if (host.isEmpty()) {
            ResponseEntity.status(404).body(new SignUpResponse(ResponseStatus.NOT_FOUND, "HOST_NOT_FOUND", null));
        }
        List<ResultQuestionDto> resultQuestionDtos = new ArrayList<>();
        int allAnswersCounter = 0;
        float correctAnswersCounter = 0.0f;
        for (Question question : surveyWithChosenQuestions.get().getQuestionList()) {
            List<ResultAnswerDto> resultAnswerDtos = new ArrayList<>();
            for (SubmittedSurvey submittedSurvey : surveyWithChosenQuestions.get().getSubmittedSurveys()) {
                for (SubmittedQuestion submittedQuestion : submittedSurvey.getQuestionList()) {
                    if (question.getQuestion().equals(submittedQuestion.getQuestion())) {
                        if (question.getType().equals(Type.ABCD)) {
                            if (submittedQuestion.getSelectedAnswerId() == question.getCorrectAnswerId()
                                || submittedQuestion.isCorrect()) {
                                correctAnswersCounter++;
                            }
                            String content = "";
                            for (Answer answer : question.getAnswers()) {
                                if (answer.getId() == submittedQuestion.getSelectedAnswerId()) {
                                    content = answer.getContent();
                                    break;
                                }
                            }
                            resultAnswerDtos.add(new ResultAnswerDto(accountService.getOptionalName(submittedSurvey.getSubmitter())
                                                                         .map(Username::getUsername)
                                                                         .orElse(null), content));
                        } else if (question.getType().equals(Type.OPEN)) {
                            if (submittedQuestion.getAnswerContent().equals(question.getModelAnswer())
                                || submittedQuestion.isCorrect()) {
                                correctAnswersCounter++;
                            }
                            resultAnswerDtos.add(new ResultAnswerDto(accountService.getOptionalName(submittedSurvey.getSubmitter())
                                                                         .map(Username::getUsername)
                                                                         .orElse(null), submittedQuestion.getAnswerContent()));
                        }
                        allAnswersCounter++;
                        break;
                    }
                }
            }
            if (question.getType().equals(Type.ABCD)) {
                String content = "";
                for (Answer answer : question.getAnswers()) {
                    if (answer.getId() == question.getCorrectAnswerId()) {
                        content = answer.getContent();
                        break;
                    }
                }
                resultQuestionDtos.add(new ResultQuestionDto(question.getQuestion(), content, resultAnswerDtos));
            } else if (question.getType().equals(Type.OPEN)) {
                resultQuestionDtos.add(new ResultQuestionDto(question.getQuestion(),
                                                             question.getModelAnswer(),
                                                             resultAnswerDtos));
            }
        }
        float score = Math.round(correctAnswersCounter * 100 / allAnswersCounter);
        return ResponseEntity.status(200).body(new ResultDto((int) score, resultQuestionDtos));
    }

    @GetMapping("/user/get-survey-results-for-verification/{survey-id}")
    ResponseEntity<Object> getSurveyResultsForVerification(@RequestHeader("Authorization") String bearerToken,
                                                           @PathVariable("survey-id") int surveyId) {
        Account verifier;
        try {
            verifier = accountService.getAccountFromToken(bearerToken);
        } catch (InvalidJwtException e) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_JWT_TOKEN", null));
        }
        if (!UserRole.ROLE_VERIFIER.equals(verifier.getRole())) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_USER_ROLE", null));
        }
        List<VerificationDto> verificationDtos = new ArrayList<>();
        Optional<SubmittedSurvey> submittedSurvey = submittedSurveyRepository.findById(surveyId);
        if (submittedSurvey.isEmpty()) {
            return ResponseEntity.status(404).body(new SignUpResponse(ResponseStatus.NOT_FOUND, "SURVEY_NOT_FOUND", null));
        }

        for (Question question : submittedSurvey.get().getSurvey().getQuestionList()) {
            for (SubmittedQuestion submittedQuestion : submittedSurvey.get().getQuestionList()) {
                if (question.getQuestion().equals(submittedQuestion.getQuestion())) {
                    if(Type.OPEN.equals(question.getType())) {
                        verificationDtos.add(new VerificationDto(question.getQuestion(),
                                question.getModelAnswer(),
                                submittedQuestion.getAnswerContent(),
                                question.getType(),
                                null));
                    }
                    if(Type.ABCD.equals(question.getType())) {
                        String content = "";
                        List<String> answers = new ArrayList<>();
                        for (Answer answer : submittedQuestion.getAnswers()) {
                            answers.add(answer.getContent());
                            if (answer.getId() == submittedQuestion.getSelectedAnswerId()) {
                                content = answer.getContent();
                            }
                        }
                        String correctContent = "";
                        for(Answer answer : question.getAnswers()) {
                            if(answer.getId() == question.getCorrectAnswerId()) {
                                correctContent = answer.getContent();
                            }
                        }
                        verificationDtos.add(new VerificationDto(question.getQuestion(),
                                correctContent,
                                content,
                                question.getType(),
                                answers));
                    }
                    break;
                }
            }
        }
        return ResponseEntity.status(200)
            .body(new ListVerificationDto(verificationDtos, submittedSurvey.get().getSurvey().getSurvey().getName()));
    }

    @PostMapping("/user/verify-survey/{survey-id}")
    ResponseEntity<Object> verifySurvey(@RequestHeader("Authorization") String bearerToken,
                                        @PathVariable("survey-id") int surveyId,
                                        @RequestBody VerificationObject verificationResultDto) {
        Account verifier;
        try {
            verifier = accountService.getAccountFromToken(bearerToken);
        } catch (InvalidJwtException e) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_JWT_TOKEN", null));
        }
        if (!UserRole.ROLE_VERIFIER.equals(verifier.getRole())) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_USER_ROLE", null));
        }
        Optional<SubmittedSurvey> submittedSurvey = submittedSurveyRepository.findById(surveyId);
        if (submittedSurvey.isEmpty()) {
            return ResponseEntity.status(404).body(new SignUpResponse(ResponseStatus.NOT_FOUND, "SURVEY_NOT_FOUND", null));
        }
        for (SubmittedQuestion submittedQuestion : submittedSurvey.get().getQuestionList()) {
            for (VerifiedDto verifiedDto : verificationResultDto.object().questionList()) {
                if (submittedQuestion.getQuestion().equals(verifiedDto.question())) {
                    submittedQuestion.setCorrect(verifiedDto.isAnswerCorrect());
                    if(Type.ABCD.equals(verifiedDto.type())) {
                        for(Answer answer : submittedQuestion.getAnswers()) {
                            if(answer.getContent().equals(verifiedDto.answerContent())) {
                                submittedQuestion.setSelectedAnswerId(answer.getId());
                                break;
                            }
                        }
                    }
                    else if(Type.OPEN.equals(verifiedDto.type())) {
                        submittedQuestion.setAnswerContent(verifiedDto.answerContent());
                    }
                }
            }
        }
        submittedSurvey.get().setStatus(Status.CLOSED);
        submittedSurvey.get().setVerifier(verifier);
        submittedSurveyRepository.save(submittedSurvey.get());
        return ResponseEntity.status(200).body(new SignUpResponse(ResponseStatus.ACCEPTED, "SURVEY_SUCCESSFULLY_VERIFIED", null));
    }

    @GetMapping("/user/get-user-data")
    ResponseEntity<Object> getUserData(@RequestHeader("Authorization") String bearerToken) {
        Account user;
        try {
            user = accountService.getAccountFromToken(bearerToken);
        } catch (InvalidJwtException e) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_JWT_TOKEN", null));
        }
        return ResponseEntity.status(200).body(new UserDataDto(user.getRole(), user.getEmail().getEmail()));
    }

    @GetMapping("/admin/download-results/{survey-id}/{company-id}")
    ResponseEntity<Object> generateInvoice(@RequestHeader("Authorization") String bearerToken,
                                           @PathVariable("survey-id") int surveyId,
                                           @PathVariable("company-id") int hostId) {
        Account admin;
        try {
            admin = accountService.getAccountFromToken(bearerToken);
        } catch (InvalidJwtException e) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_JWT_TOKEN", null));
        }
        if (!UserRole.ROLE_ADMIN.equals(admin.getRole())) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_USER_ROLE", null));
        }
        Optional<SurveyWithChosenQuestions> surveyWithChosenQuestions = surveyWithChosenQuestionsRepository.findByParentId(
            surveyId, hostId);
        if (surveyWithChosenQuestions.isEmpty()) {
            ResponseEntity.status(404).body(new SignUpResponse(ResponseStatus.NOT_FOUND, "SURVEY_NOT_FOUND", null));
        }
        Optional<Account> host = accountRepository.findById(hostId);
        if (host.isEmpty()) {
            ResponseEntity.status(404).body(new SignUpResponse(ResponseStatus.NOT_FOUND, "HOST_NOT_FOUND", null));
        }

        try {
            ResultDto resultDto = accountService.getResults(surveyWithChosenQuestions.get());
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            String jsonData = gson.toJson(resultDto);

            ByteArrayResource resource = new ByteArrayResource(jsonData.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", "data.json");

            return ResponseEntity.ok().headers(headers).body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/admin/download-results-csv/{survey-id}/{company-id}")
    public ResponseEntity<Object> downloadResultsCsv(@RequestHeader("Authorization") String bearerToken,
                                                     @PathVariable("survey-id") int surveyId,
                                                     @PathVariable("company-id") int hostId) {
        Account admin;
        try {
            admin = accountService.getAccountFromToken(bearerToken);
        } catch (InvalidJwtException e) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_JWT_TOKEN", null));
        }
        if (!UserRole.ROLE_ADMIN.equals(admin.getRole())) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_USER_ROLE", null));
        }
        Optional<SurveyWithChosenQuestions> surveyWithChosenQuestions = surveyWithChosenQuestionsRepository.findByParentId(
            surveyId, hostId);
        if (surveyWithChosenQuestions.isEmpty()) {
            ResponseEntity.status(404).body(new SignUpResponse(ResponseStatus.NOT_FOUND, "SURVEY_NOT_FOUND", null));
        }
        Optional<Account> host = accountRepository.findById(hostId);
        if (host.isEmpty()) {
            ResponseEntity.status(404).body(new SignUpResponse(ResponseStatus.NOT_FOUND, "HOST_NOT_FOUND", null));
        }

        try {
            ResultDto resultDto = accountService.getResults(surveyWithChosenQuestions.get());

            StringBuilder csvContent = new StringBuilder();

            csvContent.append("Score,Question,Correct Answer,Username,Answer\n");

            System.out.println("GENERATING CSV");
            for (ResultQuestionDto question : resultDto.questions()) {
                for (ResultAnswerDto answer : question.answers()) {
                    String scoreAsString = "";
                    if (resultDto.score() != null) {
                        try {
                            scoreAsString = String.valueOf(resultDto.score());
                        } catch (NumberFormatException e) {
                            scoreAsString = "";
                        }
                    }
                    System.out.println("ADDING DATA");
                    System.out.println(question.question());
                    csvContent.append(scoreAsString)
                        .append(',')
                        .append(question.question())
                        .append(',')
                        .append(question.correctAnswer())
                        .append(',')
                        .append(answer.username())
                        .append(',')
                        .append(answer.answer())
                        .append('\n');
                }
            }

            ByteArrayResource resource = new ByteArrayResource(csvContent.toString().getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "data.csv");

            return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/admin/get-survey-details-to-assign/{survey-id}")
    ResponseEntity<Object> getSurveyDetailsToAssign(@RequestHeader("Authorization") String bearerToken,
                                                    @PathVariable("survey-id") int surveyId) {
        Account admin;
        try {
            admin = accountService.getAccountFromToken(bearerToken);
        } catch (InvalidJwtException | UserEmailNotFoundException e) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_JWT_TOKEN", null));
        }
        if (!UserRole.ROLE_ADMIN.equals(admin.getRole())) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_USER_ROLE", null));
        }
        Optional<Survey> survey = surveyRepository.findById(surveyId);
        if (survey.isEmpty()) {
            return ResponseEntity.status(404).body(new SignUpResponse(ResponseStatus.NOT_FOUND, "SURVEY_NOT_FOUND", null));
        }
        List<QuestionDetailsDto> questionDetailsDtos = new ArrayList<>();
        for (Question question : survey.get().getQuestionList()) {
            questionDetailsDtos.add(new QuestionDetailsDto(question.getQuestionId(), question.getQuestion()));
        }
        return ResponseEntity.status(200).body(new SurveyDetailsDto(survey.get().getName(), questionDetailsDtos));
    }

    @GetMapping("/admin/get-survey-result-list/{survey-id}")
    ResponseEntity<Object> getSurveyResultList(@RequestHeader("Authorization") String bearerToken,
                                               @PathVariable("survey-id") int surveyId) {
        Account admin;
        try {
            admin = accountService.getAccountFromToken(bearerToken);
        } catch (InvalidJwtException | UserEmailNotFoundException e) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_JWT_TOKEN", null));
        }
        if (!UserRole.ROLE_ADMIN.equals(admin.getRole())) {
            return ResponseEntity.status(403).body(new SignUpResponse(ResponseStatus.FORBIDDEN, "INVALID_USER_ROLE", null));
        }
        Optional<Survey> survey = surveyRepository.findById(surveyId);
        if (survey.isEmpty()) {
            return ResponseEntity.status(404).body(new SignUpResponse(ResponseStatus.NOT_FOUND, "SURVEY_NOT_FOUND", null));
        }
        List<SurveyWithChosenQuestions> surveysWithChosenQuestions = surveyWithChosenQuestionsRepository.findAllByParentIdN1(
            surveyId);
        List<SurveyDetailsForAdminDto> surveyDetailsForAdminDtos = new ArrayList<>();
        for (SurveyWithChosenQuestions surveyWithChosenQuestions : surveysWithChosenQuestions) {
            surveyDetailsForAdminDtos.add(new SurveyDetailsForAdminDto(surveyWithChosenQuestions.getHost().getAccountId(),
                                                                       accountService.getOptionalName(surveyWithChosenQuestions.getHost())
                                                                           .map(Username::getUsername)
                                                                           .orElse(null),
                                                                       surveyWithChosenQuestions.getSubmittedSurveys().size()));
        }
        return ResponseEntity.status(200).body(surveyDetailsForAdminDtos);
    }
}
