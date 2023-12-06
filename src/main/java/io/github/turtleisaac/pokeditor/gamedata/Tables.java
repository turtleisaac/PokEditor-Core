package io.github.turtleisaac.pokeditor.gamedata;

public enum Tables
{
    PARTY_ICON_PALETTE,
    TRAINER_CLASS_GENDER,
    TRAINER_CLASS_PRIZE_MONEY,
    ITEMS,
    TM_HM_MOVES
    ;

    private GameCodeBinaries pointerLocation;
    private int pointerOffset;

    public GameCodeBinaries getPointerLocation()
    {
        return pointerLocation;
    }

    public int getPointerOffset()
    {
        return pointerOffset;
    }

    public static void initialize(Game baseROM)
    {
        switch (baseROM) {
            case Platinum -> {
                PARTY_ICON_PALETTE.pointerLocation = GameCodeBinaries.ARM9;
                TRAINER_CLASS_GENDER.pointerLocation = GameCodeBinaries.ARM9;
                TRAINER_CLASS_PRIZE_MONEY.pointerLocation = GameCodeBinaries.BATTLE;
                TM_HM_MOVES.pointerLocation = GameCodeBinaries.ARM9;
                ITEMS.pointerLocation = GameCodeBinaries.ARM9;
                switch (baseROM.getRegion()) {
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
                switch (baseROM.getRegion()) {
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
                switch (baseROM.getRegion()) {
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
        }
    }
}
