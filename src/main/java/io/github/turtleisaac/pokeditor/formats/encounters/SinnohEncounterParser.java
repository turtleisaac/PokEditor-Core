package io.github.turtleisaac.pokeditor.formats.encounters;

import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;

import java.util.Map;

public class SinnohEncounterParser extends GenericEncounterParser<SinnohEncounterData>
{
    @Override
    SinnohEncounterData createEncounterData(BytesDataContainer files)
    {
        return new SinnohEncounterData(files);
    }
}
