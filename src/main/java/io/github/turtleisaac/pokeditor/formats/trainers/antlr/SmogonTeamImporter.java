package io.github.turtleisaac.pokeditor.formats.trainers.antlr;

import io.github.turtleisaac.pokeditor.formats.trainers.SmogonTeamBaseVisitor;
import io.github.turtleisaac.pokeditor.formats.trainers.SmogonTeamLexer;
import io.github.turtleisaac.pokeditor.formats.trainers.SmogonTeamParser;
import io.github.turtleisaac.pokeditor.formats.trainers.TrainerData;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.ArrayList;
import java.util.function.BiFunction;

public class SmogonTeamImporter extends SmogonTeamBaseVisitor<Void>
{
    private final BiFunction<SmogonStringSources, String, Integer> stringReplacementFunction;

    private ArrayList<TrainerData.TrainerPartyEntry> trainerPartyEntries;
    private TrainerData.TrainerPartyEntry current;
    int currentMoveIndex;

    public static ArrayList<TrainerData.TrainerPartyEntry> importSmogonTeam(String text, BiFunction<SmogonStringSources, String, Integer> stringReplacementFunction)
    {
        SmogonTeamImporter importer = new SmogonTeamImporter(stringReplacementFunction);

        SmogonTeamLexer lexer = new SmogonTeamLexer(CharStreams.fromString(text));

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SmogonTeamParser parser = new SmogonTeamParser(tokens);
        importer.visitTeam(parser.team());

        return importer.trainerPartyEntries;
    }

    private SmogonTeamImporter(BiFunction<SmogonStringSources, String, Integer> stringReplacementFunction)
    {
        this.stringReplacementFunction = stringReplacementFunction;
        trainerPartyEntries = new ArrayList<>();
    }

    @Override
    public Void visitTeam(SmogonTeamParser.TeamContext ctx)
    {
        return super.visitTeam(ctx);
//        for (ParseTree child : ctx.children)
//        {
//            if (child instanceof SmogonTeamParser.SpeciesEntryContext)
//            {
//                child.accept(this);
//            }
//        }
//        return null;
    }

    @Override
    public Void visitSpeciesEntry(SmogonTeamParser.SpeciesEntryContext ctx)
    {
        current = new TrainerData.TrainerPartyEntry();
        trainerPartyEntries.add(current);
        currentMoveIndex = 0;
        return super.visitSpeciesEntry(ctx);
    }

    @Override
    public Void visitSpecies(SmogonTeamParser.SpeciesContext ctx)
    {
        for (ParseTree child : ctx.children)
        {
            if (child instanceof TerminalNodeImpl terminalNode && terminalNode.symbol.getType() == SmogonTeamLexer.NAME)
            {
                current.setSpecies(stringReplacementFunction.apply(SmogonStringSources.SPECIES, terminalNode.getText()));
                break;
            }
        }
        return super.visitSpecies(ctx);
    }

    @Override
    public Void visitItem(SmogonTeamParser.ItemContext ctx)
    {
        for (ParseTree child : ctx.children)
        {
            if (child instanceof SmogonTeamParser.NameWithSpaceContext)
            {
                current.setHeldItem(stringReplacementFunction.apply(SmogonStringSources.ITEMS, child.getText()));
                break;
            }
        }
        return null;
    }

    @Override
    public Void visitAbility(SmogonTeamParser.AbilityContext ctx)
    {
        for (ParseTree child : ctx.children)
        {
            if (child instanceof SmogonTeamParser.NameWithSpaceContext)
            {
                current.setAbility(stringReplacementFunction.apply(SmogonStringSources.ABILITIES, child.getText()));
                break;
            }
        }
        return null;
    }

    @Override
    public Void visitLevel(SmogonTeamParser.LevelContext ctx)
    {
        for (ParseTree child : ctx.children)
        {
            if (child instanceof TerminalNodeImpl terminalNode && terminalNode.symbol.getType() == SmogonTeamLexer.NUMBER)
            {
                current.setLevel(Integer.parseInt(child.getText()));
                break;
            }
        }
        return null;
    }

    @Override
    public Void visitEffortValues(SmogonTeamParser.EffortValuesContext ctx)
    {
        return super.visitEffortValues(ctx);
    }

    @Override
    public Void visitNature(SmogonTeamParser.NatureContext ctx)
    {
        return super.visitNature(ctx);
    }

    @Override
    public Void visitMove(SmogonTeamParser.MoveContext ctx)
    {
        for (ParseTree child : ctx.children)
        {
            if (child instanceof SmogonTeamParser.NameWithSpaceContext)
            {
                int moveID;
                try {
                    moveID = stringReplacementFunction.apply(SmogonStringSources.MOVES, child.getText());
                } catch (NullPointerException exception) {
                    throw new RuntimeException("Invalid string provided: " + child.getText(), exception);
                }
                current.setMove(currentMoveIndex++, moveID);
                break;
            }
        }

        return null;
    }

    public enum SmogonStringSources
    {
        SPECIES,
        ITEMS,
        ABILITIES,
        MOVES,
        NATURES
    }
}
