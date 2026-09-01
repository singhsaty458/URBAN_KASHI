const CACHE_NAME = 'uk-pos-cache-v1';
const urlsToCache = [
  '/',
  '/pos',
  '/css/store-style.css',
  '/css/pos-style.css',
  '/js/storefront.js',
  '/js/barcode-scanner.js',
  '/manifest.json'
];

self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(urlsToCache))
  );
});

self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request)
      .then(response => {
        if (response) {
          return response;
        }
        return fetch(event.request);
      })
  );
});
