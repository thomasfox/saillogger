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
        when(location.getLatitude()).thenReturn(180d/Math.PI);
        when(location.getLongitude()).thenReturn(-180d/Math.PI);
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

    @Test
    public void getX()
    {
        Location location = Mockito.mock(Location.class);
        when(location.getLatitude()).thenReturn(0d);
        when(location.getLongitude()).thenReturn(90d);
        MemoryLocation memoryLocation = new MemoryLocation(location);
        assertThat(memoryLocation.getX()).isCloseTo(6371000d * Math.PI/2, Offset.offset(1d));
        assertThat(memoryLocation.getY()).isEqualTo(0d);
    }

    @Test
    public void getY()
    {
        Location location = Mockito.mock(Location.class);
        when(location.getLatitude()).thenReturn(90d);
        when(location.getLongitude()).thenReturn(0d);
        MemoryLocation memoryLocation = new MemoryLocation(location);
        assertThat(memoryLocation.getY()).isCloseTo(6371000d * Math.PI/2, Offset.offset(1d));
        assertThat(memoryLocation.getX()).isEqualTo(0d);
    }

    @Test
    public void getXAndY()
    {
        Location location = Mockito.mock(Location.class);
        when(location.getLatitude()).thenReturn(45d);
        when(location.getLongitude()).thenReturn(45d);
        MemoryLocation memoryLocation = new MemoryLocation(location);
        assertThat(memoryLocation.getY()).isCloseTo(6371000d * Math.PI/4, Offset.offset(1d));
        assertThat(memoryLocation.getX()).isEqualTo(6371000d * Math.PI/4/Math.sqrt(2d));
    }
}