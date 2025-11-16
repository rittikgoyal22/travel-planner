package com.etd.travel_planner.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "travel_budget_allocations")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TravelBudgetAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "travel_request_id", referencedColumnName = "request_id")
    private TravelRequest travelRequest;

    @Column(name = "approved_budget")
    private Long approvedBudget;

    @Column(name = "approved_mode_of_travel")
    private String approvedModeOfTravel;

    @Column(name = "approved_hotel_star_rating")
    private String approvedHotelStarRating;

}
