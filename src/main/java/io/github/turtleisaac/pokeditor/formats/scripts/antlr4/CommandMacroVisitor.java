package io.github.turtleisaac.pokeditor.formats.scripts.antlr4;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.scripts.MacrosBaseVisitor;
import io.github.turtleisaac.pokeditor.formats.scripts.MacrosLexer;
import io.github.turtleisaac.pokeditor.formats.scripts.MacrosParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class CommandMacroVisitor<T> extends MacrosBaseVisitor<T>
{
    @Override
    public T visitId_line(MacrosParser.Id_lineContext ctx)
    {
        boolean foundShort = false;
        boolean foundValue = false;
        int idNumber = -1;
        for (int i = 0; i < ctx.getChildCount(); i++)
        {
            ParseTree c = ctx.getChild(i);

            int type = ((TerminalNodeImpl) c).symbol.getType();
            if (type == MacrosLexer.SHORT)
            {
                foundShort = true;
            }
            else if (type == MacrosLexer.NUMBER) {
                foundValue = true;
                idNumber = Integer.parseInt(c.getText());
                break;
            }
        }

        if (foundShort && foundValue) {
            return idLineAction(idNumber);
        }

        return null;
    }

    protected T idLineAction(int idNumber) {
        return null;
    }

    @Override
    public T visitWrite(MacrosParser.WriteContext ctx)
    {
//        System.out.println(ctx.getText());
//        ParseTree c = ctx.getChild(0);

        int dataType = -1;
        boolean foundWriteValue = false;
        int writeValue = -1;
        for (int i = 0; i < ctx.getChildCount(); i++)
        {
            ParseTree c = ctx.getChild(i);

            if (c instanceof TerminalNodeImpl terminalNode)
            {
                int type = terminalNode.symbol.getType();
                if (type == MacrosLexer.BYTE)
                {
                    dataType = MacrosLexer.BYTE;
                }
                else if (type == MacrosLexer.SHORT)
                {
                    dataType = MacrosLexer.SHORT;
                }
                else if (type == MacrosLexer.WORD)
                {
                    dataType = MacrosLexer.WORD;
                }
                else if (type == MacrosLexer.NUMBER) {
                    foundWriteValue = true;
                    writeValue = Integer.parseInt(c.getText());
                    break;
                }
            }
            else if (c instanceof MacrosParser.InputContext inputContext) {
                return writeLineAction(ctx, inputContext, dataType);
            }

        }

        return null;
    }

    protected T writeLineAction(MacrosParser.WriteContext writeContext, MacrosParser.InputContext inputContext, int dataType) {
        return null;
    }

    @Override
    public T visitIf_block(MacrosParser.If_blockContext ctx)
    {
        boolean result = false;
        for (ParseTree child : ctx.children)
        {
            if (child instanceof MacrosParser.If_lineContext)
            {
                result = child.accept(new CommandMacroVisitor<>()
                {
                    @Override
                    public Boolean visitIf_line(MacrosParser.If_lineContext ctx)
                    {
                        return super.visitIf_line(ctx);
                    }
                });
            }
        }
        return super.visitIf_block(ctx);
    }

    @Override
    public T visitCompare(MacrosParser.CompareContext ctx)
    {
        return super.visitCompare(ctx);
    }
}
