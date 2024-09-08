export enum DATASOURCES {
    Sources = "sources",
    Data311s = "data311s",
    DataCrimes = "dataCrimes",
    HeatMapDataCrimes = "heatMapDataCrimes",
    HeatMapData311s = "heatMapData311s",
}

export interface GeoJSONSource {
    type: "geojson";
    data: any;
    cluster?: boolean;
    clusterMaxZoom?: number;
    clusterRadius?: number;
}

export interface DataSourcesMap {
    [DATASOURCES.Sources]: GeoJSONSource;
    [DATASOURCES.Data311s]: GeoJSONSource;
    [DATASOURCES.DataCrimes]: GeoJSONSource;
    [DATASOURCES.HeatMapDataCrimes]: GeoJSONSource;
    [DATASOURCES.HeatMapData311s]: GeoJSONSource;
}

