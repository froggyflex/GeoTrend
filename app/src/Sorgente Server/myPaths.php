<?php
	include "connect.php";


	function rand_color() {
		    return '#' . str_pad(dechex(mt_rand(0, 0xFFFFFF)), 6, '0', STR_PAD_LEFT);

	}
	
	$user = $_GET['userid']; //'61af54aec8514008';//

	$getpaths = "select st_x(a.point) as x, st_y(a.point) as y ,current_status as state, token
				 from geotrend.paths as a
				 where a.device_id='$user' order by token
				 ";

	$result = pg_query($db_connection, $getpaths);

	

    $lastX = 0;
    $lastY = 0;

	$geo    = array();
	
	$holder = array();
	$color  = "#FF5733"; //default color
	while($row = pg_fetch_object($result))
    {
    	
    	$lastX = $row->x;
    	$lastY = $row->y;
    	//print_r($holder);
    	
    	//change color based on path
    	if(count($holder) == 0)
    	{
    		array_push($holder, $row->token);
    		
    	}
    	else
    	{
    		
    		if($holder[0] != $row->token )
	    	{
	    		$color = rand_color();
	    		$holder = array();
	    		array_push($holder, $row->token);
	  
	    	}
	    
    	}
        //-------------------------

        $geometry[] = [
        	"type" => "Feature",
        	"geometry" =>  array(
						        "type" => "Point",
						        "coordinates" => array((double)$row->x,(double)$row->y)
						    ),
        	"properties" => array(
        						"status" => $row->state,
        						"marker-color" => $color
        					)

        ];
	   
	
    }

    if(!(isset($geometry)))
	{
		echo "You may have not registered any paths yet!";
		exit;
	}
    $fullgeojson=array("type"=>"FeatureCollection", "features" => $geometry);

    $encoded = json_encode($fullgeojson, JSON_NUMERIC_CHECK );
    $path = getcwd();
    $path .= '/Paths/'.$user.'.json';



	if (file_exists($path))
	{
	    unlink ($path);
	    file_put_contents($path, $encoded); 
	    
	    if (file_exists($path)) {
	    
	    } else {
	         echo "An Error occured";
	    }
	} 
	else 
	{
	    file_put_contents($path, $encoded); 
	    
	    if (file_exists($path)){
	   
	    } else {
	         echo "An Error Occured";
	    }
	  
	}


?>

<!doctype html>
<html lang="en">
  <head>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/openlayers/openlayers.github.io@master/en/v6.1.1/css/ol.css" type="text/css">
    <style>
      .map {
        height: 80vh;
        width: 100%;
      }
    </style>
    <script src="https://cdn.jsdelivr.net/gh/openlayers/openlayers.github.io@master/en/v6.1.1/build/ol.js"></script>
    <title>My Paths</title>
  </head>
  <body>
    <h2>Paths of <?php echo $user;?></h2>
    <div id="map" class="map"></div>
    <script type="text/javascript">

      let user = '<?php echo $user;?>';
      
  //       var style1 = [
		//    , 
		// ];
      // Define a new vector Layer
      var geoLayer = new ol.layer.Vector({
	       title: 'GeoJSON Layer',
	       source: new ol.source.Vector({
             projection : 'EPSG:4326',
             url: "Paths/"+user+".json",
             format: new ol.format.GeoJSON()
         }),
			style: function(feature, resolution) {
			        return (new ol.style.Style({
							       image: new ol.style.Circle({
							           radius: 5,
							           fill: new ol.style.Fill({ color: feature.get('marker-color') })
							      }),
							    zIndex: 5
							})
					);
			}	
      });

     let x = '<?php echo (double)$lastX; ?>';
     let y = '<?php echo (double)$lastY; ?>';
     var map = new ol.Map({
		target: 'map',
        layers: [
            new ol.layer.Tile({
              source: new ol.source.OSM()
            })
        ],
        view: new ol.View({
            center: ol.proj.fromLonLat([parseFloat(x), parseFloat(y)]),
            zoom: 10
       	})
      });

     map.addLayer(geoLayer);



    </script>
  </body>
</html>