package com.etd.travel_planner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TravelRequestDTO {

    private Long raisedByEmployeeId;

    private Long toBeApprovedByHrId;

    private Date fromDate;

    private Date toDate;

    private String purposeOfTravel;

    private Long locationId;

    private String priority;

}
