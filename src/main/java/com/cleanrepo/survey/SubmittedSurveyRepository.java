package com.cleanrepo.survey;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmittedSurveyRepository extends JpaRepository<SubmittedSurvey, Integer> {
    @Query("SELECT s FROM SubmittedSurvey s LEFT JOIN FETCH s.questionList WHERE s.status = ?1")
    List<SubmittedSurvey> findAllToBeVerifiedSurveysN1(Status status);

    @Query("SELECT s FROM SubmittedSurvey s LEFT JOIN FETCH s.questionList WHERE s.verifier.accountId = ?1")
    List<SubmittedSurvey> findAllSpecificVerifierClosedSurveysN1(int verifierId);
}
