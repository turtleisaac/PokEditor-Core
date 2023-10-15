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

package io.github.turtleisaac.pokeditor.formats.babies;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class BabyFormData implements GenericFileData
{
    // u16[number of species]
    private ArrayList<Integer> babyFormList;

    public BabyFormData(Map<GameFiles, byte[]> files)
    {
        setData(files);
    }

    @Override
    public void setData(Map<GameFiles, byte[]> files)
    {
        if (!files.containsKey(GameFiles.BABY_FORMS))
        {
            throw new RuntimeException("Baby forms file not provided to editor");
        }

        byte[] file = files.get(GameFiles.BABY_FORMS);

        if (file.length % 2 != 0)
        {
            throw new RuntimeException("Invalid baby forms file, length is not even");
        }

        babyFormList = new ArrayList<>();

        MemBuf dataBuf = MemBuf.create(file);
        MemBuf.MemBufReader reader = dataBuf.reader();

        while (reader.getPosition() < file.length)
        {
            babyFormList.add(reader.readUInt16());
        }
    }

    @Override
    public Map<GameFiles, byte[]> save()
    {
        MemBuf dataBuf = MemBuf.create();
        MemBuf.MemBufWriter writer = dataBuf.writer();

        for (Integer species : babyFormList)
        {
            writer.writeShort(species.shortValue());
        }

        return Collections.singletonMap(GameFiles.BABY_FORMS, dataBuf.reader().getBuffer());
    }

    public ArrayList<Integer> getBabyFormList()
    {
        return babyFormList;
    }

    public void setBabyFormList(ArrayList<Integer> babyFormList)
    {
        this.babyFormList = babyFormList;
    }
}
