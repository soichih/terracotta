package edu.iu.terracotta.service.app.impl;

import edu.iu.terracotta.exceptions.ConnectionException;
import edu.iu.terracotta.exceptions.DataServiceException;
import edu.iu.terracotta.model.ags.Score;
import edu.iu.terracotta.model.app.Assessment;
import edu.iu.terracotta.model.app.Assignment;
import edu.iu.terracotta.model.app.Experiment;
import edu.iu.terracotta.model.app.Participant;
import edu.iu.terracotta.model.app.QuestionSubmission;
import edu.iu.terracotta.model.app.Submission;
import edu.iu.terracotta.model.app.SubmissionComment;
import edu.iu.terracotta.model.app.dto.QuestionSubmissionDto;
import edu.iu.terracotta.model.app.dto.SubmissionCommentDto;
import edu.iu.terracotta.model.app.dto.SubmissionDto;
import edu.iu.terracotta.model.oauth2.LTIToken;
import edu.iu.terracotta.model.oauth2.SecurityInfo;
import edu.iu.terracotta.repository.AllRepositories;
import edu.iu.terracotta.service.app.AssessmentService;
import edu.iu.terracotta.service.app.AssignmentService;
import edu.iu.terracotta.service.app.QuestionSubmissionService;
import edu.iu.terracotta.service.app.SubmissionCommentService;
import edu.iu.terracotta.service.app.SubmissionService;
import edu.iu.terracotta.service.lti.AdvantageAGSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class SubmissionServiceImpl implements SubmissionService {

    @Autowired
    AllRepositories allRepositories;

    @Autowired
    QuestionSubmissionService questionSubmissionService;

    @Autowired
    SubmissionCommentService submissionCommentService;

    @Autowired
    AssignmentService assignmentService;

    @Autowired
    AssessmentService assessmentService;

    @Autowired
    AdvantageAGSService advantageAGSService;

    @Override
    public List<Submission> findAllByAssessmentId(Long assessmentId) {
        return allRepositories.submissionRepository.findByAssessment_AssessmentId(assessmentId);
    }

    @Override
    public SubmissionDto toDto(Submission submission, boolean questionSubmissions, boolean submissionComments) {

        SubmissionDto submissionDto = new SubmissionDto();
        submissionDto.setSubmissionId(submission.getSubmissionId());
        submissionDto.setParticipantId(submission.getParticipant().getParticipantId());
        submissionDto.setAssessmentId(submission.getAssessment().getAssessmentId());
        submissionDto.setCalculatedGrade(submission.getCalculatedGrade());
        submissionDto.setAlteredCalculatedGrade(submission.getAlteredCalculatedGrade());
        submissionDto.setTotalAlteredGrade(submission.getTotalAlteredGrade());
        submissionDto.setDateSubmitted(submission.getDateSubmitted());
        submissionDto.setLateSubmission(submission.getLateSubmission());
        List<QuestionSubmissionDto> questionSubmissionDtoList = new ArrayList<>();
        if(questionSubmissions) {
            List<QuestionSubmission> questionSubmissionList = allRepositories.questionSubmissionRepository.findBySubmission_SubmissionId(submission.getSubmissionId());
            for(QuestionSubmission questionSubmission : questionSubmissionList) {
                questionSubmissionDtoList.add(questionSubmissionService.toDto(questionSubmission, false));
            }
        }
        submissionDto.setQuestionSubmissionDtoList(questionSubmissionDtoList);
        List<SubmissionCommentDto> submissionCommentDtoList = new ArrayList<>();
        if(submissionComments) {
            List<SubmissionComment> submissionCommentList = allRepositories.submissionCommentRepository.findBySubmission_SubmissionId(submission.getSubmissionId());
            for(SubmissionComment submissionComment : submissionCommentList) {
                submissionCommentDtoList.add(submissionCommentService.toDto(submissionComment));
            }
        }
        submissionDto.setSubmissionCommentDtoList(submissionCommentDtoList);

        return submissionDto;
    }

    @Override
    public Submission fromDto(SubmissionDto submissionDto) throws DataServiceException {

        Submission submission = new Submission();
        submission.setSubmissionId(submissionDto.getSubmissionId());
        submission.setCalculatedGrade(submissionDto.getCalculatedGrade());
        submission.setAlteredCalculatedGrade(submissionDto.getAlteredCalculatedGrade());
        submission.setTotalAlteredGrade(submissionDto.getTotalAlteredGrade());
        submission.setDateSubmitted(submissionDto.getDateSubmitted());
        submission.setLateSubmission(submissionDto.getLateSubmission());
        Optional<Participant> participant = allRepositories.participantRepository.findById(submissionDto.getParticipantId());
        if(participant.isPresent()){
            submission.setParticipant(participant.get());
        } else{
            throw new DataServiceException("The participant for the submission does not exist.");
        }
        Optional<Assessment> assessment = allRepositories.assessmentRepository.findById(submissionDto.getAssessmentId());
        if(assessment.isPresent()) {
            submission.setAssessment(assessment.get());
        } else {
            throw new DataServiceException("The assessment for the submission does not exist.");
        }

        return submission;
    }

    @Override
    public Submission save(Submission submission) { return allRepositories.submissionRepository.save(submission); }

    @Override
    public Optional<Submission> findById(Long id) { return allRepositories.submissionRepository.findById(id); }

    @Override
    public List<Submission> findByParticipantId(Long participantId) { return allRepositories.submissionRepository.findByParticipant_ParticipantId(participantId); }

    @Override
    public Optional<Submission> findByParticipantIdAndSubmissionId(Long participantId, Long submissionId) {
        return allRepositories.submissionRepository.findByParticipant_ParticipantIdAndSubmissionId(participantId, submissionId);
    }

    @Override
    public Participant findByExperiment_ExperimentIdAndLtiUserEntity_UserKey(Long experimentId, String userId) {
        return allRepositories.participantRepository.findByExperiment_ExperimentIdAndLtiUserEntity_UserKey(experimentId, userId);
    }

    @Override
    public void saveAndFlush(Submission submissionToChange) { allRepositories.submissionRepository.saveAndFlush(submissionToChange); }

    @Override
    public void deleteById(Long id) throws EmptyResultDataAccessException { allRepositories.submissionRepository.deleteById(id); }

    @Override
    public boolean submissionBelongsToAssessment(Long assessmentId, Long submissionId) {
        return allRepositories.submissionRepository.existsByAssessment_AssessmentIdAndSubmissionId(assessmentId, submissionId);
    }

    @Override
    @Transactional
    public void finalizeAndGrade(Long submissionId, SecurityInfo securityInfo) throws DataServiceException {
        Optional<Submission> submissionOptional =  allRepositories.submissionRepository.findById(submissionId);
        if (submissionOptional.isPresent()){
            //TODO, manage late submissions.
            //We are not changing the submission date once it is set.
            if (submissionOptional.get().getDateSubmitted()==null) {
                submissionOptional.get().setDateSubmitted(submissionOptional.get().getUpdatedAt());
            }
            saveAndFlush(gradeSubmission(submissionOptional.get()));
        } else {
            throw new DataServiceException("Submission not found");
        }
    }

    @Override
    @Transactional
    public void grade(Long submissionId, SecurityInfo securityInfo) throws DataServiceException {
        Optional<Submission> submissionOptional =  allRepositories.submissionRepository.findById(submissionId);
        if (submissionOptional.isPresent()){
            saveAndFlush(gradeSubmission(submissionOptional.get()));
        } else {
            throw new DataServiceException("Submission not found");
        }
    }


    @Override
    public Submission gradeSubmission(Submission submission){
        //We need to calculate the 2 the possible grades. Automatic and manual
        Float automatic = Float.parseFloat("0");
        Float manual = Float.parseFloat("0");
        for (QuestionSubmission questionSubmission:submission.getQuestionSubmissions()) {
            //We need to grade the question first automatically it it was not graded before.
            //If multiple choice, we take the automatic score for automatic and the manual if any for manual, and if no manual, then the automatic for manual
            QuestionSubmission questionGraded = questionSubmissionService.automaticGrading(questionSubmission);
            automatic = automatic + questionGraded.getCalculatedPoints();
            if (questionGraded.getAlteredGrade()!=null && !questionGraded.getAlteredGrade().isNaN()) {
                manual = manual + questionSubmission.getAlteredGrade();
            } else {
                manual = manual + questionSubmission.getCalculatedPoints();
            }
            //TODO: If open question, we take the manual score for both, because the automatic will be always 0
        }
        submission.setCalculatedGrade(automatic);
        submission.setAlteredCalculatedGrade(manual);
        return submission;
    }

    @Override
    public void sendSubmissionGradeToCanvas(Submission submission) throws ConnectionException, DataServiceException {
        //We need, the assignment, and the iss configuration...
        Assignment assignment = submission.getAssessment().getTreatment().getAssignment();
        Experiment experiment = assignment.getExposure().getExperiment();
        LTIToken ltiTokenScore = advantageAGSService.getToken("scores", experiment.getPlatformDeployment());
        LTIToken ltiTokenResults = advantageAGSService.getToken("results", experiment.getPlatformDeployment());
        //find the right id to pass based on the assignment
        String lineitemId = assignmentService.lineItemId(assignment);
        if (lineitemId==null){
            throw new DataServiceException("The assignment is not linked to any Canvas assignment");
        }
        Score score = new Score();
        score.setUserId(submission.getParticipant().getLtiUserEntity().getUserKey());
        if (submission.getTotalAlteredGrade()!=null) {
            score.setScoreGiven(submission.getTotalAlteredGrade().toString());
        } else {
            score.setScoreGiven(submission.getAlteredCalculatedGrade().toString());
        }
        score.setScoreMaximum(assessmentService.calculateMaxScore(submission.getAssessment()).toString());
        score.setActivityProgress("Completed");
        score.setGradingProgress("FullyGraded");
        //TODO, check if this value is ok
        score.setTimestamp("2021-06-10T18:54:36.736+00:00");
        advantageAGSService.postScore(ltiTokenScore, ltiTokenResults, experiment.getLtiContextEntity(), lineitemId, score);


    }
}
