import { useState, useEffect, useReducer } from "react";
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

  const [position, setPosition] = useState({
    center: {
      lat: 30.2944,
      lng: -97.7171,
    },
    bounds: {
      _sw: { lat: null, lng: null },
      _ne: { lat: null, lng: null },
    },
  });

  const defaultData = {
    type: "FeatureCollection",
    features: [],
  };

  const defaultDateRange = {
    date: {
      startDate: new Date("01-01-2024").toLocaleDateString("en-CA"),
      endDate: new Date().toLocaleDateString("en-CA"),
    },
  };

  const [dataCrimes, setDataCrimes] = useState(defaultData);
  const [data311s, setData311s] = useState(defaultData);
  const [isDataLoaded, setIsDataLoaded] = useState(false);
  const [activeReportNum, setActiveReportNum] = useState(null);
  const [activeFeatureList, setActiveFeatureList] = useState([]);
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
    Data311s = "data311s",
    DataCrimes = "dataCrimes",
    HeatMapDataCrimes = "heatMapDataCrimes",
    HeatMapData311s = "heatMapData311s",
  }

  const dataSources = {
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

  useEffect(() => {
    const limit = 500;

    const { lat, lng } = { ...position.center };
    const { _sw, _ne } = { ...position.bounds };

    const params = new URLSearchParams({
      startDate: filterDate.date.startDate,
      endDate: filterDate.date.endDate,
      lat,
      lng,
      sw_lat: _sw.lat,
      sw_lng: _sw.lng,
      ne_lat: _ne.lat,
      ne_lng: _ne.lng,
      limit,
    });

    const dataCrimesURL = `http://localhost:8080/datacrimes.geojson?${params.toString()}`;
    const data311sURL = `http://localhost:8080/data311s.geojson?${params.toString()}`;
    const categoriesURL = `http://localhost:8080/categories`;
    console.log("FETCH", dataCrimesURL, data311sURL, categoriesURL);

    Promise.all([
      getData(dataCrimesURL),
      getData(data311sURL),
      getData(categoriesURL),
    ]).then(([dataCrimes, data311s, categories]) => {
      setDataCrimes(dataCrimes);
      setData311s(data311s);
      setCategories(categories.data);

      activeCategoriesDispatcher({
        type: "init",
        categories: Categories.buildHierarchy(categories.data),
      });

      setIsDataLoaded(true);
    });

    //to make new request
    //position.center - too sensitive, even zoom will trigger
  }, [position.center, filterDate.date.startDate, filterDate.date.endDate]);

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
    isDataLoaded,
  });

  console.log("[App] Render", position, activeReportNum);
  console.log(
    "MapController",
    mapController,
    position.center,
    position.zoom,
    position.bounds
  );

  return (
    <>
      <ControlBar
        map={map}
        mapController={mapController}
        DATASOURCES={DATASOURCES}
        setActiveFeatureList={setActiveFeatureList}
        activeCategories={activeCategories}
        activeCategoriesDispatcher={activeCategoriesDispatcher}
        filterDate={filterDate}
        filterDateDispatcher={filterDateDispatcher}
      />

      <div id="container">
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
