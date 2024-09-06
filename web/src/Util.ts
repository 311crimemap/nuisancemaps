import { LngLat, LngLatBounds } from "maplibre-gl";

export async function getData(url: string): Promise<any> {
    return fetch(url).then((res) => res.json());
}


export function slugify(text: string): string {
    return text.replaceAll(/\s+/g, "-").toLowerCase();
}

// precision: number of decimal places to floor/ciel
export function calcMaxLatLngBounds(bounds, zoom) {
    let precision = 0;
    if (zoom >= 12) {
        precision = 1
    }
    if (zoom >= 13) {
        precision = 2
    }

    const factor = 10 ** precision;


    const newMaxBounds = new LngLatBounds(
        new LngLat(
            Math.floor(factor * bounds.getSouthWest().lng) / factor,
            Math.floor(factor * bounds.getSouthWest().lat) / factor
        ),
        new LngLat(
            Math.ceil(factor * bounds.getNorthEast().lng) / factor,
            Math.ceil(factor * bounds.getNorthEast().lat) / factor
        )
    );

    console.log("[calcMaxLatLngBounds]", zoom, precision, bounds, newMaxBounds)
    return newMaxBounds;
}
