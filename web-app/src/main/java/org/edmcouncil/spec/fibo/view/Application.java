package org.edmcouncil.spec.fibo.view;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@ComponentScan(basePackages = { 
  "org.edmcouncil.spec.fibo.view", 
  "org.edmcouncil.spec.fibo.weasel",
  "org.edmcouncil.spec.fibo.config",
  "org.edmcouncil.spec.fibo.updater"
})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}

