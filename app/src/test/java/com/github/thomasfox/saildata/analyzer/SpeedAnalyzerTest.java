package com.github.thomasfox.saildata.analyzer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import android.location.Location;

import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class SpeedAnalyzerTest {

    private  SpeedAnalyzer speedAnalyzer;

    private MemoryLocationStore memoryLocationStore;

    @Before
    public void createSpeedAnalyzer() {
        memoryLocationStore = new MemoryLocationStore();
        speedAnalyzer = new SpeedAnalyzer(memoryLocationStore);
    }

    @Test
    public void maxSpeedAveragedInKnots_justInitialized() {
        assertThat(speedAnalyzer.getMaxSpeedAveragedInKnots(1000)).isNaN();
    }

    @Test
    public void maxSpeedAveragedInKnots_oneLocation() {
        addLocation(0, 0, 0);
        assertThat(speedAnalyzer.getMaxSpeedAveragedInKnots(1000)).isNaN();
    }

    @Test
    public void maxSpeedAveragedInKnots_timeNotIncreasing() {
        addLocation(0, -100, -100);
        addLocation(500, -100, -100);
        addLocation(10000, 0, 0);
        addLocation(9999, -100, -100);
        addLocation(15000, -100, -100);
        assertThat(speedAnalyzer.getMaxSpeedAveragedInKnots(1000)).isNaN();
    }

    @Test
    public void maxSpeedAveragedInKnots_twoLocationsNoDistance() {
        addLocation(0, 0, 0);
        addLocation(10000, 0, 0);
        assertThat(speedAnalyzer.getMaxSpeedAveragedInKnots(1000)).isEqualTo(0);
    }

    @Test
    public void maxSpeedAveragedInKnots_twoLocationsX() {
        addLocation(0, 0, 0);
        addLocation(10000, 100, 0);
        assertThat(speedAnalyzer.getMaxSpeedAveragedInKnots(1000))
                .isCloseTo(19.43844, Offset.offset(0.0001));
    }

    @Test
    public void maxSpeedAveragedInKnots_twoLocationsY() {
        addLocation(0, 0, 0);
        addLocation(10000, 0, 100);
        assertThat(speedAnalyzer.getMaxSpeedAveragedInKnots(1000))
                .isCloseTo(19.43844, Offset.offset(0.0001));
    }

    @Test
    public void maxSpeedAveragedInKnots_twoLocationsXAndY() {
        addLocation(0, 0, 0);
        addLocation(10000, -100, -100);
        assertThat(speedAnalyzer.getMaxSpeedAveragedInKnots(1000))
                .isCloseTo(19.43844*Math.sqrt(2), Offset.offset(0.0001));
    }

    @Test
    public void maxSpeedAveragedInKnots_severalLocationsCloserThanAverageTime() {
        addLocation(0, 0, 0);
        addLocation(500, 1, 0);
        addLocation(1000, 1, 0); // first point for largest velocity
        addLocation(1500, 2, 0);
        addLocation(2000, 6, 0);
        addLocation(2500, 7, 0); // second point for largest velocity
        addLocation(3000, 7, 0);
        addLocation(3500, 8, 0);
        assertThat(speedAnalyzer.getMaxSpeedAveragedInKnots(1100))
                .isCloseTo(4d*3600/1852, Offset.offset(0.0001));
    }

    @Test
    public void maxSpeedAveragedInKnots_severalLocationsLargerThanAverageTime() {
        addLocation(0, 0, 0);
        addLocation(500, 1, 0);
        addLocation(1000, 1, 0);
        addLocation(1500, 2, 0); // first point for largest velocity
        addLocation(2000, 6, 0); // second point for largest velocity
        addLocation(2500, 7, 0);
        addLocation(2750, 7, 0);
        addLocation(4000, 8, 0);
        assertThat(speedAnalyzer.getMaxSpeedAveragedInKnots(400))
                .isCloseTo(8d*3600/1852, Offset.offset(0.0001));
    }

    private void addLocation(long time, double x, double y)
    {
        Location location = Mockito.mock(Location.class);
        when(location.getTime()).thenReturn(time);
        double latitude = y / Constants.EARTH_RADIUS;
        when(location.getLatitude()).thenReturn(latitude);
        when(location.getLongitude()).thenReturn(x / Constants.EARTH_RADIUS / Math.cos(latitude));
        memoryLocationStore.onLocationChanged(location);
    }
}