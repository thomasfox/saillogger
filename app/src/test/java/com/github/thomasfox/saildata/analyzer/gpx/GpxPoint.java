package com.github.thomasfox.saildata.analyzer.gpx;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Date;

public class GpxPoint {
    public Date time;

    @JacksonXmlProperty(isAttribute = true)
    public double lat;

    @JacksonXmlProperty(isAttribute = true)
    public double lon;
}
