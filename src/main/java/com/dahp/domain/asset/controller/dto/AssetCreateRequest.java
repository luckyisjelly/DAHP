package com.dahp.domain.asset.controller.dto;

import com.dahp.domain.asset.domain.AssetType;
import com.dahp.domain.asset.domain.SensitivityLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssetCreateRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200, message = "제목은 최대 200자입니다.")
        String title,

        @NotNull(message = "자산 타입은 필수입니다.")
        AssetType type,

        @Size(max = 1000, message = "설명은 최대 1000자입니다.")
        String description,

        String content,

        @Size(max = 500, message = "외부 참조는 최대 500자입니다.")
        String externalRef,

        SensitivityLevel sensitivityLevel
) {
}
