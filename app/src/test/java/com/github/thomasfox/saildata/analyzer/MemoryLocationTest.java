package com.github.thomasfox.saildata.analyzer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import android.location.Location;

import org.assertj.core.data.Offset;
import org.junit.Test;
import org.mockito.Mockito;

public class MemoryLocationTest {

    @Test
    public void Constructor() {
        Location location = Mockito.mock(Location.class);
        when(location.getTime()).thenReturn(123456789L);
        when(location.getLatitude()).thenReturn(360d/Math.PI);
        when(location.getLongitude()).thenReturn(-360d/Math.PI);
        when(location.getAccuracy()).thenReturn(5f);
        when(location.getBearing()).thenReturn(new Double(2*360f/Math.PI).floatValue());
        when(location.getSpeed()).thenReturn(3f);
        when(location.getAltitude()).thenReturn(456d);

        MemoryLocation memoryLocation = new MemoryLocation(location);

        assertThat(memoryLocation.getDeviceTimeMillis()).isCloseTo(System.currentTimeMillis(), Offset.offset(1000L));
        assertThat(memoryLocation.getLocationTimeMillis()).isEqualTo(123456789L);
        assertThat(memoryLocation.getLatitude()).isCloseTo(1d, Offset.offset(0.00001d));
        assertThat(memoryLocation.getLongitude()).isCloseTo(-1d, Offset.offset(0.00001d));
        assertThat(memoryLocation.getLocationAccuracy()).isEqualTo(5f);
        assertThat(memoryLocation.getLocationBearing()).isCloseTo(2f, Offset.offset(0.00001f));
        assertThat(memoryLocation.getLocationVelocity()).isEqualTo(3f);
        assertThat(memoryLocation.getLocationAltitude()).isEqualTo(456d);
    }
}