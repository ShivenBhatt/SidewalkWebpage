var Progress = Progress || {};

Progress.core = (function Map($, L) {
  var self = {};

  function _init(data) {
    L.mapbox.accessToken = 'pk.eyJ1Ijoia290YXJvaGFyYSIsImEiOiJDdmJnOW1FIn0.kJV65G6eNXs4ATjWCtkEmA';
	Progress.map = L.mapbox.map('map', 'mapbox.streets')
	  .setView([38.8961, -76.9806], 15);

    // https://www.mapbox.com/guides/an-open-platform/#tilejson
	 var tileLayer = L.tileLayer("https://api.tiles.mapbox.com/v4/mapbox.streets/{z}/{x}/{y}.png?access_token=pk.eyJ1Ijoia290YXJvaGFyYSIsImEiOiJDdmJnOW1FIn0.kJV65G6eNXs4ATjWCtkEmA", {
		"minzoom": 0,
		"maxzoom": 18,
		attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
		'<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
		'Imagery © <a href="http://mapbox.com">Mapbox</a>',
		id: 'examples.map-20v6611k'
	 });

    // Tile:
    // https://www.mapbox.com/developers/api/maps/#tilejson
    //Progress.map = L.mapbox.map('map', 'mapbox.streets');
	/*
	 *var tileLayer = L.tileLayer('https://{s}.tiles.mapbox.com/v3/{id}/{z}/{x}/{y}.png', {
	 *  maxZoom: 20,
     *  attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
     *    '<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
     *    'Imagery © <a href="http://mapbox.com">Mapbox</a>',
     *  id: 'examples.map-20v6611k'
     *});
	 */
    tileLayer.addTo(Progress.map);
	var lcontrol = L.control.layers([tileLayer], [])


    // Initialize other modules
    Progress.streets.init(data);
  }

  self.init = _init;
  return self;
} ($, L));
