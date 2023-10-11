package io.github.turtleisaac.pokeditor.formats.items;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.GameFiles;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;

import java.util.Collections;
import java.util.Map;

public class ItemData implements GenericFileData
{
    int price; // u16
    int equipmentEffect; // u8
    int power; // u8
    int pluckEffect; // u8
    int flingEffect; // u8
    int flingPower; // u8
    int naturalGiftPower; // u8

    int naturalGiftType; // u16:5
    boolean unableToDiscard; // u16:1
    boolean canRegister; // u16:1
    int fieldBag; // u16:4
    int battleBag; // u16:5

    int fieldFunction; // u8
    int battleFunction; // u8
    int workType; // u8

    //sleep, poison, burn, freeze, paralyze, confuse, attract, guard spec
    boolean[] statusRecoveries; // bitfield in this u8

    //revive, revive all (sacred ash), rare candy, evolution stone
    boolean[] utilities;

    int[] statBoosts; //atk, def, speed, spatk, spdef, accuracy, crit

    boolean[] ppUpEffects; //pp up, pp max

    boolean[] recoveryToggles; //pp, all pp (max elixir), hp recovery

    boolean[] evYieldToggles; //hp, atk, def, speed, spatk, spdef
    boolean[] friendshipChangeToggles;

    int[] evYields;

    int hpRecoveryAmount;
    int ppRecoveryAmount;

    int[] friendshipChangeAmounts;

    public ItemData(Map<GameFiles, byte[]> files)
    {
        setData(files);
    }

    @Override
    public void setData(Map<GameFiles, byte[]> files)
    {
        if (!files.containsKey(GameFiles.ITEMS))
        {
            throw new RuntimeException("Items file not provided to editor");
        }

        MemBuf dataBuf = MemBuf.create(files.get(GameFiles.ITEMS));
        MemBuf.MemBufReader reader = dataBuf.reader();

        price = reader.readUInt16();
        equipmentEffect = reader.readUInt8();
        power = reader.readUInt8();
        pluckEffect = reader.readUInt8();
        flingEffect = reader.readUInt8();
        flingPower = reader.readUInt8();
        naturalGiftPower = reader.readUInt8();

        int bitfield1 = reader.readUInt16();
        naturalGiftType = bitfield1 & 0x1f;
        unableToDiscard = ((bitfield1 >> 5) & 1) == 1;
        canRegister = ((bitfield1 >> 6) & 1) == 1;
        fieldBag = ((bitfield1 >> 7) & 0xf);
        battleBag = ((bitfield1 >> 11) & 0x1f);

        fieldFunction = reader.readUInt8();
        battleFunction = reader.readUInt8();
        workType = reader.readUInt8();
        reader.skip(1);

        statusRecoveries = new boolean[NUM_STATUS_RECOVERIES];
        int recovery = reader.readUInt8();
        for (int i = 0; i < statusRecoveries.length; i++)
        {
            statusRecoveries[i] = ((recovery >> i) & 1) == 1;
        }

        utilities = new boolean[NUM_UTILITIES];
        int utilityInfo = reader.readUInt8();
        for (int i = 0; i < utilities.length; i++)
        {
            utilities[i] = ((utilityInfo >> i) & 1) == 1;
        }

        statBoosts = new int[NUM_STAT_BOOSTS];
        statBoosts[0] = (utilityInfo >> 4) & 0xf;

        int battleBoosts;
        for (int i = 1; i < 4; i += 2)
        {
            battleBoosts = reader.readUInt8();
            statBoosts[i] = battleBoosts & 0xf;
            statBoosts[i + 1] = (battleBoosts >> 4) & 0xf;
        }

        battleBoosts = reader.readUInt8();
        statBoosts[5] = battleBoosts & 0xf;
        statBoosts[6] = (battleBoosts >> 4) & 0x3;

        ppUpEffects = new boolean[NUM_PP_UP_EFFECTS];
        ppUpEffects[0] = ((battleBoosts >> 6) & 1) == 1;
        ppUpEffects[1] = ((battleBoosts >> 7) & 1) == 1;

        recoveryToggles = new boolean[NUM_RECOVERY_TOGGLES];
        recovery = reader.readUInt8();
        for (int i = 0; i < 3; i++)
        {
            recoveryToggles[i] = ((recovery >> i) & 1) == 1;
        }

        evYieldToggles = new boolean[NUM_EV_YIELDS];
        for(int i = 3; i < 8; i++)
        {
            evYieldToggles[i - 3] = ((recovery >> i) & 1) == 1;
        }

        int bitfield2 = reader.readUInt8();
        evYieldToggles[NUM_EV_YIELDS - 1] = (bitfield2 & 1) == 1;

        friendshipChangeToggles = new boolean[NUM_FRIENDSHIP_CHANGE_FIELDS];
        for (int i = 0; i < NUM_FRIENDSHIP_CHANGE_FIELDS; i++)
        {
            friendshipChangeToggles[i] = ((bitfield2 >> (i + 1)) & 1) == 1;
        }

        evYields = new int[NUM_EV_YIELDS];
        for (int i = 0; i < NUM_EV_YIELDS; i++)
        {
            evYields[i] = reader.readByte(); // s8
        }

        hpRecoveryAmount = reader.readByte();
        ppRecoveryAmount = reader.readByte();

        friendshipChangeAmounts = new int[NUM_FRIENDSHIP_CHANGE_FIELDS];
        for (int i = 0; i < NUM_FRIENDSHIP_CHANGE_FIELDS; i++)
        {
            friendshipChangeAmounts[i] = reader.readByte();
        }
    }

    @Override
    public Map<GameFiles, byte[]> save()
    {
        MemBuf dataBuf = MemBuf.create();
        MemBuf.MemBufWriter writer = dataBuf.writer();

        



        return Collections.singletonMap(GameFiles.ITEMS, dataBuf.reader().getBuffer());
    }

    private static final int NUM_STATUS_RECOVERIES = 8;
    private static final int NUM_UTILITIES = 4;
    private static final int NUM_STAT_BOOSTS = 7;
    private static final int NUM_PP_UP_EFFECTS = 2;
    private static final int NUM_RECOVERY_TOGGLES = 3;
    private static final int NUM_EV_YIELDS = 6;
    private static final int NUM_FRIENDSHIP_CHANGE_FIELDS = 3;
}
