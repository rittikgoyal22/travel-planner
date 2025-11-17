package com.etd.travel_planner.service.interfaces;

import com.etd.travel_planner.dto.TravelBudgetAllocationRequestDTO;

public interface TravelBudgetAllocationService {

    Long calculateBudget(TravelBudgetAllocationRequestDTO travelBudgetAllocationRequestDTO);

}
