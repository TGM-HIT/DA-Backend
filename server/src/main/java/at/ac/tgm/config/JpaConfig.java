package at.ac.tgm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {"at.ac.tgm.repository"})
public class JpaConfig {
}
