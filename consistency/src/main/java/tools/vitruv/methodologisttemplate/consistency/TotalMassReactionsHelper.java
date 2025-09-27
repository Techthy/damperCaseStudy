package tools.vitruv.methodologisttemplate.consistency;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import mafds.DamperSystem;
import tools.vitruv.methodologisttemplate.consistency.utils.StoexConsistencyHelper;
import tools.vitruv.stoex.stoex.Expression;
import uncertainty.Effect;
import uncertainty.Pattern;
import uncertainty.Uncertainty;
import uncertainty.UncertaintyAnnotationRepository;
import uncertainty.UncertaintyFactory;
import uncertainty.UncertaintyLocation;
import uncertainty.UncertaintyLocationType;
import uncertainty.UncertaintyPerspective;

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
        StoexConsistencyHelper helper = new StoexConsistencyHelper();

        helper.putVariable("massNew", affectedExpr);
        helper.putVariable("massOld", massOld);
        helper.putVariable("count", count);

        if (totalMassUncertainty == null) {
            totalMassUncertainty = UncertaintyReactionsHelper.deepCopyUncertainty(affectedUncertainty);
            totalMassUncertainty.setId(EcoreUtil.generateUUID());
            totalMassUncertainty.getUncertaintyLocation().setParameterLocation("totalMassInKg");
            totalMassUncertainty.getUncertaintyLocation().getReferencedComponents().add(affectedEObject.eContainer());
            helper.putVariable("totalMassInKg", springDamper.getTotalMassInKg());
        } else {
            helper.putVariable("totalMassInKg", totalMassUncertainty.getEffect().getExpression());
        }

        Expression delta = (Expression) helper.evaluateToStoexExpression("massNew - massOld");
        Expression totalDelta = sumExpressions(count, delta);
        helper.putVariable("totalDelta", totalDelta);
        Expression newTotalMassExpr = helper.evaluateToStoexExpression("totalMassInKg + totalDelta");
        totalMassUncertainty.getEffect().setExpression(newTotalMassExpr);
        springDamper.setTotalMassInKg(helper.getMean(newTotalMassExpr).doubleValue());

        if (!repo.getUncertainties().contains(totalMassUncertainty)) {
            repo.getUncertainties().add(totalMassUncertainty);
        }
        return totalMassUncertainty;
    }

    private static Expression sumExpressions(int k, Expression expr) {
        StoexConsistencyHelper helper = new StoexConsistencyHelper();
        Expression sum = expr;
        helper.putVariable("expr", expr);
        helper.putVariable("sum", 0);
        for (int i = 0; i < k; i++) {
            sum = helper.evaluateToStoexExpression("sum + expr");
            helper.putVariable("sum", sum);
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
        StoexConsistencyHelper helper = new StoexConsistencyHelper();

        helper.putVariable("newValue", affectedExpr);
        helper.putVariable("oldValue", oldValue);

        if (totalMassUncertainty == null) {
            totalMassUncertainty = UncertaintyReactionsHelper.deepCopyUncertainty(affectedUncertainty);
            totalMassUncertainty.setId(EcoreUtil.generateUUID());
            totalMassUncertainty.getUncertaintyLocation().setParameterLocation("totalMassInKg");
            totalMassUncertainty.getUncertaintyLocation().getReferencedComponents().add(affectedEObject.eContainer());
            helper.putVariable("totalMassInKg", springDamper.getTotalMassInKg());
        } else {
            helper.putVariable("totalMassInKg", totalMassUncertainty.getEffect().getExpression());
        }

        Expression newTotalMassExpr = (Expression) helper
                .evaluateToStoexExpression("totalMassInKg + newValue - oldValue");
        totalMassUncertainty.getEffect().setExpression(newTotalMassExpr);
        springDamper.setTotalMassInKg(helper.getMean(newTotalMassExpr).doubleValue());

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
