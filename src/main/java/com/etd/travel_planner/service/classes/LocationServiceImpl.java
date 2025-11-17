package com.etd.travel_planner.service.classes;

import com.etd.travel_planner.dao.LocationRepo;
import com.etd.travel_planner.dto.LocationResponseDTO;
import com.etd.travel_planner.entity.Location;
import com.etd.travel_planner.mapper.LocationMapper;
import com.etd.travel_planner.service.interfaces.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationServiceImpl implements LocationService {

    private final Logger logger = LoggerFactory.getLogger(LocationServiceImpl.class);
    private final LocationRepo locationRepo;
    private final LocationMapper locationMapper;

    public LocationServiceImpl(LocationRepo locationRepo, LocationMapper locationMapper) {
        this.locationRepo = locationRepo;
        this.locationMapper = locationMapper;
    }

    @Override
    public List<LocationResponseDTO> getAllLocations() {
        logger.info("Inside LocationServiceImpl :: Fetching all locations from the database");
        List<Location> locations = locationRepo.findAll();
        return locationMapper.mapLocationToLocationResponseDTO(locations);
    }

}
