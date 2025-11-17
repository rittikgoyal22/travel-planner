package com.etd.travel_planner.mapper;

import com.etd.travel_planner.dto.TravelRequestDTO;
import com.etd.travel_planner.dto.TravelRequestDetailResponseDTO;
import com.etd.travel_planner.dto.TravelResponseDTO;
import com.etd.travel_planner.entity.Location;
import com.etd.travel_planner.entity.TravelBudgetAllocation;
import com.etd.travel_planner.entity.TravelRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.List;

@Component
public class TravelRequestMapper {

    Logger logger = LoggerFactory.getLogger(TravelRequestMapper.class);

    public TravelRequest mapTravelRequestDTOToTravelRequest(TravelRequestDTO travelRequestDTO, Location location) {
        logger.info("Inside TravelRequestMapper :: Mapping TravelRequestDTO to TravelRequest");
        return TravelRequest.builder()
                .raisedByEmployeeId(travelRequestDTO.getRaisedByEmployeeId())
                .toBeApprovedByHrId(travelRequestDTO.getToBeApprovedByHrId())
                .requestRaisedOn(new Date(System.currentTimeMillis()))
                .fromDate(travelRequestDTO.getFromDate())
                .toDate(travelRequestDTO.getToDate())
                .purposeOfTravel(travelRequestDTO.getPurposeOfTravel())
                .location(location)
                .requestStatus("NEW")
                .priority(travelRequestDTO.getPriority())
                .build();
    }

    public TravelResponseDTO mapTravelRequestToTravelResponseDTO(TravelRequest travelRequest) {
        logger.info("Inside TravelRequestMapper :: Mapping TravelRequest to TravelResponseDTO");
        return TravelResponseDTO.builder()
                .requestId(travelRequest.getRequestId())
                .raisedByEmployeeId(travelRequest.getRaisedByEmployeeId())
                .toBeApprovedByHrId(travelRequest.getToBeApprovedByHrId())
                .requestRaisedOn(travelRequest.getRequestRaisedOn())
                .fromDate(travelRequest.getFromDate())
                .toDate(travelRequest.getToDate())
                .purposeOfTravel(travelRequest.getPurposeOfTravel())
                .locationName(travelRequest.getLocation().getName())
                .requestStatus(travelRequest.getRequestStatus())
                .requestApprovedOn(travelRequest.getRequestApprovedOn())
                .priority(travelRequest.getPriority())
                .build();
    }

    public List<TravelResponseDTO> mapTravelRequestListToTravelResponseDTOList(List<TravelRequest> travelRequests) {
        logger.info("Inside TravelRequestMapper :: Mapping List<TravelRequest> to List<TravelResponseDTO>");
        return travelRequests.stream()
                .map(this::mapTravelRequestToTravelResponseDTO)
                .toList();
    }

    public TravelRequestDetailResponseDTO mapTravelRequestAndTravelBudgetAllocationToTravelRequestDetailResponseDTO(TravelRequest travelRequest, TravelBudgetAllocation travelBudgetAllocation) {
        logger.info("Inside TravelRequestMapper :: Mapping TravelRequest and TravelBudgetAllocation to TravelRequestDetailResponseDTO");
        TravelRequestDetailResponseDTO travelRequestDetailResponseDTO = TravelRequestDetailResponseDTO.builder()
                .requestId(travelRequest.getRequestId())
                .raisedByEmployeeId(travelRequest.getRaisedByEmployeeId())
                .toBeApprovedByHrId(travelRequest.getToBeApprovedByHrId())
                .requestRaisedOn(travelRequest.getRequestRaisedOn())
                .fromDate(travelRequest.getFromDate())
                .toDate(travelRequest.getToDate())
                .purposeOfTravel(travelRequest.getPurposeOfTravel())
                .locationName(travelRequest.getLocation().getName())
                .requestStatus(travelRequest.getRequestStatus())
                .requestApprovedOn(travelRequest.getRequestApprovedOn())
                .priority(travelRequest.getPriority()).build();
        if(travelBudgetAllocation != null) {
            travelRequestDetailResponseDTO.setTravelBudgetAllocationId(travelBudgetAllocation.getId());
            travelRequestDetailResponseDTO.setApprovedModeOfTravel(travelBudgetAllocation.getApprovedModeOfTravel());
            travelRequestDetailResponseDTO.setApprovedBudget(travelBudgetAllocation.getApprovedBudget());
            travelRequestDetailResponseDTO.setApprovedHotelStarRating(travelBudgetAllocation.getApprovedHotelStarRating());
        }
        return travelRequestDetailResponseDTO;
    }

}
