import { LngLat, LngLatBounds } from "maplibre-gl";
import { Log } from "./Logger";

export async function getData(url: string): Promise<any> {
  return fetch(url).then((res) => res.json());
}

export function slugify(text: string): string {
  return text.replaceAll(/\s+/g, "-").toLowerCase();
}

export function deslugify(text: string | undefined): string {
  if (!text) return "";

  return text
    .split("-")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join(" ");
}

export function dateFormat(dateString: string): string {
  const date = new Date(dateString);

  // extract components
  const day = String(date.getDate()).padStart(2, "0"); // Ensure 2 digits
  const month = String(date.getMonth() + 1).padStart(2, ""); // Ensure 2 digits
  const year = date.getFullYear();

  // format as "1-1-2020"
  return `${month}-${day}-${year}`;
}

// precision: number of decimal places to floor/ciel
export function calcMaxLatLngBounds(
  bounds: LngLatBounds,
  zoom: number
): LngLatBounds {
  let precision = 0;
  if (zoom >= 12) {
    precision = 1;
  }
  if (zoom >= 13) {
    precision = 2;
  }

  const factor: number = 10 ** precision;

  const newMaxBounds: LngLatBounds = new LngLatBounds(
    new LngLat(
      Math.floor(factor * bounds.getSouthWest().lng) / factor,
      Math.floor(factor * bounds.getSouthWest().lat) / factor
    ),
    new LngLat(
      Math.ceil(factor * bounds.getNorthEast().lng) / factor,
      Math.ceil(factor * bounds.getNorthEast().lat) / factor
    )
  );

  Log.log({
    msg: "[calcMaxLatLngBounds]",
    params: { zoom, precision, bounds, newMaxBounds },
    ...Log.data,
  });
  return newMaxBounds;
}
