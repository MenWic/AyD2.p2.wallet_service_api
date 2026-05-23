package ayd2.p2b.wallet_service_api.feature.payment.application.register;

import ayd2.p2b.wallet_service_api.feature.payment.dto.response.PaymentResponse;
import lombok.Value;

@Value
public class RegisterPaymentResult {

    PaymentResponse payload;
    boolean replay;

    public static RegisterPaymentResult newPayment(PaymentResponse payload) {
        return new RegisterPaymentResult(payload, false);
    }

    public static RegisterPaymentResult replay(PaymentResponse payload) {
        return new RegisterPaymentResult(payload, true);
    }
}
