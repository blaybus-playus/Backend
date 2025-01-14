package org.example.playus.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD REQUEST"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT FOUND"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"),

    // TOKEN
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다."),
    TOKEN_EXPIRATION(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다. 재로그인 해주세요."),
    NOT_SUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "지원되지 않는 JWT 토큰입니다."),
    FALSE_TOKEN(HttpStatus.BAD_REQUEST, "잘못된 JWT 토큰입니다."),
    HEADER_NOT_FOUND(HttpStatus.BAD_REQUEST, "헤더가 잘못되었거나 누락되었습니다."),
    UNMATCHED_TOKEN(HttpStatus.BAD_REQUEST, "일치하지 않는 토큰입니다."),

    // USER
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "유저를 찾을 수 없습니다."),
    USER_ALREADY_EXIST(HttpStatus.CONFLICT, "이미 같은 이름을 가진 유저가 존재합니다."),
    PASSWORD_NOT_CORRECT(HttpStatus.BAD_REQUEST, "패스워드가 일치하지 않습니다."),

    // EMPLOYEE
    EMPLOYEE_NOT_FOUND(HttpStatus.BAD_REQUEST, "직원을 찾을 수 없습니다."),
    EMPLOYEE_EXIST(HttpStatus.BAD_REQUEST,"등록된 사번입니다."),

    // LOGIN
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED,"로그인 실패하였습니다." ),

    //SHEET
    SHEET_NOT_FOUND(HttpStatus.NOT_FOUND,"Google Sheets에서 해당 ID의 게시글을 찾을 수 없습니다." ),
    INVALID_TYPE(HttpStatus.UNAUTHORIZED,"잘못된 검색 타입입니다." ),
    SEARCH_NOT_FOUND(HttpStatus.NOT_FOUND,"검색 결과가 없습니다." ),

    // LEVEL
    LEVEL_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 레벨 그룹입니다."),
    INVALID_GROUP_TYPE(HttpStatus.FORBIDDEN,"존재하지 않는 직군입니다." );
    private final HttpStatus status;
    private final String message;
}
