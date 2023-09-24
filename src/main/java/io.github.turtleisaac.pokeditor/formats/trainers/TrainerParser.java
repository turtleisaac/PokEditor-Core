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

package io.github.turtleisaac.pokeditor.formats.trainers;

import io.github.turtleisaac.nds4j.Fnt;
import io.github.turtleisaac.nds4j.Narc;
import io.github.turtleisaac.nds4j.framework.Endianness;
import io.github.turtleisaac.pokeditor.formats.GameFiles;
import io.github.turtleisaac.pokeditor.formats.GenericParser;

import java.util.*;

public class TrainerParser implements GenericParser<TrainerData>
{
    @Override
    public List<TrainerData> generateDataList(Map<GameFiles, Narc> narcs)
    {
        if (!narcs.containsKey(GameFiles.TRAINER_DATA))
        {
            throw new RuntimeException("Trainer data narc not provided to editor");
        }

        if (!narcs.containsKey(GameFiles.TRAINER_POKEMON))
        {
            throw new RuntimeException("Trainer pokemon narc not provided to editor");
        }

        Narc trainerData = narcs.get(GameFiles.TRAINER_DATA);
        Narc trainerPokemon = narcs.get(GameFiles.TRAINER_POKEMON);
        ArrayList<TrainerData> data = new ArrayList<>();

        for (int i = 0; i < trainerData.getFiles().size(); i++)
        {
            HashMap<GameFiles, byte[]> map = new HashMap<>();
            map.put(GameFiles.TRAINER_DATA, trainerData.getFile(i));
            map.put(GameFiles.TRAINER_POKEMON, trainerPokemon.getFile(i));
            data.add(new TrainerData(map));
        }

        return data;
    }

    @Override
    public Map<GameFiles, Narc> processDataList(List<TrainerData> data)
    {
        ArrayList<byte[]> trainerDataSubfiles = new ArrayList<>();
        ArrayList<byte[]> trainerPokemonSubfiles = new ArrayList<>();

        for(TrainerData trainer : data) {
            Map<GameFiles, byte[]> saveResults = trainer.save();

            trainerDataSubfiles.add(saveResults.get(GameFiles.TRAINER_DATA));
            trainerPokemonSubfiles.add(saveResults.get(GameFiles.TRAINER_POKEMON));
        }

        HashMap<GameFiles, Narc> map = new HashMap<>();
        map.put(GameFiles.TRAINER_DATA, Narc.fromContentsAndNames(trainerDataSubfiles, new Fnt.Folder(), Endianness.EndiannessType.BIG));
        map.put(GameFiles.TRAINER_POKEMON, Narc.fromContentsAndNames(trainerPokemonSubfiles, new Fnt.Folder(), Endianness.EndiannessType.BIG));

        return map;
    }

    @Override
    public List<GameFiles> getRequirements()
    {
        return Arrays.asList(GameFiles.TRAINER_DATA, GameFiles.TRAINER_POKEMON);
    }

    public enum TrainerSheets {
        TRAINER_DATA,
        TRAINER_POKEMON
    }
}
