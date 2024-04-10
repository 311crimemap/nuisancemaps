import { useState, useEffect } from "react";
import { debounce } from 'lodash';

export default function useMapMoveEndHandler({ map,
    dataCrimeClusters, data311Clusters,
    setVisibleCrimes, setVisible311s,
    dataCrimeCheck, data311Check }) {


    const getUniqueFeatures = (features, comparatorProperty) => {
        const uniqueIds = new Set();
        const uniqueFeatures = [];
        for (const feature of features) {
            const id = feature.properties[comparatorProperty];
            if (id && !uniqueIds.has(id)) {
                uniqueIds.add(id);
                uniqueFeatures.push(feature);
            }
        }
        return uniqueFeatures;
    }

    const moveendHandler = async (source, clusterLayers, setDataFn) => {

        const clusterSource = map.getSource(source);

        if (!clusterSource) return;

        //queries on visible in window
        const features = map.queryRenderedFeatures({
            //layers: [].concat(dataCrimeClusters, data311Clusters),
            layers: clusterLayers

            //any filter here will only filter unclustered points (clustered don't have the reportCategory property)
            //filter: ["==", "reportCategory", "FAMILY DISTURBANCE"]
        });

        //const visibility = map.getLayoutProperty(dataCrimeClusters, "visibility");

        console.log("FEATURES", features);  //the visible clusters

        //features are mix of clusters and unclustered points
        //clusters need to be deliberately unpacked (getClusterLeaves()) to get individual features
        //unclustered points need to be collected
        //each type is slightly different (different property with getUniqueFeatures)
        //NB: cluster-counts are also passed in, but get unique'd out
        const uniqueFeatures = getUniqueFeatures(features, 'cluster_id');
        const unClusteredFeatures = getUniqueFeatures(features, 'reportNum');

        let reports = [];
        for (let feature of uniqueFeatures) {

            //if cluster process, otherwise unclustered point
            if (!!feature.properties.cluster_id) {
                const clusterId = feature.properties.cluster_id;
                const point_count = feature.properties.point_count;
                feature = clusterSource.getClusterLeaves(clusterId, point_count, 0);
            }

            reports.push(feature);
        }

        reports.push(...unClusteredFeatures);


        let res = await Promise.all(reports);
        res = res.flat();
        console.log("RES", res);

        //extracted data andrendered
        //setVisibleCrimes(res);
        setDataFn(res);

    };

    const dataCrimeDebouncedHandler = debounce(moveendHandler, 350);
    const data311DebouncedHandler = debounce(moveendHandler, 350);

    //set initial moveend handler to update dataset
    useEffect(() => {
        console.log("[useMap] hook");

        if (!map) return;

        map.on('moveend', () => dataCrimeDebouncedHandler("datacrimes", dataCrimeClusters, setVisibleCrimes));
        map.on('moveend', () => data311DebouncedHandler("data311s", data311Clusters, setVisible311s));

        return (() => {
            map.off('moveend', dataCrimeDebouncedHandler);
            map.off('moveend', data311DebouncedHandler);
        })
    }, [map]);


    //trigger update on filter (checkbox) change
    //TODO: refactor as add more filters
    useEffect(() => {

        if (!map) return;

        dataCrimeDebouncedHandler("datacrimes", dataCrimeClusters, setVisibleCrimes);
        data311DebouncedHandler("data311s", data311Clusters, setVisible311s);

    }, [dataCrimeCheck, data311Check]);

}
