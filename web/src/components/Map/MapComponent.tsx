import { useState, useEffect, useMemo, useRef } from 'react'
import "maplibre-gl/dist/maplibre-gl.css";

export default function MapComponent(props: any) {

    useEffect(() => {
        if (!props.map) return;

        const dataCrimesSource = props.map.getSource('datacrimes');
        const data311sSource = props.map.getSource('data311s');

        const dataCrimes = props.dataCrimes;
        const data311s = props.data311s;

        setIconCategory(dataCrimes);
        setIconCategory311(data311s);

        dataCrimesSource.setData(dataCrimes);
        data311sSource.setData(data311s);

    }, [props.dataCrimes, props.data311s])


    //given filter checkbox, filter at data level
    //
    //NB: need to do this at data level because clusters do not have any
    //underlying features - they are calculated.
    //so only way to adjust cluster counts is at data level
    useEffect(() => {

        if (!props.map) return;

        try {

            const dataCrimesSource = props.map.getSource('datacrimes');
            const data311sSource = props.map.getSource('data311s');

            const activeCategoriesIds = props.activeCategories.filter(c => c.checked).map(c => c.id);

            const dataCrimes = {
                type: "FeatureCollection",
                features: props.dataCrimes.features
                    .filter(feature => activeCategoriesIds.includes(feature.properties.category.id))
            }

            const data311s = {
                type: "FeatureCollection",
                features: props.data311s.features
                    .filter(feature => activeCategoriesIds.includes(feature.properties.category.id))
            }

            dataCrimesSource.setData(dataCrimes);
            data311sSource.setData(data311s);

        } catch (e) {
            console.error(e)
        }
    }, [props.activeCategories, props.dataCrimes, props.data311s])


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
                if (feature.properties['reportCategory'].toLowerCase().includes(ki.keyword)) {
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

    const setIconCategory311 = (data: any) => {
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
