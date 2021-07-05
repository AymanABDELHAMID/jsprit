package com.graphhopper.jsprit.core.problem;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BatteryTest {
    @Test
    public void whenSettingSimplyOneBatDimension_nuOfDimensionMustBeCorrect() {
        BatteryAM.Builder BatBuilder = BatteryAM.Builder.newInstance();
        BatBuilder.addDimension(0, 4);
        BatteryAM bat = BatBuilder.build();
        assertEquals(1, bat.getNuOfDimensions());
    }

    @Test
    public void whenSettingTwoBatDimension_nuOfDimensionMustBeCorrect() {
        BatteryAM.Builder BatBuilder = BatteryAM.Builder.newInstance();
        BatBuilder.addDimension(0, 4);
        BatBuilder.addDimension(1, 10);
        BatteryAM bat = BatBuilder.build();
        assertEquals(2, bat.getNuOfDimensions());
    }

    @Test
    public void whenSettingRandomNuOfBatDimension_nuOfDimensionMustBeCorrect() {
        Random rand = new Random();
        int nuOfBatDimensions = 1 + rand.nextInt(100);
        BatteryAM.Builder BatBuilder = BatteryAM.Builder.newInstance();
        BatBuilder.addDimension(nuOfBatDimensions - 1, 4);
        BatteryAM bat = BatBuilder.build();
        assertEquals(nuOfBatDimensions, bat.getNuOfDimensions());
    }

    @Test
    public void whenSettingOneDimValue_valueMustBeCorrect() {
        BatteryAM.Builder BatBuilder = BatteryAM.Builder.newInstance();
        BatBuilder.addDimension(0, 4);
        BatteryAM bat = BatBuilder.build();
        assertEquals(4, bat.get(0), 0.1);
    }

    @Test
    public void whenGettingIndexWhichIsHigherThanNuOfBatDimensions_itShouldReturn0() {
        BatteryAM.Builder BatBuilder = BatteryAM.Builder.newInstance();
        BatBuilder.addDimension(0, 4);
        BatteryAM bat = BatBuilder.build();
        assertEquals(0, bat.get(2), 0.1);
    }

    @Test
    public void whenSettingNoDim_DefaultIsOneDimWithDimValueOfZero() {
        BatteryAM.Builder BatBuilder = BatteryAM.Builder.newInstance();
        BatteryAM bat = BatBuilder.build();
        assertEquals(1, bat.getNuOfDimensions());
        assertEquals(0, bat.get(0), 0.1);
    }

    @Test
    public void whenCopyingBatteryAMWithTwoBatDim_copiedObjShouldHvSameNuOfDims() {
        BatteryAM.Builder BatBuilder = BatteryAM.Builder.newInstance();
        BatBuilder.addDimension(0, 4);
        BatBuilder.addDimension(1, 10);
        BatteryAM bat = BatBuilder.build();

        BatteryAM copiedBatteryAM = BatteryAM.copyOf(bat);
        assertEquals(2, copiedBatteryAM.getNuOfDimensions());
    }

    @Test
    public void whenCopyingBatteryAMWithTwoBatDim_copiedObjShouldHvSameValues() {
        BatteryAM.Builder BatBuilder = BatteryAM.Builder.newInstance();
        BatBuilder.addDimension(0, 4);
        BatBuilder.addDimension(1, 10);
        BatteryAM bat = BatBuilder.build();

        BatteryAM copiedBatteryAM = BatteryAM.copyOf(bat);
        assertEquals(4, copiedBatteryAM.get(0), 0.1);
        assertEquals(10, copiedBatteryAM.get(1), 0.1);
    }

    @Test
    public void whenCopyingNull_itShouldReturnNull() {
        BatteryAM nullBat = BatteryAM.copyOf(null);
        assertTrue(nullBat == null);
    }

}
