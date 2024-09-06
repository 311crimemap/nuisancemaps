import { DataFeature } from "./datafeatures";

export interface ActiveFeatures {
    source: "dataCrimes" | "data311s";
    features: DataFeature[],
    clusterExpansionZoom: number,
    clusterMaxZoom: number
}
