package tools.vitruv.methodologisttemplate.vsum.mafds;

import java.beans.Expression;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
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
import tools.vitruv.methodologisttemplate.vsum.uncertainty.UncertaintyTestFactory;
import tools.vitruv.methodologisttemplate.vsum.uncertainty.UncertaintyTestUtil;
import tools.vitruv.stoex.stoex.NormalDistribution;
import tools.vitruv.stoex.stoex.StoexFactory;
import uncertainty.Uncertainty;
import uncertainty.UncertaintyAnnotationRepository;
import uncertainty.UncertaintyLocationType;

public class WeightTest {

    @BeforeAll
    static void setup() {
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*",
                new XMIResourceFactoryImpl());
    }

    @Test
    @DisplayName("Model Damper System and Compute Weight")
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

        assertEquals(50.7146, damperSystem.getTotalWeightInKg(), 0.001);

    }

    @Test
    @DisplayName("Model Damper System and Compute Weight With Uncertainty Annotations and StoEx")
    void addDamperSystemWithUncertaintyAndStoExTest(@TempDir Path tempDir) {

        // SETUP VSUM
        VirtualModel vsum = UncertaintyTestUtil.createDefaultVirtualModel(tempDir);
        UncertaintyTestUtil.registerRootObjects(vsum, tempDir);

        System.out.println("UNCERTAINTY AND STOEX TEST");

        CommittableView uncertaintyView = UncertaintyTestUtil.getDefaultView(vsum,
                List.of(DamperRepository.class, UncertaintyAnnotationRepository.class))
                .withChangeRecordingTrait();
        modifyView(uncertaintyView, this::annotateWithUncertainty);

        View afterAddView = UncertaintyTestUtil.getDefaultView(vsum,
                List.of(DamperRepository.class, UncertaintyAnnotationRepository.class))
                .withChangeRecordingTrait();

        Uncertainty totalMassUncertainty = getTotalMassUncertainty(afterAddView);
        System.out.println("Total Mass Uncertainty: " + totalMassUncertainty);
        assertTrue(totalMassUncertainty != null);
        assertEquals("totalMassInKg", totalMassUncertainty.getUncertaintyLocation().getParameterLocation());
        System.out.println("Total Mass Uncertainty Distribution: "
                + totalMassUncertainty.getEffect().getExpression());
        // TODO expect the expresion to be a normal distribution with mu=50.7146 and
        // sigma=0.187
        // assertEquals(expected, actual);
        DamperSystem damperSystem = getDamperSystem(afterAddView);
        assertEquals(50.7146, damperSystem.getTotalWeightInKg(), 0.001);

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

    private Uncertainty getTotalMassUncertainty(View view) {
        UncertaintyAnnotationRepository uncertaintyRepo = view
                .getRootObjects(UncertaintyAnnotationRepository.class).iterator().next();
        return findUncertaintyByLocation(uncertaintyRepo, "totalMassInKg",
                getDamperSystem(view));
    }

    private DamperSystem getDamperSystem(View view) {
        DamperRepository damperRepository = view.getRootObjects(DamperRepository.class).iterator().next();
        return damperRepository.getDampers();
    }

    private void annotateWithUncertainty(CommittableView view) {
        DamperSystem damperSystem = MafdsFactory.eINSTANCE.createDamperSystem();

        UpperTruss upperTruss = MafdsFactory.eINSTANCE.createUpperTruss();
        upperTruss.setCrossLinkMassInKg(13.74);
        upperTruss.setMassOfThreadedRodInKg(0.363);
        upperTruss.setNumberOfThreadedRods(21);
        upperTruss.setSphereMassInKg(0.76);
        damperSystem.setUpperTruss(upperTruss);
        // Total Mass Upper Truss: 22.123

        // Upper Truss
        Uncertainty upperTrussCrossLinkMassUncertainty = createUncertainty(upperTruss,
                "crossLinkMassInKg",
                13.74, 0.5);
        Uncertainty upperTrussSphereMassUncertainty = createUncertainty(upperTruss,
                "sphereMassInKg", 0.76,
                0.003);
        Uncertainty upperTrussRodMassUncertainty = createUncertainty(upperTruss,
                "massOfThreadedRodInKg",
                0.363, 0.015);

        // Lower Truss
        LowerTruss lowerTruss = MafdsFactory.eINSTANCE.createLowerTruss();
        lowerTruss.setSphereMassInKg(0.76);
        lowerTruss.setMassOfThreadedRodInKg(0.363);
        lowerTruss.setNumberOfThreadedRods(6);
        damperSystem.setLowerTruss(lowerTruss);
        // Total Mass Lower Truss: 2.938
        Uncertainty lowerTrussSphereMassUncertainty = createUncertainty(lowerTruss,
                "sphereMassInKg", 0.76,
                0.003);
        Uncertainty lowerTrussRodMassUncertainty = createUncertainty(lowerTruss,
                "massOfThreadedRodInKg",
                0.363, 0.015);

        // Guidance Element
        GuidanceElement guidanceElement = MafdsFactory.eINSTANCE.createGuidanceElement();
        guidanceElement.setMassOfArmInKg(1.46);
        guidanceElement.setNumberOfArms(3);
        guidanceElement.setMassOfJointMiddlePartInKg(0.9236);
        damperSystem.setGuidanceElement(guidanceElement);
        // Total Mass Guidance Element: 5.3036
        Uncertainty guidanceElementArmMassUncertainty = createUncertainty(guidanceElement,
                "massOfArmInKg",
                1.46, 0.075);
        Uncertainty guidanceElementJointMassUncertainty = createUncertainty(guidanceElement,
                "massOfJointMiddlePartInKg", 0.9236, 0.03);

        // Spring Damper
        SpringDamper springDamper = MafdsFactory.eINSTANCE.createSpringDamper();
        springDamper.setStiffnessInNPerM(27000);
        springDamper.setDampingConstantInNsPerM(140);
        springDamper.setSpringSupportMassInKg(20.35);
        damperSystem.setSpringDamper(springDamper);
        // Mass Spring Damper: 20.35
        Uncertainty springStiffnessUncertainty = createUncertainty(springDamper, "stiffnessInNPerM",
                27000,
                1200);
        Uncertainty springDampingConstantUncertainty = createUncertainty(springDamper,
                "dampingConstantInNsPerM", 140, 7);
        Uncertainty springSupportMassUncertainty = createUncertainty(springDamper,
                "springSupportMassInKg",
                20.35, 0.25);
        view.getRootObjects(DamperRepository.class).iterator().next().setDampers(damperSystem);

        List<Uncertainty> allUncertainties = List.of(upperTrussCrossLinkMassUncertainty,
                upperTrussSphereMassUncertainty,
                upperTrussRodMassUncertainty, lowerTrussSphereMassUncertainty,
                lowerTrussRodMassUncertainty,
                guidanceElementArmMassUncertainty, guidanceElementJointMassUncertainty,
                springStiffnessUncertainty,
                springDampingConstantUncertainty, springSupportMassUncertainty);

        view.getRootObjects(UncertaintyAnnotationRepository.class).iterator().next()
                .getUncertainties().addAll(allUncertainties);
    }

    private Uncertainty createUncertainty(EObject referencedObject, String parameter, double mu, double sigma) {
        NormalDistribution distribution = StoexFactory.eINSTANCE.createNormalDistribution();
        distribution.setMu(mu);
        distribution.setSigma(sigma);
        return UncertaintyTestFactory.createUncertainty(referencedObject, parameter, distribution);
    }

    private void createDamperSystem(CommittableView view) {
        DamperSystem damperSystem = MafdsFactory.eINSTANCE.createDamperSystem();

        UpperTruss upperTruss = MafdsFactory.eINSTANCE.createUpperTruss();
        upperTruss.setCrossLinkMassInKg(13.74);
        upperTruss.setMassOfThreadedRodInKg(0.363);
        upperTruss.setNumberOfThreadedRods(21);
        upperTruss.setSphereMassInKg(0.76);
        // Total Mass Upper Truss: 22.123

        LowerTruss lowerTruss = MafdsFactory.eINSTANCE.createLowerTruss();
        lowerTruss.setSphereMassInKg(0.76);
        lowerTruss.setMassOfThreadedRodInKg(0.363);
        lowerTruss.setNumberOfThreadedRods(6);
        // Total Mass Lower Truss: 2.938

        GuidanceElement guidanceElement = MafdsFactory.eINSTANCE.createGuidanceElement();
        guidanceElement.setMassOfArmInKg(1.46);
        guidanceElement.setNumberOfArms(3);
        guidanceElement.setMassOfJointMiddlePartInKg(0.9236);
        // Total Mass Guidance Element: 5.3036

        SpringDamper springDamper = MafdsFactory.eINSTANCE.createSpringDamper();
        springDamper.setStiffnessInNPerM(27000);
        springDamper.setDampingConstantInNsPerM(140);
        springDamper.setSpringSupportMassInKg(20.35);
        // Mass Spring Damper: 20.35

        damperSystem.setUpperTruss(upperTruss);
        damperSystem.setLowerTruss(lowerTruss);
        damperSystem.setGuidanceElement(guidanceElement);
        damperSystem.setSpringDamper(springDamper);
        // Total Weight Damper System: 22.123 + 5.3036 + 20.35 + 2.938 = 50.7146

        view.getRootObjects(DamperRepository.class).iterator().next().setDampers(damperSystem);
    }

    private void modifyView(CommittableView view, Consumer<CommittableView> modificationFunction) {
        modificationFunction.accept(view);
        view.commitChanges();
    }

}
