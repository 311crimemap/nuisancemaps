import { useState, useEffect, useReducer } from "react";
import "./App.css";
import Categories from "./components/Map/categories";
import useMap from "./components/Map/useMap";
import { MapComponent } from "./components/Map";
import { Sidebar } from "./components/Sidebar";
import { SpiderListComponent } from "./components/SpiderList";
import BottomSheetComponent from "./BottomSheetComponent.tsx";
import categoryCheckBoxReducer from "./components/Sidebar/CategoryFilterReducer";
import dateFilterReducer from "./components/Sidebar/DateFilterReducer";

function App() {
    const spiderZoomLevel = 17;

    const [position, setPosition] = useState({
        center: {
            lat: 30.2944,
            lng: -97.7171
        }
    });

    const defaultData = {
        type: "FeatureCollection",
        features: [],
    };

    const defaultDateRange = {
        date: {
            startDate: (new Date()).toLocaleDateString('en-CA'),
            endDate: (new Date()).toLocaleDateString('en-CA')
        }
    }

    const [dataCrimes, setDataCrimes] = useState(defaultData);
    const [data311s, setData311s] = useState(defaultData);
    const [isDataLoaded, setIsDataLoaded] = useState(false);
    const [activeReportNum, setActiveReportNum] = useState(null);
    const [activeSpiderList, setActiveSpiderList] = useState([]);
    const [activeCategories, activeCategoriesDispatcher] = useReducer(categoryCheckBoxReducer, []);
    const [filterDate, filterDateDispatcher] = useReducer(dateFilterReducer, defaultDateRange);


    const getData = async (url: string) => {
        return fetch(url).then((res) => res.json());
    };

    useEffect(() => {

        const limit = 500;
        const {lat, lng} = {...position.center};

        const params = new URLSearchParams({
            startDate: filterDate.date.startDate,
            endDate: filterDate.date.endDate,
            lat, lng,
            limit,
        });

        const dataCrimesURL = `http://localhost:8080/datacrimes.geojson?${params.toString()}`;
        const data311sURL = `http://localhost:8080/data311s.geojson?${params.toString()}`;
        const categoriesURL = `http://localhost:8080/categories`;
        console.log("FETCH", dataCrimesURL, data311sURL, categoriesURL);

        Promise.all([getData(dataCrimesURL), getData(data311sURL), getData(categoriesURL)]).then(
            ([dataCrimes, data311s, categories]) => {
                setDataCrimes(dataCrimes);
                setData311s(data311s);

                activeCategoriesDispatcher({
                    type: "init",
                    categories: Categories.buildHierarchy(categories.data)
                })

                setIsDataLoaded(true);
            }
        );

        //to make new request
        //position.center - too sensitive, even zoom will trigger
    }, [filterDate.date.startDate, filterDate.date.endDate]);

    const map = useMap({
        position,
        setPosition,
        activeReportNum,
        setActiveReportNum,
        dataCrimes,
        data311s,
        setActiveSpiderList,
        spiderZoomLevel,
        isDataLoaded,
    });

    console.log("[App] Render", position, activeReportNum);
    return (
        <>
            <div id="container">
                <Sidebar
                    map={map}
                    activeReportNum={activeReportNum}
                    setActiveReportNum={setActiveReportNum}
                    activeCategories={activeCategories}
                    activeCategoriesDispatcher={activeCategoriesDispatcher}
                    filterDate={filterDate}
                    filterDateDispatcher={filterDateDispatcher}
                />

                <MapComponent
                    map={map}
                    position={position}
                    setPosition={setPosition}
                    activeReportNum={activeReportNum}
                    setActiveReportNum={setActiveReportNum}
                    setActiveSpiderList={setActiveSpiderList}
                    activeCategories={activeCategories}
                    dataCrimes={dataCrimes}
                    data311s={data311s}
                />

                <SpiderListComponent
                    map={map}
                    spiderZoomLevel={spiderZoomLevel}
                    activeSpiderList={activeSpiderList}
                    activeReportNum={activeReportNum}
                    setActiveReportNum={setActiveReportNum}
                />
            </div>

            <BottomSheetComponent map={map} />
        </>
    );
}

export default App;
