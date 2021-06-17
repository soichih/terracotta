package edu.iu.terracotta.repository;

import edu.iu.terracotta.model.app.Outcome;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutcomeRepository extends JpaRepository<Outcome, Long> {
    List<Outcome> findByExposure_ExposureId(Long exposureId);

    boolean existsByExposure_Experiment_ExperimentIdAndExposure_ExposureIdAndOutcomeId(Long experimentId, Long exposureId, Long outcomeId);
}