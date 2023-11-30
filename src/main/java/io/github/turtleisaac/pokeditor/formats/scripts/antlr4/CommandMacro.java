package io.github.turtleisaac.pokeditor.formats.scripts.antlr4;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.scripts.MacrosParser;

import java.util.Arrays;

public class CommandMacro
{
    private String name;
    private int id = -1;
    private String[] parameters;

    private MacrosParser.EntryContext entryContext;


    public Number[] readParameters(MemBuf.MemBufReader reader)
    {
        CommandReader commandReader = new CommandReader(reader, this);
        commandReader.visitEntry(entryContext);

        if (parameters.length != 0)
        {
            Number[] arr = new Number[parameters.length];

            int idx = 0;
            for (String parameter : parameters) {
                arr[idx++] = commandReader.parameterToValueMap.get(parameter);
            }
            return arr;
        }
        return null;
    }

    public void write(MemBuf outputBuf)
    {
        CommandWriter writer = new CommandWriter(outputBuf);
        writer.visitEntry(entryContext);
    }

    public String getName()
    {
        return name;
    }

    protected void setName(String name)
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
}
