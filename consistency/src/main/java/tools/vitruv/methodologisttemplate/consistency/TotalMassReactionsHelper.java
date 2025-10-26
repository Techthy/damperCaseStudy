package tools.vitruv.methodologisttemplate.consistency;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import mafds.DamperSystem;
import tools.vitruv.stoex.interpreter.StoexEvaluator;
import tools.vitruv.stoex.stoex.Expression;
import uncertainty.Uncertainty;
import uncertainty.UncertaintyAnnotationRepository;
import uncertainty.UncertaintyLocationType;

/**
 * This test class tests the propagation of the total mass
 * the parameters damping constant, spring stiffness and total mass.
 * The first test, shows the propagation without using the UnCertaGator.
 * The second test shows the same propagation but with the UnCertaGator and
 * uncertainty annotations.
 * The third test shows the same propagation now using the UnCertaGator with the
 * StoEx extension.
 *
 *
 * @author Claus Hammann
 */
public class TotalMassReactionsHelper {
    private TotalMassReactionsHelper() {
        // Utility class
    }

    public static Uncertainty handleUncertaintyMultiple(UncertaintyAnnotationRepository repo,
            String parameterLocation, Integer count, Double massNew, Double massOld, EObject affectedEObject,
            DamperSystem springDamper) {

        Uncertainty affectedUncertainty = findUncertaintyByLocation(repo, parameterLocation, affectedEObject);
        if (affectedUncertainty == null || affectedUncertainty.getEffect() == null
                || affectedUncertainty.getEffect().getExpression() == null) {
            return null;
        }

        Uncertainty totalMassUncertainty = findUncertaintyByLocation(repo, "totalMassInKg",
                affectedEObject.eContainer());
        Expression affectedExpr = affectedUncertainty.getEffect().getExpression();
        StoexEvaluator stoexEvaluator = new StoexEvaluator();

        stoexEvaluator.setVariable("massNew", affectedExpr);
        stoexEvaluator.setVariable("massOld", massOld);
        stoexEvaluator.setVariable("count", count);

        if (totalMassUncertainty == null) {
            totalMassUncertainty = UncertaintyReactionsHelper.deepCopyUncertainty(affectedUncertainty);
            totalMassUncertainty.setId(EcoreUtil.generateUUID());
            totalMassUncertainty.getUncertaintyLocation().setParameterLocation("totalMassInKg");
            totalMassUncertainty.getUncertaintyLocation().getReferencedComponents().add(affectedEObject.eContainer());
            stoexEvaluator.setVariable("totalMassInKg", springDamper.getTotalMassInKg());
        } else {
            stoexEvaluator.setVariable("totalMassInKg", totalMassUncertainty.getEffect().getExpression());
        }

        Expression delta = (Expression) stoexEvaluator.evaluate("massNew - massOld");
        Expression totalDelta = sumExpressions(count, delta);
        stoexEvaluator.setVariable("totalDelta", totalDelta);
        Expression newTotalMassExpr = stoexEvaluator.evaluate("totalMassInKg + totalDelta");
        totalMassUncertainty.getEffect().setExpression(newTotalMassExpr);
        springDamper.setTotalMassInKg(stoexEvaluator.getMean(newTotalMassExpr).doubleValue());

        if (!repo.getUncertainties().contains(totalMassUncertainty)) {
            repo.getUncertainties().add(totalMassUncertainty);
        }
        return totalMassUncertainty;
    }

    private static Expression sumExpressions(int k, Expression expr) {
        StoexEvaluator stoexEvaluator = new StoexEvaluator();
        Expression sum = expr;
        stoexEvaluator.setVariable("expr", expr);
        stoexEvaluator.setVariable("sum", 0);
        for (int i = 0; i < k; i++) {
            sum = stoexEvaluator.evaluate("sum + expr");
            stoexEvaluator.setVariable("sum", sum);
        }
        return sum;
    }

    public static Uncertainty handleUncertainty(UncertaintyAnnotationRepository repo, String parameterLocation,
            Double oldValue, Double newValue, EObject affectedEObject, DamperSystem springDamper) {

        Uncertainty affectedUncertainty = findUncertaintyByLocation(repo, parameterLocation, affectedEObject);
        if (affectedUncertainty == null || affectedUncertainty.getEffect() == null
                || affectedUncertainty.getEffect().getExpression() == null) {
            return null;
        }

        Uncertainty totalMassUncertainty = findUncertaintyByLocation(repo, "totalMassInKg",
                affectedEObject.eContainer());
        Expression affectedExpr = affectedUncertainty.getEffect().getExpression();
        StoexEvaluator stoexEvaluator = new StoexEvaluator();

        stoexEvaluator.setVariable("newValue", affectedExpr);
        stoexEvaluator.setVariable("oldValue", oldValue);

        if (totalMassUncertainty == null) {
            totalMassUncertainty = UncertaintyReactionsHelper.deepCopyUncertainty(affectedUncertainty);
            totalMassUncertainty.setId(EcoreUtil.generateUUID());
            totalMassUncertainty.getUncertaintyLocation().setParameterLocation("totalMassInKg");
            totalMassUncertainty.getUncertaintyLocation().getReferencedComponents().add(affectedEObject.eContainer());
            stoexEvaluator.setVariable("totalMassInKg", springDamper.getTotalMassInKg());
        } else {
            stoexEvaluator.setVariable("totalMassInKg", totalMassUncertainty.getEffect().getExpression());
        }

        Expression newTotalMassExpr = (Expression) stoexEvaluator
                .evaluate("totalMassInKg + newValue - oldValue");
        totalMassUncertainty.getEffect().setExpression(newTotalMassExpr);
        springDamper.setTotalMassInKg(stoexEvaluator.getMean(newTotalMassExpr).doubleValue());

        if (!repo.getUncertainties().contains(totalMassUncertainty)) {
            repo.getUncertainties().add(totalMassUncertainty);
        }
        return totalMassUncertainty;
    }

    private static Uncertainty findUncertaintyByLocation(UncertaintyAnnotationRepository repo,
            String parameterLocation,
            EObject referencedObject) {
        return repo.getUncertainties().stream()
                .filter(u -> u.getUncertaintyLocation().getParameterLocation().equals(parameterLocation)
                        && u.getUncertaintyLocation()
                                .getLocation() == UncertaintyLocationType.PARAMETER)
                .findFirst()
                .orElse(null);
    }
}
