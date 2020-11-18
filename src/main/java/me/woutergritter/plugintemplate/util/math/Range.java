package me.woutergritter.plugintemplate.util.math;

public class Range {
    public final int min, max;

    /**
     * @param min Minimum (inclusive)
     * @param max Maximum (inclusive)
     */
    public Range(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public boolean isInRange(int n) {
        return n >= min && n <= max;
    }

    public int random() {
        return (int) (Math.random() * (max + 1 - min) + min);
    }

    public static Range parse(String s) {
        if(!s.contains("-")) {
            return null;
        }

        String[] parts = s.split("-");
        if(parts.length != 2) {
            return null;
        }

        int min, max;
        try{
            min = Integer.parseInt(parts[0]);
            max = Integer.parseInt(parts[1]);
        }catch(NumberFormatException e) {
            return null;
        }

        return new Range(min, max);
    }
}
