package com.etd.travel_planner.service.classes;

import com.etd.travel_planner.client.AccountManagementClient;
import com.etd.travel_planner.dao.LocationRepo;
import com.etd.travel_planner.dao.TravelBudgetAllocationRepo;
import com.etd.travel_planner.dao.TravelRequestRepo;
import com.etd.travel_planner.dto.TravelRequestDTO;
import com.etd.travel_planner.dto.TravelRequestDetailResponseDTO;
import com.etd.travel_planner.dto.TravelResponseDTO;
import com.etd.travel_planner.dto.UpdateTravelRequestDTO;
import com.etd.travel_planner.entity.Location;
import com.etd.travel_planner.entity.TravelBudgetAllocation;
import com.etd.travel_planner.entity.TravelRequest;
import com.etd.travel_planner.mapper.TravelRequestMapper;
import com.etd.travel_planner.service.interfaces.TravelRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;

@Service
public class TravelRequestServiceImpl implements TravelRequestService {

    private final Logger logger = LoggerFactory.getLogger(TravelRequestServiceImpl.class);
    private final TravelRequestRepo travelRequestRepo;
    private final LocationRepo locationRepo;
    private final TravelRequestMapper travelRequestMapper;
    private final TravelBudgetAllocationRepo travelBudgetAllocationRepo;

    public TravelRequestServiceImpl(TravelRequestRepo travelRequestRepo, LocationRepo locationRepo, TravelRequestMapper travelRequestMapper, TravelBudgetAllocationRepo travelBudgetAllocationRepo) {
        this.travelRequestRepo = travelRequestRepo;
        this.locationRepo = locationRepo;
        this.travelRequestMapper = travelRequestMapper;
        this.travelBudgetAllocationRepo = travelBudgetAllocationRepo;
    }

    @Override
    public TravelResponseDTO createTravelRequest(TravelRequestDTO travelRequestDTO) {
        logger.info("Inside TravelRequestServiceImpl :: Creating travel request: {}", travelRequestDTO);

        if(travelRequestDTO.getFromDate().compareTo(travelRequestDTO.getToDate())>0){
            throw new IllegalArgumentException("From date cannot be after To date");
        }

        long diffInMillis = travelRequestDTO.getToDate().getTime() - travelRequestDTO.getFromDate().getTime();
        long diffInDays = Math.abs(diffInMillis / (24 * 60 * 60 * 1000)) + 1;

        String priority = travelRequestDTO.getPriority();
        switch(priority)
        {
            case "THREE":
                if(diffInDays > 30){
                    throw new IllegalArgumentException("Three priority trips cannot be longer than 30 days");
                }
                break;
            case "TWO":
                if(diffInDays>20){
                    throw new IllegalArgumentException("Two priority trips cannot be longer than 20 days");
                }
                break;
            case "ONE":
                if(diffInDays>10){
                    throw new IllegalArgumentException("One priority trips cannot be longer than 10 days");
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid priority level");
        }

        Location location = locationRepo.findById(travelRequestDTO.getLocationId()).orElse(null);
        if(location == null){
            throw new IllegalArgumentException("Invalid location ID");
        }

        TravelRequest travelRequest = travelRequestMapper.mapTravelRequestDTOToTravelRequest(travelRequestDTO, location);
        travelRequestRepo.save(travelRequest);
        return travelRequestMapper.mapTravelRequestToTravelResponseDTO(travelRequest);

    }

    @Override
    public List<TravelResponseDTO> getPendingTravelRequestByHrId(Long hrId) {
        logger.info("Inside TravelRequestServiceImpl :: Fetching pending travel requests for HR id: {}", hrId);
        List<TravelRequest> travelRequests = travelRequestRepo.findPendingTravelRequestByHrId(hrId);
        return travelRequestMapper.mapTravelRequestListToTravelResponseDTOList(travelRequests);
    }

    @Override
    public TravelRequestDetailResponseDTO getTravelRequestDetailByTravelRequestId(Long trid) {
        logger.info("Inside TravelRequestServiceImpl :: Fetching travel request detail for id: {}", trid);
        TravelRequest travelRequest = travelRequestRepo.findById(trid).orElse(null);
        if(travelRequest == null){
            throw new IllegalArgumentException("Invalid travel request ID");
        }

        TravelBudgetAllocation travelBudgetAllocation = travelBudgetAllocationRepo.findByTravelRequestId(trid);
        if(travelBudgetAllocation == null){
            logger.warn("No budget allocation found for travel request id: {}", trid);
        }
        return travelRequestMapper.mapTravelRequestAndTravelBudgetAllocationToTravelRequestDetailResponseDTO(travelRequest, travelBudgetAllocation);
    }

    @Override
    public TravelResponseDTO updateTravelRequest(Long trid, UpdateTravelRequestDTO updateTravelRequestDTO) {
        logger.info("Inside TravelRequestServiceImpl :: Updating travel request id: {} with status: {}", trid, updateTravelRequestDTO.getRequestStatus());
        TravelRequest travelRequest = travelRequestRepo.findById(trid).orElse(null);
        if(travelRequest == null){
            throw new IllegalArgumentException("Invalid travel request ID");
        }
        if(!"NEW".equals(travelRequest.getRequestStatus())){
            throw new IllegalArgumentException("Travel request has already been processed");
        }
        String requestStatus = updateTravelRequestDTO.getRequestStatus().toUpperCase();
        if(!"APPROVED".equals(requestStatus) && !"REJECTED".equals(requestStatus)){
            throw new IllegalArgumentException("Invalid request status. Must be either APPROVED or REJECTED");
        }

        travelRequest.setRequestStatus(requestStatus);
        travelRequest.setRequestApprovedOn(new Date(System.currentTimeMillis()));
        travelRequestRepo.save(travelRequest);
        return travelRequestMapper.mapTravelRequestToTravelResponseDTO(travelRequest);
    }

}
