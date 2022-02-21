package kz.salyqtez.broker.repository;

import kz.salyqtez.broker.model.Excel;
import kz.salyqtez.broker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select distinct botUserId from User")
    List<Long> findAllBotId();
}
