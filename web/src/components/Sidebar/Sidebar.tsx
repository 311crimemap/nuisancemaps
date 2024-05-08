import { useState, useEffect } from "react";

import { View } from "./View";
import SidebarNav from "./SidebarNav";
import IncidentView from "./IncidentView";
import FiltersView from "./FiltersView";

import useMapVisiblePointsSync from "./useMapVisiblePointsSync";

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

    const categoriesURL = `http://localhost:8080/categories`;

    const [view, setView] = useState(View.INCIDENTS);

    const [sidebarDisplayCrimes, setSidebarDisplayCrimes] = useState([]);
    const [sidebarDisplay311s, setSidebarDisplay311s] = useState([]);

    const [categories, setCategories] = useState([]);

    const initLoadCategoriesAPI = async (url: String) => {
        return fetch(url).then(res => res.json());
    }

    //sets map.onMove listener to update visible data in incidents panel
    //given what's visible on map
    useMapVisiblePointsSync({
        map,
        dataCrimeClusters, data311Clusters,
        setSidebarDisplayCrimes, setSidebarDisplay311s,
    })

    // initial load of categories list
    useEffect(() => {
        initLoadCategoriesAPI(categoriesURL).then((res) => {
            if (res.status == "success") {
                const _categories = (res.data).map((category) => {
                    category.checked = true;
                    return category;
                });

                //build Hierarchy

                const parentCrime = {
                    dataType: "crime",
                    id: "crime",
                    text: "DataCrime",
                    label: null,
                    parent: null,
                    checked: true
                }


                const parent311 = {
                    dataType: "311",
                    id: "311",
                    text: "Data 311",
                    label: null,
                    parent: null,
                    checked: true
                }


                //refactor this - set as funtion?
                for (let category of _categories) {
                    let c = category;

                    while (c) {

                        if (c.parent === null) {

                            if (c.dataType == "crime") {
                                c.parent = parentCrime;
                                break;
                            }
                            if (c.dataType == "311") {
                                c.parent = parent311;
                                break;
                            }
                        }

                        c = c.parent;
                    }
                }

                _categories.push(parentCrime, parent311);

                setCategories(_categories);
            }
        });
    }, []);

    console.log("[Sidebar] Render", categories);

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
                    categories={categories}
                />
            }

        </div>
    );
}
