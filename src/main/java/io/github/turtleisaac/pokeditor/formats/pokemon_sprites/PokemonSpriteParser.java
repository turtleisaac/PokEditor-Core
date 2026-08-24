package io.github.turtleisaac.pokeditor.formats.pokemon_sprites;

import io.github.turtleisaac.nds4j.Fnt;
import io.github.turtleisaac.nds4j.Narc;
import io.github.turtleisaac.nds4j.binaries.CodeBinary;
import io.github.turtleisaac.nds4j.binaries.MainCodeFile;
import io.github.turtleisaac.nds4j.framework.Endianness;
import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.nds4j.images.Palette;
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.formats.GenericParser;
import io.github.turtleisaac.pokeditor.gamedata.GameCodeBinaries;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
import io.github.turtleisaac.pokeditor.gamedata.Tables;

import java.util.*;

//import static io.github.turtleisaac.pokeditor.formats.pokemon_sprites.PokemonSpriteData.BattleSpriteNarcPattern.*;

public class PokemonSpriteParser implements GenericParser<PokemonSpriteData>
{

    public static final int MAX_PARTY_ICON_PALETTE_VALUE = 2;

    private static final int NUM_PARTY_ICON_STARTING_FILES = 7;

    // Instance state rather than static. As in PersonalParser, this is not what isolates one
    // ROM from another - the parser is a Guice singleton - it is simply where the state belongs.
    private List<byte[]> partyIconStartingFiles;
    private int partyIconPaletteTableLength = -1;

    @Override
    public List<PokemonSpriteData> generateDataList(Map<GameFiles, Narc> narcs, Map<GameCodeBinaries, CodeBinary> codeBinaries)
    {
        if (!narcs.containsKey(GameFiles.BATTLE_SPRITES))
        {
            throw new RuntimeException("Pokemon battle sprites narc not provided to editor");
        }

        if (!narcs.containsKey(GameFiles.BATTLE_SPRITE_HEIGHT))
        {
            throw new RuntimeException("Pokemon battle sprites height narc not provided to editor");
        }

        if (!narcs.containsKey(GameFiles.BATTLE_SPRITE_METADATA))
        {
            throw new RuntimeException("Pokemon battle sprites metadata narc not provided to editor");
        }

        if (!narcs.containsKey(GameFiles.PARTY_ICONS))
        {
            throw new RuntimeException("Pokemon party icons narc not provided to editor");
        }

        if (codeBinaries == null)
        {
            throw new RuntimeException("Code binaries not provided to editor");
        }

        if (!codeBinaries.containsKey(GameCodeBinaries.ARM9))
        {
            throw new RuntimeException("Arm9 not provided to editor");
        }

        MainCodeFile arm9 = (MainCodeFile) codeBinaries.get(GameCodeBinaries.ARM9);

        Narc sprites = narcs.get(GameFiles.BATTLE_SPRITES);
        Narc spriteHeights = narcs.get(GameFiles.BATTLE_SPRITE_HEIGHT);
        Narc spriteMetadata = narcs.get(GameFiles.BATTLE_SPRITE_METADATA);
        Narc partyIcons = narcs.get(GameFiles.PARTY_ICONS);
        ArrayList<PokemonSpriteData> data = new ArrayList<>();

        PokemonSpriteData.BattleSpriteNarcPattern[] spritesNarcPattern = PokemonSpriteData.BattleSpriteNarcPattern.values();
        PokemonSpriteData.BattleSpriteHeightOffsetsPattern[] spriteHeightOffsetsPattern = PokemonSpriteData.BattleSpriteHeightOffsetsPattern.values();

        int numSpecies = sprites.getFiles().size() / spritesNarcPattern.length;

        // read the entire party icon palette index table up front so the shared arm9 buffer is only
        // touched while its lock is held
        int[] partyIconPaletteIndices;
        arm9.lock();
        try {
            MemBuf.MemBufReader arm9Reader = arm9.getPhysicalAddressBuffer().reader();
            arm9Reader.setPosition(Tables.PARTY_ICON_PALETTE.getPointerOffset());
            int offset = arm9Reader.readInt();
            arm9Reader.setPosition(offset - arm9.getRamStartAddress());
            partyIconPaletteIndices = arm9Reader.readBytesI(numSpecies);
        }
        finally {
            arm9.unlock();
        }
        partyIconPaletteTableLength = partyIconPaletteIndices.length;

        Palette partyIconPalette = new Palette(partyIcons.getFile(0), 4);
        partyIconStartingFiles = new ArrayList<>();
        for (int i = 0; i < NUM_PARTY_ICON_STARTING_FILES; i++)
        {
            partyIconStartingFiles.add(partyIcons.getFile(i));
        }

        MemBuf spriteMetadataBuffer = MemBuf.create(spriteMetadata.getFile(0));
        MemBuf.MemBufReader spriteMetadataReader = spriteMetadataBuffer.reader();

        for (int i = 0; i < numSpecies; i++)
        {
            BytesDataContainer container = new BytesDataContainer();
            for (PokemonSpriteData.BattleSpriteNarcPattern entry : spritesNarcPattern)
            {
                container.insert(GameFiles.BATTLE_SPRITES, entry, sprites.getFile((i*spritesNarcPattern.length) + entry.getIndex()));
            }
            container.insert(GameFiles.PARTY_ICONS, null, partyIcons.getFile(i + NUM_PARTY_ICON_STARTING_FILES));
            container.insert(GameFiles.BATTLE_SPRITE_METADATA, null, spriteMetadataReader.readBytes(89));
            for (PokemonSpriteData.BattleSpriteHeightOffsetsPattern entry : spriteHeightOffsetsPattern)
            {
                container.insert(GameFiles.BATTLE_SPRITE_HEIGHT, entry, spriteHeights.getFile(i*spriteHeightOffsetsPattern.length + entry.getIndex()));
            }

            PokemonSpriteData species = new PokemonSpriteData(container);
            species.getPartyIcon().setPalette(partyIconPalette);
            // the raw value is stored even when it is out of the range the editor knows about, otherwise
            // simply loading and saving a ROM would rewrite the table
            species.setPartyIconPaletteIndex(partyIconPaletteIndices[i]);

            data.add(species);
        }

        return data;
    }

    @Override
    public Map<GameFiles, Narc> processDataList(List<PokemonSpriteData> data, Map<GameCodeBinaries, CodeBinary> codeBinaries)
    {
        if (codeBinaries == null)
        {
            throw new RuntimeException("Code binaries not provided to editor");
        }

        if (!codeBinaries.containsKey(GameCodeBinaries.ARM9))
        {
            throw new RuntimeException("Arm9 not provided to editor");
        }

        HashMap<GameFiles, Narc> outputMap = new HashMap<>();

        MainCodeFile arm9 = (MainCodeFile) codeBinaries.get(GameCodeBinaries.ARM9);
        arm9.lock();
        try {
            MemBuf.MemBufReader arm9Reader = arm9.getPhysicalAddressBuffer().reader();
            arm9Reader.setPosition(Tables.PARTY_ICON_PALETTE.getPointerOffset());
            int offset = arm9Reader.readInt();
            MemBuf.MemBufWriter arm9Writer = arm9.getPhysicalAddressBuffer().writer();
            arm9Writer.setPosition(offset - arm9.getRamStartAddress());

            ArrayList<byte[]> spritesSubfiles = new ArrayList<>();
            ArrayList<byte[]> heightsSubfiles = new ArrayList<>();
            ArrayList<byte[]> metadataSubfiles = new ArrayList<>();
            ArrayList<byte[]> partyIconSubfiles = new ArrayList<>();

            MemBuf spriteMetadataBuffer = MemBuf.create();
            MemBuf.MemBufWriter spriteMetadataWriter = spriteMetadataBuffer.writer();

            if (partyIconStartingFiles == null)
            {
                throw new IllegalStateException("The party icon narc's leading files are not known - generateDataList() must be run on this parser instance before processDataList()");
            }
            partyIconSubfiles.addAll(partyIconStartingFiles);

            for (int idx = 0; idx < data.size(); idx++)
            {
                PokemonSpriteData entry = data.get(idx);
                BytesDataContainer saveResults = entry.save();

                spritesSubfiles.add(saveResults.get(GameFiles.BATTLE_SPRITES, PokemonSpriteData.BattleSpriteNarcPattern.FEMALE_BACK));
                spritesSubfiles.add(saveResults.get(GameFiles.BATTLE_SPRITES, PokemonSpriteData.BattleSpriteNarcPattern.MALE_BACK));
                spritesSubfiles.add(saveResults.get(GameFiles.BATTLE_SPRITES, PokemonSpriteData.BattleSpriteNarcPattern.FEMALE_FRONT));
                spritesSubfiles.add(saveResults.get(GameFiles.BATTLE_SPRITES, PokemonSpriteData.BattleSpriteNarcPattern.MALE_FRONT));
                spritesSubfiles.add(saveResults.get(GameFiles.BATTLE_SPRITES, PokemonSpriteData.BattleSpriteNarcPattern.PALETTE));
                spritesSubfiles.add(saveResults.get(GameFiles.BATTLE_SPRITES, PokemonSpriteData.BattleSpriteNarcPattern.SHINY_PALETTE));

                heightsSubfiles.add(saveResults.get(GameFiles.BATTLE_SPRITE_HEIGHT, PokemonSpriteData.BattleSpriteHeightOffsetsPattern.FEMALE_BACK_Y));
                heightsSubfiles.add(saveResults.get(GameFiles.BATTLE_SPRITE_HEIGHT, PokemonSpriteData.BattleSpriteHeightOffsetsPattern.MALE_BACK_Y));
                heightsSubfiles.add(saveResults.get(GameFiles.BATTLE_SPRITE_HEIGHT, PokemonSpriteData.BattleSpriteHeightOffsetsPattern.FEMALE_FRONT_Y));
                heightsSubfiles.add(saveResults.get(GameFiles.BATTLE_SPRITE_HEIGHT, PokemonSpriteData.BattleSpriteHeightOffsetsPattern.MALE_FRONT_Y));

                spriteMetadataWriter.write(saveResults.get(GameFiles.BATTLE_SPRITE_METADATA, null));

                partyIconSubfiles.add(saveResults.get(GameFiles.PARTY_ICONS, null));
                // the arm9 table is a fixed size - writing more entries than it holds would run over
                // whatever follows it
                if (idx < partyIconPaletteTableLength)
                    arm9Writer.writeBytes(entry.getPartyIconPaletteIndex() & 0xff);
            }

            metadataSubfiles.add(spriteMetadataBuffer.reader().getBuffer());

            outputMap.put(GameFiles.BATTLE_SPRITES, Narc.fromContentsAndNames(spritesSubfiles, new Fnt.Folder(), Endianness.EndiannessType.BIG));
            outputMap.put(GameFiles.BATTLE_SPRITE_METADATA, Narc.fromContentsAndNames(metadataSubfiles, new Fnt.Folder(), Endianness.EndiannessType.BIG));
            outputMap.put(GameFiles.BATTLE_SPRITE_HEIGHT, Narc.fromContentsAndNames(heightsSubfiles, new Fnt.Folder(), Endianness.EndiannessType.BIG));
            outputMap.put(GameFiles.PARTY_ICONS, Narc.fromContentsAndNames(partyIconSubfiles, new Fnt.Folder(), Endianness.EndiannessType.BIG));
        }
        finally {
            arm9.unlock();
        }

        return outputMap;
    }

    @Override
    public List<GameFiles> getRequirements()
    {
        return Arrays.asList(GameFiles.BATTLE_SPRITES, GameFiles.BATTLE_SPRITE_HEIGHT, GameFiles.BATTLE_SPRITE_METADATA, GameFiles.PARTY_ICONS);
    }

    @Override
    public List<GameCodeBinaries> getRequiredBinaries()
    {
        return List.of(GameCodeBinaries.ARM9);
    }
}
