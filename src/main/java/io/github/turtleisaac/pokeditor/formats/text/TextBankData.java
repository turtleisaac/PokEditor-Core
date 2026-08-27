package io.github.turtleisaac.pokeditor.formats.text;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * The VAST majority of the code in this class was originally written by JackHack96 for
 * an unreleased Java port of DSPRE in 2020. The code was then obtained by turtleisaac for
 * use in PokEditor-v2. The version seen here has been adapted to conform to the
 * GenericFileData and GenericParser schema for PokEditor-Core, and has been completed to
 * account for the 9-bit string compression reapplication.
 * <p>
 * Most credits for the algorithm implementation realistically belong to JackHack96 up
 * until the completion of the compress() method and the addition of the Message inner class.
 * <p>
 * This class handles Pokémon Gen 4 text encoding/decoding.
 */
public class TextBankData extends ArrayList<TextBankData.Message> implements GenericFileData
{
    private static final InputStream inputStream = TextBankData.class.getResourceAsStream("/data/characters.json");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final JsonNode characters;

    static {
        try {
            characters = objectMapper.readTree(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
            inputStream.close();
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int seed;

    private int bankIndex = -1;

    public TextBankData(BytesDataContainer files)
    {
        super();
        setData(files);
    }

    public TextBankData(BytesDataContainer files, int bankIndex)
    {
        super();
        this.bankIndex = bankIndex;
        setData(files);
    }

    /**
     * Gets the index of this text bank within the text narc, or -1 if it is not known
     * @return an <code>int</code>
     */
    public int getBankIndex()
    {
        return bankIndex;
    }

    /**
     * Sets the index of this text bank within the text narc (used purely for error reporting)
     * @param bankIndex an <code>int</code>
     */
    public void setBankIndex(int bankIndex)
    {
        this.bankIndex = bankIndex;
    }

    private String describe(int messageIdx)
    {
        return "text bank " + (bankIndex >= 0 ? String.valueOf(bankIndex) : "?") + ", message " + messageIdx;
    }

    @Override
    public void setData(BytesDataContainer files)
    {
        if (!files.containsKey(GameFiles.TEXT))
        {
            throw new RuntimeException("Text file not provided to editor");
        }

        byte[] file = files.get(GameFiles.TEXT, null);
        MemBuf dataBuf = MemBuf.create(file);
        MemBuf.MemBufReader reader = dataBuf.reader();

        int numEntries = reader.readUInt16();
        seed = reader.readUInt16();

        int[] offsets = new int[numEntries];
        int[] sizes = new int[numEntries];

        int[][] binaryStrings = new int[numEntries][];

        int key;
        for (int i = 0; i < numEntries; i++)
        {
            key = ((seed * (i + 1) * 0x2fd) & 0xffff) | ((seed * (i + 1) * 0x2fd0000) & 0xffff0000);
            offsets[i] = reader.readInt() ^ key;
            sizes[i] = reader.readInt() ^ key;

            if (sizes[i] < 0)
            {
                throw new TextEncodingException(describe(i) + " declares a negative length (" + sizes[i] + ")");
            }
            if (offsets[i] < 0 || offsets[i] > file.length)
            {
                throw new TextEncodingException(describe(i) + " declares an out of bounds offset (" + offsets[i] + ", file is " + file.length + " bytes)");
            }
            if (offsets[i] + sizes[i] * 2L > file.length)
            {
                throw new TextEncodingException(describe(i) + " runs past the end of the file (offset " + offsets[i] + ", length " + sizes[i] + " halfwords, file is " + file.length + " bytes)");
            }
        }


        for (int messageIdx = 0; messageIdx < numEntries; messageIdx++)
        {
            boolean compressed = false;
            key = (0x91bd3 * (messageIdx + 1)) & 0xffff;
            binaryStrings[messageIdx] = new int[sizes[messageIdx]];
            reader.setPosition(offsets[messageIdx]);

            // decrypt strings
            for (int j = 0; j < sizes[messageIdx]; j++)
            {
                binaryStrings[messageIdx][j] = reader.readUInt16() ^ key;
                key = (key + 0x493d) & 0xffff;
            }

            if (binaryStrings[messageIdx][0] == 0xf100)
            {
                compressed = true;
                binaryStrings[messageIdx] = decompress(binaryStrings[messageIdx]);
                sizes[messageIdx] = binaryStrings[messageIdx].length;
            }

            StringBuilder text = new StringBuilder();
            for (int j = 0; j < binaryStrings[messageIdx].length; j++)
            {
                int c = binaryStrings[messageIdx][j];
                if (c == 0xffff)
                {
                    break; // if the character is 0xffff, the string finishes here
                }
                else if (c == 0xfffe)
                {
                    // if the character is 0xfffe, then it's a special VAR case
                    int[] binaryString = binaryStrings[messageIdx];

                    if (j + 2 >= binaryString.length)
                    {
                        throw new TextEncodingException(describe(messageIdx) + " contains a truncated VAR() - the variable id and argument count run past the end of the message");
                    }

                    text.append("VAR(");
                    StringBuilder args = new StringBuilder();
                    args.append(binaryString[++j]);
                    int argNum = binaryString[++j];

                    if (argNum < 0 || j + argNum >= binaryString.length)
                    {
                        throw new TextEncodingException(describe(messageIdx) + " contains a VAR() declaring " + argNum + " argument(s), which runs past the end of the message");
                    }

                    for (int k = 0; k < argNum; k++) {
                        args.append(", ");
                        args.append(binaryString[++j]);
                    }
                    args.append(")");
                    text.append(args);
                }
                else
                {
                    try
                    {
                        text.append(characters.get("getChar").get(String.valueOf(c)).asText());
                    }
                    catch (Exception e) {
                        text.append(String.format("\\?%04x", c));
                    }
                }
            }

            add(new Message(text.toString(), compressed));
        }
    }

    @Override
    public BytesDataContainer save()
    {
        MemBuf dataBuf = MemBuf.create();
        MemBuf.MemBufWriter writer = dataBuf.writer();

        List<Integer> tmpBinaryString;
        ArrayList<List<Integer>> binaryStrings = new ArrayList<>();

        // encode text and write it
        for (int messageIdx = 0; messageIdx < size(); messageIdx++)
        {
            Message msg = get(messageIdx);
            String message = msg.text;
            tmpBinaryString = new ArrayList<>();

            // encode the binary strings
            for (int j = 0; j < message.length(); j++)
            {
                String sub = message.substring(j, Math.min(j + 4, message.length()));
                if (message.charAt(j) == '\\')
                {
                    if (j + 1 >= message.length())
                    {
                        throw new TextEncodingException(describe(messageIdx) + " ends with a lone '\\' escape character");
                    }

                    switch (message.charAt(j + 1))
                    {
                        case 'r' -> tmpBinaryString.add(0x25bc);
                        case 'n' -> tmpBinaryString.add(0xe000);
                        case 'f' -> tmpBinaryString.add(0x25bd);
                        default -> {
                            if (j + 6 > message.length())
                            {
                                throw new TextEncodingException(describe(messageIdx) + " contains a truncated escape sequence \"" + message.substring(j) + "\" - four hexadecimal digits are required");
                            }

                            String hex = message.substring(j + 2, j + 6);
                            try {
                                tmpBinaryString.add(Integer.parseInt(hex, 16));
                            }
                            catch (NumberFormatException e) {
                                throw new TextEncodingException(describe(messageIdx) + " contains a malformed escape sequence \"" + message.substring(j, j + 6) + "\"", e);
                            }
                            j += 4;
                        }
                    }
                    j++;
                }
                else if (sub.equals("VAR("))
                {
                    int endOfVar = message.indexOf(')', j);
                    if (endOfVar == -1) {
                        throw new TextEncodingException(describe(messageIdx) + ": could not find end of VAR()");
                    }

                    String[] args = message.substring(j + 4, endOfVar).split(",");
                    tmpBinaryString.add(0xfffe);
                    try {
                        tmpBinaryString.add(Integer.parseInt(args[0].trim()));
                        tmpBinaryString.add(args.length - 1);

                        for (int x = 1; x < args.length; x++)
                        {
                            tmpBinaryString.add(Integer.parseInt(args[x].trim()));
                        }
                    }
                    catch (NumberFormatException e) {
                        throw new TextEncodingException(describe(messageIdx) + " contains a VAR() with a non-numeric argument: \"" + message.substring(j, endOfVar + 1) + "\"", e);
                    }

                    j = endOfVar;
                }
                else
                {
                    char c = message.charAt(j);
                    JsonNode node = characters.get("getInt").get(String.valueOf(c));
                    if (node == null)
                    {
                        throw new TextEncodingException(String.format("%s contains the character '%c' (U+%04X) at index %d, which has no Pokemon Gen 4 character encoding", describe(messageIdx), c, (int) c, j));
                    }

                    tmpBinaryString.add(node.asInt());
                }
            }

            if (msg.compressed)
            {
                try {
                    binaryStrings.add(compress(tmpBinaryString.toArray(Integer[]::new)));
                }
                catch (RuntimeException e) {
                    throw new TextEncodingException(describe(messageIdx) + " is marked as 9-bit compressed but " + e.getMessage(), e);
                }
            }
            else
            {
                binaryStrings.add(tmpBinaryString);
            }
        }

        // generate offsets and sizes and write them
        writer.writeShort((short) size());
        writer.writeShort((short) seed);

        int currentOffset = 4 + (size() * 8);
        int key, currentSize;

        for (int i = 0; i < size(); i++)
        {
            key = ((seed * (i + 1) * 0x2fd) & 0xffff) | ((seed * (i + 1) * 0x2fd0000) & 0xffff0000);
            currentSize = binaryStrings.get(i).size() + 1;

            writer.writeInt(currentOffset ^ key); // write encrypted offset
            writer.writeInt(currentSize ^ key); // write encrypted string size

            currentOffset += currentSize * 2;
        }

        // actually write the encoded strings
        for (int messageIdx = 0; messageIdx < binaryStrings.size(); messageIdx++)
        {
            key = (0x91bd3 * (messageIdx + 1)) & 0xffff;
            for (int j = 0; j < binaryStrings.get(messageIdx).size(); j++)
            {
                writer.writeShort((short) (binaryStrings.get(messageIdx).get(j) ^ key));
                key = (key + 0x493d) & 0xffff;
            }
            writer.writeShort((short) (0xffff ^ key));
        }

        return new BytesDataContainer(GameFiles.TEXT, null, dataBuf.reader().getBuffer());
    }



    /**
     * Decompress the given 9-bit encoded string to normal 16-bit string
     *
     * @param compressedString Compressed 9-bit encoded string
     * @return Uncompressed 16-bit encoded string
     */
    protected static int[] decompress(int[] compressedString)
    {
        // decompress string: characters are stored in 9 bits instead of 16
        int container = 0;
        int bitshift = 0;
        int index = 0;

        int maxDecompressedLength = (compressedString.length - 1) * 2; //remove the compression flag and the end
        int[] newBinaryString = new int[maxDecompressedLength];

        for (int i = 1; i < compressedString.length; i++)
        {
            container |= compressedString[i] << bitshift;
            bitshift += 0xf;
            while (bitshift >= 0x9)
            {
                bitshift -= 0x9;
                if ((container & 0x1ff) == 0x1ff)
                {
                    newBinaryString[index++] = 0xffff;
                }
                else
                {
                    newBinaryString[index++] = container & 0x1ff;
                }

                container >>= 9;
            }
        }

        return newBinaryString;
    }

    // todo remove these methods
    private static void printBinaryString(int num, int len)
    {
        System.out.println(formatBinaryString(num, len));
    }

    private static String[] splitToFours(String binaryString)
    {
        int r = binaryString.length() % 4;

        ArrayList<String> splits = new ArrayList<>();

        String sb = "_".repeat(4 - r) + binaryString.substring(0, r);
        splits.add(sb);

        for (int i = r; i < binaryString.length(); i += 4)
        {
            splits.add(binaryString.substring(i, i + 4));
        }

        return splits.toArray(String[]::new);
    }

    private static String formatBinaryString(int num, int len)
    {
        String base = "____ ";
        int numTimes = 8;

        String binaryString = Integer.toBinaryString(num);
        len -= binaryString.length();
        String[] splits = splitToFours(binaryString);

        for (int i = 1; i <= splits.length; i++)
        {
            String c = splits[splits.length - i];

            while (c.contains("_") && len > 0)
            {
                int idx = c.lastIndexOf('_');
                c = c.substring(0, idx) + "0" + c.substring(idx + 1);
                len--;
            }

            splits[splits.length - i] = c;
        }

        String[] filler = new String[0];
        if (len > 0)
            filler = splitToFours("0".repeat(len));

        StringBuilder sb = new StringBuilder();
        sb.append(base.repeat(Math.max(0, numTimes - filler.length - splits.length)));

        for (String f : filler) {
            sb.append(f).append(" ");
        }


        for (String s : splits)
            sb.append(s).append(" ");


        return sb.toString();
    }

    /**
     * Compress the given 16-bit encoded string to 9-bit encoding
     *
     * @param uncompressedString Uncompressed 16-bit encoded string
     * @return Compressed 9-bit encoded string
     */
    protected static ArrayList<Integer> compress(Integer[] uncompressedString) throws RuntimeException
    {
        int container = 0;
        int bitshift = 0;

        ArrayList<Integer> compressed = new ArrayList<>();
        compressed.add(0xf100);

        for (int c : uncompressedString)
        {
            // only 9 bits are available per character, and all-ones (0x1ff) is reserved as the terminator
            // by decompress(), so anything outside 0x000-0x1fe would silently overwrite the characters
            // which follow it
            if (c < 0 || c >= 0x1ff)
            {
                throw new RuntimeException(String.format("character %04x cannot be represented in the 9-bit compressed encoding", c));
            }

            container |= c << bitshift;
            bitshift += 9;

            while (bitshift >= 15)
            {
                bitshift -= 15;
                compressed.add(container & 0x7fff);
                container >>= 15;
            }
        }

        if (bitshift != 0)
        {
            container |= 0xffff << bitshift;
            compressed.add(container & 0x7fff);
        }

        return compressed;
    }

    public TextBankData()
    {
        super();
        seed = 0;
    }

    public TextBankData(Collection<? extends Message> c)
    {
        super(c);
        seed = 0;
    }

    public TextBankData(TextBankData data)
    {
        super();
        addAll(data);
        seed = data.seed;
    }

    public List<String> getStringList()
    {
        List<String> output = new ArrayList<>();
        for (Message message : this)
            output.add(message.text);
        return output;
    }

    /**
     * Thrown when a text bank cannot be decoded from or encoded to the Pokemon Gen 4 text format.
     * The message names the offending bank and message so the caller can report it to the user.
     */
    public static class TextEncodingException extends RuntimeException
    {
        public TextEncodingException(String message)
        {
            super(message);
        }

        public TextEncodingException(String message, Throwable cause)
        {
            super(message, cause);
        }
    }

    public static class Message {
        String text;
        boolean compressed;

        public Message(String text, boolean compressed)
        {
            this.text = text;
            this.compressed = compressed;
        }

        public Message(String text)
        {
            this.text = text;
            compressed = false;
        }

        /**
         * Gets this <code>Message</code>'s text
         * @return a <code>String</code> containing this <code>Message</code>'s text
         */
        public String getText()
        {
            return text;
        }

        /**
         * Sets this <code>Message</code>'s text to the provided value
         * @param text a <code>String</code> containing the new text
         */
        public void setText(String text)
        {
            this.text = text;
        }

        /**
         * Gets whether this <code>Message</code> is 9-bit compressed
         * @return a <code>boolean</code> containing whether this <code>Message</code> is 9-bit compressed
         */
        public boolean isCompressed()
        {
            return compressed;
        }

        /**
         * Sets this <code>Message</code> to be marked as compressed
         * @param compressed a <code>boolean</code>
         */
        public void setCompressed(boolean compressed)
        {
            this.compressed = compressed;
        }


        /**
         * DO NOT CONFUSE WITH valueOf()
         */
        @Override
        public String toString()
        {
            return (compressed ? "[Compressed]" : "") + text;
        }

        @Override
        public boolean equals(Object o)
        {
            if(this == o) {
                return true;
            }
            if(o == null || getClass() != o.getClass()) {
                return false;
            }

            Message message = (Message) o;
            return compressed == message.compressed && text.equals(message.text);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(text, compressed);
        }
    }
}
