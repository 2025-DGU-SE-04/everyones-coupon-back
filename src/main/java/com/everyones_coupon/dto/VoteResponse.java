package com.everyones_coupon.dto;

import com.everyones_coupon.domain.FeedbackStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VoteResponse {
    private FeedbackStatusEnum myVote;
    private int validCount;
    private int invalidCount;
}