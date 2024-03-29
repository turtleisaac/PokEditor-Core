package io.github.turtleisaac.pokeditor.formats.scripts.antlr4;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.scripts.macros.MacrosBaseVisitor;
import io.github.turtleisaac.pokeditor.formats.scripts.macros.MacrosLexer;
import io.github.turtleisaac.pokeditor.formats.scripts.macros.MacrosParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandWriter extends CommandMacroVisitor<Integer>
{
    private MemBuf.MemBufWriter writer;
    private CommandMacro macro;
    private LabelOffsetObtainer offsetObtainer;

    private int parameterType;
    private boolean compareMode;

    public CommandWriter(MemBuf.MemBufWriter writer, LabelOffsetObtainer offsetObtainer, Map<String, Object> parameterToValueMap)
    {
        this.writer = writer;
        this.parameterToValueMap = parameterToValueMap;
        this.offsetObtainer = offsetObtainer;
    }

    @Override
    protected Integer idLineAction(int idNumber)
    {
        writer.writeShort((short) idNumber);
        return null;
    }

    @Override
    protected Integer writeLineAction(MacrosParser.WriteContext writeContext, MacrosParser.AlgebraContext inputContext, int dataType)
    {
        parameterType = dataType;

        int val = inputContext.accept(this);

        switch (parameterType) {
            case MacrosLexer.BYTE -> writer.write((byte) val);
            case MacrosLexer.SHORT -> writer.writeShort((short) val);
            case MacrosLexer.WORD -> writer.writeInt(val);
            default -> throw new IllegalStateException("Unexpected value: " + parameterType);
        }

        return 0;
    }

    @Override
    public Integer visitTerminal(TerminalNode node)
    {
        TerminalNodeImpl terminalNode = (TerminalNodeImpl) node;
        String text = terminalNode.getText().substring(1);
        if ((terminalNode.symbol.getType() == MacrosLexer.ARGUMENT_USAGE)) {
            if (!compareMode) { // writing arg value to file

                Object value = parameterToValueMap.get(text);
                int valueToWrite;

                if (value instanceof Number number)
                    valueToWrite = (int) number;
                else
                {
                    valueToWrite = offsetObtainer.accept((String) value);
                }

                return switch (parameterType)
                {
                    case MacrosLexer.BYTE, MacrosLexer.SHORT, MacrosLexer.WORD -> valueToWrite;
                    default -> throw new IllegalStateException("Unexpected value: " + parameterType);
                };
            }
            else // this is a calculation which requires an already read value
            {
                return (int) parameterToValueMap.get(text);
            }
        } else if (terminalNode.symbol.getType() == MacrosLexer.NUMBER) {
            return Integer.parseInt(terminalNode.getText());
        } else if (terminalNode.symbol.getType() == MacrosLexer.CURRENT_OFFSET) {
            return writer.getPosition();
        }

        return null;
    }

    @Override
    public Integer visitAlgebra(MacrosParser.AlgebraContext ctx)
    {
        if (ctx.children.size() == 1) {
            return super.visitAlgebra(ctx);
        }
        else if (ctx.children.size() == 3)
        {
            Integer result = attemptInterceptAlgebraicParenthesesWrapping(ctx);
            if (result != null) // if the result is null then there was no wrapped parentheses here, so continue to normal calculation
                return result;
        }

        ArrayList<Integer> inputs = new ArrayList<>();
        AlgebraicOperation operation = AlgebraicOperation.ERROR;
        for (ParseTree child : ctx.children) {
            if (child instanceof MacrosParser.Number_or_argumentContext || child instanceof MacrosParser.AlgebraContext) {
                inputs.add(child.accept(this));
            }
            else if (child instanceof TerminalNodeImpl terminalNode)
            {
                if (terminalNode.symbol.getType() == MacrosLexer.ADD_SUBTRACT)
                {
                    if (terminalNode.getText().equals("-")) {
                        operation = AlgebraicOperation.SUBTRACT;
                    } else {
                        operation = AlgebraicOperation.ADD;
                    }
                }
                else if (terminalNode.symbol.getType() == MacrosLexer.MULT_DIV)
                {
                    if (terminalNode.getText().equals("/")) {
                        operation = AlgebraicOperation.DIVIDE;
                    } else {
                        operation = AlgebraicOperation.MULTIPLY;
                    }
                }
            }

        }

        return performAlgebraicOperation(inputs, operation);
    }

    @Override
    public Integer visitIf_block(MacrosParser.If_blockContext ctx)
    {
        boolean result = false;
        for (ParseTree child : ctx.children)
        {
            if (child instanceof MacrosParser.If_lineContext ifContext)
            {
                for (ParseTree ifChild : ifContext.children)
                {
                    if (ifChild instanceof MacrosParser.CompareContext)
                    {
                        result = ifChild.accept(this) != 0;
                        break;
                    }

                }
            }
            else if (result && !(child instanceof MacrosParser.Else_blockContext)) {
                child.accept(this);
            }
            else if (!result && child instanceof MacrosParser.Else_blockContext elseBlockContext) {
                for (ParseTree elseChild : elseBlockContext.children) {
                    if (!(elseChild instanceof MacrosParser.Else_lineContext) && !(elseChild instanceof MacrosParser.Endif_lineContext))
                    {
                        elseChild.accept(this);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Integer visitCompare(MacrosParser.CompareContext ctx)
    {
        if (ctx.children.size() == 1) {
            int val = super.visitCompare(ctx);
            return val;
        }

        ArrayList<Integer> compareInputs = new ArrayList<>();

        int operation = -1;
        for (ParseTree child : ctx.children)
        {
            if (child instanceof MacrosParser.AlgebraContext || child instanceof MacrosParser.CompareContext) {
                compareMode = true;
                compareInputs.add(child.accept(this));
                compareMode = false;
            }
            else if (child instanceof TerminalNodeImpl terminalNode) {
                int type = terminalNode.symbol.getType();
                if (type == MacrosLexer.COMPARATOR) {
                    String comparator = child.getText();
                    switch (comparator) {
                        case "<" -> operation = 0;
                        case "==" -> operation = 1;
                        case ">" -> operation = 2;
                        case "<=" -> operation = 3;
                        case ">=" -> operation = 4;
                        case "!=" -> operation = 5;
                    }
                } else if (type == MacrosLexer.AND_OR) {
                    if (child.getText().equals("&&")) {
                        operation = 6;
                    }
                    else {
                        operation = 7;
                    }
                }
            }
        }

        switch (operation) {
            case 0 -> {
                return compareInputs.get(0) < compareInputs.get(1) ? 1 : 0;
            }
            case 1 -> {
                return Objects.equals(compareInputs.get(0), compareInputs.get(1)) ? 1 : 0;
            }
            case 2 -> {
                return (compareInputs.get(0) > compareInputs.get(1)) ? 1 : 0;
            }
            case 3 -> {
                return (compareInputs.get(0) <= compareInputs.get(1)) ? 1 : 0;
            }
            case 4 -> {
                return (compareInputs.get(0) >= compareInputs.get(1)) ? 1 : 0;
            }
            case 5 -> {
                return ( !Objects.equals(compareInputs.get(0), compareInputs.get(1)) ) ? 1 : 0;
            }
            case 6 -> {
                return ( (compareInputs.get(0) != 0) && (compareInputs.get(1) != 0) ) ? 1 : 0;
            }
            case 7 -> {
                return ( (compareInputs.get(0) != 0) || (compareInputs.get(1) != 0) ) ? 1 : 0;
            }
        }

        return 0;
    }

    @Override
    protected Integer defaultResult()
    {
        return 0;
    }

    public interface LabelOffsetObtainer
    {
        int accept(String labelName);
    }
}
