package com.taron.authenticate.config.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taron.authenticate.common.enums.type.ErrorCodeType;
import com.taron.authenticate.common.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@ControllerAdvice(
        {"com.taron.authenticate"}
)
public class ExceptionAdvice {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDeniedException(HttpServletResponse httpServletResponse) {
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try {
            objectMapper.writeValue(httpServletResponse.getWriter(), CommonUtil.successResponse(ErrorCodeType.ACCESS_DENIED));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
