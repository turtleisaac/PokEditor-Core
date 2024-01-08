package io.github.turtleisaac.pokeditor.formats;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import io.github.turtleisaac.nds4j.Narc;
import io.github.turtleisaac.nds4j.NintendoDsRom;
import io.github.turtleisaac.nds4j.binaries.CodeBinary;
import io.github.turtleisaac.pokeditor.formats.encounters.JohtoEncounterData;
import io.github.turtleisaac.pokeditor.formats.encounters.SinnohEncounterData;
import io.github.turtleisaac.pokeditor.formats.evolutions.EvolutionData;
import io.github.turtleisaac.pokeditor.formats.items.ItemData;
import io.github.turtleisaac.pokeditor.formats.learnsets.LearnsetData;
import io.github.turtleisaac.pokeditor.formats.moves.MoveData;
import io.github.turtleisaac.pokeditor.formats.personal.PersonalData;
import io.github.turtleisaac.pokeditor.formats.scripts.GenericScriptData;
import io.github.turtleisaac.pokeditor.formats.scripts.LevelScriptData;
import io.github.turtleisaac.pokeditor.formats.scripts.ScriptData;
import io.github.turtleisaac.pokeditor.formats.scripts.ScriptParser;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.formats.trainers.TrainerData;
import io.github.turtleisaac.pokeditor.gamedata.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.turtleisaac.pokeditor.formats.TestsInjector.injector;
import static org.assertj.core.api.Assertions.assertThat;

public class ParserTests
{
    public static class PersonalTests extends GenericParserTest<PersonalData>
    {
        @Override
        protected GenericParser<PersonalData> createParser()
        {
            return injector.getInstance(Key.get(new TypeLiteral<>() {}));
        }
    }

    public static class LearnsetsTests extends GenericParserTest<LearnsetData>
    {
        @Override
        protected GenericParser<LearnsetData> createParser()
        {
            return injector.getInstance(Key.get(new TypeLiteral<>() {}));
        }
    }

    public static class EvolutionsTests extends GenericParserTest<EvolutionData>
    {
        @Override
        protected GenericParser<EvolutionData> createParser()
        {
            return injector.getInstance(Key.get(new TypeLiteral<>() {}));
        }
    }

    public static class TrainersTests extends GenericParserTest<TrainerData>
    {
        @Override
        protected GenericParser<TrainerData> createParser()
        {
            return injector.getInstance(Key.get(new TypeLiteral<>() {}));
        }
    }

    public static class MovesTests extends GenericParserTest<MoveData>
    {
        @Override
        protected GenericParser<MoveData> createParser()
        {
            return injector.getInstance(Key.get(new TypeLiteral<>() {}));
        }
    }

    public static class SinnohEncountersTests extends GenericParserTest<SinnohEncounterData>
    {
        @Override
        protected GenericParser<SinnohEncounterData> createParser()
        {
            return injector.getInstance(Key.get(new TypeLiteral<>() {}));
        }
    }

    public static class JohtoEncountersTests extends GenericParserTest<JohtoEncounterData>
    {
        @Override
        protected GenericParser<JohtoEncounterData> createParser()
        {
            return injector.getInstance(Key.get(new TypeLiteral<>() {}));
        }

        @BeforeEach
        @Override
        protected void setup()
        {
            parser = createParser();
            rom = NintendoDsRom.fromFile("HeartGold.nds");
            Game game = Game.parseBaseRom(rom.getGameCode());
            GameFiles.initialize(game);
            TextFiles.initialize(game);
            GameCodeBinaries.initialize(game);
            Tables.initialize(game);
        }
    }

    public static class ItemsTests extends GenericParserTest<ItemData>
    {
        @Override
        protected GenericParser<ItemData> createParser()
        {
            return injector.getInstance(Key.get(new TypeLiteral<>() {}));
        }
    }

    public static class TextBankTests extends GenericParserTest<TextBankData>
    {
        @Override
        protected GenericParser<TextBankData> createParser()
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
            return new ScriptParser();
        }

        @BeforeEach
        @Override
        protected void setup()
        {
            parser = createParser();
            rom = NintendoDsRom.fromFile("HeartGold.nds");
            Game game = Game.parseBaseRom(rom.getGameCode());
            GameFiles.initialize(game);
            TextFiles.initialize(game);
            GameCodeBinaries.initialize(game);
            Tables.initialize(game);
        }

        @Test
        @Override
        void outputMatchesInput()
        {
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
                for (int idx = 0; idx < originalNarc.getFiles().size(); idx++) {
                    byte[] outputFile = outputNarc.getFile(idx);
                    if (Arrays.equals(originalNarc.getFile(idx), outputFile))
                    {
                        assertThat(outputFile)
                                .isEqualTo(originalNarc.getFile(idx));
                    }
                    else if (outputFile.length != 0)
                    {
                        BytesDataContainer container = new BytesDataContainer();
                        container.insert(GameFiles.FIELD_SCRIPTS, null, outputFile);

                        GenericScriptData scriptData;
                        if (ScriptParser.testFileIsLevelScript(originalNarc.getFile(idx)))
                            scriptData = new LevelScriptData(container);
                        else
                            scriptData = new ScriptData(container);

                        container = scriptData.save();
                        byte[] rebuiltResult = container.get(GameFiles.FIELD_SCRIPTS, null);

                        if (Arrays.equals(rebuiltResult, outputFile))
                            System.out.println("Valid but non-1:1 Match: File " + idx);
                        else
                        {
                            System.err.println("File did not match original, attempted conditional rebuild but failed");
                        }

                        assertThat(rebuiltResult)
                                .isEqualTo(outputFile);
                    }
                    else
                    {
                        System.out.println("Level Script here");
                    }

                }
            }
        }
    }
}
