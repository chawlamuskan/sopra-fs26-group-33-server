package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;
import java.util.List;

import ch.uzh.ifi.hase.soprafs26.constant.PrivacyLevel;

public class TravelBoardGetDTO {

    private Long id;

    private String name;

    private String location;
    
    private LocalDate startDate;
    
    private LocalDate endDate;

    private Long ownerId;

    private List<Long> memberIds;
    
    private String inviteCode;

    private PrivacyLevel privacy;
    
    private LocalDate dateCreated;

    private List<ActivityLogDTO> activityLogs;
    
    private Double latMin;
    private Double latMax;
    private Double lngMin;
    private Double lngMax;

    private String countryCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public List<Long> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Long> memberIds) {
        this.memberIds = memberIds;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public PrivacyLevel getPrivacy() {
        return privacy;
    }

    public void setPrivacy(PrivacyLevel privacy) {
        this.privacy = privacy;
    }

        public LocalDate getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }

    public List<ActivityLogDTO> getActivityLogs() { return activityLogs; }
    public void setActivityLogs(List<ActivityLogDTO> activityLogs) { this.activityLogs = activityLogs; }

    public Double getLatMin() { return latMin; }
    public void setLatMin(Double latMin) { this.latMin = latMin; }

    public Double getLatMax() { return latMax; }
    public void setLatMax(Double latMax) { this.latMax = latMax; }

    public Double getLngMin() { return lngMin; }
    public void setLngMin(Double lngMin) { this.lngMin = lngMin; }

    public Double getLngMax() { return lngMax; }
    public void setLngMax(Double lngMax) { this.lngMax = lngMax; }

    public String getCountryCode() {
        return countryCode;
    }
    
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}