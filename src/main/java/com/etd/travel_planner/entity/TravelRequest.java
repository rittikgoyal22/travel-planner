package com.etd.travel_planner.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "travel_requests")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TravelRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "raised_by_employee_id")
    private Long raisedByEmployeeId;

    @Column(name = "to_be_approved_by_hr_id")
    private Long toBeApprovedByHrId;

    @Column(name = "request_raised_on")
    private Date requestRaisedOn;

    @Column(name = "from_date")
    private Date fromDate;

    @Column(name = "to_date")
    private Date toDate;

    @Column(name = "purpose_of_travel")
    private String purposeOfTravel;

    @ManyToOne
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private Location location;

    @Column(name = "request_status")
    private String requestStatus;

    @Column(name = "request_approved_on")
    private Date requestApprovedOn;

    @Column(name = "priority")
    private String priority;

    @OneToOne(mappedBy = "travelRequest")
    private TravelBudgetAllocation budgetAllocation;

}
