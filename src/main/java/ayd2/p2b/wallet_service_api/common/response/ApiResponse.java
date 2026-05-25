package ayd2.p2b.wallet_service_api.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard successful response envelope used by Wallet endpoints")
public class ApiResponse<T> {
    @Schema(description = "Endpoint payload")
    private T data;

    @Schema(description = "Optional semantic message (for example idempotency.replay)", example = "idempotency.replay")
    private String message;

    public static <T> ApiResponse<T> of(T data) {
        return ApiResponse.<T>builder()
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> of(T data, String message) {
        return ApiResponse.<T>builder()
                .data(data)
                .message(message)
                .build();
    }
}
