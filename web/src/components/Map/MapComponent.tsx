import { useState, useEffect, useMemo, useRef } from 'react'
import "maplibre-gl/dist/maplibre-gl.css";

export default function MapComponent(props: any) {

    useEffect(() => {

        if (!props.map) return;

        try {
            const dataCrimesSource = props.map.getSource('datacrimes');
            const data311sSource = props.map.getSource('data311s');

            dataCrimesSource.setData(props.dataCrimes);
            data311sSource.setData(props.data311s);

            setIconCategory(props.dataCrimes);
            //TODO: - need to decide icons
            setIconCategory311(props.data311s);

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

    const setIconCategory311 = (data:any) => {
        for (const feature of data.features) {

            //default
            if (!feature.properties['icon-category']) {
                feature.properties['icon-category'] = "cluster-sdf"
            }
        }
    }

    return (
        <div id='map'></div>
    )
}
