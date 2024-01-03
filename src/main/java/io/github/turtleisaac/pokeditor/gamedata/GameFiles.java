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

package io.github.turtleisaac.pokeditor.gamedata;

public enum GameFiles
{
    PERSONAL,
    LEVEL_UP_LEARNSETS,
    EVOLUTIONS,
    BABY_FORMS,
    MOVES,
    ITEMS,
    ENCOUNTERS,
    TRAINER_POKEMON,
    TRAINER_DATA,
    TEXT,
    BATTLE_SPRITE_METADATA,
    BATTLE_SPRITE_HEIGHT,
    TRAINER_TEXT_ASSIGNMENT,
    TRAINER_TEXT_OFFSET,
    PARTY_ICONS,
    BATTLE_SPRITES,
    TRAINER_SPRITES,
    SCRIPTS,
	TR_AI,
    EVENTS
    ;

    private String path;

    public String getPath()
    {
        return path;
    }

    public static void initialize(Game baseROM)
    {
        switch (baseROM) {
            case Platinum -> {
                PERSONAL.path = "poketool/personal/pl_personal.narc";
                LEVEL_UP_LEARNSETS.path = "poketool/personal/wotbl.narc";
                EVOLUTIONS.path = "poketool/personal/evo.narc";
                BABY_FORMS.path = "poketool/personal/pms.narc";
                MOVES.path = "poketool/waza/pl_waza_tbl.narc";
                ITEMS.path = "itemtool/itemdata/pl_item_data.narc";
                ENCOUNTERS.path = "fielddata/encountdata/pl_enc_data.narc";
                TRAINER_POKEMON.path = "poketool/trainer/trpoke.narc";
                TRAINER_DATA.path = "poketool/trainer/trdata.narc";
                TEXT.path = "msgdata/pl_msg.narc";
                BATTLE_SPRITE_METADATA.path = "poketool/poke_edit/pl_poke_data.narc";
                BATTLE_SPRITE_HEIGHT.path = "poketool/pokegra/height.narc";
                TRAINER_TEXT_ASSIGNMENT.path = "poketool/trmsg/trtbl.narc";
                TRAINER_TEXT_OFFSET.path = "poketool/trmsg/trtblofs.narc";
                PARTY_ICONS.path = "poketool/icongra/pl_poke_icon.narc";
                BATTLE_SPRITES.path = "poketool/pokegra/pl_pokegra.narc";
//   todo             TRAINER_SPRITES.path =
                SCRIPTS.path = "fielddata/script/scr_seq.narc";
				TR_AI.path = "battle/tr_ai/tr_ai_seq.narc";
            }
            case HeartGold, SoulSilver -> {
                PERSONAL.path = "a/0/0/2";
                LEVEL_UP_LEARNSETS.path = "a/0/3/3";
                EVOLUTIONS.path = "a/0/3/4";
                BABY_FORMS.path = "poketool/personal/pms.narc";
                MOVES.path = "a/0/1/1";
                ITEMS.path = "a/0/1/7";
                TRAINER_POKEMON.path = "a/0/5/6";
                TRAINER_DATA.path = "a/0/5/5";
                TEXT.path = "a/0/2/7";
                BATTLE_SPRITE_METADATA.path = "a/1/8/0";
                BATTLE_SPRITE_HEIGHT.path = "a/0/0/5";
                TRAINER_TEXT_ASSIGNMENT.path = "a/0/5/7";
                TRAINER_TEXT_OFFSET.path = "a/1/3/1";
                PARTY_ICONS.path = "a/0/2/0";
                BATTLE_SPRITES.path = "a/0/0/4";
                TRAINER_SPRITES.path = "a/0/5/8";
                if (baseROM.equals(Game.HeartGold)) {
                    ENCOUNTERS.path = "a/0/3/7";
                }
                else {
                    ENCOUNTERS.path = "a/1/3/6";
                }
                SCRIPTS.path = "a/0/1/2";
				TR_AI.path = "a/0/9/9";
            }
        }
    }
}
