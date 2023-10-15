package io.github.turtleisaac.pokeditor.gamedata;

import static io.github.turtleisaac.pokeditor.gamedata.TextBanks.SpecificTextBanks.*;

public enum TextBanks
{
    ITEM_NAMES,
    SPECIES_NAMES,
    ABILITY_NAMES,
    MOVE_NAMES,
    TRAINER_NAMES,
    TRAINER_CLASS_NAMES,
    TRAINER_TEXT;

    private int value;

    public int getValue() {return value;}

    public static void initialize(Game baseROM)
    {
        switch (baseROM) {
            case Diamond, Pearl -> {
                ITEM_NAMES.value = DP_ITEM_NAMES.value;
                SPECIES_NAMES.value = DP_SPECIES_NAMES.value;
                ABILITY_NAMES.value = DP_ABILITY_NAMES.value;
                MOVE_NAMES.value = DP_MOVE_NAMES.value;
                TRAINER_NAMES.value = DP_TRAINER_NAMES.value;
                TRAINER_CLASS_NAMES.value = DP_TRAINER_CLASS_NAMES.value;
                TRAINER_TEXT.value = DP_TRAINER_TEXT.value;
            }
            case Platinum -> {
                ITEM_NAMES.value = PLAT_ITEM_NAMES.value;
                SPECIES_NAMES.value = PLAT_SPECIES_NAMES.value;
                ABILITY_NAMES.value = PLAT_ABILITY_NAMES.value;
                MOVE_NAMES.value = PLAT_MOVE_NAMES.value;
                TRAINER_NAMES.value = PLAT_TRAINER_NAMES.value;
                TRAINER_CLASS_NAMES.value = PLAT_TRAINER_CLASS_NAMES.value;
                TRAINER_TEXT.value = PLAT_TRAINER_TEXT.value;
            }
            case HeartGold, SoulSilver -> {
                ITEM_NAMES.value = HGSS_ITEM_NAMES.value;
                SPECIES_NAMES.value = HGSS_SPECIES_NAMES.value;
                ABILITY_NAMES.value = HGSS_ABILITY_NAMES.value;
                MOVE_NAMES.value = HGSS_MOVE_NAMES.value;
                TRAINER_NAMES.value = HGSS_TRAINER_NAMES.value;
                TRAINER_CLASS_NAMES.value = HGSS_TRAINER_CLASS_NAMES.value;
                TRAINER_TEXT.value = HGSS_TRAINER_TEXT.value;
            }
        }
    }

    enum SpecificTextBanks
    {
        //    DP__NAMES(),
        //    PLAT__NAMES(),
        //    HGSS__NAMES()

        DP_ITEM_NAMES(344),
        PLAT_ITEM_NAMES(392),
        HGSS_ITEM_NAMES(222),

        DP_SPECIES_NAMES(362),
        PLAT_SPECIES_NAMES(412),
        HGSS_SPECIES_NAMES(237),

        DP_ABILITY_NAMES(552),
        PLAT_ABILITY_NAMES(610),
        HGSS_ABILITY_NAMES(720),

        DP_MOVE_NAMES(588),
        PLAT_MOVE_NAMES(647),
        HGSS_MOVE_NAMES(750),

        DP_TRAINER_NAMES(559),
        PLAT_TRAINER_NAMES(618),
        HGSS_TRAINER_NAMES(729),

        DP_TRAINER_CLASS_NAMES(560),
        PLAT_TRAINER_CLASS_NAMES(619),
        HGSS_TRAINER_CLASS_NAMES(730),

        DP_TRAINER_TEXT(0), //TODO change
        PLAT_TRAINER_TEXT(617), //TODO change
        HGSS_TRAINER_TEXT(728);

        private final int value;

        SpecificTextBanks(int value) {this.value = value;}
    }
}
