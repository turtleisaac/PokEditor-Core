package io.github.turtleisaac.pokeditor.formats;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import io.github.turtleisaac.nds4j.Narc;
import io.github.turtleisaac.nds4j.binaries.CodeBinary;
import io.github.turtleisaac.pokeditor.formats.encounters.JohtoEncounterData;
import io.github.turtleisaac.pokeditor.formats.encounters.SinnohEncounterData;
import io.github.turtleisaac.pokeditor.formats.evolutions.EvolutionData;
import io.github.turtleisaac.pokeditor.formats.items.ItemData;
import io.github.turtleisaac.pokeditor.formats.learnsets.LearnsetData;
import io.github.turtleisaac.pokeditor.formats.moves.MoveData;
import io.github.turtleisaac.pokeditor.formats.personal.PersonalData;
import io.github.turtleisaac.pokeditor.formats.pokemon_sprites.PokemonSpriteData;
import io.github.turtleisaac.pokeditor.formats.scripts.GenericScriptData;
import io.github.turtleisaac.pokeditor.formats.scripts.FieldScriptParser;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.formats.trainers.TrainerData;
import io.github.turtleisaac.pokeditor.gamedata.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.turtleisaac.pokeditor.formats.TestsInjector.injector;
import static org.assertj.core.api.Assertions.assertThat;

public class ParserTests
{
    @Nested
    class PersonalTests extends GenericParserTest<PersonalData>
    {
        @Override
        protected GenericParser<PersonalData> createParser()
        {
            return injector.getInstance(Key.get(new TypeLiteral<>() {}));
        }
    }

    @Nested
    class LearnsetsTests extends GenericParserTest<LearnsetData>
    {
        @Override
        protected GenericParser<LearnsetData> createParser()
        {
            return injector.getInstance(Key.get(new TypeLiteral<>() {}));
        }
    }

    @Nested
    class EvolutionsTests extends GenericParserTest<EvolutionData>
    {
        @Override
        protected GenericParser<EvolutionData> createParser()
        {
            return injector.getInstance(Key.get(new TypeLiteral<>() {}));
        }
    }

    @Nested
    class TrainersTests extends GenericParserTest<TrainerData>
    {
        @Override
        protected GenericParser<TrainerData> createParser()
        {
            return injector.getInstance(Key.get(new TypeLiteral<>() {}));
        }
    }

    @Nested
    class MovesTests extends GenericParserTest<MoveData>
    {
        @Override
        protected GenericParser<MoveData> createParser()
        {
            return injector.getInstance(Key.get(new TypeLiteral<>() {}));
        }
    }

    @Nested
    class SinnohEncountersTests extends GenericParserTest<SinnohEncounterData>
    {
        @Override
        protected GenericParser<SinnohEncounterData> createParser()
        {
            return injector.getInstance(Key.get(new TypeLiteral<>() {}));
        }
    }

    @Nested
    class JohtoEncountersTests extends GenericParserTest<JohtoEncounterData>
    {
        @Override
        protected GenericParser<JohtoEncounterData> createParser()
        {
            return injector.getInstance(Key.get(new TypeLiteral<>() {}));
        }

        @Override
        protected String romFileName()
        {
            return System.getProperty(HEARTGOLD_ROM_PROPERTY, DEFAULT_HEARTGOLD_ROM);
        }
    }

    @Nested
    class ItemsTests extends GenericParserTest<ItemData>
    {
        @Override
        protected GenericParser<ItemData> createParser()
        {
            return injector.getInstance(Key.get(new TypeLiteral<>() {}));
        }
    }

    @Nested
    class TextBankTests extends GenericParserTest<TextBankData>
    {
        @Override
        protected GenericParser<TextBankData> createParser()
        {
            return injector.getInstance(Key.get(new TypeLiteral<>() {}));
        }
    }

    @Nested
    class PokemonSpriteTests extends GenericParserTest<PokemonSpriteData>
    {
        @Override
        protected GenericParser<PokemonSpriteData> createParser()
        {
            return injector.getInstance(Key.get(new TypeLiteral<>() {}));
        }
    }

    @Nested
    class FieldScriptsTests extends GenericParserTest<GenericScriptData>
    {
        @Override
        protected GenericParser<GenericScriptData> createParser()
        {
            return new FieldScriptParser();
        }

        @Override
        protected String romFileName()
        {
            return System.getProperty(HEARTGOLD_ROM_PROPERTY, DEFAULT_HEARTGOLD_ROM);
        }

        @Test
        @Override
        void outputMatchesInput()
        {
            loadRom();

            HashMap<GameFiles, Narc> map = new HashMap<>();
            for (GameFiles gameFile : parser.getRequirements()) {
                map.put(gameFile, new Narc(rom.getFileByName(gameFile.getPath())));
            }

            HashMap<GameCodeBinaries, CodeBinary> codeBinaries = new HashMap<>();
            codeBinaries.put(GameCodeBinaries.ARM9, rom.loadArm9());

            List<GenericScriptData> data = parser.generateDataList(map, codeBinaries);
            Map<GameFiles, Narc> output = parser.processDataList(data, codeBinaries);

            for (GameFiles gameFile : parser.getRequirements()) {
                Narc originalNarc = map.get(gameFile);
                Narc outputNarc = output.get(gameFile);

                assertThat(outputNarc)
                        .as("no output narc was produced for %s", gameFile)
                        .isNotNull();

                assertThat(outputNarc.getFiles().size())
                        .as("the number of script files changed")
                        .isEqualTo(originalNarc.getFiles().size());

                for (int idx = 0; idx < originalNarc.getFiles().size(); idx++) {
                    byte[] outputFile = outputNarc.getFile(idx);

                    assertThat(outputFile)
                            .as("script file %d serialized to nothing", idx)
                            .isNotEmpty();

                    assertThat(outputFile)
                            .as("script file %d", idx)
                            .isEqualTo(originalNarc.getFile(idx));
                }
            }
        }
    }
}
