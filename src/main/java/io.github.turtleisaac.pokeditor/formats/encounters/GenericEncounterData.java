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

    int[] miscSpecies = new int[NUM_MISC_ENCOUNTER_SLOTS];
    int[] specialSpecies = new int[NUM_SPECIAL_ENCOUNTER_SLOTS];

    WaterEncounterSet[] waterEncounters = new WaterEncounterSet[NUM_WATER_ENCOUNTER_SETS];


    public GenericEncounterData(Map<GameFiles, byte[]> files)
    {
        setData(files);
    }

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

    public int getFieldSpecies(int set, int idx)
    {
        return fieldSpecies[set][idx];
    }

    public void setFieldSpecies(int set, int idx, int species)
    {
        fieldSpecies[set][idx] = species;
    }

    public int getPlatSwarmSpecies(int idx)
    {
        return miscSpecies[idx];
    }

    public void setPlatSwarmSpecies(int idx, int species)
    {
        miscSpecies[idx] = species;
    }

    public int getPlatDaySpecies(int idx)
    {
        return miscSpecies[idx + 2];
    }

    public void setPlatDaySpecies(int idx, int species)
    {
        miscSpecies[idx + 2] = species;
    }

    public int getPlatNightSpecies(int idx)
    {
        return miscSpecies[idx + 4];
    }

    public void setPlatNightSpecies(int idx, int species)
    {
        miscSpecies[idx + 4] = species;
    }

    public int getPlatRadarSpecies(int idx)
    {
        return specialSpecies[idx];
    }

    public void setPlatRadarSpecies(int idx, int species)
    {
        specialSpecies[idx] = species;
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
    protected static final int NUM_MISC_ENCOUNTER_SLOTS = 6; // day/night/swarm in Plat, sinnoh/hoenn/smash in HGSS
    protected static final int NUM_SPECIAL_ENCOUNTER_SLOTS = 4; // pokeradar in Plat, swarm in HGSS
    protected static final int NUM_WATER_ENCOUNTER_SETS = 4;

    public static class WaterEncounterSet {
        int[] minLevels = new int[NUM_SLOTS];
        int[] maxLevels = new int[NUM_SLOTS];
        int[] species = new int[NUM_SLOTS];

        protected static final int NUM_SLOTS = 5;
    }
}
