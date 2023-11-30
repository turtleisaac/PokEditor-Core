package io.github.turtleisaac.pokeditor.formats.scripts.antlr4;

import io.github.turtleisaac.pokeditor.formats.scripts.MacrosLexer;
import io.github.turtleisaac.pokeditor.formats.scripts.MacrosParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommandDiscoverer extends CommandMacroVisitor<CommandMacro>
{
    public List<CommandMacro> discoverAllCommands(MacrosParser.EntriesContext ctx)
    {
        List<CommandMacro> commands = new ArrayList<>();
        List<ParseTree> macros = ctx.children.stream().filter(parseTree -> parseTree instanceof MacrosParser.EntryContext).toList();

        for (ParseTree child : macros)
        {
            commands.add(child.accept(this));
        }

        return commands;
    }

    @Override
    public CommandMacro visitEntry(MacrosParser.EntryContext ctx)
    {
        CommandMacro macro = null;

        for (ParseTree child : ctx.children)
        {
            if (child instanceof MacrosParser.DefinitionContext)
            {
                macro = child.accept(this);
                macro.setEntryContext(ctx);
            }
            else if (child instanceof MacrosParser.Id_lineContext)
            {
                assert macro != null;
                macro.setId(child.accept(new CommandMacroVisitor<>()
                {
                    @Override
                    protected Integer idLineAction(int idNumber)
                    {
                        return idNumber;
                    }
                }));
            }
        }

        return macro;
    }

    @Override
    public CommandMacro visitDefinition(MacrosParser.DefinitionContext ctx)
    {
        CommandMacro macro = new CommandMacro();
        ArrayList<String> parameters = new ArrayList<>();

        for (int i = 0; i < ctx.getChildCount(); i++)
        {
            ParseTree c = ctx.getChild(i);

            if (c instanceof TerminalNodeImpl terminalNode) {
                int type = terminalNode.symbol.getType();
                if (type == MacrosLexer.NAME) {
                    macro.setName(c.getText());
                }
            }
            else {
                parameters.add(c.accept(new CommandMacroVisitor<String>()
                {
                    String visitArgument(ParserRuleContext ctx) {
                        for(ParseTree child : ctx.children) {
                            if(child instanceof TerminalNodeImpl terminalNode) {
                                int type = terminalNode.symbol.getType();
                                if(type == MacrosLexer.NAME) {
                                    return child.getText();
                                }
                            }
                        }
                        return null;
                    }

                    @Override
                    public String visitArgument_definition(MacrosParser.Argument_definitionContext ctx)
                    {
                        return visitArgument(ctx);
                    }

                    @Override
                    public String visitLast_argument_definition(MacrosParser.Last_argument_definitionContext ctx)
                    {
                        return visitArgument(ctx);
                    }
                }));
            }
        }

        macro.setParameters(parameters.toArray(String[]::new));

        return macro;
    }

    public static void main(String[] args) throws IOException
    {
        MacrosLexer lexer = new MacrosLexer(CharStreams.fromFileName("HgScriptRaw.txt"));

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MacrosParser parser = new MacrosParser(tokens);
//        ParseTree parseTree = parser.entry();

        MacrosParser.EntriesContext entryContext = parser.entries();
        CommandDiscoverer visitor = new CommandDiscoverer();

        List<CommandMacro> commandMacros = visitor.discoverAllCommands(entryContext);

        HashMap<Integer, CommandMacro> commands = new HashMap<>();
        commandMacros.forEach(commandMacro ->
        {
            if (commandMacro.getId() >= 0)
                commands.put(commandMacro.getId(), commandMacro);
        });

        System.currentTimeMillis();
    }
}
