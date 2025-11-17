package com.etd.travel_planner.dao;

import com.etd.travel_planner.entity.TravelBudgetAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelBudgetAllocationRepo extends JpaRepository<TravelBudgetAllocation, Long> {

    @Query("SELECT tba FROM TravelBudgetAllocation tba WHERE tba.travelRequest.id = :travelRequestId")
    TravelBudgetAllocation findByTravelRequestId(Long travelRequestId);

}
