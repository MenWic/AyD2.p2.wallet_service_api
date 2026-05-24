package ayd2.p2b.wallet_service_api.unit.common;

import ayd2.p2b.wallet_service_api.common.exception.ApiException;
import ayd2.p2b.wallet_service_api.common.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void should_wrap_data_without_message() {
        ApiResponse<String> response = ApiResponse.of("payload");

        assertThat(response.getData()).isEqualTo("payload");
        assertThat(response.getMessage()).isNull();
    }

    @Test
    void should_wrap_data_with_message() {
        ApiResponse<Integer> response = ApiResponse.of(42, "done");

        assertThat(response.getData()).isEqualTo(42);
        assertThat(response.getMessage()).isEqualTo("done");
    }

    @Test
    void should_build_api_exception_with_all_fields() {
        ApiException ex = new ApiException(HttpStatus.NOT_FOUND, "resource.not_found", "Not found");

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getCode()).isEqualTo("resource.not_found");
        assertThat(ex.getMessage()).isEqualTo("Not found");
    }
}
