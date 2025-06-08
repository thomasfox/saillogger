package com.github.thomasfox.saildata.analyzer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.location.Location;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.thomasfox.saildata.analyzer.gpx.Gpx;
import com.github.thomasfox.saildata.analyzer.gpx.GpxPoint;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TackDirectionChangeAnalyzerTest {

    private TackDirectionChangeAnalyzer tackDirectionChangeAnalyzer;

    @Before
    public void createTackDirectionChangeAnalyzer() {
        tackDirectionChangeAnalyzer = new TackDirectionChangeAnalyzer();
    }

    // tests with lat and long set to ints -> easy understanding of tests
    @Test
    public void justInitialized() {
        assertThat(tackDirectionChangeAnalyzer.getOffTackCounter()).isEqualTo(0);
        assertThat(tackDirectionChangeAnalyzer.getDirectionRelativeToTackDirection()).isNull();
        assertThat(tackDirectionChangeAnalyzer.getTackStartLocation()).isNull();
        assertThat(tackDirectionChangeAnalyzer.getLastLocationInTack()).isNull();
    }

    @Test
    public void startLatLongLocationReceived() {
        givenLocationReceivedWithLatZeroAndLongZero();

        assertThat(tackDirectionChangeAnalyzer.getOffTackCounter()).isEqualTo(0);
        assertThat(tackDirectionChangeAnalyzer.getDirectionRelativeToTackDirection()).isNull();
        assertThat(tackDirectionChangeAnalyzer.getTackStartLocation().getLatitude())
                .isEqualTo(0d);
        assertThat(tackDirectionChangeAnalyzer.getTackStartLocation().getLongitude())
                .isEqualTo(0d);
        assertThat(tackDirectionChangeAnalyzer.getLastLocationInTack().getLatitude())
                .isEqualTo(0d);
        assertThat(tackDirectionChangeAnalyzer.getLastLocationInTack().getLongitude())
                .isEqualTo(0d);
    }

    @Test
    public void secondLatLongLocationReceived() {
        givenSecondLocationReceivedWithLat0AndLong90();

        assertThat(tackDirectionChangeAnalyzer.getOffTackCounter()).isEqualTo(0);
        assertThat(tackDirectionChangeAnalyzer.getTackStartLocation().getLatitude())
                .isEqualTo(0d);
        assertThat(tackDirectionChangeAnalyzer.getTackStartLocation().getLongitude())
                .isEqualTo(0d);
        assertThat(tackDirectionChangeAnalyzer.getLastLocationInTack().getLatitude())
                .isEqualTo(0d);
        assertThat(tackDirectionChangeAnalyzer.getLastLocationInTack().getLongitude())
                .isEqualTo(90d);
    }

    @Test
    public void firstOffLatLongLocationReceived() {
        givenThirdLocationReceivedWithLat1AndLong90();

        assertThat(tackDirectionChangeAnalyzer.getOffTackCounter()).isEqualTo(1);
        assertThat(tackDirectionChangeAnalyzer.getTackStartLocation().getLatitude())
                .isEqualTo(0d);
        assertThat(tackDirectionChangeAnalyzer.getTackStartLocation().getLongitude())
                .isEqualTo(0d);
        assertThat(tackDirectionChangeAnalyzer.getLastLocationInTack().getLatitude())
                .isEqualTo(1d);
        assertThat(tackDirectionChangeAnalyzer.getLastLocationInTack().getLongitude())
                .isEqualTo(90d);
    }

    @Test
    public void secondOffLatLongLocationReceived() {
        givenFourthLocationReceivedWithLat2AndLong90();

        assertThat(tackDirectionChangeAnalyzer.getOffTackCounter()).isEqualTo(0);
        assertThat(tackDirectionChangeAnalyzer.getTackStartLocation().getLatitude())
                .isEqualTo(2d);
        assertThat(tackDirectionChangeAnalyzer.getTackStartLocation().getLongitude())
                .isEqualTo(90d);
        assertThat(tackDirectionChangeAnalyzer.getLastLocationInTack().getLatitude())
                .isEqualTo(2d);
        assertThat(tackDirectionChangeAnalyzer.getLastLocationInTack().getLongitude())
                .isEqualTo(90d);
    }

    private Location givenLocationReceivedWithLatZeroAndLongZero()
    {
        Location createdLocation = createLatLongLocation(0, 0);
        tackDirectionChangeAnalyzer.onLocationChanged(createdLocation);
        return createdLocation;
    }

    private List<Location> givenSecondLocationReceivedWithLat0AndLong90()
    {
        Location zeroLocation = givenLocationReceivedWithLatZeroAndLongZero();
        Location long90Location = createLatLongLocation(0, 90);
        when(zeroLocation.bearingTo(long90Location)).thenReturn(90f);

        tackDirectionChangeAnalyzer.onLocationChanged(long90Location);
        List<Location> result = new ArrayList<>();
        result.add(zeroLocation);
        result.add(long90Location);
        return result;
    }

    private List<Location> givenThirdLocationReceivedWithLat1AndLong90()
    {
        List<Location> alreadyCreatedLocations = givenSecondLocationReceivedWithLat0AndLong90();
        Location lat1long90Location = createLatLongLocation(1, 90);
        when(alreadyCreatedLocations.get(0).bearingTo(lat1long90Location)).thenReturn(89f);
        when(alreadyCreatedLocations.get(1).bearingTo(lat1long90Location)).thenReturn(0f);

        tackDirectionChangeAnalyzer.onLocationChanged(lat1long90Location);
        List<Location> result = new ArrayList<>(alreadyCreatedLocations);
        result.add(lat1long90Location);
        return result;
    }

    private void givenFourthLocationReceivedWithLat2AndLong90()
    {
        List<Location> alreadyCreatedLocations = givenThirdLocationReceivedWithLat1AndLong90();
        Location lat2long90Location = createLatLongLocation(2, 90);
        when(alreadyCreatedLocations.get(0).bearingTo(lat2long90Location)).thenReturn(88f);
        when(alreadyCreatedLocations.get(2).bearingTo(lat2long90Location)).thenReturn(0f);

        tackDirectionChangeAnalyzer.onLocationChanged(lat2long90Location);
    }

    private Location createLatLongLocation(double latitude, double longitude) {
        Location result = mock(Location.class);
        when(result.getLatitude()).thenReturn(latitude);
        when(result.getLongitude()).thenReturn(longitude);
        return result;
    }

    private List<Location> loadGpxFile(File gpxFile)
    {
        List<Location> result = new ArrayList<>();
        List<GpxPoint> rawData = loadGpxFileInternal(gpxFile);
        for (GpxPoint rawPoint : rawData)
        {
            Location location = createLatLongLocation(rawPoint.lat, rawPoint.lon);
            result.add(location);
        }
        return result;
    }

    private List<GpxPoint> loadGpxFileInternal(File gpxFile)
    {
        List<GpxPoint> result;
        try (InputStream is = new FileInputStream(gpxFile))
        {
            ObjectMapper xmlMapper = new XmlMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Gpx value = xmlMapper.readValue(is, Gpx.class);
            result = value.trk.trkseg.trkpt;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return result;
    }
}