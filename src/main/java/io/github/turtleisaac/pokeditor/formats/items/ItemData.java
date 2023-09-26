package io.github.turtleisaac.pokeditor.formats.items;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.GameFiles;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;

import java.util.Collections;
import java.util.Map;

public class ItemData implements GenericFileData
{
    public ItemData(Map<GameFiles, byte[]> files)
    {
        setData(files);
    }

    @Override
    public void setData(Map<GameFiles, byte[]> files)
    {
        if (!files.containsKey(GameFiles.ITEMS))
        {
            throw new RuntimeException("Items file not provided to editor");
        }

        MemBuf dataBuf = MemBuf.create(files.get(GameFiles.ITEMS));
        MemBuf.MemBufReader reader = dataBuf.reader();


    }

    @Override
    public Map<GameFiles, byte[]> save()
    {
        MemBuf dataBuf = MemBuf.create();
        MemBuf.MemBufWriter writer = dataBuf.writer();

        return Collections.singletonMap(GameFiles.ITEMS, dataBuf.reader().getBuffer());
    }
}
