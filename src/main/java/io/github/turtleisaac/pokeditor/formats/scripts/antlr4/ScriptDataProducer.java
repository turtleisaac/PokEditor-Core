package io.github.turtleisaac.pokeditor.formats.scripts.antlr4;

import io.github.turtleisaac.pokeditor.formats.scripts.*;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ScriptDataProducer extends ScriptFileBaseVisitor<Void>
{
    private ScriptData data;

    private HashMap<Integer, ScriptData.ScriptLabel> scriptEntryPoints;

    private ScriptCompilationException scriptCompilationException;

    public ScriptData produceScriptData(String text) throws ScriptCompilationException
    {
        data = new ScriptData();
        scriptEntryPoints = new HashMap<>();
        scriptCompilationException = new ScriptCompilationException();

        ScriptFileLexer lexer = new ScriptFileLexer(CharStreams.fromString(text));

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ScriptFileParser parser = new ScriptFileParser(tokens);
        visitScript_file(parser.script_file());

        Set<Integer> keys = scriptEntryPoints.keySet();
        List<Integer> scriptNumbers = keys.stream().sorted().toList();

        boolean missingScriptNumber = false;
        for (int i = 0; i < scriptNumbers.size(); i++)
        {
            int current = scriptNumbers.get(i);

            if (current != i + 1)
            {
                missingScriptNumber = true;
                scriptCompilationException.addSuppressed(new ScriptCompilationException("You are missing a script with ID number: " + (i+1)));
            }
        }

        if (missingScriptNumber)
        {
            scriptCompilationException.addSuppressed(new ScriptCompilationException(String.format("As you have %d script(s) in this file, they should be numbered [1, %d)", scriptNumbers.size(), scriptNumbers.size()+1)));
        }

        if (scriptCompilationException.getSuppressed().length != 0)
            throw scriptCompilationException;

        return data;
    }

    @Override
    public Void visitLabel_definition(ScriptFileParser.Label_definitionContext ctx)
    {
        String labelName = null;
        boolean script = false;
        int scriptNumber = -1;

        for (ParseTree child : ctx.children)
        {
            if (child instanceof ScriptFileParser.LabelContext)
            {
                labelName = child.getText();
                break;
            }

            if (child instanceof ScriptFileParser.Script_definitionContext scriptDefinitionContext)
            {
                script = true;
                for (ParseTree scriptChild : scriptDefinitionContext.children)
                {
                    if (scriptChild instanceof TerminalNodeImpl terminalNode)
                    {
                        if (terminalNode.symbol.getType() == ScriptFileLexer.NUMBER)
                        {
                            if (terminalNode.symbol.getStartIndex() == -1)
                            {
                                scriptCompilationException.addSuppressed(new ScriptCompilationException("Missing a script number at: " + scriptDefinitionContext.getText()));
                            }
                            else if (Integer.parseInt(terminalNode.getText()) != 0)
                            {
                                scriptNumber = Integer.parseInt(terminalNode.getText());
                            }
                            break;
                        }
                    }
                }

//                    scriptDefinitionContext.accept(this);
//                    break;
            }
        }

        if (labelName != null)
        {
            ScriptData.ScriptLabel label = new ScriptData.ScriptLabel(labelName);

            boolean foundConflict = false;
            for (GenericScriptData.ScriptComponent scriptComponent : data)
            {
                if (scriptComponent instanceof ScriptData.ScriptLabel existingLabel)
                {
                    if (existingLabel.getName().equals(labelName))
                    {
                        foundConflict = true;
                        break;
                    }
                }
            }

            if (foundConflict)
            {
                scriptCompilationException.addSuppressed(new ScriptCompilationException("There is already a label with name \"" + labelName + "\""));
                return null;
            }

            data.add(label);
            if (script && scriptNumber != -1)
            {
                if (scriptEntryPoints.containsKey(scriptNumber))
                    scriptCompilationException.addSuppressed(new ScriptCompilationException("There is already a script with ID number " + scriptNumber));
                else
                    scriptEntryPoints.put(scriptNumber, label);
            }
        }


        return null;
    }

    @Override
    public Void visitAction_definition(ScriptFileParser.Action_definitionContext ctx)
    {
        String actionName = null;

        for (ParseTree child : ctx.children)
        {
            if (child instanceof ScriptFileParser.ActionContext)
            {
                actionName = child.getText();
                break;
            }
        }

        if (actionName != null)
        {
            ScriptData.ActionLabel action = new ScriptData.ActionLabel(actionName);

            boolean foundConflict = false;
            for (GenericScriptData.ScriptComponent scriptComponent : data)
            {
                if (scriptComponent instanceof ScriptData.ActionLabel existingLabel)
                {
                    if (existingLabel.getName().equals(actionName))
                    {
                        foundConflict = true;
                        break;
                    }
                }
            }

            if (foundConflict)
            {
                scriptCompilationException.addSuppressed(new ScriptCompilationException("There is already an action with name \"" + actionName + "\""));
                return null;
            }

            data.add(action);
        }


        return null;
    }

    @Override
    public Void visitCommand(ScriptFileParser.CommandContext ctx)
    {
        String name = null;
        CommandMacro commandMacro = null;
        Object[] parameters = null;

        for (ParseTree child : ctx.children)
        {
            if (child instanceof TerminalNodeImpl terminalNode)
            {
                if (terminalNode.symbol.getType() == ScriptFileLexer.NAME)
                {
                    name = terminalNode.getText();
                    for (CommandMacro macro : ScriptParser.commandMacros)
                    {
                        if (macro.getName().equals(terminalNode.getText()))
                        {
                            commandMacro = macro;
                            break;
                        }
                    }
                }
            }
            else if (child instanceof ScriptFileParser.ParametersContext)
            {
                parameters = child.accept(new ScriptFileBaseVisitor<>()
                {
                    @Override
                    public Object[] visitParameters(ScriptFileParser.ParametersContext ctx)
                    {
                        if (ctx.children == null)
                            return null;

                        List<Object> objects = new ArrayList<>();
                        for (ParseTree parametersChild : ctx.children) {
                            if (parametersChild instanceof ScriptFileParser.ParameterContext parameterContext) {
                                objects.add(visitCommandParameterHelper(parameterContext));
                            }
                        }

                        return objects.toArray(Object[]::new);
                    }
                });
            }
        }

        if (commandMacro == null) {
            if (name != null)
                scriptCompilationException.addSuppressed(new ScriptCompilationException(String.format("\"%s\" is not a valid command name", name)));
            else
                scriptCompilationException.addSuppressed(new ScriptCompilationException("Name not found for command: " + ctx.getText()));
            return null; //todo better
        }

        ScriptData.ScriptCommand command = new ScriptData.ScriptCommand(commandMacro);
        command.setParameters(parameters);
        data.add(command);

        return super.visitCommand(ctx);
    }

    private Object visitCommandParameterHelper(ScriptFileParser.ParameterContext ctx)
    {
        Object result = null;
        for (ParseTree child : ctx.children)
        {
            if (child instanceof TerminalNodeImpl terminalNode)
            {
                String text = terminalNode.getText();
                int type = terminalNode.symbol.getType();

                if (type == ScriptFileLexer.NUMBER)
                {
                    if (text.contains("0x"))
                        return Integer.parseInt(text.substring(2), 16);
                    else
                        return Integer.parseInt(text);
                }
                else if (type == ScriptFileLexer.NAME)
                {
                    if (text.startsWith("0x"))
                    {
                        try {
                            return Integer.parseInt(text.substring(2), 16);
                        } catch(NumberFormatException ignored) {}
                    }
                    return text;
                }
            }
            else if (child instanceof ScriptFileParser.LabelContext labelContext)
            {
                return labelContext.getText();
            }
        }

        return ctx.getText();
    }

    @Override
    public Void visitAction_command(ScriptFileParser.Action_commandContext ctx)
    {
        return super.visitAction_command(ctx);
    }

    public static class ScriptCompilationException extends Exception
    {
        public ScriptCompilationException()
        {
            super("The following errors occurred while attempting to compile this script file");
        }

        public ScriptCompilationException(String message)
        {
            super(message);
        }
    }
}
