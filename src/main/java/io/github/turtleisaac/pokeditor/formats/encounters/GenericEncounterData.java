package io.github.turtleisaac.pokeditor.formats.encounters;

import io.github.turtleisaac.pokeditor.formats.GameFiles;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;

import java.util.Map;

public abstract class GenericEncounterData implements GenericFileData
{
    int fieldRate;
    int surfRate;
    int rockSmashRate;
    int oldRodRate;
    int goodRodRate;
    int superRodRate;

    int[] fieldLevels = new int[NUM_BASE_FIELD_ENCOUNTER_SLOTS];
    int[][] fieldSpecies; // [1][NUM_BASE_FIELD_ENCOUNTER_SLOTS] in Plat, [3][NUM_BASE_FIELD_ENCOUNTER_SLOTS] in HGSS

    WaterEncounterSet[] waterEncounters = new WaterEncounterSet[NUM_WATER_ENCOUNTER_SETS];


    public GenericEncounterData(Map<GameFiles, byte[]> files)
    {
        setData(files);
    }

    protected GenericEncounterData() {};

    @Override
    public abstract void setData(Map<GameFiles, byte[]> files);

    @Override
    public abstract Map<GameFiles, byte[]> save();

    public int getFieldRate()
    {
        return fieldRate;
    }

    public void setFieldRate(int fieldRate)
    {
        this.fieldRate = fieldRate;
    }

    public int getSurfRate()
    {
        return surfRate;
    }

    public void setSurfRate(int surfRate)
    {
        this.surfRate = surfRate;
    }

    public int getOldRodRate()
    {
        return oldRodRate;
    }

    public void setOldRodRate(int oldRodRate)
    {
        this.oldRodRate = oldRodRate;
    }

    public int getGoodRodRate()
    {
        return goodRodRate;
    }

    public void setGoodRodRate(int goodRodRate)
    {
        this.goodRodRate = goodRodRate;
    }

    public int getSuperRodRate()
    {
        return superRodRate;
    }

    public void setSuperRodRate(int superRodRate)
    {
        this.superRodRate = superRodRate;
    }

    public int getFieldLevel(int idx)
    {
        return fieldLevels[idx];
    }

    public void setFieldLevel(int idx, int level)
    {
        this.fieldLevels[idx] = level;
    }

    public WaterEncounterSet getWaterEncounters(int idx)
    {
        return waterEncounters[idx];
    }

    public void setWaterEncounters(int idx, WaterEncounterSet set)
    {
        waterEncounters[idx] = set;
    }

    protected static final int NUM_BASE_FIELD_ENCOUNTER_SLOTS = 12;
    protected static final int NUM_WATER_ENCOUNTER_SETS = 4;

    public static class WaterEncounterSet {
        private final int numSlots;

        int[] minLevels;
        int[] maxLevels;
        int[] species;

        WaterEncounterSet(int numSlots)
        {
            this.numSlots = numSlots;
            minLevels = new int[numSlots];
            maxLevels = new int[numSlots];
            species = new int[numSlots];
        }

        public int getNumSlots()
        {
            return numSlots;
        }

        protected static final int NUM_WATER_SLOTS = 5;
    }
}
