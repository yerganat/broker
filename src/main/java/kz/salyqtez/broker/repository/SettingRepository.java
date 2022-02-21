package kz.salyqtez.broker.repository;

import kz.salyqtez.broker.model.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SettingRepository extends JpaRepository<Setting, Long> {

    Setting findByName(String name);

    @Modifying
    @Query("update Setting e set e.value = :val where  e.name = :name")
    void updateSettings(String val, String name);
}
