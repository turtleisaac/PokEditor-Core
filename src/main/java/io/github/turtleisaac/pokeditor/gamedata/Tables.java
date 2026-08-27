package io.github.turtleisaac.pokeditor.gamedata;

import java.util.Objects;

public enum Tables
{
    PARTY_ICON_PALETTE,
    TRAINER_CLASS_GENDER,
    TRAINER_CLASS_PRIZE_MONEY,
    ITEMS,
    TM_HM_MOVES
    ;

    private static final int UNSET_POINTER_OFFSET = -1;

    private GameCodeBinaries pointerLocation;
    private int pointerOffset = UNSET_POINTER_OFFSET;

    public GameCodeBinaries getPointerLocation()
    {
        if (pointerLocation == null)
        {
            throw new IllegalStateException("The code binary containing the " + name() + " table is not known for the base ROM which is currently loaded");
        }
        return pointerLocation;
    }

    public int getPointerOffset()
    {
        if (pointerOffset == UNSET_POINTER_OFFSET)
        {
            throw new IllegalStateException("The offset of the " + name() + " table is not known for the base ROM which is currently loaded");
        }
        return pointerOffset;
    }


    public static void initialize(Game baseROM, Game.Region region)
    {
        Objects.requireNonNull(baseROM, "A base ROM must be provided in order to initialize the table pointers");
        Objects.requireNonNull(region, "The region of the base ROM must be provided in order to initialize the table pointers - it comes from Game.parseBaseRom()");

        // every constant is reset first, otherwise switching between ROMs leaves the previous ROM's
        // pointers in place for any table the new ROM does not assign
        for (Tables table : values())
        {
            table.pointerLocation = null;
            table.pointerOffset = UNSET_POINTER_OFFSET;
        }

        switch (baseROM) {
            case Platinum -> {
                PARTY_ICON_PALETTE.pointerLocation = GameCodeBinaries.ARM9;
                TRAINER_CLASS_GENDER.pointerLocation = GameCodeBinaries.ARM9;
                TRAINER_CLASS_PRIZE_MONEY.pointerLocation = GameCodeBinaries.BATTLE;
                TM_HM_MOVES.pointerLocation = GameCodeBinaries.ARM9;
                ITEMS.pointerLocation = GameCodeBinaries.ARM9;
                switch (region) {
                    case USA -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x079f80;
                        TRAINER_CLASS_PRIZE_MONEY.pointerOffset = 0x816c;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x793b4;
                        ITEMS.pointerOffset = 0x7cef4;
                        TM_HM_MOVES.pointerOffset = 0x07d288;
                    }
                    case GERMANY, FRANCE -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x07a020;
//                        TRAINER_CLASS_PRIZE_MONEY.pointerOffset = ;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x079454;
                        ITEMS.pointerOffset = 0x7cf94;
                        TM_HM_MOVES.pointerOffset = 0x7d328;
                    }
                    case EUROPE -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x079f80;
//                        TRAINER_CLASS_PRIZE_MONEY.pointerOffset = ;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x0793b4;
                        ITEMS.pointerOffset = 0x7cef4;
                        TM_HM_MOVES.pointerOffset = 0x7d288;
                    }
                    case SPAIN -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x07a020;
//                        TRAINER_CLASS_PRIZE_MONEY.pointerOffset = ;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x079454;
                        ITEMS.pointerOffset = 0x7cf94;
                        TM_HM_MOVES.pointerOffset = 0x7d328;
                    }
                    case ITALY -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x07a020;
//                        TRAINER_CLASS_PRIZE_MONEY.pointerOffset = ;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x079454;
                        ITEMS.pointerOffset = 0x7cf94;
                        TM_HM_MOVES.pointerOffset = 0x7d328;
                    }
                    case JAPAN -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x079858;
//                        TRAINER_CLASS_PRIZE_MONEY.pointerOffset = ;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x078c8c;
                        ITEMS.pointerOffset = 0x7c7c4;
                        TM_HM_MOVES.pointerOffset = 0x7cb58;
                    }
                    case KOREA -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x07a46c;
//                        TRAINER_CLASS_PRIZE_MONEY.pointerOffset = ;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x0798a0;
                        ITEMS.pointerOffset = 0x7d3e0;
                        TM_HM_MOVES.pointerOffset = 0x7d774;
                    }
                }
            }
            case HeartGold -> {
                PARTY_ICON_PALETTE.pointerLocation = GameCodeBinaries.ARM9;
                TRAINER_CLASS_GENDER.pointerLocation = GameCodeBinaries.ARM9;
                TM_HM_MOVES.pointerLocation = GameCodeBinaries.ARM9;
                ITEMS.pointerLocation = GameCodeBinaries.ARM9;
                switch (region) {
                    case USA -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074408;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073600;
                        ITEMS.pointerOffset = 0x77c94;
                        TM_HM_MOVES.pointerOffset = 0x078020;
                    }
                    case GERMANY, EUROPE, FRANCE, ITALY -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074408;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073600;
                        ITEMS.pointerOffset = 0x77c94;
                        TM_HM_MOVES.pointerOffset = 0x78020;
                    }
                    case SPAIN -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074400;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x0735f8;
                        ITEMS.pointerOffset = 0x77c8c;
                        TM_HM_MOVES.pointerOffset = 0x78018;
                    }
                    case JAPAN -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x073ea0;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073098;
                        ITEMS.pointerOffset = 0x77724;
                        TM_HM_MOVES.pointerOffset = 0x77ab0;
                    }
                    case KOREA -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074508;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073700;
                        ITEMS.pointerOffset = 0x77d94;
                        TM_HM_MOVES.pointerOffset = 0x78120;
                    }
                }
            }
            case SoulSilver -> {
                PARTY_ICON_PALETTE.pointerLocation = GameCodeBinaries.ARM9;
                TRAINER_CLASS_GENDER.pointerLocation = GameCodeBinaries.ARM9;
                TM_HM_MOVES.pointerLocation = GameCodeBinaries.ARM9;
                ITEMS.pointerLocation = GameCodeBinaries.ARM9;
                switch (region) {
                    case USA -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074408;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073600;
                        ITEMS.pointerOffset = 0x77c94;
                        TM_HM_MOVES.pointerOffset = 0x78020;
                    }
                    case GERMANY -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074408;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073600;
                        ITEMS.pointerOffset = 0x77c94;
                        TM_HM_MOVES.pointerOffset = 0x78020;
                    }
                    case EUROPE -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074408;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073600;
                        ITEMS.pointerOffset = 0x77c94;
                        TM_HM_MOVES.pointerOffset = 0x78020;
                    }
                    case FRANCE -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074408;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073600;
                        ITEMS.pointerOffset = 0x77c94;
                        TM_HM_MOVES.pointerOffset = 0x78020;
                    }
                    case SPAIN -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074408;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073600;
                        ITEMS.pointerOffset = 0x77c94;
                        TM_HM_MOVES.pointerOffset = 0x78020;
                    }
                    case ITALY -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074408;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073600;
                        ITEMS.pointerOffset = 0x77c94;
                        TM_HM_MOVES.pointerOffset = 0x78020;
                    }
                    case JAPAN -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x073ea0;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073098;
                        ITEMS.pointerOffset = 0x77724;
                        TM_HM_MOVES.pointerOffset = 0x77ab0;
                    }
                    case KOREA -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074500;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x0736f8;
                        ITEMS.pointerOffset = 0x77d8c;
                        TM_HM_MOVES.pointerOffset = 0x78118;
                    }
                }
            }
            default -> throw new UnsupportedOperationException("Unsupported base ROM: " + baseROM);
        }
    }
}
