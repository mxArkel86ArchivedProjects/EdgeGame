package util;

public class MathUtil {

    public static double min_(double... mins) {
    	double val = mins[0];
    	for (double d : mins) {
    		val = Math.min(val, d);
    	}
    	return val;
    }

    static double max_(double... maxs) {
    	double val = maxs[0];
    	for (double d : maxs) {
    		val = Math.min(val, d);
    	}
    	return val;
    }
    
}
