package com.dahp.domain.handover.controller;

import com.dahp.domain.handover.application.HandoverEventService;
import com.dahp.domain.handover.controller.dto.HandoverAccessResponse;
import com.dahp.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/handover-access")
@RequiredArgsConstructor
@Tag(name = "HandoverAccess",
        description = "수령인 자산 조회 (인증 없음, 토큰 기반). 1회만 사용 가능합니다.")
public class HandoverAccessController {

    private final HandoverEventService eventService;

    @GetMapping("/{token}")
    @Operation(summary = "수령인 토큰 접근 (공개 엔드포인트)",
            description = """
                    수령인이 이메일/메시지로 받은 토큰으로 자산을 1회 조회합니다.
                    - 토큰 잘못됨 → 404 INVALID_ACCESS_TOKEN
                    - 만료 → 410 ACCESS_TOKEN_EXPIRED
                    - 이미 사용 → 410 ACCESS_TOKEN_USED
                    - 소유자가 취소 → 410 ACCESS_TOKEN_CANCELLED
                    """)
    public ApiResponse<HandoverAccessResponse> access(@PathVariable String token) {
        HandoverAccessResponse response = eventService.access(token);
        return ApiResponse.ok("자산이 조회되었습니다. 이 링크는 더 이상 사용할 수 없습니다.", response);
    }
}
