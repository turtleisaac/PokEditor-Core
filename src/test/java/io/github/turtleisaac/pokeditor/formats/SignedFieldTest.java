package io.github.turtleisaac.pokeditor.formats;

import io.github.turtleisaac.pokeditor.formats.items.ItemData;
import io.github.turtleisaac.pokeditor.formats.moves.MoveData;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sign handling for the fields the Gen IV formats define as signed 8-bit.
 * <p>
 * The underlying buffer returns an <em>unsigned</em> byte, so any field that the format defines
 * as signed has to be converted at the point of the read. Getting this wrong does not corrupt the
 * file &mdash; every write path narrows back through a {@code byte}, so the bytes round-trip
 * either way &mdash; but it corrupts the <em>value</em>: a move priority of -7 becomes 249, which
 * is what the editor then displays and range-checks.
 * <p>
 * These tests build records from raw bytes rather than from a ROM, so they run everywhere. Each
 * asserts both halves of the property: the parsed value carries the right sign, and the bytes
 * still survive a round trip.
 */
@DisplayName("Signed 8-bit fields keep their sign")
class SignedFieldTest
{
    /** Byte 0xF9 is -7 as a signed value and 249 as an unsigned one. */
    private static final int RAW = 0xF9;
    private static final int SIGNED = -7;

    private static BytesDataContainer container(GameFiles file, byte[] data)
    {
        return new BytesDataContainer(file, null, data);
    }

    private static byte[] moveRecordWithPriority(int rawPriority)
    {
        // The moves record is a fixed 16-byte struct; priority sits at offset 0x0A, after
        // effect(2), category(1), power(1), type(1), accuracy(1), pp(1), effectChance(1),
        // target(2).
        byte[] record = new byte[16];
        record[0x0A] = (byte) rawPriority;
        return record;
    }

    @Test
    @DisplayName("move priority parses negative, as Trick Room and Roar require")
    void movePriorityIsSigned()
    {
        MoveData move = new MoveData(container(GameFiles.MOVES, moveRecordWithPriority(RAW)));

        assertThat(move.getPriority())
                .as("0x%02X in the priority field is %d, not %d", RAW, SIGNED, RAW)
                .isEqualTo(SIGNED);
    }

    @Test
    @DisplayName("move priority survives a round trip regardless of sign")
    void movePriorityRoundTrips()
    {
        for (int raw = 0; raw <= 0xFF; raw++)
        {
            byte[] original = moveRecordWithPriority(raw);
            MoveData move = new MoveData(container(GameFiles.MOVES, original));
            byte[] written = move.save().get(GameFiles.MOVES, null);

            assertThat(written[0x0A])
                    .as("priority byte 0x%02X must survive parse then save", raw)
                    .isEqualTo((byte) raw);
        }
    }

    @Test
    @DisplayName("the full signed byte domain parses to the right value")
    void movePriorityDomain()
    {
        for (int raw = 0; raw <= 0xFF; raw++)
        {
            MoveData move = new MoveData(container(GameFiles.MOVES, moveRecordWithPriority(raw)));
            assertThat(move.getPriority())
                    .as("raw byte 0x%02X", raw)
                    .isEqualTo((int) (byte) raw)
                    .isBetween(-128, 127);
        }
    }

    @Test
    @DisplayName("item EV yields and friendship changes keep their sign across a save")
    void itemSignedFieldsSurviveRoundTrip()
    {
        // Expressed without reference to field offsets: set the signed fields, save, reload, and
        // require the values to come back as they went in. That is the property a user cares
        // about, and it stays true if the record layout is ever revised.
        ItemData item = new ItemData(container(GameFiles.ITEMS, new byte[ITEM_RECORD_SIZE]));
        item.setEvYields(new int[]{SIGNED, SIGNED, SIGNED, SIGNED, SIGNED, SIGNED});
        item.setFriendshipChangeAmounts(new int[]{SIGNED, SIGNED, SIGNED});

        ItemData reloaded = new ItemData(
                container(GameFiles.ITEMS, item.save().get(GameFiles.ITEMS, null)));

        assertThat(reloaded.getEvYields())
                .as("EV-reducing berries yield negative EVs, so the sign has to survive")
                .containsOnly(SIGNED);
        assertThat(reloaded.getFriendshipChangeAmounts())
                .as("bitter berries lower friendship, so the sign has to survive")
                .containsOnly(SIGNED);
    }

    @Test
    @DisplayName("every signed byte value survives an item save/reload")
    void itemSignedDomain()
    {
        for (int raw = 0; raw <= 0xFF; raw++)
        {
            int signed = (byte) raw;
            ItemData item = new ItemData(container(GameFiles.ITEMS, new byte[ITEM_RECORD_SIZE]));
            item.setFriendshipChangeAmounts(new int[]{signed, signed, signed});

            ItemData reloaded = new ItemData(
                    container(GameFiles.ITEMS, item.save().get(GameFiles.ITEMS, null)));

            assertThat(reloaded.getFriendshipChangeAmounts())
                    .as("friendship change %d (raw 0x%02X)", signed, raw)
                    .containsOnly(signed);
        }
    }

    @Test
    @DisplayName("an item record round trips byte-for-byte")
    void itemRecordRoundTrips()
    {
        // Whatever the sign handling, the bytes themselves must be preserved exactly.
        ItemData item = new ItemData(container(GameFiles.ITEMS, new byte[ITEM_RECORD_SIZE]));
        byte[] first = item.save().get(GameFiles.ITEMS, null);
        byte[] second = new ItemData(container(GameFiles.ITEMS, first))
                .save().get(GameFiles.ITEMS, null);

        assertThat(second)
                .as("parse then save must be a fixed point")
                .isEqualTo(first);
    }

    /** The fixed portion of an items record is 34 bytes; anything beyond is trailing padding. */
    private static final int ITEM_RECORD_SIZE = 36;
}
