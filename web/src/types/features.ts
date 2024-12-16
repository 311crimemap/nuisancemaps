import { CategoryMinMaxReportedAt } from "./categoryminmaxreportedat";

export interface Geometry {
  type: "Point";
  coordinates: [number, number];
}

export interface FeatureProperties {
  id: number;
  name: string;
  description: string;
  city: string;
  state: string;
  categoryMinMaxReportedAt: CategoryMinMaxReportedAt[],
  attribution: string;
  enabled: boolean;
  location: [number, number];
  iconName: string;
  iconUnicode: string;
}

export interface Feature {
  type: "Feature";
  properties: FeatureProperties;
  geometry: Geometry;
}

export interface FeatureCollection {
  type: "FeatureCollection";
  features: Feature[];
  isDefaultData?: boolean;
}
