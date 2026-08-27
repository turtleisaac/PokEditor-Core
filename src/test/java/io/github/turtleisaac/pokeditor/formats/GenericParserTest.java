package io.github.turtleisaac.pokeditor.formats;

import io.github.turtleisaac.nds4j.Narc;
import io.github.turtleisaac.nds4j.NintendoDsRom;
import io.github.turtleisaac.nds4j.binaries.CodeBinary;
import io.github.turtleisaac.pokeditor.gamedata.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

abstract class GenericParserTest<E extends GenericFileData>
{
    /**
     * The directory the test ROMs live in. These are copyrighted files which cannot be committed, so the
     * suite skips rather than fails when they are not present.
     */
    static final String ROM_DIRECTORY_PROPERTY = "rom.dir";

    static final String PLATINUM_ROM_PROPERTY = "rom.platinum";
    static final String HEARTGOLD_ROM_PROPERTY = "rom.heartgold";

    static final String DEFAULT_PLATINUM_ROM = "Platinum.nds";
    static final String DEFAULT_HEARTGOLD_ROM = "HeartGold.nds";

    protected GenericParser<E> parser;
    protected NintendoDsRom rom;

    protected abstract GenericParser<E> createParser();

    /**
     * The name of the ROM file this test needs. Override this to run against a different game.
     * @return a <code>String</code>
     */
    protected String romFileName()
    {
        return System.getProperty(PLATINUM_ROM_PROPERTY, DEFAULT_PLATINUM_ROM);
    }

    @BeforeEach
    protected void setup()
    {
        parser = createParser();
    }

    /**
     * Loads the ROM this test needs, skipping the test if it is not present rather than failing the build
     */
    protected void loadRom()
    {
        String fileName = romFileName();
        Path path = Path.of(System.getProperty(ROM_DIRECTORY_PROPERTY, ".")).resolve(fileName);
        Assumptions.assumeTrue(Files.exists(path),
                () -> "Skipping - no ROM at " + path.toAbsolutePath() + ". Provide one with -D" + ROM_DIRECTORY_PROPERTY + "=<directory>");

        rom = NintendoDsRom.fromFile(path.toString());
        Game.BaseRomInfo baseRomInfo = Game.parseBaseRom(rom.getGameCode());
        GameFiles.initialize(baseRomInfo.game());
        TextFiles.initialize(baseRomInfo.game());
        GameCodeBinaries.initialize(baseRomInfo.game());
        Tables.initialize(baseRomInfo.game(), baseRomInfo.region());
    }

    @Test
    void parserNotNull() {
        assertThat(parser)
                .isNotNull();
    }

    @Test
    void outputMatchesInput() {
        loadRom();

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

            assertThat(outputNarc)
                    .as("no output narc was produced for %s", gameFile)
                    .isNotNull();

            assertThat(outputNarc.getFiles().size())
                    .as("the number of subfiles in the %s narc changed", gameFile)
                    .isEqualTo(originalNarc.getFiles().size());

            for (int idx = 0; idx < originalNarc.getFiles().size(); idx++) {
                assertThat(outputNarc.getFile(idx))
                        .as("subfile %d of the %s narc", idx, gameFile)
                        .isEqualTo(originalNarc.getFile(idx));
            }
        }
    }
}
