package org.ttarena.arena_user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

/**
 * Configuración de MongoDB reactivo para el microservicio de personajes.
 */
@Configuration
@EnableReactiveMongoRepositories(basePackages = "org.ttarena.arena_user.repository")
public class MongoConfig {

    /**
     * Configura el ReactiveMongoTemplate para operaciones reactivas con MongoDB.
     * 
     * @param factory Factory para la base de datos MongoDB reactiva
     * @param converter Conversor para mapear objetos a documentos MongoDB
     * @return ReactiveMongoTemplate configurado
     */
    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate(
            ReactiveMongoDatabaseFactory factory,
            MappingMongoConverter converter) {
        return new ReactiveMongoTemplate(factory, converter);
    }
}
