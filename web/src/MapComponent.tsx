import { useState, useEffect, useMemo, useRef } from 'react'
import maplibregl from "maplibre-gl";
import "maplibre-gl/dist/maplibre-gl.css";
import baseMapStyleJSON from "./assets/baseMapStyle.json";
import dataCrimesStyleJSON from "./assets/datacrimes_style.json";
import data311sStyleJSON from "./assets/data311s_style.json";

export default function MapComponent(props: any) {

    //const mapRef = useRef<maplibregl.Map>();
    //const map = props.map;
    //const setMap = props.setMap;

    //const [map, setMap] = useState(null);

    useEffect(() => {
        console.log("MapComponent Hook")
        //if (map) return;

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
                    clusterMaxZoom: 14, // Max zoom to cluster points on
                    clusterRadius: 50 // Radius of each cluster when clustering points (defaults to 50)
                },

                data311s: {
                    type: "geojson",
                    data: props.data311s,
                    cluster: true,
                    clusterMaxZoom: 14, // Max zoom to cluster points on
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

        props.setMap(_map);
        //setMap(_map);


        //const _map = );

        //_map.showTileBoundaries = true;



        _map.on('load', () => {

            for (const dataset of ['datacrime', 'data311']) {

                // inspect a cluster on click
                _map.on('click', `clusters-${dataset}`, async (e) => {
                    const features = _map.queryRenderedFeatures(e.point, {
                        layers: [`clusters-${dataset}`]
                    });

                    const clusterId = features[0].properties.cluster_id;
                    const source = features[0].source;

                    const zoom = await _map.getSource(source).getClusterExpansionZoom(clusterId);
                    _map.easeTo({
                        center: features[0].geometry.coordinates,
                        zoom
                    });
                });

                // When a click event occurs on a feature in
                // the unclustered-point layer, open a popup at
                // the location of the feature, with
                // description HTML from its properties.
                _map.on('click', `unclustered-point-${dataset}`, (e) => {
                    const coordinates = e.features[0].geometry.coordinates.slice();
                    const category = e.features[0].properties.category;

                    // Ensure that if the map is zoomed out such that
                    // multiple copies of the feature are visible, the
                    // popup appears over the copy being pointed to.
                    while (Math.abs(e.lngLat.lng - coordinates[0]) > 180) {
                        coordinates[0] += e.lngLat.lng > coordinates[0] ? 360 : -360;
                    }

                    new maplibregl.Popup()
                        .setLngLat(coordinates)
                        .setHTML(
                            `${category}`
                        )
                        .addTo(_map);
                });
            }

        });



        _map.on('moveend', async () => {
            const position = {
                zoom: _map.getZoom(),
                center: _map.getCenter(),
                bounds: _map.getBounds()
            };

            props.setPosition(position);
        })


        //mapRef.current = _map;
        //props.setMap(_map);


        return (() => {

            if (_map) {
                _map.remove();
                //maplibregl.removeProtocol("pmtiles");
            }
        });


    }, [])


    useEffect(() => {

        if (!props.map) return;

        const dataCrimesSource = props.map.getSource('datacrimes');
        const data311sSource = props.map.getSource('data311s');

        dataCrimesSource.setData(props.dataCrimes);
        data311sSource.setData(props.data311s);

    }, [props.dataCrimes, props.data311s])


    return (
        <div id='map' style={{ width: '75vw', height: '75vh' }}></div>
    )
}

