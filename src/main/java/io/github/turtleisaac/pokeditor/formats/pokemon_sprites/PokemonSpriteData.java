package io.github.turtleisaac.pokeditor.formats.pokemon_sprites;

import io.github.turtleisaac.nds4j.images.IndexedImage;
import io.github.turtleisaac.nds4j.images.Palette;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;

public class PokemonSpriteData implements GenericFileData
{
    private Palette palette;
    private Palette shinyPalette;
    private IndexedImage femaleBack;
    private IndexedImage maleBack;
    private IndexedImage femaleFront;
    private IndexedImage maleFront;

    private IndexedImage partyIcon;

    private boolean displayingShiny;

    public PokemonSpriteData(BytesDataContainer files)
    {
        setData(files);
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

        palette = new Palette(paletteFile, 4);
        shinyPalette = new Palette(shinyPaletteFile, 4);

        if (femaleBackFile.length != 0)
            femaleBack = new IndexedImage(femaleBackFile, 0, 4, 1, 1, true);
        if (maleBackFile.length != 0)
            maleBack = new IndexedImage(maleBackFile, 0, 4, 1, 1, true);
        if (femaleFrontFile.length != 0)
            femaleFront = new IndexedImage(femaleFrontFile, 0, 4, 1, 1, true);
        if (maleFrontFile.length != 0)
            maleFront = new IndexedImage(maleFrontFile, 0, 4, 1, 1, true);

        partyIcon = new IndexedImage(partyIconFile, 4, 0, 1, 1, true);

        toggleShinyPalette(false);
    }

    @Override
    public BytesDataContainer save()
    {
        return null;
    }

    public void toggleShinyPalette(boolean mode)
    {
        displayingShiny = mode;
        Palette palette = mode ? shinyPalette : this.palette;
        if (femaleBack != null)
            femaleBack.setPalette(palette);
        if (maleBack != null)
            maleBack.setPalette(palette);
        if (femaleFront != null)
            femaleFront.setPalette(palette);
        if (maleFront != null)
            maleFront.setPalette(palette);
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
}
