package at.ac.tgm;

import me.paulschwarz.springdotenv.DotenvConfig;
import me.paulschwarz.springdotenv.DotenvPropertySource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class TestConfiguration {
    @Bean
    public DotenvPropertySource dotenvPropertySource() {
        Properties properties = new Properties();
        properties.setProperty("DOTENV_DIRECTORY", "../.."); // Set .env location
        DotenvConfig dotenvConfig = new DotenvConfig(properties);
        return new DotenvPropertySource(dotenvConfig);
    }
}