package ayd2.p2b.wallet_service_api.unit.common;

import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.common.exception.GlobalExceptionHandler;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void should_return_403_for_access_denied() {
        ProblemDetail detail = handler.handleAccessDenied(new AccessDeniedException("denied"));

        assertThat(detail.getStatus()).isEqualTo(403);
        assertThat(detail.getProperties()).containsEntry("code", "auth.forbidden");
    }

    @Test
    void should_return_status_from_api_exception() {
        ApiException ex = new ApiException(HttpStatus.NOT_FOUND, "resource.not_found", "Not found");
        ProblemDetail detail = handler.handleApiException(ex);

        assertThat(detail.getStatus()).isEqualTo(404);
        assertThat(detail.getProperties()).containsEntry("code", "resource.not_found");
    }

    @Test
    void should_return_400_with_field_errors_for_validation_exception() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "commissionPercent", "must be in range");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ProblemDetail detail = handler.handleValidation(ex);

        assertThat(detail.getStatus()).isEqualTo(400);
        assertThat(detail.getProperties()).containsEntry("code", "validation.failed");
    }

    @Test
    void should_return_400_for_constraint_violation() {
        ConstraintViolationException ex = new ConstraintViolationException("violation", Set.of());
        ProblemDetail detail = handler.handleConstraintViolation(ex);

        assertThat(detail.getStatus()).isEqualTo(400);
        assertThat(detail.getProperties()).containsEntry("code", "validation.failed");
    }

    @Test
    void should_return_500_for_unexpected_exception() {
        ProblemDetail detail = handler.handleUnexpected(new RuntimeException("boom"));

        assertThat(detail.getStatus()).isEqualTo(500);
        assertThat(detail.getProperties()).containsEntry("code", "system.internal_error");
    }
}
