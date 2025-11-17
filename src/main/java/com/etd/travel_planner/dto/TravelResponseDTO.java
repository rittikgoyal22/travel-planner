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
public class TravelResponseDTO {

    private Long requestId;

    private Long raisedByEmployeeId;

    private Long toBeApprovedByHrId;

    private Date requestRaisedOn;

    private Date fromDate;

    private Date toDate;

    private String purposeOfTravel;

    private String locationName;

    private String requestStatus;

    private Date requestApprovedOn;

    private String priority;

}
