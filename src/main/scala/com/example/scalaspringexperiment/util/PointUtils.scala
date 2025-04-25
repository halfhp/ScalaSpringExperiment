package com.example.scalaspringexperiment.util

import net.postgis.jdbc.geometry.Point

object PointUtils {

  def pointFromLatLon(
    lat: Double,
    lon: Double,
  ): Point = {
    new Point(lat, lon)
  }
}
