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

package io.github.turtleisaac.pokeditor.formats;

import io.github.turtleisaac.nds4j.Narc;

import java.util.List;
import java.util.Map;

public interface GenericParser<E extends GenericFileData>
{
    List<E> generateDataList(Map<GameFiles, Narc> narcs);

    Map<GameFiles, Narc> processDataList(List<E> data);

    List<GameFiles> getRequirements();
}

