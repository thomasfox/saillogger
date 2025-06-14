package com.github.thomasfox.saildata.analyzer;

import static org.mockito.Mockito.when;

import android.location.Location;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class SaildataLocationProvider {

    SailLoggerData sailLoggerData;

    public SaildataLocationProvider(InputStream locationFile)
    {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            sailLoggerData = objectMapper.readValue(locationFile, SailLoggerData.class);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public MemoryLocationStore getMemoryLocationStore()
    {
        MemoryLocationStore memoryLocationStore = new MemoryLocationStore();
        for (SailLoggerTrackPoint point : sailLoggerData.track)
        {
            Location location = Mockito.mock(Location.class);
            when(location.getTime()).thenReturn(point.locT);
            when(location.getLatitude()).thenReturn(point.locLat);
            when(location.getLongitude()).thenReturn(point.locLong);


            when(location.getAccuracy()).thenReturn(point.locAcc);
            when(location.getBearing()).thenReturn(point.locBear);
            when(location.getSpeed()).thenReturn(point.locVel);
            when(location.getAltitude()).thenReturn(point.locAlt);
            memoryLocationStore.onLocationChanged(location);
        }
        return memoryLocationStore;
    }

    private static final class SailLoggerData
    {
        public SailLoggerStart start;
        public ArrayList<SailLoggerTrackPoint> track = new ArrayList<>();
        public SailLoggerEnd end;
    }

    private static final  class SailLoggerStart
    {
        /** Contains the version of the sailLogger data format used to write the data. */
        public String format;
        /** Contains the name of the program which logged the data. */
        public String loggedBy;
        /** Contains the version of the program which logged the data. */
        public String loggedByVersion;
        /**
         * The manufacturer of the device used to record the data.
         * (with typo for backwards compatibility)
         */
        public String recordedByManufactorer;
        /** The manufacturer of the device used to record the data. */
        public String recordedByManufacturer;
        /** The model name of the device used to record the data */
        public String recordedByModel;
        /** The time when recording started (typically using the hardware time of the recording device) */
        public long startT;
        /** The time when recording started, in a human readable format (dd.MM.yyyy' 'HH:mm:ss.SSSZ) */
        public String startTFormatted;
    }

    private static final class SailLoggerTrackPoint
    {
        public Long locT;
        public Float locAcc;
        public Double locLat;
        public Double locLong;
        public Float locBear;
        public Float locVel;
        public Double locAlt;
        public Long locDevT;
        public Long magT;
        public Double magX;
        public Double magY;
        public Double magZ;
        public Long accT;
        public Double accX;
        public Double accY;
        public Double accZ;

        public boolean hasGpsData()
        {
            return (locT != null);
        }

        public boolean hasCompassData()
        {
            return (magX != null && magY != null && magZ != null && magT != null);
        }

        public boolean hasAccelerationData()
        {
            return (accX != null && accY != null && accZ != null && accT != null);
        }
    }

    private static final class SailLoggerEnd
    {
        /** The time when recording ended (typically using the hardware time of the recording device) */
        public long endT;
        /** The time when recording started, in a human readable format (dd.MM.yyyy' 'HH:mm:ss.SSSZ) */
        public String endTFormatted;
    }
}
