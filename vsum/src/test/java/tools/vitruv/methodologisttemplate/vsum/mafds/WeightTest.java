package tools.vitruv.methodologisttemplate.vsum.mafds;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import tools.vitruv.methodologisttemplate.vsum.uncertainty.UncertaintyTestUtil;

public class WeightTest {

    @BeforeAll
    static void setup() {
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*",
                new XMIResourceFactoryImpl());
    }

    @Test
    @DisplayName("Model Without Uncertainty Annotations")
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

        // GOAL: 10.363
        assertEquals(0.76, damperSystem.getTotalWeightInKg(), 0.001);

    }

    private DamperSystem getDamperSystem(View view) {
        DamperRepository damperRepository = view.getRootObjects(DamperRepository.class).iterator().next();
        return damperRepository.getDampers();
    }

    private void createDamperSystem(CommittableView view) {
        DamperSystem damperSystem = MafdsFactory.eINSTANCE.createDamperSystem();

        UpperTruss upperTruss = MafdsFactory.eINSTANCE.createUpperTruss();
        upperTruss.setCrossLinkMassInKg(0.76);
        upperTruss.setMassOfThreadedRodInKg(0.363);
        upperTruss.setNumberOfThreadedRods(21);
        upperTruss.setSphereMassInKg(0.76);

        LowerTruss lowerTruss = MafdsFactory.eINSTANCE.createLowerTruss();
        lowerTruss.setSphereMassInKg(0.76);
        lowerTruss.setMassOfThreadedRodInKg(0.363);
        lowerTruss.setNumberOfThreadedRods(6);

        GuidanceElement guidanceElement = MafdsFactory.eINSTANCE.createGuidanceElement();
        guidanceElement.setMassOfArmInKg(1.46);
        guidanceElement.setNumberOfArms(3);
        guidanceElement.setMassOfJointMiddlePartInKg(0.9236);

        SpringDamper springDamper = MafdsFactory.eINSTANCE.createSpringDamper();
        springDamper.setStiffnessInNPerM(27000);
        springDamper.setDampingConstantInNsPerM(140);
        springDamper.setSpringSupportMassInKg(20.35);

        damperSystem.setUpperTruss(upperTruss);
        damperSystem.setLowerTruss(lowerTruss);
        damperSystem.setGuidanceElement(guidanceElement);
        damperSystem.setSpringDamper(springDamper);

        view.getRootObjects(DamperRepository.class).iterator().next().setDampers(damperSystem);
    }

    private void modifyView(CommittableView view, Consumer<CommittableView> modificationFunction) {
        modificationFunction.accept(view);
        view.commitChanges();
    }

}
