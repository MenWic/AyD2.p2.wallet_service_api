package ayd2.p2b.wallet_service_api.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard paginated payload wrapped inside ApiResponse")
public class PageResponse<T> {
    @Schema(description = "Current page items")
    private List<T> items;

    @Schema(description = "Zero-based page index after normalization", example = "0")
    private int page;

    @Schema(description = "Page size after normalization (default 20, max 100)", example = "20")
    private int size;

    @Schema(description = "Total number of matching items", example = "57")
    private long totalItems;

    @Schema(description = "Total number of pages", example = "3")
    private int totalPages;
}
