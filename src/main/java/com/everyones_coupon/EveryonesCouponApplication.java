package com.everyones_coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class EveryonesCouponApplication {

	public static void main(String[] args) {
		SpringApplication.run(EveryonesCouponApplication.class, args);
	}

}
