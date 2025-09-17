package tools.vitruv.methodologisttemplate.vsum.mafds;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

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

        assertEquals(37.7346, damperSystem.getTotalWeightInKg(), 0.001);

    }

    @Test
    @DisplayName("Model Damper System and Compute Weight With Uncertainty Annotations and StoEx")
    void addDamperSystemWithUncertaintyAndStoExTest(@TempDir Path tempDir) {

        // SETUP VSUM
        VirtualModel vsum = UncertaintyTestUtil.createDefaultVirtualModel(tempDir);
        UncertaintyTestUtil.registerRootObjects(vsum, tempDir);

        CommittableView damperSystemView = UncertaintyTestUtil.getDefaultView(vsum,
                List.of(DamperRepository.class))
                .withChangeRecordingTrait();
        modifyView(damperSystemView, this::createDamperSystem);
        CommittableView uncertaintyView = UncertaintyTestUtil.getDefaultView(vsum,
                List.of(DamperRepository.class, UncertaintyAnnotationRepository.class))
                .withChangeRecordingTrait();
        modifyView(uncertaintyView, this::annotateWithUncertainty);

        View afterAddView = UncertaintyTestUtil.getDefaultView(vsum,
                List.of(DamperRepository.class, UncertaintyAnnotationRepository.class))
                .withChangeRecordingTrait();
        DamperSystem damperSystem = getDamperSystem(afterAddView);

        Uncertainty springStiffnessUncertainty = getSpringStiffnessUncertainty(afterAddView);
        assertTrue(springStiffnessUncertainty != null);
        assertEquals("stiffnessInNPerM", springStiffnessUncertainty.getUncertaintyLocation().getParameterLocation());
        assertEquals(1, springStiffnessUncertainty.getUncertaintyLocation().getReferencedComponents().size());
        assertEquals(damperSystem.getSpringDamper(),
                springStiffnessUncertainty.getUncertaintyLocation().getReferencedComponents().get(0));
        assertEquals(37.7346, damperSystem.getTotalWeightInKg(), 0.001);

    }

    private Uncertainty getSpringStiffnessUncertainty(View view) {
        DamperSystem system = view.getRootObjects(DamperRepository.class).iterator().next().getDampers();
        UncertaintyAnnotationRepository uncertaintyRepo = view
                .getRootObjects(UncertaintyAnnotationRepository.class).iterator().next();
        return uncertaintyRepo.getUncertainties().stream()
                .filter(u -> u.getUncertaintyLocation().getReferencedComponents().contains(system.getSpringDamper())
                        && u.getUncertaintyLocation().getParameterLocation().equals("stiffnessInNPerM"))
                .findFirst().orElse(null);
    }

    private DamperSystem getDamperSystem(View view) {
        DamperRepository damperRepository = view.getRootObjects(DamperRepository.class).iterator().next();
        return damperRepository.getDampers();
    }

    private void annotateWithUncertainty(CommittableView view) {
        DamperSystem system = view.getRootObjects(DamperRepository.class).iterator().next().getDampers();
        NormalDistribution sprintStiffnessDistribution = StoexFactory.eINSTANCE.createNormalDistribution();
        sprintStiffnessDistribution.setMu(27000);
        sprintStiffnessDistribution.setSigma(1200);
        Uncertainty sprintStiffnessUncertainty = UncertaintyTestFactory.createUncertainty(system.getSpringDamper(),
                "stiffnessInNPerM",
                sprintStiffnessDistribution);

        view.getRootObjects(UncertaintyAnnotationRepository.class).iterator().next()
                .getUncertainties().add(sprintStiffnessUncertainty);
    }

    private void createDamperSystem(CommittableView view) {
        DamperSystem damperSystem = MafdsFactory.eINSTANCE.createDamperSystem();

        UpperTruss upperTruss = MafdsFactory.eINSTANCE.createUpperTruss();
        upperTruss.setCrossLinkMassInKg(0.76);
        upperTruss.setMassOfThreadedRodInKg(0.363);
        upperTruss.setNumberOfThreadedRods(21);
        upperTruss.setSphereMassInKg(0.76);
        // Total Mass Upper Truss: 9.143

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
        // Total Weight Damper System: 9.143 + 5.3036 + 20.35 + 2.938 = 37.7346

        view.getRootObjects(DamperRepository.class).iterator().next().setDampers(damperSystem);
    }

    private void modifyView(CommittableView view, Consumer<CommittableView> modificationFunction) {
        modificationFunction.accept(view);
        view.commitChanges();
    }

}
