import { useState, useEffect } from "react";
import maplibregl from "maplibre-gl";

import baseMapStyleJSON from "../../assets/baseMapStyle.json";
import dataCrimesStyleJSON from "../../assets/datacrimes_style.json";
import data311sStyleJSON from "../../assets/data311s_style.json";

export default function useMap(props) {

    //const mapRef = useRef<maplibregl.Map>();
    const [map, setMap] = useState(null);

    useEffect(() => {
        console.log("[useMap] Hook Init")

        if (!props.isDataLoaded) return; //NB: wait until data fetched before creating map

        const style = {
            glyphs: "https://alanverga.com/basemaps-assets/fonts/{fontstack}/{range}.pbf",
            version: 8,
            sources: {
                protomaps: {
                    type: "vector",
                    url: "https://api.protomaps.com/tiles/v3.json?key=a8e24b8d978e4df3",
                    attribution: '© <a href="https://openstreetmap.org">OpenStreetMap</a> | <a href="https://protomaps.com/">Protomaps',
                    minzoom: 2,
                    maxzoom: 12,
                },

                datacrimes: {
                    type: "geojson",
                    data: props.dataCrimes,
                    cluster: true,
                    clusterMaxZoom: 18, // Max zoom to cluster points on
                    clusterRadius: 50 // Radius of each cluster when clustering points (defaults to 50)
                },

                data311s: {
                    type: "geojson",
                    data: props.data311s,
                    cluster: true,
                    clusterMaxZoom: 18, // Max zoom to cluster points on
                    clusterRadius: 50 // Radius of each cluster when clustering points (defaults to 50)
                }

            },
            layers: [
                ...baseMapStyleJSON,
                ...dataCrimesStyleJSON,
                ...data311sStyleJSON
            ]

        };

        console.log("NEW MAP");
        const _map = new maplibregl.Map({
            container: 'map',
            //center: [-97.7171, 30.2944], // starting position [lng, lat]
            center: props.position.center,
            zoom: 12, // starting zoom
            style
        });

        //_map.showTileBoundaries = true;

        _map.on('load', async () => {
            console.log("Load");
        });


        for (const dataset of ['datacrime', 'data311']) {

            // When a click event occurs on a feature in
            // the unclustered-point layer, open a popup at
            // the location of the feature, with
            // description HTML from its properties.

            _map.on('click', `unclustered-point-${dataset}`, (e) => {
                console.log("CLICK unclustered");

                const layer = e.features[0].layer;
                const circleLayerID = layer.id.includes("311") ?
                                      "circle-data311-layer" : "circle-datacrime-layer";

                const coordinates = e.features[0].geometry.coordinates.slice();
                const reportCategory = e.features[0].properties.reportCategory;
                const reportNum = e.features[0].properties.reportNum;

                // Ensure that if the map is zoomed out such that
                // multiple copies of the feature are visible, the
                // popup appears over the copy being pointed to.
                while (Math.abs(e.lngLat.lng - coordinates[0]) > 180) {
                    coordinates[0] += e.lngLat.lng > coordinates[0] ? 360 : -360;
                }

                //increae icon size
                _map.setLayoutProperty(layer.id, 'text-size',
                    [
                        'match',
                        ['get', 'reportNum'], reportNum, // get the feature id
                        30, //new text-size
                        18  //default - needs to be constant not layer reference (since it will change here)
                    ]
                )

                //increase background circle radius
                _map.setPaintProperty(circleLayerID, 'circle-radius',
                    [
                        'match',
                        ['get', 'reportNum'], reportNum,
                        24, //new radius
                        16  //default
                    ]
                )

                new maplibregl.Popup()
                    .setLngLat(coordinates)
                    .setHTML(
                        `${reportCategory}`
                    )
                    .addTo(_map);

                props.setActiveReportNum(reportNum);
                props.setActiveFeatureList({});
            });

            //click on a clustered point
            _map.on('click', `clusters-${dataset}`, async (e) => {
                console.log("CLICK Cluster", e);

                const source = e.features[0].source;
                const cluster_id = e.features[0].properties.cluster_id;
                const coordinates = e.features[0].geometry.coordinates;
                const point_count = e.features[0].properties.point_count;

                const clusterSource = _map.getSource(source);

                //TODO: consistent layer id /source name
                const zoom = await _map.getSource(`${dataset}s`).getClusterExpansionZoom(cluster_id);
                _map.easeTo({
                    center: coordinates,
                    zoom
                });


                //1. get list of individual elements in cluster (ids)
                //2. set open

                const features = await clusterSource.getClusterLeaves(cluster_id, point_count, 0);
                console.log("FE", features);
                const featureList = {
                    source,
                    features
                }

                props.setActiveFeatureList(featureList);
            });

        }


        /*
         * "general" click handler
         * this is clicking anywhere not a "point" (cluster / uncluster)
         * so anywhere that's not base source protomaps
         * used to clear displays like feature list
         */
        _map.on('click', (e) => {

            const features = _map.queryRenderedFeatures(e.point)
                                 .filter(f => f.source != "protomaps");

            console.log("GEN CLICK", features);

            //TODO: refactor this once default values figured out

            if (features.length) {
                if (features[0].source.includes("311")) {
                    _map.setLayoutProperty('unclustered-point-datacrime', 'text-size', 18)
                    _map.setPaintProperty('circle-datacrime-layer', 'circle-radius', 16);
                } else {
                    _map.setLayoutProperty('unclustered-point-data311', 'text-size', 18)
                    _map.setPaintProperty('circle-data311-layer', 'circle-radius', 16);
                }
            }

            //clear all
            if (features.length === 0) {

                //clear, turn off anything in previous click handlers
                _map.setLayoutProperty('unclustered-point-datacrime', 'text-size', 18)
                _map.setLayoutProperty('unclustered-point-data311', 'text-size', 18)

                _map.setPaintProperty('circle-datacrime-layer', 'circle-radius', 16);
                _map.setPaintProperty('circle-data311-layer', 'circle-radius', 16);


                props.setActiveFeatureList({});

            }

        })


        _map.on('moveend', async () => {

            const position = {
                zoom: _map.getZoom(),
                center: _map.getCenter(),
                bounds: _map.getBounds()
            };

            props.setPosition(position);
        })

        _map.addControl(new maplibregl.NavigationControl(), 'bottom-right');

        //mapRef.current = _map;
        setMap(_map);

        return (() => {

            if (_map) {
                console.log("[useMap] Remove")
                _map.remove();
                //maplibregl.removeProtocol("pmtiles");
            }
        });


    }, [props.isDataLoaded])

    return map;
}
