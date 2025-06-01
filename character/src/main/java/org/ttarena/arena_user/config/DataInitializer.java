package org.ttarena.arena_user.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.ttarena.arena_user.model.Paladin;
import org.ttarena.arena_user.model.Priest;
import org.ttarena.arena_user.model.Warrior;
import org.ttarena.arena_user.model.enums.PaladinSpecialization;
import org.ttarena.arena_user.model.enums.PriestSpecialization;
import org.ttarena.arena_user.model.enums.WarriorSpecialization;
import org.ttarena.arena_user.service.CharacterService;
import reactor.core.publisher.Flux;

/**
 * Configuración para cargar datos de ejemplo en el entorno de desarrollo.
 */
@Configuration
@Profile("dev")
public class DataInitializer {

    @Bean
    public CommandLineRunner loadData(CharacterService characterService) {
        return args -> {
            characterService.getAllCharacters()
                .flatMap(character -> characterService.deleteCharacter(character.getId()))
                .blockLast();

            Flux.just(
                characterService.createWarrior("Conan", 200, 100, WarriorSpecialization.ARMS),
                characterService.createWarrior("Garrosh", 180, 120, WarriorSpecialization.FURY),
                characterService.createWarrior("Muradin", 250, 80, WarriorSpecialization.PROTECTION)
            ).blockLast();

            Flux.just(
                characterService.createPriest("Anduin", 150, 200, PriestSpecialization.HOLY),
                characterService.createPriest("Moira", 140, 180, PriestSpecialization.DISCIPLINE),
                characterService.createPriest("Velen", 130, 220, PriestSpecialization.SHADOW)
            ).blockLast();

            Flux.just(
                characterService.createPaladin("Uther", 220, 150, PaladinSpecialization.HOLY),
                characterService.createPaladin("Tirion", 200, 170, PaladinSpecialization.RETRIBUTION),
                characterService.createPaladin("Bolvar", 240, 130, PaladinSpecialization.PROTECTION)
            ).blockLast();

            System.out.println("Personajes cargados:");
            characterService.getAllCharacters()
                .doOnNext(System.out::println)
                .blockLast();
        };
    }
}
