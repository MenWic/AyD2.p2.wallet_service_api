package ayd2.p2b.wallet_service_api.feature.wallet.mapper;

import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.TransactionData;
import ayd2.p2b.wallet_service_api.feature.wallet.domain.model.WalletAccount;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.response.TransactionResponse;
import ayd2.p2b.wallet_service_api.feature.wallet.dto.response.WalletBalanceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WalletMapper {

    WalletBalanceResponse toBalanceResponse(WalletAccount wallet);

    TransactionResponse toTransactionResponse(TransactionData transaction);
}
