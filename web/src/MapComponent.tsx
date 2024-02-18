import { useState, useEffect, useRef } from 'react'
import maplibregl from "maplibre-gl";
import "maplibre-gl/dist/maplibre-gl.css";
import styleJSON from "./style.json";

export default function MapComponent(props: any) {

    //const mapRef = useRef<maplibregl.Map>();
    const [map, setMap] = useState();

    useEffect( () => {

        const style = {
            glyphs: "https://protomaps.github.io/basemaps-assets/fonts/{fontstack}/{range}.pbf",
            version: 8,
            sources: {
                protomaps : {
                    type: "vector",
                    url: "https://api.protomaps.com/tiles/v3.json?key=a8e24b8d978e4df3",
                    attribution: '© <a href="https://openstreetmap.org">OpenStreetMap</a> | <a href="https://protomaps.com/">Protomaps',
                    minzoom: 2,
                    maxzoom: 12,
                }
            },
            ...styleJSON
        };

        const _map = new maplibregl.Map({
            container: 'map',
            center: [-97.7171, 30.2944], // starting position [lng, lat]
            zoom: 12, // starting zoom
            style
        });

        //_map.showTileBoundaries = true;
        setMap(_map);

        //mapRef.current = _map;

        return(() => {

            if (map) {
                map.remove();
                maplibregl.removeProtocol("pmtiles");
            }
        });

    }, [])


    /*
     *  MARKER
     */
    if(map) {

        props.dataCrimes.forEach( data => {
            // create the popup
            const popup = new maplibregl.Popup({ offset: 25 }).setHTML(
                `<h3>${data.category}</h3>`
            );

            const el = document.createElement('div');
            el.className = 'marker';
            el.style.backgroundImage =
                `url(https://placekitten.com/g/50/50/)`;
            el.style.width = `50px`;
            el.style.height = `50px`;

            new maplibregl.Marker({ element: el })
                .setLngLat({lng: data.longitude, lat: data.latitude})
                .setPopup(popup)
                .addTo(map);


        });


        console.log("Crimes", props.dataCrimes);
        console.log("311", props.data311s);
    }



    return (
        <div id='map' style={{width: '100vw', height: '100vh'}}></div>
    )
}
