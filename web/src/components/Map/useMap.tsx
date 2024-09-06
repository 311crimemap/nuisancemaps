import debounce from "lodash/debounce";
import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import maplibregl from "maplibre-gl";
import { LngLat, LngLatBounds } from "maplibre-gl";
import { createMapLibreGlMapController } from "@maptiler/geocoding-control/maplibregl-controller";
import "@maptiler/geocoding-control/style.css";
import { slugify, calcMaxLatLngBounds } from "../../Util";

import baseMapStyleJSON from "../../assets/baseMapStyle.json";
import dataSourcesStyleJSON from "../../assets/sources_style.json";
import dataCrimesStyleJSON from "../../assets/datacrimes_style.json";
import data311sStyleJSON from "../../assets/data311s_style.json";
import heatMapStyleJSON from "../../assets/heatmap_style.json";
import DuplicatePointNudge from "./DuplicatePointNudge";

const MAX_DATA_RECORDS = import.meta.env.VITE_MAX_DATA_RECORDS;

export default function useMap(props) {
  //const mapRef = useRef<maplibregl.Map>();
  const { city } = useParams();
  const [map, setMap] = useState(null);
  const [mapController, setMapController] = useState(null);

  useEffect(() => {
    if (!props.isInitLoaded) {
      return;
    }

    console.log("[useMap] Hook Init");

    const attribution = [
      '<a href="https://openstreetmap.org">&copy; OpenStreetMap</a>',
      '<a href="https://protomaps.com/">Protomaps</a>',
      '<a href= "https://www.maptiler.com/copyright/" target="_blank" >&copy; MapTiler</a>',
      'Powered by <a href="https://www.geoapify.com/">Geoapify</a>',
    ].join(" | ");

    const style = {
      glyphs: "https://basemaps.311crimemap.com/fonts/{fontstack}/{range}.pbf",
      version: 8,
      sources: {
        protomaps: {
          type: "vector",
          url: `https://api.protomaps.com/tiles/v3.json?key=${
            import.meta.env.VITE_PROTOMAPS_API_KEY
          }`,
          attribution,
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

    console.log("NEW MAP", style);

    const _map = new maplibregl.Map({
      container: "map",
      center: props.position.center,
      zoom: props.position.zoom, // starting zoom
      style,
    });

    // if city parameter initially exists, center to that city
    if (city) {
      const _position = cityPosition(
        props.position,
        props.dataSources.sources.data.features,
        city
      );
      if (!_position) return;

      _map.setCenter(_position.center);
      _map.setZoom(12);
    }

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

      const dataCrimesSource = _map.getSource(props.DATASOURCES.DataCrimes);
      const data311sSource = _map.getSource(props.DATASOURCES.Data311s);
      const numDataCrimesSource =
        dataCrimesSource?._data?.features?.length || 0;
      const numData311sSource = data311sSource?._data?.features?.length || 0;

      const newMaxBounds = calcMaxLatLngBounds(bounds, _map.getZoom());

      // NB: need functional update as props.position is a stale closure
      props.setPosition((prevPosition) => {
        const SOURCE_LAYER_ZOOM = 10; //TODO: set some source layer constant
        const zoom = _map.getZoom();

        //current location exceeds fetch bounds, need to refetch with new position
        const exceedBounds = !(
          prevPosition.fetchBounds?.contains(bounds.getSouthWest()) &&
          prevPosition.fetchBounds?.contains(bounds.getNorthEast())
        );

        // zoomInToggle: true when zooming in and layer transitions from
        // sources layer to data
        const zoomInToggle =
          zoom > SOURCE_LAYER_ZOOM && prevPosition.zoom <= SOURCE_LAYER_ZOOM;

        // indicates a zoom in behavior
        const zoomIn = prevPosition.zoom < zoom;

        // indicates a zoom out behavior
        const zoomOut = prevPosition.zoom > zoom;

        const isMax =
          numData311sSource >= MAX_DATA_RECORDS ||
          numDataCrimesSource >= MAX_DATA_RECORDS;

        // isRefresh criteria
        //
        // 1. exceedBounds, except when zooming out and displaying source layer
        // * on zoom out, bounds by definition expand, so exceedBounds will
        //   always be true.
        //
        // * This is wasteful when in source layer zoom and data is not visible
        //
        // * so refresh when exceed bounds, except to avoid excess fetches:
        //   (exceedBounds && !zoomOut and zoom >= SOURCE_LAYER_ZOOM)
        //
        // 2.zoomInToggle: from source to data layer, refetch
        //
        const isRefresh =
          (exceedBounds && !zoomOut && zoom >= SOURCE_LAYER_ZOOM) ||
          (zoomIn && isMax) ||
          zoomInToggle;

        const newPosition = {
          ...prevPosition,
          zoom: _map.getZoom(),
          center: _map.getCenter(),
          bounds,
          fetchBounds: isRefresh ? newMaxBounds : prevPosition.fetchBounds,
          refresh: prevPosition.refresh + isRefresh,
        };

        console.log(
          "[useMap] onMove isRefresh: ",
          isRefresh,
          "| ",
          "exceed but not zoomOut and zoom >= 10:",
          exceedBounds && !zoomOut && zoom >= 10,
          " || ",
          exceedBounds,
          !zoomOut,
          zoom >= 10,
          "||",
          "zoomIn Max",
          zoomIn && isMax,
          "||",
          "zoomInToggle: ",
          zoomInToggle
        );

        console.log(
          "[useMap] onMove isRefresh: ",
          isRefresh,
          prevPosition,
          newPosition
        );

        return newPosition;
      });
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
  }, [props.isInitLoaded]);

  return { map, mapController };
}

function cityPosition(position: any, features: any, city: any) {
  if (!city) return null;
  for (const feature of features) {
    if (slugify(feature.properties.city) == slugify(city)) {
      position = {
        ...position,
        //TODO: bounds?
        center: {
          lat: feature.properties.location[1],
          lng: feature.properties.location[0],
        },
      };
      return position;
    }
  }

  return null;
}
