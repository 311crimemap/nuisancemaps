import { debounce } from "lodash";
import { useState, useEffect, SetStateAction, Dispatch } from "react";
import { useParams } from "react-router-dom";
import maplibregl from "maplibre-gl";
import {
  LngLat,
  Map,
  GeoJSONSource,
  MapGeoJSONFeature,
  StyleSpecification,
} from "maplibre-gl";
import { MapController } from "@maptiler/geocoding-control/types";
import { createMapLibreGlMapController } from "@maptiler/geocoding-control/maplibregl-controller";
import "@maptiler/geocoding-control/style.css";
import { slugify, calcMaxLatLngBounds } from "../../Util";
import { Log } from "../../Logger";

import baseMapStyleJSON from "../../assets/baseMapStyle.json";
import dataSourcesStyleJSON from "../../assets/sources_style.json";
import dataCrimesStyleJSON from "../../assets/datacrimes_style.json";
import data311sStyleJSON from "../../assets/data311s_style.json";
import heatMapStyleJSON from "../../assets/heatmap_style.json";
import DuplicatePointNudge from "./DuplicatePointNudge";
import { DataFeatureCollection } from "../../types/datafeatures";
import { MapPosition } from "../../types/mapposition.ts";
import { DATASOURCES, DataSourcesMap } from "../../types/datasources";
import { ActiveFeatures } from "../../types/activefeatures";
import { defaultMapPosition } from "../../types/mapposition";

const MAX_DATA_RECORDS = import.meta.env.VITE_MAX_DATA_RECORDS;

interface useMapsProps {
  position: MapPosition;
  setPosition: Dispatch<SetStateAction<MapPosition>>;
  setActiveReportNum: Dispatch<SetStateAction<null>>;
  dataSources: DataSourcesMap;
  setActiveFeatures: Dispatch<SetStateAction<ActiveFeatures>>;
  featureZoomLevel: number;
  isInitLoaded: boolean;
}

export default function useMap({
  position,
  setPosition,
  setActiveReportNum,
  dataSources,
  setActiveFeatures,
  featureZoomLevel,
  isInitLoaded,
}: useMapsProps) {
  const { city } = useParams();
  const [map, setMap] = useState<Map>();
  const [mapController, setMapController] = useState<MapController>();

  useEffect(() => {
    if (!isInitLoaded) {
      return;
    }

    Log.log({ msg: "Hook Init", ...Log.data });

    const attribution = [
      '<a href="https://openstreetmap.org">&copy; OpenStreetMap</a>',
      '<a href="https://protomaps.com/">Protomaps</a>',
      '<a href= "https://www.maptiler.com/copyright/" target="_blank" >&copy; MapTiler</a>',
      'Powered by <a href="https://www.geoapify.com/">Geoapify</a>',
    ].join(" | ");

    // typing the baseMap is too much of mess
    const style: any | StyleSpecification = {
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
        ...dataSources,
      },
      layers: [
        ...baseMapStyleJSON,
        ...dataSourcesStyleJSON,
        ...dataCrimesStyleJSON,
        ...data311sStyleJSON,
        ...heatMapStyleJSON,
      ],
    };

    Log.log({ msg: "New Map", params: { style }, ...Log.data });

    const _map = new maplibregl.Map({
      container: "map",
      center: position.center,
      zoom: position.zoom, // starting zoom
      style,
    });

    // if city parameter initially exists, center to that city
    if (city) {
      const _position = cityPosition(
        position,
        dataSources.sources.data.features,
        city
      );
      if (!_position) return;

      _map.setCenter(_position.center);
      _map.setZoom(12);
    }

    _map.on("load", async () => {
      Log.log({ msg: "onLoad", ...Log.data });
    });

    //source points zoom in
    _map.on(
      "click",
      "point-circle-sources",
      async (e: maplibregl.MapLayerMouseEvent) => {
        Log.log({ msg: "click unclustered", params: { e }, ...Log.data });
        if (!e.features) return;

        const feature: MapGeoJSONFeature = e.features[0];
        const geometry = feature.geometry as GeoJSON.Point;
        const coordinates = geometry.coordinates;

        _map.flyTo({
          center: new LngLat(coordinates[0], coordinates[1]),
          zoom: 10,
        });
      }
    );

    for (const dataset of [DATASOURCES.Data311s, DATASOURCES.DataCrimes]) {
      // When a click event occurs on a feature in
      // the unclustered-point layer, open a popup at
      // the location of the feature, with
      // description HTML from its properties.

      _map.on(
        "click",
        `point-${dataset}`,
        (e: maplibregl.MapLayerMouseEvent) => {
          Log.log({ msg: "click unclustered", params: { e }, ...Log.data });

          if (!e.features) return;
          const features: MapGeoJSONFeature[] = e.features;
          const feature = e.features[0];
          const source = feature.source;
          const layer = feature.layer;
          const circleLayerID = `point-circle-${dataset}`;

          const reportNum = feature.properties.reportNum;

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

          const activeFeatures = {
            source,
            features,
          } as ActiveFeatures;

          setActiveReportNum(reportNum);
          setActiveFeatures(activeFeatures);
        }
      );

      //click on a clustered point
      _map.on(
        "click",
        `clusters-${dataset}`,
        async (e: maplibregl.MapLayerMouseEvent) => {
          Log.log({ msg: "click clustered", params: { e }, ...Log.data });

          if (!e.features) return;
          //const features: MapGeoJSONFeature[] = e.features;
          const feature: MapGeoJSONFeature = e.features[0];
          const source = feature.source;

          const cluster_id = feature.properties.cluster_id;
          const geometry = feature.geometry as GeoJSON.Point;
          const coordinates = geometry.coordinates;
          const point_count = feature.properties.point_count;

          const clusterSource = _map.getSource(source) as GeoJSONSource;
          const clusterMaxZoom =
            dataSources[dataset].clusterMaxZoom || featureZoomLevel;

          //getClusterExpansionZoom returns (clusterMaxZoom + 1) when
          //the cluster is "terminal". Meaning any deeper zoom will not
          //break up the cluster.
          let clusterExpansionZoom = clusterMaxZoom;
          const dataSetSource = (await _map.getSource(
            dataset
          )) as GeoJSONSource;
          if (dataSetSource) {
            clusterExpansionZoom = await dataSetSource.getClusterExpansionZoom(
              cluster_id
            );
          }

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
              center: new LngLat(coordinates[0], coordinates[1]),
              zoom: clusterExpansionZoom,
            });
          }

          const activeFeatures = {
            source,
            features,
            clusterExpansionZoom,
            clusterMaxZoom,
          } as ActiveFeatures;

          setActiveFeatures(activeFeatures);
        }
      );
    }

    /*
     * "general" click handler
     * this is clicking anywhere not a "point" (cluster / uncluster)
     * so anywhere that's not base source protomaps
     * used to clear displays like feature list
     */
    _map.on("click", (e: maplibregl.MapLayerMouseEvent) => {
      const features = _map
        .queryRenderedFeatures(e.point)
        .filter((f: MapGeoJSONFeature) => f.source != "protomaps");

      Log.log({ msg: "click", params: { features }, ...Log.data });

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

        setActiveFeatures({ source: "", features: [] });
      }
    });

    const debouncedZoomNudgeHandler = debounce(() => {
      const sourceData311s = _map.getSource("data311s") as
        | GeoJSONSource
        | undefined;
      const sourceDataCrimes = _map.getSource("dataCrimes") as
        | GeoJSONSource
        | undefined;

      if (!sourceData311s || !sourceDataCrimes) {
        Log.error({
          msg: "[debouncedZoomNudgeHandler] sources are undefined",
          ...Log.data,
        });
        return;
      }

      let sd311 = sourceData311s._data as DataFeatureCollection;
      let sdCrime = sourceDataCrimes._data as DataFeatureCollection;

      const layers = [
        "clusters-dataCrimes",
        "clusters-data311s",
        "point-data311s",
        "point-dataCrimes",
      ];

      const features = _map
        .queryRenderedFeatures({ layers })
        .filter((f: MapGeoJSONFeature) => f.source != "protomaps");

      const duplicatePoint = new DuplicatePointNudge(features);
      duplicatePoint.init();

      duplicatePoint.nudge(sd311, "311");
      duplicatePoint.nudge(sdCrime, "crime");

      if (sd311) sourceData311s.setData(sd311);
      if (sdCrime) sourceDataCrimes.setData(sdCrime);
    }, 300);

    _map.on("zoom", debouncedZoomNudgeHandler);

    _map.on("moveend", async () => {
      const bounds = _map.getBounds();

      const dataCrimesSource = _map.getSource(DATASOURCES.DataCrimes) as
        | GeoJSONSource
        | undefined;
      const data311sSource = _map.getSource(DATASOURCES.Data311s) as
        | GeoJSONSource
        | undefined;

      if (!dataCrimesSource || !data311sSource) {
        Log.error({ msg: "[moveend] sources are undefined", ...Log.data });
        return;
      }

      let sdCrime = dataCrimesSource._data as DataFeatureCollection;
      let sd311 = data311sSource._data as DataFeatureCollection;

      const numDataCrimesSource = sdCrime.features.length;
      const numData311sSource = sd311.features.length;

      const newMaxBounds = calcMaxLatLngBounds(bounds, _map.getZoom());

      // NB: need functional update as props.position is a stale closure
      setPosition((prevPosition: MapPosition) => {
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
          refresh: prevPosition.refresh + Number(isRefresh),
        };

        const logStr = [
          `[useMap] onMove isRefresh: ${isRefresh} |`,
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
          zoomInToggle,
        ].join(" ");

        Log.log({ msg: logStr, ...Log.data });

        Log.log({
          msg: "[useMap] onMove isRefresh: ",
          params: {
            isRefresh,
            prevPosition,
            newPosition,
          },
          ...Log.data,
        });

        return newPosition;
      });
    });

    _map.addControl(new maplibregl.NavigationControl(), "bottom-right");

    setMap(_map);
    setMapController(createMapLibreGlMapController(_map, maplibregl, false));

    // flyTo default position on home link click
    const $homeLink = document.getElementById("homelink");
    const homeLinkClick = () => _map.flyTo(defaultMapPosition);
    if ($homeLink) {
      $homeLink.addEventListener("click", homeLinkClick);
    }

    return () => {
      if (_map) {
        Log.log({ msg: "[useMap] Remove", ...Log.data });
        _map.remove();
      }

      if ($homeLink) {
        $homeLink.removeEventListener("click", homeLinkClick);
      }
    };
  }, [isInitLoaded]);

  return { map, mapController };
}

function cityPosition(position: any, features: any, city: any) {
  if (!city) return null;
  for (const feature of features) {
    if (slugify(feature.properties.city) == slugify(city)) {
      position = {
        ...position,
        //TODO: bounds?
        center: new LngLat(
          feature.properties.location[0],
          feature.properties.location[1]
        ),
      };
      return position;
    }
  }

  return null;
}
