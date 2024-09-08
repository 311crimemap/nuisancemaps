import { LngLat, LngLatBounds } from "maplibre-gl";

export interface MapPosition {
    zoom: number;
    center: LngLat,
    bounds: LngLatBounds | null,
    fetchBounds: LngLatBounds | null,
    refresh: number,
}
