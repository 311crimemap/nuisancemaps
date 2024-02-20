import { useState, useEffect } from "react";
import {debounce} from 'lodash';


export default function BaseComponent({map}) {
    if (!map) return null;

    const [visibleCrimes, setVisibleCrimes] = useState([]);
    const [visible311s, setVisible311s] = useState([]);
    const dataCrimeClusters = [
        "clusters-datacrime",
        "cluster-count-datacrime",
        "unclustered-point-datacrime",
    ];

    const getUniqueFeatures = (features, comparatorProperty) => {
        const uniqueIds = new Set();
        const uniqueFeatures = [];
        for (const feature of features) {
            const id = feature.properties[comparatorProperty];
            if (!uniqueIds.has(id)) {
                uniqueIds.add(id);
                uniqueFeatures.push(feature);
            }
        }
        return uniqueFeatures;
    }

    console.log("RenderBase", visibleCrimes);

    useEffect(() => {
        console.log("RenderBase hook");

        //WORKING HERE
        if (!map) return;
        const moveendHandler = async () => {
            const clusterSource = map.getSource('datacrimes');
            if (!clusterSource) return;

            const features = map.queryRenderedFeatures({ layers: dataCrimeClusters });

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

            //WORKING: decide cluster, uncluster approach
            //right now inputs are a mix so not processing corectly
            //should be separate cluster or uncluster?
            console.log("FEATURES", features);  //the visible clusters
            console.log("FEATLEN", features.length);

            const uniqueFeatures = getUniqueFeatures(features, 'cluster_id');
            console.log("UF", uniqueFeatures);

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

            let res = await Promise.all(reports);
            res = res.flat();
            console.log("RES", res);

            //extracted data andrendered
            setVisibleCrimes(res);

        };

        map.on('moveend', debounce(moveendHandler, 350));
    }, [map]);

    return (
        <div id="base">
            <ul>
                {visibleCrimes.map(crime => {
                    return <li>{crime.properties.reportNum} | {crime.properties.category}</li>
                })
                }
            </ul>
        </div>
    )
}
