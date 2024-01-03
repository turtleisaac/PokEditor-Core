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

package io.github.turtleisaac.pokeditor.formats.scripts;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.turtleisaac.nds4j.Fnt;
import io.github.turtleisaac.nds4j.Narc;
import io.github.turtleisaac.nds4j.binaries.CodeBinary;
import io.github.turtleisaac.nds4j.framework.Endianness;
import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.formats.GenericParser;
import io.github.turtleisaac.pokeditor.formats.scripts.antlr4.CommandDiscoverer;
import io.github.turtleisaac.pokeditor.formats.scripts.antlr4.CommandMacro;
import io.github.turtleisaac.pokeditor.formats.scripts.macros.MacrosLexer;
import io.github.turtleisaac.pokeditor.formats.scripts.macros.MacrosParser;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gamedata.GameCodeBinaries;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ScriptParser implements GenericParser<GenericScriptData>
{
    public static List<CommandMacro> commandMacros;
    public static HashMap<Integer, CommandMacro> nativeCommands;
    public static List<CommandMacro> convenienceCommands;
    public static HashMap<Integer, String> movementNames;

    public static HashMap<String, Integer> definedValues;

    static {

        MacrosParser.EntriesContext entryContext = prepareMacros("/data/Scrcmd_Hg.txt");
        CommandDiscoverer visitor = new CommandDiscoverer();
        commandMacros = visitor.discoverAllCommands(entryContext);

        String[] alternateNames;
        try {
            alternateNames = new String(ScriptParser.class.getResourceAsStream("/data/AlternateNames_Hg.txt").readAllBytes()).split("\n");
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }

        nativeCommands = new HashMap<>();
        commandMacros.forEach(commandMacro ->
        {
            if (commandMacro.getId() >= 0)
            {
                nativeCommands.put(commandMacro.getId(), commandMacro);
                String name = alternateNames[commandMacro.getId()];
                if (!name.isEmpty())
                    commandMacro.setName(name);
            }
        });

        entryContext = prepareMacros("/data/Convenience_Hg.txt");
        visitor = new CommandDiscoverer.ConvenienceCommandDiscoverer();
        convenienceCommands = visitor.discoverAllCommands(entryContext);

        InputStream inputStream = TextBankData.class.getResourceAsStream("/data/Movements_Hg.json");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        JsonNode movements;

        try {
            movements = objectMapper.readTree(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
            inputStream.close();
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }

        movementNames = new HashMap<>();
        for (int i = 0; i < 255; i++)
        {
            JsonNode node = movements.get(String.valueOf(i));
            if (node != null)
                movementNames.put(i, node.asText());
        }

        definedValues = new HashMap<>();
        definedValues.put("LESS", 0);
        definedValues.put("EQUAL", 1);
        definedValues.put("GREATER", 2);
        definedValues.put("LESS_OR_EQUAL", 3);
        definedValues.put("GREATER_OR_EQUAL", 4);
        definedValues.put("DIFFERENT", 5);
    }

    private static MacrosParser.EntriesContext prepareMacros(String path)
    {
        MacrosLexer lexer;
        try {
            lexer = new MacrosLexer(CharStreams.fromStream(ScriptParser.class.getResourceAsStream(path)));
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MacrosParser parser = new MacrosParser(tokens);

        return parser.entries();
    }

    @Override
    public List<GenericScriptData> generateDataList(Map<GameFiles, Narc> narcs, Map<GameCodeBinaries, CodeBinary> codeBinaries)
    {
        if (!narcs.containsKey(GameFiles.SCRIPTS))
        {
            throw new RuntimeException("Scripts narc not provided to editor");
        }

        Narc scripts = narcs.get(GameFiles.SCRIPTS);
        ArrayList<GenericScriptData> data = new ArrayList<>();

        int i = 0;
        for (byte[] subfile : scripts.getFiles())
        {
//            System.out.print(i);
            if (i == 266) {
                System.currentTimeMillis();
            }
            if (testFileIsLevelScript(subfile))
            {
//                System.out.println(" (Level)");
                data.add(new LevelScriptData(new BytesDataContainer(GameFiles.SCRIPTS, null, subfile)));
            }
            else
            {
//                System.out.println(" (Normal)");
                data.add(new ScriptData(new BytesDataContainer(GameFiles.SCRIPTS, null, subfile)));
            }
            i++;
        }

        return data;
    }

    private boolean testFileIsLevelScript(byte[] subfile)
    {
        MemBuf buf = MemBuf.create(subfile);
        MemBuf.MemBufReader reader = buf.reader();

        List<Integer> temp = new ArrayList<>();

        // Is Level Script as long as magic number FD13 doesn't exist
        boolean ret = true;
        try
        {
            ret = GenericScriptData.fileIsLevelScriptFile(reader, temp);
            return ret;
        }
        catch (IllegalStateException e)
        {
            // in theory should only happen if the file is not a level script?
            // Now this may appear in a few level scripts that don't have a 4-byte aligned "00 00 00 00"
            if (!ret) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    @Override
    public Map<GameFiles, Narc> processDataList(List<GenericScriptData> data, Map<GameCodeBinaries, CodeBinary> codeBinaries)
    {
        ArrayList<byte[]> subfiles = new ArrayList<>();
        for (GenericScriptData scriptData : data)
        {
            subfiles.add(scriptData.save().get(GameFiles.SCRIPTS, null));
        }

        return Collections.singletonMap(GameFiles.SCRIPTS, Narc.fromContentsAndNames(subfiles, new Fnt.Folder(), Endianness.EndiannessType.BIG));
    }

    @Override
    public List<GameFiles> getRequirements()
    {
        return Collections.singletonList(GameFiles.SCRIPTS);
    }

    @Override
    public List<GameCodeBinaries> getRequiredBinaries()
    {
        return Collections.emptyList();
    }

    static final int SCRIPT_MAGIC_ID = 0xFD13;
}
