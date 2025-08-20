package dev.dexellent.dexapi;

import org.springframework.boot.SpringApplication;

public class TestDexapiApplication {

    public static void main(String[] args) {
        SpringApplication.from(DexapiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
