package io.github.turtleisaac.pokeditor.formats.scripts.antlr4;

import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.scripts.MacrosLexer;
import io.github.turtleisaac.pokeditor.formats.scripts.MacrosParser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandReader extends CommandMacroVisitor<Integer>
{
    private MemBuf.MemBufReader reader;
    private CommandMacro macro;

    HashMap<String, Number> parameterToValueMap = new HashMap<>();

    private int parameterType;

    CommandReader(MemBuf.MemBufReader reader, CommandMacro commandMacro)
    {
        this.reader = reader;
        this.macro = commandMacro;
    }

    @Override
    protected Integer writeLineAction(MacrosParser.WriteContext writeContext, MacrosParser.InputContext inputContext, int dataType)
    {
        if (macro.getName().contains("goto"))
        {
            System.currentTimeMillis();
        }
        parameterType = dataType;
        Integer ret = inputContext.accept(this);

        Pattern pattern = Pattern.compile("\\\\[\\da-zA-Z_]+");
        Matcher matcher = pattern.matcher(inputContext.getText());
        if (matcher.find()) {
            parameterToValueMap.put(matcher.group().substring(1), ret);
        }
        return ret;
    }

    @Override
    public Integer visitAlgebra(MacrosParser.AlgebraContext ctx)
    {
        if (ctx.children.size() == 1) {
            return super.visitAlgebra(ctx);
        }
        ArrayList<Integer> inputs = new ArrayList<>();
        int operation = -1;
        for (ParseTree child : ctx.children) {
            if (child instanceof MacrosParser.Number_or_argumentContext || child instanceof MacrosParser.AlgebraContext) {
                inputs.add(child.accept(this));
            }
            else if (child instanceof TerminalNodeImpl terminalNode)
            {
                if (terminalNode.symbol.getType() == MacrosLexer.ADD_SUBTRACT)
                {
                    if (terminalNode.getText().equals("-")) {
                        operation = 0;
                    } else {
                        operation = 1;
                    }
                }
                else if (terminalNode.symbol.getType() == MacrosLexer.MULT_DIV)
                {
                    if (terminalNode.getText().equals("/")) {
                        operation = 2;
                    } else {
                        operation = 3;
                    }
                }
            }

        }

        switch (operation) {
            case 0:
                return inputs.get(0) + inputs.get(1);
            case 1:
                return inputs.get(0) - inputs.get(1);
            case 2:
                return inputs.get(0) * inputs.get(1);
            case 3:
                return inputs.get(0) / inputs.get(1);
        }
        return null;
//        return super.visitAlgebra(ctx);
    }

    @Override
    public Integer visitTerminal(TerminalNode node)
    {
        TerminalNodeImpl terminalNode = (TerminalNodeImpl) node;
        if ((terminalNode.symbol.getType() == MacrosLexer.ARGUMENT_USAGE)) {
            //            System.out.println("read var: " + val);
            return switch (parameterType)
            {
                case MacrosLexer.BYTE -> (int) reader.readUInt8();
                case MacrosLexer.SHORT -> reader.readUInt16();
                case MacrosLexer.WORD -> reader.readInt();
                default -> throw new IllegalStateException("Unexpected value: " + parameterType);
            };
        } else if (terminalNode.symbol.getType() == MacrosLexer.NUMBER) {
            int val = Integer.parseInt(terminalNode.getText());
//            System.out.println("read value: " + val);
            return Integer.parseInt(terminalNode.getText());
        } else if (terminalNode.symbol.getType() == MacrosLexer.CURRENT_OFFSET) {
//            System.out.println("current pos: " + reader.getPosition());
            return reader.getPosition() - 4;
        }

        return null;
    }
}
