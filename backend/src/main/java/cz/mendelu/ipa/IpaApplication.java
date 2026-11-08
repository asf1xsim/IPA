package cz.mendelu.ipa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class IpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(IpaApplication.class, args);
	}

}