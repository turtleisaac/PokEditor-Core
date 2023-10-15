package io.github.turtleisaac.pokeditor.formats.evolutions;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class EvolutionData extends ArrayList<EvolutionData.EvolutionEntry> implements GenericFileData
{
    public EvolutionData(Map<GameFiles, byte[]> files)
    {
        super();
        setData(files);
    }

    @Override
    public void setData(Map<GameFiles, byte[]> files)
    {
        if (!files.containsKey(GameFiles.EVOLUTIONS))
        {
            throw new RuntimeException("Evolutions narc not provided to editor");
        }

        byte[] file = files.get(GameFiles.EVOLUTIONS);

        MemBuf dataBuf = MemBuf.create(file);
        MemBuf.MemBufReader reader = dataBuf.reader();

        for (int i = 0; i < file.length / 6; i++)
        {
            add(new EvolutionEntry(reader.readShort(), reader.readShort(), reader.readShort()));
        }
    }

    @Override
    public Map<GameFiles, byte[]> save()
    {
        MemBuf dataBuf = MemBuf.create();
        MemBuf.MemBufWriter writer = dataBuf.writer();

        for(EvolutionEntry entry : this)
        {
            writer.writeShort((short) entry.getMethod());
            writer.writeShort((short) entry.getRequirement());
            writer.writeShort((short) entry.getResultSpecies());
        }

        writer.writeShort((short) 0);

        return Collections.singletonMap(GameFiles.EVOLUTIONS, dataBuf.reader().getBuffer());
    }

    public static class EvolutionEntry
    {
        int method;
        int requirement;
        int resultSpecies;

        public EvolutionEntry()
        {
            this.method = 0;
            this.requirement = 0;
            this.resultSpecies = 0;
        }

        public EvolutionEntry(int method, int requirement, int resultSpecies)
        {
            this.method = method;
            this.requirement = requirement;
            this.resultSpecies = resultSpecies;
        }

        public int getMethod()
        {
            return method;
        }

        public void setMethod(int method)
        {
            this.method = method;
        }

        public int getRequirement()
        {
            return requirement;
        }

        public void setRequirement(int requirement)
        {
            this.requirement = requirement;
        }

        public int getResultSpecies()
        {
            return resultSpecies;
        }

        public void setResultSpecies(int resultSpecies)
        {
            this.resultSpecies = resultSpecies;
        }
    }
}
