package com.etd.travel_planner.controller;

import com.etd.travel_planner.dto.LocationResponseDTO;
import com.etd.travel_planner.service.interfaces.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/travelrequests/locations")
public class LocationController {

    private final Logger logger = LoggerFactory.getLogger(LocationController.class);
    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping()
    public ResponseEntity<List<LocationResponseDTO>> getAllLocations() {
        logger.info("Inside LocationController :: Fetching all locations");
        List<LocationResponseDTO> locations = locationService.getAllLocations();
        return new ResponseEntity<>(locations, HttpStatus.OK);
    }

}
