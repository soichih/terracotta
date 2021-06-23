package edu.iu.terracotta.repository;

import edu.iu.terracotta.model.app.QuestionSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionSubmissionRepository extends JpaRepository<QuestionSubmission, Long> {

    List<QuestionSubmission> findBySubmission_SubmissionId(Long submissionId);

    QuestionSubmission findByQuestionSubmissionId(Long questionSubmissionId);

    boolean existsBySubmission_Assessment_AssessmentIdAndQuestion_QuestionId(Long assessmentId, Long questionId);

    boolean existsBySubmission_Assessment_AssessmentIdAndSubmission_SubmissionIdAndQuestionSubmissionId(Long assessmentId, Long submissionId, Long questionSubmissionId);
}