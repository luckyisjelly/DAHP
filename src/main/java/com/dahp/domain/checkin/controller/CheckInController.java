package com.dahp.domain.checkin.controller;

import com.dahp.domain.checkin.application.CheckInService;
import com.dahp.domain.checkin.controller.dto.CheckInStatusResponse;
import com.dahp.global.response.ApiResponse;
import com.dahp.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/check-ins")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "CheckIn", description = "체크인 (사용자 활동 확인). INACTIVITY_PERIOD 규칙 평가 입력값.")
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping
    @Operation(summary = "수동 체크인", description = "lastCheckInAt을 현재로 갱신하고 nextCheckInDueAt을 자동 재계산합니다.")
    public ApiResponse<CheckInStatusResponse> checkIn(@AuthenticationPrincipal CustomUserDetails userDetails) {
        CheckInStatusResponse status = checkInService.recordCheckIn(userDetails.getUserId());
        return ApiResponse.ok("체크인 완료. 다음 만료일이 갱신되었습니다.", status);
    }

    @GetMapping("/status")
    @Operation(summary = "체크인 상태 조회", description = "마지막 체크인 시각, 다음 만료일, overdue 여부 등")
    public ApiResponse<CheckInStatusResponse> status(@AuthenticationPrincipal CustomUserDetails userDetails) {
        CheckInStatusResponse status = checkInService.status(userDetails.getUserId());
        String msg = status.overdue()
                ? "체크인 만료 상태입니다. INACTIVITY_PERIOD 규칙이 다음 평가 시 자동 트리거될 수 있습니다."
                : String.format("정상. 다음 체크인 만료까지 약 %d일.", Math.max(0, status.daysUntilDue()));
        return ApiResponse.ok(msg, status);
    }
}
