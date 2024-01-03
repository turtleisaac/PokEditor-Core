package io.github.turtleisaac.pokeditor.formats.scripts.antlr4;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.scripts.macros.MacrosParser;
import io.github.turtleisaac.pokeditor.formats.scripts.ScriptParser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class serves to function as a representation of a single macro defining the parameters
 * and types (byte/u8, short/u16, int/u32) of a single command for one of the games'
 * internal scripting languages
 */
public class CommandMacro
{
    private String name;
    private int id = -1;
    private String[] parameters;

    private MacrosParser.EntryContext entryContext;


    public Object[] readParameters(MemBuf.MemBufReader reader)
    {
        CommandReader commandReader = new CommandReader(reader, this);
        commandReader.visitEntry(entryContext);

        if (parameters.length != 0)
        {
            Object[] arr = new Object[parameters.length];

            int idx = 0;
            for (String parameter : parameters) {
                arr[idx++] = commandReader.parameterToValueMap.get(parameter);
            }
            return arr;
        }
        return null;
    }

    public void write(MemBuf memBuf, CommandWriter.LabelOffsetObtainer offsetObtainer, Object[] parameterValues)
    {
        Map<String, Object> parameterToValueMap = new HashMap<>();

        if (parameters.length != 0)
        {
            int idx = 0;
            for (String parameter : parameters) {
                Object param = parameterValues[idx++];
                if (param instanceof Number number)
                    parameterToValueMap.put(parameter, number);
                else if (param instanceof String str)
                {
                    Object value = ScriptParser.definedValues.get(str);
                    parameterToValueMap.put(parameter, Objects.requireNonNullElse(value, param));
                }

            }
        }

        CommandWriter commandWriter = new CommandWriter(memBuf.writer(), offsetObtainer, parameterToValueMap);
        commandWriter.visitEntry(entryContext);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getId()
    {
        return id;
    }

    protected void setId(int id)
    {
        this.id = id;
    }

    public String[] getParameters()
    {
        return parameters;
    }

    protected void setParameters(String[] parameters)
    {
        this.parameters = parameters;
    }

    public MacrosParser.EntryContext getEntryContext()
    {
        return entryContext;
    }

    public void setEntryContext(MacrosParser.EntryContext entryContext)
    {
        this.entryContext = entryContext;
    }

    public String getMacroRepresentation()
    {
        return entryContext.getText();
    }

    @Override
    public String toString()
    {
        if (parameters != null)
            return name + " " + Arrays.toString(parameters);
        return name;
    }

    public static class ConvenienceCommandMacro extends CommandMacro
    {
        String[] commands;

        public void setCommands(String[] commands)
        {
            this.commands = commands;
        }

        public String[] getCommands()
        {
            return commands;
        }
    }
}
