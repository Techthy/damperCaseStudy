package tools.vitruv.methodologisttemplate.vsum.mafds;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

public class DampingRatioTest {

    @Test
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

    private DamperSystem getDamperSystem(View view) {
        DamperRepository damperRepository = view.getRootObjects(DamperRepository.class).iterator().next();
        return damperRepository.getDamperSystems().stream().findFirst().orElse(null);
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
        // Total Mass Damper System: 22.123 + 5.3036 + 20.35 + 2.938 = 50.7146

        view.getRootObjects(DamperRepository.class).iterator().next().getDamperSystems().add(damperSystem);
    }

    private void modifyView(CommittableView view, Consumer<CommittableView> modificationFunction) {
        modificationFunction.accept(view);
        view.commitChanges();
    }

}
