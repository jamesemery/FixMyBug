package carleton.comps;

import carleton.comps.javaparser.JavaBaseListener;
import carleton.comps.javaparser.JavaParser;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by alexgriese on 2/4/17.
 */
public class IdentifierListener extends JavaBaseListener {
    public List<Integer> classIds = new ArrayList<Integer>();
    public List<Integer> function = new ArrayList<Integer>();
    public List<Integer> variable = new ArrayList<Integer>();
    public List<Integer> outside = new ArrayList<Integer>();
    public List<Integer> variableClass = new ArrayList<Integer>();
    public List<Integer> variableFunction = new ArrayList<Integer>();
    public List<Integer> functionVariableClass = new ArrayList<Integer>();
    public List<String> identifierPosition = new ArrayList<String>();

    @Override
    public void enterClassIdentifier(JavaParser.ClassIdentifierContext ctx) {
        classIds.add(ctx.getStart().getTokenIndex());
        identifierPosition.add("class");
    }
    @Override
    public void enterFunctionIdentifier(JavaParser.FunctionIdentifierContext ctx) {
        identifierPosition.add("function");
        function.add(ctx.getStart().getTokenIndex());
    }
    @Override
    public void enterVariableIdentifier(JavaParser.VariableIdentifierContext ctx) {
        identifierPosition.add("variable");
        variable.add(ctx.getStart().getTokenIndex());
    }
    @Override
    public void enterOutsideIdentifier(JavaParser.OutsideIdentifierContext ctx) {
        identifierPosition.add("outside");
        outside.add(ctx.getStart().getTokenIndex());
    }
    @Override
    public void enterVariableClassIdentifier(JavaParser.VariableClassIdentifierContext ctx) {
        identifierPosition.add("variableClass");
        variableClass.add(ctx.getStart().getTokenIndex());
    }
    @Override
    public void enterVariableFunctionIdentifier(JavaParser.VariableFunctionIdentifierContext ctx) {
        identifierPosition.add("variableFunction");
        variableFunction.add(ctx.getStart().getTokenIndex());
    }
    @Override
    public void enterFunctionVariableClassIdentifier(JavaParser.FunctionVariableClassIdentifierContext ctx) {
        identifierPosition.add("functionVariableClass");
        functionVariableClass.add(ctx.getStart().getTokenIndex());
    }
}