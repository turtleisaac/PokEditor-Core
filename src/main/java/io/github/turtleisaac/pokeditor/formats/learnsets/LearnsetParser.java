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

package io.github.turtleisaac.pokeditor.formats.learnsets;

import io.github.turtleisaac.nds4j.Fnt;
import io.github.turtleisaac.nds4j.Narc;
import io.github.turtleisaac.nds4j.framework.Endianness;
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
import io.github.turtleisaac.pokeditor.formats.GenericParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LearnsetParser implements GenericParser<LearnsetData>
{

    @Override
    public List<LearnsetData> generateDataList(Map<GameFiles, Narc> narcs)
    {
        if (!narcs.containsKey(GameFiles.LEVEL_UP_LEARNSETS))
        {
            throw new RuntimeException("Level-up learnsets narc not provided to editor");
        }

        Narc learnsets = narcs.get(GameFiles.LEVEL_UP_LEARNSETS);
        ArrayList<LearnsetData> data = new ArrayList<>();

        for (byte[] subfile : learnsets.getFiles())
        {
            data.add(new LearnsetData(new BytesDataContainer(GameFiles.LEVEL_UP_LEARNSETS, null, subfile)));
        }

        return data;
    }

    @Override
    public Map<GameFiles, Narc> processDataList(List<LearnsetData> data)
    {
        ArrayList<byte[]> subfiles = new ArrayList<>();
        for (LearnsetData learnset : data)
        {
            //todo figure out if this should be here realistically
//            learnset.sortLearnset();
            subfiles.add(learnset.save().get(GameFiles.LEVEL_UP_LEARNSETS, null));
        }

        return Collections.singletonMap(GameFiles.LEVEL_UP_LEARNSETS, Narc.fromContentsAndNames(subfiles, new Fnt.Folder(), Endianness.EndiannessType.BIG));
    }

    @Override
    public List<GameFiles> getRequirements()
    {
        return Collections.singletonList(GameFiles.LEVEL_UP_LEARNSETS);
    }
}
