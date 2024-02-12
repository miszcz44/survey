package com.cleanrepo.survey;

import com.cleanrepo.account.Account;
import com.cleanrepo.question.SubmittedQuestion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "submitted_survey")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmittedSurvey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int submittedSurveyId;

    @ManyToOne
    @JoinColumn(name = "survey_with_chosen_questions_id")
    private SurveyWithChosenQuestions survey;

    private String name;

    private Status status;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account submitter;

    @ManyToOne
    private Account verifier;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "submitted_survey_id")
    private List<SubmittedQuestion> questionList = new ArrayList<>();
}
