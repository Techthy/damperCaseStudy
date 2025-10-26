package tools.vitruv.methodologisttemplate.consistency;

import org.checkerframework.checker.units.qual.s;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import mafds.DamperSystem;
import tools.vitruv.stoex.interpreter.StoexEvaluator;
import tools.vitruv.stoex.stoex.DoubleLiteral;
import tools.vitruv.stoex.stoex.Expression;
import tools.vitruv.stoex.stoex.StoexFactory;
import uncertainty.Uncertainty;
import uncertainty.UncertaintyAnnotationRepository;
import uncertainty.UncertaintyFactory;
import uncertainty.UncertaintyLocation;
import uncertainty.UncertaintyLocationType;

/**
 * This test class tests the propagation of the damping ratio and its dependence
 * on the parameters damping constant, spring stiffness and total mass.
 * The first test, shows the propagation without using the UnCertaGator.
 * The second test shows the same propagation but with the UnCertaGator and
 * uncertainty annotations.
 * The third test shows the same propagation now using the UnCertaGator with the
 * StoEx extension.
 *
 *
 * @author Claus Hammann
 */
public class DampingRatioReactionsHelper {

    private DampingRatioReactionsHelper() {
        // Utility class
    }

    // Return true if damping ratio is changed
    public static Uncertainty handleUncertainty(UncertaintyAnnotationRepository repo, DamperSystem springDamper,
            String parameterLocation, EObject affectedEObject) {
        Uncertainty affectedUncertainty = findUncertaintyByLocation(repo, parameterLocation);
        // System.out.println("");
        // System.out.println("+++++++ HANDLING DAMPING RATIO +++++++");
        // System.out.println("Affected EObject: " + affectedEObject);
        // System.out.println("Affected Uncertainty: " + affectedUncertainty);
        // System.out.println("Handling uncertainty for parameter location: " +
        // parameterLocation);
        if (affectedUncertainty == null || affectedUncertainty.getEffect() == null
                || affectedUncertainty.getEffect().getExpression() == null) {
            return null;
        }
        // System.out.println("StoEx " +
        // affectedUncertainty.getEffect().getExpression());

        Uncertainty dampingRatioUncertainty = findUncertaintyByLocation(repo, "dampingRatio");

        StoexEvaluator stoexEvaluator = new StoexEvaluator();

        if (dampingRatioUncertainty == null) {
            dampingRatioUncertainty = UncertaintyReactionsHelper.deepCopyUncertainty(affectedUncertainty);
            dampingRatioUncertainty.setId(EcoreUtil.generateUUID());
            dampingRatioUncertainty.getUncertaintyLocation().setParameterLocation("dampingRatio");
            dampingRatioUncertainty.getUncertaintyLocation().getReferencedComponents().add(springDamper);
            repo.getUncertainties().add(dampingRatioUncertainty);
        }

        stoexEvaluator.setVariable("c", getExpression(repo, springDamper, "dampingConstantInNsPerM"));
        stoexEvaluator.setVariable("k", getExpression(repo, springDamper, "stiffnessInNPerM"));
        stoexEvaluator.setVariable("m", getExpression(repo, springDamper, "totalMassInKg"));
        Expression newDampingRatioExpr = stoexEvaluator
                .evaluate("c / (2 * (k * m)^0.5)");
        dampingRatioUncertainty.getEffect().setExpression(newDampingRatioExpr);
        springDamper.setDampingRatio(stoexEvaluator.getMean(newDampingRatioExpr).doubleValue());
        return dampingRatioUncertainty;
    }

    private static Expression getExpression(UncertaintyAnnotationRepository repo,
            DamperSystem springDamper, String parameterLocation) {
        Uncertainty u = findUncertaintyByLocation(repo, parameterLocation);
        if (u == null || u.getEffect() == null || u.getEffect().getExpression() == null) {
            DoubleLiteral literal = StoexFactory.eINSTANCE.createDoubleLiteral();
            literal.setValue(springDamper.getSpringDamper().getDampingConstantInNsPerM());
            return literal;
        }
        return u.getEffect().getExpression();
    }

    private static Uncertainty findUncertaintyByLocation(UncertaintyAnnotationRepository repo,
            String parameterLocation) {
        return repo.getUncertainties().stream()
                .filter(u -> u.getUncertaintyLocation().getParameterLocation().equals(parameterLocation)
                        && u.getUncertaintyLocation()
                                .getLocation() == UncertaintyLocationType.PARAMETER)
                .findFirst()
                .orElse(null);
    }

}
