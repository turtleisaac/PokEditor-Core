package io.github.turtleisaac.pokeditor.formats.personal;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;

public class PersonalData implements GenericFileData
{
    private int hp; // u8
    private int atk; // u8
    private int def; // u8
    private int speed; // u8
    private int spAtk; // u8
    private int spDef; // u8
    private int type1; // u8
    private int type2; // u8
    private int catchRate; // u8
    private int baseExp; // u8

    private int hpEvYield; // u16:2
    private int atkEvYield; // u16:2
    private int defEvYield; // u16:2
    private int speedEvYield; // u16:2
    private int spAtkEvYield; // u16:2
    private int spDefEvYield; // u16:2

    private int uncommonItem; // u16
    private int rareItem; // u16

    private int genderRatio; // u8
    private int hatchMultiplier; // u8
    private int baseHappiness; // u8
    private int expRate; // u8
    private int eggGroup1; // u8
    private int eggGroup2; // u8
    private int ability1; // u8
    private int ability2; // u8

    private int runChance; // u8

    private int dexColor; // u8:7
    private boolean flip;  // u8:1

    private boolean[] tmCompatibility; // u8[16], each TM is a single bit

    public PersonalData()
    {
//        hp = 0;
//        atk = 0;
//        def = 0;
//        speed = 0;
//        spAtk = 0;
//        spDef = 0;
//        type1 = 0;
//        type2 = 0;
//        catchRate = 0;
//        baseExp = 0;
//        hpEvYield = 0;
//        atkEvYield = 0;
//        defEvYield = 0;
//        speedEvYield = 0;
//        spAtkEvYield = 0;
//        spDefEvYield = 0;
//        uncommonItem = 0;
//        rareItem = 0;
//        genderRatio = 0;
//        hatchMultiplier = 0;
//        baseHappiness = 0;
//        expRate = 0;
//        eggGroup1 = 0;
//        eggGroup2 = 0;
//        ability1 = 0;
//        ability2 = 0;
//        runChance = 0;
//        dexColor = 0;
//        flip = false;
        tmCompatibility = new boolean[NUMBER_TM_HM_BITS];
    }

    public PersonalData(BytesDataContainer files)
    {
        setData(files);
    }

    @Override
    public void setData(BytesDataContainer files)
    {
        if (!files.containsKey(GameFiles.PERSONAL))
        {
            throw new RuntimeException("Personal file not provided to editor");
        }

        MemBuf dataBuf = MemBuf.create(files.get(GameFiles.PERSONAL, null));
        MemBuf.MemBufReader reader = dataBuf.reader();

        hp = reader.readUInt8();
        atk = reader.readUInt8();
        def = reader.readUInt8();
        speed = reader.readUInt8();
        spAtk = reader.readUInt8();
        spDef = reader.readUInt8();
        type1 = reader.readUInt8();
        type2 = reader.readUInt8();
        catchRate = reader.readUInt8();
        baseExp = reader.readUInt8();

        int evYields = reader.readUInt16();
        hpEvYield = getHpEv(evYields);
        atkEvYield = getAtkEv(evYields);
        defEvYield = getDefEv(evYields);
        speedEvYield = getSpeedEv(evYields);
        spAtkEvYield = getSpAtkEv(evYields);
        spDefEvYield = getSpDefEv(evYields);

        uncommonItem = reader.readUInt16();
        rareItem = reader.readUInt16();

        genderRatio = reader.readUInt8();
        hatchMultiplier = reader.readUInt8();
        baseHappiness = reader.readUInt8();
        expRate = reader.readUInt8();
        eggGroup1 = reader.readUInt8();
        eggGroup2 = reader.readUInt8();
        ability1 = reader.readUInt8();
        ability2 = reader.readUInt8();

        runChance = reader.readUInt8();

        int colorFlip = reader.readUInt8();
        dexColor = colorFlip & 0x7F;
        flip = ((colorFlip & 0x80) >> 7) == 1;

        reader.skip(2); // 2 bytes padding
        byte[] tmLearnset = reader.readBytes(16);

        this.tmCompatibility = new boolean[NUMBER_TM_HM_BITS];
        for (int i = 0; i < NUMBER_TM_HM_BITS; i++)
        {
            this.tmCompatibility[i] = (tmLearnset[i / 8] & 1 << (i % 8)) != 0;
        }
    }

    @Override
    public BytesDataContainer save()
    {
        MemBuf dataBuf = MemBuf.create();
        MemBuf.MemBufWriter writer = dataBuf.writer();

        writer.writeBytes(hp, atk, def, speed, spAtk, spDef, type1, type2, catchRate, baseExp);
        writer.writeShort(getCombinedEvShort());
        writer.writeShort((short)uncommonItem);
        writer.writeShort((short)rareItem);
        writer.writeBytes(genderRatio,hatchMultiplier,baseHappiness,expRate,eggGroup1,eggGroup2,ability1,ability2,runChance,getCombinedColorFlip());
        writer.skip(2);

        int[] tmLearnsetData = new int[16];
        for (int i = 0; i < NUMBER_TM_HM_BITS; i++)
        {
            tmLearnsetData[i / 8] |= ((tmCompatibility[i] ? 1 : 0) << (i % 8));
        }
        writer.writeBytes(tmLearnsetData);

        return new BytesDataContainer(GameFiles.PERSONAL, null, dataBuf.reader().getBuffer());
    }

//    @Override
//    public void serialize(JsonGenerator jsonGenerator) throws IOException
//    {
//        jsonGenerator.writeObjectFieldStart("base_stats");
//        jsonGenerator.writeObjectField("hp", hp);
//        jsonGenerator.writeObjectField("attack", atk);
//        jsonGenerator.writeObjectField("defense", def);
//        jsonGenerator.writeObjectField("speed", speed);
//        jsonGenerator.writeObjectField("special_attack", spAtk);
//        jsonGenerator.writeObjectField("special_defense", spDef);
//        jsonGenerator.writeEndObject();
//
//        jsonGenerator.writeArrayFieldStart("types");
////        jsonGenerator.writeA;
//        jsonGenerator.writeEndArray();
//
//        jsonGenerator.writeObjectField("catch_rate", catchRate);
//        jsonGenerator.writeObjectField("base_exp_reward", baseExp);
//
//        jsonGenerator.writeObjectFieldStart("ev_yields");
//        jsonGenerator.writeObjectField("hp", hpEvYield);
//        jsonGenerator.writeObjectField("attack", atkEvYield);
//        jsonGenerator.writeObjectField("defense", defEvYield);
//        jsonGenerator.writeObjectField("speed", speedEvYield);
//        jsonGenerator.writeObjectField("special_attack", spAtkEvYield);
//        jsonGenerator.writeObjectField("special_defense", spDefEvYield);
//        jsonGenerator.writeEndObject();
//
//        jsonGenerator.writeArrayFieldStart("held_items");
//        jsonGenerator.writeEndArray();
//
//        jsonGenerator.writeObjectField("gender_ratio", genderRatio);
//        jsonGenerator.writeObjectField("hatch_cycles", hatchMultiplier);
//        jsonGenerator.writeObjectField("base_friendship", baseHappiness);
//        jsonGenerator.writeObjectField("exp_rate", expRate);
//
//        jsonGenerator.writeArrayFieldStart("egg_groups");
//        jsonGenerator.writeEndArray();
//
//        jsonGenerator.writeArrayFieldStart("abilities");
//        jsonGenerator.writeEndArray();
//
//        jsonGenerator.writeObjectField("great_marsh_flee_rate", runChance);
//
//        jsonGenerator.writeObjectFieldStart("sprite");
//        jsonGenerator.writeObjectField("color", dexColor);
//        jsonGenerator.writeObjectField("flip_sprite", flip);
//        jsonGenerator.writeEndObject();
//
//        jsonGenerator.writeObjectFieldStart("learnset");
//
//        jsonGenerator.writeArrayFieldStart("tms");
//        jsonGenerator.writeEndArray();
//
//        jsonGenerator.writeEndObject();
//    }

    private short getCombinedEvShort()
    {
        int val = 0;
        val |= hpEvYield;
        val |= (atkEvYield << 2);
        val |= (defEvYield << 4);
        val |= (speedEvYield << 6);
        val |= (spAtkEvYield << 8);
        val |= (spDefEvYield << 10);

        return (short) val;
    }

    private int getCombinedColorFlip()
    {
        return dexColor | (flip ? 0x80 : 0);
    }

    public int getHp()
    {
        return hp;
    }

    public void setHp(int hp)
    {
        if (hp >= 256)
            throw new RuntimeException("Maximum HP value is 255. Provided: " + hp);
        this.hp = hp;
    }

    public int getAtk()
    {
        return atk;
    }

    public void setAtk(int atk)
    {
        if (atk >= 256)
            throw new RuntimeException("Maximum Attack value is 255. Provided: " + atk);
        this.atk = atk;
    }

    public int getDef()
    {
        return def;
    }

    public void setDef(int def)
    {
        if (def >= 256)
            throw new RuntimeException("Maximum Defense value is 255. Provided: " + def);
        this.def = def;
    }

    public int getSpeed()
    {
        return speed;
    }

    public void setSpeed(int speed)
    {
        if (speed >= 256)
            throw new RuntimeException("Maximum Speed value is 255. Provided: " + speed);
        this.speed = speed;
    }

    public int getSpAtk()
    {
        return spAtk;
    }

    public void setSpAtk(int spAtk)
    {
        if (spAtk >= 256)
            throw new RuntimeException("Maximum Special Attack value is 255. Provided: " + spAtk);
        this.spAtk = spAtk;
    }

    public int getSpDef()
    {
        return spDef;
    }

    public void setSpDef(int spDef)
    {
        if (spDef >= 256)
            throw new RuntimeException("Maximum Special Defense value is 255. Provided: " + spDef);
        this.spDef = spDef;
    }

    public int getType1()
    {
        return type1;
    }

    public void setType1(int type1)
    {
        if (type1 >= 19)  // TODO have a way to know for certain the number of types
            throw new RuntimeException("Maximum type value is 18. Provided: " + type1);
        this.type1 = type1;
    }

    public int getType2()
    {
        return type2;
    }

    public void setType2(int type2)
    {
        if (type2 >= 19)  // TODO have a way to know for certain the number of types
            throw new RuntimeException("Maximum type value is 18. Provided: " + type2);
        this.type2 = type2;
    }

    public int getCatchRate()
    {
        return catchRate;
    }

    public void setCatchRate(int catchRate)
    {
        if (catchRate >= 256)
            throw new RuntimeException("Maximum catch rate value is 255. Provided: " + catchRate);
        this.catchRate = catchRate;
    }

    public int getBaseExp()
    {
        return baseExp;
    }

    public void setBaseExp(int baseExp)
    {
        if (baseExp >= 256)
            throw new RuntimeException("Maximum exp yield value is 255. Provided: " + baseExp);
        this.baseExp = baseExp;
    }

    public int getHpEvYield()
    {
        return hpEvYield;
    }

    public void setHpEvYield(int hpEvYield)
    {
        if (hpEvYield >= 5)
            throw new RuntimeException("Maximum HP EV yield value is 4. Provided: " + hpEvYield);
        this.hpEvYield = hpEvYield;
    }

    public int getAtkEvYield()
    {
        return atkEvYield;
    }

    public void setAtkEvYield(int atkEvYield)
    {
        if (atkEvYield >= 5)
            throw new RuntimeException("Maximum Attack EV yield value is 4. Provided: " + atkEvYield);
        this.atkEvYield = atkEvYield;
    }

    public int getDefEvYield()
    {
        return defEvYield;
    }

    public void setDefEvYield(int defEvYield)
    {
        if (defEvYield >= 5)
            throw new RuntimeException("Maximum Defense EV yield value is 4. Provided: " + defEvYield);
        this.defEvYield = defEvYield;
    }

    public int getSpeedEvYield()
    {
        return speedEvYield;
    }

    public void setSpeedEvYield(int speedEvYield)
    {
        if (speedEvYield >= 5)
            throw new RuntimeException("Maximum Speed EV yield value is 4. Provided: " + speedEvYield);
        this.speedEvYield = speedEvYield;
    }

    public int getSpAtkEvYield()
    {
        return spAtkEvYield;
    }

    public void setSpAtkEvYield(int spAtkEvYield)
    {
        if (spAtkEvYield >= 5)
            throw new RuntimeException("Maximum Special Attack EV yield value is 4. Provided: " + spAtkEvYield);
        this.spAtkEvYield = spAtkEvYield;
    }

    public int getSpDefEvYield()
    {
        return spDefEvYield;
    }

    public void setSpDefEvYield(int spDefEvYield)
    {
        if (spDefEvYield >= 5)
            throw new RuntimeException("Maximum Special Defense EV yield value is 4. Provided: " + spDefEvYield);
        this.spDefEvYield = spDefEvYield;
    }

    public int getUncommonItem()
    {
        return uncommonItem;
    }

    public void setUncommonItem(int uncommonItem)
    {
        if (uncommonItem >= 65536)
            throw new RuntimeException("Maximum uncommon held item value is 65535. Provided: " + uncommonItem);
        this.uncommonItem = uncommonItem;
    }

    public int getRareItem()
    {
        return rareItem;
    }

    public void setRareItem(int rareItem)
    {
        if (rareItem >= 65536)
            throw new RuntimeException("Maximum rare held item value is 65535. Provided: " + rareItem);
        this.rareItem = rareItem;
    }

    public int getGenderRatio()
    {
        return genderRatio;
    }

    public void setGenderRatio(int genderRatio)
    {
        if (genderRatio >= 256)
            throw new RuntimeException("Maximum gender ratio value is 255. Provided: " + genderRatio);
        this.genderRatio = genderRatio;
    }

    public int getHatchMultiplier()
    {
        return hatchMultiplier;
    }

    public void setHatchMultiplier(int hatchMultiplier)
    {
        if (hatchMultiplier >= 256)
            throw new RuntimeException("Maximum hatch multiplier value is 255. Provided: " + hatchMultiplier);
        this.hatchMultiplier = hatchMultiplier;
    }

    public int getBaseHappiness()
    {
        return baseHappiness;
    }

    public void setBaseHappiness(int baseHappiness)
    {
        if (baseHappiness >= 256)
            throw new RuntimeException("Maximum base happiness value is 255. Provided: " + baseHappiness);
        this.baseHappiness = baseHappiness;
    }

    public int getExpRate()
    {
        return expRate;
    }

    public void setExpRate(int expRate)
    {
        if (expRate >= 256)
            throw new RuntimeException("Maximum exp rate value is 255. Provided: " + expRate);
        this.expRate = expRate;
    }

    public int getEggGroup1()
    {
        return eggGroup1;
    }

    public void setEggGroup1(int eggGroup1)
    {
        if (eggGroup1 >= 256)
            throw new RuntimeException("Maximum egg group value is 255. Provided: " + eggGroup1);
        this.eggGroup1 = eggGroup1;
    }

    public int getEggGroup2()
    {
        return eggGroup2;
    }

    public void setEggGroup2(int eggGroup2)
    {
        if (eggGroup2 >= 256)
            throw new RuntimeException("Maximum egg group value is 255. Provided: " + eggGroup2);
        this.eggGroup2 = eggGroup2;
    }

    public int getAbility1()
    {
        return ability1;
    }

    public void setAbility1(int ability1)
    {
        if (ability1 >= 256)
            throw new RuntimeException("Maximum ability value is 255. Provided: " + ability1);
        this.ability1 = ability1;
    }

    public int getAbility2()
    {
        return ability2;
    }

    public void setAbility2(int ability2)
    {
        if (ability2 >= 256)
            throw new RuntimeException("Maximum ability value is 255. Provided: " + ability2);
        this.ability2 = ability2;
    }

    public int getRunChance()
    {
        return runChance;
    }

    public void setRunChance(int runChance)
    {
        if (runChance >= 256)
            throw new RuntimeException("Maximum run chance value is 255. Provided: " + runChance);
        this.runChance = runChance;
    }

    public int getDexColor()
    {
        return dexColor;
    }

    public void setDexColor(int dexColor)
    {
        if (dexColor >= 129)
            throw new RuntimeException("Maximum dex color value is 128. Provided: " + dexColor);
        this.dexColor = dexColor;
    }

    public boolean isFlip()
    {
        return flip;
    }

    public void setFlip(boolean flip)
    {
        this.flip = flip;
    }

    public boolean[] getTmCompatibility()
    {
        return tmCompatibility;
    }

    public void setTmCompatibility(boolean[] tmCompatibility)
    {
        this.tmCompatibility = tmCompatibility;
    }

    private static final int NUMBER_TM_HM_BITS = 128;
    protected static final int NUMBER_TMS_HMS = 100;

    private static int getHpEv(int x)
    {
        return x & 0x03;
    }

    private static int getAtkEv(int x)
    {
        return (x >> 2) & 0x03;
    }

    private static int getDefEv(int x)
    {
        return (x >> 4) & 0x03;
    }

    private static int getSpeedEv(int x)
    {
        return (x >> 6) & 0x03;
    }

    private static int getSpAtkEv(int x)
    {
        return (x >> 8) & 0x03;
    }

    private static int getSpDefEv(int x)
    {
        return (x >> 10) & 0x03;
    }
}
