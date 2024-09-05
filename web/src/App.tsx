import { useState, useEffect, useReducer } from "react";
import { LngLat, LngLatBounds } from "maplibre-gl";
import "./App.css";
import Categories from "./components/Map/categories";
import useMap from "./components/Map/useMap";
import { ControlBar } from "./components/ControlBar";
import { MapComponent } from "./components/Map";
import { FeatureListComponent } from "./components/FeatureList";
import categoryCheckBoxReducer from "./components/ControlBar/CategoryDropDown/CategoryFilterReducer";
import dateFilterReducer from "./components/ControlBar/DateDropDown/DateFilterReducer";

function App() {
    const featureZoomLevel = 17;

    const defaultData = {
        type: "FeatureCollection",
        features: [
            {
                type: "Feature",
                properties: {},
                geometry: {
                    type: "Point",
                    coordinates: [],
                },
            },
        ],
    };

    const endDate = new Date();
    endDate.setDate(endDate.getDate() - 1);

    const defaultDateRange = {
        date: {
            startDate: new Date("01-01-2024").toLocaleDateString("en-CA"),
            endDate: endDate.toLocaleDateString("en-CA"),
        },
        isBusy: false,
    };

    // default position
    const [position, setPosition] = useState({
        zoom: 3.25,
        center: {
            lat: 38.345,
            lng: -95.0173,
        },
        bounds: null,
        fetchBounds: null,
        refresh: 0,
    });

    const [dataCrimes, setDataCrimes] = useState(defaultData);
    const [data311s, setData311s] = useState(defaultData);
    const [isInitLoaded, setIsInitLoaded] = useState(false);
    const [isDataLoading, setIsDataLoading] = useState(false);
    const [activeReportNum, setActiveReportNum] = useState(null);
    const [activeFeatureList, setActiveFeatureList] = useState([]);
    const [sources, setSources] = useState(defaultData);
    const [categories, setCategories] = useState([]);
    const [activeCategories, activeCategoriesDispatcher] = useReducer(
        categoryCheckBoxReducer,
        []
    );
    const [filterDate, filterDateDispatcher] = useReducer(
        dateFilterReducer,
        defaultDateRange
    );

    enum DATASOURCES {
        Sources = "sources",
        Data311s = "data311s",
        DataCrimes = "dataCrimes",
        HeatMapDataCrimes = "heatMapDataCrimes",
        HeatMapData311s = "heatMapData311s",
    }

    const dataSources = {
        [DATASOURCES.Sources]: {
            type: "geojson",
            data: sources, // zoomed out city points
        },
        [DATASOURCES.Data311s]: {
            type: "geojson",
            data: data311s,
            cluster: true,
            clusterMaxZoom: 18, // Max zoom to cluster points on
            clusterRadius: 50, // Radius of each cluster when clustering points (defaults to 50)
        },
        [DATASOURCES.DataCrimes]: {
            type: "geojson",
            data: dataCrimes,
            cluster: true,
            clusterMaxZoom: 18, // Max zoom to cluster points on
            clusterRadius: 50, // Radius of each cluster when clustering points (defaults to 50)
        },

        //TODO: issue w/ dueling layers - they get overlaid not aggregated (like clusters)
        //so not desirable for heatmaps...
        [DATASOURCES.HeatMapDataCrimes]: {
            type: "geojson",
            data: dataCrimes,
            cluster: false,
        },
        [DATASOURCES.HeatMapData311s]: {
            type: "geojson",
            data: data311s,
            cluster: false,
        },
    };

    const getData = async (url: string) => {
        return fetch(url).then((res) => res.json());
    };

    /*
     * FETCH
     */

    useEffect(() => {
        const initURL = `${import.meta.env.VITE_API_SERVER_URL}/init`;

        Promise.all([
            categories.length == 0 ? getData(initURL) : Promise.resolve(categories),
        ]).then(([dataSourceCategories]) => {
            //preserve any checked filters
            if (categories.length == 0) {
                setCategories(dataSourceCategories.data.categories);
                setSources(dataSourceCategories.data.sources);

                activeCategoriesDispatcher({
                    type: "init",
                    categories: Categories.buildHierarchy(
                        dataSourceCategories.data.categories
                    ),
                });
            }

            filterDateDispatcher({
                type: "isBusy",
                value: false,
            });

            setIsInitLoaded(true);
        });
    }, []);

    // want to initalize map first
    // to populate position obj
    const { map, mapController } = useMap({
        position,
        setPosition,
        activeReportNum,
        setActiveReportNum,
        DATASOURCES,
        dataSources,
        dataCrimes,
        data311s,
        categories,
        setActiveFeatureList,
        featureZoomLevel,
        isInitLoaded,
    });

    useEffect(() => {
        if (!isInitLoaded) return;
        if (!map) return;
        if (map.getZoom() < 10) return;

        filterDateDispatcher({
            type: "isBusy",
            value: true,
        });

        const { lat, lng } = { ...position.center };

        const bounds = map.getBounds();
        const fetchBounds =
            position.fetchBounds ||
            new LngLatBounds(
                new LngLat(
                    Math.floor(bounds.getSouthWest().lng),
                    Math.floor(bounds.getSouthWest().lat)
                ),
                new LngLat(
                    Math.ceil(bounds.getNorthEast().lng),
                    Math.ceil(bounds.getNorthEast().lat)
                )
            );

        // setting position fetchBounds here avoids a duplicate fetch
        // (useMap onMove sets refresh true if no fetchBounds)

        const params = new URLSearchParams({
            startDate: filterDate.date.startDate,
            endDate: filterDate.date.endDate,
            //mute lat/lng to make uri more cacheable
            //lat,
            //lng,
            sw_lat: fetchBounds.getSouthWest().lat,
            sw_lng: fetchBounds.getSouthWest().lng,
            ne_lat: fetchBounds.getNorthEast().lat,
            ne_lng: fetchBounds.getNorthEast().lng,
        });

        const dataCrimesURL = `${import.meta.env.VITE_API_SERVER_URL}/datacrimes.geojson?${params.toString()}`;
        const data311sURL = `${import.meta.env.VITE_API_SERVER_URL}/data311s.geojson?${params.toString()}`;

        setIsDataLoading(true);

        console.log("FETCH DATA", dataCrimesURL, data311sURL);

        Promise.all([getData(dataCrimesURL), getData(data311sURL)]).then(
            ([dataCrimes, data311s]) => {
                setDataCrimes(dataCrimes);
                setData311s(data311s);

                filterDateDispatcher({
                    type: "isBusy",
                    value: false,
                });

                setIsDataLoading(false);
            }
        );
        //to make new request
        //position.center - too sensitive, even zoom will trigger
    }, [
        map,
        isInitLoaded,
        position.refresh,
        filterDate.date.startDate,
        filterDate.date.endDate,
    ]);

    console.log("[App] Render", position, activeReportNum);
    console.log(
        "MapController",
        mapController,
        position.center,
        position.zoom,
        position.bounds
    );
    console.log("Map", map);

    return (
        <>
            <div className="mt-16">
                <ControlBar
                    map={map}
                    mapController={mapController}
                    DATASOURCES={DATASOURCES}
                    setActiveFeatureList={setActiveFeatureList}
                    activeCategories={activeCategories}
                    activeCategoriesDispatcher={activeCategoriesDispatcher}
                    filterDate={filterDate}
                    filterDateDispatcher={filterDateDispatcher}
                    isDataLoading={isDataLoading}
                />
            </div>

            {isDataLoading && (
                <div id="spinner" className="flex flex-col items-center z-10">
                    <span className="loading loading-spinner text-error loading-lg mb-4"></span>
                    <span>Loading</span>
                </div>
            )}

            <div
                id="container"
                className={`${isDataLoading ? "opacity-50" : "opacity-100"}`}
            >
                <MapComponent
                    map={map}
                    position={position}
                    setPosition={setPosition}
                    activeReportNum={activeReportNum}
                    setActiveReportNum={setActiveReportNum}
                    setActiveFeatureList={setActiveFeatureList}
                    activeCategories={activeCategories}
                    DATASOURCES={DATASOURCES}
                    dataCrimes={dataCrimes}
                    data311s={data311s}
                />

                <FeatureListComponent
                    map={map}
                    featureZoomLevel={featureZoomLevel}
                    activeFeatureList={activeFeatureList}
                    activeReportNum={activeReportNum}
                    setActiveReportNum={setActiveReportNum}
                />
            </div>
        </>
    );
}

export default App;
