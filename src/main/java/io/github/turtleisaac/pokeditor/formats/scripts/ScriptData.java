package io.github.turtleisaac.pokeditor.formats.scripts;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.formats.scripts.antlr4.CommandMacro;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class ScriptData extends GenericScriptData
{
    private static final IntPredicate isCallCommand = commandID -> commandID >= 0x16 && commandID <= 0x1D && commandID != 0x1B;
    private static final IntPredicate isEndCommand = commandId -> commandId == 0x2 || commandId == 0x16 || commandId == 0x1B;

    public ScriptData(BytesDataContainer files)
    {
        super(files);
    }

    @Override
    public void setData(BytesDataContainer files)
    {
        if (!files.containsKey(GameFiles.SCRIPTS))
        {
            throw new RuntimeException("Script file not provided to editor");
        }

        MemBuf dataBuf = MemBuf.create(files.get(GameFiles.SCRIPTS, null));
        MemBuf.MemBufReader reader = dataBuf.reader();

        ArrayList<Integer> globalScriptOffsets = new ArrayList<>();

        boolean isLevelScript;
        try {
            isLevelScript = isLevelScript(reader, globalScriptOffsets);
        }
        catch (IllegalStateException e)
        {
            // in theory should only happen if the file is not a level script?
            // Now this may appear in a few level scripts that don't have a 4-byte aligned "00 00 00 00"
            throw new RuntimeException(e);
        }

        if (isLevelScript) {
            throw new IllegalStateException("This is a level script file, not a normal script file");
        }

        ArrayList<Integer> labelOffsets = new ArrayList<>(globalScriptOffsets);
        ArrayList<Integer> visitedOffsets = new ArrayList<>();
//
//        for (int i = 0; i < globalScriptOffsets.size(); i++)
//        {
//            readAtOffset(dataBuf, globalScriptOffsets, labelOffsets, visitedOffsets, globalScriptOffsets.get(i), false);
//        }

        int lastSize = labelOffsets.size();
        do {
            lastSize = labelOffsets.size();
            for (int i = 0; i < labelOffsets.size(); i++)
            {
                readAtOffset(dataBuf, globalScriptOffsets, labelOffsets, visitedOffsets, labelOffsets.get(i), false);
            }
        }
        while (lastSize != labelOffsets.size());

//        labelOffsets.sort(Comparator.naturalOrder());
        for (int i = 0; i < labelOffsets.size(); i++)
        {
            readAtOffset(dataBuf, globalScriptOffsets, labelOffsets, visitedOffsets, labelOffsets.get(i), true);
        }

        for (CommandMacro commandMacro : ScriptParser.convenienceCommands)
        {
            boolean matchFound = true;
            CommandMacro.ConvenienceCommandMacro convenienceCommand = (CommandMacro.ConvenienceCommandMacro) commandMacro;
            int startIdx = 0;
            for (; startIdx < size() - convenienceCommand.getCommands().length; startIdx++)
            {
                matchFound = true;
                int convenienceIdx = 0;
                for (String commandName : convenienceCommand.getCommands())
                {
                    ScriptComponent component = get(startIdx + convenienceIdx++);
                    if (component instanceof ScriptCommand command)
                    {
                        if (!commandName.equals(command.getName()))
                        {
                            matchFound = false;
                        }
                    }
                    else
                    {
                        matchFound = false;
                    }
                }

                if (matchFound)
                {
                    break;
                }
            }

            if (matchFound)
            {
                for (int i = 0; i < convenienceCommand.getCommands().length; i++)
                {
                    remove(startIdx);
                }
                ScriptCommand convenience = new ScriptCommand(convenienceCommand);
                convenience.name = "*" + convenienceCommand.getName();
                add(startIdx, convenience);
                break;
            }
        }

        for (ScriptComponent component : this) {
            if (component instanceof ScriptCommand command)
                System.out.println("\t\t" + command.contextualToString(labelOffsets));
            else
                System.out.println("\t" + component.toString());
        }
    }

    private void readAtOffset(MemBuf dataBuf, ArrayList<Integer> globalScriptOffsets, ArrayList<Integer> labelOffsets, ArrayList<Integer> visitedOffsets, int offset, boolean finalRun)
    {
        MemBuf.MemBufReader reader = dataBuf.reader();
        if (visitedOffsets.contains(offset)) {
            return;
        }
        reader.setPosition(offset);
//        System.out.println("0x" + Integer.toHexString(offset));
        while (reader.getPosition() < dataBuf.writer().getPosition())
        {
            if (finalRun)
            {
                visitedOffsets.add(reader.getPosition());
                if (globalScriptOffsets.contains(reader.getPosition())) {
                    add(new ScriptLabel("script(" + globalScriptOffsets.indexOf(reader.getPosition()) + ") label_" + Integer.toHexString(reader.getPosition())));
//                    System.out.println( + ": ");
                } else if (labelOffsets.contains(reader.getPosition())) {
                    add(new ScriptLabel("label_" + Integer.toHexString(reader.getPosition())));
//                    System.out.println("\t label_" + labelOffsets.indexOf(reader.getPosition()) + ": ");
                }
            }

            int commandID = reader.readUInt16();
            if (commandID == 0)
                break;

            CommandMacro commandMacro = ScriptParser.nativeCommands.get(commandID);
            if (commandMacro == null) {
                System.currentTimeMillis();
            }

            ScriptCommand command = new ScriptCommand(commandMacro);
            command.name = commandMacro.getName();

            if (command.name.contains("goto")) {
                System.currentTimeMillis();
            }

            command.parameters = commandMacro.readParameters(reader);

            if (isCallCommand.test(commandID)) {
                int offsetParam = (int) command.parameters[command.parameters.length-1];
                if (!labelOffsets.contains(offsetParam))
                    labelOffsets.add(offsetParam);
            }

            if (finalRun)
                add(command);

//            System.out.println("\t\t" + command.contextualToString(labelOffsets));
            if (isEndCommand.test(commandID))
                break;
        }
    }

    @Override
    public BytesDataContainer save()
    {
        MemBuf dataBuf = MemBuf.create();
        MemBuf.MemBufWriter writer = dataBuf.writer();

        return new BytesDataContainer(GameFiles.SCRIPTS, null, dataBuf.reader().getBuffer());
    }

    interface ScriptComponent {
        String getName();
    }

    static class ScriptLabel implements ScriptComponent {
        private String name;

        public ScriptLabel(String name)
        {
            this.name = name;
        }

        @Override
        public String toString()
        {
            return name + ": ";
        }

        @Override
        public String getName()
        {
            return name;
        }
    }

    static class ScriptCommand implements ScriptComponent {
        String name;
        Number[] parameters;

        private CommandMacro commandMacro;

        public ScriptCommand(CommandMacro commandMacro)
        {
            this.commandMacro = commandMacro;
        }

        @Override
        public String toString()
        {
            if (parameters == null)
                return name;
            StringBuilder builder = new StringBuilder(name).append(" [");
            for (int i = 0; i < parameters.length; i++)
            {
                if (commandMacro.getParameters()[i].contains("var"))
                {
                    builder.append("0x").append(Integer.toHexString((int) parameters[i]));
                }
                else
                {
                    builder.append(parameters[i]);
                }

                if (i != parameters.length - 1)
                {
                    builder.append(", ");
                }
            }

            return builder.append("]").toString();
        }

        public String contextualToString(List<Integer> offsets)
        {
            if (!isCallCommand.test(commandMacro.getId())) {
                return toString();
            }

            StringBuilder builder = new StringBuilder(name).append(" [");
            for (int i = 0; i < parameters.length; i++)
            {
                String paramName = commandMacro.getParameters()[i];
                if (paramName.contains("var"))
                {
                    builder.append("0x").append(Integer.toHexString((int) parameters[i]));
                }
                else if (paramName.contains("dest") || paramName.contains("sub"))
                {
                    builder.append("label_").append(Integer.toHexString((int) parameters[i]));
                }
                else
                {
                    builder.append(parameters[i]);
                }

                if (i != parameters.length - 1)
                {
                    builder.append(", ");
                }
            }

            return builder.append("]").toString();
        }

        @Override
        public String getName()
        {
            return name;
        }
    }

    static abstract class AbstractScript extends ArrayList<ScriptCommand>
    {

    }

    static class Script extends AbstractScript {

    }

    static class ActionScript extends AbstractScript {

    }
}
