package ayd2.p2b.wallet_service_api;

import org.springframework.boot.SpringApplication;

public class TestWalletServiceApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(WalletServiceApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
