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

package io.github.turtleisaac.pokeditor.formats.items;

import io.github.turtleisaac.nds4j.Fnt;
import io.github.turtleisaac.nds4j.Narc;
import io.github.turtleisaac.nds4j.framework.Endianness;
import io.github.turtleisaac.pokeditor.formats.GameFiles;
import io.github.turtleisaac.pokeditor.formats.GenericParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ItemParser implements GenericParser<ItemData>
{
    @Override
    public List<ItemData> generateDataList(Map<GameFiles, Narc> narcs)
    {
        if (!narcs.containsKey(GameFiles.ITEMS))
        {
            throw new RuntimeException("Items narc not provided to editor");
        }

        Narc personal = narcs.get(GameFiles.ITEMS);
        ArrayList<ItemData> data = new ArrayList<>();

        for (byte[] subfile : personal.getFiles())
        {
            data.add(new ItemData(Collections.singletonMap(GameFiles.ITEMS, subfile)));
        }

        return data;
    }

    @Override
    public Map<GameFiles, Narc> processDataList(List<ItemData> data)
    {
        ArrayList<byte[]> subfiles = new ArrayList<>();
        for (ItemData item : data)
        {
            subfiles.add(item.save().get(GameFiles.ITEMS));
        }

        return Collections.singletonMap(GameFiles.ITEMS, Narc.fromContentsAndNames(subfiles, new Fnt.Folder(), Endianness.EndiannessType.BIG));
    }

    @Override
    public List<GameFiles> getRequirements()
    {
        return Collections.singletonList(GameFiles.ITEMS);
    }
}
