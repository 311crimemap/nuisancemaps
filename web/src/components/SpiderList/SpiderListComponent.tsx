/*
 * TODO:
 * toggle visible and pass spider cluster elements when spider cluster is clicked
 * make sure to hook into spider close event
 */

import {useState, useEffect} from "react";

export default function SpiderListComponent({ map, activeSpiderList, spiderZoomLevel }) {

    const [isVisible, setIsVisible] = useState(false);

    const { source, features } = activeSpiderList;

    useEffect( ()=> {
        if (!features || map.getZoom() < spiderZoomLevel) {
            setIsVisible(false);
        } else {
            setIsVisible(true);
        }
    }, [features]);


    const style = {
        display: isVisible ? 'block' : 'none'
    }

    return (
        <div id="spider-list-component" style={style}>
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
