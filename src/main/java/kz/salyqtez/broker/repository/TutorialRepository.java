package kz.salyqtez.broker.repository;

import kz.salyqtez.broker.model.Tutorial;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
}
