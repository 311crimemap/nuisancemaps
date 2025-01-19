# React + TypeScript + Vite

Preview Prod in local dev

```
docker exec -it nuisancemaps_web_1 bash
npm run build
npm run preview
```

---

## Arch

* Map Library: [Maplibre GL JS](https://maplibre.org/maplibre-gl-js/docs/)
* Tilesets: [Protomaps](https://protomaps.com/):
* API: data for rendering
* basemap-assets: serve font awesome glyphs
* Styling via daisyUI / tailwind

## Important Component Overview

* `/web`: static page build and deployed by Cloudflare.
* [./src/App.tsx](`App.tsx`): loads map, controlbar, etc.
* [./src/components/Map/useMap.tsx](`useMap.tsx`): wrapper
  hook for maplibre
* [./src/components/ControlBar](ControlBar component): control bar component;
  drop down, selectors.
* [./src/components/FeatureList](FeatureList): pop up with incident details
* [./pages](Static pages): misc static pages


## Layers

Layers described in [./src/assets/](./src/assets/).

### Initial layer

"Sources" is dataset name.

The icons are the same, but cluster this layer because it looks infinitely nicer
versus overlapping icons.

* `cluster-circle-sources`: clustered circles (yellow background)
* `cluster-sources`: cluster icon (building unicode hard coded)
* `point-circle-sources`: individual source circles
* `point-sources`: individual cluster icon (building via accessor get
  iconUnicode).

### Heatmap Properties

When using clustered data source, heatmap only contains clustered points
(`point_count` property), and ignores non-clustered. But ideally want all points on
the same layer, so aggregation and resulting heatmap is accurate.

Current fix is to duplicate the  datasources with `cluster:false`. Shouldn't be too bad since
heatmap vs detail map are visually exclusive.


* `heatmap-weight`: set to 1 (default)
* `heatmap-intensity`: linear interpolate based on zoom; (zoom, intensity) pairs.
* `heatmap-color`: based on density [0,1], with some example color scheme pasted in.
* `heatmap-radius`: given zoom level, increase the radius of the flare. Smaller
  values mean less blob on zoom out, but on zoom in looks more like colored
  points vs heatmap. Find balance.


## Maplibre Notes

TODO:

* Hierarchy FeatureCollection / Feature, etc. Data format
* Properties: stick things in there's
* interpolation text-size
* clustering and source id via filter
* paint layer and glyph and circles

---


## Web Install Notes

* Bootstrap container from `Dockerfile` needs to have vite installed
* Build container
  * `docker build -t <name/web>`
  * `docker run -v $(pwd):/web -it <name/web>`

* Install vite to generate project configuration and preserve on bind mount
  * `npm create vite@latest <web> --template react-ts`
  * `cd <web>`
  * `npm install`
  * adjust `vite.config.ts` server param to bind to '0.0.0.0'
* Added service `<web>` to `docker-compose.yml`.
