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
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.formats.learnsets.LearnsetData;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
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
    int effectChance; // u8

    int target; // u16
    int priority; // s8
    boolean[] flags; // u8

    int contestEffect; // u8
    int contestType; // u8

    public MoveData()
    {
        effect = 0;
        category = 0;
        power = 0;

        type = 0;
        accuracy = 0;
        pp = 0;
        effectChance = 0;

        target = 0;
        priority = 0;
        flags = new boolean[NUM_MOVE_FLAGS];

        contestEffect = 0;
        contestType = 0;
    }

    public MoveData(BytesDataContainer files)
    {
        setData(files);
    }

    @Override
    public void setData(BytesDataContainer files)
    {
        if (!files.containsKey(GameFiles.MOVES))
        {
            throw new RuntimeException("Moves narc not provided to editor");
        }

        MemBuf dataBuf = MemBuf.create(files.get(GameFiles.MOVES, null));
        MemBuf.MemBufReader reader = dataBuf.reader();

        effect = reader.readUInt16();
        category = reader.readUInt8();
        power = reader.readUInt8();

        type = reader.readUInt8();
        accuracy = reader.readUInt8();
        pp = reader.readUInt8();
        effectChance = reader.readUInt8();

        target = reader.readUInt16();

        priority = reader.readByte();

        flags = new boolean[NUM_MOVE_FLAGS];
        int composite = reader.readUInt8();
        for (int i = 0; i < flags.length; i++)
        {
            flags[i] = ((composite >> i) & 1) == 1;
        }

        contestEffect = reader.readUInt8();
        contestType = reader.readUInt8();
    }

    @Override
    public BytesDataContainer save()
    {
        MemBuf dataBuf = MemBuf.create();
        MemBuf.MemBufWriter writer = dataBuf.writer();

        writer.writeShort((short) effect);
        writer.writeBytes(category, power, type, accuracy, pp, effectChance);

        writer.writeShort((short) target);

        writer.writeBytes(priority);

        int composite = 0;
        for (int i = 0; i < NUM_MOVE_FLAGS; i++)
        {
            composite |= ((flags[i] ? 1 : 0) << i);
        }
        writer.writeBytes(composite, contestEffect, contestType, 0, 0);

        return new BytesDataContainer(GameFiles.MOVES, null, dataBuf.reader().getBuffer());
    }

    private static final int NUM_TARGET_FLAGS = 12;
    private static final int NUM_MOVE_FLAGS = 8;

    public int getEffect()
    {
        return effect;
    }

    public void setEffect(int effect)
    {
        this.effect = effect;
    }

    public int getCategory()
    {
        return category;
    }

    public void setCategory(int category)
    {
        this.category = category;
    }

    public int getPower()
    {
        return power;
    }

    public void setPower(int power)
    {
        this.power = power;
    }

    public int getType()
    {
        return type;
    }

    public void setType(int type)
    {
        this.type = type;
    }

    public int getAccuracy()
    {
        return accuracy;
    }

    public void setAccuracy(int accuracy)
    {
        this.accuracy = accuracy;
    }

    public int getPp()
    {
        return pp;
    }

    public void setPp(int pp)
    {
        this.pp = pp;
    }

    public int getEffectChance()
    {
        return effectChance;
    }

    public void setEffectChance(int effectChance)
    {
        this.effectChance = effectChance;
    }

    public int getTarget()
    {
        return target;
    }

    public void setTarget(int target)
    {
        this.target = target;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    public boolean[] getFlags()
    {
        return flags;
    }

    public void setFlags(boolean[] flags)
    {
        this.flags = flags;
    }

    public int getContestEffect()
    {
        return contestEffect;
    }

    public void setContestEffect(int contestEffect)
    {
        this.contestEffect = contestEffect;
    }

    public int getContestType()
    {
        return contestType;
    }

    public void setContestType(int contestType)
    {
        this.contestType = contestType;
    }
}
