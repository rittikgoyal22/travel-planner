package com.etd.travel_planner.controller;

import com.etd.travel_planner.dto.TravelBudgetAllocationRequestDTO;
import com.etd.travel_planner.service.interfaces.TravelBudgetAllocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/travelrequests")
public class TravelBudgetAllocationController {

    private final Logger logger = LoggerFactory.getLogger(TravelBudgetAllocationController.class);
    private final TravelBudgetAllocationService travelBudgetAllocationService;

    public TravelBudgetAllocationController(TravelBudgetAllocationService travelBudgetAllocationService) {
        this.travelBudgetAllocationService = travelBudgetAllocationService;
    }

    @PostMapping("/calculatebudget")
    public ResponseEntity<Long> calculateBudget(@RequestBody TravelBudgetAllocationRequestDTO travelBudgetAllocationRequestDTO) {
        logger.info("Inside TravelBudgetAllocationController :: Calculating budget for travel request: {}", travelBudgetAllocationRequestDTO);
        Long budget = travelBudgetAllocationService.calculateBudget(travelBudgetAllocationRequestDTO);
        return ResponseEntity.ok(budget);
    }

}
