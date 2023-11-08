package io.github.turtleisaac.pokeditor.formats.pokemon_sprites;

import io.github.turtleisaac.nds4j.Narc;
import io.github.turtleisaac.nds4j.images.Palette;
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.formats.GenericParser;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.turtleisaac.pokeditor.formats.pokemon_sprites.PokemonSpriteData.BattleSpriteNarcPattern.*;

public class PokemonSpriteParser implements GenericParser<PokemonSpriteData>
{

    @Override
    public List<PokemonSpriteData> generateDataList(Map<GameFiles, Narc> narcs)
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

        Narc sprites = narcs.get(GameFiles.BATTLE_SPRITES);
        Narc spriteHeights = narcs.get(GameFiles.BATTLE_SPRITE_HEIGHT);
        Narc spriteMetadata = narcs.get(GameFiles.BATTLE_SPRITE_METADATA);
        Narc partyIcons = narcs.get(GameFiles.PARTY_ICONS);
        ArrayList<PokemonSpriteData> data = new ArrayList<>();

        Palette partyIconPalette = new Palette(partyIcons.getFile(0), 4);

        PokemonSpriteData.BattleSpriteNarcPattern[] pattern = values();
        for (int i = 0; i < sprites.getFiles().size() / pattern.length; i++)
        {
            System.out.println(i);
            BytesDataContainer container = new BytesDataContainer();
            for (PokemonSpriteData.BattleSpriteNarcPattern entry : pattern)
            {
                container.insert(GameFiles.BATTLE_SPRITES, entry, sprites.getFile((i*pattern.length) + entry.getIndex()));
            }
            container.insert(GameFiles.PARTY_ICONS, null, partyIcons.getFile(i + 7));

            PokemonSpriteData species = new PokemonSpriteData(container);
            species.getPartyIcon().setPalette(partyIconPalette);

            data.add(species);


//            HashMap<GameFiles, byte[]> map = new HashMap<>();
//            map.put(GameFiles.TRAINER_DATA, trainerData.getFile(i));
//            map.put(GameFiles.TRAINER_POKEMON, trainerPokemon.getFile(i));
//            data.add(new TrainerData(map));
        }

        return data;
    }

    @Override
    public Map<GameFiles, Narc> processDataList(List<PokemonSpriteData> data)
    {
        return null;
    }

    @Override
    public List<GameFiles> getRequirements()
    {
        return null;
    }
}
