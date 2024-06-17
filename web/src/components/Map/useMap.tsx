import debounce from "lodash/debounce";
import { useState, useEffect } from "react";
import maplibregl from "maplibre-gl";
import { LngLat, LngLatBounds } from "maplibre-gl";
import { createMapLibreGlMapController } from "@maptiler/geocoding-control/maplibregl-controller";
import "@maptiler/geocoding-control/style.css";

import baseMapStyleJSON from "../../assets/baseMapStyle.json";
import dataSourcesStyleJSON from "../../assets/sources_style.json";
import dataCrimesStyleJSON from "../../assets/datacrimes_style.json";
import data311sStyleJSON from "../../assets/data311s_style.json";
import heatMapStyleJSON from "../../assets/heatmap_style.json";
import DuplicatePointNudge from "./DuplicatePointNudge";

export default function useMap(props) {
  //const mapRef = useRef<maplibregl.Map>();
  const [map, setMap] = useState(null);
  const [mapController, setMapController] = useState(null);

  useEffect(() => {
    console.log("[useMap] Hook Init");

    if (!props.isDataLoaded) return; //NB: wait until data fetched before creating map

    const style = {
      glyphs:
        "https://alanverga.com/basemaps-assets/fonts/{fontstack}/{range}.pbf",
      version: 8,
      sources: {
        protomaps: {
          type: "vector",
          url: "https://api.protomaps.com/tiles/v3.json?key=a8e24b8d978e4df3",
          attribution:
            '© <a href="https://openstreetmap.org">OpenStreetMap</a> | <a href="https://protomaps.com/">Protomaps',
          minzoom: 2,
          maxzoom: 12,
        },
        ...props.dataSources,
      },
      layers: [
        ...baseMapStyleJSON,
        ...dataSourcesStyleJSON,
        ...dataCrimesStyleJSON,
        ...data311sStyleJSON,
        ...heatMapStyleJSON,
      ],
    };

    console.log("NEW MAP");
    const _map = new maplibregl.Map({
      container: "map",
      //center: [-97.7171, 30.2944], // starting position [lng, lat]
      center: props.position.center,
      zoom: 12, // starting zoom
      style,
    });

    //_map.showTileBoundaries = true;

    _map.on("load", async () => {
      console.log("Load");
    });

    for (const dataset of [
      props.DATASOURCES.Data311s,
      props.DATASOURCES.DataCrimes,
    ]) {
      // When a click event occurs on a feature in
      // the unclustered-point layer, open a popup at
      // the location of the feature, with
      // description HTML from its properties.

      _map.on("click", `point-${dataset}`, (e) => {
        console.log("CLICK unclustered");
        if (e.clickOnLayer) return;
        e.clickOnLayer = true;

        const source = e.features[0].source;
        const layer = e.features[0].layer;
        const circleLayerID = `point-circle-${dataset}`;

        const reportNum = e.features[0].properties.reportNum;
        const features = e.features;

        //increae icon size
        _map.setLayoutProperty(layer.id, "text-size", [
          "match",
          ["get", "reportNum"],
          reportNum, // get the feature id
          30, //new text-size
          18, //default - needs to be constant not layer reference (since it will change here)
        ]);

        //increase background circle radius
        _map.setPaintProperty(circleLayerID, "circle-radius", [
          "match",
          ["get", "reportNum"],
          reportNum,
          24, //new radius
          16, //default
        ]);

        const featureList = {
          source,
          features,
        };

        props.setActiveReportNum(reportNum);
        props.setActiveFeatureList(featureList);
      });

      //click on a clustered point
      _map.on("click", `clusters-${dataset}`, async (e) => {
        console.log("CLICK Cluster", e);

        if (e.clickOnLayer) return;
        e.clickOnLayer = true;

        const source = e.features[0].source;
        const cluster_id = e.features[0].properties.cluster_id;
        const coordinates = e.features[0].geometry.coordinates;
        const point_count = e.features[0].properties.point_count;

        const clusterSource = _map.getSource(source);
        const clusterMaxZoom = props.dataSources[dataset].clusterMaxZoom;

        //getClusterExpansionZoom returns (clusterMaxZoom + 1) when
        //the cluster is "terminal". Meaning any deeper zoom will not
        //break up the cluster.
        const clusterExpansionZoom = await _map
          .getSource(dataset)
          .getClusterExpansionZoom(cluster_id);

        //1. get list of individual elements in cluster (ids)
        //2. set open
        const features = await clusterSource.getClusterLeaves(
          cluster_id,
          point_count,
          0
        );

        //if the next cluster zoom is less than max, zoom in.
        //otherwise we're at "terminal" cluster, no need to zoom any further
        if (clusterExpansionZoom < clusterMaxZoom) {
          _map.easeTo({
            center: coordinates,
            zoom: clusterExpansionZoom,
          });
        }

        const featureList = {
          source,
          features,
          clusterExpansionZoom,
          clusterMaxZoom,
        };

        props.setActiveFeatureList(featureList);
      });
    }

    /*
     * "general" click handler
     * this is clicking anywhere not a "point" (cluster / uncluster)
     * so anywhere that's not base source protomaps
     * used to clear displays like feature list
     */
    _map.on("click", (e) => {
      const features = _map
        .queryRenderedFeatures(e.point)
        .filter((f) => f.source != "protomaps");

      console.log("GEN CLICK", features);

      //TODO: refactor this once default values figured out

      if (features.length) {
        if (features[0].source.includes("311")) {
          _map.setLayoutProperty("point-dataCrimes", "text-size", 18);
          _map.setPaintProperty("point-circle-dataCrimes", "circle-radius", 16);
        } else {
          _map.setLayoutProperty("point-data311s", "text-size", 18);
          _map.setPaintProperty("point-circle-data311s", "circle-radius", 16);
        }
      }

      //clear all
      if (features.length === 0) {
        //clear, turn off anything in previous click handlers
        _map.setLayoutProperty("point-dataCrimes", "text-size", 18);
        _map.setLayoutProperty("point-data311s", "text-size", 18);

        _map.setPaintProperty("point-circle-dataCrimes", "circle-radius", 16);
        _map.setPaintProperty("point-circle-data311s", "circle-radius", 16);

        props.setActiveFeatureList({});
      }
    });

    const debouncedZoomNudgeHandler = debounce((e) => {
      console.log("debouncedZoom", e);

      const sourceData311s = _map.getSource("data311s");
      const sourceDataCrimes = _map.getSource("dataCrimes");

      let sd311 = sourceData311s._data;
      let sdCrime = sourceDataCrimes._data;

      const layers = [
        "clusters-dataCrimes",
        "clusters-data311s",
        "point-data311s",
        "point-dataCrimes",
      ];

      const features = _map
        .queryRenderedFeatures({ layers })
        .filter((f) => f.source != "protomaps");

      const duplicatePoint = new DuplicatePointNudge(features);
      duplicatePoint.init();

      duplicatePoint.nudge(sd311, "311");
      duplicatePoint.nudge(sdCrime, "crime");

      sourceData311s.setData(sd311);
      sourceDataCrimes.setData(sdCrime);
    }, 300);

    _map.on("zoom", debouncedZoomNudgeHandler);

    _map.on("moveend", async (e) => {
      const bounds = _map.getBounds();
      const maxBounds = new LngLatBounds(
        props.position.maxBounds.sw,
        props.position.maxBounds.ne
      );

      //test if exceeds, set new maxBounds
      const refresh = !(
        maxBounds.contains(bounds.getSouthWest()) &&
        maxBounds.contains(bounds.getNorthEast())
      );

      console.log("onMove position fetch refresh:", refresh);

      const _position = {
        ...props.position,
        zoom: _map.getZoom(),
        center: _map.getCenter(),
        bounds,
        maxBounds: refresh
          ? {
              sw: new LngLat(
                Math.floor(bounds.getSouthWest().lng),
                Math.floor(bounds.getSouthWest().lat)
              ),
              ne: new LngLat(
                Math.ceil(bounds.getNorthEast().lng),
                Math.ceil(bounds.getNorthEast().lat)
              ),
            }
          : props.position.maxBounds,
        refresh,
      };

      props.setPosition(_position);
    });

    _map.addControl(new maplibregl.NavigationControl(), "bottom-right");

    //mapRef.current = _map;
    setMap(_map);
    setMapController(createMapLibreGlMapController(_map, maplibregl, false));

    return () => {
      if (_map) {
        console.log("[useMap] Remove");
        _map.remove();
        //maplibregl.removeProtocol("pmtiles");
      }
    };
  }, [props.isDataLoaded]);

  return { map, mapController };
}
