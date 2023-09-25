package io.github.turtleisaac.pokeditor.formats.encounters;

import io.github.turtleisaac.pokeditor.formats.GameFiles;

import java.util.Map;

public class JohtoEncounterData extends GenericEncounterData
{
    public JohtoEncounterData(Map<GameFiles, byte[]> files)
    {
        super(files);
    }

    @Override
    public void setData(Map<GameFiles, byte[]> files)
    {

    }

    @Override
    public Map<GameFiles, byte[]> save()
    {
        return null;
    }
}
