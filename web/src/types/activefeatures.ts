import { DataFeature } from "./datafeatures";
import { MapGeoJSONFeature } from "maplibre-gl";

export interface ActiveFeatures {
  source: string;
  features: DataFeature[] | MapGeoJSONFeature[];
  clusterExpansionZoom?: number;
  clusterMaxZoom?: number;
}

export const defaultActiveFeatures: ActiveFeatures = {
  source: "dataCrimes",
  features: [],
  clusterExpansionZoom: 18,
  clusterMaxZoom: 18,
};
