package me.dohyeon.deepdive.domain.interview.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InterviewCategory {
    OS("운영체제"),
    SECURITY("보안"),
    DATABASE("데이터베이스"),
    DATA_STRUCTURE("자료구조"),
    NETWORK("네트워크"),
    SOFTWARE_DESIGN("소프트웨어 설계");

    private final String description;
}
