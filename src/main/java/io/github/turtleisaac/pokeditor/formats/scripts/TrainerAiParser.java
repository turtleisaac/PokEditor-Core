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
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.formats.GenericParser;
import io.github.turtleisaac.pokeditor.formats.scripts.antlr4.CommandDiscoverer;
import io.github.turtleisaac.pokeditor.formats.scripts.antlr4.CommandMacro;
import io.github.turtleisaac.pokeditor.formats.scripts.macros.MacrosLexer;
import io.github.turtleisaac.pokeditor.formats.scripts.macros.MacrosParser;
import io.github.turtleisaac.pokeditor.gamedata.GameCodeBinaries;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.util.*;

public class TrainerAiParser implements GenericParser<TrainerAiData>
{
    public static List<CommandMacro> commandMacros;
    public static HashMap<Integer, CommandMacro> nativeCommands;
    public static List<CommandMacro> convenienceCommands;

    static {

        MacrosParser.EntriesContext entryContext = prepareMacros("/data/macros_plat.txt");
        CommandDiscoverer visitor = new CommandDiscoverer();
        commandMacros = visitor.discoverAllCommands(entryContext);

        nativeCommands = new HashMap<>();
        commandMacros.forEach(commandMacro ->
        {
            if (commandMacro.getId() >= 0)
            {
                nativeCommands.put(commandMacro.getId(), commandMacro);
            }
        });

        entryContext = prepareMacros("/data/Convenience_Hg.txt");
        visitor = new CommandDiscoverer.ConvenienceCommandDiscoverer();
        convenienceCommands = visitor.discoverAllCommands(entryContext);
    }

    private static MacrosParser.EntriesContext prepareMacros(String path)
    {
        MacrosLexer lexer;
        try {
            lexer = new MacrosLexer(CharStreams.fromStream(TrainerAiParser.class.getResourceAsStream(path)));
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MacrosParser parser = new MacrosParser(tokens);

        return parser.entries();
    }

    @Override
    public List<TrainerAiData> generateDataList(Map<GameFiles, Narc> narcs, Map<GameCodeBinaries, CodeBinary> codeBinaries)
    {
        if (!narcs.containsKey(GameFiles.TRAINER_AI_SCRIPTS))
        {
            throw new RuntimeException("Trainer AI narc not provided to editor");
        }

        Narc scripts = narcs.get(GameFiles.TRAINER_AI_SCRIPTS);
        ArrayList<TrainerAiData> data = new ArrayList<>();

        int i = 0;
        for (byte[] subfile : scripts.getFiles())
        {
            System.out.println(i + " -> " + subfile.length);
            if (i == 266) {
                System.currentTimeMillis();
            }
			data.add(new TrainerAiData(new BytesDataContainer(GameFiles.TRAINER_AI_SCRIPTS, null, subfile)));
            i++;
        }

        return data;
    }

    @Override
    public Map<GameFiles, Narc> processDataList(List<TrainerAiData> data, Map<GameCodeBinaries, CodeBinary> codeBinaries)
    {

        ArrayList<byte[]> subfiles = new ArrayList<>();
        for (TrainerAiData trainerAiData : data)
        {
            subfiles.add(trainerAiData.save().get(GameFiles.TRAINER_AI_SCRIPTS, null));
        }

        return Collections.singletonMap(GameFiles.TRAINER_AI_SCRIPTS, Narc.fromContentsAndNames(subfiles, new Fnt.Folder(), Endianness.EndiannessType.BIG));
    }

    @Override
    public List<GameFiles> getRequirements()
    {
        return Collections.singletonList(GameFiles.TRAINER_AI_SCRIPTS);
    }

    @Override
    public List<GameCodeBinaries> getRequiredBinaries()
    {
        return List.of(GameCodeBinaries.ARM9);
    }

    static final int SCRIPT_MAGIC_ID = 0xFD13;
}
