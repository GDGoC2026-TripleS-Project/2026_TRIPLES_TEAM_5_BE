package com.triples.team5be.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// PostCreateRequest.java
@Getter
@NoArgsConstructor
public class PostCreateRequest {
    @NotBlank(message = "제목은 필수입니다.")
    @Size(min = 2, max = 30, message = "제목은 2자에서 30자 이내여야 합니다.")
    private String title;

    @NotBlank(message = "모든 칸의 내용을 입력해주세요.")
    @Size(min = 50, max = 300, message = "칸당 50자에서 300자 이내로 작성해주세요.")
    private String situation;

    @NotBlank(message = "모든 칸의 내용을 입력해주세요.")
    @Size(min = 50, max = 300, message = "칸당 50자에서 300자 이내로 작성해주세요.")
    private String action;

    @NotBlank(message = "모든 칸의 내용을 입력해주세요.")
    @Size(min = 50, max = 300, message = "칸당 50자에서 300자 이내로 작성해주세요.")
    private String retrospective;

    private Boolean isPremium;

    private Integer requiredToken;

    private Boolean isAnonymous;
}
