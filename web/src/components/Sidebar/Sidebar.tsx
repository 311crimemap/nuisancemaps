import { useState, useEffect } from "react";

import { View } from "./View";
import SidebarNav from "./SidebarNav";
import IncidentView from "./IncidentView";
import FiltersView from "./FiltersView";

import useMapVisiblePointsSync from "./useMapVisiblePointsSync";

export default function Sidebar({ map, activeReportNum, setActiveReportNum, activeCategories, activeCategoriesDispatcher }) {

    //TODO: lift cluster declarations up
    const dataCrimeClusters = [
        "clusters-datacrime",
        "cluster-count-datacrime",
        "unclustered-point-datacrime",
    ];

    const data311Clusters = [
        "clusters-data311",
        "cluster-count-data311",
        "unclustered-point-data311",
    ];


    const [view, setView] = useState(View.INCIDENTS);

    const [sidebarDisplayCrimes, setSidebarDisplayCrimes] = useState([]);
    const [sidebarDisplay311s, setSidebarDisplay311s] = useState([]);


    //sets map.onMove listener to update visible data in incidents panel
    //given what's visible on map
    useMapVisiblePointsSync({
        map,
        dataCrimeClusters, data311Clusters,
        setSidebarDisplayCrimes, setSidebarDisplay311s,
    })


    //console.log("[Sidebar] Render", categories);

    return (
        <div id="sidebar">

            <SidebarNav view={view} setView={setView} />

            {view == View.INCIDENTS &&
                <IncidentView map={map}
                    visibleCrimes={sidebarDisplayCrimes} visible311s={sidebarDisplay311s}
                    activeReportNum={activeReportNum} setActiveReportNum={setActiveReportNum} />
            }

            {view == View.FILTERS &&
                <FiltersView map={map}
                    dataCrimeClusters={dataCrimeClusters} data311Clusters={data311Clusters}
                    activeCategories={activeCategories} activeCategoriesDispatcher={activeCategoriesDispatcher}
                />
            }

        </div>
    );
}
