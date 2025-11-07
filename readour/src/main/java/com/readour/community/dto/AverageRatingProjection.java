package com.readour.community.dto;

public interface AverageRatingProjection {
    Long getBookId();
    Double getAverageRating();
    Long getReviewCount();
}