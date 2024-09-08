import { LngLat, LngLatBounds } from "maplibre-gl";

export interface MapPosition {
    zoom: number;
    center: LngLat,
    bounds: LngLatBounds | null,
    fetchBounds: LngLatBounds | null,
    refresh: number,
}


// default position
export const defaultMapPosition: MapPosition = {
    zoom: 3.25,
    center: new LngLat(-95.0173, 38.345),
    bounds: null,
    fetchBounds: null,
    refresh: 0,
};
