package com.recaudo.api.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.recaudo.api.domain.model.entity.HolidayEntity;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HolidaysRepository extends JpaRepository<HolidayEntity, LocalDate> {

    boolean existsByHoliDateAndHoliStatus(LocalDate holiDate, String holiStatus);

    @Query(value = "SELECT holi_date FROM holidays " +
            "WHERE holi_date > :desde AND holi_date <= :hasta " +
            "AND holi_status = 'A'",
            nativeQuery = true)
    List<java.sql.Date> findActiveBetween(@Param("desde") LocalDate desde,
                                          @Param("hasta") LocalDate hasta);

    @Query(value = "SELECT holi_date FROM holidays WHERE holi_status = 'A'", nativeQuery = true)
    List<java.sql.Date> findAllActive();
}