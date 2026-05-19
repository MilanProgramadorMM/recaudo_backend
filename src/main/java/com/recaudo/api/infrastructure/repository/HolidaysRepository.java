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

    @Query(value = "SELECT COUNT(*) > 0 FROM holidays WHERE holi_date = :fecha AND holi_status = 'A'",
            nativeQuery = true)
    boolean existsByHoliDateAndActive(@Param("fecha") LocalDate fecha);

    @Query(value = "SELECT holi_date FROM holidays " +
            "WHERE holi_date > :desde AND holi_date <= :hasta " +
            "AND holi_status = 'A'",
            nativeQuery = true)
    List<java.sql.Date> findActiveBetween(@Param("desde") LocalDate desde,
                                          @Param("hasta") LocalDate hasta);
}