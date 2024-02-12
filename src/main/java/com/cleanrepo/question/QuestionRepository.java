package com.cleanrepo.question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.answers WHERE q.questionId IN ?1")
    List<Question> findAllByIdN1(List<Integer> ids);
}
