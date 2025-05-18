package com.jgmedellin.concept_coffee_shop

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class CoolLightsApplicationTests {

	@Test
	fun contextLoads() {
	}

}
