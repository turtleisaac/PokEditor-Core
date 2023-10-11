package io.github.turtleisaac.pokeditor.formats.learnsets;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.GameFiles;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class LearnsetData extends ArrayList<LearnsetData.LearnsetEntry> implements GenericFileData
{
    public LearnsetData(Map<GameFiles, byte[]> files)
    {
        super();
        setData(files);
    }

    @Override
    public void setData(Map<GameFiles, byte[]> files)
    {
        if (!files.containsKey(GameFiles.LEVEL_UP_LEARNSETS))
        {
            throw new RuntimeException("Level-up learnsets narc not provided to editor");
        }

        byte[] file = files.get(GameFiles.LEVEL_UP_LEARNSETS);

//        int numMoves;
        MemBuf dataBuf = MemBuf.create(file);
        MemBuf.MemBufReader reader = dataBuf.reader();

//        reader.skip(file.length - 4);
//        int end = reader.readInt();
//
//        if (end == 0xFFFF0000)
//        {
//            numMoves = (file.length-4) / 2;
//        }
//        else if ((end & 0xFFFF) == 0xFFFF)
//        {
//            numMoves = (file.length-2) / 2;
//        }
//        else
//        {
//            throw new RuntimeException("Learnset file missing delimiter");
//        }
//
//        reader.setPosition(0);

        short combinedValue;
        while ((combinedValue = reader.readShort()) != (short) 0xFFFF)
        {
            add(new LearnsetEntry(getMoveId(combinedValue), getLevelLearned(combinedValue)));
        }
    }

    @Override
    public Map<GameFiles, byte[]> save()
    {
        MemBuf dataBuf = MemBuf.create();
        MemBuf.MemBufWriter writer = dataBuf.writer();

        int idx = 0;
        for(LearnsetEntry entry : this)
        {
            writer.writeShort((short) produceLearnData(entry));
            idx++;
        }

        if (idx % 2 == 0)
        {
            writer.writeBytes(0xFF, 0xFF, 0, 0);
        }
        else {
            writer.writeShort((short) 0xFFFF);
        }

        return Collections.singletonMap(GameFiles.LEVEL_UP_LEARNSETS, dataBuf.reader().getBuffer());
    }

    private static int getMoveId(short x)
    {
        return x & 0x1FF;
    }

    private static int getLevelLearned(short x)
    {
        return (x >> 9) & 0x7F;
    }

    private static int produceLearnData(LearnsetEntry entry)
    {
        return ((entry.getLevel() & 0x7f) << 9) | (entry.getMoveID() & 0x1ff);
    }

    public void sortLearnset()
    {
        super.sort(Comparator.comparingInt(m -> m.level));
//        ArrayList<LearnsetEntry> sortedLearnset = new ArrayList<>();
//        for (int i = 0; i <= 100; i++)
//        {
//            for (LearnsetEntry entry : this)
//            {
//                if (entry.getLevel() == i)
//                {
//                    sortedLearnset.add(entry);
//                }
//            }
//        }
//
//        clear();
//        addAll(sortedLearnset);
    }

    public static class LearnsetEntry
    {
        private int moveID;
        private int level;

        public LearnsetEntry()
        {
            moveID = 1;
            level = 1;
        }

        public LearnsetEntry(int moveID, int level)
        {
            this.moveID = moveID;
            this.level = level;
        }

        public int getMoveID()
        {
            return moveID;
        }

        public void setMoveID(int moveID)
        {
            this.moveID = moveID;
        }

        public int getLevel()
        {
            return level;
        }

        public void setLevel(int level)
        {
            this.level = level;
        }
    }
}
