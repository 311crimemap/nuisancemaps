import { useState, useEffect, useReducer } from "react";
import "./App.css";
import Categories from "./components/Map/categories";
import useMap from "./components/Map/useMap";
import { MapComponent } from "./components/Map";
import { Sidebar } from "./components/Sidebar";
import { SpiderListComponent } from "./components/SpiderList";
import BottomSheetComponent from "./BottomSheetComponent.tsx";
import categoryCheckBoxReducer from "./components/Sidebar/CategoryFilterReducer";

function App() {
    const spiderZoomLevel = 17;

    const [position, setPosition] = useState({
        center: [-97.7171, 30.2944],
    });

    const defaultData = {
        type: "FeatureCollection",
        features: [],
    };

    const [dataCrimes, setDataCrimes] = useState(defaultData);
    const [data311s, setData311s] = useState(defaultData);
    const [isDataLoaded, setIsDataLoaded] = useState(false);
    const [activeReportNum, setActiveReportNum] = useState(null);
    const [activeSpiderList, setActiveSpiderList] = useState([]);
    const [activeCategories, activeCategoriesDispatcher] = useReducer(categoryCheckBoxReducer, []);


    const getData = async (url: string) => {
        return fetch(url).then((res) => res.json());
    };

    useEffect(() => {
        console.log("FETCH");
        const limit = 500;
        const center = position.center;
        const dataCrimesURL = `http://localhost:8080/datacrimes.geojson?center=${center}&limit=${limit}`;
        const data311sURL = `http://localhost:8080/data311s.geojson?center=${center}&limit=${limit}`;
        const categoriesURL = `http://localhost:8080/categories`;

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
    }, []);

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
