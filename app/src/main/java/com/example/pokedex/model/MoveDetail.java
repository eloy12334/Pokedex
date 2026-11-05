
package com.example.pokedex.model;

public class MoveDetail {

    private String name;
    private int power;
    private int accuracy;
    private int pp;
    private int priority;

    private TypeInfo type;
    private DamageClass damage_class;

    public String getName() {
        return name;
    }

    public int getPower() {
        return power;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public int getPp() {
        return pp;
    }

    public int getPriority() {
        return priority;
    }

    public TypeInfo getType() {
        return type;
    }

    public DamageClass getDamage_class() {
        return damage_class;
    }

    public static class TypeInfo {

        private String name;

        public String getName() {
            return name;
        }
    }

    public static class DamageClass {

        private String name;

        public String getName() {
            return name;
        }
    }
}
