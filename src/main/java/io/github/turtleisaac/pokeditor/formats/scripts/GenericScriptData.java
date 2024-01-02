package io.github.turtleisaac.pokeditor.formats.scripts;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;

import java.util.ArrayList;
import java.util.List;

import static io.github.turtleisaac.pokeditor.formats.scripts.ScriptParser.SCRIPT_MAGIC_ID;

public abstract class GenericScriptData extends ArrayList<ScriptData.ScriptComponent> implements GenericFileData
{
    private ArrayList<ScriptComponent> components = new ArrayList<>();

    public GenericScriptData()
    {

    }

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

//    public void add(ScriptComponent scriptComponent)
//    {
//        components.add(scriptComponent);
//    }
//
//    public void add(int index, ScriptComponent scriptComponent)
//    {
//        components.add(index, scriptComponent);
//    }
//
//    public ScriptComponent get(int index)
//    {
//        return components.get(index);
//    }
//
//    public ScriptComponent remove(int index)
//    {
//        return components.remove(index);
//    }
//
//    public int size()
//    {
//        return components.size();
//    }
//
//    public Iterable<ScriptComponent> iterable()
//    {
//        return components;
//    }
//
//    public boolean isEmpty()
//    {
//        return components.isEmpty();
//    }

    public interface ScriptComponent {
        String getName();
    }
}
