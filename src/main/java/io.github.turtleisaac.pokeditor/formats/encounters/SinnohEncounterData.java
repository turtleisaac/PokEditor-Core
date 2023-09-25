package io.github.turtleisaac.pokeditor.formats.encounters;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.GameFiles;

import java.util.Collections;
import java.util.Map;

public class SinnohEncounterData extends GenericEncounterData
{
    public SinnohEncounterData(Map<GameFiles, byte[]> files)
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

        fieldRate = reader.readInt();
        fieldSpecies = new int[1][NUM_BASE_FIELD_ENCOUNTER_SLOTS];

        for (int i = 0; i < NUM_BASE_FIELD_ENCOUNTER_SLOTS; i++)
        {
            fieldLevels[i] = reader.readInt();
            fieldSpecies[0][i] = reader.readInt();
        }

        //swarm species, then day species, then night species (2 each)
        for (int i = 0; i < NUM_MISC_ENCOUNTER_SLOTS; i += NUM_SWARM_DAY_NIGHT_SPECIES)
        {
            miscSpecies[i] = reader.readInt();
            miscSpecies[i + 1] = reader.readInt();
        }

        // four pokeradar mons
        for (int i = 0; i < NUM_SPECIAL_ENCOUNTER_SLOTS; i++)
        {
            specialSpecies[i] = reader.readInt();
        }

        //todo really read the form probability table
        byte[] formProbability = reader.readBytes(24);

        //todo really read the dual slot mons
        byte[] dualSlot = reader.readBytes(4 * 2 * 5);

        surfRate = reader.readInt();
        waterEncounters[0] = readWaterEncounterSet(reader);

        //todo figure out wtf is going on here
        reader.skip(0x2C);

        oldRodRate = reader.readInt();
        waterEncounters[1] = readWaterEncounterSet(reader);

        goodRodRate = reader.readInt();
        waterEncounters[2] = readWaterEncounterSet(reader);

        superRodRate = reader.readInt();
        waterEncounters[3] = readWaterEncounterSet(reader);
    }

    private WaterEncounterSet readWaterEncounterSet(MemBuf.MemBufReader reader)
    {
        WaterEncounterSet set = new WaterEncounterSet();
        for (int i = 0; i < WaterEncounterSet.NUM_SLOTS; i++)
        {
            set.maxLevels[i] = reader.readByte();
            set.minLevels[i] = reader.readByte();
            reader.skip(2);
            set.species[i] = reader.readInt();
        }
        return set;
    }

    @Override
    public Map<GameFiles, byte[]> save()
    {
        MemBuf dataBuf = MemBuf.create();
        MemBuf.MemBufWriter writer = dataBuf.writer();

        writer.writeInt(fieldRate);

        for (int i = 0; i < NUM_BASE_FIELD_ENCOUNTER_SLOTS; i++)
        {
            writer.writeInt(fieldLevels[i]);
            writer.writeInt(fieldSpecies[0][i]);
        }

        //swarm species, then day species, then night species (2 each)
        for (int i = 0; i < NUM_MISC_ENCOUNTER_SLOTS; i++)
        {
            writer.writeInt(miscSpecies[i]);
        }

        // four pokeradar mons
        for (int i = 0; i < NUM_SPECIAL_ENCOUNTER_SLOTS; i++)
        {
            writer.writeInt(specialSpecies[i]);
        }

        //todo really write the form probability table
        writer.skip(24);

        //todo really write the dual slot mons
        writer.skip(2 * 4 * 5);

        writeWaterEncounterSet(writer, surfRate, waterEncounters[0]);

        //todo figure out wtf is going on here
        writer.skip(0x2C);

        writeWaterEncounterSet(writer, oldRodRate, waterEncounters[1]);
        writeWaterEncounterSet(writer, goodRodRate, waterEncounters[2]);
        writeWaterEncounterSet(writer, superRodRate, waterEncounters[3]);

        return Collections.singletonMap(GameFiles.ENCOUNTERS, dataBuf.reader().getBuffer());
    }

    private void writeWaterEncounterSet(MemBuf.MemBufWriter writer, int rate, WaterEncounterSet set)
    {
        writer.writeInt(rate);
        for (int i = 0; i < WaterEncounterSet.NUM_SLOTS; i++)
        {
            writer.writeBytes(set.maxLevels[i], set.minLevels[i]);
            writer.skip(2);
            writer.writeInt(set.species[i]);
        }
    }

    private static final int NUM_SWARM_DAY_NIGHT_SPECIES = 2;
}
