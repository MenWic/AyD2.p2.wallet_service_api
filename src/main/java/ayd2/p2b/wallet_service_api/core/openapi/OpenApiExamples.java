package ayd2.p2b.wallet_service_api.core.openapi;

public final class OpenApiExamples {

    private OpenApiExamples() {
    }

    public static final String PROBLEM_VALIDATION_FAILED = """
            {"type":"about:blank","title":"Bad Request","status":400,"detail":"Validation failed","code":"validation.failed"}
            """;
    public static final String PROBLEM_UNAUTHORIZED = """
            {"status":401,"title":"Unauthorized","detail":"Authentication is required","code":"auth.token_invalid"}
            """;
    public static final String PROBLEM_FORBIDDEN = """
            {"type":"about:blank","title":"Forbidden","status":403,"detail":"Access denied","code":"auth.forbidden"}
            """;
    public static final String PROBLEM_NOT_FOUND = """
            {"type":"about:blank","title":"Not Found","status":404,"detail":"Payment not found","code":"resource.not_found"}
            """;
    public static final String PROBLEM_CONFLICT = """
            {"type":"about:blank","title":"Conflict","status":409,"detail":"Idempotency-Key already used with a different request","code":"resource.conflict"}
            """;
    public static final String PROBLEM_INSUFFICIENT_FUNDS = """
            {"type":"about:blank","title":"Unprocessable Entity","status":422,"detail":"Insufficient wallet balance","code":"wallet.insufficient_funds"}
            """;
    public static final String PROBLEM_INTERNAL_ERROR = """
            {"type":"about:blank","title":"Internal Server Error","status":500,"detail":"Unexpected error","code":"system.internal_error"}
            """;

    public static final String REQUEST_CREATE_WALLET = """
            {"userId":"00000000-0000-0000-0000-000000000001"}
            """;
    public static final String REQUEST_TOP_UP = """
            {"amount":150.00,"paymentDate":"2026-05-20"}
            """;
    public static final String REQUEST_REGISTER_PAYMENT = """
            {"userId":"00000000-0000-0000-0000-000000000001","congressId":"00000000-0000-0000-0000-000000000010","institutionId":"00000000-0000-0000-0000-000000000020","congressNameSnapshot":"Congreso Nacional de Ingenieria","institutionNameSnapshot":"Universidad Nacional","amount":350.00,"paymentDate":"2026-05-20"}
            """;
    public static final String REQUEST_UPDATE_SYSTEM_CONFIG = """
            {"commissionPercent":10.00}
            """;

    public static final String RESPONSE_WALLET_BALANCE = """
            {"data":{"userId":"00000000-0000-0000-0000-000000000001","balance":850.00}}
            """;
    public static final String RESPONSE_TRANSACTION_PAGE = """
            {"data":{"items":[{"id":"11111111-1111-1111-1111-111111111111","walletUserId":"00000000-0000-0000-0000-000000000001","type":"TOP_UP","amount":150.00,"transactionDate":"2026-05-20","referencePaymentId":null,"createdAt":"2026-05-20T13:10:00Z"}],"page":0,"size":20,"totalItems":1,"totalPages":1}}
            """;
    public static final String RESPONSE_PAYMENT_REGISTERED = """
            {"data":{"id":"22222222-2222-2222-2222-222222222222","userId":"00000000-0000-0000-0000-000000000001","congressId":"00000000-0000-0000-0000-000000000010","institutionId":"00000000-0000-0000-0000-000000000020","congressNameSnapshot":"Congreso Nacional de Ingenieria","institutionNameSnapshot":"Universidad Nacional","commissionPercentSnapshot":10.00,"amount":350.00,"commissionAmount":35.00,"netAmount":315.00,"paymentDate":"2026-05-20","idempotencyKey":"idem-12345","createdAt":"2026-05-20T13:12:00Z"}}
            """;
    public static final String RESPONSE_PAYMENT_REPLAY = """
            {"data":{"id":"22222222-2222-2222-2222-222222222222","userId":"00000000-0000-0000-0000-000000000001","congressId":"00000000-0000-0000-0000-000000000010","institutionId":"00000000-0000-0000-0000-000000000020","congressNameSnapshot":"Congreso Nacional de Ingenieria","institutionNameSnapshot":"Universidad Nacional","commissionPercentSnapshot":10.00,"amount":350.00,"commissionAmount":35.00,"netAmount":315.00,"paymentDate":"2026-05-20","idempotencyKey":"idem-12345","createdAt":"2026-05-20T13:12:00Z"},"message":"idempotency.replay"}
            """;
    public static final String RESPONSE_PAYMENT_DETAIL = """
            {"data":{"id":"22222222-2222-2222-2222-222222222222","userId":"00000000-0000-0000-0000-000000000001","congressId":"00000000-0000-0000-0000-000000000010","institutionId":"00000000-0000-0000-0000-000000000020","congressNameSnapshot":"Congreso Nacional de Ingenieria","institutionNameSnapshot":"Universidad Nacional","commissionPercentSnapshot":10.00,"amount":350.00,"commissionAmount":35.00,"netAmount":315.00,"paymentDate":"2026-05-20","idempotencyKey":"idem-12345","createdAt":"2026-05-20T13:12:00Z"}}
            """;
    public static final String RESPONSE_PAYMENTS_PAGE = """
            {"data":{"items":[{"id":"22222222-2222-2222-2222-222222222222","userId":"00000000-0000-0000-0000-000000000001","congressId":"00000000-0000-0000-0000-000000000010","institutionId":"00000000-0000-0000-0000-000000000020","congressNameSnapshot":"Congreso Nacional de Ingenieria","institutionNameSnapshot":"Universidad Nacional","commissionPercentSnapshot":10.00,"amount":350.00,"commissionAmount":35.00,"netAmount":315.00,"paymentDate":"2026-05-20","idempotencyKey":"idem-12345","createdAt":"2026-05-20T13:12:00Z"}],"page":0,"size":20,"totalItems":1,"totalPages":1}}
            """;
    public static final String RESPONSE_SYSTEM_CONFIG = """
            {"data":{"commissionPercent":10.00,"updatedBy":"00000000-0000-0000-0000-000000000999","updatedAt":"2026-05-20T13:00:00Z"}}
            """;
    public static final String RESPONSE_EARNINGS_BY_CONGRESS = """
            {"data":{"items":[{"congressId":"00000000-0000-0000-0000-000000000010","congressName":"Congreso Nacional de Ingenieria","institutionId":"00000000-0000-0000-0000-000000000020","institutionName":"Universidad Nacional","totalAmount":1000.00,"commissionAmount":100.00,"netAmount":900.00,"paymentCount":4}],"totalItems":1,"grandTotalAmount":1000.00,"grandTotalCommission":100.00,"grandTotalNet":900.00}}
            """;
    public static final String RESPONSE_PLATFORM_EARNINGS = """
            {"data":{"items":[{"institutionId":"00000000-0000-0000-0000-000000000020","institutionName":"Universidad Nacional","congresses":[{"congressId":"00000000-0000-0000-0000-000000000010","congressName":"Congreso Nacional de Ingenieria","totalAmount":1000.00,"commissionAmount":100.00,"netAmount":900.00,"paymentCount":4}],"institutionTotalAmount":1000.00,"institutionTotalCommission":100.00,"institutionTotalNet":900.00,"paymentCount":4}],"totalItems":1,"grandTotalAmount":1000.00,"grandTotalCommission":100.00,"grandTotalNet":900.00}}
            """;
}

