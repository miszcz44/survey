package com.cleanrepo.survey;

import com.cleanrepo.account.Account;
import com.cleanrepo.question.Question;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "survey_with_chosen_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyWithChosenQuestions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int surveyWithChosenQuestionsId;

    @ManyToOne
    @JoinColumn(name = "survey_id")
    private Survey survey;

    private LocalDate assignedDate;

    private Status status;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "survey_with_chosen_questions_id")
    private List<Question> questionList;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "survey_with_chosen_questions_id")
    private List<SubmittedSurvey> submittedSurveys;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account host;

    public SurveyWithChosenQuestions(Survey survey, LocalDate assignedDate, List<Question> questionList) {
        this.survey = survey;
        this.assignedDate = assignedDate;
        this.questionList = questionList;
    }
}
