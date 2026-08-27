package io.github.turtleisaac.pokeditor.gamedata;

import static io.github.turtleisaac.pokeditor.gamedata.TextFiles.SpecificTextBanks.*;

public enum TextFiles
{
    ITEM_NAMES,
    SPECIES_NAMES,
    ABILITY_NAMES,
    MOVE_NAMES,
    TRAINER_NAMES,
    TRAINER_CLASS_NAMES,
    TRAINER_TEXT,
    TYPE_NAMES;

    // 0 is a valid bank index, so an unknown bank has to be represented by something which isn't
    static final int UNKNOWN_BANK = -1;

    private int value;

    public int getValue()
    {
        if (value == UNKNOWN_BANK)
        {
            throw new IllegalStateException("The " + name() + " text bank index is not known for the base ROM which is currently loaded");
        }
        return value;
    }

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
                TYPE_NAMES.value = DP_TYPE_NAMES.value;
            }
            case Platinum -> {
                ITEM_NAMES.value = PLAT_ITEM_NAMES.value;
                SPECIES_NAMES.value = PLAT_SPECIES_NAMES.value;
                ABILITY_NAMES.value = PLAT_ABILITY_NAMES.value;
                MOVE_NAMES.value = PLAT_MOVE_NAMES.value;
                TRAINER_NAMES.value = PLAT_TRAINER_NAMES.value;
                TRAINER_CLASS_NAMES.value = PLAT_TRAINER_CLASS_NAMES.value;
                TRAINER_TEXT.value = PLAT_TRAINER_TEXT.value;
                TYPE_NAMES.value = PLAT_TYPE_NAMES.value;
            }
            case HeartGold, SoulSilver -> {
                ITEM_NAMES.value = HGSS_ITEM_NAMES.value;
                SPECIES_NAMES.value = HGSS_SPECIES_NAMES.value;
                ABILITY_NAMES.value = HGSS_ABILITY_NAMES.value;
                MOVE_NAMES.value = HGSS_MOVE_NAMES.value;
                TRAINER_NAMES.value = HGSS_TRAINER_NAMES.value;
                TRAINER_CLASS_NAMES.value = HGSS_TRAINER_CLASS_NAMES.value;
                TRAINER_TEXT.value = HGSS_TRAINER_TEXT.value;
                TYPE_NAMES.value = HGSS_TYPE_NAMES.value;
            }
            default -> throw new UnsupportedOperationException("Unsupported base ROM: " + baseROM);
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

        DP_TRAINER_TEXT(TextFiles.UNKNOWN_BANK), //TODO find the real bank index
        PLAT_TRAINER_TEXT(617), //TODO change
        HGSS_TRAINER_TEXT(728),

        DP_TYPE_NAMES(TextFiles.UNKNOWN_BANK), //TODO find the real bank index
        PLAT_TYPE_NAMES(624),
        HGSS_TYPE_NAMES(735);

        private final int value;

        SpecificTextBanks(int value) {this.value = value;}
    }
}
