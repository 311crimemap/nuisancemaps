/*
 * TODO:
 * toggle visible and pass cluster elements when cluster is clicked
 * make sure to hook into close event
 */

import {useState, useEffect} from "react";

export default function FeatureListComponent({ map, activeFeatureList, featureZoomLevel }) {

    const [isVisible, setIsVisible] = useState(false);

    const { source, features } = activeFeatureList;

    useEffect( ()=> {

        //1. unclustered feature  at any zoom -> show
        //2. random click, or cluster at high zoom (large clusters)
        //    don't toggle, allow default behavior to zoom in
        //3. else it's a cluster at acceptable zoom -> show

        if (features && features.length == 1) {
            setIsVisible(true);
        } else if (!features || map.getZoom() < featureZoomLevel) {
            setIsVisible(false);
        } else {
            setIsVisible(true);
        }
    }, [features]);


    const style = {
        display: isVisible ? 'block' : 'none'
    }

    return (
        <div id="feature-list-component" style={style}>
            <ul>
                {
                    (features || []).map(feature => {
                        return (
                            <li key={feature.properties.reportNum}>
                                {feature.properties.reportCategory}
                            </li>
                        )
                    })
                }
            </ul>
        </div>
    )
}
