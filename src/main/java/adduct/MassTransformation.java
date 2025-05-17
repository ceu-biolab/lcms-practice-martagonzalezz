package adduct;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MassTransformation {
    //I declared these variables global as they will be used on all the methods of this class
    //Pattern for detecting the multimer
    private static final Pattern MULTIMER_PATTERN =
            Pattern.compile("\\[(\\d*)M");

    //Pattern for detecting the charge
    private static final Pattern CHARGE_PATTERN =
            Pattern.compile("(\\d+)?([\\+−])\\]");

    /**
     * M = (mz * z + Δ) / n
     * (usando massToSearch = –Δ de tus mapas)
     */

    public static Double getMonoisotopicMassFromMZ(Double mz, String adduct) {
        if (mz == null || adduct == null) return null;
        //If we don't have mz or adduct, we dont have monoisotopic mass

        //Searches for the adduct in both lists
        Double massToSearch = AdductList.MAPMZPOSITIVEADDUCTS.get(adduct);
        if (massToSearch == null) {
            massToSearch = AdductList.MAPMZNEGATIVEADDUCTS.get(adduct);
        }
        if (massToSearch == null) return null;

        //creates the matcher for detecting the multimer
       Matcher multiMatcher = MULTIMER_PATTERN.matcher(adduct);

        int multimer = 1; //as defect
        if (multiMatcher.find()) {
            if (!multiMatcher.group(1).isEmpty()){
            multimer = Integer.parseInt(multiMatcher.group(1));}
        }
        //Finds M

        //Creates the matcher for the charge
        Matcher chargeMatcher = CHARGE_PATTERN.matcher(adduct);
        int charge = 1; //as defect
        if (chargeMatcher.find() && chargeMatcher.group(1) != null) {
            if(!chargeMatcher.group(1).isEmpty()){
                charge = Integer.parseInt(chargeMatcher.group(1));
            }
        }
        //formula for monoisotopic mass
        return (((mz * charge )+ massToSearch) / multimer);
    }

    /**
     * mz = (M * n - Δ) / z
     */
    public static Double getMZFromMonoisotopicMass(Double M, String adduct) {
        if (M == null || adduct == null) return null;
        //If we dont have M or adduct, we dont have mz

        //Searches for the adduct in both of the lists
        Double massToSearch = AdductList.MAPMZPOSITIVEADDUCTS.get(adduct);
        if (massToSearch == null) {
            massToSearch = AdductList.MAPMZNEGATIVEADDUCTS.get(adduct);
        }
        if (massToSearch == null) return null;

        //Creates the matcher for detecting M
        Matcher multiMatcher = MULTIMER_PATTERN.matcher(adduct);
        int multimer = 1; //as defect
        if (multiMatcher.find()) {
            if (!multiMatcher.group(1).isEmpty()){
                multimer = Integer.parseInt(multiMatcher.group(1));
            }
        }
        //Creates the matcher for detecting charge
        int charge = 1; //as defect
        Matcher chargeMatcher = CHARGE_PATTERN.matcher(adduct);
        if (chargeMatcher.find() && chargeMatcher.group(1) != null) {
            if(!chargeMatcher.group(1).isEmpty()){
                charge = Integer.parseInt(chargeMatcher.group(1));
            }
        }
        //formula for mz
        return (((M * multimer) - massToSearch) / charge);
    }
}

