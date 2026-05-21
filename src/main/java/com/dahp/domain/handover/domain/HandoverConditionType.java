package com.dahp.domain.handover.domain;

public enum HandoverConditionType {
    /** 소유자가 수동 trigger API를 호출하면 발동 */
    MANUAL_APPROVAL,

    /** conditionValue(ISO-8601 날짜)가 지나면 자동 trigger */
    SPECIFIC_DATE,

    /** 소유자의 lastCheckInAt이 conditionValue(일수) 이상 경과 시 자동 trigger */
    INACTIVITY_PERIOD
}
