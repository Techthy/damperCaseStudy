package tools.vitruv.methodologisttemplate.consistency.utils;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import mafds.DamperSystem;
import tools.vitruv.stoex.stoex.Expression;
import uncertainty.Effect;
import uncertainty.Pattern;
import uncertainty.Uncertainty;
import uncertainty.UncertaintyAnnotationRepository;
import uncertainty.UncertaintyFactory;
import uncertainty.UncertaintyLocation;
import uncertainty.UncertaintyLocationType;
import uncertainty.UncertaintyPerspective;

public class ReactionsHelper {
    private ReactionsHelper() {
        // Utility class
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
            totalMassUncertainty = copyUncertainty(affectedUncertainty);
            totalMassUncertainty.setId(EcoreUtil.generateUUID());
            totalMassUncertainty.getUncertaintyLocation().setParameterLocation("totalMassInKg");
            totalMassUncertainty.getUncertaintyLocation().getReferencedComponents().add(affectedEObject.eContainer());
            helper.putVariable("totalMassInKg", springDamper.getTotalWeightInKg());
        } else {
            helper.putVariable("totalMassInKg", totalMassUncertainty.getEffect().getExpression());
        }

        Expression newTotalMassExpr = (Expression) helper
                .evaluateToStoexExpression("totalMassInKg + newValue - oldValue");
        totalMassUncertainty.getEffect().setExpression(newTotalMassExpr);
        springDamper.setTotalWeightInKg(helper.getMean(newTotalMassExpr).doubleValue());

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

    private static Uncertainty copyUncertainty(Uncertainty original) {
        Uncertainty copy = uncertainty.UncertaintyFactory.eINSTANCE.createUncertainty();
        copy.setId(EcoreUtil.generateUUID());
        copy.setKind(original.getKind());
        copy.setNature(original.getNature());
        copy.setReducability(original.getReducability());
        // Deep copy of UncertaintyLocation
        UncertaintyLocation originalLocation = original.getUncertaintyLocation();
        UncertaintyLocation copyLocation = UncertaintyFactory.eINSTANCE.createUncertaintyLocation();
        copyLocation.setLocation(originalLocation.getLocation());
        copyLocation.setSpecification(originalLocation.getSpecification());
        copy.setUncertaintyLocation(copyLocation);
        // Deep copy of Effect
        Effect originalEffect = original.getEffect();
        Effect copyEffect = UncertaintyFactory.eINSTANCE.createEffect();
        copyEffect.setSpecification(originalEffect.getSpecification());
        copyEffect.setRepresentation(originalEffect.getRepresentation());
        copyEffect.setStochasticity(originalEffect.getStochasticity());
        copy.setEffect(copyEffect);
        // Deep copy of Pattern
        Pattern originalPattern = original.getPattern();
        Pattern copyPattern = UncertaintyFactory.eINSTANCE.createPattern();
        copyPattern.setPatternType(originalPattern.getPatternType());
        copy.setPattern(copyPattern);
        // Deep copy of UncertaintyPerspective
        UncertaintyPerspective originalPerspective = original.getPerspective();
        UncertaintyPerspective copyPerspective = UncertaintyFactory.eINSTANCE.createUncertaintyPerspective();
        copyPerspective.setPerspective(originalPerspective.getPerspective());
        copyPerspective.setSpecification(originalPerspective.getSpecification());
        copy.setPerspective(copyPerspective);
        copy.setOnDelete(original.getOnDelete());
        return copy;
    }
}
