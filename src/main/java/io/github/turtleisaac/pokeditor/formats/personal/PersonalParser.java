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

package io.github.turtleisaac.pokeditor.formats.personal;

import io.github.turtleisaac.nds4j.Fnt;
import io.github.turtleisaac.nds4j.Narc;
import io.github.turtleisaac.nds4j.binaries.CodeBinary;
import io.github.turtleisaac.nds4j.framework.Endianness;
import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.formats.moves.MoveData;
import io.github.turtleisaac.pokeditor.gamedata.GameCodeBinaries;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
import io.github.turtleisaac.pokeditor.formats.GenericParser;
import io.github.turtleisaac.pokeditor.gamedata.Tables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PersonalParser implements GenericParser<PersonalData>
{
    public static final int[] tmMoveIdNumbers = new int[PersonalData.NUMBER_TMS_HMS];
    public static final int[] tmMoveTypes = new int[PersonalData.NUMBER_TMS_HMS];
    private static boolean hasReadTmMoveIds = false;
    private static boolean hasReadTmMoveTypes = false;

    @Override
    public List<PersonalData> generateDataList(Map<GameFiles, Narc> narcs, Map<GameCodeBinaries, CodeBinary> codeBinaries)
    {
        if (!narcs.containsKey(GameFiles.PERSONAL))
        {
            throw new RuntimeException("Personal narc not provided to editor");
        }

        if (codeBinaries == null)
        {
            throw new RuntimeException("Code binaries not provided to editor");
        }

        if (!codeBinaries.containsKey(GameCodeBinaries.ARM9))
        {
            throw new RuntimeException("Arm9 not provided to editor");
        }

        Narc personal = narcs.get(GameFiles.PERSONAL);
        ArrayList<PersonalData> data = new ArrayList<>();

        CodeBinary arm9 = codeBinaries.get(GameCodeBinaries.ARM9);
        arm9.lock();
        try {
            MemBuf.MemBufReader reader = arm9.getPhysicalAddressBuffer().reader();
            reader.setPosition(Tables.TM_HM_MOVES.getPointerOffset());
            int offset = reader.readInt();
            reader.setPosition(offset - arm9.getRamStartAddress());
            for (int i = 0; i < PersonalData.NUMBER_TMS_HMS; i++)
            {
                tmMoveIdNumbers[i] = reader.readUInt16();
            }
            hasReadTmMoveIds = true;

            reader.setPosition(Tables.ITEMS.getPointerOffset());
            offset = reader.readInt();
            reader.setPosition(offset - arm9.getRamStartAddress());
            reader.skip(2*4*TM01_INDEX);
            for (int i = 0; i < PersonalData.NUMBER_TMS_HMS; i++)
            {
                reader.skip(4);
                tmMoveTypes[i] = tmHmPaletteIndexToType(reader.readUInt16());
                reader.skip(2);
            }

            hasReadTmMoveTypes = true;
        } finally {
            arm9.unlock();
        }

        for (byte[] subfile : personal.getFiles())
        {
            data.add(new PersonalData(new BytesDataContainer(GameFiles.PERSONAL, null, subfile)));
        }

        return data;
    }

    @Override
    public Map<GameFiles, Narc> processDataList(List<PersonalData> data, Map<GameCodeBinaries, CodeBinary> codeBinaries)
    {
        if (codeBinaries == null)
        {
            throw new RuntimeException("Code binaries not provided to editor");
        }

        if (!codeBinaries.containsKey(GameCodeBinaries.ARM9))
        {
            throw new RuntimeException("Arm9 not provided to editor");
        }

        if (hasReadTmMoveIds)
        {
            CodeBinary arm9 = codeBinaries.get(GameCodeBinaries.ARM9);
            arm9.lock();
            try {
                MemBuf.MemBufReader reader = arm9.getPhysicalAddressBuffer().reader();
                reader.setPosition(Tables.TM_HM_MOVES.getPointerOffset());
                int offset = reader.readInt();
                MemBuf.MemBufWriter writer = arm9.getPhysicalAddressBuffer().writer();
                writer.setPosition(offset - arm9.getRamStartAddress());
                for (int i = 0; i < PersonalData.NUMBER_TMS_HMS; i++)
                {
                    writer.writeShort((short) tmMoveIdNumbers[i]);
                }
            } finally {
                arm9.unlock();
            }
        }

        if (hasReadTmMoveTypes)
        {
            CodeBinary arm9 = codeBinaries.get(GameCodeBinaries.ARM9);
            arm9.lock();
            try {
                MemBuf.MemBufReader reader = arm9.getPhysicalAddressBuffer().reader();
                reader.setPosition(Tables.ITEMS.getPointerOffset());
                int offset = reader.readInt();
                MemBuf.MemBufWriter writer = arm9.getPhysicalAddressBuffer().writer();
                writer.setPosition(offset - arm9.getRamStartAddress());
                writer.skip(2*4*TM01_INDEX);
                for (int i = 0; i < PersonalData.NUMBER_TMS_HMS; i++)
                {
                    writer.skip(4);
                    writer.writeShort((short) typeToTmHmPaletteIndex(tmMoveTypes[i]));
                    writer.skip(2);
                }
            } finally {
                arm9.unlock();
            }
        }

        ArrayList<byte[]> subfiles = new ArrayList<>();
        for (PersonalData personal : data)
        {
            subfiles.add(personal.save().get(GameFiles.PERSONAL, null));
        }

        return Collections.singletonMap(GameFiles.PERSONAL, Narc.fromContentsAndNames(subfiles, new Fnt.Folder(), Endianness.EndiannessType.BIG));
    }

    @Override
    public List<GameFiles> getRequirements()
    {
        return Collections.singletonList(GameFiles.PERSONAL);
    }

    @Override
    public List<GameCodeBinaries> getRequiredBinaries()
    {
        return List.of(GameCodeBinaries.ARM9);
    }

//    public static class PersonalSerializer extends StdSerializer<PersonalData>
//    {
//        public PersonalSerializer() {
//            this(null);
//        }
//
//        public PersonalSerializer(Class<PersonalData> data) {
//            super(data);
//        }
//
//        @Override
//        public void serialize(PersonalData personalData, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException
//        {
//            jsonGenerator.writeStartObject();
//            personalData.serialize(jsonGenerator);
//            jsonGenerator.writeEndObject();
//        }
//    }

    private static final int TM01_INDEX = 328;

    private static int tmHmPaletteIndexToType(int paletteIndex)
    {
        return switch(paletteIndex) {
            case 0x18E -> 1; // fighting
            case 0x18F -> 16; // dragon
            case 0x190 -> 11; // water
            case 0x191 -> 14; // psychic
            case 0x192 -> 0; // normal
            case 0x193 -> 3; // poison
            case 0x194 -> 15; // ice
            case 0x195 -> 12; // grass
            case 0x196 -> 10; // fire
            case 0x197 -> 17; // dark
            case 0x198 -> 8; // steel
            case 0x199 -> 13; // electric
            case 0x19A -> 4; // ground
            case 0x19B -> 7; // ghost
            case 0x19C -> 5; // rock
            case 0x19D -> 2; // flying
            case 0x262 -> 6; // bug
            default -> 0;
            //todo figure out how to handle ???/fairy
        };
    }

    private static int typeToTmHmPaletteIndex(int type)
    {
        return switch(type) {
            case 1 -> 0x18E; // fighting
            case 16 -> 0x18F; // dragon
            case 11 -> 0x190; // water
            case 14 -> 0x191; // psychic
            case 0 -> 0x192; // normal
            case 3 -> 0x193; // poison
            case 15 -> 0x194; // ice
            case 12 -> 0x195; // grass
            case 10 -> 0x196; // fire
            case 17 -> 0x197; // dark
            case 8 -> 0x198; // steel
            case 13 -> 0x199; // electric
            case 4 -> 0x19A; // ground
            case 7 -> 0x19B; // ghost
            case 5 -> 0x19C; // rock
            case 2 -> 0x19D; // flying
            case 6 -> 0x262; // bug
            case 9 -> 0x191; // ???/fairy
            default -> 0x192;
            //todo figure out how to handle ???/fairy
        };
    }

    public static void updateTmType(int tmID, int moveID, List<MoveData> moves)
    {
        tmMoveIdNumbers[tmID] = moveID;
        tmMoveTypes[tmID] = moves.get(moveID).getType();
    }
}
