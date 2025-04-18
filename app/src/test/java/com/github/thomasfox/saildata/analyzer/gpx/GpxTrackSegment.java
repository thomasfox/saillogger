package com.github.thomasfox.saildata.analyzer.gpx;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.List;

public class GpxTrackSegment {
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<GpxPoint> trkpt;
}
