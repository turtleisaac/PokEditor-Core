package io.github.turtleisaac.pokeditor.formats.scripts.antlr4;

import io.github.turtleisaac.pokeditor.formats.scripts.macros.MacrosBaseVisitor;
import io.github.turtleisaac.pokeditor.formats.scripts.macros.MacrosLexer;
import io.github.turtleisaac.pokeditor.formats.scripts.macros.MacrosParser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.*;

public abstract class CommandMacroVisitor<T> extends MacrosBaseVisitor<T>
{
    Map<String, Object> parameterToValueMap = new HashMap<>();;

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
            if (type == MacrosLexer.SHORT || type == MacrosLexer.WORD)
            {
                foundShort = true;
            }
            else if (type == MacrosLexer.NUMBER) {
                foundValue = true;
                idNumber = Integer.decode(c.getText());
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
        int dataType = -1;
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
            }
            else if (c instanceof MacrosParser.AlgebraContext inputContext) {
                return writeLineAction(ctx, inputContext, dataType);
            }

        }

        return null;
    }

    protected T writeLineAction(MacrosParser.WriteContext writeContext, MacrosParser.AlgebraContext inputContext, int dataType) {
        return null;
    }

    public T attemptInterceptAlgebraicParenthesesWrapping(MacrosParser.AlgebraContext ctx)
    {
        int openParenthesesIdx = -1;
        int contentsIdx = -1;
        int closeParenthesesIdx = -1;

        MacrosParser.AlgebraContext inputContext = null;

        int idx = 0;
        for (ParseTree child : ctx.children)
        {
            if (child instanceof TerminalNodeImpl terminalNode)
            {
                if (terminalNode.symbol.getType() == MacrosLexer.OPEN_PARENTHESES)
                {
                    openParenthesesIdx = idx;
                }
                else if (terminalNode.symbol.getType() == MacrosLexer.CLOSE_PARENTHESES)
                {
                    closeParenthesesIdx = idx;
                }
            }
            else if (child instanceof MacrosParser.AlgebraContext foundContext)
            {
                if (contentsIdx != -1)
                    return null;
                contentsIdx = idx;
                inputContext = foundContext;
            }
            idx++;
        }

        if (openParenthesesIdx != -1 && closeParenthesesIdx != -1 && inputContext != null)
        {
            if (openParenthesesIdx < contentsIdx && closeParenthesesIdx > contentsIdx)
            {
                return inputContext.accept(this);
            }
        }
        else if (openParenthesesIdx == -1 && closeParenthesesIdx == -1) // ignore cases where there were no parentheses, as this was not a parentheses attempt in actuality
        {
            return null;
        }
        else if (inputContext != null)
        {
            return inputContext.accept(this);
        }
        else {
            throw new RuntimeException("An error occurred while attempting to parse an input for an algebraic operation.");
        }

        return null;
    }

    static Integer performAlgebraicOperation(ArrayList<Integer> inputs, AlgebraicOperation operation)
    {
        return switch (operation) {
            case ADD -> inputs.get(0) + inputs.get(1);
            case SUBTRACT -> inputs.get(0) - inputs.get(1);
            case MULTIPLY -> inputs.get(0) * inputs.get(1);
            case DIVIDE -> inputs.get(0) / inputs.get(1);
            default -> null;
        };
    }

    public enum AlgebraicOperation
    {
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE,
        ERROR
    }
}
