package io.github.turtleisaac.pokeditor.formats;

/**
 * Checks that a value fits the field it is about to be written into.
 * <p>
 * Every format in this package writes its fields by narrowing: {@code (short) value},
 * {@code writeBytes(value)}, or an explicit mask such as {@code value & 0x1FF}. Narrowing
 * silently discards the high bits, so a value the editor accepted could be written back as
 * a completely different one - a learnset move of 512 was stored as move 0 (no move at all),
 * and a level of 200 as level 72. Nothing reported it, and the only way to notice was to
 * reopen the file and read the wrong value back.
 * <p>
 * These methods turn that silent loss into a failure that names the field and the value.
 * They are deliberately called on the <em>write</em> path rather than in the setters: the
 * setters are also fed by the load path in some formats, and a file already containing an
 * unusual value must still open. What must not happen is writing a value back out as
 * something other than what was set.
 */
public final class FieldWidth
{
    private FieldWidth() {}

    /**
     * @param value the value about to be written
     * @param numBits the number of bits the field actually occupies
     * @param fieldName the field's name, for the failure message
     * @return {@code value}, when it fits
     * @throws IllegalArgumentException when it does not
     */
    public static int bits(int value, int numBits, String fieldName)
    {
        int max = (1 << numBits) - 1;
        if (value < 0 || value > max)
        {
            throw new IllegalArgumentException(String.format(
                    "%s is %d, which does not fit in the %d bits the file gives it (allowed: 0 to %d). "
                            + "Writing it would silently store a different value.",
                    fieldName, value, numBits, max));
        }
        return value;
    }

    /**
     * An unsigned byte field, written through {@code writeBytes}.
     */
    public static int u8(int value, String fieldName)
    {
        return bits(value, 8, fieldName);
    }

    /**
     * An unsigned 16-bit field, written through {@code writeShort}.
     */
    public static int u16(int value, String fieldName)
    {
        return bits(value, 16, fieldName);
    }

    /**
     * A signed byte field. Distinct from {@link #u8} because the range is -128 to 127, not
     * 0 to 255 - move priority is the field this exists for.
     */
    public static int s8(int value, String fieldName)
    {
        if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE)
        {
            throw new IllegalArgumentException(String.format(
                    "%s is %d, which does not fit in the signed byte the file gives it "
                            + "(allowed: %d to %d). Writing it would silently store a different value.",
                    fieldName, value, Byte.MIN_VALUE, Byte.MAX_VALUE));
        }
        return value;
    }
}
