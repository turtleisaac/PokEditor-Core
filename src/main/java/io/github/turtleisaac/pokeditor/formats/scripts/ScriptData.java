package io.github.turtleisaac.pokeditor.formats.scripts;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.formats.scripts.antlr4.CommandMacro;
import io.github.turtleisaac.pokeditor.formats.scripts.antlr4.CommandWriter;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;

import java.util.*;
import java.util.function.IntPredicate;

import static io.github.turtleisaac.pokeditor.formats.scripts.ScriptParser.SCRIPT_MAGIC_ID;

public class ScriptData extends GenericScriptData
{
    private static final IntPredicate isCallCommand = commandID -> commandID >= 0x16 && commandID <= 0x1D && commandID != 0x1B;
    private static final IntPredicate isEndCommand = commandId -> commandId == 0x2 || commandId == 0x16 || commandId == 0x1B;
    private static final IntPredicate isDoIfCommand = commandID -> commandID == 28 || commandID == 29 || commandID == 225;
    private static final IntPredicate isMovementCommand = commandID -> commandID == 0x5E;
    private static final IntPredicate isEndMovementCommand = commandID -> commandID == 0xFE;
//    private static final IntPredicate isOverworldObjectCommand

    private static final Map<Integer, String> overworldNames = new HashMap<>();

    static {
        overworldNames.put(250, "Daycare1");
        overworldNames.put(251, "Daycare2");
        overworldNames.put(253, "Follower");
        overworldNames.put(255, "Player");
    }

    private ArrayList<ScriptLabel> scripts;
    private ArrayList<ScriptLabel> labels;
    private ArrayList<ActionLabel> actions;

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
        labels = new ArrayList<>();
        actions = new ArrayList<>();

        MemBuf dataBuf = MemBuf.create(files.get(GameFiles.FIELD_SCRIPTS, null));
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

        for (ScriptComponent component : this)
        {
            if (component instanceof ScriptCommand command)
            {
                if (command.parameters != null)
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

        while (reader.getPosition() < dataBuf.writer().getPosition())
        {
            if (finalRun && !visitedOffsets.contains(reader.getPosition()))
            {
                visitedOffsets.add(reader.getPosition());
                if (globalScriptOffsets.contains(reader.getPosition())) {
                    ScriptLabel scriptLabel = new ScriptLabel("label_" + Integer.toHexString(reader.getPosition()));
                    scripts.add(scriptLabel);
                    labels.add(scriptLabel);
                    scriptLabel.name = "label_" + labels.indexOf(scriptLabel);
                    labelMap.put(reader.getPosition(), scriptLabel);
//                    add(new ScriptLabel("script(" + globalScriptOffsets.indexOf(reader.getPosition()) + ") label_" + Integer.toHexString(reader.getPosition())));
                    add(scriptLabel);
                } else if (labelOffsets.contains(reader.getPosition())) {
                    ScriptLabel label = new ScriptLabel("label_" + Integer.toHexString(reader.getPosition()));
                    labels.add(label);
                    labelMap.put(reader.getPosition(), label);
                    label.name = "label_" + labels.indexOf(label);
                    add(label);
                } else if (actionOffsets.contains(reader.getPosition())) {
                    ActionLabel actionLabel = new ActionLabel("action_" + Integer.toHexString(reader.getPosition()));
                    actions.add(actionLabel);
                    actionLabel.name = "action_" + actions.indexOf(actionLabel);
                    add(actionLabel);
                }
            }

            int commandID = reader.readUInt16();
            if (commandID == 0)
                break;

//			System.err.println(commandID);
            CommandMacro commandMacro = ScriptParser.nativeCommands.get(commandID);
            if (commandMacro == null) {
                System.currentTimeMillis();
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
                command.parameters[0] = switch ((Integer) command.parameters[0])
                {
                    case 0 -> "LESS";
                    case 1 -> "EQUAL";
                    case 2 -> "GREATER";
                    case 3 -> "LESS_OR_EQUAL";
                    case 4 -> "GREATER_OR_EQUAL";
                    case 5 -> "DIFFERENT";
                    default -> throw new IllegalStateException("Unexpected value: " + (Integer) command.parameters[0]);
                };
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
                break;
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
                    actions.add(actionLabel);
                    actionMap.put(reader.getPosition(), actionLabel);
                    actionLabel.name = "action_" + actions.indexOf(actionLabel);
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
        MemBuf dataBuf = MemBuf.create();
        MemBuf.MemBufWriter writer = dataBuf.writer();

        // todo write header, then skip number of slots needed for all script offsets

        for (int i = 0; i < scripts.size(); i++)
        {
            writer.skip(4);
        }

        writer.writeShort((short) SCRIPT_MAGIC_ID);

        int scriptsStartPosition = writer.getPosition();

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
//            System.out.println(component);
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
                command.commandMacro.write(dataBuf, offsetObtainer, command.parameters);
            }
            else if (component instanceof ActionCommand actionCommand)
            {
                writer.writeShort((short) actionCommand.id);
                writer.writeShort((short) actionCommand.parameter);
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
                command.commandMacro.write(dataBuf, offsetObtainer, command.parameters);
            }
            else if (component instanceof ActionCommand actionCommand)
            {
                writer.writeShort((short) actionCommand.id);
                writer.writeShort((short) actionCommand.parameter);
            }
        }

        writer.skip(4 - writer.getPosition() % 4);

        return new BytesDataContainer(GameFiles.FIELD_SCRIPTS, null, dataBuf.reader().getBuffer());
    }

    public ArrayList<ScriptLabel> getScripts()
    {
        return scripts;
    }

//    public ArrayList<ScriptLabel> getLabels()
//    {
//        return labels;
//    }


    public void setScripts(ArrayList<ScriptLabel> scripts)
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
                    builder.append("script(").append(getScripts().indexOf(label) + 1).append(") ");
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
    }
}
