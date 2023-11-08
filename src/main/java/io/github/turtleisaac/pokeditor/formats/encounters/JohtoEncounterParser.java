package io.github.turtleisaac.pokeditor.formats.encounters;

import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;

import java.util.Map;

public class JohtoEncounterParser extends GenericEncounterParser<JohtoEncounterData>
{
    @Override
    JohtoEncounterData createEncounterData(BytesDataContainer files)
    {
        return new JohtoEncounterData(files);
    }
}