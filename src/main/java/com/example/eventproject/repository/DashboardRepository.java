package com.example.eventproject.repository;
import com.example.eventproject.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository extends JpaRepository<Registration, Integer> {

    // ---- All Events ----
    @Query("SELECT COUNT(e) FROM Event e WHERE e.status = 'OPEN'")
    long countActiveEvents();

    @Query("SELECT COUNT(r) FROM Registration r WHERE r.paymentStatus = 'PAID'")
    long countTicketsSoldAll();

    @Query("SELECT COUNT(r) FROM Registration r")
    long countRegistrationsAll();

    @Query("SELECT COUNT(r) FROM Registration r WHERE r.isCheckedIn = true")
    long countCheckinAll();

    // ---- Filter by Event ----
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.event.id = :eventId")
    long countSignupsByEvent(@Param("eventId") int eventId);

    @Query("SELECT COUNT(r) FROM Registration r " +
            "WHERE r.event.id = :eventId AND r.paymentStatus != 'PAID'")
    long countDropoffsByEvent(@Param("eventId") int eventId);

    @Query("SELECT COUNT(r) FROM Registration r " +
            "WHERE r.event.id = :eventId AND r.paymentStatus = 'PAID'")
    long countTicketsSoldByEvent(@Param("eventId") int eventId);

    @Query("SELECT COUNT(r) FROM Registration r " +
            "WHERE r.event.id = :eventId AND r.isCheckedIn = true")
    long countCheckinByEvent(@Param("eventId") int eventId);

    // หาจำนวน sold ต่อ event
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.event.id = :eventId AND r.paymentStatus = 'PAID'")
    long countSoldByEvent(@Param("eventId") int eventId);

    // หา capacity = sum(z.capacity)
    @Query("SELECT COALESCE(SUM(z.capacity),0) FROM EventZone z WHERE z.session.event.id = :eventId")
    long sumCapacityByEvent(@Param("eventId") int eventId);

}




