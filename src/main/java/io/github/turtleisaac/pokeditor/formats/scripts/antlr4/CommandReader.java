package io.github.turtleisaac.pokeditor.formats.scripts.antlr4;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.scripts.macros.MacrosLexer;
import io.github.turtleisaac.pokeditor.formats.scripts.macros.MacrosParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandReader extends CommandMacroVisitor<Integer>
{
    private MemBuf.MemBufReader reader;
    private CommandMacro macro;

    private int parameterType;
    private int storedReadValue;
    private boolean compareMode;

    CommandReader(MemBuf.MemBufReader reader, CommandMacro commandMacro)
    {
        this.reader = reader;
        this.macro = commandMacro;
    }

    @Override
    protected Integer writeLineAction(MacrosParser.WriteContext writeContext, MacrosParser.AlgebraContext inputContext, int dataType)
    {
        parameterType = dataType;

        storedReadValue = switch (dataType)
        {
            case MacrosLexer.BYTE -> (int) reader.readUInt8();
            case MacrosLexer.SHORT -> reader.readUInt16();
            case MacrosLexer.WORD -> reader.readInt();
            default -> throw new IllegalStateException("Unexpected value: " + parameterType);
        };

        Integer ret = inputContext.accept(this);

//        Pattern pattern = Pattern.compile("\\\\[\\da-zA-Z_]+");
//        Matcher matcher = pattern.matcher(inputContext.getText());
//        if (matcher.find()) {
//            parameterToValueMap.put(matcher.group().substring(1), ret);
//        }
        return ret;
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
        ArrayList<ParseTree> inputNodes = new ArrayList<>();
        AlgebraicOperation operation = AlgebraicOperation.ERROR;
        for (ParseTree child : ctx.children) {
            if (child instanceof MacrosParser.Number_or_argumentContext || child instanceof MacrosParser.AlgebraContext) {
                inputNodes.add(child);
            }
            else if (child instanceof TerminalNodeImpl terminalNode)
            {
                if (terminalNode.symbol.getType() == MacrosLexer.ADD_SUBTRACT)
                {
                    if (terminalNode.getText().equals("-")) {
                        operation = AlgebraicOperation.ADD; // need to do the inverse of the specified operation since this is reading, not writing
                    } else {
                        operation = AlgebraicOperation.SUBTRACT;
                    }
                }
                else if (terminalNode.symbol.getType() == MacrosLexer.MULT_DIV)
                {
                    if (terminalNode.getText().equals("/")) {
                        operation = AlgebraicOperation.MULTIPLY; // need to do the inverse of the specified operation since this is reading, not writing
                    } else {
                        operation = AlgebraicOperation.DIVIDE;
                    }
                }
            }

        }

        if (inputNodes.size() == 2)
        {
            inputs.add(storedReadValue);
            inputs.add(inputNodes.get(1).accept(this));

            storedReadValue = performAlgebraicOperation(inputs, operation);
            inputNodes.get(0).accept(this);
        }

        return null;
    }



    @Override
    public Integer visitTerminal(TerminalNode node)
    {
        TerminalNodeImpl terminalNode = (TerminalNodeImpl) node;
        if ((terminalNode.symbol.getType() == MacrosLexer.ARGUMENT_USAGE)) {
            if (!compareMode) { // reading arg from file for the first time
                parameterToValueMap.put(terminalNode.getText().substring(1), storedReadValue);
            }
            else // this is a calculation which requires an already read value
            {
                return (int) parameterToValueMap.get(terminalNode.getText().substring(1));
            }
        } else if (terminalNode.symbol.getType() == MacrosLexer.NUMBER) {
            return Integer.parseInt(terminalNode.getText());
        } else if (terminalNode.symbol.getType() == MacrosLexer.CURRENT_OFFSET) {
            return reader.getPosition() - 4;
        }

        return null;
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
    public Integer visitDefinition(MacrosParser.DefinitionContext ctx)
    {
        ArrayList<String> parameters = new ArrayList<>();

        int argIndex = 0;

        for (int i = 0; i < ctx.getChildCount(); i++)
        {
            ParseTree c = ctx.getChild(i);

            if (c instanceof TerminalNodeImpl terminalNode) {
                int type = terminalNode.symbol.getType();
                if (type == MacrosLexer.NAME) {
                    if (c.getText().contains("scrcmd_465"))
                        System.currentTimeMillis();
                }
            }
            else if (c instanceof MacrosParser.Argument_definitionContext || c instanceof MacrosParser.Last_argument_definitionContext) {
                CommandReader selfReference = this;

                int finalArgIndex = argIndex;
                c.accept(new CommandMacroVisitor<String>()
                {
                    String visitArgument(ParserRuleContext ctx) {
                        for (ParseTree child : ctx.children) {
                            if (child instanceof MacrosParser.Number_or_argumentContext) {
                                parameterToValueMap.put(macro.getParameters()[finalArgIndex], child.accept(selfReference));
                            }
                        }
                        return null;
                    }

                    @Override
                    public String visitArgument_definition(MacrosParser.Argument_definitionContext ctx)
                    {
                        return visitArgument(ctx);
                    }

                    @Override
                    public String visitLast_argument_definition(MacrosParser.Last_argument_definitionContext ctx)
                    {
                        return visitArgument(ctx);
                    }
                });

                argIndex++;
            }
        }

        return null;
    }
}
