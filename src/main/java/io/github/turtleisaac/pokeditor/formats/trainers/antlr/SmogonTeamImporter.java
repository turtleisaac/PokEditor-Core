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

        // the grammar requires each species entry to start on a fresh line and the paste to end with one
        String normalized = text;
        if (!normalized.startsWith("\n") && !normalized.startsWith("\r"))
            normalized = "\n" + normalized;
        if (!normalized.endsWith("\n"))
            normalized = normalized + "\n";

        SmogonTeamLexer lexer = new SmogonTeamLexer(CharStreams.fromString(normalized));

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SmogonTeamParser parser = new SmogonTeamParser(tokens);
        SmogonTeamParser.TeamContext team = parser.team();

        int syntaxErrors = parser.getNumberOfSyntaxErrors();
        if (syntaxErrors != 0)
        {
            throw new SmogonImportException(String.format("The provided team could not be understood - %d syntax error(s) were found in it", syntaxErrors));
        }

        importer.visitTeam(team);

        if (importer.trainerPartyEntries.size() > TrainerData.MAX_NUMBER_TRAINER_MONS)
        {
            throw new SmogonImportException(String.format("A trainer can have at most %d Pokemon, but the provided team contains %d", TrainerData.MAX_NUMBER_TRAINER_MONS, importer.trainerPartyEntries.size()));
        }

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
    public Void visitIndividualValues(SmogonTeamParser.IndividualValuesContext ctx)
    {
        // the gen 4 trainer format has a single "difficulty" byte rather than per-stat IVs, and the exporter
        // writes the same value out for every stat, so the first entry is the one which matters
        for (ParseTree child : ctx.children)
        {
            if (child instanceof SmogonTeamParser.EffortValueEntryContext entryContext)
            {
                for (ParseTree entryChild : entryContext.children)
                {
                    if (entryChild instanceof TerminalNodeImpl terminalNode && terminalNode.symbol.getType() == SmogonTeamLexer.NUMBER)
                    {
                        int iv = Integer.parseInt(terminalNode.getText());
                        if (iv < 0 || iv > MAX_IV)
                        {
                            throw new SmogonImportException("An IV must be between 0 and " + MAX_IV + ". Provided: " + iv);
                        }
                        current.setDifficultyValue(iv * MAX_DIFFICULTY_VALUE / MAX_IV);
                        return null;
                    }
                }
            }
        }

        return null;
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

    /**
     * The largest value an individual value can hold
     */
    public static final int MAX_IV = 31;

    /**
     * The largest value a trainer party entry's difficulty byte can hold
     */
    public static final int MAX_DIFFICULTY_VALUE = 255;

    /**
     * Thrown when a pasted Smogon team cannot be turned into a trainer's party
     */
    public static class SmogonImportException extends RuntimeException
    {
        public SmogonImportException(String message)
        {
            super(message);
        }
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
