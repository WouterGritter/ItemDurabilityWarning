package me.woutergritter.itemdurabilitywarning.itemwarning;

public enum WarningType {
    NONE(0), SUBTLE(1), LARGE(2);

    public final int priority;

    WarningType(int priority) {
        this.priority = priority;
    }

    public boolean isHigherPriorityThan(WarningType other) {
        if(other == null) {
            return true;
        }

        return this.priority > other.priority;
    }
}
