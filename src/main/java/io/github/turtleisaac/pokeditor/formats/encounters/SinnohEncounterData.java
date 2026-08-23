package io.github.turtleisaac.pokeditor.formats.encounters;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;

import java.util.Collections;
import java.util.Map;

public class SinnohEncounterData extends GenericEncounterData
{
    int[] swarmSpecies;
    int[] daySpecies;
    int[] nightSpecies;
    int[] radarSpecies;
    byte[] formProbability;

    int[][] dualSlotSpecies;

    // 0x2C bytes sitting between the surf encounters and the old rod encounters - contents are not
    // understood yet, so they are preserved verbatim rather than zeroed out on save
    byte[] unknown;

    public SinnohEncounterData(BytesDataContainer files)
    {
        super(files);
    }

    private SinnohEncounterData() {
        super();
        fieldSpecies = new int[1][NUM_BASE_FIELD_ENCOUNTER_SLOTS];
        swarmSpecies = new int[NUM_SWARM_DAY_NIGHT_ENCOUNTER_SLOTS];
        daySpecies = new int[NUM_SWARM_DAY_NIGHT_ENCOUNTER_SLOTS];
        nightSpecies = new int[NUM_SWARM_DAY_NIGHT_ENCOUNTER_SLOTS];
        radarSpecies = new int[NUM_RADAR_ENCOUNTER_SLOTS];
        formProbability = new byte[NUM_FORM_PROBABILITY_BYTES];
        dualSlotSpecies = new int[NUM_DUAL_SLOT_GAMES][NUM_DUAL_SLOT_ENCOUNTER_SLOTS];
        unknown = new byte[NUM_UNKNOWN_BYTES];

        for (int i = 0; i < waterEncounters.length; i++)
        {
            WaterEncounterSet set = new WaterEncounterSet(WaterEncounterSet.NUM_WATER_SLOTS);
            for (int slot = 0; slot < WaterEncounterSet.NUM_WATER_SLOTS; slot++)
            {
                set.slotPadding[slot] = new byte[NUM_WATER_SLOT_PADDING_BYTES];
            }
            waterEncounters[i] = set;
        }
    }

    public static SinnohEncounterData create()
    {
        return new SinnohEncounterData();
    }

    @Override
    public void setData(BytesDataContainer files)
    {
        if (!files.containsKey(GameFiles.ENCOUNTERS))
        {
            throw new RuntimeException("Encounters narc not provided to editor");
        }

        byte[] file = files.get(GameFiles.ENCOUNTERS, null);

        MemBuf dataBuf = MemBuf.create(file);
        MemBuf.MemBufReader reader = dataBuf.reader();

        fieldRate = reader.readInt();
        fieldSpecies = new int[1][NUM_BASE_FIELD_ENCOUNTER_SLOTS];

        for (int i = 0; i < NUM_BASE_FIELD_ENCOUNTER_SLOTS; i++)
        {
            fieldLevels[i] = reader.readInt();
            fieldSpecies[PLAT_FIELD_ENCOUNTER_SET_IDX][i] = reader.readInt();
        }

        //swarm species
        swarmSpecies = new int[NUM_SWARM_DAY_NIGHT_ENCOUNTER_SLOTS];
        for (int i = 0; i < NUM_SWARM_DAY_NIGHT_ENCOUNTER_SLOTS; i++)
        {
            swarmSpecies[i] = reader.readInt();
        }

        // day species
        daySpecies = new int[NUM_SWARM_DAY_NIGHT_ENCOUNTER_SLOTS];
        for (int i = 0; i < NUM_SWARM_DAY_NIGHT_ENCOUNTER_SLOTS; i++)
        {
            daySpecies[i] = reader.readInt();
        }

        // night species
        nightSpecies = new int[NUM_SWARM_DAY_NIGHT_ENCOUNTER_SLOTS];
        for (int i = 0; i < NUM_SWARM_DAY_NIGHT_ENCOUNTER_SLOTS; i++)
        {
            nightSpecies[i] = reader.readInt();
        }

        // four pokeradar mons
        radarSpecies = new int[NUM_RADAR_ENCOUNTER_SLOTS];
        for (int i = 0; i < NUM_RADAR_ENCOUNTER_SLOTS; i++)
        {
            radarSpecies[i] = reader.readInt();
        }

        //todo really read the form probability table
        formProbability = reader.readBytes(NUM_FORM_PROBABILITY_BYTES);

        //todo really read the dual slot mons
        dualSlotSpecies = new int[NUM_DUAL_SLOT_GAMES][NUM_DUAL_SLOT_ENCOUNTER_SLOTS];
        for (int game = 0; game < NUM_DUAL_SLOT_GAMES; game++)
        {
            for (int slot = 0; slot < NUM_DUAL_SLOT_ENCOUNTER_SLOTS; slot++)
            {
                dualSlotSpecies[game][slot] = reader.readInt();
            }
        }

        surfRate = reader.readInt();
        waterEncounters[0] = readWaterEncounterSet(reader);

        //todo figure out wtf is going on here - preserved verbatim so a save round-trips
        unknown = reader.readBytes(NUM_UNKNOWN_BYTES);

        oldRodRate = reader.readInt();
        waterEncounters[1] = readWaterEncounterSet(reader);

        goodRodRate = reader.readInt();
        waterEncounters[2] = readWaterEncounterSet(reader);

        superRodRate = reader.readInt();
        waterEncounters[3] = readWaterEncounterSet(reader);
    }

    private WaterEncounterSet readWaterEncounterSet(MemBuf.MemBufReader reader)
    {
        WaterEncounterSet set = new WaterEncounterSet(WaterEncounterSet.NUM_WATER_SLOTS);
        for (int i = 0; i < WaterEncounterSet.NUM_WATER_SLOTS; i++)
        {
            set.maxLevels[i] = reader.readUInt8();
            set.minLevels[i] = reader.readUInt8();
            set.slotPadding[i] = reader.readBytes(NUM_WATER_SLOT_PADDING_BYTES);
            set.species[i] = reader.readInt();
        }
        return set;
    }

    @Override
    public BytesDataContainer save()
    {
        MemBuf dataBuf = MemBuf.create();
        MemBuf.MemBufWriter writer = dataBuf.writer();

        writer.writeInt(fieldRate);

        for (int i = 0; i < NUM_BASE_FIELD_ENCOUNTER_SLOTS; i++)
        {
            writer.writeInt(fieldLevels[i]);
            writer.writeInt(fieldSpecies[PLAT_FIELD_ENCOUNTER_SET_IDX][i]);
        }

        //swarm species, then day species, then night species (2 each)
        for (int i = 0; i < NUM_SWARM_DAY_NIGHT_ENCOUNTER_SLOTS; i++)
        {
            writer.writeInt(swarmSpecies[i]);
        }

        for (int i = 0; i < NUM_SWARM_DAY_NIGHT_ENCOUNTER_SLOTS; i++)
        {
            writer.writeInt(daySpecies[i]);
        }

        for (int i = 0; i < NUM_SWARM_DAY_NIGHT_ENCOUNTER_SLOTS; i++)
        {
            writer.writeInt(nightSpecies[i]);
        }

        // four pokeradar mons
        for (int i = 0; i < NUM_RADAR_ENCOUNTER_SLOTS; i++)
        {
            writer.writeInt(radarSpecies[i]);
        }

        //todo really write the form probability table
        writer.write(formProbability);

        //todo really write the dual slot mons
        for (int game = 0; game < NUM_DUAL_SLOT_GAMES; game++)
        {
            for (int slot = 0; slot < NUM_DUAL_SLOT_ENCOUNTER_SLOTS; slot++)
            {
                writer.writeInt(dualSlotSpecies[game][slot]);
            }
        }

        writeWaterEncounterSet(writer, surfRate, waterEncounters[0]);

        //todo figure out wtf is going on here - preserved verbatim so a save round-trips
        writer.write(unknown);

        writeWaterEncounterSet(writer, oldRodRate, waterEncounters[1]);
        writeWaterEncounterSet(writer, goodRodRate, waterEncounters[2]);
        writeWaterEncounterSet(writer, superRodRate, waterEncounters[3]);

        return new BytesDataContainer(GameFiles.ENCOUNTERS, null, dataBuf.reader().getBuffer());
    }

    private void writeWaterEncounterSet(MemBuf.MemBufWriter writer, int rate, WaterEncounterSet set)
    {
        writer.writeInt(rate);
        for (int i = 0; i < set.getNumSlots(); i++)
        {
            writer.writeBytes(set.maxLevels[i], set.minLevels[i]);
            writer.write(set.slotPadding[i] != null ? set.slotPadding[i] : new byte[NUM_WATER_SLOT_PADDING_BYTES]);
            writer.writeInt(set.species[i]);
        }
    }

    public int getFieldSpecies(int idx)
    {
        return fieldSpecies[PLAT_FIELD_ENCOUNTER_SET_IDX][idx];
    }

    public void setFieldSpecies(int idx, int species)
    {
        fieldSpecies[PLAT_FIELD_ENCOUNTER_SET_IDX][idx] = species;
    }

    public int getSwarmSpecies(int idx)
    {
        return swarmSpecies[idx];
    }

    public void setSwarmSpecies(int idx, int species)
    {
        this.swarmSpecies[idx] = species;
    }

    public int getDaySpecies(int idx)
    {
        return daySpecies[idx];
    }

    public void setDaySpecies(int idx, int species)
    {
        this.daySpecies[idx] = species;
    }

    public int getNightSpecies(int idx)
    {
        return nightSpecies[idx];
    }

    public void setNightSpecies(int idx, int species)
    {
        this.nightSpecies[idx] = species;
    }

    public int getRadarSpecies(int idx)
    {
        return radarSpecies[idx];
    }

    public void setRadarSpecies(int idx, int species)
    {
        this.radarSpecies[idx] = species;
    }

    private static final int NUM_SWARM_DAY_NIGHT_ENCOUNTER_SLOTS = 2;
    private static final int NUM_RADAR_ENCOUNTER_SLOTS = 4;
    private static final int NUM_DUAL_SLOT_ENCOUNTER_SLOTS = 2;
    private static final int NUM_DUAL_SLOT_GAMES = 5;
    private static final int NUM_FORM_PROBABILITY_BYTES = 24;
    private static final int NUM_UNKNOWN_BYTES = 0x2C;
    private static final int NUM_WATER_SLOT_PADDING_BYTES = 2;
    private static final int PLAT_FIELD_ENCOUNTER_SET_IDX = 0;

    enum DualSlot {
        RUBY,
        SAPPHIRE,
        EMERALD,
        FIRERED,
        LEAFGREEN
    }
}
