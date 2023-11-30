package io.github.turtleisaac.pokeditor.formats.scripts;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;

import java.util.ArrayList;
import java.util.List;

import static io.github.turtleisaac.pokeditor.formats.scripts.ScriptParser.SCRIPT_MAGIC_ID;

public abstract class GenericScriptData extends ArrayList<ScriptData.ScriptComponent> implements GenericFileData
{
    public GenericScriptData(BytesDataContainer files)
    {
        setData(files);
    }

    boolean isLevelScript(MemBuf.MemBufReader reader, List<Integer> globalScriptOffsets) {
        return fileIsLevelScriptFile(reader, globalScriptOffsets);
    }

    protected static boolean fileIsLevelScriptFile(MemBuf.MemBufReader reader, List<Integer> globalScriptOffsets)
    {
        // Is Level Script as long as magic number FD13 doesn't exist
        while (true)
        {
            int checker = reader.readUInt16();
            reader.setPosition(reader.getPosition()-2);
            long value = reader.readUInt32();

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
    }
}
