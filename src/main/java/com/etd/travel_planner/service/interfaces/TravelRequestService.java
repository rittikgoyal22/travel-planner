package com.etd.travel_planner.service.interfaces;

import com.etd.travel_planner.dto.TravelRequestDTO;
import com.etd.travel_planner.dto.TravelRequestDetailResponseDTO;
import com.etd.travel_planner.dto.TravelResponseDTO;
import com.etd.travel_planner.dto.UpdateTravelRequestDTO;

import java.util.List;

public interface TravelRequestService {

    TravelResponseDTO createTravelRequest(TravelRequestDTO travelRequestDTO);

    List<TravelResponseDTO> getPendingTravelRequestByHrId(Long hrId);

    TravelRequestDetailResponseDTO getTravelRequestDetailByTravelRequestId(Long trid);

    TravelResponseDTO updateTravelRequest(Long trid, UpdateTravelRequestDTO updateTravelRequestDTO);

}
