package edu.iu.terracotta.service.canvas.impl;

import edu.iu.terracotta.exceptions.CanvasApiException;
import edu.iu.terracotta.model.PlatformDeployment;
import edu.iu.terracotta.model.canvas.AssignmentExtended;
import edu.iu.terracotta.service.canvas.AssignmentReaderExtended;
import edu.iu.terracotta.service.canvas.AssignmentWriterExtended;
import edu.iu.terracotta.service.canvas.CanvasAPIClient;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import edu.ksu.canvas.interfaces.SubmissionReader;
import edu.ksu.canvas.interfaces.SubmissionWriter;
import edu.ksu.canvas.model.Progress;
import edu.ksu.canvas.model.assignment.Submission;
import edu.ksu.canvas.requestOptions.GetSingleAssignmentOptions;
import edu.ksu.canvas.requestOptions.GetSubmissionsOptions;
import edu.ksu.canvas.requestOptions.ListCourseAssignmentsOptions;
import edu.ksu.canvas.requestOptions.MultipleSubmissionsOptions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import edu.ksu.canvas.oauth.OauthToken;
import edu.ksu.canvas.oauth.NonRefreshableOauthToken;


@Service
public class CanvasAPIClientImpl implements CanvasAPIClient {


    @Override
    public Optional<AssignmentExtended> createCanvasAssignment(AssignmentExtended canvasAssignment, String canvasCourseId, PlatformDeployment platformDeployment ) throws CanvasApiException {
        //https://github.com/kstateome/canvas-api/tree/master

        try {
            String canvasBaseUrl = platformDeployment.getBaseUrl();
            OauthToken oauthToken = new NonRefreshableOauthToken(platformDeployment.getApiToken());
            CanvasApiFactoryExtended apiFactory = new CanvasApiFactoryExtended(canvasBaseUrl);
            AssignmentWriterExtended assignmentWriter = apiFactory.getWriter(AssignmentWriterExtended.class, oauthToken);
            return assignmentWriter.createAssignment(canvasCourseId, canvasAssignment);
        } catch (IOException ex){
            throw new CanvasApiException(
                    "Failed to create Assignment in Canvas course by ID [" + canvasCourseId + "]", ex);
        }
    }

    @Override
    public List<AssignmentExtended> listAssignments(String canvasCourseId, PlatformDeployment platformDeployment) throws CanvasApiException {
        try {
            String canvasBaseUrl = platformDeployment.getBaseUrl();
            OauthToken oauthToken = new NonRefreshableOauthToken(platformDeployment.getApiToken());
            CanvasApiFactoryExtended apiFactory = new CanvasApiFactoryExtended(canvasBaseUrl);
            AssignmentReaderExtended assignmentReader = apiFactory.getReader(AssignmentReaderExtended.class, oauthToken);
            ListCourseAssignmentsOptions listCourseAssignmentsOptions = new ListCourseAssignmentsOptions(canvasCourseId);
            return assignmentReader.listCourseAssignments(listCourseAssignmentsOptions);
        } catch (IOException ex){
            throw new CanvasApiException(
                    "Failed to get the list of assignments Canvas course [" + canvasCourseId + "]", ex);
        }
    }

    @Override
    public Optional<AssignmentExtended> listAssignment(String canvasCourseId, int assignmentId, PlatformDeployment platformDeployment) throws CanvasApiException {
        try {
            String canvasBaseUrl = platformDeployment.getBaseUrl();
            OauthToken oauthToken = new NonRefreshableOauthToken(platformDeployment.getApiToken());
            CanvasApiFactoryExtended apiFactory = new CanvasApiFactoryExtended(canvasBaseUrl);
            AssignmentReaderExtended assignmentReader = apiFactory.getReader(AssignmentReaderExtended.class, oauthToken);
            GetSingleAssignmentOptions assignmentsOptions = new GetSingleAssignmentOptions(canvasCourseId,assignmentId);
            return assignmentReader.getSingleAssignment(assignmentsOptions);
        } catch (IOException ex){
            throw new CanvasApiException(
                    "Failed to get the assignments with id [" + assignmentId + "] from canvas course [" + canvasCourseId + "]", ex);
        }
    }


        @Override
    public List<Submission> listSubmissions(Integer assignmentId, String canvasCourseId, PlatformDeployment platformDeployment) throws CanvasApiException, IOException {
        String canvasBaseUrl = platformDeployment.getBaseUrl();
        OauthToken oauthToken = new NonRefreshableOauthToken(platformDeployment.getApiToken());
        CanvasApiFactoryExtended apiFactory = new CanvasApiFactoryExtended(canvasBaseUrl);
        SubmissionReader submissionReader = apiFactory.getReader(SubmissionReader.class,oauthToken);
        GetSubmissionsOptions submissionsOptions = new GetSubmissionsOptions(canvasCourseId,assignmentId);
        submissionsOptions.includes(Collections.singletonList(GetSubmissionsOptions.Include.USER));
        return submissionReader.getCourseSubmissions(submissionsOptions);
    }

    @Override
    public Optional<Progress> postSubmission(edu.iu.terracotta.model.app.Submission submission, Float maxTerracottaScore) throws CanvasApiException, IOException {

        String canvasCourseId = getCanvasCourseId(submission.getParticipant().getLtiMembershipEntity().getContext().getContext_memberships_url());
        int assignmentId = Integer.parseInt(submission.getAssessment().getTreatment().getAssignment().getLmsAssignmentId());
        String canvasUserId = submission.getParticipant().getLtiUserEntity().getLmsUserId();
        Optional<AssignmentExtended> assignmentExtended = listAssignment(canvasCourseId,assignmentId,submission.getParticipant().getLtiUserEntity().getPlatformDeployment());
        if (!assignmentExtended.isPresent()){
            throw new CanvasApiException(
                    "Failed to get the assignments with id [" + assignmentId + "] from canvas course [" + canvasCourseId + "]");
        }
        Double maxCanvasScore = assignmentExtended.get().getPointsPossible();

        Double grade = Double.valueOf("0");
        if (submission.getTotalAlteredGrade()!=null) {
            grade= Double.parseDouble(submission.getTotalAlteredGrade().toString());
        } else if (submission.getAlteredCalculatedGrade()!=null) {
            grade= Double.parseDouble(submission.getAlteredCalculatedGrade().toString());
        } else {
            grade=Double.parseDouble(submission.getCalculatedGrade().toString());
        }
        grade = grade * maxCanvasScore / Double.parseDouble(maxTerracottaScore.toString());

        PlatformDeployment platformDeployment = submission.getParticipant().getLtiUserEntity().getPlatformDeployment();
        String canvasBaseUrl = platformDeployment.getBaseUrl();
        OauthToken oauthToken = new NonRefreshableOauthToken(platformDeployment.getApiToken());
        CanvasApiFactoryExtended apiFactory = new CanvasApiFactoryExtended(canvasBaseUrl);
        SubmissionWriter submissionWriter = apiFactory.getWriter(SubmissionWriter.class,oauthToken);
        MultipleSubmissionsOptions multipleSubmissionsOptions = new MultipleSubmissionsOptions(canvasCourseId, assignmentId, new HashMap<>());
        MultipleSubmissionsOptions.StudentSubmissionOption submissionsOptions =
                multipleSubmissionsOptions.createStudentSubmissionOption(
                        null,
                        grade.toString(),
                        false,
                        false,
                        null,
                        null
                );
        HashMap<String, MultipleSubmissionsOptions.StudentSubmissionOption> submissionOptionHashMap = new HashMap<>();
        submissionOptionHashMap.put(canvasUserId, submissionsOptions);
        multipleSubmissionsOptions.setStudentSubmissionOptionMap(submissionOptionHashMap);
        submissionWriter.gradeMultipleSubmissionsByCourse(multipleSubmissionsOptions);
        return submissionWriter.gradeMultipleSubmissionsByCourse(multipleSubmissionsOptions);
    }

    @Override
    public Optional<AssignmentExtended> checkAssignmentExists(Integer assignmentId, String canvasCourseId, PlatformDeployment platformDeployment) throws CanvasApiException {
        try {
            String canvasBaseUrl = platformDeployment.getBaseUrl();
            OauthToken oauthToken = new NonRefreshableOauthToken(platformDeployment.getApiToken());
            CanvasApiFactoryExtended apiFactory = new CanvasApiFactoryExtended(canvasBaseUrl);
            AssignmentReaderExtended assignmentReader = apiFactory.getReader(AssignmentReaderExtended.class, oauthToken);
            GetSingleAssignmentOptions getSingleAssignmentsOptions = new GetSingleAssignmentOptions(canvasCourseId, assignmentId);
            return assignmentReader.getSingleAssignment(getSingleAssignmentsOptions);
        } catch (edu.ksu.canvas.exception.ObjectNotFoundException e){
            return Optional.empty();
        } catch (IOException ex){
            throw new CanvasApiException(
                    "Failed to get the Assignment in Canvas course by ID [" + canvasCourseId + "]", ex);
        }
    }

    private String getCanvasCourseId(String membershipUrl){
        return StringUtils.substringBetween(membershipUrl, "/courses/", "/");
    }

}
