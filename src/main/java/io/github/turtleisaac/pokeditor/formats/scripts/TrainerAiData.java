package io.github.turtleisaac.pokeditor.formats.scripts;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.formats.scripts.antlr4.CommandMacro;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;

import java.util.*;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TrainerAiData extends GenericScriptData
{
	public enum move_power_rating
	{
		NO_POWER(0), // Move doesn't use power.
		NOT_MAX_POWER(1), // Move doesn't have the highest power.
		MAX_POWER(2); // Move has the highest power.

		int value;

		move_power_rating(int value)
		{
			this.value = value;
		}
	}

	public enum fastest_pokemon
	{
		ATTACKER_FIRST(0), // AI is first.
		DEFENDER_FIRST(1), // Opponent is first.
		TIED_FIRST(2); // Speed tied.

		int value;

		fastest_pokemon(int value)
		{
			this.value = value;
		}
	}

	public enum move_effectiveness
	{
		SUPER_EFFECTIVE_4X(160),
		SUPER_EFFECTIVE_2X(80),
		SUPER_EFFECTIVE_15X(60),
		NORMAL_EFFECTIVE(40),
		NOT_EFFECTIVE_05X(20),
		NOT_EFFECTIVE_025X(10),
		IMMUNE(0);

		int value;

		move_effectiveness(int value)
		{
			this.value = value;
		}
	}

	public enum attack_stat_comparison
	{
		attacker_advantage(0), // Attack > Defense
		defender_advantage(1), // Defense > Attack
		no_advantage(2); // Attack == Defense

		int value;

		attack_stat_comparison(int value)
		{
			this.value = value;
		}
	}

	public enum has_move_or_ability
	{
		NO(0),
		YES(1),
		UNKNOWN(2);

		int value;

		has_move_or_ability(int value)
		{
			this.value = value;
		}
	}

	public enum pokemon_stat
	{
		HP(0),
		ATTACK(1),
		DEFENSE(2),
		SPEED(3),
		SPECIAL_ATTACK(4),
		SPECIAL_DEFENSE(5),
		ACCURACY(6),
		EVASION(7);

		int value;

		pokemon_stat(int value)
		{
			this.value = value;
		}

		// Super simple proof of concept.
		static String getFromInt(int value)
		{
			return Arrays.stream(pokemon_stat.values())
					.filter(side -> side.value == value)
					.findFirst()
					.map(side -> side.name())
					.orElse("INVALID(" + value + ")");
		}
	}

	public enum lookup_type
	{
		defender_type1(0),
		attacker_type1(1),
		defender_type2(2),
		attacker_type2(3),
		move(4),
		defender_ally_type1(0),
		attacker_ally_type1(1),
		defender_ally_type2(2),
		attacker_ally_type2(3);

		int value;

		lookup_type(int value)
		{
			this.value = value;
		}
	}

	// Attacker seems to be the current AI.
	public enum pokemon_side
	{
		DEFENDER(0),
		ATTACKER(1),
		DEFENDER_ALLY(2),
		ATTACKER_ALLY(3);

		int value;

		pokemon_side(int value)
		{
			this.value = value;
		}

		// Super simple proof of concept.
		static String getFromInt(int value)
		{
			return Arrays.stream(pokemon_side.values())
					.filter(side -> side.value == value)
					.findFirst()
					.map(side -> side.name())
					.orElse("INVALID(" + value + ")");
		}
	}

	public enum pokemon_state
	{
		BIND(0), // Guess based on dictionary definition of かなしばり.
		ENCORE(1);

		int value;

		pokemon_state(int value)
		{
			this.value = value;
		}
	}

	public enum weather
	{
		NONE(0),
		UNKNOWN1(1),
		Rain(2),
		SANDSTORM(3),
		HAIL(4),
		FOG(5);

		int value;

		weather(int value)
		{
			this.value = value;
		}
	}

    private ArrayList<ScriptLabel> scripts;
    private ArrayList<ScriptLabel> labels;
    private ArrayList<ActionLabel> actions;

    public TrainerAiData(BytesDataContainer files)
    {
        super(files);
    }

    @Override
    public void setData(BytesDataContainer files) {
		if (!files.containsKey(GameFiles.TR_AI)) {
			throw new RuntimeException("Script file not provided to editor");
		}

		scripts = new ArrayList<>();
		labels = new ArrayList<>();
		actions = new ArrayList<>();

		MemBuf dataBuf = MemBuf.create(files.get(GameFiles.TR_AI, null));
		MemBuf.MemBufReader reader = dataBuf.reader();
		System.out.println("Loading AI data with size " + reader.getBuffer().length);

		ArrayList<Integer> globalScriptOffsets = new ArrayList<>();

		for (int i = 0; i < 32; ++i)
			globalScriptOffsets.add((int) reader.readUInt32());

		ArrayList<Integer> labelOffsets = new ArrayList<>(globalScriptOffsets);
		ArrayList<Integer> actionOffsets = new ArrayList<>();
		ArrayList<Integer> visitedOffsets = new ArrayList<>();

		HashMap<Integer, ScriptLabel> labelMap = new HashMap<>();
		HashMap<Integer, ActionLabel> actionMap = new HashMap<>();

		int lastSize;
		do {
			lastSize = labelOffsets.size();
			for (int i = 0; i < labelOffsets.size(); i++) {
				if (!actionOffsets.contains(labelOffsets.get(i)))
					readAtOffset(dataBuf, globalScriptOffsets, labelOffsets, actionOffsets, visitedOffsets, labelOffsets.get(i), labelMap, false);
			}
		}
		while (lastSize != labelOffsets.size());

		labelOffsets.sort(Comparator.naturalOrder());
		for (int i = 0; i < labelOffsets.size(); i++) {
			if (!actionOffsets.contains(labelOffsets.get(i)))
				readAtOffset(dataBuf, globalScriptOffsets, labelOffsets, actionOffsets, visitedOffsets, labelOffsets.get(i), labelMap, true);
			else
				readActionAtOffset(dataBuf, actionOffsets, visitedOffsets, actionMap, labelOffsets.get(i));
		}

		this.stream()
				.filter(component -> component instanceof ScriptCommand)
				.map(component -> (ScriptCommand)component)
				.flatMap(scriptCommand -> Arrays.stream(scriptCommand.parameters))
				.filter(namedParameter -> namedParameter.name.equalsIgnoreCase("address"))
				.forEach(namedParameter -> namedParameter.value = "label_" + labels.indexOf(labelMap.get((Integer) namedParameter.value/4)) + " (0x" + Integer.toHexString((int)namedParameter.value) + ")");
	}

    private void readAtOffset(MemBuf dataBuf, ArrayList<Integer> globalScriptOffsets, ArrayList<Integer> labelOffsets, ArrayList<Integer> actionOffsets, ArrayList<Integer> visitedOffsets, int offset, HashMap<Integer, ScriptLabel> labelMap, boolean finalRun)
    {
        MemBuf.MemBufReader reader = dataBuf.reader();
        if (visitedOffsets.contains(offset)) {
            return;
        }

		System.err.println("Reading offset " + offset);
		if (offset < 0 || offset > dataBuf.writer().getPosition())
			return;
        reader.setPosition(offset);

        while (dataBuf.writer().getPosition() - reader.getPosition() >= 4)
        {
            if (finalRun && !visitedOffsets.contains(reader.getPosition()))
            {
                visitedOffsets.add(reader.getPosition());
                if (globalScriptOffsets.contains(reader.getPosition())) {
                    ScriptLabel scriptLabel = new ScriptLabel("label_" + Integer.toHexString(reader.getPosition()));
                    scripts.add(scriptLabel);
                    labels.add(scriptLabel);
                    scriptLabel.name = "label_" + labels.indexOf(scriptLabel) + " (0x" + Integer.toHexString(reader.getPosition()) + ")";
                    labelMap.put(reader.getPosition(), scriptLabel);
                    add(scriptLabel);
                } else if (labelOffsets.contains(reader.getPosition())) {
                    ScriptLabel label = new ScriptLabel("label_" + Integer.toHexString(reader.getPosition()));
                    labels.add(label);
                    labelMap.put(reader.getPosition(), label);
                    label.name = "label_" + labels.indexOf(label) + " (0x" + Integer.toHexString(reader.getPosition()) + ")";
                    add(label);
                } else if (actionOffsets.contains(reader.getPosition())) {
                    ActionLabel actionLabel = new ActionLabel("action_" + Integer.toHexString(reader.getPosition()));
                    actions.add(actionLabel);
                    actionLabel.name = "action_" + actions.indexOf(actionLabel) + " (0x" + Integer.toHexString(reader.getPosition()) + ")";
                    add(actionLabel);
                }
            }

            long commandID = reader.readUInt32();
            if (commandID > 0xff)
                continue;

            CommandMacro commandMacro = TrainerAiParser.nativeCommands.get((int)commandID);
            if (commandMacro == null) {
                System.err.println("Invalid command 0x" + Integer.toHexString((int)commandID) + " at offset " + reader.getPosition());
				continue;
            }

            ScriptCommand command = new ScriptCommand(commandMacro);
            command.name = commandMacro.getName();

            command.setParameters(commandMacro.readParameters(reader));

            if (commandMacro.getParameters().length > 0 && commandMacro.getParameters()[commandMacro.getParameters().length-1].equalsIgnoreCase("address")) {
                int offsetParam = (int) command.parameters[command.parameters.length-1].value;
				if (offsetParam >= 65536)
					System.err.println("Unusually large offset paremeter " + offsetParam + " at 0x" + Integer.toHexString(reader.getPosition()) + " command " + command.name);
                if (!labelOffsets.contains(offsetParam))
                    labelOffsets.add(offsetParam);
            }

            if (finalRun)
                add(command);

            if (command.name.equalsIgnoreCase("EndAi") || commandID == 0x4D)
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

            long commandID = reader.readUInt32();
            long parameter = reader.readUInt32();

			add(new ActionCommand((int)commandID, (int)parameter));
        }
    }

    @Override
    public BytesDataContainer save()
    {
        MemBuf dataBuf = MemBuf.create();
        MemBuf.MemBufWriter writer = dataBuf.writer();

        return new BytesDataContainer(GameFiles.TR_AI, null, dataBuf.reader().getBuffer());
    }

    public ArrayList<ScriptLabel> getScripts()
    {
        return scripts;
    }

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
            if (component instanceof TrainerAiData.ScriptLabel label)
            {
                if (getScripts().contains(label))
                {
                    builder.append("script(").append(getScripts().indexOf(label) + 1).append(") ");
                }
                builder.append(label.getName()).append(":\n");
            }
            else if (component instanceof TrainerAiData.ScriptCommand scriptCommand)
            {
                builder.append("    ").append(scriptCommand.getName());
                Stream<String> parameters = scriptCommand.getParameterStrings();

				parameters.forEach(parameter -> builder.append(" ").append(parameter));

                if (scriptCommand.getName().equalsIgnoreCase("end") || scriptCommand.getName().equalsIgnoreCase("goto") || scriptCommand.getName().equalsIgnoreCase("Jump") || scriptCommand.getName().equalsIgnoreCase("Return"))
                {
                    builder.append("\n");
                }
                builder.append("\n");
            }
            else if (component instanceof TrainerAiData.ActionLabel actionLabel)
            {
                builder.append(actionLabel.getName()).append(":\n");
            }
            else if (component instanceof TrainerAiData.ActionCommand actionCommand)
            {
                builder.append("    ").append(actionCommand);
                if (actionCommand.getName().equalsIgnoreCase("End"))
                {
                    builder.append("\n");
                }
                builder.append("\n");
            }
			else
				System.err.println("Invalid Component Type");
        }

        return builder.toString().strip() + "\n";
    }

    public static class ScriptLabel implements ScriptComponent {
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

    public static class ScriptCommand implements ScriptComponent {
        String name;
		NamedParameter[] parameters;

        private CommandMacro commandMacro;

		private class NamedParameter
		{
			String name;
			Object value;
		}

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
            return getParameterStrings().collect(Collectors.joining(", ", " [", "]"));
        }

        @Override
        public String getName()
        {
            return name;
        }

        public Stream<String> getParameterStrings()
        {
            if (parameters == null)
                return Stream.of();

			return Arrays.stream(parameters).filter(Objects::nonNull).map(parameter -> {
				if (parameter.value instanceof Integer val)
				{
					if (parameter.name.equals("side"))
						return pokemon_side.getFromInt(val);
					if (parameter.name.equals("stat"))
						return pokemon_stat.getFromInt(val);
					if (val >= 0x4000)
						return "0x" + Integer.toHexString(val);
					else
						return String.valueOf(val);
				}
				else
					return String.valueOf(parameter.value);
			});
        }

        public void setParameters(Object[] newParameters)
        {
			int len = commandMacro.getParameters().length;
			this.parameters = new NamedParameter[len];
            for (int i = 0; i < len && i < newParameters.length; ++i)
			{
				this.parameters[i] = new NamedParameter();
				this.parameters[i].name = commandMacro.getParameters()[i];
				this.parameters[i].value = newParameters[i];
			}
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
        private int parameter;

        public ActionCommand(String name, int parameter)
        {
            this.name = name;
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
