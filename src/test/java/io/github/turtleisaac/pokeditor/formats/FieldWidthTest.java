package io.github.turtleisaac.pokeditor.formats;

import io.github.turtleisaac.pokeditor.formats.learnsets.LearnsetData;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A field that does not fit its storage must fail loudly rather than be narrowed into a
 * different value.
 * <p>
 * The bounds asserted here are derived from the file layout, not from what the code
 * currently does: a level occupies 7 bits and a move ID 9 bits of one shared 16-bit word,
 * so the representable values are exactly 0..127 and 0..511. Anything outside that cannot
 * be stored, and storing it anyway is data loss - move 512 became move 0, which reads back
 * as "this Pokemon learns nothing at this level".
 */
class FieldWidthTest
{
    @Nested
    @DisplayName("the width check itself")
    class Widths
    {
        @Test
        void everyValueThatFitsIsReturnedUnchanged()
        {
            // the check is the identity on its whole domain - it must not clamp, wrap or
            // otherwise alter a value that was always fine
            for (int i = 0; i <= 0xFF; i++)
                assertThat(FieldWidth.u8(i, "f")).isEqualTo(i);
            for (int i = 0; i <= 0xFFFF; i++)
                assertThat(FieldWidth.u16(i, "f")).isEqualTo(i);
            for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++)
                assertThat(FieldWidth.s8(i, "f")).isEqualTo(i);
        }

        @Test
        void theBoundariesThemselvesFit()
        {
            // an off-by-one in a bounds check shows up first at the bounds
            assertThatCode(() -> {
                FieldWidth.u8(0, "f");
                FieldWidth.u8(255, "f");
                FieldWidth.u16(0, "f");
                FieldWidth.u16(65535, "f");
                FieldWidth.s8(-128, "f");
                FieldWidth.s8(127, "f");
                FieldWidth.bits(0, 7, "f");
                FieldWidth.bits(127, 7, "f");
                FieldWidth.bits(511, 9, "f");
            }).doesNotThrowAnyException();
        }

        @Test
        void oneStepPastEachBoundaryIsRejected()
        {
            assertThatThrownBy(() -> FieldWidth.u8(256, "f")).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> FieldWidth.u8(-1, "f")).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> FieldWidth.u16(65536, "f")).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> FieldWidth.u16(-1, "f")).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> FieldWidth.s8(128, "f")).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> FieldWidth.s8(-129, "f")).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> FieldWidth.bits(128, 7, "f")).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> FieldWidth.bits(512, 9, "f")).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void theFailureNamesTheFieldTheValueAndTheLimit()
        {
            // a message that omits any of the three cannot be acted on: the user needs to know
            // which cell, what they typed, and what would have been acceptable
            assertThatThrownBy(() -> FieldWidth.bits(512, 9, "Move ID"))
                    .hasMessageContaining("Move ID")
                    .hasMessageContaining("512")
                    .hasMessageContaining("511");
        }
    }

    @Nested
    @DisplayName("learnset packing")
    class LearnsetPacking
    {
        /** an empty level-up learnset: just the 0xFFFF terminator and its padding */
        private LearnsetData empty()
        {
            return new LearnsetData(new BytesDataContainer(GameFiles.LEVEL_UP_LEARNSETS, null,
                    new byte[] {(byte) 0xFF, (byte) 0xFF, 0, 0}));
        }

        private LearnsetData withOneEntry(int moveID, int level)
        {
            LearnsetData data = empty();
            data.add(new LearnsetData.LearnsetEntry(moveID, level));
            return data;
        }

        @Test
        void anEntryThatFitsSurvivesSaveAndReload()
        {
            // the property that matters: what was set is what comes back. asserted at the
            // extremes of both fields, where a mask would bite first
            // (511, 127) is deliberately absent: it is individually in range but packs to
            // 0xFFFF, the terminator, and is rejected. It is covered by its own test below.
            for (int[] pair : new int[][] {{0, 0}, {511, 126}, {510, 127}, {1, 1}, {350, 100}})
            {
                LearnsetData saved = withOneEntry(pair[0], pair[1]);
                LearnsetData reloaded = empty();
                reloaded.setData(saved.save());

                assertThat(reloaded).as("learnset holding move %d at level %d", pair[0], pair[1])
                        .hasSize(1);
                assertThat(reloaded.get(0).getMoveID()).isEqualTo(pair[0]);
                assertThat(reloaded.get(0).getLevel()).isEqualTo(pair[1]);
            }
        }

        @Test
        void aMoveIdPastNineBitsIsRefusedRatherThanTruncated()
        {
            // 512 & 0x1FF == 0, so this used to save as "no move" with nothing reported.
            // the test asserts the failure, and that the message names the value the user typed
            assertThatThrownBy(() -> withOneEntry(512, 5).save())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("512");
        }

        @Test
        void aLevelPastSevenBitsIsRefusedRatherThanTruncated()
        {
            // 200 & 0x7F == 72
            assertThatThrownBy(() -> withOneEntry(1, 200).save())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("200");
        }

        @Test
        void theCombinationThatCollidesWithTheTerminatorIsRefused()
        {
            // (127 << 9) | 511 == 0xFFFF, which is exactly the end-of-list marker. both fields
            // are individually legal, so only the packed value reveals the problem - saving it
            // wrote a terminator and the entry silently disappeared on reload
            assertThatThrownBy(() -> withOneEntry(511, 127).save())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("511")
                    .hasMessageContaining("127");
        }

        @Test
        void everyOtherInRangeCombinationRoundTrips()
        {
            // exhaustive over both fields: the terminator collision must be the ONLY hole.
            // a sampled test would have missed it, and would miss a second one just as easily
            for (int level = 0; level <= 127; level++)
            {
                for (int move = 0; move <= 511; move++)
                {
                    if (((level << 9) | move) == 0xFFFF)
                        continue;

                    LearnsetData reloaded = empty();
                    reloaded.setData(withOneEntry(move, level).save());
                    assertThat(reloaded).as("move %d at level %d", move, level).hasSize(1);
                    assertThat(reloaded.get(0).getMoveID()).as("move %d at level %d", move, level).isEqualTo(move);
                    assertThat(reloaded.get(0).getLevel()).as("move %d at level %d", move, level).isEqualTo(level);
                }
            }
        }

        @Test
        void aNegativeMoveIdIsRefused()
        {
            // reachable from the sheet: a combo box with nothing selected reports -1, and
            // -1 & 0x1FF is 511 - a real move, silently substituted for an empty selection
            assertThatThrownBy(() -> withOneEntry(-1, 5).save())
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
