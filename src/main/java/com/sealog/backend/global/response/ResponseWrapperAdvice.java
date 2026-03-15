package com.sealog.backend.global.response;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * [전역 응답 래퍼 어드바이스]
 * 모든 REST 컨트롤러의 반환값(Body)을 가로채서 공통 응답 규격(CustomResponse)으로 자동 포장합니다.
 */
@RestControllerAdvice(basePackages = "com.sealog.backend")
public class ResponseWrapperAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * 컨트롤러의 반환값이 클라이언트에게 전달되기 직전에 가로채어 최종 응답을 구성합니다.
     * 💡 HTTP 헤더에서 상태 코드를 자동 추출하여 JSON 본문(status 필드)에 주입합니다.
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        // 1. HTTP 헤더에서 상태 코드를 자동 추출 (200, 201, 203 등)
        int status = ((ServletServerHttpResponse) response).getServletResponse().getStatus();

        // 2. 이미 CustomResponse인 경우 (에러 응답 또는 컨트롤러에서 직접 생성한 경우)
        if (body instanceof CustomResponse) {
            CustomResponse<?> res = (CustomResponse<?>) body;
            
            // 💡 상태 코드가 비어있다면(@ResponseStatus 등을 통해 결정된) 헤더 값으로 보충합니다.
            if (res.getStatus() == null) {
                return CustomResponse.success(res.getData(), res.getMessage(), status);
            }
            return body;
        }

        // 3. String 반환 시 ClassCastException 방지를 위해 래핑 제외 (DTO 반환 권장)
        if (body instanceof String) {
            return body;
        }

        // 4. [핵심] 순수 DTO 또는 null인 경우 CustomResponse.success()로 자동 포장
        // 헤더에서 추출한 상태 코드를 함께 주입합니다.
        return CustomResponse.success(body, null, status);
    }
}
