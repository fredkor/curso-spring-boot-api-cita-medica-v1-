package mx.com.fredkor.apps.citamedica.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EnableJpaRepositories(
		basePackages = {
				"mx.com.fredkor.apps.citamedica.catalogos.persistence.repository",
				"mx.com.fredkor.apps.citamedica.core.persistence.repository",
				"mx.com.fredkor.apps.citamedica.usuario.persistence"
		}
)
@EntityScan(
		basePackages = {
				"mx.com.fredkor.apps.citamedica.catalogos.persistence.entity",
				"mx.com.fredkor.apps.citamedica.core.persistence.entity",
				"mx.com.fredkor.security.persistence.entity"
		}
)
@ComponentScan(basePackages = {
		"mx.com.fredkor.apps.citamedica.core",
		"mx.com.fredkor.apps.citamedica.catalogos",
		"mx.com.fredkor.apps.citamedica.usuario",
		"mx.com.fredkor.apps.citamedica.platform",
		"mx.com.fredkor.apps.citamedica.shared",
})
public class ApiCitaMedicaV1Application {

	public static void main(String[] args) {
		SpringApplication.run(ApiCitaMedicaV1Application.class, args);
	}

}
