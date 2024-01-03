package io.github.turtleisaac.pokeditor.formats;

import io.github.turtleisaac.nds4j.Narc;
import io.github.turtleisaac.nds4j.NintendoDsRom;
import io.github.turtleisaac.nds4j.binaries.CodeBinary;
import io.github.turtleisaac.pokeditor.gamedata.*;
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
        Game game = Game.parseBaseRom(rom.getGameCode());
        GameFiles.initialize(game);
        TextFiles.initialize(game);
        GameCodeBinaries.initialize(game);
        Tables.initialize(game);
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

        HashMap<GameCodeBinaries, CodeBinary> codeBinaries = new HashMap<>();
        codeBinaries.put(GameCodeBinaries.ARM9, rom.loadArm9());

        List<E> data = parser.generateDataList(map, codeBinaries);
        Map<GameFiles, Narc> output = parser.processDataList(data, codeBinaries);

        for (GameFiles gameFile : parser.getRequirements()) {
            Narc originalNarc = map.get(gameFile);
            Narc outputNarc = output.get(gameFile);
            for (int idx = 0; idx < originalNarc.getFiles().size(); idx++) {
                System.out.println(idx);
                assertThat(outputNarc.getFile(idx))
                        .isEqualTo(Arrays.copyOf(originalNarc.getFile(idx), outputNarc.getFile(idx).length));
            }
        }
    }
}
