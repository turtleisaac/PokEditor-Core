/*
 * Copyright (c) 2023 Turtleisaac.
 *
 * This file is part of PokEditor.
 *
 * PokEditor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PokEditor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PokEditor. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.turtleisaac.pokeditor.formats.encounters;

import io.github.turtleisaac.nds4j.Fnt;
import io.github.turtleisaac.nds4j.Narc;
import io.github.turtleisaac.nds4j.framework.Endianness;
import io.github.turtleisaac.pokeditor.formats.GameFiles;
import io.github.turtleisaac.pokeditor.formats.GenericParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class GenericEncounterParser<E extends GenericEncounterData> implements GenericParser<E>
{
    abstract E createEncounterData(Map<GameFiles, byte[]> files);

    @Override
    public List<E> generateDataList(Map<GameFiles, Narc> narcs)
    {
        if (!narcs.containsKey(GameFiles.ENCOUNTERS))
        {
            throw new RuntimeException("Encounters narc not provided to editor");
        }

        Narc encounters = narcs.get(GameFiles.ENCOUNTERS);
        ArrayList<E> data = new ArrayList<>();

        for (byte[] subfile : encounters.getFiles())
        {
            data.add(createEncounterData(Collections.singletonMap(GameFiles.ENCOUNTERS, subfile)));
        }

        return data;
    }

    @Override
    public Map<GameFiles, Narc> processDataList(List<E> data)
    {
        ArrayList<byte[]> subfiles = new ArrayList<>();
        for (GenericEncounterData encounters : data)
        {
            subfiles.add(encounters.save().get(GameFiles.ENCOUNTERS));
        }

        return Collections.singletonMap(GameFiles.ENCOUNTERS, Narc.fromContentsAndNames(subfiles, new Fnt.Folder(), Endianness.EndiannessType.BIG));
    }

    @Override
    public List<GameFiles> getRequirements()
    {
        return Collections.singletonList(GameFiles.ENCOUNTERS);
    }
}
