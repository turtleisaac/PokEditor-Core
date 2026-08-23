package io.github.turtleisaac.pokeditor.formats.evolutions;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.formats.FieldWidth;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class EvolutionData extends ArrayList<EvolutionData.EvolutionEntry> implements GenericFileData
{
    private int fileSize = FIXED_FILE_SIZE;

    public EvolutionData(BytesDataContainer files)
    {
        super();
        setData(files);
    }

    @Override
    public void setData(BytesDataContainer files)
    {
        if (!files.containsKey(GameFiles.EVOLUTIONS))
        {
            throw new RuntimeException("Evolutions narc not provided to editor");
        }

        byte[] file = files.get(GameFiles.EVOLUTIONS, null);

        MemBuf dataBuf = MemBuf.create(file);
        MemBuf.MemBufReader reader = dataBuf.reader();

        fileSize = Math.max(file.length, FIXED_FILE_SIZE);

        // everything the file holds is read - dropping entries here would silently discard them on save
        int numEntries = file.length / ENTRY_SIZE;
        for (int i = 0; i < numEntries; i++)
        {
            // unsigned: these are species and item IDs, which run past 0x7FFF in expanded ROMs.
            // readShort() sign extended them, so a species above 32767 came back negative and the
            // sheet's declared 0..65535 range was a value the read path could not reproduce.
            add(new EvolutionEntry(reader.readUInt16(), reader.readUInt16(), reader.readUInt16()));
        }
    }

    @Override
    public BytesDataContainer save()
    {
        MemBuf dataBuf = MemBuf.create();
        MemBuf.MemBufWriter writer = dataBuf.writer();

        // the cap is what THIS file can hold, not what a retail one holds. setData deliberately
        // reads every entry present so an expanded table is not silently truncated, and fileSize
        // is kept at the input's length for the same reason - refusing to write those entries back
        // would parse a hacked ROM cleanly and then abort the whole NARC on save.
        int capacity = (fileSize - TERMINATOR_SIZE) / ENTRY_SIZE;
        if (size() > capacity)
        {
            throw new RuntimeException("This evolution file holds " + capacity + " entries ("
                    + fileSize + " bytes). Provided: " + size()
                    + ". Remove an evolution, or expand the file first.");
        }

        for(EvolutionEntry entry : this)
        {
            writer.writeShort((short) FieldWidth.u16(entry.getMethod(), "Evolution method"));
            writer.writeShort((short) FieldWidth.u16(entry.getRequirement(), "Evolution requirement"));
            writer.writeShort((short) FieldWidth.u16(entry.getResultSpecies(), "Evolution result species"));
        }

        writer.writeShort((short) 0);

        // the game reads a fixed-size record regardless of how many evolutions are actually populated,
        // so the subfile must always be emitted at its full length
        if (writer.getPosition() < fileSize)
        {
            writer.writeByteNumTimes((byte) 0, fileSize - writer.getPosition());
        }

        return new BytesDataContainer(GameFiles.EVOLUTIONS, null, dataBuf.reader().getBuffer());
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

    /** bytes per entry: method, requirement and result species, each a u16 */
    public static final int ENTRY_SIZE = 6;

    /** the trailing u16 terminator every evolution file carries */
    public static final int TERMINATOR_SIZE = 2;

    /** how many entries a retail-sized file holds; an expanded file holds more */
    public static final int MAX_NUM_ENTRIES = 7;

    public static final int FIXED_FILE_SIZE = MAX_NUM_ENTRIES * ENTRY_SIZE + TERMINATOR_SIZE;
}
