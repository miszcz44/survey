package com.cleanrepo.question;

import com.cleanrepo.answer.Answer;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "submitted_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmittedQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int submittedQuestionId;

    private String question;

    private Type type;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "submitted_question_id")
    private List<Answer> answers;

    private String hint;

    private String answerContent;

    private int selectedAnswerId;

    boolean isCorrect;
}
