package tools.vitruv.methodologisttemplate.vsum.mafds;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.emf.ecore.EObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import mafds.DamperRepository;
import mafds.DamperSystem;
import mafds.GuidanceElement;
import mafds.LowerTruss;
import mafds.MafdsFactory;
import mafds.SpringDamper;
import mafds.UpperTruss;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.vsum.VirtualModel;
import tools.vitruv.methodologisttemplate.consistency.utils.StoexConsistencyHelper;
import tools.vitruv.methodologisttemplate.vsum.uncertainty.UncertaintyTestFactory;
import tools.vitruv.methodologisttemplate.vsum.uncertainty.UncertaintyTestUtil;
import tools.vitruv.stoex.stoex.Expression;
import tools.vitruv.stoex.stoex.NormalDistribution;
import tools.vitruv.stoex.stoex.SampledDistribution;
import tools.vitruv.stoex.stoex.StoexFactory;
import uncertainty.Uncertainty;
import uncertainty.UncertaintyAnnotationRepository;

public class DampingRatioTest {

    @Test
    @DisplayName("Model Damper System and check calculated Damping Ratio")
    void addDamperSystemTest(@TempDir Path tempDir) {
        // SETUP VSUM
        VirtualModel vsum = UncertaintyTestUtil.createDefaultVirtualModel(tempDir);
        UncertaintyTestUtil.registerRootObjects(vsum, tempDir);

        CommittableView damperSystemView = UncertaintyTestUtil.getDefaultView(vsum,
                List.of(DamperRepository.class))
                .withChangeRecordingTrait();
        modifyView(damperSystemView, this::createDamperSystem);

        View afterAddView = UncertaintyTestUtil.getDefaultView(vsum,
                List.of(DamperRepository.class))
                .withChangeRecordingTrait();
        DamperSystem damperSystem = getDamperSystem(afterAddView);

        // Damping Ratio = c / (2 * sqrt(k * m))
        // 140 / (2 * sqrt(27000 * 50.7146)) = 0.0598204473
        assertEquals(0.0598204473, damperSystem.getDampingRatio(), 0.001);
    }

    @Test
    @DisplayName("Model Damper System and check calculated Damping Ratio with Uncertainty")
    void addDamperSystemWithUncertaintyTest(@TempDir Path tempDir) {
        // SETUP VSUM
        VirtualModel vsum = UncertaintyTestUtil.createDefaultVirtualModel(tempDir);
        UncertaintyTestUtil.registerRootObjects(vsum, tempDir);

        CommittableView damperSystemView = UncertaintyTestUtil.getDefaultView(vsum,
                List.of(DamperRepository.class, UncertaintyAnnotationRepository.class))
                .withChangeRecordingTrait();
        modifyView(damperSystemView, this::createDamperSystemWithUncertainty);

        View afterAddView = UncertaintyTestUtil.getDefaultView(vsum,
                List.of(DamperRepository.class, UncertaintyAnnotationRepository.class))
                .withChangeRecordingTrait();

        // Should be same as without uncertainty
        DamperSystem damperSystem = getDamperSystem(afterAddView);
        assertEquals(0.0598204473, damperSystem.getDampingRatio(), 0.001);
        UncertaintyAnnotationRepository uncertaintyRepo = afterAddView
                .getRootObjects(UncertaintyAnnotationRepository.class).iterator().next();
        assertEquals(3, uncertaintyRepo.getUncertainties().size());
    }

    @Test
    @DisplayName("Model Damper System and check calculated Damping Ratio with Uncertainty and Stoex Expression")
    void addDamperSystemWithUncertaintyAndStoExTest(@TempDir Path tempDir) {
        VirtualModel vsum = UncertaintyTestUtil.createDefaultVirtualModel(tempDir);
        UncertaintyTestUtil.registerRootObjects(vsum, tempDir);

        CommittableView damperSystemView = UncertaintyTestUtil.getDefaultView(vsum,
                List.of(DamperRepository.class, UncertaintyAnnotationRepository.class))
                .withChangeRecordingTrait();
        modifyView(damperSystemView,
                this::createDamperSystemWithUncertaintyAndStoex);

        View afterAddView = UncertaintyTestUtil.getDefaultView(vsum,
                List.of(DamperRepository.class, UncertaintyAnnotationRepository.class))
                .withChangeRecordingTrait();
        DamperSystem damperSystem = getDamperSystem(afterAddView);

        // Damping Ratio = c / (2 * sqrt(k * m))
        // 140 / (2 * sqrt(27000 * 50.7146)) = 0.0598204473
        assertEquals(0.0598204473, damperSystem.getDampingRatio(), 0.001);

        Uncertainty dampingUncertainty = getDampingRatioUncertainty(afterAddView);
        assertTrue(dampingUncertainty != null);
        assertEquals("dampingRatio", dampingUncertainty.getUncertaintyLocation().getParameterLocation());

        Expression expr = dampingUncertainty.getEffect().getExpression();
        StoexConsistencyHelper helper = new StoexConsistencyHelper();
        assertTrue(expr instanceof SampledDistribution);
        assertEquals(0.0598204473, helper.getMean(expr).doubleValue(), 0.001);
    }

    private Uncertainty getDampingRatioUncertainty(View view) {
        UncertaintyAnnotationRepository uncertaintyRepo = view
                .getRootObjects(UncertaintyAnnotationRepository.class).iterator().next();
        return uncertaintyRepo.getUncertainties().stream()
                .filter(u -> u.getUncertaintyLocation().getParameterLocation().equals("dampingRatio"))
                .findFirst()
                .orElse(null);
    }

    private DamperSystem getDamperSystem(View view) {
        DamperRepository damperRepository = view.getRootObjects(DamperRepository.class).iterator().next();
        return damperRepository.getDamperSystems().stream().findFirst().orElse(null);
    }

    private void createDamperSystem(CommittableView view) {
        // Only relevant parts are added for this test
        // Complete instantiation of the MAFDS with values is done in total mass test

        DamperSystem damperSystem = MafdsFactory.eINSTANCE.createDamperSystem();

        SpringDamper springDamper = MafdsFactory.eINSTANCE.createSpringDamper();
        springDamper.setStiffnessInNPerM(27000);
        springDamper.setDampingConstantInNsPerM(140);

        damperSystem.setSpringDamper(springDamper);
        damperSystem.setTotalMassInKg(50.7146);

        view.getRootObjects(DamperRepository.class).iterator().next().getDamperSystems().add(damperSystem);
    }

    private void createDamperSystemWithUncertainty(CommittableView view) {
        DamperSystem damperSystem = MafdsFactory.eINSTANCE.createDamperSystem();

        SpringDamper springDamper = MafdsFactory.eINSTANCE.createSpringDamper();
        springDamper.setStiffnessInNPerM(27000);
        springDamper.setDampingConstantInNsPerM(140);

        damperSystem.setSpringDamper(springDamper);
        damperSystem.setTotalMassInKg(50.7146);

        view.getRootObjects(DamperRepository.class).iterator().next().getDamperSystems().add(damperSystem);

        Uncertainty springStiffnessUncertainty = createUncertainty(springDamper, "stiffnessInNPerM");
        Uncertainty dampingConstantUncertainty = createUncertainty(springDamper, "dampingConstantInNsPerM");
        Uncertainty totalMassUncertainty = createUncertainty(damperSystem, "totalMassInKg");

        view.getRootObjects(UncertaintyAnnotationRepository.class).iterator().next()
                .getUncertainties()
                .addAll(List.of(springStiffnessUncertainty, dampingConstantUncertainty, totalMassUncertainty));
    }

    private void createDamperSystemWithUncertaintyAndStoex(CommittableView view) {
        // Only relevant parts are added for this test
        // Complete instantiation of the MAFDS with values is done in total mass test

        DamperSystem damperSystem = MafdsFactory.eINSTANCE.createDamperSystem();

        SpringDamper springDamper = MafdsFactory.eINSTANCE.createSpringDamper();
        springDamper.setStiffnessInNPerM(27000);
        springDamper.setDampingConstantInNsPerM(140);

        damperSystem.setSpringDamper(springDamper);
        damperSystem.setTotalMassInKg(50.7146);

        view.getRootObjects(DamperRepository.class).iterator().next().getDamperSystems().add(damperSystem);

        Uncertainty springStiffnessUncertainty = createUncertainty(springDamper, "stiffnessInNPerM", 27000, 1200);
        Uncertainty dampingConstantUncertainty = createUncertainty(springDamper, "dampingConstantInNsPerM", 140, 7);
        Uncertainty totalMassUncertainty = createUncertainty(damperSystem, "totalMassInKg", 50.7146, 0.5829);

        view.getRootObjects(UncertaintyAnnotationRepository.class).iterator().next()
                .getUncertainties()
                .addAll(List.of(springStiffnessUncertainty, dampingConstantUncertainty, totalMassUncertainty));

    }

    private Uncertainty createUncertainty(EObject referencedObject, String parameter) {
        return UncertaintyTestFactory.createUncertainty(referencedObject, parameter, null);
    }

    private Uncertainty createUncertainty(EObject referencedObject, String parameter, double mu, double sigma) {
        NormalDistribution distribution = StoexFactory.eINSTANCE.createNormalDistribution();
        distribution.setMu(mu);
        distribution.setSigma(sigma);
        return UncertaintyTestFactory.createUncertainty(referencedObject, parameter, distribution);
    }

    private void modifyView(CommittableView view, Consumer<CommittableView> modificationFunction) {
        modificationFunction.accept(view);
        view.commitChanges();
    }

}
