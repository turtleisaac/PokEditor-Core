package io.github.turtleisaac.pokeditor.formats.scripts;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;

import java.util.ArrayList;
import java.util.List;

import static io.github.turtleisaac.pokeditor.formats.scripts.FieldScriptParser.SCRIPT_MAGIC_ID;

public abstract class GenericScriptData extends ArrayList<GenericScriptData.ScriptComponent> implements GenericFileData
{
    public GenericScriptData()
    {}

    public GenericScriptData(BytesDataContainer files)
    {
        setData(files);
    }

    boolean isLevelScript(MemBuf memBuf, List<Integer> globalScriptOffsets) {
        return fileIsLevelScriptFile(memBuf, globalScriptOffsets);
    }

    protected static boolean fileIsLevelScriptFile(MemBuf dataBuf, List<Integer> globalScriptOffsets)
    {
        MemBuf.MemBufReader reader = dataBuf.reader();
        // Is Level Script as long as magic number FD13 doesn't exist
        while (reader.getPosition() < dataBuf.writer().getPosition())
        {
            int checker = reader.readUInt16();
            reader.setPosition(reader.getPosition()-2);
            int value = reader.readInt();

            if (value == 0 && globalScriptOffsets.isEmpty()) { // yep this is a level script
                return true;
            } else if (checker == SCRIPT_MAGIC_ID) { // magic appeared so this is a normal script
//                reader.setPosition(reader.getPosition()-4);
                return false;
            } else {
                int offsetFromStart = (int)(value + reader.getPosition());  // Don't change order of addition
                globalScriptOffsets.add(offsetFromStart);
            }
        }

//        if (reader.getPosition() < dataBuf.writer().getPosition())
            return true;
    }

	public interface ScriptComponent {
		String getName();
	}

    public static class ScriptLabel implements ScriptComponent {
        protected String name;
        private int scriptID;

        public ScriptLabel(String name)
        {
            this.name = name;
            this.scriptID = -1;
        }

        @Override
        public String toString()
        {
            if (scriptID == -1)
                return name;
            else
                return String.format("script(%d) %s", scriptID, name);
        }

        @Override
        public String getName()
        {
            return name;
        }

        public int getScriptID()
        {
            return scriptID;
        }

        public void setScriptID(int scriptID)
        {
            this.scriptID = scriptID;
        }
    }
}
