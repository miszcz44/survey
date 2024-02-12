package com.cleanrepo.question;

import com.cleanrepo.answer.Answer;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "question")
@Getter
@Setter(AccessLevel.PACKAGE)
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int questionId;

    private String question;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "question_id")
    private List<Answer> answers;

    private String hint;

    private String modelAnswer;

    private int correctAnswerId;

    @Enumerated(EnumType.STRING)
    private Type type;
}
