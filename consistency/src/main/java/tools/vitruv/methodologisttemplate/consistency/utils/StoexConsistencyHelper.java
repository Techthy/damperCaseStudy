package tools.vitruv.methodologisttemplate.consistency.utils;

import java.util.HashMap;
import java.util.Map;

import tools.vitruv.stoex.interpreter.StoexEvaluator;
import tools.vitruv.stoex.stoex.DoubleLiteral;
import tools.vitruv.stoex.stoex.Expression;
import tools.vitruv.stoex.stoex.StoexFactory;

/**
 * Utility class for using Stoex expressions in consistency transformations.
 * This class provides helper methods to evaluate stoex expressions in the
 * context
 * of brake system uncertainty analysis and CAD model synchronization.
 */
public class StoexConsistencyHelper {

    private final StoexEvaluator stoexEvaluator;
    private final Map<String, Object> variables;

    public StoexConsistencyHelper() {
        this.stoexEvaluator = new StoexEvaluator();
        this.variables = new HashMap<>();
    }

    public void putVariable(String name, Object value) {
        variables.put(name, value);
    }

    public Number getMean(Expression expression) {
        return stoexEvaluator.getMean(expression);
    }

    /**
     * Evaluates a stoex expression with the given variables.
     * 
     * @param expression The stoex expression as a string
     * @return The evaluation result
     */
    public String evaluateExpression(String expression) {
        Object result = stoexEvaluator.evaluate(expression, variables);
        return stoexEvaluator.serialize(result);
    }

    public Expression evaluateToStoexExpression(String expressionString) {
        Object result = stoexEvaluator.evaluate(expressionString, variables);
        if (result instanceof Expression expression) {
            return expression;
        } else if (result instanceof Number number) {
            DoubleLiteral literal = StoexFactory.eINSTANCE.createDoubleLiteral();
            literal.setValue(number.doubleValue());
            return literal;
        } else {
            throw new IllegalArgumentException("Expression did not evaluate to a valid Stoex Expression or Number.");
        }

    }

    public String serializeToStoexExpression(Object expression) {
        return stoexEvaluator.serialize(expression);
    }

}
