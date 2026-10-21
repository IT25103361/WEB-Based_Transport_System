package com.webtrans.admin.dto;

public class TrendPointDto {
    private String dateLabel;
    private long rideCount;
    private long rentalCount;
    private long schoolCount;
    private long totalCount;

    public TrendPointDto() {}

    public TrendPointDto(String dateLabel, long rideCount, long rentalCount, long schoolCount) {
        this.dateLabel = dateLabel;
        this.rideCount = rideCount;
        this.rentalCount = rentalCount;
        this.schoolCount = schoolCount;
        this.totalCount = rideCount + rentalCount + schoolCount;
    }

    public String getDateLabel() { return dateLabel; }
    public void setDateLabel(String dateLabel) { this.dateLabel = dateLabel; }

    public long getRideCount() { return rideCount; }
    public void setRideCount(long rideCount) { this.rideCount = rideCount; }

    public long getRentalCount() { return rentalCount; }
    public void setRentalCount(long rentalCount) { this.rentalCount = rentalCount; }

    public long getSchoolCount() { return schoolCount; }
    public void setSchoolCount(long schoolCount) { this.schoolCount = schoolCount; }

    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
}
