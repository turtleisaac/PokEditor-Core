package io.github.turtleisaac.pokeditor.formats.scripts;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.formats.scripts.antlr4.CommandMacro;
import io.github.turtleisaac.pokeditor.formats.scripts.antlr4.CommandWriter;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

import static io.github.turtleisaac.pokeditor.formats.scripts.ScriptParser.SCRIPT_MAGIC_ID;

public class ScriptData extends GenericScriptData
{
    private static final IntPredicate isCallCommand = commandID -> commandID >= 0x16 && commandID <= 0x1D && commandID != 0x1B;
    private static final IntPredicate isEndCommand = commandId -> commandId == 0x2 || commandId == 0x16 || commandId == 0x1B;
    private static final IntPredicate isDoIfCommand = commandID -> commandID == 28 || commandID == 29 || commandID == 225;
    private static final IntPredicate isMovementCommand = commandID -> commandID == 0x5E;
    private static final IntPredicate isEndMovementCommand = commandID -> commandID == 0xFE;
//    private static final IntPredicate isOverworldObjectCommand

    private static final CommandMacro.OntoIntegerUppercaseStringMap overworldNames = new CommandMacro.OntoIntegerUppercaseStringMap();
    private static final CommandMacro.OntoIntegerUppercaseStringMap comparators = new CommandMacro.OntoIntegerUppercaseStringMap();

    static {
        overworldNames.put(250, "Daycare1");
        overworldNames.put(251, "Daycare2");
        overworldNames.put(253, "Follower");
        overworldNames.put(255, "Player");

        comparators.put(0, "LESS");
        comparators.put(1, "EQUAL");
        comparators.put(2, "GREATER");
        comparators.put(3, "LESS_OR_EQUAL");
        comparators.put(4, "GREATER_OR_EQUAL");
        comparators.put(5, "DIFFERENT");
    }

    private List<ScriptLabel> scripts;

    public ScriptData(BytesDataContainer files)
    {
        super(files);
    }

    public ScriptData()
    {
        super();
    }

    @Override
    public void setData(BytesDataContainer files)
    {
        if (!files.containsKey(GameFiles.FIELD_SCRIPTS))
        {
            throw new RuntimeException("Script file not provided to editor");
        }

        scripts = new ArrayList<>();

        MemBuf dataBuf = MemBuf.create(files.get(GameFiles.FIELD_SCRIPTS, null));
        MemBuf.MemBufReader reader = dataBuf.reader();

        ArrayList<Integer> globalScriptOffsets = new ArrayList<>();

        boolean isLevelScript;
        try {
            isLevelScript = isLevelScript(dataBuf, globalScriptOffsets);
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
        ArrayList<Integer> actionOffsets = new ArrayList<>();
        ArrayList<Integer> visitedOffsets = new ArrayList<>();

        HashMap<Integer, ScriptLabel> labelMap = new HashMap<>();
        HashMap<Integer, ActionLabel> actionMap = new HashMap<>();

        int lastSize;
        do {
            lastSize = labelOffsets.size();
            for (int i = 0; i < labelOffsets.size(); i++)
            {
                if (!actionOffsets.contains(labelOffsets.get(i)))
                    readAtOffset(dataBuf, globalScriptOffsets, labelOffsets, actionOffsets, visitedOffsets, labelOffsets.get(i), labelMap, false);
            }
        }
        while (lastSize != labelOffsets.size());

        labelOffsets.sort(Comparator.naturalOrder());
        for (int i = 0; i < labelOffsets.size(); i++)
        {
            if (!actionOffsets.contains(labelOffsets.get(i)))
                readAtOffset(dataBuf, globalScriptOffsets, labelOffsets, actionOffsets, visitedOffsets, labelOffsets.get(i), labelMap, true);
            else
                readActionAtOffset(dataBuf, actionOffsets, visitedOffsets, actionMap, labelOffsets.get(i));
        }

        List<ScriptLabel> labels = new ArrayList<>();
        List<ActionLabel> actions = new ArrayList<>();

        for (ScriptComponent component : this)
        {
            if (component instanceof ScriptLabel label)
            {
                label.name = "label_" + labels.size();
                labels.add(label);
            }
            else if (component instanceof ActionLabel actionLabel)
            {
                actionLabel.name = "action_" + actions.size();
                actions.add(actionLabel);
            }
        }

        scripts.sort(Comparator.comparingInt(ScriptLabel::getScriptID));

        if (scripts.size() != globalScriptOffsets.size())
            throw new RuntimeException(String.format("the expected number of scripts (%d) does not match the actual located amount (%d)", globalScriptOffsets.size(), globalScriptOffsets.size()));

        for (ScriptComponent component : this)
        {
            if (component instanceof ScriptCommand command && command.parameters != null)
            {
                if (isCallCommand.test(command.commandMacro.getId()))
                {
                    for (int i = 0; i < command.parameters.length; i++)
                    {
                        String paramName = command.commandMacro.getParameters()[i];
                        if ((paramName.contains("dest") || paramName.contains("sub")))
                        {
                            command.parameters[i] = "label_" + labels.indexOf(labelMap.get((Integer) command.parameters[i]));
                        }
                    }
                }
                else if (isMovementCommand.test(command.commandMacro.getId()))
                {
                    for (int i = 0; i < command.parameters.length; i++)
                    {
                        String paramName = command.commandMacro.getParameters()[i];
                        if ((paramName.contains("dest") || paramName.contains("sub")))
                        {
                            command.parameters[i] = "action_" + actions.indexOf(actionMap.get((Integer) command.parameters[i]));
                        }
                        else if (paramName.contains("overworld"))
                        {
                            if ((int) command.parameters[i] < 0x4000)
                            {
                                String valueName = overworldNames.get((int) command.parameters[i]);
                                command.parameters[i] = "Overworld." + (valueName != null ? valueName : command.parameters[i]);
                            }
                        }
                    }
                }
            }
        }

//        findAndReplaceSequencesWithConvenienceCommands();

//        for (ScriptComponent component : this) {
//            if (component instanceof ScriptCommand command)
//                System.out.println("\t\t" + command.contextualToString(labelOffsets));
//            else
//                System.out.println("\t" + component.toString());
//        }
    }

    private void readAtOffset(MemBuf dataBuf, ArrayList<Integer> globalScriptOffsets, ArrayList<Integer> labelOffsets, ArrayList<Integer> actionOffsets, ArrayList<Integer> visitedOffsets, int offset, HashMap<Integer, ScriptLabel> labelMap, boolean finalRun)
    {
        MemBuf.MemBufReader reader = dataBuf.reader();
        if (visitedOffsets.contains(offset)) {
            return;
        }

        reader.setPosition(offset);

        int currentPosition;

        while (reader.getPosition() < dataBuf.writer().getPosition())
        {
            currentPosition = reader.getPosition();
            if (finalRun && !visitedOffsets.contains(currentPosition))
            {
                visitedOffsets.add(currentPosition);
                if (globalScriptOffsets.contains(currentPosition))
                {
                    List<Integer> globalScriptOffsetsCopy = new ArrayList<>(globalScriptOffsets);
                    while (globalScriptOffsetsCopy.contains(currentPosition))
                    {
                        int idx = globalScriptOffsetsCopy.indexOf(currentPosition);
                        globalScriptOffsetsCopy.set(idx, null);
                        ScriptLabel label = new ScriptLabel("label_" + Integer.toHexString(currentPosition));
                        if (globalScriptOffsets.contains(currentPosition))
                        {
                            scripts.add(label);
                            label.setScriptID(idx+1);
                        }
                        labelMap.putIfAbsent(currentPosition, label);
                        add(label);
                    }
                }
                else if (labelOffsets.contains(currentPosition))
                {
                    ScriptLabel label = new ScriptLabel("label_" + Integer.toHexString(currentPosition));
                    labelMap.putIfAbsent(currentPosition, label);
                    add(label);
                }
                else if (actionOffsets.contains(currentPosition))
                {
                    ActionLabel actionLabel = new ActionLabel("action_" + Integer.toHexString(currentPosition));
                    add(actionLabel);
                }
            }

            int commandID = reader.readUInt16();
            if (commandID == 0)
                break;

//			System.err.println(commandID);
            CommandMacro commandMacro = ScriptParser.nativeCommands.get(commandID);
            if (commandMacro == null) {
                throw new RuntimeException("Invalid command ID: " + commandID);
            }

            ScriptCommand command = new ScriptCommand(commandMacro);
            command.name = commandMacro.getName();

            command.parameters = commandMacro.readParameters(reader);

//            if (command.parameters != null)
//            {
//                for (int i = 0; i < command.parameters.length; i++)
//                {
//                    if (command.parameters[i] == null) {
//                        System.currentTimeMillis();
//                    }
//                }
//            }

            if (isDoIfCommand.test(commandID))
            {
                String s = comparators.get((Integer) command.parameters[0]);
                if (s == null)
                    throw new IllegalStateException("Unexpected value: " + command.parameters[0]);

                command.parameters[0] = s;
            }

            if (isMovementCommand.test(commandID)) {
                int offsetParam = (int) command.parameters[command.parameters.length-1];
                if (!actionOffsets.contains(offsetParam))
                {
                    actionOffsets.add(offsetParam);
                    labelOffsets.add(offsetParam);
                }
            }

            if (isCallCommand.test(commandID)) {
                int offsetParam = (int) command.parameters[command.parameters.length-1];
                if (!labelOffsets.contains(offsetParam))
                    labelOffsets.add(offsetParam);
            }

//            System.out.println(command);

            if (finalRun)
                add(command);

            if (isEndCommand.test(commandID))
            {
//                if (reader.getPosition() < dataBuf.writer().getPosition() - 2)
//                {
//                    int next = reader.readUInt16();
//                    reader.setPosition(reader.getPosition() - 2);
//                    if (!isEndCommand.test(next))
//                    {
//                        break;
//                    }
//                }
//                else
//                {
                    break;
//                }

            }

        }
    }

    private void readActionAtOffset(MemBuf dataBuf, ArrayList<Integer> actionOffsets, ArrayList<Integer> visitedOffsets, HashMap<Integer, ActionLabel> actionMap, int offset)
    {
        MemBuf.MemBufReader reader = dataBuf.reader();
        if (visitedOffsets.contains(offset)) {
            return;
        }

        reader.setPosition(offset);

        while (reader.getPosition() < dataBuf.writer().getPosition())
        {
            if (!visitedOffsets.contains(reader.getPosition()))
            {
                visitedOffsets.add(reader.getPosition());
                if (actionOffsets.contains(reader.getPosition()))
                {
                    ActionLabel actionLabel = new ActionLabel("action_" + Integer.toHexString(reader.getPosition()));
                    actionMap.put(reader.getPosition(), actionLabel);
                    add(actionLabel);
                }
            }

            int commandID = reader.readUInt16();
            int parameter = reader.readUInt16();

            String name = ScriptParser.movementNames.get(commandID);
            if (name != null)
                add(new ActionCommand(name, commandID, parameter));
            else
                add(new ActionCommand(commandID, parameter));

            if (isEndMovementCommand.test(commandID))
            {
                break;
            }

//            CommandMacro commandMacro = ScriptParser.nativeCommands.get(commandID); //todo change to movement commands
//            if (commandMacro == null) {
//                System.currentTimeMillis();
//            }
//
//            ScriptCommand command = new ScriptCommand(commandMacro);
//            command.name = commandMacro.getName();
//
//            command.parameters = commandMacro.readParameters(reader);
        }
    }

    private void findAndReplaceSequencesWithConvenienceCommands()
    {
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
    }

    @Override
    public BytesDataContainer save()
    {
        List<ScriptLabel> labels = new ArrayList<>();
        List<ActionLabel> actions = new ArrayList<>();

        for (ScriptComponent component : this)
        {
            if (component instanceof ScriptLabel label)
            {
                labels.add(label);
            }
            else if (component instanceof ActionLabel actionLabel)
            {
                actions.add(actionLabel);
            }
        }

        MemBuf dataBuf = MemBuf.create();
        MemBuf.MemBufWriter writer = dataBuf.writer();

        // todo write header, then skip number of slots needed for all script offsets

        for (int i = 0; i < scripts.size(); i++)
        {
            writer.skip(4);
        }

        writer.writeShort((short) SCRIPT_MAGIC_ID);

        // write each command out, and if it is a jump command we need to store a placeholder for later when we come back and insert the offset (if the destination hasn't been written yet)
        // at the start of each script we need to grab the offset for the header
        // something something dark side

        int[] scriptOffsets = new int[scripts.size()];
        int[] labelOffsets = new int[labels.size()];
        int[] actionOffsets = new int[actions.size()];

        CommandWriter.LabelOffsetObtainer offsetObtainer = labelName ->
        {
            int idx = 0;
            boolean found = false;
            for (ScriptLabel label : labels)
            {
                if (label.getName().equals(labelName))
                {
                    return labelOffsets[idx];
                }
                idx++;
            }

            idx = 0;
            for (ActionLabel label : actions)
            {
                if (label.getName().equals(labelName))
                {
                    return actionOffsets[idx];
                }
                idx++;
            }

            return 0;
        };

        for (ScriptComponent component : this)
        {
            if (component instanceof ScriptLabel label)
            {
                int scriptNumber = scripts.indexOf(label);
                if (scriptNumber != -1) // if the label is in the list of scripts
                    scriptOffsets[scriptNumber] = writer.getPosition();

                labelOffsets[labels.indexOf(label)] = writer.getPosition();
            }
            else if (component instanceof ActionLabel actionLabel)
            {
                actionOffsets[actions.indexOf(actionLabel)] = writer.getPosition();
            }
            else if (component instanceof ScriptCommand command) // is a command
            {
                command.commandMacro.write(dataBuf, offsetObtainer, command.parameters, null, defaultReplaceParameterStringWithIntegerFunction);

//                if (isEndCommand.test(command.commandMacro.getId()))
//                    if (writer.getPosition() % 4 != 0)
//                        writer.skip(4 - writer.getPosition() % 4);
            }
            else if (component instanceof ActionCommand actionCommand)
            {
                writer.writeShort((short) actionCommand.id);
                writer.writeShort((short) actionCommand.parameter);

//                if (isEndMovementCommand.test(actionCommand.id))
//                {
//                    if (writer.getPosition() % 4 != 0)
//                        writer.skip(4 - writer.getPosition() % 4);
//                }
            }
        }

        writer.setPosition(0);

        for (int i = 0; i < scripts.size(); i++)
        {
            int adjustment = writer.getPosition() + 4;
            writer.writeInt(scriptOffsets[i] - adjustment);
        }

        writer.skip(2);

        for (ScriptComponent component : this)
        {
            if (component instanceof ScriptCommand command) // is a command
            {
                command.commandMacro.write(dataBuf, offsetObtainer, command.parameters, null, defaultReplaceParameterStringWithIntegerFunction);
//                if (isEndCommand.test(command.commandMacro.getId()))
//                    if (writer.getPosition() % 4 != 0)
//                        writer.skip(4 - writer.getPosition() % 4);
            }
            else if (component instanceof ActionCommand actionCommand)
            {
                writer.writeShort((short) actionCommand.id);
                writer.writeShort((short) actionCommand.parameter);

//                if (isEndMovementCommand.test(actionCommand.id))
//                {
//                    if (writer.getPosition() % 4 != 0)
//                        writer.skip(4 - writer.getPosition() % 4);
//                }
            }
        }

//        writer.align(4);
        if (writer.getPosition() % 4 != 0)
            writer.skip(4 - writer.getPosition() % 4);
//        dataBuf.reader().setPosition(writer.getPosition() - 4);
//        int last = dataBuf.reader().readInt();
//        if (last != 0)
//            writer.skip(4);
//        dataBuf.reader().setPosition(0);

        return new BytesDataContainer(GameFiles.FIELD_SCRIPTS, null, dataBuf.reader().getBuffer());
    }

    public List<ScriptLabel> getScripts()
    {
        return scripts;
    }

//    public ArrayList<ScriptLabel> getLabels()
//    {
//        return labels;
//    }


    public void setScripts(List<ScriptLabel> scripts)
    {
        this.scripts = scripts;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for (ScriptComponent component : this)
        {
            if (component instanceof ScriptData.ScriptLabel label)
            {
//                        if (scriptData.getScripts().contains(label))
//                        {
//                            builder.append("script(").append(scriptData.getScripts().indexOf(label) + 1).append(") ");
//                            builder.append(label.getName()).append(":\n");
//                        }
//                        else
//                        {
//                            builder.append(label).append("\n");
//                        }

                if (getScripts().contains(label))
                {
                    builder.append("script(").append(scripts.indexOf(label)+1).append(") ");
                }
                builder.append(label.getName()).append(":\n");
            }
            else if (component instanceof ScriptData.ScriptCommand scriptCommand)
            {
                builder.append("    ").append(scriptCommand.getName());
                String[] parameters = scriptCommand.getParameterStrings();

                for (String parameter : parameters) {
                    builder.append(" ").append(parameter);
                }

                if (scriptCommand.getName().equalsIgnoreCase("end") || scriptCommand.getName().equalsIgnoreCase("goto") || scriptCommand.getName().equalsIgnoreCase("Jump") || scriptCommand.getName().equalsIgnoreCase("Return"))
                {
                    builder.append("\n");
                }
                builder.append("\n");
            }
            else if (component instanceof ScriptData.ActionLabel actionLabel)
            {
                builder.append(actionLabel.getName()).append(":\n");
            }
            else if (component instanceof ScriptData.ActionCommand actionCommand)
            {
                builder.append("    ").append(actionCommand);
                if (actionCommand.getName().equalsIgnoreCase("End"))
                {
                    builder.append("\n");
                }
                builder.append("\n");
            }
        }

        return builder.toString().strip() + "\n";
    }

    private static final CommandMacro.ParameterStringReplacementFunction defaultReplaceParameterStringWithIntegerFunction = (commandID, parameterString) ->
    {
        if (isDoIfCommand.test(commandID))
        {
            Integer result = comparators.get(parameterString.toUpperCase());
            if (result != null)
                return result;

            if (parameterString.startsWith("label_"))
            {
                return parameterString;
            }
        }

        if (isMovementCommand.test(commandID))
        {
            if (parameterString.startsWith("Overworld."))
            {
                parameterString = parameterString.replace("Overworld.", "");
                int overworldNum;
                try {
                    overworldNum = Integer.parseInt(parameterString);
                } catch(NumberFormatException exception) {
                    return overworldNames.get(parameterString);
                }
                return overworldNum;
            }
            else if (parameterString.startsWith("action_"))
            {
                return parameterString;
            }
        }

        if (isCallCommand.test(commandID))
        {
            return parameterString;
        }

        return null;
    };

    public static class ScriptCommand implements ScriptComponent {
        String name;
        Object[] parameters;

        private CommandMacro commandMacro;

        public ScriptCommand(CommandMacro commandMacro)
        {
            this.commandMacro = commandMacro;
            this.name = commandMacro.getName();
        }

        @Override
        public String toString()
        {
            if (parameters == null)
                return name;
            StringBuilder builder = new StringBuilder(name).append(" [");
            for (int i = 0; i < parameters.length; i++)
            {
                if (parameters[i] instanceof Integer val)
                {
                    if (val >= 0x4000)
                    {
                        builder.append("0x").append(Integer.toHexString((int) parameters[i]));
                    }
                    else
                    {
                        builder.append(parameters[i]);
                    }
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

//        public String contextualToString(List<Integer> offsets)
//        {
//            if (!isCallCommand.test(commandMacro.getId())) {
//                return toString();
//            }
//
//            StringBuilder builder = new StringBuilder(name).append(" [");
//            for (int i = 0; i < parameters.length; i++)
//            {
//                String paramName = commandMacro.getParameters()[i];
//                if ((int) parameters[i] >= 0x4000)
//                {
//                    builder.append("0x").append(Integer.toHexString((int) parameters[i]));
//                }
//                else if (paramName.contains("dest") || paramName.contains("sub"))
//                {
//                    builder.append("label_").append(Integer.toHexString((int) parameters[i]));
//                }
//                else
//                {
//                    builder.append(parameters[i]);
//                }
//
//                if (i != parameters.length - 1)
//                {
//                    builder.append(", ");
//                }
//            }
//
//            return builder.append("]").toString();
//        }

        @Override
        public String getName()
        {
            return name;
        }

        public String[] getParameterStrings()
        {
            if (parameters == null)
                return new String[] {};

            String[] parameterStrings = new String[parameters.length];

//            StringBuilder builder = new StringBuilder(name).append(" [");
            for (int i = 0; i < parameters.length; i++)
            {
                if (parameters[i] instanceof Integer val)
                {
                    if (val >= 0x4000)
                    {
                        parameterStrings[i] = "0x" + Integer.toHexString((int) parameters[i]);
                    }
                    else
                    {
                        parameterStrings[i] = String.valueOf(parameters[i]);
                    }
                }
                else
                {
                    parameterStrings[i] = String.valueOf(parameters[i]);
                }
            }

            return parameterStrings;
        }

        public void setParameters(Object[] newParameters)
        {
            parameters = newParameters;
        }

        @Override
        public boolean equals(Object o)
        {
            if(this == o) {
                return true;
            }
            if(o == null || getClass() != o.getClass()) {
                return false;
            }
            ScriptCommand command = (ScriptCommand) o;
            return Objects.equals(name, command.name) && Arrays.equals(parameters, command.parameters) && Objects.equals(commandMacro, command.commandMacro);
        }

        @Override
        public int hashCode()
        {
            int result = Objects.hash(name, commandMacro);
            result = 31 * result + Arrays.hashCode(parameters);
            return result;
        }
    }

    public static class ActionLabel implements ScriptComponent
    {
        private String name;

        public ActionLabel(String name)
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

    public static class ActionCommand implements ScriptComponent
    {
        private String name;
        private int id;
        private int parameter;

        public ActionCommand(String name, int id, int parameter)
        {
            this.name = name;
            this.id = id;
            this.parameter = parameter;
        }

        public ActionCommand(int id, int parameter)
        {
            this.name = String.valueOf(id);
            this.parameter = parameter;
        }

        @Override
        public String toString()
        {
            return "Action " + name + " " + parameter;
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public boolean equals(Object o)
        {
            if(this == o) {
                return true;
            }
            if(o == null || getClass() != o.getClass()) {
                return false;
            }
            ActionCommand that = (ActionCommand) o;
            return id == that.id && parameter == that.parameter && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(name, id, parameter);
        }
    }
}
