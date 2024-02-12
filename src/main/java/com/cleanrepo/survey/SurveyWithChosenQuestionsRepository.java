package com.cleanrepo.survey;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyWithChosenQuestionsRepository extends JpaRepository<SurveyWithChosenQuestions, Integer> {
    @Query("SELECT s FROM SurveyWithChosenQuestions s LEFT JOIN FETCH s.questionList WHERE s.survey.surveyId = ?1")
    List<SurveyWithChosenQuestions> findAllByParentIdN1(int surveyId);

    @Query("SELECT s FROM SurveyWithChosenQuestions s  WHERE s.survey.surveyId = ?1 AND s.host.accountId = ?2")
    Optional<SurveyWithChosenQuestions> findByParentId(int surveyId, int hostId);
}
