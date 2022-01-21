package kz.salyqtez.broker.repository;

import kz.salyqtez.broker.model.Excel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ExcelRepository extends JpaRepository<Excel, Long> {
    @Modifying
    @Query("update Excel e set e.botSendFileId = ?1 where  e.botUserId = ?2 and e.botActionTime = ?3")
    void updateSendFileId(String botSendFileId, Long botUserId, Integer botActionTime);
}
