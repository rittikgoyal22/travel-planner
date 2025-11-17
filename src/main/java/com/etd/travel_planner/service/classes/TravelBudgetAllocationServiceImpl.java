package com.etd.travel_planner.service.classes;

import com.etd.travel_planner.client.AccountManagementClient;
import com.etd.travel_planner.dao.TravelBudgetAllocationRepo;
import com.etd.travel_planner.dao.TravelRequestRepo;
import com.etd.travel_planner.dto.TravelBudgetAllocationRequestDTO;
import com.etd.travel_planner.entity.TravelBudgetAllocation;
import com.etd.travel_planner.entity.TravelRequest;
import com.etd.travel_planner.mapper.TravelBudgetAllocationMapper;
import com.etd.travel_planner.service.interfaces.TravelBudgetAllocationService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class TravelBudgetAllocationServiceImpl implements TravelBudgetAllocationService {

    private final Logger logger = LoggerFactory.getLogger(TravelBudgetAllocationServiceImpl.class);
    private final TravelRequestRepo travelRequestRepo;
    private final TravelBudgetAllocationMapper travelBudgetAllocationMapper;
    private final TravelBudgetAllocationRepo travelBudgetAllocationRepo;
    private final AccountManagementClient accountManagementClient;

    public TravelBudgetAllocationServiceImpl(TravelRequestRepo travelRequestRepo, TravelBudgetAllocationMapper travelBudgetAllocationMapper, TravelBudgetAllocationRepo travelBudgetAllocationRepo, AccountManagementClient accountManagementClient) {
        this.travelRequestRepo = travelRequestRepo;
        this.travelBudgetAllocationMapper = travelBudgetAllocationMapper;
        this.travelBudgetAllocationRepo = travelBudgetAllocationRepo;
        this.accountManagementClient = accountManagementClient;
    }

    @Override
    public Long calculateBudget(TravelBudgetAllocationRequestDTO travelBudgetAllocationRequestDTO) {
        logger.info("Inside TravelBudgetAllocationServiceImpl :: Calculating budget for request: {}", travelBudgetAllocationRequestDTO);

        TravelRequest travelRequest = travelRequestRepo.findById(travelBudgetAllocationRequestDTO.getTravelRequestId())
                .orElseThrow(()-> new IllegalArgumentException("Travel Request not found with id: " + travelBudgetAllocationRequestDTO.getTravelRequestId()));
        if(!travelRequest.getRequestStatus().equals("APPROVED"))
        {
            throw new IllegalArgumentException("Travel Request with id " + travelBudgetAllocationRequestDTO.getTravelRequestId() + " is not approved yet.");
        }

        ObjectNode employeeDetail = accountManagementClient.getEmployeeById(travelRequest.getRaisedByEmployeeId());
        String employeeRole = employeeDetail.get("role").asText();
        ArrayList<String> allowedHotelTypes = new ArrayList<>();
        if(Objects.equals(employeeRole, "HR"))
        {
            allowedHotelTypes.add("5-STAR");
            allowedHotelTypes.add("7-STAR");
        }
        else
        {
            allowedHotelTypes.add("3-STAR");
            allowedHotelTypes.add("5-STAR");
        }

        if(!allowedHotelTypes.contains(travelBudgetAllocationRequestDTO.getApprovedHotelStarRating()))
        {
            throw new IllegalArgumentException("Employee with role " + employeeRole + " is not allowed to book hotel type " + travelBudgetAllocationRequestDTO.getApprovedHotelStarRating());
        }
        List<String> allowedTravelModes = List.of("AIR", "TRAIN", "BUS");
        if(!allowedTravelModes.contains(travelBudgetAllocationRequestDTO.getApprovedModeOfTravel()))
        {
            throw new IllegalArgumentException("Invalid travel mode: " + travelBudgetAllocationRequestDTO.getApprovedModeOfTravel());
        }

        long diffInMillis = travelRequest.getToDate().getTime() - travelRequest.getFromDate().getTime();
        long diffInDays = Math.abs(diffInMillis / (24 * 60 * 60 * 1000)) + 1;

        String employeeGrade = employeeDetail.get("gradeName").asText();
        Long maxBudgetPerDay = switch (employeeGrade) {
            case "Grade-1" -> 15000L;
            case "Grade-2" -> 12500L;
            case "Grade-3" -> 10000L;
            default -> throw new IllegalArgumentException("Invalid employee grade: " + employeeGrade);
        };
        Long totalBudget = maxBudgetPerDay * diffInDays;
        logger.info("Calculated total budget: {}", totalBudget);

        TravelBudgetAllocation travelBudgetAllocation = travelBudgetAllocationMapper.mapTravelBudgetAllocationByTravelBudgetAllocationRequestDTO(travelBudgetAllocationRequestDTO, totalBudget, travelRequest);
        travelBudgetAllocationRepo.save(travelBudgetAllocation);
        return totalBudget;
    }

}
