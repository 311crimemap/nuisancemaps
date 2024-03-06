import { useState } from "react";

import { View } from "./View";
import SidebarNav from "./SidebarNav";
import IncidentView from "./IncidentView";
import FiltersView from "./FiltersView";

import useMapMoveEndHandler from "./useMapMoveEndHandler";

export default function Sidebar({ map, activeReportNum, setActiveReportNum }) {

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

    const [dataCrimeCheck, setDataCrimeCheck] = useState(true);
    const [data311Check, setData311Check] = useState(true);

    const [visibleCrimes, setVisibleCrimes] = useState([]);
    const [visible311s, setVisible311s] = useState([]);

    //sets map.onMove listener to update data in visible window
    useMapMoveEndHandler({
        map,
        dataCrimeClusters, data311Clusters,
        setVisibleCrimes, setVisible311s,
        dataCrimeCheck, data311Check
    })

    console.log("[Sidebar] Render");

    return (
        <div id="sidebar">

            <SidebarNav view={view} setView={setView} />

            {view == View.INCIDENTS &&
                <IncidentView map={map}
                    visibleCrimes={visibleCrimes} visible311s={visible311s}
                    activeReportNum={activeReportNum} setActiveReportNum={setActiveReportNum} />
            }

            {view == View.FILTERS &&
                <FiltersView map={map}
                    dataCrimeCheck={dataCrimeCheck} setDataCrimeCheck={setDataCrimeCheck}
                    data311Check={data311Check} setData311Check={setData311Check}
                    dataCrimeClusters={dataCrimeClusters} data311Clusters={data311Clusters} />
            }

        </div>
    );
}
