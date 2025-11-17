package com.etd.travel_planner.controller;

import com.etd.travel_planner.dto.TravelRequestDTO;
import com.etd.travel_planner.dto.TravelRequestDetailResponseDTO;
import com.etd.travel_planner.dto.TravelResponseDTO;
import com.etd.travel_planner.dto.UpdateTravelRequestDTO;
import com.etd.travel_planner.service.interfaces.TravelRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/travelrequests")
public class TravelRequestController {

    private final Logger logger = LoggerFactory.getLogger(TravelRequestController.class);
    private final TravelRequestService travelRequestService;

    public TravelRequestController(TravelRequestService travelRequestService) {
        this.travelRequestService = travelRequestService;
    }

    @PostMapping("/new")
    public ResponseEntity<TravelResponseDTO> createTravelRequest(@RequestBody TravelRequestDTO travelRequestDTO) {
        logger.info("Inside TravelRequestController :: Received new travel request: {}", travelRequestDTO);
        TravelResponseDTO travelResponseDTO = travelRequestService.createTravelRequest(travelRequestDTO);
        return ResponseEntity.ok(travelResponseDTO);
    }

    @GetMapping("/{hrId}/pending")
    public ResponseEntity<List<TravelResponseDTO>> getPendingTravelRequestByHrId(@PathVariable("hrId") Long hrId) {
        logger.info("Inside TravelRequestController :: Fetching pending travel request with HR id: {}", hrId);
        List<TravelResponseDTO> travelResponses = travelRequestService.getPendingTravelRequestByHrId(hrId);
        return ResponseEntity.ok(travelResponses);
    }

    @GetMapping("/{trid}")
    public ResponseEntity<TravelRequestDetailResponseDTO> getTravelRequestDetailByTravelRequestId(@PathVariable("trid") Long trid) {
        logger.info("Inside TravelRequestController :: Fetching travel request detail with id: {}", trid);
        TravelRequestDetailResponseDTO travelRequestDetail = travelRequestService.getTravelRequestDetailByTravelRequestId(trid);
        return ResponseEntity.ok(travelRequestDetail);
    }

    @PutMapping("/{trid}/update")
    public ResponseEntity<TravelResponseDTO> updateTravelRequest(@PathVariable("trid") Long trid, @RequestBody UpdateTravelRequestDTO updateTravelRequestDTO) {
        logger.info("Inside TravelRequestController :: Updating travel request with id: {}", trid);
        TravelResponseDTO updatedTravelResponse = travelRequestService.updateTravelRequest(trid, updateTravelRequestDTO);
        return ResponseEntity.ok(updatedTravelResponse);
    }

}
