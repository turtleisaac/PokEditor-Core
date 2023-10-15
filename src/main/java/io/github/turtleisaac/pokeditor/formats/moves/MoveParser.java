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

package io.github.turtleisaac.pokeditor.formats.moves;

import io.github.turtleisaac.nds4j.Fnt;
import io.github.turtleisaac.nds4j.Narc;
import io.github.turtleisaac.nds4j.framework.Endianness;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
import io.github.turtleisaac.pokeditor.formats.GenericParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MoveParser implements GenericParser<MoveData>
{
    @Override
    public List<MoveData> generateDataList(Map<GameFiles, Narc> narcs)
    {
        if (!narcs.containsKey(GameFiles.MOVES))
        {
            throw new RuntimeException("Moves narc not provided to editor");
        }

        Narc moves = narcs.get(GameFiles.MOVES);
        ArrayList<MoveData> data = new ArrayList<>();

        for (byte[] subfile : moves.getFiles())
        {
            data.add(new MoveData(Collections.singletonMap(GameFiles.MOVES, subfile)));
        }

        return data;
    }

    @Override
    public Map<GameFiles, Narc> processDataList(List<MoveData> data)
    {
        ArrayList<byte[]> subfiles = new ArrayList<>();
        for (MoveData move : data)
        {
            subfiles.add(move.save().get(GameFiles.MOVES));
        }

        return Collections.singletonMap(GameFiles.MOVES, Narc.fromContentsAndNames(subfiles, new Fnt.Folder(), Endianness.EndiannessType.BIG));
    }

    @Override
    public List<GameFiles> getRequirements()
    {
        return Collections.singletonList(GameFiles.MOVES);
    }
}
