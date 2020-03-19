<?php
 include "connect.php";
 

 // $query = "select st_y(point) as long, st_x(point) as lat from geotrend.paths where device_id = '$userid';";
 // $result =  pg_query($db_connection, $query);
 // $pointlist = array();

 // while($row = pg_fetch_assoc($result)) {
	// 	$pointlist[]=array($row['long'], $row['lat']);

 // };

 $getUsers      = "select distinct device_id  from geotrend.paths group by device_id;";
 $resultUsers   =  pg_query($db_connection, $getUsers);
 $users = "<option>Select User</option><option>All Users</option>";
 while($row = pg_fetch_assoc($resultUsers)) {
		$users .= "<option>".$row['device_id']."</option>";

 };

function rand_color() {
	return '#' . str_pad(dechex(mt_rand(0, 0xFFFFFF)), 6, '0', STR_PAD_LEFT);
}

?>
<!DOCTYPE html>
<html lang="en">

<head>

	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1">

        <link rel="icon" href="pics/website.png" type="image/gif" sizes="16x16">
	<title>GeoTrend Admin Page</title>
	

	<!-- Google font -->
    	<link href="https://fonts.googleapis.com/css?family=Montserrat:400,700%7CVarela+Round" rel="stylesheet">
    	 <link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/openlayers/openlayers.github.io@master/en/v6.1.1/css/ol.css" type="text/css">
    
    	<!-- Bootstrap -->
    	 <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
    	 <script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.9.2/jquery-ui.min.js"></script>
    	 <link rel="stylesheet" href="https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.4/themes/smoothness/jquery-ui.css">
      	
    	<!-- Font Awesome Icon -->
    	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.11.2/css/all.min.css">
	    <!-- Custom stlylesheet -->
		<script src="https://cdn.jsdelivr.net/gh/openlayers/openlayers.github.io@master/en/v6.1.1/build/ol.js"></script>

		<link rel="stylesheet" href=" https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css">

		 
    <style>
      .map {
        height: 80vh;
        width: 90%;
        margin:0 auto;
        text-align: center;
      }
      object
      {
      	height: 60vh;
        width: 60%;
        margin:0 auto;
        text-align: center;
      }
      h2
      {
      	color:grey;
      }
    </style>
</head>

<body style='background: url("pics/ab.jpg") ;background-size: auto;'>

	<div style='width:80%;margin:0 auto;text-align: center'>
		
		<h2>Pannello di Controllo - GeoTrend V1</h2>

		<br>
		<br>
		<br>

		<div style='width:60%;margin:0 auto;text-align: center;background-color:#298fca;opacity: 0.9; border-radius: 10px;height:250px;' ><br> 
			<div style='margin:0 auto;text-align: center;'><legend style="color:black;">Registered Users</legend>
				<select style='border-radius: 5px;' onchange="loadPathOfUser(this.value)" id='users'><?php echo $users; ?></select> <br> <br>
		    </div>
		    <div style='margin:0 auto;text-align: center; background-color:white; width:95%; border-radius:10px;'>
		    	<br><legend>Option Menu</legend>
		    	<button class='btn btn-primary' id='Pusers' onclick='displayQt()'>Visited Areas</button> &nbsp;&nbsp;
				<button class='btn btn-primary' id='Pusers' onclick='displayVF()'>Visited Fences</button> &nbsp;&nbsp;
				<button class='btn btn-success' id='Kmeans' onclick='displayKmeans()' >K-MEANS CLUSTER (K=2) </button>&nbsp;&nbsp;
				<button class='btn btn-success' id='Kmeans' onclick='loadFences()'>Reset Map</button>
				 <br> <br>
		    </div>
		</div>
		<br> <br><br> <br>
		<div id="map" class="map"></div>
		<br>
		<br>
	</div>

</body>
<script type="text/javascript">


     

	 var fences = new ol.style.Style({
		        stroke: new ol.style.Stroke({
		          color: [255,0,0,0.6],
		          width: 2
		        }),
		        fill: new ol.style.Fill({
		          color: [255,0,0,0.2]
		        }),
		        zIndex: 1
      });
    


     let x = '<?php echo (double)11.3586; ?>';
     let y = '<?php echo (double)44.4498; ?>';
    
    
	var geoLayer = null;
	var map = null;
     function loadFences()
     {
     	$("#map").html("");
     	 
		map = new ol.Map({
					target: 'map',
			        layers: [
			            new ol.layer.Tile({
			              source: new ol.source.OSM()
			            })
			        ],
			        view: new ol.View({
			            center: ol.proj.fromLonLat([parseFloat(x), parseFloat(y)]),
			            zoom: 12
			       	})
		      	 });
     	var geoLayer1 = new ol.layer.Vector({
		       title: 'GeoJSON Layer',
		       source: new ol.source.Vector({
	             projection : 'EPSG:4326',
	             url: "fences/geofence_walk.geojson",
	             format: new ol.format.GeoJSON()
	         }),
		       style: function(feature, resolution) {
				      return fences;
				}
				
	      });
			map.addLayer(geoLayer1);
	     
     }
     
     function loadPathOfUser(userP)
     {	
			
     		if(userP != "All Users")
     		{
	     		  if(geoLayer != null){map.removeLayer(geoLayer); }

				   geoLayer = new ol.layer.Vector({
			       title: 'Paths',
			       source: new ol.source.Vector({
		             projection : 'EPSG:4326',
		             url: "Paths/"+userP+".json",
		             format: new ol.format.GeoJSON()
		         	}),
			       	style:function(feature)
			       	{
			       		return(
			       			new ol.style.Style({
								image: new ol.style.Circle({
								            radius: 5,
								            fill: new ol.style.Fill({color: 'lime'})
								})
							})
			       		);
			       	}
		       });

		      map.addLayer(geoLayer);
	  	}
	  	else
	  	{
	  		if(geoLayer != null){map.removeLayer(geoLayer); }

	  		 <?php

	  			$getpaths = "select st_x(a.point) as x, st_y(a.point) as y ,current_status as state, device_id
				 from geotrend.paths as a
				 order by device_id, token;
				 ";

				$result = pg_query($db_connection, $getpaths);

				$holder = array();
				$color  = "#FF5733"; //default color
				while($row = pg_fetch_object($result))
			    {
			    	
			    	$lastX = $row->x;
			    	$lastY = $row->y;
			    	//print_r($holder);
			    	
			    	//change color based on device_id
			    	if(count($holder) == 0)
			    	{
			    		array_push($holder, $row->device_id);
			    		
			    	}
			    	else
			    	{
			    		
			    		if($holder[0] != $row->device_id )
				    	{
				    		$color = rand_color();
				    		$holder = array();
				    		array_push($holder, $row->device_id);
				  
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
			        						"device_id" => $row->device_id,
			        						"marker-color" => $color
			        					)

			        ];
				   
				
			    }

			    $fullgeojson=array("type"=>"FeatureCollection", "features" => $geometry);

			    $encoded = json_encode($fullgeojson, JSON_NUMERIC_CHECK );
			    $path = getcwd();
			    $path .= '/requests/all_users.json';



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

	  		 geoLayer = new ol.layer.Vector({
			       title: 'Requests',
			       source: new ol.source.Vector({
		             projection : 'EPSG:4326',
		             url: "requests/all_users.json",
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

		      map.addLayer(geoLayer);

	  	}
     }
    function displayVF()
	{	
		document.getElementById("map").innerHTML='<br> <label style="color:#298fca;font-size:20px;">QUERY USED: select st_crosses(geometry, st_makeline(path)), st_astext(geometry) <br>from geotrend.pathwalk, geotrend.walk<br> group by geometry</label><object type="text/html"  data="requests/qgis2web_2020_02_08-17_05_18_143667/index.html" ></object>';
	}
	function displayQt()
	{	
		document.getElementById("map").innerHTML='<br> <label style="color:#298fca;font-size:20px;">QUERY USED: select st_astext(geometry) as fence, count( distinct token) as numero_utenti <br> from geotrend.walk left join geotrend.paths on <br>st_contains(geometry, st_setsrid(point, 4326)) <br>group by geometry</label><object type="text/html"  data="requests/qgis2web_2020_02_12-17_35_06_099357/index.html" ></object>';
	}
	function displayKmeans()
	{	
		document.getElementById("map").innerHTML='<object type="text/html"  data="requests/qgis2web_2020_02_12-17_52_27_043578/index.html" ></object>';
	}

    </script>
</html>
