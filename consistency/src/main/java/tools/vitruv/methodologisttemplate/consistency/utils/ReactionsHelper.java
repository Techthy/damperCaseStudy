package tools.vitruv.methodologisttemplate.consistency.utils;

import org.eclipse.emf.ecore.util.EcoreUtil;

import uncertainty.Effect;
import uncertainty.Pattern;
import uncertainty.Uncertainty;
import uncertainty.UncertaintyFactory;
import uncertainty.UncertaintyLocation;
import uncertainty.UncertaintyPerspective;

public class ReactionsHelper {

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
