import { useState, useEffect, useReducer } from "react";
import { LngLat } from "maplibre-gl";
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

  const sw = new LngLat(-97.77095608156225, 30.259261190163073);
  const ne = new LngLat(-97.68701127709589, 30.30730791957427);

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

  const [position, setPosition] = useState({
    center: {
      lat: 30.2944,
      lng: -97.7171,
    },
    bounds: {
      sw,
      ne,
    },
    maxBounds: {
      sw: new LngLat(Math.floor(sw.lng), Math.floor(sw.lat)),
      ne: new LngLat(Math.ceil(ne.lng), Math.ceil(ne.lat)),
    },
    refresh: true,
  });

  const [dataCrimes, setDataCrimes] = useState(defaultData);
  const [data311s, setData311s] = useState(defaultData);
  const [isDataLoaded, setIsDataLoaded] = useState(false);
  const [activeReportNum, setActiveReportNum] = useState(null);
  const [activeFeatureList, setActiveFeatureList] = useState([]);
  const [sources, setSources] = useState([]);
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

  useEffect(() => {
    const limit = 10000;

    const { lat, lng } = { ...position.center };
    const { sw, ne } = { ...position.maxBounds };

    const params = new URLSearchParams({
      startDate: filterDate.date.startDate,
      endDate: filterDate.date.endDate,
      lat,
      lng,
      sw_lat: sw.lat,
      sw_lng: sw.lng,
      ne_lat: ne.lat,
      ne_lng: ne.lng,
      limit,
    });

    const dataCrimesURL = `http://localhost:8080/datacrimes.geojson?${params.toString()}`;
    const data311sURL = `http://localhost:8080/data311s.geojson?${params.toString()}`;
    const initURL = `http://localhost:8080/init`;

    console.log("FETCH", dataCrimesURL, data311sURL, initURL);

    Promise.all([
      getData(dataCrimesURL),
      getData(data311sURL),
      categories.length == 0 ? getData(initURL) : Promise.resolve(categories),
    ]).then(([dataCrimes, data311s, dataSourceCategories]) => {
      setDataCrimes(dataCrimes);
      setData311s(data311s);

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

      setPosition({
        ...position,
        refresh: false,
      });
      setIsDataLoaded(true);
    });

    //to make new request
    //position.center - too sensitive, even zoom will trigger
  }, [position.refresh, filterDate.date.startDate, filterDate.date.endDate]);

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
