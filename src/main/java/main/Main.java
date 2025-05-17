package main;

import adduct.MassTransformation;
import lipid.*;
import org.drools.ruleunits.api.RuleUnitInstance;
import org.drools.ruleunits.api.RuleUnitProvider;

import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args) {
        LipidScoreUnit lipidScoreUnit = new LipidScoreUnit();

        RuleUnitInstance<LipidScoreUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(lipidScoreUnit);

        //Checks to see if the methods of Mass transformation work properly
        double mz     = 700.500;      // m/z observe
        String adduct = "[M+H]+";     // adduct to be checked

        Double monoMass = MassTransformation.getMonoisotopicMassFromMZ(mz, adduct);
        Double mzBack   = MassTransformation.getMZFromMonoisotopicMass(monoMass, adduct);

        System.out.println("Adduct: " + adduct);
        System.out.println("mz observed   : " + mz);
        System.out.println("M calculated    : " + monoMass);
        System.out.println("mz recalculated : " + mzBack);

        double mz2     = 500.250;   // m/z observed
        String adduct2 = "[M-H]−";  // negative adduct to be checked

        Double monoMass2 = MassTransformation.getMonoisotopicMassFromMZ(mz2, adduct2);
        Double mzBack2   = MassTransformation.getMZFromMonoisotopicMass(monoMass2, adduct2);

        System.out.println("Adduct        : " + adduct2);
        System.out.println("mz observed  : " + mz2);
        System.out.println("M calculated   : " + monoMass2);
        System.out.println("mz recalculated: " + mzBack2);

        //Checks to see if the method adductDetection() works properly
        Peak pH  = new Peak(700.500, 100_000);
        Peak pNa = new Peak(722.482,  80_000);
        Annotation ann1 = new Annotation(
                new Lipid(1, "PC 34:1", "C42H82NO8P", LipidType.PC, 34, 1),
                pH.getMz(), pH.getIntensity(),
                6.5, IoniationMode.POSITIVE,
                Set.of(pH, pNa)
        );
        System.out.println("Case 1  adduct detected: " + ann1.detectAdduct());

        Peak pH2   = new Peak(700.500, 90_000);
        Peak pH2O  = new Peak(682.4894, 70_000);
        Annotation ann2 = new Annotation(
                new Lipid(2, "PE 36:2", "C41H78NO8P", LipidType.PE, 36, 2),
                pH2.getMz(), pH2.getIntensity(),
                7.5, IoniationMode.POSITIVE,
                Set.of(pH2, pH2O)
        );
        System.out.println("Case 2  adduct detected: " + ann2.detectAdduct());

        Peak p1 = new Peak(700.500, 100_000);   // [M+H]+
        Peak p2 = new Peak(350.754,  85_000);   // [M+2H]2+
        Annotation ann3 = new Annotation(
                new Lipid(3, "TG 54:3", "C57H104O6", LipidType.TG, 54, 3),
                p1.getMz(), p1.getIntensity(),
                10.0, IoniationMode.POSITIVE,
                Set.of(p1, p2)
        );
        System.out.println("Case 3  adduct detected: " + ann3.detectAdduct());

        double mzA = 400.300;   // [M-2H]2−
        double mzB = 801.607;   // [2M-H]−

        Peak pA = new Peak(mzA, 120_000);
        Peak pB = new Peak(mzB,  90_000);

        Annotation ann4 = new Annotation(
                new Lipid(4, "PI 38:4", "C47H83O13P", LipidType.PI, 38, 4),
                pA.getMz(), pA.getIntensity(),
                5.2, IoniationMode.NEGATIVE,
                Set.of(pA, pB)
        );

        System.out.println("Case 4  adduct detected: " + ann4.detectAdduct());

        try {
            // TODO INTRODUCE THE CODE IF DESIRED TO INSERT FACTS AND TRIGGER RULES
            instance.fire();
            // TODO INTRODUCE THE QUERIES IF DESIRED


        } finally {
            instance.close();
        }
    }
}
