package tools.vitruv.methodologisttemplate.vsum.uncertainty;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import tools.vitruv.stoex.stoex.Expression;
import uncertainty.Effect;
import uncertainty.OnDeleteMode;
import uncertainty.Pattern;
import uncertainty.PatternType;
import uncertainty.ReducabilityLevel;
import uncertainty.StochasticityEffectType;
import uncertainty.StructuralEffectTypeRepresentation;
import uncertainty.Uncertainty;
import uncertainty.UncertaintyFactory;
import uncertainty.UncertaintyKind;
import uncertainty.UncertaintyLocation;
import uncertainty.UncertaintyLocationType;
import uncertainty.UncertaintyNature;
import uncertainty.UncertaintyPerspective;
import uncertainty.UncertaintyPerspectiveType;
import uncertainty.UncertaintySource;
import uncertainty.UncertaintySourceType;

public class UncertaintyTestFactory {

	private UncertaintyTestFactory() {
		// Utility class
	}

	public static UncertaintyLocation createUncertaintyLocation(List<EObject> referencedComponents) {
		UncertaintyLocation location = UncertaintyFactory.eINSTANCE.createUncertaintyLocation();
		location.setLocation(UncertaintyLocationType.PARAMETER);
		location.setSpecification("Location specification");
		location.getReferencedComponents().addAll(referencedComponents);
		return location;
	}

	public static UncertaintyLocation createUncertaintyLocationWithLocationType(List<EObject> referencedComponents,
			UncertaintyLocationType locationtype) {
		UncertaintyLocation location = UncertaintyFactory.eINSTANCE.createUncertaintyLocation();
		location.setLocation(locationtype);
		location.setSpecification("Location specification");
		location.getReferencedComponents().addAll(referencedComponents);
		return location;
	}

	public static UncertaintyLocation createUncertaintyLocation(List<EObject> referencedComponents,
			UncertaintyLocationType locationtype, String parameterLocation) {
		UncertaintyLocation location = UncertaintyFactory.eINSTANCE.createUncertaintyLocation();
		location.setLocation(locationtype);
		location.setSpecification("Location specification");
		location.setParameterLocation(parameterLocation);
		location.getReferencedComponents().addAll(referencedComponents);
		return location;
	}

	public static Effect createEffect(Expression expression) {
		Effect effect = UncertaintyFactory.eINSTANCE.createEffect();
		effect.setSpecification("Effect specification");
		effect.setRepresentation(StructuralEffectTypeRepresentation.CONTINUOUS);
		effect.setStochasticity(StochasticityEffectType.PROBABILISTIC);
		effect.setExpression(expression);
		return effect;
	}

	public static UncertaintyPerspective createUncertaintyPerspective() {
		UncertaintyPerspective perspective = UncertaintyFactory.eINSTANCE.createUncertaintyPerspective();
		perspective.setSpecification("Perspective specification");
		perspective.setPerspective(UncertaintyPerspectiveType.OBJECTIVE);
		return perspective;
	}

	public static Pattern createPattern() {
		Pattern pattern = UncertaintyFactory.eINSTANCE.createPattern();
		pattern.setPatternType(PatternType.PERSISTENT);
		return pattern;
	}

	public static UncertaintySource createUncertaintySource() {
		UncertaintySource source = UncertaintyFactory.eINSTANCE.createUncertaintySource();
		source.setSource(UncertaintySourceType.ENVIRONMENT);
		return source;
	}

	public static Uncertainty createUncertainty(EObject referencedComponent,
			String parameterLocation, Expression expression) {

		Uncertainty uncertainty = UncertaintyFactory.eINSTANCE.createUncertainty();
		uncertainty.setId(EcoreUtil.generateUUID());
		uncertainty.setKind(UncertaintyKind.BEHAVIOR_UNCERTAINTY);
		uncertainty.setReducability(ReducabilityLevel.UNKNOWN);
		uncertainty.setNature(UncertaintyNature.ALEATORY);
		uncertainty.setSetManually(true);
		uncertainty.setOnDelete(OnDeleteMode.CASCADE);

		uncertainty.setUncertaintyLocation(createUncertaintyLocation(
				List.of(referencedComponent), UncertaintyLocationType.PARAMETER, parameterLocation));
		uncertainty.setEffect(createEffect(expression));
		uncertainty.setPerspective(createUncertaintyPerspective());
		uncertainty.setPattern(createPattern());

		return uncertainty;
	}

}
