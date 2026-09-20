package com.br.tcc;

import org.springframework.boot.SpringApplication;

public class TestTccApplication {

	public static void main(String[] args) {
		SpringApplication.from(TccApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
