## Demo for comparison of OkHttp3 and Cronet
Based on chromium sample and official okhttp example.

### Config options
1. Restful or CDN request. Restful by default.
2. Http1.1 or Http2. Http1.1 by default.
3. Number of concurrent request. 1 by default.

### Something to note
1. Disable http cache by default.
2. Visit https://console.cloud.google.com/storage/browser/chromium-cronet/android to get a newer Cronet.

### Version

okhttp:3.11.0

Cronet:75.0.3770.150

### TODO list
1. Adapter for profiler for both networking library.