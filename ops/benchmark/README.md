# Benchmark

* `generate_urls.py`: creates unique urls, outputs to .txt.

*  `siege -f urls.txt -c 5 -i -r 1`
   * c: num concurrent
   * t: length of test
   * r: num repeat visits
   * i: internet mode (random pluck from list)

