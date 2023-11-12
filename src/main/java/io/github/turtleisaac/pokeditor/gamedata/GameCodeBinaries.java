package io.github.turtleisaac.pokeditor.gamedata;

public enum GameCodeBinaries
{
    ARM9,
    ARM7,
    FIELD,
    BATTLE;

    private int id;

    public int getId()
    {
        return id;
    }

    public static void initialize(Game baseROM)
    {
        switch (baseROM) {
            case Platinum -> {
                BATTLE.id = 16;
            }
            case HeartGold, SoulSilver -> {
                BATTLE.id = 12;
            }
        }
    }

}
