package com.github.thomasfox.saildata.analyzer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import android.location.Location;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class MemoryLocationStoreTest {

    MemoryLocationStore memoryLocationStore = new MemoryLocationStore();

    @Before
    public void createSpeedAnalyzer() {
        memoryLocationStore = new MemoryLocationStore();
    }

    @Test
    public void isLocationTimeMonotonicallyIncreasing_noEntries()
    {
        assertThat(memoryLocationStore.isLocationTimeMonotonicallyIncreasing()).isTrue();
    }

    @Test
    public void isLocationTimeMonotonicallyIncreasing_oneEntry()
    {
        addLocation(0);
        assertThat(memoryLocationStore.isLocationTimeMonotonicallyIncreasing()).isTrue();
    }

    @Test
    public void isLocationTimeMonotonicallyIncreasing_twoEntriesNoDistance()
    {
        addLocation(0);
        addLocation(0);
        assertThat(memoryLocationStore.isLocationTimeMonotonicallyIncreasing()).isTrue();
    }

    @Test
    public void isLocationTimeMonotonicallyIncreasing_twoEntriesPositiveDistance()
    {
        addLocation(0);
        addLocation(1);
        assertThat(memoryLocationStore.isLocationTimeMonotonicallyIncreasing()).isTrue();
    }

    @Test
    public void isLocationTimeMonotonicallyIncreasing_twoEntriesNegativeDistance()
    {
        addLocation(1);
        addLocation(0);
        assertThat(memoryLocationStore.isLocationTimeMonotonicallyIncreasing()).isFalse();
    }

    @Test
    public void isLocationTimeMonotonicallyIncreasing_threeEntriesNoDistance()
    {
        addLocation(1);
        addLocation(1);
        addLocation(1);
        assertThat(memoryLocationStore.isLocationTimeMonotonicallyIncreasing()).isTrue();
    }

    @Test
    public void isLocationTimeMonotonicallyIncreasing_threeEntriesPositiveDistance()
    {
        addLocation(0);
        addLocation(0);
        addLocation(1);
        assertThat(memoryLocationStore.isLocationTimeMonotonicallyIncreasing()).isTrue();
    }

    @Test
    public void isLocationTimeMonotonicallyIncreasing_threeEntriesNegativeDistance()
    {
        addLocation(1);
        addLocation(1);
        addLocation(0);
        assertThat(memoryLocationStore.isLocationTimeMonotonicallyIncreasing()).isFalse();
    }

    private void addLocation(long time)
    {
        Location location = Mockito.mock(Location.class);
        when(location.getTime()).thenReturn(time);
        memoryLocationStore.onLocationChanged(location);
    }
}