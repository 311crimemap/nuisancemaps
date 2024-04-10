import { useState, useEffect } from "react";
import maplibregl from "maplibre-gl";

import baseMapStyleJSON from "../../assets/baseMapStyle.json";
import dataCrimesStyleJSON from "../../assets/datacrimes_style.json";
import data311sStyleJSON from "../../assets/data311s_style.json";
import AssetLoader from "./AssetLoader";

import Spiderfy from '@nazka/map-gl-js-spiderfy';

export default function useMap(props) {

    //const mapRef = useRef<maplibregl.Map>();
    const [map, setMap] = useState(null);
    const assetLoader = new AssetLoader();

    useEffect(() => {
        console.log("[useMap] Hook Init")

        if (!props.isDataLoaded) return; //NB: wait until data fetched before creating map

        const style = {
            glyphs: "https://protomaps.github.io/basemaps-assets/fonts/{fontstack}/{range}.pbf",
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
                    clusterMaxZoom: 19, // Max zoom to cluster points on
                    clusterRadius: 50 // Radius of each cluster when clustering points (defaults to 50)
                },

                data311s: {
                    type: "geojson",
                    data: props.data311s,
                    cluster: true,
                    clusterMaxZoom: 19, // Max zoom to cluster points on
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

            await assetLoader.init(_map);

            var spiderfyCrime = new Spiderfy(_map, {
                onLeafClick: (f, e) => {

                    //console.log("Feature", f)
                    console.log("Element", e)
                    const features = _map.queryRenderedFeatures(e.point);
                    const sources = _map.getStyle().sources;
                    console.log("Features", features)

                    const leaf = features.find(f => f.layer.id.includes(`spiderfy-leaf`));

                    if (leaf) {
                        console.log("LEAF", leaf);
                        console.log("SOURCES", sources);
                        console.log("THIS", this, spiderfyCrime);

                        //TODO: call setClickedID/setActiveID(leaf.properties['reportNum'])
                        //to trigger panel format, detail view parallel to map handlers

                        //active leaf
                        _map.setLayoutProperty(leaf.layer.id, 'icon-image',
                            [
                                'match',
                                ['get', 'reportNum'], // get the feature id
                                leaf.properties['reportNum'],
                                'robbery', //image when id is the clicked feature id
                                leaf.properties['icon-category']  //default
                            ]
                        )

                        //inactive leaves layers
                        const layers = Object.keys(sources);
                        const inActiveLeafIds = layers.filter(l => l.includes('spiderfy-leaf') &&
                            l != leaf.layer.id);
                        console.log("InActiveLEafIds", inActiveLeafIds);
                        for (const layerID of inActiveLeafIds) {
                            const origFeature = sources[layerID].data.features[0];
                            const origIcon = origFeature.properties['icon-category'];
                            _map.setLayoutProperty(layerID, 'icon-image', origIcon);

                        }

                    }



                    //const coordinates = f.geometry.coordinates.slice();
                    const coordinates = [e.lngLat.lng, e.lngLat.lat, 0]; //cursor click
                    const reportCategory = f.properties.reportCategory;
                    const reportNum = f.properties.reportNum;
                    //while (Math.abs(e.lngLat.lng - coordinates[0]) > 180) {
                    //     coordinates[0] += e.lngLat.lng > coordinates[0] ? 360 : -360;
                    // }

                    //console.log("adjcoords", coordinates);
                    new maplibregl.Popup()
                        .setLngLat(coordinates)
                        .setHTML(
                            `${reportCategory}`
                        )
                        .addTo(_map);

                    //setActiveReportNum
                    props.setActiveReportNum(reportNum);

                    console.log(f)
                },

                closeOnLeafClick: false,
                //clustered can't be styled into distinct unclustered - all the "same" except location
                //spiderLeavesPaint: {},
                spiderLeavesLayout: {
                    "icon-image": ["get", "icon-category"],
                },
                minZoomLevel: props.spiderZoomLevel,
                zoomIncrement: 2,

            });

            //apply to layerID
            spiderfyCrime.applyTo('clusters-datacrime');


            //need separate spider per source, or it can trigger auto close
            var spiderfy311 = new Spiderfy(_map, {
                onLeafClick: (f, e) => {

                    console.log("E", e)
                    //const coordinates = f.geometry.coordinates.slice();
                    const coordinates = [e.lngLat.lng, e.lngLat.lat, 0]; //cursor click
                    const reportCategory = f.properties.reportCategory;
                    const reportNum = f.properties.reportNum;

                    //while (Math.abs(e.lngLat.lng - coordinates[0]) > 180) {
                    //     coordinates[0] += e.lngLat.lng > coordinates[0] ? 360 : -360;
                    // }

                    //console.log("adjcoords", coordinates);
                    new maplibregl.Popup()
                        .setLngLat(coordinates)
                        .setHTML(
                            `${reportCategory}`
                        )
                        .addTo(_map);

                    props.setActiveReportNum(reportNum);

                    console.log(f)
                },

                closeOnLeafClick: false,
                //clustered can't be styled into distinct unclustered - all the "same" except location
                //spiderLeavesPaint: {},
                minZoomLevel: props.spiderZoomLevel,
                zoomIncrement: 2,

            });


            spiderfy311.applyTo('clusters-data311');


        });


        for (const dataset of ['datacrime', 'data311']) {

            // When a click event occurs on a feature in
            // the unclustered-point layer, open a popup at
            // the location of the feature, with
            // description HTML from its properties.

            _map.on('click', `unclustered-point-${dataset}`, (e) => {

                const coordinates = e.features[0].geometry.coordinates.slice();
                const reportCategory = e.features[0].properties.reportCategory;
                const reportNum = e.features[0].properties.reportNum;

                // Ensure that if the map is zoomed out such that
                // multiple copies of the feature are visible, the
                // popup appears over the copy being pointed to.
                while (Math.abs(e.lngLat.lng - coordinates[0]) > 180) {
                    coordinates[0] += e.lngLat.lng > coordinates[0] ? 360 : -360;
                }

                new maplibregl.Popup()
                    .setLngLat(coordinates)
                    .setHTML(
                        `${reportCategory}`
                    )
                    .addTo(_map);

                props.setActiveReportNum(reportNum);
                props.setActiveSpiderList({});
            });

            //click on a clustered point
            _map.on('click', `clusters-${dataset}`, async (e) => {

                const source = e.features[0].source;
                const cluster_id = e.features[0].properties.cluster_id;
                const point_count = e.features[0].properties.point_count;

                const clusterSource = _map.getSource(source);

                const features = await clusterSource.getClusterLeaves(cluster_id, point_count, 0);

                //1. get list of individual elements in cluster (ids)
                //2. set open
                const spiderListFeatures = {
                    source,
                    features
                }

                props.setActiveSpiderList(spiderListFeatures);
            });

        }


        /*
         * "general" click handler
         * this is clicking anywhere not a "point" (cluster / uncluster)
         * so anywhere that's not base source protomaps
         * used to clear displays like spider list
         */
        _map.on('click', (e) => {
            const features = _map.queryRenderedFeatures(e.point)
                                 .filter(f => f.source != "protomaps");

            if (features.length === 0) {
                //clear
                props.setActiveSpiderList({});
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
