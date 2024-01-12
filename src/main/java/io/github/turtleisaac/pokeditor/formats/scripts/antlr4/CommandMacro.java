package io.github.turtleisaac.pokeditor.formats.scripts.antlr4;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.scripts.macros.MacrosParser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

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

    public void write(MemBuf memBuf, CommandWriter.LabelOffsetObtainer offsetObtainer, Object[] parameterValues, ParameterStringReplacementFunction customReplaceParameterStringWithIntegerFunction, ParameterStringReplacementFunction defaultReplaceParameterStringWithIntegerFunction)
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
                    Object value = null;

                    if (customReplaceParameterStringWithIntegerFunction != null)
                    {
                        value = customReplaceParameterStringWithIntegerFunction.apply(id, str);
                    }

                    if (value != null)
                    {
                        parameterToValueMap.put(parameter, value);
                    }
                    else
                    {
                        value = defaultReplaceParameterStringWithIntegerFunction.apply(id, str);
                        if (value != null)
                            parameterToValueMap.put(parameter, value);
                        else
                            throw new RuntimeException(String.format("An invalid parameter was provided (%s) in \"%s\"", str, this));
                    }
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

    public interface ParameterStringReplacementFunction extends BiFunction<Integer, String, Object>
    {

        /**
         * Replaces String parameters for a command with the given ID with the corresponding integer value.
         *
         * @param commandID an <code>Integer</code> containing the ID of the command whose parameters need to be replace
         * @param parameterString a <code>String</code> containing a parameter for the given command
         * @return the integer value to replace the parameter string with
         */
        @Override
        Object apply(Integer commandID, String parameterString);
    }

    public static class OntoIntegerUppercaseStringMap
    {
        private final Map<Integer, String> numberToStringMap;
        private final Map<String, Integer> stringToNumberMap;

        public OntoIntegerUppercaseStringMap()
        {
            numberToStringMap = new HashMap<>();
            stringToNumberMap = new HashMap<>();
        }

        public void put(Integer number, String str)
        {
            str = str.toUpperCase();
            numberToStringMap.put(number, str);
            stringToNumberMap.put(str, number);
        }

        public Integer get(String str)
        {
            str = str.toUpperCase();
            return stringToNumberMap.get(str);
        }

        public String get(Integer number)
        {
            return numberToStringMap.get(number);
        }
    }
}
