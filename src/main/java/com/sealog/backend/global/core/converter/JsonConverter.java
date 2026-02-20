package com.sealog.backend.global.core.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.global.core.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JSON <-> String 간 변환 지원 클래스
 */
@Component
@RequiredArgsConstructor
public class JsonConverter {

    // 사용 의존성
    private final ObjectMapper objectMapper;

    // 사용 상수
    private static final String MESSAGE_SERIALIZE = "JSON 직렬화 실패.";
    private static final String MESSAGE_DESERIALIZE = "JSON 직렬화 실패.";


    /**
     * Object -> JSON 직렬화
     * @param data 직렬화 대상 객체
     * @return JSON String
     */
    public String serialize(Object data) {
        return run(
                () -> objectMapper.writeValueAsString(data),
                MESSAGE_SERIALIZE
        );
    }


    /**
     * JSON -> Object 역직렬화
     * @param json 역직렬화 대상 JSON String
     * @param jsonClass 역직렬화 Class 타입 정보
     * @param <T> 역직렬화 클래스 타입
     * @return 클래스 정보 기반 변환된 객체
     */
    public <T> T deserialize(String json, Class<T> jsonClass) {
        return run(
                () -> objectMapper.readValue(json, jsonClass),
                MESSAGE_DESERIALIZE
        );
    }


    /**
     * JSON -> Object 역직렬화
     * @param json 역직렬화 대상 JSON String
     * @param jsonClass 역직렬화 Class 타입 정보
     * @param <T> 역직렬화 클래스 타입
     * @return 클래스 정보 기반 변환된 객체
     */
    public <T> T deserialize(String json, TypeReference<T> jsonClass) {
        return run(
                () -> objectMapper.readValue(json, jsonClass),
                MESSAGE_DESERIALIZE
        );
    }


    /**
     * Object -> JSON 변환 (List 대상)
     * @param json 역직렬화 대상 JSON String
     * @param jsonListElementClass 역직렬화 리스트 원소 타입 정보
     * @param <T> 역직렬화 리스트 원소 타입 정보
     * @return 클래스 정보 기반 변환된 객체 (List)
     */
    public <T> List<T> deserializeToList(String json, Class<T> jsonListElementClass) {
        return run(
                () -> objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, jsonListElementClass)),
                MESSAGE_DESERIALIZE
        );
    }


    /**
     * 메소드 수행 (체크 예외를 잡고, 언체크 커스텀 예외로 다시 던짐)
     */
    private <T> T run(CheckedSupplier<T> task, String errorMessage) {

        try {
            return task.get();

        } catch (CustomException e) {
            throw e;

        } catch (Exception e) {
            throw new CustomException(
                    "%s 발생 오류 : %s".formatted(errorMessage, e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    /**
     * 체크 예외 처리를 위한 Custom Supplier
     */
    @FunctionalInterface
    private interface CheckedSupplier<T> {
        T get() throws Exception;
    }

}
