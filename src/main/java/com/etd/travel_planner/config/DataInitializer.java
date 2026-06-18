package com.etd.travel_planner.config;

import com.etd.travel_planner.dao.LocationRepo;
import com.etd.travel_planner.entity.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final LocationRepo locationRepo;

    public DataInitializer(LocationRepo locationRepo) {
        this.locationRepo = locationRepo;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedLocations();
    }

    private void seedLocations() {
        if (locationRepo.count() > 0) {
            logger.info("DataInitializer :: Locations already seeded, skipping.");
            return;
        }
        List<String> locationNames = List.of(
                "Mumbai", "Delhi", "Bangalore", "Chennai",
                "Hyderabad", "Pune", "Kolkata", "Ahmedabad"
        );
        locationNames.forEach(name ->
                locationRepo.save(Location.builder().name(name).build())
        );
        logger.info("DataInitializer :: Seeded {} locations.", locationNames.size());
    }

}
