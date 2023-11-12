package io.github.turtleisaac.pokeditor.gamedata;

public enum Tables
{
    PARTY_ICON_PALETTE,
    TRAINER_CLASS_GENDER,
    TRAINER_CLASS_PRIZE_MONEY,
    ITEMS
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
                ITEMS.pointerLocation = GameCodeBinaries.ARM9;
                switch (baseROM.getRegion()) {
                    case USA -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x079f80;
                        TRAINER_CLASS_PRIZE_MONEY.pointerOffset = 0x816c;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x793b4;
                    }
                    case GERMANY -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x07a020;
//                        TRAINER_CLASS_PRIZE_MONEY.pointerOffset = ;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x079454;
                    }
                    case EUROPE -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x079f80;
//                        TRAINER_CLASS_PRIZE_MONEY.pointerOffset = ;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x0793b4;
                    }
                    case FRANCE -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x07a020;
//                        TRAINER_CLASS_PRIZE_MONEY.pointerOffset = ;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x079454;
                    }
                    case SPAIN -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x07a020;
//                        TRAINER_CLASS_PRIZE_MONEY.pointerOffset = ;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x079454;
                    }
                    case ITALY -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x07a020;
//                        TRAINER_CLASS_PRIZE_MONEY.pointerOffset = ;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x079454;
                    }
                    case JAPAN -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x079858;
//                        TRAINER_CLASS_PRIZE_MONEY.pointerOffset = ;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x078c8c;
                    }
                    case KOREA -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x07a46c;
//                        TRAINER_CLASS_PRIZE_MONEY.pointerOffset = ;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x0798a0;
                    }
                }
            }
            case HeartGold -> {
                PARTY_ICON_PALETTE.pointerLocation = GameCodeBinaries.ARM9;
                TRAINER_CLASS_GENDER.pointerLocation = GameCodeBinaries.ARM9;
                switch (baseROM.getRegion()) {
                    case USA -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074408;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073600;
                    }
                    case GERMANY -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074408;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073600;
                    }
                    case EUROPE -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074408;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073600;
                    }
                    case FRANCE -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074408;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073600;
                    }
                    case SPAIN -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074400;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x0735f8;
                    }
                    case ITALY -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074408;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073600;
                    }
                    case JAPAN -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x073ea0;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073098;
                    }
                    case KOREA -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074508;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073700;
                    }
                }
            }
            case SoulSilver -> {
                PARTY_ICON_PALETTE.pointerLocation = GameCodeBinaries.ARM9;
                TRAINER_CLASS_GENDER.pointerLocation = GameCodeBinaries.ARM9;
                switch (baseROM.getRegion()) {
                    case USA -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074408;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073600;
                    }
                    case GERMANY -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074408;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073600;
                    }
                    case EUROPE -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074408;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073600;
                    }
                    case FRANCE -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074408;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073600;
                    }
                    case SPAIN -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074408;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073600;
                    }
                    case ITALY -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074408;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073600;
                    }
                    case JAPAN -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x073ea0;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x073098;
                    }
                    case KOREA -> {
                        PARTY_ICON_PALETTE.pointerOffset = 0x074500;
                        TRAINER_CLASS_GENDER.pointerOffset = 0x0736f8;
                    }
                }
            }
        }
    }
}
