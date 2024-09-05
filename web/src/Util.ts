import { LngLat, LngLatBounds } from "maplibre-gl";

export function slugify(text: string): string {
    return text.replaceAll(/\s+/g, "-").toLowerCase();
}

export function calcMaxLatLngBounds(bounds, precision = 1) {

    const newMaxBounds = new LngLatBounds(
        new LngLat(
            Math.floor(bounds.getSouthWest().lng),
            Math.floor(bounds.getSouthWest().lat)
        ),
        new LngLat(
            Math.ceil(bounds.getNorthEast().lng),
            Math.ceil(bounds.getNorthEast().lat)
        )
    );

    return newMaxBounds;
}
