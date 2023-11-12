package io.github.turtleisaac.pokeditor.gamedata;

public enum Game
{
    Diamond(
            new String[] {"Personal","TM Learnsets","Level-Up Learnsets","Evolutions","Tutor Move List","Tutor Move Compatibility","Baby Forms","Moves","Items","Field Encounters","Water Encounters","Swarm/ Day/ Night Encounters","Poke Radar Encounters","Dual-Slot Mode Encounters","Alt Form Encounters"},
            new String[] {"Personal","Learnsets","Evolutions","Tutors","Babies","Moves","Items","Encounters"}),

    Pearl(new String[] {"Personal","TM Learnsets","Level-Up Learnsets","Evolutions","Tutor Move List","Tutor Move Compatibility","Baby Forms","Moves","Items","Field Encounters","Water Encounters","Swarm/ Day/ Night Encounters","Poke Radar Encounters","Dual-Slot Mode Encounters","Alt Form Encounters"},
            new String[] {"Personal","Learnsets","Evolutions","Tutors","Babies","Moves","Items","Encounters"}),

    //NOTE: "Tutor Move List","Tutor Move Compatibility" have been removed from arr1, "Tutors" removed from arr2
    Platinum(new String[] {"Personal","TM Learnsets","Level-Up Learnsets","Evolutions","Baby Forms","Trainer Data","Trainer Pokemon","Moves","Items","Field Encounters","Water Encounters","Swarm/ Day/ Night Encounters","Poke Radar Encounters","Dual-Slot Mode Encounters","Alt Form Encounters"},
            new String[] {"Personal","Level-Up Learnsets","Evolutions","Babies","Trainers","Moves","Items","Encounters"}),

    HeartGold(new String[] {"Personal","TM Learnsets","Level-Up Learnsets","Evolutions","Baby Forms","Trainer Data","Trainer Pokemon","Moves","Items","Field Encounters","Water Encounters","Rock Smash Encounters","Mass-Outbreak Encounters","Sound Encounters"},
            new String[] {"Personal","Level-Up Learnsets","Evolutions","Babies","Trainers","Moves","Items","Encounters"}),

    SoulSilver(new String[] {"Personal","TM Learnsets","Level-Up Learnsets","Evolutions","Baby Forms","Trainer Data","Trainer Pokemon","Moves","Items","Field Encounters","Water Encounters","Rock Smash Encounters","Mass-Outbreak Encounters","Sound Encounters"},
            new String[] {"Personal","Level-Up Learnsets","Evolutions","Babies","Trainers","Moves","Items","Encounters"})
    ;


    public final String[] sheetList;
    public final String[] editorList;

    private Region region;

    Game(String[] sheetList, String[] editorList)
    {
        this.sheetList= sheetList;
        this.editorList= editorList;
    }

    public Region getRegion()
    {
        return region;
    }

    public static Game parseBaseRom(String baseRomGameCode)
    {
        Game game = switch (baseRomGameCode.substring(0, 3)) {
            case "ADA" -> Game.Diamond;
            case "APA" -> Game.Pearl;
            case "CPU" -> Game.Platinum;
            case "IPK" -> Game.HeartGold;
            case "IPG" -> Game.SoulSilver;
            default -> throw new RuntimeException("Invalid game");
        };

        game.region = Region.getRegion(baseRomGameCode.charAt(3));
        return game;
    }

    public enum Region
    {
        USA,
        GERMANY,
        FRANCE,
        ITALY,
        JAPAN,
        KOREA,
        EUROPE,
        SPAIN;

        static Region getRegion(char c)
        {
            return switch (c)
            {
                case 'D' -> GERMANY;
                case 'E' -> USA;
                case 'F' -> FRANCE;
                case 'I' -> ITALY;
                case 'J' -> JAPAN;
                case 'K' -> KOREA;
                case 'P' -> EUROPE;
                case 'S' -> SPAIN;
                default -> throw new IllegalStateException("Unexpected game code region: " + c);
            };
        }
    }
}
