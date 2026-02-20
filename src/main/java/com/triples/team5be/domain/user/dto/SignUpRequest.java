package com.triples.team5be.domain.user.dto;

import com.triples.team5be.domain.user.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SignUpRequest (

        @NotBlank(message = "아이디는 필수입니다.")
        @Size(min = 4, max = 10, message = "아이디는 4자에서 10자 사이여야 합니다.")
        String loginId,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,16}$",
                message = "비밀번호는 8~16자이며, 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다."
        )
        String password,

        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 30, message = "사용자 이름은 30자를 초과할 수 없습니다.")
        String userName,

        @NotBlank(message = "생년월일을 입력해주세요.")
        LocalDate birthDate,

        @NotBlank(message = "성별을 입력해주세요.")
        Gender gender,

        @NotBlank(message = "핸드폰 번호를 입력해 주세요.")
        String phoneNumber,

        Boolean thirdPartyConsent,

        Boolean marketingConsent
) {
}
