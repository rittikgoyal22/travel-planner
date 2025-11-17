package com.etd.travel_planner.dao;

import com.etd.travel_planner.entity.TravelRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelRequestRepo extends JpaRepository<TravelRequest, Long> {

    @Query("SELECT tr FROM TravelRequest tr WHERE tr.toBeApprovedByHrId = :hrId AND tr.requestStatus = 'NEW'")
    List<TravelRequest> findPendingTravelRequestByHrId(Long hrId);

}
