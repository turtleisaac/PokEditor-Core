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

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.GameFiles;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;

import java.util.Collections;
import java.util.Map;

public class MoveData implements GenericFileData
{
    int effect; // u16
    int category; // u8
    int power; // u8

    int type; // u8
    int accuracy; // u8
    int pp; // u8
    int additionalEffect; // u8

    boolean[] target; // u16
    int priority; // s8
    boolean[] flags; // u8

    int contestEffect; // u8
    int contestType; // u8

    public MoveData(Map<GameFiles, byte[]> files)
    {
        setData(files);
    }

    @Override
    public void setData(Map<GameFiles, byte[]> files)
    {
        if (!files.containsKey(GameFiles.MOVES))
        {
            throw new RuntimeException("Moves narc not provided to editor");
        }

        MemBuf dataBuf = MemBuf.create(files.get(GameFiles.MOVES));
        MemBuf.MemBufReader reader = dataBuf.reader();

        effect = reader.readUInt16();
        category = reader.readUInt8();
        power = reader.readUInt8();

        type = reader.readUInt8();
        accuracy = reader.readUInt8();
        pp = reader.readUInt8();
        additionalEffect = reader.readUInt8();

        target = new boolean[NUM_TARGET_FLAGS];
        int composite = reader.readUInt16();
        for (int i = 0; i < target.length; i++)
        {
            target[i] = ((composite >> i) & 1) == 1;
        }

        priority = reader.readByte();

        flags = new boolean[NUM_MOVE_FLAGS];
        composite = reader.readUInt8();
        for (int i = 0; i < flags.length; i++)
        {
            flags[i] = ((composite >> i) & 1) == 1;
        }

        contestEffect = reader.readUInt8();
        contestType = reader.readUInt8();
    }

    @Override
    public Map<GameFiles, byte[]> save()
    {
        MemBuf dataBuf = MemBuf.create();
        MemBuf.MemBufWriter writer = dataBuf.writer();

        writer.writeShort((short) effect);
        writer.writeBytes(category, power, type, accuracy, pp, additionalEffect);

        int composite = 0;
        for (int i = 0; i < NUM_TARGET_FLAGS; i++)
        {
            composite |= ((target[i] ? 1 : 0) << i);
        }
        writer.writeShort((short) composite);

        writer.writeBytes(priority);

        composite = 0;
        for (int i = 0; i < NUM_MOVE_FLAGS; i++)
        {
            composite |= ((flags[i] ? 1 : 0) << i);
        }
        writer.writeBytes(composite, contestEffect, contestType, 0, 0);

        return Collections.singletonMap(GameFiles.MOVES, dataBuf.reader().getBuffer());
    }

    private static final int NUM_TARGET_FLAGS = 12;
    private static final int NUM_MOVE_FLAGS = 8;
}
