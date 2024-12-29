# Layer Notes

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
