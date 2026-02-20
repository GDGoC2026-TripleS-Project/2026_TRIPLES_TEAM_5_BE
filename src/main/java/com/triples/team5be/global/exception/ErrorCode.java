package com.triples.team5be.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 유저 관련
    USER_NOT_FOUND(404, "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(400, "U002", "이미 존재하는 아이디입니다."),

    // 게시글 관련
    POST_NOT_FOUND(404, "P001", "존재하지 않는 게시글입니다."),
    HANDLE_ACCESS_DENIED(403, "G001", "권한이 없습니다."),

    // 인증 관련
    INVALID_TOKEN(401, "A001", "잘못된 토큰입니다."),
    EXPIRED_TOKEN(401, "A002", "만료된 토큰입니다."),
    LOGOUT_TOKEN(401, "A003", "로그아웃된 토큰입니다.");

    private final int status;
    private final String code;
    private final String message;
}
