package io.github.turtleisaac.pokeditor.formats;

import io.github.turtleisaac.pokeditor.gamedata.GameFiles;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BytesDataContainer extends HashMap<GameFiles, Map<BytesDataContainer.PatternIndex, byte[]>>
{
    public BytesDataContainer() {

    }

    public BytesDataContainer(GameFiles file, PatternIndex pattern, byte[] data)
    {
        super();
        insert(file, pattern, data);
    }

    public void insert(GameFiles file, PatternIndex pattern, byte[] data)
    {
        computeIfAbsent(file, gameFiles -> new HashMap<>());
        get(file).put(Objects.requireNonNullElse(pattern, Default.NO_GROUPING), data);
    }

    public byte[] get(GameFiles file, PatternIndex pattern)
    {
        if (containsKey(file)) {
            return get(file).getOrDefault(Objects.requireNonNullElse(pattern, Default.NO_GROUPING), null);
        }
        return null;
    }

    public boolean containsKey(GameFiles key)
    {
        return super.containsKey(key);
    }

    public boolean containsPatternKey(GameFiles file, PatternIndex key)
    {
        return get(file).containsKey(key);
    }

    public interface PatternIndex {
        int getIndex();
    }

    enum Default implements PatternIndex
    {
        NO_GROUPING;

        @Override
        public int getIndex()
        {
            return 0;
        }
    }
}
