package com.etd.travel_planner.mapper;

import com.etd.travel_planner.dto.TravelBudgetAllocationRequestDTO;
import com.etd.travel_planner.entity.TravelBudgetAllocation;
import com.etd.travel_planner.entity.TravelRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TravelBudgetAllocationMapper {

    private final Logger logger = LoggerFactory.getLogger(TravelBudgetAllocationMapper.class);

    public TravelBudgetAllocation mapTravelBudgetAllocationByTravelBudgetAllocationRequestDTO(TravelBudgetAllocationRequestDTO travelBudgetAllocationRequestDTO, Long totalBudget, TravelRequest travelRequest) {
        logger.info("Inside TravelBudgetAllocationMapper :: Mapping TravelBudgetAllocationRequestDTO to TravelBudgetAllocation entity");
        return TravelBudgetAllocation.builder()
                .approvedBudget(totalBudget)
                .travelRequest(travelRequest)
                .approvedModeOfTravel(travelBudgetAllocationRequestDTO.getApprovedModeOfTravel())
                .approvedHotelStarRating(travelBudgetAllocationRequestDTO.getApprovedHotelStarRating())
                .build();
    }

}
