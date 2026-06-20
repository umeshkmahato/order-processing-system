package com.orderprocessing.dto;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseDto {
    private boolean success;
    private String message;
    private Map<String, String> errors;
    private LocalDateTime timestamp;

    public static ErrorResponseDto of(String message) {
        return ErrorResponseDto.builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponseDto of(String message, Map<String, String> errors) {
        return ErrorResponseDto.builder()
                .success(false)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

