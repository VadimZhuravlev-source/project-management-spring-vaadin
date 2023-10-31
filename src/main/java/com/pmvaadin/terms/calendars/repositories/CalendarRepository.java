package com.pmvaadin.terms.calendars.repositories;

import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calendars.entity.CalendarImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CalendarRepository extends Repository<CalendarImpl, Integer> {

    List<Calendar> findAll();
    List<Calendar> findAllById(Iterable<?> ids);
    <I> Optional<Calendar> findById(I id);
    Calendar save(Calendar calendar);
    void deleteAllById(Iterable<?> ids);
    void deleteById(Integer id);
    <T> List<T> findAllByIdIn(@Param("id") Iterable<?> ids, Class<T> type);

    <T> List<T> findByNameLikeIgnoreCase(String name, Pageable pageable, Class<T> type);
//    int countByNameLikeIgnoreCase(String name, Pageable pageable);

    @Query( value = """
            WITH predefined_calendars AS(
            SELECT\s
            	id,
            	name,
            	settings_id,
            	start_time,
            	predefined
            FROM calendars\s
            WHERE\s
            id = ANY(ids)\s
            	AND predefined
            ),
                        
            used_calendars_ids AS(
            SELECT DISTINCT
            	calendar_id id
            FROM project_tasks
            WHERE
            	project_tasks.calendar_id = ANY(ids)
            ),
                        
            used_calendars AS (
            SELECT
            	id,
            	name,
            	settings_id,
            	start_time,
            	predefined
            FROM calendars
            WHERE
            	id IN(SELECT id FROM used_calendars_ids)
            )
                        
            SELECT DISTINCT\s
            	*
            FROM predefined_calendars
                        
            UNION
                        
            SELECT
            	*
            FROM used_calendars
            """, nativeQuery = true)
    <T> List<T> findCalendarsThatCannotBeDeleted(List<?> ids, Class<T> type);

}
