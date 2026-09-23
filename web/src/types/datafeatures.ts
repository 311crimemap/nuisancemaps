
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
    dateMatch?: "within_range" | "older_context";
    olderCount?: number;
}

export interface DataFeature {
    type: "Feature";
    properties: DataFeatureProperties;
    geometry: DataGeometry
}

export interface DataFeatureCollection {
    type: "FeatureCollection";
    features: DataFeature[]
    isDefaultData?: boolean;
}

export const defaultData: DataFeatureCollection = {
    type: "FeatureCollection",
    features: [
        {
            type: "Feature",
            properties: {
                address: null,
                category: "",
                location: null,
                reportCategory: "",
                reportNum: "",
                reportedAt: null,
                dateMatch: "within_range",
                olderCount: 0,
            },
            geometry: {
                type: "Point",
                coordinates: [0, 0, 0],
            },
        },
    ],
    isDefaultData: true,
};
