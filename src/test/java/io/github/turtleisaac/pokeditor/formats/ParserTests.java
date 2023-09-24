package io.github.turtleisaac.pokeditor.formats;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import io.github.turtleisaac.pokeditor.formats.evolutions.EvolutionData;
import io.github.turtleisaac.pokeditor.formats.learnsets.LearnsetData;
import io.github.turtleisaac.pokeditor.formats.moves.MoveData;
import io.github.turtleisaac.pokeditor.formats.personal.PersonalData;
import io.github.turtleisaac.pokeditor.formats.trainers.TrainerData;

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
}
