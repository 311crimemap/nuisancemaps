import { useState, useEffect, useMemo, useRef } from 'react'
import maplibregl from "maplibre-gl";
import "maplibre-gl/dist/maplibre-gl.css";

import baseMapStyleJSON from "./assets/baseMapStyle.json";
import dataCrimesStyleJSON from "./assets/datacrimes_style.json";
import data311sStyleJSON from "./assets/data311s_style.json";

import gunSVG from "./assets/gun.svg";
import maskSVG from "./assets/mask.svg";
import robberySVG from "./assets/robbery.svg";

import Spiderfy from '@nazka/map-gl-js-spiderfy';

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

        props.setMap(_map);

        //_map.showTileBoundaries = true;


        _map.on('load', async () => {

            const { data: image } = await _map.loadImage(
                'https://raw.githubusercontent.com/nazka/map-gl-js-spiderfy/dev/demo/img/circle-yellow.png'
            );

            try {

                for (const svg of [{ name: 'robbery', url: robberySVG },
                                   { name: 'mask', url: maskSVG },
                                   { name: 'gun', url: gunSVG }]) {

                    let img = new Image(20, 20)
                    img.onload = () => _map.addImage(svg.name, img)
                    img.src = svg.url;
                }

            } catch (e) {
                console.log("ERR", e);
            }


            //sdf is optimziation for monochromatic png; esp for paint ontop.
            _map.addImage('cluster', image);


            var spiderfyCrime = new Spiderfy(_map, {
                onLeafClick: (f, e) => {
                    console.log("E", e)
                    //const coordinates = f.geometry.coordinates.slice();
                    const coordinates = [e.lngLat.lng, e.lngLat.lat, 0]; //cursor click
                    const category = f.properties.category;

                    //while (Math.abs(e.lngLat.lng - coordinates[0]) > 180) {
                    //     coordinates[0] += e.lngLat.lng > coordinates[0] ? 360 : -360;
                    // }

                    //console.log("adjcoords", coordinates);
                    new maplibregl.Popup()
                        .setLngLat(coordinates)
                        .setHTML(
                            `${category}`
                        )
                        .addTo(_map);

                    console.log(f)
                },

                closeOnLeafClick: false,
                //clustered can't be styled into distinct unclustered - all the "same" except location
                //spiderLeavesPaint: {},
                spiderLeavesLayout: {
                    "icon-image": ["get", "icon-category"],
                },
                minZoomLevel: 17,
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
                    const category = f.properties.category;

                    //while (Math.abs(e.lngLat.lng - coordinates[0]) > 180) {
                    //     coordinates[0] += e.lngLat.lng > coordinates[0] ? 360 : -360;
                    // }

                    //console.log("adjcoords", coordinates);
                    new maplibregl.Popup()
                        .setLngLat(coordinates)
                        .setHTML(
                            `${category}`
                        )
                        .addTo(_map);

                    console.log(f)
                },

                closeOnLeafClick: false,
                //clustered can't be styled into distinct unclustered - all the "same" except location
                //spiderLeavesPaint: {},
                minZoomLevel: 17,
                zoomIncrement: 2,

            });


            spiderfy311.applyTo('clusters-data311');

            for (const dataset of ['datacrime', 'data311']) {

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

        try {
            const dataCrimesSource = props.map.getSource('datacrimes');
            const data311sSource = props.map.getSource('data311s');

            dataCrimesSource.setData(props.dataCrimes);
            data311sSource.setData(props.data311s);

            setIconCategory(props.dataCrimes);
            //TODO: setIconCategory(props.data311s);

        } catch (e) {
            console.error(e)
        }
    }, [props.position.center, props.dataCrimes, props.data311s])


    //TODO: separate class, add to this useMap above init
    //load keywords as json input
    //should have crime and 311 icon sets
    //iconLoader
    //icon mapper: icon name -> file/dimen
    //  calls loadImage
    //load keyword mapping
    //priority by order and unique
    //apply(data)
    const setIconCategory = (data: any) => {

        const keywords = [
            {
                keyword: "theft",
                //filename / dim?
                icon: "robbery",
            },
            {
                keyword: "assault",
                icon: "gun",
            },
            {
                keyword: "criminal",
                icon: "mask",
            },
        ];

        console.time("keyword");
        if (!data.features.length) return;
        for (const feature of data.features) {

            for (const ki of keywords) {
                if (feature.properties.category.toLowerCase().includes(ki.keyword)) {
                    feature.properties['icon-category'] = ki.icon;
                    break;
                }
            }
            //default
            if (!feature.properties['icon-category']) {
                feature.properties['icon-category'] = "cluster"
            }
        }
        console.timeEnd("keyword");

    }

    return (
        <div id='map' style={{ width: '75vw', height: '75vh' }}></div>
    )
}
