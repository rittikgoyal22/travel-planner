package com.etd.travel_planner.service.classes;

import com.etd.travel_planner.client.AccountManagementClient;
import com.etd.travel_planner.dao.TravelBudgetAllocationRepo;
import com.etd.travel_planner.dao.TravelRequestRepo;
import com.etd.travel_planner.dto.TravelBudgetAllocationRequestDTO;
import com.etd.travel_planner.entity.TravelBudgetAllocation;
import com.etd.travel_planner.entity.TravelRequest;
import com.etd.travel_planner.exception.BadRequestException;
import com.etd.travel_planner.exception.IllegalArgumentException;
import com.etd.travel_planner.exception.NotFoundException;
import com.etd.travel_planner.mapper.TravelBudgetAllocationMapper;
import com.etd.travel_planner.service.interfaces.TravelBudgetAllocationService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.etd.travel_planner.constant.AppConstant.AIR;
import static com.etd.travel_planner.constant.AppConstant.APPROVED;
import static com.etd.travel_planner.constant.AppConstant.APPROVED_HOTEL_STAR_RATING;
import static com.etd.travel_planner.constant.AppConstant.APPROVED_MODE_OF_TRAVEL;
import static com.etd.travel_planner.constant.AppConstant.BUS;
import static com.etd.travel_planner.constant.AppConstant.EMPLOYEE_GRADE;
import static com.etd.travel_planner.constant.AppConstant.EMPLOYEE_GRADE_INVALID;
import static com.etd.travel_planner.constant.AppConstant.EMPLOYEE_ROLE_NOT_ALLOWED_HOTEL;
import static com.etd.travel_planner.constant.AppConstant.GRADE;
import static com.etd.travel_planner.constant.AppConstant.GRADE_1;
import static com.etd.travel_planner.constant.AppConstant.GRADE_2;
import static com.etd.travel_planner.constant.AppConstant.GRADE_3;
import static com.etd.travel_planner.constant.AppConstant.HR;
import static com.etd.travel_planner.constant.AppConstant.ROLE;
import static com.etd.travel_planner.constant.AppConstant.STAR_3;
import static com.etd.travel_planner.constant.AppConstant.STAR_5;
import static com.etd.travel_planner.constant.AppConstant.STAR_7;
import static com.etd.travel_planner.constant.AppConstant.TRAIN;
import static com.etd.travel_planner.constant.AppConstant.TRAVEL_MODE_INVALID;
import static com.etd.travel_planner.constant.AppConstant.TRAVEL_REQUEST_ID;
import static com.etd.travel_planner.constant.AppConstant.TRAVEL_REQUEST_NOT_APPROVED;
import static com.etd.travel_planner.constant.AppConstant.TRAVEL_REQUEST_NOT_FOUND;
import static com.etd.travel_planner.constant.AppConstant.TRAVEL_REQUEST_STATUS;

@Service
public class TravelBudgetAllocationServiceImpl implements TravelBudgetAllocationService {

    private final Logger logger = LoggerFactory.getLogger(TravelBudgetAllocationServiceImpl.class);
    private final TravelRequestRepo travelRequestRepo;
    private final TravelBudgetAllocationMapper travelBudgetAllocationMapper;
    private final TravelBudgetAllocationRepo travelBudgetAllocationRepo;
    private final AccountManagementClient accountManagementClient;
    private final MessageSource messageSource;

    public TravelBudgetAllocationServiceImpl(TravelRequestRepo travelRequestRepo, TravelBudgetAllocationMapper travelBudgetAllocationMapper, TravelBudgetAllocationRepo travelBudgetAllocationRepo, AccountManagementClient accountManagementClient, MessageSource messageSource) {
        this.travelRequestRepo = travelRequestRepo;
        this.travelBudgetAllocationMapper = travelBudgetAllocationMapper;
        this.travelBudgetAllocationRepo = travelBudgetAllocationRepo;
        this.accountManagementClient = accountManagementClient;
        this.messageSource = messageSource;
    }

    @Override
    public Long calculateBudget(TravelBudgetAllocationRequestDTO travelBudgetAllocationRequestDTO) {
        logger.info("Inside TravelBudgetAllocationServiceImpl :: Calculating budget for request: {}", travelBudgetAllocationRequestDTO);

        TravelRequest travelRequest = travelRequestRepo.findById(travelBudgetAllocationRequestDTO.getTravelRequestId())
                .orElseThrow(()-> new NotFoundException(messageSource.getMessage(TRAVEL_REQUEST_NOT_FOUND, new Object[]{travelBudgetAllocationRequestDTO.getTravelRequestId()}, Locale.ENGLISH), TRAVEL_REQUEST_ID));

        if(!APPROVED.equals(travelRequest.getRequestStatus()))
        {
            throw new IllegalArgumentException(messageSource.getMessage(TRAVEL_REQUEST_NOT_APPROVED, new Object[]{travelRequest.getRequestId()}, Locale.ENGLISH), TRAVEL_REQUEST_STATUS);
        }

        ObjectNode employeeDetail = accountManagementClient.getEmployeeById(travelRequest.getRaisedByEmployeeId());
        String employeeRole = employeeDetail.get(ROLE).asText();
        List<String> allowedHotelTypes = allowedHotel(employeeRole);

        if(!allowedHotelTypes.contains(travelBudgetAllocationRequestDTO.getApprovedHotelStarRating()))
        {
            throw new IllegalArgumentException(messageSource.getMessage(EMPLOYEE_ROLE_NOT_ALLOWED_HOTEL, new Object[]{employeeRole, travelBudgetAllocationRequestDTO.getApprovedHotelStarRating()}, Locale.ENGLISH), APPROVED_HOTEL_STAR_RATING);
        }

        List<String> allowedTravelModes = List.of(AIR, TRAIN, BUS);

        if(!allowedTravelModes.contains(travelBudgetAllocationRequestDTO.getApprovedModeOfTravel()))
        {
            throw new IllegalArgumentException(messageSource.getMessage(TRAVEL_MODE_INVALID, new Object[]{travelBudgetAllocationRequestDTO.getApprovedModeOfTravel()}, Locale.ENGLISH), APPROVED_MODE_OF_TRAVEL);
        }

        long diffInMillis = travelRequest.getToDate().getTime() - travelRequest.getFromDate().getTime();
        long diffInDays = Math.abs(diffInMillis / (24 * 60 * 60 * 1000));

        Long maxBudgetPerDay = maxBudgetPerDayByGrade(employeeDetail.get(GRADE).asText());
        Long totalBudget = maxBudgetPerDay * diffInDays;

        TravelBudgetAllocation travelBudgetAllocation = travelBudgetAllocationMapper.mapTravelBudgetAllocationByTravelBudgetAllocationRequestDTO(travelBudgetAllocationRequestDTO, totalBudget, travelRequest);
        travelBudgetAllocationRepo.save(travelBudgetAllocation);
        return totalBudget;
    }

    public Long maxBudgetPerDayByGrade(String gradeName)
    {
        return switch (gradeName) {
            case GRADE_1 -> 15000L;
            case GRADE_2 -> 12500L;
            case GRADE_3 -> 10000L;
            default -> throw new BadRequestException(messageSource.getMessage(EMPLOYEE_GRADE_INVALID, new Object[]{gradeName}, Locale.ENGLISH), EMPLOYEE_GRADE);
        };
    }

    public List<String> allowedHotel(String employeeRole)
    {
        if(Objects.equals(employeeRole, HR))
        {
            return List.of(STAR_5, STAR_7);
        }
        else
        {
            return List.of(STAR_3, STAR_5);
        }
    }

}
