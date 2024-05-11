import { useState, useEffect, useMemo, useRef } from 'react'
import "maplibre-gl/dist/maplibre-gl.css";

export default function MapComponent(props: any) {

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


    return (
        <div id='map'></div>
    )
}
