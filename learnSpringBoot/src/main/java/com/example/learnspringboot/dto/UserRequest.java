package com.example.learnspringboot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;


@Getter
@NoArgsConstructor
public class UserRequest {
    @Schema(description = "사용자 이름", example = "홍길동")
    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @Schema(description = "이메일 주소", example = "hong@example.com")
    @Email(message = "이메일 형식이 아닙니다.")
    private String email;
}
