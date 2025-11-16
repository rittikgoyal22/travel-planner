package com.etd.travel_planner.mapper;

import com.etd.travel_planner.dto.LocationResponseDTO;
import com.etd.travel_planner.entity.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocationMapper {

    private final Logger logger = LoggerFactory.getLogger(LocationMapper.class);

    public List<LocationResponseDTO> mapLocationToLocationResponseDTO(List<Location> locations) {
        logger.info("Inside Location Mapper :: Mapping Location entities to LocationResponseDTOs");
        return locations.stream()
                .map(location ->
                        LocationResponseDTO
                                .builder()
                                .id(location.getId())
                                .name(location.getName())
                                .build())
                .toList();
    }

}
