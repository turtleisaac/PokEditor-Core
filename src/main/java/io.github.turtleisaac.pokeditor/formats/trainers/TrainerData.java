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

package io.github.turtleisaac.pokeditor.formats.trainers;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.GameFiles;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TrainerData implements GenericFileData
{
    boolean movesEnabled;
    boolean itemEnabled;

    int trainerClass;
    int battleType;

    int[] items;

    boolean[] ai;
    int battleType2;
    short unknown1;
    short unknown2;
    short unknown3;

    ArrayList<TrainerPartyEntry> trainerPartyEntries;

    public TrainerData(Map<GameFiles, byte[]> files)
    {
        setData(files);
    }

    @Override
    public void setData(Map<GameFiles, byte[]> files)
    {
        if (!files.containsKey(GameFiles.TRAINER_DATA))
        {
            throw new RuntimeException("Trainer data narc not provided to editor");
        }

        if (!files.containsKey(GameFiles.TRAINER_POKEMON))
        {
            throw new RuntimeException("Trainer pokemon narc not provided to editor");
        }

        trainerPartyEntries = new ArrayList<>();

        MemBuf dataBuf = MemBuf.create(files.get(GameFiles.TRAINER_DATA));
        MemBuf.MemBufReader reader = dataBuf.reader();


        // trdata
        int flag = reader.readUInt8();
        movesEnabled = (flag & 1) == 1;
        itemEnabled = ((flag >> 1) & 1) == 1;

        trainerClass = reader.readUInt8();
        battleType = reader.readUInt8();
        int numPokemon = reader.readUInt8();

        items = new int[NUMBER_TRAINER_ITEMS];

        for (int i = 0; i < items.length; i++)
        {
            items[i] = reader.readUInt16();
        }

        ai = new boolean[NUMBER_AI_FLAGS];
        long aiComposite = reader.readUInt32();

        for (int i = 0; i < ai.length; i++)
        {
            ai[i] = ((aiComposite >> i) & 1) == 1;
        }

        battleType2 = reader.readUInt8();
        unknown1 = reader.readUInt8();
        unknown2 = reader.readUInt8();
        unknown3 = reader.readUInt8();


        // trpoke
        dataBuf = MemBuf.create(files.get(GameFiles.TRAINER_POKEMON));
        reader = dataBuf.reader();

        for (int i = 0; i < numPokemon; i++)
        {
            TrainerPartyEntry entry = new TrainerPartyEntry();
            entry.setDifficultyValue(reader.readUInt8());
            entry.setAbility(reader.readUInt8());
            entry.setLevel(reader.readUInt16());

            int combinedMon = reader.readUInt16();
            entry.setSpecies(combinedMon & 0x3ff);
            entry.setAltForm(combinedMon >> 10);

            if (itemEnabled)
            {
                entry.setHeldItem(reader.readUInt16());
            }

            if (movesEnabled)
            {
                for (int j = 0; j < NUMBER_MOVES; j++)
                {
                    entry.setMove(j, reader.readUInt16());
                }
            }

            entry.setBallCapsule(reader.readUInt16());

            trainerPartyEntries.add(entry);
        }
    }

    @Override
    public Map<GameFiles, byte[]> save()
    {
        MemBuf dataBuf = MemBuf.create();
        MemBuf.MemBufWriter writer = dataBuf.writer();

        // trdata
        int compositeFlags = (movesEnabled ? 1 : 0) | (itemEnabled ? 0b10 : 0);
        writer.writeBytes(compositeFlags, trainerClass, battleType, trainerPartyEntries.size());
        for (int i = 0; i < NUMBER_TRAINER_ITEMS; i++)
        {
            writer.writeShort((short) items[i]);
        }

        int compositeAiFlags = 0;
        for (int i = 0; i < NUMBER_AI_FLAGS; i++)
        {
            compositeAiFlags |= ((ai[i] ? 1 : 0) << i);
        }
        writer.writeInt(compositeAiFlags);
        writer.writeBytes(battleType2, unknown1, unknown2, unknown3);

        byte[] trainerDataOutput = dataBuf.reader().getBuffer();

        // trpoke
        dataBuf = MemBuf.create();
        writer = dataBuf.writer();

        for (TrainerPartyEntry entry : trainerPartyEntries)
        {
            writer.writeBytes(entry.getDifficultyValue(), entry.getAbility());
            writer.writeShort((short) entry.getLevel());
            writer.writeShort((short)( ( (entry.getAltForm() & 0x7) << 10) | (entry.getSpecies() & 0x3ff) ) );

            if (itemEnabled)
            {
                writer.writeShort((short) entry.getHeldItem());
            }

            if (movesEnabled)
            {
                for (int move : entry.getMoves())
                {
                    writer.writeShort((short) move);
                }
            }

            writer.writeShort((short) entry.getBallCapsule());
        }

        byte[] trainerPokemonOutput = dataBuf.reader().getBuffer();

        HashMap<GameFiles, byte[]> map = new HashMap<>();
        map.put(GameFiles.TRAINER_DATA, trainerDataOutput);
        map.put(GameFiles.TRAINER_POKEMON, trainerPokemonOutput);

        return map;
    }

    public boolean isMovesEnabled()
    {
        return movesEnabled;
    }

    public void setMovesEnabled(boolean movesEnabled)
    {
        this.movesEnabled = movesEnabled;
    }

    public boolean isItemEnabled()
    {
        return itemEnabled;
    }

    public void setItemEnabled(boolean itemEnabled)
    {
        this.itemEnabled = itemEnabled;
    }

    public int getTrainerClass()
    {
        return trainerClass;
    }

    public void setTrainerClass(int trainerClass)
    {
        this.trainerClass = trainerClass;
    }

    public int getBattleType()
    {
        return battleType;
    }

    public void setBattleType(int battleType)
    {
        this.battleType = battleType;
    }

    public int getNumPokemon()
    {
        return trainerPartyEntries.size();
    }

    public int[] getItems()
    {
        return items;
    }

    public void setItems(int[] items)
    {
        this.items = items;
    }

    public void setItem(int index, int item)
    {
        this.items[index] = item;
    }

    public boolean[] getAi()
    {
        return ai;
    }

    public void setAi(boolean[] ai)
    {
        this.ai = ai;
    }

    public void setAiFlag(int index, boolean flag)
    {
        this.ai[index] = flag;
    }

    public int getBattleType2()
    {
        return battleType2;
    }

    public void setBattleType2(int battleType2)
    {
        this.battleType2 = battleType2;
    }

    public short getUnknown1()
    {
        return unknown1;
    }

    public void setUnknown1(short unknown1)
    {
        this.unknown1 = unknown1;
    }

    public short getUnknown2()
    {
        return unknown2;
    }

    public void setUnknown2(short unknown2)
    {
        this.unknown2 = unknown2;
    }

    public short getUnknown3()
    {
        return unknown3;
    }

    public void setUnknown3(short unknown3)
    {
        this.unknown3 = unknown3;
    }

    public TrainerPartyEntry getTrainerPartyEntry(int index)
    {
        return trainerPartyEntries.get(index);
    }

    public void setTrainerPartyEntries(ArrayList<TrainerPartyEntry> trainerPartyEntries)
    {
        this.trainerPartyEntries = trainerPartyEntries;
    }

    public void setTrainerPartyEntry(int index, TrainerPartyEntry entry)
    {
        trainerPartyEntries.set(index, entry);
    }

    public boolean addTrainerPartyEntry(TrainerPartyEntry entry)
    {
        if (trainerPartyEntries.size() < MAX_NUMBER_TRAINER_MONS)
        {
            trainerPartyEntries.add(entry);
            return true;
        }
        return false;
    }

    public boolean addTrainerPartyEntry(int index, TrainerPartyEntry entry)
    {
        if (trainerPartyEntries.size() < MAX_NUMBER_TRAINER_MONS)
        {
            trainerPartyEntries.add(index, entry);
            return true;
        }
        return false;
    }

    public TrainerPartyEntry removeTrainerPartyEntry(int index)
    {
        if (trainerPartyEntries.size() > MIN_NUMBER_TRAINER_MONS)
        {
            return trainerPartyEntries.remove(index);
        }
        else
        {
            return null;
        }
    }

    private static final int MIN_NUMBER_TRAINER_MONS = 1;
    private static final int MAX_NUMBER_TRAINER_MONS = 6;
    private static final int NUMBER_MOVES = 4;
    private static final int NUMBER_TRAINER_ITEMS = 4;
    private static final int NUMBER_AI_FLAGS = 14;

    public static class TrainerPartyEntry
    {
        int difficultyValue;
        int ability;
        int level;
        int species;
        int altForm;
        int heldItem;
        int[] moves;
        int ballCapsule;

        public TrainerPartyEntry()
        {
            difficultyValue = 0;
            ability = 0;
            level = 1;
            species = 1;
            altForm = 0;
            heldItem = 0;
            moves = new int[NUMBER_MOVES];
            ballCapsule = 0;
        }

        public int getDifficultyValue()
        {
            return difficultyValue;
        }

        public void setDifficultyValue(int difficultyValue)
        {
            this.difficultyValue = difficultyValue;
        }

        public int getAbility()
        {
            return ability;
        }

        public void setAbility(int ability)
        {
            this.ability = ability;
        }

        public int getLevel()
        {
            return level;
        }

        public void setLevel(int level)
        {
            this.level = level;
        }

        public int getSpecies()
        {
            return species;
        }

        public void setSpecies(int species)
        {
            this.species = species;
        }

        public int getAltForm()
        {
            return altForm;
        }

        public void setAltForm(int altForm)
        {
            this.altForm = altForm;
        }

        public int getHeldItem()
        {
            return heldItem;
        }

        public void setHeldItem(int heldItem)
        {
            this.heldItem = heldItem;
        }

        public int[] getMoves()
        {
            return moves;
        }

        public void setMoves(int[] moves)
        {
            this.moves = moves;
        }

        public void setMove(int index, int move)
        {
            this.moves[index] = move;
        }

        public int getBallCapsule()
        {
            return ballCapsule;
        }

        public void setBallCapsule(int ballCapsule)
        {
            this.ballCapsule = ballCapsule;
        }
    }
}
