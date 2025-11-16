package com.etd.travel_planner.dao;

import com.etd.travel_planner.entity.TravelRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelRequestRepo extends JpaRepository<TravelRequest, Long> {

}
