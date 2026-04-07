package me.dohyeon.deepdive.global.common.response;

public record CommonResponse<T>(boolean success, String message, T data) {

    public static <T> CommonResponse<T> ok(T data) {
        return new CommonResponse<>(true, "success", data);
    }

    public static <T> CommonResponse<T> ok() {
        return new CommonResponse<>(true, "success", null);
    }
}
