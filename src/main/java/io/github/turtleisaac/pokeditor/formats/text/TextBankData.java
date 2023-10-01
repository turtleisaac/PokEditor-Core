package io.github.turtleisaac.pokeditor.formats.text;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.GameFiles;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import org.checkerframework.checker.units.qual.A;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * The VAST majority of the code in this class was originally written by JackHack96 for
 * an unreleased Java port of DSPRE in 2020. The code was then obtained by turtleisaac for
 * use in PokEditor-v2. The version seen here has been adapted to conform to the
 * GenericFileData and GenericParser schema for PokEditor-Core. All credits for this
 * code realistically belong to JackHack96 as I am way too dumb to have figured it all
 * out myself, even with pseudocode for the algorithm being available on
 * the projectpokemon rawdb.
 *
 * This class handles Pokémon Gen 4 text encoding/decoding.
 */
public class TextBankData extends ArrayList<TextBankData.Message> implements GenericFileData
{
    private int seed;

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

    public TextBankData(Map<GameFiles, byte[]> files)
    {
        super();
        setData(files);
    }

    @Override
    public void setData(Map<GameFiles, byte[]> files)
    {
        if (!files.containsKey(GameFiles.TEXT))
        {
            throw new RuntimeException("Text file not provided to editor");
        }

        MemBuf dataBuf = MemBuf.create(files.get(GameFiles.TEXT));
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
                System.out.println(messageIdx);
                compressed = true;
                binaryStrings[messageIdx] = decompress(binaryStrings[messageIdx]);
                sizes[messageIdx] = binaryStrings[messageIdx].length;
            }

            StringBuilder text = new StringBuilder();
            for (int j = 0; j < binaryStrings[messageIdx].length; j++) {
                int c = binaryStrings[messageIdx][j];
                if (c == 0xffff) {
                    // if the character is 0xffff, the string finishes here
                    break;
                } else if (c == 0xfffe) {
                    // if the character is 0xfffe, then it's a special VAR vase
                    text.append("VAR(");
                    StringBuilder args = new StringBuilder();
                    args.append(binaryStrings[messageIdx][++j]);
                    int argNum = binaryStrings[messageIdx][++j];
                    for (int k = 0; k < argNum; k++) {
                        args.append(", ");
                        args.append(binaryStrings[messageIdx][++j]);
                    }
                    args.append(")");
                    text.append(args);
                } else {
                    try {
                        text.append(characters.get("getChar").get(String.valueOf(c)).asText());
                    } catch (Exception e) {
                        text.append(String.format("\\?%04x", c));
                    }
                }
            }

            add(new Message(text.toString(), compressed));
        }
    }

    @Override
    public Map<GameFiles, byte[]> save()
    {
        MemBuf dataBuf = MemBuf.create();
        MemBuf.MemBufWriter writer = dataBuf.writer();

        List<Integer> tmpBinaryString;
        ArrayList<List<Integer>> binaryStrings = new ArrayList<>();

        int idx = 0;
        // encode text and write it
        for (Message msg : this)
        {
            String message = msg.text;
            tmpBinaryString = new ArrayList<>();

            // encode the binary strings
            for (int j = 0; j < message.length(); j++)
            {
                String sub = message.substring(j, Math.min(j + 4, message.length()));
                if (message.charAt(j) == '\\')
                {
                    switch (message.charAt(j + 1)) {
                        case 'r':
                            tmpBinaryString.add(0x25bc);
                            break;
                        case 'n':
                            tmpBinaryString.add(0xe000);
                            break;
                        case 'f':
                            tmpBinaryString.add(0x25bd);
                            break;
                        default:
                            tmpBinaryString.add(Integer.parseInt(message.substring(j + 2, j + 6), 16));
                            j += 4;
                            break;
                    }
                    j++;
                }
                else if (sub.equals("VAR("))
                {
                    int endOfVar = message.indexOf(')', j);
                    if (endOfVar == -1) {
                        throw new RuntimeException("Could not find end of VAR()");
                    }

                    String[] args = message.substring(j + 4, endOfVar).split(",");
                    tmpBinaryString.add(0xfffe);
                    tmpBinaryString.add(Integer.parseInt(args[0].trim()));
                    tmpBinaryString.add(args.length - 1);

                    for (int x = 1; x < args.length; x++) {
                        tmpBinaryString.add(Integer.parseInt(args[x].trim()));
                    }

                    j = endOfVar;
                }
                else if (sub.equals("[PK]") || sub.equals("[MN]"))
                {
                    tmpBinaryString.add(characters.get("getInt").get(sub).asInt());
                    j += 3;
                }
                else
                {
                    int val = 0;
                    try {
                        val = characters.get("getInt").get(String.valueOf(message.charAt(j))).asInt();
                    }
                    catch(NullPointerException e) {
                        e.printStackTrace();
                    }

                    tmpBinaryString.add(val);
                }
            }

            if (msg.compressed) {
                ArrayList<Integer> compressedBytes = compress(tmpBinaryString.toArray(new Integer[0]));
                binaryStrings.add(compressedBytes);
            }
            else {
                binaryStrings.add(tmpBinaryString);
            }
            idx++;
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
        for (int i = 0; i < binaryStrings.size(); i++)
        {
            key = (0x91bd3 * (i + 1)) & 0xffff;
            for (int j = 0; j < binaryStrings.get(i).size(); j++)
            {
                writer.writeShort((short) (binaryStrings.get(i).get(j) ^ key));
                key = (key + 0x493d) & 0xffff;
            }
            writer.writeShort((short) (0xffff ^ key));
        }

        return Collections.singletonMap(GameFiles.TEXT, dataBuf.reader().getBuffer());
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
            System.out.print("Adding << " + bitshift +  ": ");
            printBinaryString(compressedString[i]);
            container |= compressedString[i] << bitshift;
            printBinaryString(container);
            bitshift += 0xf;
            while (bitshift >= 0x9)
            {
                bitshift -= 0x9;
                if ((container & 0x1ff) == 0x1ff) {
                    newBinaryString[index++] = 0xffff;
//                    return newBinaryString;
                }
                else {
                    newBinaryString[index++] = container & 0x1ff;
                }

                container >>= 9;
                System.out.println("Shifting >> 9");
                printBinaryString(container);
            }
        }

        return newBinaryString;
    }

    private static void printBinaryString(int num)
    {
        System.out.println(formatBinaryString(num));
    }

    private static String[] splitToFours(int num)
    {
        String s = Integer.toBinaryString(num);
        int r = s.length() % 4;

        ArrayList<String> splits = new ArrayList<>();

        String sb = "_".repeat(4 - r) + s.substring(0, r);
        splits.add(sb);

        for (int i = r; i < s.length(); i += 4)
        {
            splits.add(s.substring(i, i + 4));
        }

        return splits.toArray(String[]::new);
    }

    private static String formatBinaryString(int num)
    {
        String base = "____";
        int numTimes = 8;
        String[] holders = new String[numTimes];
        Arrays.fill(holders, base);

        String[] splits = splitToFours(num);

        for (int i = 1; i <= splits.length; i++)
        {
            if (splits[splits.length - i].length() == 4)
            {
                holders[holders.length - i] = splits[splits.length - i];
            }
            else
            {

            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < holders.length; i++)
        {
            sb.append(holders[i]);
            if (i != holders.length - 1)
            {
                sb.append(" ");
            }
        }

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
        int index = 0;

        int maxCompressedLength = (uncompressedString.length / 2) + 3;
        Integer[] newBinaryString = new Integer[maxCompressedLength];
        Arrays.fill(newBinaryString, 0);
        newBinaryString[0] = 0xf100;

        for (int i = 1; i < newBinaryString.length; i++)
        {
            int c = uncompressedString[index];
            if ((c >> 9) == 1) {
                throw new RuntimeException(String.format("%04x cannot be compressed", c));
            }


            container |= c << bitshift;
            bitshift += 9;

            while (bitshift >= 0xf)
            {
                bitshift -= 0xf;
                newBinaryString[++index] = container & 0xffff;
                container >>= 0xf;
            }
        }
        container |= 0xffffffff << bitshift;
        newBinaryString[++index] = container & 0xffff;
        container >>= 9;
        newBinaryString[++index] = container & 0xffff;

        return new ArrayList<>(Arrays.asList(newBinaryString));


//        ArrayList<Integer> newBinaryString = new ArrayList<>();
//        newBinaryString.add(0xf100);

//        int bit = 0;
//        int currentOutputChar;
//        int srcIndex = 0;
//        do {
//            currentOutputChar = ((uncompressedString[srcIndex] >> bit) & 0x1FF);
//            bit += 9;
//            if (bit >= 15)
//            {
//                srcIndex++;
//                bit -= 15;
//                if (bit != 0 && srcIndex < uncompressedString.length)
//                {
//                    currentOutputChar |= (uncompressedString[srcIndex] << (9 - bit)) & 0x1FF;
//                }
//            }
//            currentOutputChar &= 0xFFFF;
//            newBinaryString.add(currentOutputChar);
//        } while (srcIndex < uncompressedString.length && currentOutputChar != 0x1FF);


//        for (int i = 1; i < co)

//        return newBinaryString;
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
    }
}
