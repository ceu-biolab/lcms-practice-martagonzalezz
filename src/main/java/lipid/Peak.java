package lipid;

public class Peak implements Comparable <Peak> {

    private final double mz;
    private final double intensity;

    public Peak(double mz, double intensity) {
        this.mz = mz;
        this.intensity = intensity;
    }

    public double getMz() {
        return mz;
    }

    public double getIntensity() {
        return intensity;
    }

    @Override
    public String toString() {
        return String.format("Peak(mz=%.4f, intensity=%.2f)", mz, intensity);
    }

    @Override
    public int hashCode() {
        return Double.hashCode(mz) * 31;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Peak)) return false;
        Peak other = (Peak) obj;
        return Double.compare(mz, other.mz) == 0;
    }

    //método compare to implement
    // !!TODO This set should be sorted according to help the program to deisotope the signals plus detect the adduct
    //We use this to sort the peaks from lowest to greatest mz
    public int compareTo(Peak other) {
        return Double.compare(this.mz, other.mz);
    }
}