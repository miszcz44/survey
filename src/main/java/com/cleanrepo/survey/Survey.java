package com.cleanrepo.survey;

import com.cleanrepo.account.Account;
import com.cleanrepo.question.Question;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "survey")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int surveyId;

    private String name;

    private Status status;

    private LocalDate assignedDate;
    @ManyToOne
    @JoinColumn(name = "account_id")
    Account admin;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "survey_id")
    private List<SurveyWithChosenQuestions> surveyWithChosenQuestions;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "survey_id")
    private List<Question> questionList;
}
