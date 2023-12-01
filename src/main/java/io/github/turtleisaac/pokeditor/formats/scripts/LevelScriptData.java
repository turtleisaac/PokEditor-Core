package io.github.turtleisaac.pokeditor.formats.scripts;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;

import java.util.ArrayList;
import java.util.List;

public class LevelScriptData extends GenericScriptData
{
    public LevelScriptData(BytesDataContainer files)
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

        ArrayList<Integer> temp = new ArrayList<>();

        boolean isLevelScript = true;
        try {
            isLevelScript = isLevelScript(reader, temp);
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
    }

    @Override
    public BytesDataContainer save()
    {
        MemBuf dataBuf = MemBuf.create();
        MemBuf.MemBufWriter writer = dataBuf.writer();

        return new BytesDataContainer(GameFiles.SCRIPTS, null, dataBuf.reader().getBuffer());
    }
}
