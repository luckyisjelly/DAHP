package com.dahp.global.health;

import com.dahp.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "서버 상태 확인")
public class HealthController {

    @GetMapping
    @Operation(summary = "서버 헬스체크", description = "서버가 정상 응답하는지 확인합니다.")
    public ApiResponse<HealthStatus> health() {
        return ApiResponse.ok("DAHP 서버 정상 작동 중", new HealthStatus("UP", "DAHP", OffsetDateTime.now()));
    }

    public record HealthStatus(String status, String application, OffsetDateTime timestamp) {
    }
}
