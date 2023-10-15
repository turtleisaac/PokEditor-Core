package io.github.turtleisaac.pokeditor.formats.encounters;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;

import java.util.Collections;
import java.util.Map;

public class JohtoEncounterData extends GenericEncounterData
{
    int smashRate;

    int[] hoennSoundSpecies;
    int[] sinnohSoundSpecies;

    WaterEncounterSet smashEncounterSet;

    int[] swarmSpecies;

    public JohtoEncounterData(Map<GameFiles, byte[]> files)
    {
        super(files);
    }

    @Override
    public void setData(Map<GameFiles, byte[]> files)
    {
        if (!files.containsKey(GameFiles.ENCOUNTERS))
        {
            throw new RuntimeException("Encounters narc not provided to editor");
        }

        byte[] file = files.get(GameFiles.ENCOUNTERS);

        MemBuf dataBuf = MemBuf.create(file);
        MemBuf.MemBufReader reader = dataBuf.reader();

        fieldRate = reader.readUInt8();
        surfRate = reader.readUInt8();
        smashRate = reader.readUInt8();
        oldRodRate = reader.readUInt8();
        goodRodRate = reader.readUInt8();
        superRodRate = reader.readUInt8();
        reader.skip(2);

        for (int i = 0; i < NUM_BASE_FIELD_ENCOUNTER_SLOTS; i++)
        {
            fieldLevels[i] = reader.readUInt8();
        }

        fieldSpecies = new int[NUM_FIELD_ENCOUNTER_SETS][NUM_BASE_FIELD_ENCOUNTER_SLOTS];
        for (int set = 0; set < NUM_FIELD_ENCOUNTER_SETS; set++)
        {
            for (int i = 0; i < NUM_BASE_FIELD_ENCOUNTER_SLOTS; i++)
            {
                fieldSpecies[set][i] = reader.readUInt16();
            }
        }

        hoennSoundSpecies = new int[NUM_SOUND_SMASH_ENCOUNTER_SLOTS];
        for (int i = 0; i < NUM_SOUND_SMASH_ENCOUNTER_SLOTS; i++)
        {
            hoennSoundSpecies[i] = reader.readUInt16();
        }

        sinnohSoundSpecies = new int[NUM_SOUND_SMASH_ENCOUNTER_SLOTS];
        for (int i = 0; i < NUM_SOUND_SMASH_ENCOUNTER_SLOTS; i++)
        {
            sinnohSoundSpecies[i] = reader.readUInt16();
        }

        waterEncounters[0] = readWaterEncounterSet(reader, WaterEncounterSet.NUM_WATER_SLOTS);
        smashEncounterSet = readWaterEncounterSet(reader, NUM_SMASH_SLOTS);
        for(int i = 1; i < NUM_WATER_ENCOUNTER_SETS; i++)
        {
            waterEncounters[i] = readWaterEncounterSet(reader, WaterEncounterSet.NUM_WATER_SLOTS);
        }

        swarmSpecies = new int[NUM_SWARM_ENCOUNTER_SLOTS];
        for (int i = 0; i < NUM_SWARM_ENCOUNTER_SLOTS; i++)
        {
            swarmSpecies[i] = reader.readUInt16();
        }
    }

    private WaterEncounterSet readWaterEncounterSet(MemBuf.MemBufReader reader, int numSlots)
    {
        WaterEncounterSet set = new WaterEncounterSet(numSlots);
        for (int i = 0; i < numSlots; i++)
        {
            set.minLevels[i] = reader.readUInt8();
            set.maxLevels[i] = reader.readUInt8();
            set.species[i] = reader.readUInt16();
        }
        return set;
    }

    @Override
    public Map<GameFiles, byte[]> save()
    {
        MemBuf dataBuf = MemBuf.create();
        MemBuf.MemBufWriter writer = dataBuf.writer();

        writer.writeBytes(fieldRate, surfRate, smashRate, oldRodRate, goodRodRate, superRodRate);
        writer.skip(2);

        writer.writeBytes(fieldLevels);

        for (int set = 0; set < NUM_FIELD_ENCOUNTER_SETS; set++)
        {
            for (int i = 0; i < NUM_BASE_FIELD_ENCOUNTER_SLOTS; i++)
            {
                writer.writeShort((short) fieldSpecies[set][i]);
            }
        }

        for (int i = 0; i < NUM_SOUND_SMASH_ENCOUNTER_SLOTS; i++)
        {
            writer.writeShort((short) hoennSoundSpecies[i]);
        }

        for (int i = 0; i < NUM_SOUND_SMASH_ENCOUNTER_SLOTS; i++)
        {
            writer.writeShort((short) sinnohSoundSpecies[i]);
        }

        writeWaterEncounterSet(writer, waterEncounters[0]);
        writeWaterEncounterSet(writer, smashEncounterSet);
        for(int i = 1; i < NUM_WATER_ENCOUNTER_SETS; i++)
        {
            writeWaterEncounterSet(writer, waterEncounters[i]);
        }

        for (int i = 0; i < NUM_SWARM_ENCOUNTER_SLOTS; i++)
        {
            writer.writeShort((short) swarmSpecies[i]);
        }

        return Collections.singletonMap(GameFiles.ENCOUNTERS, dataBuf.reader().getBuffer());
    }

    private void writeWaterEncounterSet(MemBuf.MemBufWriter writer, WaterEncounterSet set)
    {
        for (int i = 0; i < set.getNumSlots(); i++)
        {
            writer.writeBytes(set.minLevels[i], set.maxLevels[i]);
            writer.writeShort((short) set.species[i]);
        }
    }

    private static final int NUM_SOUND_SMASH_ENCOUNTER_SLOTS = 2;
    private static final int NUM_SMASH_SLOTS = 2;
    private static final int NUM_SWARM_ENCOUNTER_SLOTS = 4;
    private static final int NUM_FIELD_ENCOUNTER_SETS = 3;
}
