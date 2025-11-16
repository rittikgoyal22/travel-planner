package com.etd.travel_planner.service.interfaces;

import com.etd.travel_planner.dto.LocationResponseDTO;

import java.util.List;

public interface LocationService {

    List<LocationResponseDTO> getAllLocations();

}
