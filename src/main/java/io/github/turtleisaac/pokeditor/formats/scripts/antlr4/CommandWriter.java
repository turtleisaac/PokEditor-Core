package io.github.turtleisaac.pokeditor.formats.scripts.antlr4;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.scripts.MacrosBaseVisitor;
import io.github.turtleisaac.pokeditor.formats.scripts.MacrosLexer;
import io.github.turtleisaac.pokeditor.formats.scripts.MacrosParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

public class CommandWriter extends CommandMacroVisitor<MemBuf>
{
    private MemBuf memBuf;
    private MemBuf.MemBufWriter writer;

    public CommandWriter(MemBuf memBuf)
    {
        this.memBuf = memBuf;
        writer = memBuf.writer();
    }

    @Override
    protected MemBuf idLineAction(int idNumber)
    {
        writer.writeShort((short) idNumber);
        return memBuf;
    }

    @Override
    protected MemBuf defaultResult()
    {
        return memBuf;
    }

    public static void main(String[] args)
    {
        MacrosLexer lexer = new MacrosLexer(CharStreams.fromString("\t.macro scrcmd_465 arg0, arg1=0, arg2=0\n" +
                "\t.short 465\n" +
                "\t.short \\arg0\n" +
                "\t.if \\arg0 <= 3\n" +
                "\t\t.short \\arg1\n" +
                "\t\t.short \\arg2\n" +
                "\t.else\n" +
                "\t\t.if \\arg0 != 6\n" +
                "\t\t\t.short \\arg1\n" +
                "\t\t.endif\n" +
                "\t.endif\n" +
                "\t.endm\n" +
                "\n" +
                "\t.macro scrcmd_466 arg0, arg1\n" +
                "\t.short 466\n" +
                "\t.short \\arg0\n" +
                "\t.short \\arg1\n" +
                "\t.endm"));

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MacrosParser parser = new MacrosParser(tokens);
//        ParseTree parseTree = parser.entry();

        MacrosParser.EntriesContext entryContext = parser.entries();
        CommandWriter visitor = new CommandWriter(MemBuf.create());

        visitor.visitEntries(entryContext);
    }
}
