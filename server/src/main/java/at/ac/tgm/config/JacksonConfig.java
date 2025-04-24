package at.ac.tgm.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.naming.Name;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper registerObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("MyObjectSerializer");
        module.addSerializer(Name.class, new NameJsonSerializer());
        module.addSerializer(LocalDateTime.class, new LocalDateTimeJsonSerializer());
        module.addSerializer(LocalDate.class, new LocalDateJsonSerializer());
        mapper.registerModule(module);
        return mapper;
    }
    
    static class NameJsonSerializer extends StdSerializer<Name> {
        public NameJsonSerializer() {
            this(null);
        }
        
        public NameJsonSerializer(Class<Name> t) {
            super(t);
        }
        
        @Override
        public void serialize(Name value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(value.toString());
        }
    }
    
    static class LocalDateTimeJsonSerializer extends StdSerializer<LocalDateTime> {
        public LocalDateTimeJsonSerializer() {
            this(null);
        }
        
        public LocalDateTimeJsonSerializer(Class<LocalDateTime> t) {
            super(t);
        }
        
        @Override
        public void serialize(LocalDateTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
    }
    
    static class LocalDateJsonSerializer extends StdSerializer<LocalDate> {
        public LocalDateJsonSerializer() {
            this(null);
        }
        
        public LocalDateJsonSerializer(Class<LocalDate> t) {
            super(t);
        }
        
        @Override
        public void serialize(LocalDate value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
    }
}
