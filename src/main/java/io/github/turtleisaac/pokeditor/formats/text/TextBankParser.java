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

package io.github.turtleisaac.pokeditor.formats.text;

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

public class TextBankParser implements GenericParser<TextBankData>
{
    @Override
    public List<TextBankData> generateDataList(Map<GameFiles, Narc> narcs)
    {
        if (!narcs.containsKey(GameFiles.TEXT))
        {
            throw new RuntimeException("Text narc not provided to editor");
        }

        Narc personal = narcs.get(GameFiles.TEXT);
        ArrayList<TextBankData> data = new ArrayList<>();

        for (byte[] subfile : personal.getFiles())
        {
            data.add(new TextBankData(new BytesDataContainer(GameFiles.TEXT, null, subfile)));
        }

        return data;
    }

    @Override
    public Map<GameFiles, Narc> processDataList(List<TextBankData> data)
    {
        ArrayList<byte[]> subfiles = new ArrayList<>();
        for (TextBankData personal : data)
        {
            subfiles.add(personal.save().get(GameFiles.TEXT, null));
        }

        return Collections.singletonMap(GameFiles.TEXT, Narc.fromContentsAndNames(subfiles, new Fnt.Folder(), Endianness.EndiannessType.BIG));
    }

    @Override
    public List<GameFiles> getRequirements()
    {
        return Collections.singletonList(GameFiles.TEXT);
    }
}
