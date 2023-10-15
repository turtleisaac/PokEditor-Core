package io.github.turtleisaac.pokeditor.formats.encounters;

import io.github.turtleisaac.pokeditor.gamedata.GameFiles;

import java.util.Map;

public class JohtoEncounterParser extends GenericEncounterParser<JohtoEncounterData>
{
    @Override
    JohtoEncounterData createEncounterData(Map<GameFiles, byte[]> files)
    {
        return new JohtoEncounterData(files);
    }
}