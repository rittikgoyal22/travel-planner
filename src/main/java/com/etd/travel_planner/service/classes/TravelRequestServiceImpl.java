package com.etd.travel_planner.service.classes;

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
import com.etd.travel_planner.exception.BadRequestException;
import com.etd.travel_planner.exception.InvalidDateRangeException;
import com.etd.travel_planner.exception.NotFoundException;
import com.etd.travel_planner.mapper.TravelRequestMapper;
import com.etd.travel_planner.service.interfaces.TravelRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;
import java.util.Locale;

import static com.etd.travel_planner.constant.AppConstant.APPROVED;
import static com.etd.travel_planner.constant.AppConstant.DATE_RANGE_INVALID;
import static com.etd.travel_planner.constant.AppConstant.LOCATION_ID;
import static com.etd.travel_planner.constant.AppConstant.LOCATION_ID_INVALID;
import static com.etd.travel_planner.constant.AppConstant.NEW;
import static com.etd.travel_planner.constant.AppConstant.PRIORITY;
import static com.etd.travel_planner.constant.AppConstant.PRIORITY_ONE;
import static com.etd.travel_planner.constant.AppConstant.PRIORITY_THREE;
import static com.etd.travel_planner.constant.AppConstant.PRIORITY_TWO;
import static com.etd.travel_planner.constant.AppConstant.REJECTED;
import static com.etd.travel_planner.constant.AppConstant.TRAVEL_DURATION_EXCEEDED;
import static com.etd.travel_planner.constant.AppConstant.TRAVEL_PRIORITY_INVALID;
import static com.etd.travel_planner.constant.AppConstant.TRAVEL_REQUEST_ALREADY_PROCESSED;
import static com.etd.travel_planner.constant.AppConstant.TRAVEL_REQUEST_ID;
import static com.etd.travel_planner.constant.AppConstant.TRAVEL_REQUEST_NOT_FOUND;
import static com.etd.travel_planner.constant.AppConstant.TRAVEL_REQUEST_STATUS;
import static com.etd.travel_planner.constant.AppConstant.TRAVEL_REQUEST_STATUS_INVALID;

@Service
public class TravelRequestServiceImpl implements TravelRequestService {

    private final Logger logger = LoggerFactory.getLogger(TravelRequestServiceImpl.class);
    private final TravelRequestRepo travelRequestRepo;
    private final LocationRepo locationRepo;
    private final TravelRequestMapper travelRequestMapper;
    private final TravelBudgetAllocationRepo travelBudgetAllocationRepo;
    private final MessageSource messageSource;

    public TravelRequestServiceImpl(TravelRequestRepo travelRequestRepo, LocationRepo locationRepo, TravelRequestMapper travelRequestMapper, TravelBudgetAllocationRepo travelBudgetAllocationRepo, MessageSource messageSource) {
        this.travelRequestRepo = travelRequestRepo;
        this.locationRepo = locationRepo;
        this.travelRequestMapper = travelRequestMapper;
        this.travelBudgetAllocationRepo = travelBudgetAllocationRepo;
        this.messageSource = messageSource;
    }

    @Override
    public TravelResponseDTO createTravelRequest(TravelRequestDTO travelRequestDTO) {
        logger.info("Inside TravelRequestServiceImpl :: Creating travel request: {}", travelRequestDTO);

        if(travelRequestDTO.getFromDate().compareTo(travelRequestDTO.getToDate())>0){
            throw new InvalidDateRangeException(messageSource.getMessage(DATE_RANGE_INVALID, null, Locale.ENGLISH));
        }

        long diffInMillis = travelRequestDTO.getToDate().getTime() - travelRequestDTO.getFromDate().getTime();
        long diffInDays = Math.abs(diffInMillis / (24 * 60 * 60 * 1000)) + 1;

        String priority = travelRequestDTO.getPriority();
        validatePriority(diffInDays, priority);

        Location location = locationRepo.findById(travelRequestDTO.getLocationId()).orElse(null);

        if(location == null){
            throw new BadRequestException(messageSource.getMessage(LOCATION_ID_INVALID, new Object[]{travelRequestDTO.getLocationId()}, Locale.ENGLISH), LOCATION_ID);
        }

        TravelRequest travelRequest = travelRequestMapper.mapTravelRequestDTOToTravelRequest(travelRequestDTO, location);
        travelRequestRepo.save(travelRequest);
        return travelRequestMapper.mapTravelRequestToTravelResponseDTO(travelRequest);
    }

    public void validatePriority(long travelDurationDays, String priority) {
        switch(priority)
        {
            case PRIORITY_THREE:
                if(travelDurationDays > 30){
                    throw new InvalidDateRangeException(messageSource.getMessage(TRAVEL_DURATION_EXCEEDED,new Object[]{PRIORITY_THREE, 30}, Locale.ENGLISH));
                }
                break;
            case PRIORITY_TWO:
                if(travelDurationDays>20){
                    throw new InvalidDateRangeException(messageSource.getMessage(TRAVEL_DURATION_EXCEEDED,new Object[]{PRIORITY_TWO, 20}, Locale.ENGLISH));
                }
                break;
            case PRIORITY_ONE:
                if(travelDurationDays>10){
                    throw new InvalidDateRangeException(messageSource.getMessage(TRAVEL_DURATION_EXCEEDED,new Object[]{PRIORITY_ONE, 10}, Locale.ENGLISH));
                }
                break;
            default:
                throw new BadRequestException(messageSource.getMessage(TRAVEL_PRIORITY_INVALID, new Object[]{priority}, Locale.ENGLISH), PRIORITY);
        }
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
            throw new NotFoundException(messageSource.getMessage(TRAVEL_REQUEST_NOT_FOUND, new Object[]{trid}, Locale.ENGLISH), TRAVEL_REQUEST_ID);
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
            throw new NotFoundException(messageSource.getMessage(TRAVEL_REQUEST_NOT_FOUND, new Object[]{trid}, Locale.ENGLISH), TRAVEL_REQUEST_ID);
        }
        if(!NEW.equals(travelRequest.getRequestStatus())){
            throw new BadRequestException(messageSource.getMessage(TRAVEL_REQUEST_ALREADY_PROCESSED, new Object[]{trid}, Locale.ENGLISH), TRAVEL_REQUEST_ID);
        }
        String requestStatus = updateTravelRequestDTO.getRequestStatus().toUpperCase();
        if(!APPROVED.equals(requestStatus) && !REJECTED.equals(requestStatus)){
            throw new BadRequestException(messageSource.getMessage(TRAVEL_REQUEST_STATUS_INVALID, null, Locale.ENGLISH),TRAVEL_REQUEST_STATUS);
        }
        travelRequest.setRequestStatus(requestStatus);
        travelRequest.setRequestApprovedOn(new Date(System.currentTimeMillis()));
        travelRequestRepo.save(travelRequest);
        return travelRequestMapper.mapTravelRequestToTravelResponseDTO(travelRequest);
    }

}
