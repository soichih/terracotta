package edu.iu.terracotta.model.app;

import edu.iu.terracotta.model.LtiUserEntity;
import edu.iu.terracotta.model.app.enumerator.Source;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Table(name = "terr_participant")
@Entity
public class Participant {
    @Column(name = "participant_id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long participantId;

    @JoinColumn(name = "experiment_experiment_id", nullable = false)
    @ManyToOne(optional = false)
    private Experiment experiment;

    @JoinColumn(name = "lti_user_entity_user_id", nullable = false)
    @OneToOne(optional = false)
    private LtiUserEntity ltiUserEntity;

    @Column(name = "consent")
    private Boolean consent;

    @Column(name = "date_given")
    private Timestamp dateGiven;

    @Column(name = "date_revoked")
    private Timestamp dateRevoked;

    @Enumerated(EnumType.STRING)
    @Column(name = "source")
    private Source source;

    public LtiUserEntity getLtiUserEntity() {
        return ltiUserEntity;
    }

    public void setLtiUserEntity(LtiUserEntity ltiUserEntity) {
        this.ltiUserEntity = ltiUserEntity;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public Long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Long participantId) {
        this.participantId = participantId;
    }

    public Boolean getConsent() { return consent; }

    public void setConsent(Boolean consent) { this.consent = consent; }

    public Timestamp getDateGiven() { return dateGiven; }

    public void setDateGiven(Timestamp dateGiven) { this.dateGiven = dateGiven; }

    public Timestamp getDateRevoked() { return dateRevoked; }

    public void setDateRevoked(Timestamp dateRevoked) { this.dateRevoked = dateRevoked; }

    public Source getSource() { return source; }

    public void setSource(Source source) { this.source = source; }
}