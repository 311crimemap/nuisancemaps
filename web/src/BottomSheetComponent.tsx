import { useState, useEffect } from "react";
import {debounce} from 'lodash';


export default function BottomSheetComponent({map}) {
    if (!map) return null;

    const [visibleCrimes, setVisibleCrimes] = useState([]);
    const [visible311s, setVisible311s] = useState([]);

    const dataCrimeClusters = [
        "clusters-datacrime",
        //"cluster-count-datacrime",
        "unclustered-point-datacrime",
    ];

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

    console.log("RenderBase", visibleCrimes);

    useEffect(() => {
        console.log("RenderBase hook");

        if (!map) return;


        const moveendHandler = async () => {
            const clusterSource = map.getSource('datacrimes');
            if (!clusterSource) return;

            console.log("ZOOM", map.getZoom());
            console.log("CENTER", map.getCenter());
            console.log("BOUNDS", map.getBounds());

            /*
            const dataCrimes = map.querySourceFeatures("datacrimes",
                                                       {filter: ["==", "category", "FAMILY DISTURBANCE"]});
            console.log("DC", dataCrimes);
            //clusterSource.setData(dataCrimes);
            */

            //layer filter (cluster, number, uncluster)
            //but category doesn't apply to cluster
            //map.setFilter('unclustered-point-datacrime', ['==', 'category', 'FAMILY DISTURBANCE']);

            //queries on visible in window
            const features = map.queryRenderedFeatures({
                layers: dataCrimeClusters,

                //any filter here will only filter unclustered points (clustered don't have the category property)
                //filter: ["==", "category", "FAMILY DISTURBANCE"]
            });

            //const visibility = map.getLayoutProperty(dataCrimeClusters, "visibility");

            //TODO: 1. add unclustered points
            //TODO: 2. move this logic into a custom hook triggered at parent.
            //TODO: trigger visibility, and set via parent
            /*
            if (visibility == "none") {
                setVisibleCrimes([]);
                return;
            }
            */

            console.log("FEATURES", features);  //the visible clusters
            console.log("FEATLEN", features.length);

            //features are mix of clusters and unclustered points
            //clusters need to be deliberately unpacked (getClusterLeaves()) to get individual features
            //unclustered points need to be collected
            //each type is slightly different (different property with getUniqueFeatures)
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
            setVisibleCrimes(res);

        };

        map.on('moveend', debounce(moveendHandler, 350));
    }, [map]);

    return (
        <div id="bottomsheet">
            <ul>
                {visibleCrimes.map(crime => {

                    const location = crime.properties.location

                    return <li>{crime.properties.reportNum} | {location} | {crime.properties.category}</li>
                })
                }
            </ul>
        </div>
    )
}
