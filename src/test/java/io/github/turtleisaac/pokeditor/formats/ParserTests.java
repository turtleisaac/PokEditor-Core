package io.github.turtleisaac.pokeditor.formats;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import io.github.turtleisaac.nds4j.NintendoDsRom;
import io.github.turtleisaac.pokeditor.formats.encounters.JohtoEncounterData;
import io.github.turtleisaac.pokeditor.formats.encounters.SinnohEncounterData;
import io.github.turtleisaac.pokeditor.formats.evolutions.EvolutionData;
import io.github.turtleisaac.pokeditor.formats.items.ItemData;
import io.github.turtleisaac.pokeditor.formats.learnsets.LearnsetData;
import io.github.turtleisaac.pokeditor.formats.moves.MoveData;
import io.github.turtleisaac.pokeditor.formats.personal.PersonalData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.formats.trainers.TrainerData;
import io.github.turtleisaac.pokeditor.project.Game;
import org.junit.jupiter.api.BeforeEach;

import static io.github.turtleisaac.pokeditor.formats.TestsInjector.injector;

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
            GameFiles.initialize(Game.HeartGold);
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
}
