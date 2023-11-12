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
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.gamedata.GameCodeBinaries;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
import io.github.turtleisaac.pokeditor.formats.GenericParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PersonalParser implements GenericParser<PersonalData>
{
    @Override
    public List<PersonalData> generateDataList(Map<GameFiles, Narc> narcs, Map<GameCodeBinaries, CodeBinary> codeBinaries)
    {
        if (!narcs.containsKey(GameFiles.PERSONAL))
        {
            throw new RuntimeException("Personal narc not provided to editor");
        }

        Narc personal = narcs.get(GameFiles.PERSONAL);
        ArrayList<PersonalData> data = new ArrayList<>();

        for (byte[] subfile : personal.getFiles())
        {
            data.add(new PersonalData(new BytesDataContainer(GameFiles.PERSONAL, null, subfile)));
        }

        return data;
    }

    @Override
    public Map<GameFiles, Narc> processDataList(List<PersonalData> data, Map<GameCodeBinaries, CodeBinary> codeBinaries)
    {
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
