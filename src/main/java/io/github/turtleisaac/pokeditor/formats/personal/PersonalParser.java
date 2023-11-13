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
    private static boolean hasReadTmMoveIds = false;

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
}
