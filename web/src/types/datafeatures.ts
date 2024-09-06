
export interface DataGeometry {
    type: "Point";
    coordinates: [number, number, number];
}

export interface DataCategory {
    dataType: "crime" | "311";
    iconName: string;
    iconUnicode: string;
    id: number;
    label: number | null;
    text: string
}

export interface DataFeatureProperties {
    address: string | null;
    category: DataCategory | string;  //this sometimes needs to be parsed (maplibre issue)
    location: string | null,
    reportCategory: string;
    reportNum: string;
    reportedAt: string | null;

}

export interface DataFeature {
    type: "Feature";
    properties: DataFeatureProperties;
    geometry: DataGeometry
}
