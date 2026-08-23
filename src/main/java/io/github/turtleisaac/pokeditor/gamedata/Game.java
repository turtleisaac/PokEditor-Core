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


    Game(String[] sheetList, String[] editorList)
    {
        this.sheetList= sheetList;
        this.editorList= editorList;
    }

    /**
     * Gets the region of the most recently parsed base ROM.
     *
     * @return a <code>Region</code>, or <code>null</code> if no base ROM has been parsed yet
     * @deprecated the region belongs to the ROM, not to the <code>Game</code> constant, which is shared by
     * every ROM opened in this process. Use the {@link BaseRomInfo} returned by
     * {@link #parseBaseRom(String)} and pass its region around explicitly instead.
     */

    /**
     * The game and region identified by a base ROM's game code
     *
     * @param game a <code>Game</code>
     * @param region a <code>Region</code>
     */
    public record BaseRomInfo(Game game, Region region) {}

    public static BaseRomInfo parseBaseRom(String baseRomGameCode)
    {
        Game game = switch (baseRomGameCode.substring(0, 3)) {
            case "ADA" -> Game.Diamond;
            case "APA" -> Game.Pearl;
            case "CPU" -> Game.Platinum;
            case "IPK" -> Game.HeartGold;
            case "IPG" -> Game.SoulSilver;
            default -> throw new RuntimeException("Invalid game");
        };

        // the region travels with the result rather than being stashed anywhere: a field on the
        // enum, static or per-constant, is shared by every ROM opened in the process
        return new BaseRomInfo(game, Region.getRegion(baseRomGameCode.charAt(3)));
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

        public static Region getRegion(char c)
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
