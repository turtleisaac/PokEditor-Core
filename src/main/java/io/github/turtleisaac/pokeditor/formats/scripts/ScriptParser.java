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

import io.github.turtleisaac.nds4j.Fnt;
import io.github.turtleisaac.nds4j.Narc;
import io.github.turtleisaac.nds4j.binaries.CodeBinary;
import io.github.turtleisaac.nds4j.framework.Endianness;
import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.formats.GenericParser;
import io.github.turtleisaac.pokeditor.formats.scripts.antlr4.CommandDiscoverer;
import io.github.turtleisaac.pokeditor.formats.scripts.antlr4.CommandMacro;
import io.github.turtleisaac.pokeditor.gamedata.GameCodeBinaries;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.util.*;

public class ScriptParser implements GenericParser<GenericScriptData>
{
    public static List<CommandMacro> commandMacros;
    public static HashMap<Integer, CommandMacro> nativeCommands;
    public static List<CommandMacro> convenienceCommands;

    static {

        MacrosParser.EntriesContext entryContext = prepareMacros("/data/Scrcmd_Hg.txt");
        CommandDiscoverer visitor = new CommandDiscoverer();
        commandMacros = visitor.discoverAllCommands(entryContext);

        nativeCommands = new HashMap<>();
        commandMacros.forEach(commandMacro ->
        {
            if (commandMacro.getId() >= 0)
                nativeCommands.put(commandMacro.getId(), commandMacro);
        });

        entryContext = prepareMacros("/data/Convenience_Hg.txt");
        visitor = new CommandDiscoverer.ConvenienceCommandDiscoverer();
        convenienceCommands = visitor.discoverAllCommands(entryContext);
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
//        if (codeBinaries == null)
//        {
//            throw new RuntimeException("Code binaries not provided to editor");
//        }
//
//        if (!codeBinaries.containsKey(GameCodeBinaries.ARM9))
//        {
//            throw new RuntimeException("Arm9 not provided to editor");
//        }

//        if (hasReadTmMoveIds)
//        {
//            CodeBinary arm9 = codeBinaries.get(GameCodeBinaries.ARM9);
//            arm9.lock();
//            try {
//                MemBuf.MemBufReader reader = arm9.getPhysicalAddressBuffer().reader();
//                reader.setPosition(Tables.TM_HM_MOVES.getPointerOffset());
//                int offset = reader.readInt();
//                MemBuf.MemBufWriter writer = arm9.getPhysicalAddressBuffer().writer();
//                writer.setPosition(offset - arm9.getRamStartAddress());
//                for (int i = 0; i < GenericScriptData.NUMBER_TMS_HMS; i++)
//                {
//                    writer.writeShort((short) tmMoveIdNumbers[i]);
//                }
//            } finally {
//                arm9.unlock();
//            }
//        }

        ArrayList<byte[]> subfiles = new ArrayList<>();
        for (GenericScriptData personal : data)
        {
            subfiles.add(personal.save().get(GameFiles.PERSONAL, null));
        }

        return Collections.singletonMap(GameFiles.PERSONAL, Narc.fromContentsAndNames(subfiles, new Fnt.Folder(), Endianness.EndiannessType.BIG));
    }

    @Override
    public List<GameFiles> getRequirements()
    {
        return Collections.singletonList(GameFiles.SCRIPTS);
    }

    @Override
    public List<GameCodeBinaries> getRequiredBinaries()
    {
        return List.of(GameCodeBinaries.ARM9);
    }

    static final int SCRIPT_MAGIC_ID = 0xFD13;
}
