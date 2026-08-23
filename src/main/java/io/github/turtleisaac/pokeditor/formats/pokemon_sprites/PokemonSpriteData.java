package io.github.turtleisaac.pokeditor.formats.pokemon_sprites;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.nds4j.images.IndexedImage;
import io.github.turtleisaac.nds4j.images.Palette;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.gamedata.Game;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;

import java.util.Objects;

public class PokemonSpriteData implements GenericFileData
{
    public static final int BATTLE_SPRITE_HEIGHT = 80;
    public static final int BATTLE_SPRITE_WIDTH = 80;

    public static final int PARTY_ICON_HEIGHT = 32;
    public static final int PARTY_ICON_WIDTH = 32;

    private Palette palette;
    private Palette shinyPalette;
    private IndexedImage femaleBack;
    private IndexedImage maleBack;
    private IndexedImage femaleFront;
    private IndexedImage maleFront;

    private IndexedImage partyIcon;

    private int femaleBackOffset;
    private int maleBackOffset;
    private int femaleFrontOffset;
    private int maleFrontOffset;

    private int unknownByte;
    private int movement;
    private byte[] unknownSection1;
    private int backMovement;
    private byte[] unknownSection2;
    private int globalFrontYOffset;
    private int shadowXOffset;
    private int shadowSize;

    private int partyIconPaletteIndex = 0;

    private final boolean scanFrontToBack;

    private boolean femaleBackOffsetEmpty;
    private boolean maleBackOffsetEmpty;
    private boolean femaleFrontOffsetEmpty;
    private boolean maleFrontOffsetEmpty;

    private boolean paletteEmpty;
    private boolean shinyPaletteEmpty;

    /**
     * Creates a <code>PokemonSpriteData</code> for a game which scans its sprites front-to-back
     * (Platinum, HeartGold and SoulSilver - the games this module supports)
     * @param files a <code>BytesDataContainer</code>
     */
    public PokemonSpriteData(BytesDataContainer files)
    {
        this(files, true);
    }

    /**
     * Creates a <code>PokemonSpriteData</code>, deriving the sprite scan direction from the given game
     * @param files a <code>BytesDataContainer</code>
     * @param game a <code>Game</code>
     */
    public PokemonSpriteData(BytesDataContainer files, Game game)
    {
        this(files, scanDirectionFor(game));
    }

    private PokemonSpriteData(BytesDataContainer files, boolean scanFrontToBack)
    {
        this.scanFrontToBack = scanFrontToBack;
        setData(files);
    }

    /**
     * Gets whether the given game scans its sprite data front-to-back
     * @param game a <code>Game</code>
     * @return a <code>boolean</code>
     */
    public static boolean scanDirectionFor(Game game)
    {
        Objects.requireNonNull(game, "A game must be provided in order to determine the sprite scan direction");
        return switch (game) {
            case Platinum, HeartGold, SoulSilver -> true;
            // Diamond and Pearl seed their scanned sprite decoding from the last halfword rather than the first
            case Diamond, Pearl -> false;
        };
    }

    @Override
    public void setData(BytesDataContainer files)
    {
        byte[] paletteFile = files.get(GameFiles.BATTLE_SPRITES, BattleSpriteNarcPattern.PALETTE);
        byte[] shinyPaletteFile = files.get(GameFiles.BATTLE_SPRITES, BattleSpriteNarcPattern.SHINY_PALETTE);
        byte[] femaleBackFile = files.get(GameFiles.BATTLE_SPRITES, BattleSpriteNarcPattern.FEMALE_BACK);
        byte[] maleBackFile = files.get(GameFiles.BATTLE_SPRITES, BattleSpriteNarcPattern.MALE_BACK);
        byte[] femaleFrontFile = files.get(GameFiles.BATTLE_SPRITES, BattleSpriteNarcPattern.FEMALE_FRONT);
        byte[] maleFrontFile = files.get(GameFiles.BATTLE_SPRITES, BattleSpriteNarcPattern.MALE_FRONT);
        byte[] partyIconFile = files.get(GameFiles.PARTY_ICONS, null);

        byte[] metadata = files.get(GameFiles.BATTLE_SPRITE_METADATA, null);
        byte[] femaleBackHeightOffsetFile = files.get(GameFiles.BATTLE_SPRITE_HEIGHT, BattleSpriteHeightOffsetsPattern.FEMALE_BACK_Y);
        byte[] maleBackHeightOffsetFile = files.get(GameFiles.BATTLE_SPRITE_HEIGHT, BattleSpriteHeightOffsetsPattern.MALE_BACK_Y);
        byte[] femaleFrontHeightOffsetFile = files.get(GameFiles.BATTLE_SPRITE_HEIGHT, BattleSpriteHeightOffsetsPattern.FEMALE_FRONT_Y);
        byte[] maleFrontHeightOffsetFile = files.get(GameFiles.BATTLE_SPRITE_HEIGHT, BattleSpriteHeightOffsetsPattern.MALE_FRONT_Y);

        paletteEmpty = paletteFile.length == 0;
        shinyPaletteEmpty = shinyPaletteFile.length == 0;

        if (!paletteEmpty)
            palette = new Palette(paletteFile, 4);
        if (!shinyPaletteEmpty)
            shinyPalette = new Palette(shinyPaletteFile, 4);

        if (femaleBackFile.length != 0)
            femaleBack = new IndexedImage(femaleBackFile, 0, 0, 1, 1, scanFrontToBack);
        if (maleBackFile.length != 0)
            maleBack = new IndexedImage(maleBackFile, 0, 0, 1, 1, scanFrontToBack);
        if (femaleFrontFile.length != 0)
            femaleFront = new IndexedImage(femaleFrontFile, 0, 0, 1, 1, scanFrontToBack);
        if (maleFrontFile.length != 0)
            maleFront = new IndexedImage(maleFrontFile, 0, 0, 1, 1, scanFrontToBack);

        femaleBackOffsetEmpty = femaleBackHeightOffsetFile.length == 0;
        maleBackOffsetEmpty = maleBackHeightOffsetFile.length == 0;
        femaleFrontOffsetEmpty = femaleFrontHeightOffsetFile.length == 0;
        maleFrontOffsetEmpty = maleFrontHeightOffsetFile.length == 0;

        if (!femaleBackOffsetEmpty)
            femaleBackOffset = -(femaleBackHeightOffsetFile[0] & 0xff);
        if (!maleBackOffsetEmpty)
            maleBackOffset = -(maleBackHeightOffsetFile[0] & 0xff);
        if (!femaleFrontOffsetEmpty)
            femaleFrontOffset = -(femaleFrontHeightOffsetFile[0] & 0xff);
        if (!maleFrontOffsetEmpty)
            maleFrontOffset = -(maleFrontHeightOffsetFile[0] & 0xff);

        MemBuf buffer = MemBuf.create(metadata);
        MemBuf.MemBufReader reader = buffer.reader();

        unknownByte = reader.readByte(); //byte 0
        movement = reader.readUInt8(); //byte 1
        unknownSection1 = reader.readBytes(42); //bytes 2-43
        backMovement = reader.readUInt8(); //byte 44
        unknownSection2 = reader.readBytes(41); //bytes 45-85
        globalFrontYOffset = reader.readByte(); //byte 86
        shadowXOffset = reader.readByte(); //byte 87
        shadowSize = reader.readUInt8(); //byte 88

        partyIcon = new IndexedImage(partyIconFile, 4, 0, 1, 1, scanFrontToBack);
    }

    @Override
    public BytesDataContainer save()
    {
        BytesDataContainer container = new BytesDataContainer();
        container.insert(GameFiles.BATTLE_SPRITES, BattleSpriteNarcPattern.FEMALE_BACK, femaleBack != null ? femaleBack.save() : new byte[] {});
        container.insert(GameFiles.BATTLE_SPRITES, BattleSpriteNarcPattern.MALE_BACK, maleBack != null ? maleBack.save() : new byte[] {});
        container.insert(GameFiles.BATTLE_SPRITES, BattleSpriteNarcPattern.FEMALE_FRONT, femaleFront != null ? femaleFront.save() : new byte[] {});
        container.insert(GameFiles.BATTLE_SPRITES, BattleSpriteNarcPattern.MALE_FRONT, maleFront != null ? maleFront.save() : new byte[] {});
        container.insert(GameFiles.BATTLE_SPRITES, BattleSpriteNarcPattern.PALETTE, palette != null ? palette.save() : new byte[] {});
        container.insert(GameFiles.BATTLE_SPRITES, BattleSpriteNarcPattern.SHINY_PALETTE, shinyPalette != null ? shinyPalette.save() : new byte[] {});

        container.insert(GameFiles.PARTY_ICONS, null, partyIcon != null ? partyIcon.save() : new byte[] {});

        container.insert(GameFiles.BATTLE_SPRITE_HEIGHT, BattleSpriteHeightOffsetsPattern.FEMALE_BACK_Y, heightOffsetFile(femaleBackOffset, femaleBackOffsetEmpty));
        container.insert(GameFiles.BATTLE_SPRITE_HEIGHT, BattleSpriteHeightOffsetsPattern.MALE_BACK_Y, heightOffsetFile(maleBackOffset, maleBackOffsetEmpty));
        container.insert(GameFiles.BATTLE_SPRITE_HEIGHT, BattleSpriteHeightOffsetsPattern.FEMALE_FRONT_Y, heightOffsetFile(femaleFrontOffset, femaleFrontOffsetEmpty));
        container.insert(GameFiles.BATTLE_SPRITE_HEIGHT, BattleSpriteHeightOffsetsPattern.MALE_FRONT_Y, heightOffsetFile(maleFrontOffset, maleFrontOffsetEmpty));

        MemBuf buffer = MemBuf.create();
        MemBuf.MemBufWriter writer = buffer.writer();

        writer.write((byte) unknownByte, (byte) movement); //bytes 0, 1
        writer.write(unknownSection1); //bytes 2-43
        writer.write((byte) backMovement); //byte 44
        writer.write(unknownSection2); //bytes 45-85
        writer.write((byte) globalFrontYOffset, (byte) shadowXOffset, (byte) shadowSize); //bytes 86, 87, 88

        //todo work on party icon palette idx
        container.insert(GameFiles.BATTLE_SPRITE_METADATA, null, buffer.reader().getBuffer());

        return container;
    }

    /**
     * Produces the height offset subfile for the given offset. A zero-length entry in the ROM has to stay
     * zero-length, and the stored byte is the exact inverse of what <code>setData</code> reads, which
     * negates it.
     *
     * @param offset an <code>int</code> containing the height offset
     * @param wasEmpty a <code>boolean</code> containing whether the entry this came from was zero-length
     * @return a <code>byte[]</code>
     */
    private static byte[] heightOffsetFile(int offset, boolean wasEmpty)
    {
        if (wasEmpty)
            return new byte[0];
        return new byte[] {(byte) (-offset)};
    }


    public Palette getPalette()
    {
        return palette;
    }

    public void setPalette(Palette palette)
    {
        this.palette = palette;
    }

    public Palette getShinyPalette()
    {
        return shinyPalette;
    }

    public void setShinyPalette(Palette shinyPalette)
    {
        this.shinyPalette = shinyPalette;
    }

    public IndexedImage getFemaleBack()
    {
        return femaleBack;
    }

    public void setFemaleBack(IndexedImage femaleBack)
    {
        this.femaleBack = femaleBack;
    }

    public IndexedImage getMaleBack()
    {
        return maleBack;
    }

    public void setMaleBack(IndexedImage maleBack)
    {
        this.maleBack = maleBack;
    }

    public IndexedImage getFemaleFront()
    {
        return femaleFront;
    }

    public void setFemaleFront(IndexedImage femaleFront)
    {
        this.femaleFront = femaleFront;
    }

    public IndexedImage getMaleFront()
    {
        return maleFront;
    }

    public void setMaleFront(IndexedImage maleFront)
    {
        this.maleFront = maleFront;
    }

    public IndexedImage getPartyIcon()
    {
        return partyIcon;
    }

    public void setPartyIcon(IndexedImage partyIcon)
    {
        this.partyIcon = partyIcon;
    }

    public int getFemaleBackOffset()
    {
        return femaleBackOffset;
    }

    public void setFemaleBackOffset(int femaleBackOffset)
    {
        this.femaleBackOffset = femaleBackOffset;
    }

    public int getMaleBackOffset()
    {
        return maleBackOffset;
    }

    public void setMaleBackOffset(int maleBackOffset)
    {
        this.maleBackOffset = maleBackOffset;
    }

    public int getFemaleFrontOffset()
    {
        return femaleFrontOffset;
    }

    public void setFemaleFrontOffset(int femaleFrontOffset)
    {
        this.femaleFrontOffset = femaleFrontOffset;
    }

    public int getMaleFrontOffset()
    {
        return maleFrontOffset;
    }

    public void setMaleFrontOffset(int maleFrontOffset)
    {
        this.maleFrontOffset = maleFrontOffset;
    }

    public int getMovement()
    {
        return movement;
    }

    public void setMovement(int movement)
    {
        this.movement = movement;
    }

    public int getBackMovement()
    {
        return backMovement;
    }

    public void setBackMovement(int backMovement)
    {
        this.backMovement = backMovement;
    }

    public int getGlobalFrontYOffset()
    {
        return globalFrontYOffset;
    }

    public void setGlobalFrontYOffset(int globalFrontYOffset)
    {
        this.globalFrontYOffset = globalFrontYOffset;
    }

    public int getShadowXOffset()
    {
        return shadowXOffset;
    }

    public void setShadowXOffset(int shadowXOffset)
    {
        this.shadowXOffset = shadowXOffset;
    }

    public int getShadowSize()
    {
        return shadowSize;
    }

    public void setShadowSize(int shadowSize)
    {
        this.shadowSize = shadowSize;
    }

    public int getPartyIconPaletteIndex()
    {
        return partyIconPaletteIndex;
    }

    public void setPartyIconPaletteIndex(int partyIconPaletteIndex)
    {
        this.partyIconPaletteIndex = partyIconPaletteIndex;
    }

    public enum BattleSpriteNarcPattern implements BytesDataContainer.PatternIndex
    {
        FEMALE_BACK {
            @Override
            public int getIndex()
            {
                return 0;
            }
        },
        MALE_BACK {
            @Override
            public int getIndex()
            {
                return 1;
            }
        },
        FEMALE_FRONT {
            @Override
            public int getIndex()
            {
                return 2;
            }
        },
        MALE_FRONT {
            @Override
            public int getIndex()
            {
                return 3;
            }
        },
        PALETTE {
            @Override
            public int getIndex()
            {
                return 4;
            }
        },
        SHINY_PALETTE {
            @Override
            public int getIndex()
            {
                return 5;
            }
        }
    }

    public enum BattleSpriteHeightOffsetsPattern implements BytesDataContainer.PatternIndex
    {
        FEMALE_BACK_Y {
            @Override
            public int getIndex()
            {
                return 0;
            }
        },
        MALE_BACK_Y {
            @Override
            public int getIndex()
            {
                return 1;
            }
        },
        FEMALE_FRONT_Y {
            @Override
            public int getIndex()
            {
                return 2;
            }
        },
        MALE_FRONT_Y {
            @Override
            public int getIndex()
            {
                return 3;
            }
        }
    }
}
