package lipid;

import adduct.Adduct;
import adduct.AdductList;
import adduct.MassTransformation;
import java.util.*;

/**
 * Class to represent the annotation over a lipid
 */
public class Annotation {

    private final Lipid lipid;
    private final double mz;
    private final double intensity; // intensity of the most abundant peak in the groupedPeaks
    private final double rtMin;
    private final IoniationMode ionizationMode;
    private String adduct; // !!TODO The adduct will be detected based on the groupedSignals
    private final Set<Peak> groupedSignals;
    private int score;
    private int totalScoresApplied;


    /**
     * @param lipid
     * @param mz
     * @param intensity
     * @param retentionTime
     * @param ionizationMode
     */
    public Annotation(Lipid lipid, double mz, double intensity, double retentionTime, IoniationMode ionizationMode) {
        this(lipid, mz, intensity, retentionTime, ionizationMode, Collections.emptySet());
    }

    /**
     * @param lipid
     * @param mz
     * @param intensity
     * @param retentionTime
     * @param ionizationMode
     * @param groupedSignals
     */
    public Annotation(Lipid lipid, double mz, double intensity, double retentionTime, IoniationMode ionizationMode, Set<Peak> groupedSignals) {
        this.lipid = lipid;
        this.mz = mz;
        this.rtMin = retentionTime;
        this.intensity = intensity;
        this.ionizationMode = ionizationMode;
        // !!TODO This set should be sorted according to help the program to deisotope the signals plus detect the adduct
        this.groupedSignals = new TreeSet<>(groupedSignals);
        this.score = 0;
        this.totalScoresApplied = 0;
    }

    public Lipid getLipid() {
        return lipid;
    }

    public double getMz() {
        return mz;
    }

    public double getRtMin() {
        return rtMin;
    }

    public String getAdduct() {
        return adduct;
    }

    public void setAdduct(String adduct) {
        this.adduct = adduct;
    }

    public double getIntensity() {
        return intensity;
    }

    public IoniationMode getIonizationMode() {
        return ionizationMode;
    }

    public Set<Peak> getGroupedSignals() {
        return Collections.unmodifiableSet(groupedSignals);
    }


    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    // !CHECK Take into account that the score should be normalized between -1 and 1
    public void addScore(int delta) {
        this.score += delta;
        this.totalScoresApplied++;
    }

    /**
     * @return The normalized score between 0 and 1 that consists on the final number divided into the times that the rule
     * has been applied.
     */


    public double getNormalizedScore() {
        return (double) this.score / this.totalScoresApplied;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Annotation)) return false;
        Annotation that = (Annotation) o;
        return Double.compare(that.mz, mz) == 0 &&
                Double.compare(that.rtMin, rtMin) == 0 &&
                Objects.equals(lipid, that.lipid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lipid, mz, rtMin);
    }

    @Override
    public String toString() {
        return String.format("Annotation(%s, mz=%.4f, RT=%.2f, adduct=%s, intensity=%.1f, score=%d)",
                lipid.getName(), mz, rtMin, adduct, intensity, score);
    }

    // !!TODO Detect the adduct with an algorithm or with drools, up to the user.
    //Adduct--> product of a direct addition of 2 or more distinct molecules, resulting
    //in a single reaction product containing all atoms of all components
    //the adduct will be detected based on the grouped signals

    public String detectAdduct() {
        double mzTolerance = 0.2;

        //we need at least 2 peaks
        if (groupedSignals == null || groupedSignals.size() < 2) {
            return "Unknown";
        }
        //choose the adduct list
        Map<String, Double> adductMap = (getIonizationMode() == IoniationMode.POSITIVE)
                ? AdductList.MAPMZPOSITIVEADDUCTS
                : AdductList.MAPMZNEGATIVEADDUCTS;


        //to pick the first element in the sorted set
        double observedMz = groupedSignals.iterator().next().getMz();

        System.out.println("detectAdduct: observedMz = " + observedMz + ", mode = " + getIonizationMode());

        //loop over candidate adducts
        for (String candidateAdduct : adductMap.keySet()) {
            System.out.println("Testing candidate adduct: " + candidateAdduct);
            try {
                //Convert the observed m/z to a monoisotopic mass
                double monoisotopicMass = MassTransformation
                        .getMonoisotopicMassFromMZ(observedMz, candidateAdduct);
                System.out.println("monoisotopicMass = " + monoisotopicMass);

                //To check if the peak fits
                double backMz = MassTransformation.getMZFromMonoisotopicMass(monoisotopicMass, candidateAdduct);

                if (Math.abs(backMz - observedMz) > mzTolerance) {
                    continue;
                }
                //Search for peaks with the same mass
                for (Peak otherPeak : groupedSignals) {
                    System.out.println("Comparing with otherPeak: " + otherPeak);
                    if (Math.abs(otherPeak.getMz() - observedMz) <= mzTolerance) {
                        //Is the same peak as the observed so we skip
                        System.out.println("Skip: same as observedMz");
                        continue;
                    }
                    //Loop over every possible adduct
                    for (String secondAdduct : adductMap.keySet()) {
                        //compute the expected mz
                        double expectedMz = MassTransformation.getMZFromMonoisotopicMass(monoisotopicMass, secondAdduct);
                        double diff = Math.abs(expectedMz - otherPeak.getMz());
                        System.out.println("secondAdduct=" + secondAdduct + ", expectedMz=" + expectedMz + ", observed=" + otherPeak.getMz() + ", diff=" + diff);
                        //we found a different peak whose mz fits the same M via a different adduct
                        if (diff <= mzTolerance) {
                            System.out.println("DETECTED adduct: " + candidateAdduct + " (via " + secondAdduct + ")");
                            return candidateAdduct;
                        }
                    }
                }

            } catch (IllegalArgumentException e) {
                //If error
                System.out.println("Ignored candidateAdduct: " + candidateAdduct);
            }
        }

        //If nothing matches
        System.out.println("detectAdduct: No adducts detected");
        return "Unknown";
    }
}