package edu.iu.terracotta.service.caliper.impl;

import edu.iu.terracotta.model.LtiContextEntity;
import edu.iu.terracotta.model.LtiMembershipEntity;
import edu.iu.terracotta.model.PlatformDeployment;
import edu.iu.terracotta.model.app.Assessment;
import edu.iu.terracotta.model.app.Participant;
import edu.iu.terracotta.model.app.Submission;
import edu.iu.terracotta.model.events.Event;
import edu.iu.terracotta.model.oauth2.SecuredInfo;
import edu.iu.terracotta.repository.AllRepositories;
import edu.iu.terracotta.service.app.AssessmentService;
import edu.iu.terracotta.service.app.SubmissionService;
import edu.iu.terracotta.service.caliper.CaliperService;
import edu.iu.terracotta.utils.LtiStrings;
import liquibase.util.CollectionUtil;
import org.apache.commons.lang3.StringUtils;
import org.imsglobal.caliper.Envelope;
import org.imsglobal.caliper.Sensor;
import org.imsglobal.caliper.actions.Action;
import org.imsglobal.caliper.clients.HttpClient;
import org.imsglobal.caliper.clients.HttpClientOptions;
import org.imsglobal.caliper.context.JsonldContext;
import org.imsglobal.caliper.context.JsonldStringContext;
import org.imsglobal.caliper.entities.CaliperEntity;
import org.imsglobal.caliper.entities.CaliperGeneratable;
import org.imsglobal.caliper.entities.CaliperReferrer;
import org.imsglobal.caliper.entities.EntityType;
import org.imsglobal.caliper.entities.agent.CaliperOrganization;
import org.imsglobal.caliper.entities.agent.CourseSection;
import org.imsglobal.caliper.entities.agent.Membership;
import org.imsglobal.caliper.entities.agent.Person;
import org.imsglobal.caliper.entities.agent.Role;
import org.imsglobal.caliper.entities.agent.SoftwareApplication;
import org.imsglobal.caliper.entities.agent.Status;
import org.imsglobal.caliper.entities.resource.Attempt;
import org.imsglobal.caliper.entities.session.LtiSession;
import org.imsglobal.caliper.events.AssessmentEvent;
import org.imsglobal.caliper.events.EventType;
import org.imsglobal.caliper.events.ToolUseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.joda.time.DateTime;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CaliperServiceImpl implements CaliperService {

    private Sensor sensor;
    private HttpClient httpClient;
    public static final String DATA_VERSION = "http://purl.imsglobal.org/ctx/caliper/v1p2";
    static final Logger log = LoggerFactory.getLogger(CaliperServiceImpl.class);
    private String applicationName;
    private String applicationUrl;
    private final SoftwareApplication softwareApplication;
    private final JsonldContext context;
    private final boolean caliperSend;
    private final boolean caliperDB;

    @Autowired
    AllRepositories allRepositories;

    @Autowired
    AssessmentService assessmentService;

    @Autowired
    SubmissionService submissionService;

    @Autowired
    public CaliperServiceImpl(@Value("${caliper.sensor-id:1}")final String sensorId,
                              @Value("${caliper.client-id:1}") final String clientId,
                              @Value("${caliper.api-key:1}") final String apiKey,
                              @Value("${caliper.connection-timeout:10000}") final int connectionTimeout,
                              @Value("${caliper.content-type:application/json}") final String contentType,
                              @Value("${caliper.host:nohost}") final String host,
                              @Value("${caliper.socket-timeout:10000}") final int socketTimeOut,
                              @Value("${caliper.send:false}") final boolean caliperSendAttribute,
                              @Value("${caliper.store-db:false}") final boolean caliperStoreDBAttribute,
                              @Value("${application.name}") final String applicationNameAttribute,
                              @Value("${application.url}") final String applicationUrlAttribute){
        applicationName = applicationNameAttribute;
        applicationUrl = applicationUrlAttribute;
        caliperSend = caliperSendAttribute;
        caliperDB = caliperStoreDBAttribute;
        context = JsonldStringContext.create(DATA_VERSION);
        softwareApplication = prepareSoftwareApplication();
        if (caliperSend) {
            sensor = Sensor.create(sensorId);
            HttpClientOptions httpClientOptions = new HttpClientOptions.OptionsBuilder()
                    .apiKey(apiKey)
                    .connectionTimeout(connectionTimeout)
                    .contentType(contentType)
                    .host(host)
                    .socketTimeout(socketTimeOut)
                    .build();
            httpClient = HttpClient.create(clientId, httpClientOptions);
            sensor.registerClient(httpClient);
        }
    }

    @Override
    public void send(Envelope envelope) {
        sensor.send(envelope);
    }

    @Override
    public void sendAssignmentStarted(Submission submission, SecuredInfo securedInfo) {
        DateTime time = DateTime.now();
        Participant participant= submission.getParticipant();
        LtiMembershipEntity membershipEntity = participant.getLtiMembershipEntity();
        LtiSession ltiSession = prepareLtiSession(securedInfo);
        Person actor = prepareActor(membershipEntity, securedInfo.getCanvasUserGlobalId());
        CaliperOrganization group = prepareGroup(membershipEntity, securedInfo);
        org.imsglobal.caliper.entities.resource.Assessment assessment = prepareAssessment(submission, securedInfo);
        Attempt attempt = prepareAttempt(submission, actor, assessment);
        String uuid = "urn:uuid:" + UUID.randomUUID();
        if (caliperSend) {
            log.debug("Caliper event being generated: Assessment Starting Use");

            AssessmentEvent assessmentEvent = AssessmentEvent.builder()
                    .id(uuid)
                    .actor(actor)
                    .action(Action.STARTED)
                    .edApp(softwareApplication)
                    .context(context)
                    .eventTime(DateTime.now())
                    .membership(prepareMembership(participant, securedInfo))
                    .object(assessment)
                    .referrer(prepareReferrer(membershipEntity.getContext().getPlatformDeployment()))
                    .federatedSession(ltiSession)
                    .generated(attempt)
                    .group(group)
                    .build();
            Envelope envelope = new Envelope(sensor.getId(), DateTime.now(), DATA_VERSION, Collections.singletonList(assessmentEvent));
            send(envelope);
            log.debug("Caliper event sent");
        }
        if (caliperDB) {
            log.debug("Caliper event to DB: Assessment Started");
            Event event = new Event();
            event.setCaliperId(uuid);
            event.setEventTime(new Timestamp(time.getMillis()));
            event.setActorId(actor.getId());
            event.setActorType(actor.getType().value());
            event.setPlatform_deployment(membershipEntity.getUser().getPlatformDeployment().getBaseUrl());
            event.setType(EventType.ASSESSMENT.value());
            event.setProfile("AssessmentProfile");
            event.setAction(Action.STARTED.value());
            event.setGroup(group.getId());
            event.setObjectId(assessment.getId());
            event.setObjectType(EntityType.ASSESSMENT.value());
            event.setReferrerId(membershipEntity.getUser().getPlatformDeployment().getBaseUrl());
            event.setReferredType(EntityType.SOFTWARE_APPLICATION.value());
            event.setGeneratedId(attempt.getId());
            event.setGeneratedType(EntityType.ATTEMPT.value());
            event.setMembershipId(membershipEntity.getUser().getUserKey());
            event.setMembershipRoles(roleToString(membershipEntity.getRole()));
            event.setFederatedSession(ltiSession.getId());
            saveEvent(event);
            log.debug("Event Saved");
        }
    }

    @Override
    public void sendAssignmentSubmitted(Submission submission, SecuredInfo securedInfo) {
        DateTime time = DateTime.now();
        Participant participant= submission.getParticipant();
        LtiMembershipEntity membershipEntity = participant.getLtiMembershipEntity();
        Person actor = prepareActor(membershipEntity, securedInfo.getCanvasUserGlobalId());
        LtiSession ltiSession = prepareLtiSession(securedInfo);
        CaliperOrganization group = prepareGroup(membershipEntity, securedInfo);
        org.imsglobal.caliper.entities.resource.Assessment assessment = prepareAssessment(submission, securedInfo);
        Attempt attempt = prepareAttempt(submission, actor, assessment);
        String uuid = "urn:uuid:" + UUID.randomUUID();
        if (caliperSend) {
            log.debug("Caliper event being generated: Assessment Submitted Use");

            AssessmentEvent assessmentEvent = AssessmentEvent.builder()
                    .id(uuid)
                    .actor(actor)
                    .action(Action.SUBMITTED)
                    .edApp(softwareApplication)
                    .context(context)
                    .eventTime(DateTime.now())
                    .membership(prepareMembership(participant, securedInfo))
                    .object(assessment)
                    .referrer(prepareReferrer(membershipEntity.getContext().getPlatformDeployment()))
                    .federatedSession(ltiSession)
                    .generated(attempt)
                    .group(group)
                    .build();
            Envelope envelope = new Envelope(sensor.getId(), DateTime.now(), DATA_VERSION, Collections.singletonList(assessmentEvent));
            send(envelope);
            log.debug("Caliper event sent");
        }
        if (caliperDB) {
            log.debug("Caliper event to DB: Assessment Started");
            Event event = new Event();
            event.setCaliperId(uuid);
            event.setEventTime(new Timestamp(time.getMillis()));
            event.setActorId(actor.getId());
            event.setActorType(actor.getType().value());
            event.setPlatform_deployment(membershipEntity.getUser().getPlatformDeployment().getBaseUrl());
            event.setType(EventType.ASSESSMENT.value());
            event.setProfile("AssessmentProfile");
            event.setAction(Action.SUBMITTED.value());
            event.setGroup(group.getId());
            event.setObjectId(assessment.getId());
            event.setObjectType(EntityType.ASSESSMENT.value());
            event.setReferrerId(membershipEntity.getUser().getPlatformDeployment().getBaseUrl());
            event.setReferredType(EntityType.SOFTWARE_APPLICATION.value());
            event.setGeneratedId(attempt.getId());
            event.setGeneratedType(EntityType.ATTEMPT.value());
            event.setMembershipId(membershipEntity.getUser().getUserKey());
            event.setMembershipRoles(roleToString(membershipEntity.getRole()));
            event.setFederatedSession(ltiSession.getId());
            saveEvent(event);
            log.debug("Event Saved");
        }
    }

    @Override
    public void sendAssignmentRestarted(Submission submission, SecuredInfo securedInfo) {
        DateTime time = DateTime.now();
        Participant participant= submission.getParticipant();
        LtiMembershipEntity membershipEntity = participant.getLtiMembershipEntity();
        Person actor = prepareActor(membershipEntity, securedInfo.getCanvasUserGlobalId());
        LtiSession ltiSession = prepareLtiSession(securedInfo);
        CaliperOrganization group = prepareGroup(membershipEntity, securedInfo);
        org.imsglobal.caliper.entities.resource.Assessment assessment = prepareAssessment(submission, securedInfo);
        Attempt attempt = prepareAttempt(submission, actor, assessment);
        String uuid = "urn:uuid:" + UUID.randomUUID();
        if (caliperSend) {
            log.debug("Caliper event being generated: Assessment Starting Use");

            AssessmentEvent assessmentEvent = AssessmentEvent.builder()
                    .id(uuid)
                    .actor(actor)
                    .action(Action.RESTARTED)
                    .edApp(softwareApplication)
                    .context(context)
                    .eventTime(DateTime.now())
                    .membership(prepareMembership(participant, securedInfo))
                    .object(assessment)
                    .generated(attempt)
                    .referrer(prepareReferrer(membershipEntity.getContext().getPlatformDeployment()))
                    .federatedSession(ltiSession)
                    .group(group)
                    .build();
            Envelope envelope = new Envelope(sensor.getId(), DateTime.now(), DATA_VERSION, Collections.singletonList(assessmentEvent));
            send(envelope);
            log.debug("Caliper event sent");
        }
        if (caliperDB) {
            log.debug("Caliper event to DB: Assessment Started");
            Event event = new Event();
            event.setCaliperId(uuid);
            event.setEventTime(new Timestamp(time.getMillis()));
            event.setActorId(actor.getId());
            event.setActorType(actor.getType().value());
            event.setPlatform_deployment(membershipEntity.getUser().getPlatformDeployment().getBaseUrl());
            event.setType(EventType.ASSESSMENT.value());
            event.setProfile("AssessmentProfile");
            event.setAction(Action.RESTARTED.value());
            event.setGroup(group.getId());
            event.setObjectId(assessment.getId());
            event.setObjectType(EntityType.ASSESSMENT.value());
            event.setReferrerId(membershipEntity.getUser().getPlatformDeployment().getBaseUrl());
            event.setReferredType(EntityType.SOFTWARE_APPLICATION.value());
            event.setGeneratedId(attempt.getId());
            event.setGeneratedType(EntityType.ATTEMPT.value());
            event.setMembershipId(membershipEntity.getUser().getUserKey());
            event.setMembershipRoles(roleToString(membershipEntity.getRole()));
            event.setFederatedSession(ltiSession.getId());
            saveEvent(event);
            log.debug("Event Saved");
        }
    }

    @Override
    public void sendNavigationEvent(Participant participant, String whereTo, SecuredInfo securedInfo) {

    }

    @Override
    public void sendFeedbackEvent(Participant participant, Assessment assessment, SecuredInfo securedInfo) {

    }

    @Override
    public void sendViewGradeEvent(Participant participant, Assessment assessment, SecuredInfo securedInfo) {

    }

    @Override
    public void sendToolUseEvent(LtiMembershipEntity membershipEntity,
                                 String canvasUserGlobalId,
                                 String canvasCourseId,
                                 String canvasUserId,
                                 String canvasLoginId,
                                 List<String> canvasRoles,
                                 String canvasUserName
    ) {
        DateTime time = DateTime.now();
        Person actor = prepareActor(membershipEntity, canvasUserGlobalId);
        SecuredInfo securedInfo = new SecuredInfo();
        securedInfo.setCanvasUserGlobalId(canvasUserGlobalId);
        securedInfo.setCanvasCourseId(canvasCourseId);
        securedInfo.setCanvasUserId(canvasUserId);
        securedInfo.setCanvasLoginId(canvasLoginId);
        securedInfo.setRoles(canvasRoles);
        securedInfo.setCanvasUserName(canvasUserName);
        LtiSession ltiSession = prepareLtiSession(securedInfo);
        CaliperOrganization group = prepareGroup(membershipEntity, securedInfo);
        String uuid = "urn:uuid:" + UUID.randomUUID();
        if (caliperSend) {
            log.debug("Caliper event being generated: Tool Use");
            ToolUseEvent toolUseEvent = ToolUseEvent.builder()
                    .id(uuid)
                    .actor(actor)
                    .action(Action.USED)
                    .edApp(softwareApplication)
                    .context(context)
                    .eventTime(DateTime.now())
                    .object(softwareApplication)
                    .referrer(prepareReferrer(membershipEntity.getContext().getPlatformDeployment()))
                    .federatedSession(ltiSession)
                    .group(group)
                    .build();
            Envelope envelope = new Envelope(sensor.getId(), DateTime.now(), DATA_VERSION, Collections.singletonList(toolUseEvent));
            send(envelope);
            log.debug("Caliper event sent");
        }
        if (caliperDB) {
            log.debug("Caliper event to DB: Tool Use");
            Event event = new Event();
            event.setCaliperId(uuid);
            event.setEventTime(new Timestamp(time.getMillis()));
            event.setActorId(actor.getId());
            event.setActorType(actor.getType().value());
            event.setPlatform_deployment(membershipEntity.getUser().getPlatformDeployment().getBaseUrl());
            event.setType(EventType.TOOL_USE.value());
            event.setProfile("ToolUseProfile");
            event.setAction(Action.USED.value());
            event.setGroup(group.getId());
            event.setObjectId(softwareApplication.getId());
            event.setObjectType(softwareApplication.getType().value());
            event.setReferrerId(membershipEntity.getUser().getPlatformDeployment().getBaseUrl());
            event.setReferredType(EntityType.SOFTWARE_APPLICATION.value());
            event.setMembershipId(membershipEntity.getUser().getUserKey());
            event.setMembershipRoles(roleToString(membershipEntity.getRole()));
            event.setFederatedSession(ltiSession.getId());
            saveEvent(event);
            log.debug("Event Saved");
        }

    }

    private void saveEvent(Event event){
        allRepositories.eventRepository.save(event);
    }


    private Person prepareActor(LtiMembershipEntity participant, String canvasGlobalId){

        Map<String, Object> extensions = new HashMap<>();
        extensions.put("canvas_global_id", canvasGlobalId);
        extensions.put("lti_id", participant.getUser().getUserKey());
        extensions.put("lti_tenant", participant.getUser().getPlatformDeployment().getBaseUrl());
        Person actor = Person.builder()
                .id(applicationUrl + "/users/" + participant.getUser().getUserId())
                .extensions(extensions)
                .type(EntityType.PERSON)
                .build();
        return actor;
    }

    private SoftwareApplication prepareSoftwareApplication() {
        return SoftwareApplication.builder()
                .name(applicationName)
                .id(applicationUrl)
                .build();
    }
    private CaliperReferrer prepareReferrer(PlatformDeployment platformDeployment){
        return SoftwareApplication.builder()
                .id(platformDeployment.getBaseUrl())
                .type(EntityType.SOFTWARE_APPLICATION)
                .build();
    }

    private org.imsglobal.caliper.entities.resource.Assessment prepareAssessment(Submission submission, SecuredInfo securedInfo) {
        String terracottaAssessmentId = applicationUrl + "/api/experiments/" + submission.getAssessment().getTreatment().getCondition().getExperiment().getExperimentId()
                + "/conditions/" + submission.getAssessment().getTreatment().getCondition().getConditionId()
                + "/treatments/" + submission.getAssessment().getTreatment().getTreatmentId()
                + "/assessments/" + submission.getAssessment().getAssessmentId();
        String canvasAssessmentId = submission.getParticipant().getLtiUserEntity().getPlatformDeployment().getBaseUrl()
                + "/courses/" + securedInfo.getCanvasCourseId()
                + "/assignments/" + securedInfo.getCanvasAssignmentId();
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("canvas_assessment", canvasAssessmentId);
        org.imsglobal.caliper.entities.resource.Assessment assessment = org.imsglobal.caliper.entities.resource.Assessment.builder()
                .name(submission.getAssessment().getTitle())
                .id(terracottaAssessmentId)
                .extensions(extensions)
                .type(EntityType.ASSESSMENT)
                .maxAttempts(submission.getAssessment().getNumOfSubmissions())
                .maxScore(assessmentService.calculateMaxScore(submission.getAssessment()))
                .version("" + submission.getAssessment().getVersion())
                .build();
        return assessment;
    }

    private Attempt prepareAttempt(Submission submission, Person actor, org.imsglobal.caliper.entities.resource.Assessment assessment ){

        String terracottaSubmissionId = assessment.getExtensions().get("terracotta_assessment")
                + "/submissions/" + submission.getSubmissionId();
        Attempt attempt = Attempt.builder()
                .id(terracottaSubmissionId)
                .type(EntityType.ATTEMPT)
                .assignee(actor)
                .assignable(assessment)
                .count(submissionService.findByParticipantIdAndAssessmentId(submission.getParticipant().getParticipantId(), submission.getAssessment().getAssessmentId()).size())
                .dateCreated(convertTimestamp(submission.getCreatedAt()))
                .startedAtTime(convertTimestamp(submission.getCreatedAt()))
                .endedAtTime(convertTimestamp(submission.getDateSubmitted()))
                .build();
        return attempt;
    }

    private CaliperOrganization prepareGroup(LtiMembershipEntity participant, SecuredInfo securedInfo){
        LtiContextEntity contextEntity = participant.getContext();
        String canvasCourseId = participant.getContext().getPlatformDeployment().getBaseUrl()
                + "/courses/" + securedInfo.getCanvasCourseId();
        return CourseSection.builder()
                .name(contextEntity.getTitle())
                .id(canvasCourseId)
                .type(EntityType.COURSE_SECTION).build();
    }

    private Membership prepareMembership(Participant participant, SecuredInfo securedInfo){
        LtiContextEntity contextEntity = participant.getLtiMembershipEntity().getContext();
        String canvasCourseId = participant.getLtiUserEntity().getPlatformDeployment().getBaseUrl()
                + "/courses/" + securedInfo.getCanvasCourseId();
        return Membership.builder()
                .id(canvasCourseId)
                .type(EntityType.MEMBERSHIP)
                .member(prepareActor(participant.getLtiMembershipEntity(), securedInfo.getCanvasUserGlobalId()))
                .organization(prepareGroup(participant.getLtiMembershipEntity(), securedInfo))
                .status(getStatus(participant.getDropped(), participant.getExperiment().getClosed()!=null))
                .roles(Collections.singletonList(roleToCaliperRole(participant.getLtiMembershipEntity().getRole()))).build();
    }

    private LtiSession prepareLtiSession(SecuredInfo securedInfo){
        Map<String,Object> messageParameters = new HashMap<>();
        messageParameters.put("canvas_course_id", securedInfo.getCanvasCourseId());
        if (securedInfo.getCanvasAssignmentId()!=null && !securedInfo.getCanvasAssignmentId().startsWith("$")) {
            messageParameters.put("canvas_assignment_id", securedInfo.getCanvasAssignmentId());
        }
        messageParameters.put("canvas_user_id", securedInfo.getCanvasUserId());
        messageParameters.put("canvas_login_id", securedInfo.getCanvasLoginId());
        messageParameters.put("canvas_user_global_id", securedInfo.getCanvasUserGlobalId());
        messageParameters.put("canvas_roles", securedInfo.getRoles());
        messageParameters.put("canvas_user_name", securedInfo.getCanvasUserName());
        return LtiSession.builder()
                .id("urn:session_id_localized:" + applicationUrl + "/lti/oauth_nonce/" + securedInfo.getNonce())
                .type(EntityType.LTI_SESSION)
                .messageParameters(messageParameters)
                .build();
    }


    private Status getStatus(boolean dropped, boolean closed){
        if (closed || dropped) {
            return Status.INACTIVE;
        } else {
            return Status.ACTIVE;
        }
    }


    private Role roleToCaliperRole(int role) {

        if (role == 2) {
            return Role.ADMINISTRATOR;
        } else if (role == LtiStrings.ROLE_INSTRUCTOR) {
            return Role.INSTRUCTOR;
        } else if (role == LtiStrings.ROLE_STUDENT) {
            return Role.LEARNER;
        } else {
            return null;
        }
    }

    private String roleToString(int role) {

        if (role == 2) {
            return LtiStrings.LTI_ROLE_MEMBERSHIP_ADMIN;
        } else if (role == LtiStrings.ROLE_INSTRUCTOR) {
            return LtiStrings.LTI_ROLE_MEMBERSHIP_INSTRUCTOR;
        } else if (role == LtiStrings.ROLE_STUDENT){
            return LtiStrings.LTI_ROLE_LEARNER;
        } else {
            return null;
        }

    }

    private DateTime convertTimestamp(Timestamp timestamp) {
        DateTime date;
        try {
            date = new DateTime(timestamp.getTime());
        } catch (Exception e) {
            date = null;
        }
        return date;
    }

    //public CaliperReferrer prepareReferrer(PlatformDeployment platformDeployment){
        //return null;
    //}



    //GETTERS and SETTERS

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public void setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }

    public boolean isCaliperSend() {
        return caliperSend;
    }

    public boolean isCaliperDB() {
        return caliperDB;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public SoftwareApplication getSoftwareApplication() {
        return softwareApplication;
    }

    public JsonldContext getContext() {
        return context;
    }
}
