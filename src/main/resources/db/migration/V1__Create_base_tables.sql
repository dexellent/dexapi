CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Generations table
CREATE TABLE generations (
                             id BIGSERIAL PRIMARY KEY,
                             number INTEGER NOT NULL UNIQUE,
                             name VARCHAR(100) NOT NULL,
                             region VARCHAR(100),
                             release_year INTEGER,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Generation games table
CREATE TABLE generation_games (
                                  generation_id BIGINT NOT NULL,
                                  game_name VARCHAR(100) NOT NULL,
                                  CONSTRAINT fk_generation_games_generation FOREIGN KEY (generation_id) REFERENCES generations(id) ON DELETE CASCADE
);

-- Types table
CREATE TABLE types (
                       id BIGSERIAL PRIMARY KEY,
                       identifier VARCHAR(50) NOT NULL UNIQUE,
                       color VARCHAR(7),
                       generation_id BIGINT,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       CONSTRAINT fk_types_generation FOREIGN KEY (generation_id) REFERENCES generations(id)
);

-- Abilities table
CREATE TABLE abilities (
                           id BIGSERIAL PRIMARY KEY,
                           identifier VARCHAR(100) NOT NULL UNIQUE,
                           generation_id BIGINT,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT fk_abilities_generation FOREIGN KEY (generation_id) REFERENCES generations(id)
);

-- Moves table
CREATE TABLE moves (
                       id BIGSERIAL PRIMARY KEY,
                       identifier VARCHAR(100) NOT NULL UNIQUE,
                       type_id BIGINT NOT NULL,
                       category VARCHAR(50) NOT NULL CHECK (category IN ('PHYSICAL', 'SPECIAL', 'STATUS')),
                       power INTEGER,
                       accuracy INTEGER,
                       power_points INTEGER NOT NULL,
                       priority INTEGER,
                       target VARCHAR(50),
                       critical_hit_rate INTEGER,
                       flinch_chance INTEGER,
                       stat_change INTEGER,
                       ailment VARCHAR(50),
                       ailment_chance INTEGER,
                       healing INTEGER,
                       drain INTEGER,
                       generation_id BIGINT,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       CONSTRAINT fk_moves_type FOREIGN KEY (type_id) REFERENCES types(id),
                       CONSTRAINT fk_moves_generation FOREIGN KEY (generation_id) REFERENCES generations(id)
);

-- Pokemon table
CREATE TABLE pokemon (
                         id BIGSERIAL PRIMARY KEY,
                         national_dex_number INTEGER NOT NULL UNIQUE,
                         identifier VARCHAR(100) NOT NULL UNIQUE,
                         hp INTEGER NOT NULL,
                         attack INTEGER NOT NULL,
                         defense INTEGER NOT NULL,
                         special_attack INTEGER NOT NULL,
                         special_defense INTEGER NOT NULL,
                         speed INTEGER NOT NULL,
                         height DECIMAL(5,2),
                         weight DECIMAL(6,3),
                         capture_rate INTEGER,
                         base_experience INTEGER,
                         growth_rate VARCHAR(50),
                         gender_ratio VARCHAR(50),
                         egg_cycles INTEGER,
                         color VARCHAR(50),
                         shape VARCHAR(50),
                         generation_id BIGINT,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT fk_pokemon_generation FOREIGN KEY (generation_id) REFERENCES generations(id)
);

-- Pokedexes table
CREATE TABLE pokedexes (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(100) NOT NULL UNIQUE,
                           region VARCHAR(100),
                           description TEXT,
                           generation_id BIGINT,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT fk_pokedexes_generation FOREIGN KEY (generation_id) REFERENCES generations(id)
);

-- 10. Flyway Migration V2__Create_translation_tables.sql
-- Pokemon translations
CREATE TABLE pokemon_translations (
                                      id BIGSERIAL PRIMARY KEY,
                                      pokemon_id BIGINT NOT NULL,
                                      language VARCHAR(5) NOT NULL CHECK (language IN ('EN', 'FR', 'JA', 'ES', 'DE', 'IT', 'KO', 'ZH')),
    name VARCHAR(100) NOT NULL,
    species VARCHAR(100),
    description TEXT,
    habitat VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pokemon_translation_pokemon FOREIGN KEY (pokemon_id) REFERENCES pokemon(id) ON DELETE CASCADE,
    CONSTRAINT uk_pokemon_translation_lang UNIQUE (pokemon_id, language)
);

-- Move translations
CREATE TABLE move_translations (
                                   id BIGSERIAL PRIMARY KEY,
                                   move_id BIGINT NOT NULL,
                                   language VARCHAR(5) NOT NULL CHECK (language IN ('EN', 'FR', 'JA', 'ES', 'DE', 'IT', 'KO', 'ZH')),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    effect TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_move_translation_move FOREIGN KEY (move_id) REFERENCES moves(id) ON DELETE CASCADE,
    CONSTRAINT uk_move_translation_lang UNIQUE (move_id, language)
);

-- Type translations
CREATE TABLE type_translations (
                                   id BIGSERIAL PRIMARY KEY,
                                   type_id BIGINT NOT NULL,
                                   language VARCHAR(5) NOT NULL CHECK (language IN ('EN', 'FR', 'JA', 'ES', 'DE', 'IT', 'KO', 'ZH')),
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_type_translation_type FOREIGN KEY (type_id) REFERENCES types(id) ON DELETE CASCADE,
    CONSTRAINT uk_type_translation_lang UNIQUE (type_id, language)
);

-- Ability translations
CREATE TABLE ability_translations (
                                      id BIGSERIAL PRIMARY KEY,
                                      ability_id BIGINT NOT NULL,
                                      language VARCHAR(5) NOT NULL CHECK (language IN ('EN', 'FR', 'JA', 'ES', 'DE', 'IT', 'KO', 'ZH')),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    effect TEXT,
    short_effect TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ability_translation_ability FOREIGN KEY (ability_id) REFERENCES abilities(id) ON DELETE CASCADE,
    CONSTRAINT uk_ability_translation_lang UNIQUE (ability_id, language)
);

-- 11. Flyway Migration V3__Create_junction_tables.sql
-- Pokemon types (many-to-many)
CREATE TABLE pokemon_types (
                               id BIGSERIAL PRIMARY KEY,
                               pokemon_id BIGINT NOT NULL,
                               type_id BIGINT NOT NULL,
                               slot INTEGER NOT NULL CHECK (slot IN (1, 2)),
                               CONSTRAINT fk_pokemon_types_pokemon FOREIGN KEY (pokemon_id) REFERENCES pokemon(id) ON DELETE CASCADE,
                               CONSTRAINT fk_pokemon_types_type FOREIGN KEY (type_id) REFERENCES types(id) ON DELETE CASCADE,
                               CONSTRAINT uk_pokemon_type_slot UNIQUE (pokemon_id, slot)
);

-- Pokemon abilities (many-to-many)
CREATE TABLE pokemon_abilities (
                                   id BIGSERIAL PRIMARY KEY,
                                   pokemon_id BIGINT NOT NULL,
                                   ability_id BIGINT NOT NULL,
                                   is_hidden BOOLEAN NOT NULL DEFAULT FALSE,
                                   slot INTEGER,
                                   CONSTRAINT fk_pokemon_abilities_pokemon FOREIGN KEY (pokemon_id) REFERENCES pokemon(id) ON DELETE CASCADE,
                                   CONSTRAINT fk_pokemon_abilities_ability FOREIGN KEY (ability_id) REFERENCES abilities(id) ON DELETE CASCADE
);

-- Pokemon moves (many-to-many with learn method)
CREATE TABLE pokemon_moves (
                               id BIGSERIAL PRIMARY KEY,
                               pokemon_id BIGINT NOT NULL,
                               move_id BIGINT NOT NULL,
                               learn_method VARCHAR(50) NOT NULL CHECK (learn_method IN ('LEVEL_UP', 'TM', 'HM', 'EGG', 'TUTOR', 'MACHINE', 'STADIUM_SURFING_PIKACHU', 'LIGHT_BALL_EGG', 'COLOSSEUM_PURIFICATION', 'XD_SHADOW', 'XD_PURIFICATION', 'FORM_CHANGE')),
                               level_learned INTEGER,
                               generation_id BIGINT,
                               CONSTRAINT fk_pokemon_moves_pokemon FOREIGN KEY (pokemon_id) REFERENCES pokemon(id) ON DELETE CASCADE,
                               CONSTRAINT fk_pokemon_moves_move FOREIGN KEY (move_id) REFERENCES moves(id) ON DELETE CASCADE,
                               CONSTRAINT fk_pokemon_moves_generation FOREIGN KEY (generation_id) REFERENCES generations(id)
);

-- Type effectiveness
CREATE TABLE type_effectiveness (
                                    id BIGSERIAL PRIMARY KEY,
                                    attacking_type_id BIGINT NOT NULL,
                                    defending_type_id BIGINT NOT NULL,
                                    effectiveness DECIMAL(3,2) NOT NULL CHECK (effectiveness IN (0.0, 0.25, 0.5, 1.0, 2.0, 4.0)),
                                    CONSTRAINT fk_type_effectiveness_attacking FOREIGN KEY (attacking_type_id) REFERENCES types(id),
                                    CONSTRAINT fk_type_effectiveness_defending FOREIGN KEY (defending_type_id) REFERENCES types(id),
                                    CONSTRAINT uk_type_effectiveness UNIQUE (attacking_type_id, defending_type_id)
);

-- Pokedex entries
CREATE TABLE pokedex_entries (
                                 id BIGSERIAL PRIMARY KEY,
                                 pokemon_id BIGINT NOT NULL,
                                 pokedex_id BIGINT NOT NULL,
                                 entry_number INTEGER NOT NULL,
                                 CONSTRAINT fk_pokedex_entries_pokemon FOREIGN KEY (pokemon_id) REFERENCES pokemon(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_pokedex_entries_pokedex FOREIGN KEY (pokedex_id) REFERENCES pokedexes(id) ON DELETE CASCADE,
                                 CONSTRAINT uk_pokedex_entry UNIQUE (pokedex_id, entry_number)
);

-- Evolutions
CREATE TABLE evolutions (
                            id BIGSERIAL PRIMARY KEY,
                            from_pokemon_id BIGINT NOT NULL,
                            to_pokemon_id BIGINT NOT NULL,
                            trigger VARCHAR(50) NOT NULL CHECK (trigger IN ('LEVEL_UP', 'TRADE', 'USE_ITEM', 'SHED', 'SPIN', 'TOWER_OF_DARKNESS', 'TOWER_OF_WATERS', 'THREE_CRITICAL_HITS', 'TAKE_DAMAGE', 'OTHER')),
                            minimum_level INTEGER,
                            item VARCHAR(100),
                            condition VARCHAR(100),
                            minimum_happiness INTEGER,
                            time_of_day VARCHAR(50),
                            location VARCHAR(100),
                            "order" INTEGER,
                            CONSTRAINT fk_evolutions_from FOREIGN KEY (from_pokemon_id) REFERENCES pokemon(id),
                            CONSTRAINT fk_evolutions_to FOREIGN KEY (to_pokemon_id) REFERENCES pokemon(id)
);

-- 12. Flyway Migration V4__Create_indexes.sql
-- Pokemon indexes
CREATE INDEX idx_pokemon_national_dex_number ON pokemon(national_dex_number);
CREATE INDEX idx_pokemon_identifier ON pokemon(identifier);
CREATE INDEX idx_pokemon_generation_id ON pokemon(generation_id);

-- Translation indexes
CREATE INDEX idx_pokemon_translations_pokemon_lang ON pokemon_translations(pokemon_id, language);
CREATE INDEX idx_pokemon_translations_name ON pokemon_translations(name);
CREATE INDEX idx_pokemon_translations_name_lower ON pokemon_translations(LOWER(name));

CREATE INDEX idx_move_translations_move_lang ON move_translations(move_id, language);
CREATE INDEX idx_move_translations_name ON move_translations(name);

CREATE INDEX idx_type_translations_type_lang ON type_translations(type_id, language);
CREATE INDEX idx_type_translations_name ON type_translations(name);

CREATE INDEX idx_ability_translations_ability_lang ON ability_translations(ability_id, language);
CREATE INDEX idx_ability_translations_name ON ability_translations(name);

-- Junction table indexes
CREATE INDEX idx_pokemon_types_pokemon_id ON pokemon_types(pokemon_id);
CREATE INDEX idx_pokemon_types_type_id ON pokemon_types(type_id);

CREATE INDEX idx_pokemon_abilities_pokemon_id ON pokemon_abilities(pokemon_id);
CREATE INDEX idx_pokemon_abilities_ability_id ON pokemon_abilities(ability_id);

CREATE INDEX idx_pokemon_moves_pokemon_id ON pokemon_moves(pokemon_id);
CREATE INDEX idx_pokemon_moves_move_id ON pokemon_moves(move_id);
CREATE INDEX idx_pokemon_moves_learn_method ON pokemon_moves(learn_method);

-- Performance indexes
CREATE INDEX idx_type_effectiveness_attacking ON type_effectiveness(attacking_type_id);
CREATE INDEX idx_type_effectiveness_defending ON type_effectiveness(defending_type_id);

CREATE INDEX idx_pokedex_entries_pokemon_id ON pokedex_entries(pokemon_id);
CREATE INDEX idx_pokedex_entries_pokedex_id ON pokedex_entries(pokedex_id);
