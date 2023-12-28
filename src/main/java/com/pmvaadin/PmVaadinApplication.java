package com.pmvaadin;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@PWA(name = "PM with Spring+Vaadin", shortName = "PM")
@Theme(value = "myapp")
public class PmVaadinApplication extends SpringBootServletInitializer implements AppShellConfigurator {

	public static void main(String[] args) {
		SpringApplication.run(PmVaadinApplication.class, args);
	}

//	@Override
//	protected SpringApplicationBuilder configure(
//			SpringApplicationBuilder application) {
//		return application.sources(PmVaadinApplication.class);
//	}

}
