package io.github.turtleisaac.pokeditor.formats;

import io.github.turtleisaac.nds4j.Narc;
import io.github.turtleisaac.nds4j.NintendoDsRom;
import io.github.turtleisaac.pokeditor.gamedata.Game;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

abstract class GenericParserTest<E extends GenericFileData>
{
    protected GenericParser<E> parser;
    protected NintendoDsRom rom;

    protected abstract GenericParser<E> createParser();

    @BeforeEach
    protected void setup()
    {
        parser = createParser();
        rom = NintendoDsRom.fromFile("Platinum.nds");
        GameFiles.initialize(Game.Platinum);
    }

    @Test
    void parserNotNull() {
        assertThat(parser)
                .isNotNull();
    }

    @Test
    void outputMatchesInput() {
        HashMap<GameFiles, Narc> map = new HashMap<>();
        for (GameFiles gameFile : parser.getRequirements()) {
            map.put(gameFile, new Narc(rom.getFileByName(gameFile.getPath())));
        }

        List<E> data = parser.generateDataList(map);
        Map<GameFiles, Narc> output = parser.processDataList(data);

        for (GameFiles gameFile : parser.getRequirements()) {
            Narc originalNarc = map.get(gameFile);
            Narc outputNarc = output.get(gameFile);
            for (int idx = 0; idx < originalNarc.getFiles().size(); idx++) {
                assertThat(outputNarc.getFile(idx))
                        .isEqualTo(originalNarc.getFile(idx));
            }
        }
    }
}
