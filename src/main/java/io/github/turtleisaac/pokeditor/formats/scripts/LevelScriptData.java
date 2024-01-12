package io.github.turtleisaac.pokeditor.formats.scripts;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

// this class only exists because AdAstra is awesome and wrote the only tool (until now) which could work with them

/**
 * Represents a Level Script file in Pokemon DPPtHGSS
 */
public class LevelScriptData extends GenericScriptData
{
    private boolean hasPadding = true;

    public LevelScriptData()
    {
        super();
    }

    public LevelScriptData(BytesDataContainer files)
    {
        super(files);
    }

    @Override
    public void setData(BytesDataContainer files)
    {
        if (!files.containsKey(GameFiles.FIELD_SCRIPTS))
        {
            throw new RuntimeException("Script file not provided to editor");
        }

        MemBuf dataBuf = MemBuf.create(files.get(GameFiles.FIELD_SCRIPTS, null));
        MemBuf.MemBufReader reader = dataBuf.reader();

        ArrayList<Integer> temp = new ArrayList<>();

        boolean isLevelScript = true;
        try {
            isLevelScript = isLevelScript(dataBuf, temp);
        }
        catch (IllegalStateException e)
        {
            // in theory should only happen if the file is not a level script?
            // Now this may appear in a few level scripts that don't have a 4-byte aligned "00 00 00 00"
//            throw new RuntimeException(e);
        }

        if (!isLevelScript) {
            throw new IllegalStateException("This is a normal script file, not a level script file");
        }

        reader.setPosition(0);

        int scriptType;
        boolean hasConditionalStructure = false;
        int conditionalStructureOffset = -1;

        while ((scriptType = reader.readUInt8()) >= VARIABLE_VALUE && scriptType <= LOAD_GAME)
        {
            long scriptToTrigger;

            if (hasConditionalStructure)
                conditionalStructureOffset--;

            if (scriptType != VARIABLE_VALUE) {
                scriptToTrigger = reader.readUInt32();
                if (hasConditionalStructure)
                    conditionalStructureOffset -= 4;
                add(new MapScreenLoadTrigger(scriptType, (int) scriptToTrigger));
            } else {
                hasConditionalStructure = true;
                conditionalStructureOffset = (int) reader.readUInt32();
            }
        }

        if (reader.getPosition() == 1) {
            if (reader.readUInt16() == 0 && dataBuf.writer().getPosition() < SMALLEST_TRIGGER_SIZE) {
                //todo come back here
//                LSTrigger.customInfo("This level script does nothing.", "Interesting...");
                return;
            }
        }

        if (reader.getPosition() < SMALLEST_TRIGGER_SIZE) {
            //todo come back here
            throw new RuntimeException("Parser failure: The input file you attempted to load is either malformed or not a Level Script file. ");
        }

        if (hasConditionalStructure) {
            if (conditionalStructureOffset != 1) {
                //todo come back here
                throw new RuntimeException("Field error: The Level Script file you attempted to load is broken. " + conditionalStructureOffset);
            } else {
                int variableID;
                while ((variableID = reader.readUInt16()) > 0) {
                    int varExpectedValue = reader.readUInt16();
                    int scriptToTrigger = reader.readUInt16();
                    add(new VariableValueTrigger(scriptToTrigger, variableID, varExpectedValue));
                }
            }
        }
    }

    @Override
    public BytesDataContainer save()
    {
        MemBuf dataBuf = MemBuf.create();
        MemBuf.MemBufWriter writer = dataBuf.writer();

        TreeSet<MapScreenLoadTrigger> tsMapScreenLoad = new TreeSet<>();
        TreeSet<VariableValueTrigger> tsVariable = new TreeSet<>();

        if (!isEmpty())
        {
            for (ScriptComponent component : this) {
                if (!(component instanceof LevelScriptTrigger lst))
                    throw new RuntimeException("Illegal component in level script: " + component);

                if (lst.getTriggerType() == VARIABLE_VALUE) {
                    tsVariable.add((VariableValueTrigger) lst);
                } else {
                    tsMapScreenLoad.add((MapScreenLoadTrigger) lst);
                }
            }

            for (LevelScriptTrigger lstm : tsMapScreenLoad) {
                writer.writeByte((byte) lstm.getTriggerType());
                writer.writeUInt32((byte) lstm.getScriptTriggered());
            }

            if (!tsVariable.isEmpty()) {
                writer.writeByte((byte) VARIABLE_VALUE);
                writer.writeUInt32(1);
                writer.writeByte((byte) 0);
                for (VariableValueTrigger lstv : tsVariable) {
                    writer.writeShort((short) lstv.getVariableToWatch());
                    writer.writeShort((short) lstv.getExpectedValue());
                    writer.writeShort((short) lstv.getScriptTriggered());
                }
            }

            writer.writeShort((short) 0);

            if (hasPadding) {
                int missingBytes = writer.getPosition() % 4;
                if (missingBytes != 0) {
                    writer.align(4);
                }

                dataBuf.reader().setPosition(writer.getPosition() - 4);
                int last = dataBuf.reader().readInt();
                if (last != 0)
                    writer.skip(4);
                dataBuf.reader().setPosition(0);
            }
        }
        else
        {
            writer.skip(4);
        }



        return new BytesDataContainer(GameFiles.FIELD_SCRIPTS, null, dataBuf.reader().getBuffer());
    }

    public boolean isHasPadding()
    {
        return hasPadding;
    }

    public void setHasPadding(boolean hasPadding)
    {
        this.hasPadding = hasPadding;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (ScriptComponent component : this)
        {
            sb.append(component).append("\n");
        }
        return sb.toString().trim();
    }

    public static final int VARIABLE_VALUE = 1;
    public static final int MAP_CHANGE = 2;
    public static final int SCREEN_RESET = 3;
    public static final int LOAD_GAME = 4;

    private static final int SMALLEST_TRIGGER_SIZE = 5;

    public static abstract class LevelScriptTrigger implements ScriptComponent
    {
        private int triggerType;
        private int scriptTriggered;

        public LevelScriptTrigger(int triggerType, int scriptTriggered)
        {
            this.triggerType = triggerType;
            this.scriptTriggered = scriptTriggered;
        }

//        public static void customAlert(String contentText) {
//            JOptionPane.showMessageDialog(null, contentText, "Something went wrong", JOptionPane.WARNING_MESSAGE);
//        }
//
//        public static void customInfo(String contentText, String headerText) {
//            JOptionPane.showMessageDialog(null, contentText, headerText, JOptionPane.INFORMATION_MESSAGE);
//        }

        @Override
        public String getName()
        {
            return null;
        }

        public int getTriggerType()
        {
            return triggerType;
        }

        public void setTriggerType(int triggerType)
        {
            this.triggerType = triggerType;
        }

        public int getScriptTriggered()
        {
            return scriptTriggered;
        }

        public void setScriptTriggered(int scriptTriggered)
        {
            this.scriptTriggered = scriptTriggered;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LevelScriptTrigger lst)) return false;
            return this.triggerType == lst.getTriggerType() && this.scriptTriggered == lst.getScriptTriggered();
        }

        @Override
        public int hashCode() {
            return Objects.hash(triggerType, scriptTriggered);
        }

        @Override
        public String toString() {
            return "Starts Script " + getScriptTriggered();
        }
    }

    public static class MapScreenLoadTrigger extends LevelScriptTrigger implements Comparable<MapScreenLoadTrigger>
    {
        public MapScreenLoadTrigger(int triggerType, int scriptTriggered)
        {
            super(triggerType, scriptTriggered);
        }

        @Override
        public String toString()
        {
            String message = super.toString();
            switch (getTriggerType()) {
                case MAP_CHANGE -> message += " upon entering the associated header.";
                case SCREEN_RESET -> message += " when a fadescreen happens in the associated header.";
                case LOAD_GAME -> message += " when the game resumes in the associated header.";
            }
            return message;
        }

        @Override
        public int compareTo(MapScreenLoadTrigger other) {
            int i;

            i = -Integer.compare(this.getTriggerType(), other.getTriggerType());
            if (i != 0) return i;

            i = Integer.compare(this.getScriptTriggered(), other.getScriptTriggered());
            return i;
        }
    }

    public static class VariableValueTrigger extends LevelScriptTrigger implements Comparable<VariableValueTrigger>
    {
        private int variableToWatch;
        private int expectedValue;

        public VariableValueTrigger(int scriptTriggered, int variableToWatch, int expectedValue)
        {
            super(VARIABLE_VALUE, scriptTriggered);
            this.variableToWatch = variableToWatch;
            this.expectedValue = expectedValue;
        }

        public int getVariableToWatch()
        {
            return variableToWatch;
        }

        public void setVariableToWatch(int variableToWatch)
        {
            this.variableToWatch = variableToWatch;
        }

        public int getExpectedValue()
        {
            return expectedValue;
        }

        public void setExpectedValue(int expectedValue)
        {
            this.expectedValue = expectedValue;
        }

        @Override
        public String toString() {
            return super.toString() + " when Var 0x" + Integer.toHexString(variableToWatch).toUpperCase() + " == " + expectedValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VariableValueTrigger that)) return false;
            return this.getTriggerType() == that.getTriggerType()
                    && this.getScriptTriggered() == that.getScriptTriggered()
                    && this.variableToWatch == that.variableToWatch
                    && this.expectedValue == that.expectedValue;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.getTriggerType(), this.getScriptTriggered(), variableToWatch, expectedValue);
        }

        @Override
        public int compareTo(VariableValueTrigger other) {
            int i;

            i = -Integer.compare(this.getTriggerType(), other.getTriggerType());
            if (i != 0) return i;

            i = Integer.compare(this.getScriptTriggered(), other.getScriptTriggered());
            if (i != 0) return i;

            i = -Integer.compare(this.variableToWatch, other.variableToWatch);
            if (i != 0) return i;

            i = Integer.compare(this.expectedValue, other.expectedValue);
            return i;
        }
    }


}
