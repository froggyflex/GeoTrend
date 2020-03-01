<?php
	
	include "connect.php";
	
	$dataForMx = '{"type":"FeatureCollection","features":[{"type":"Feature","properties":{"message":"https://www.bolognawelcome.com/uffici-informazione/bologna-welcome-piazza-maggiore/"},"geometry":{"type":"Polygon","coordinates":[[[11.341195106506346,44.49561759886438],[11.340723037719727,44.49393393165396],[11.33965015411377,44.49402576930068],[11.338791847229004,44.492127761877086],[11.34012222290039,44.491791012172584],[11.340079307556152,44.4913624188274],[11.344842910766602,44.491515488240836],[11.345486640930176,44.49313799932412],[11.34510040283203,44.49316861230689],[11.345357894897461,44.49436250610129],[11.345529556274414,44.49518903364097],[11.34239673614502,44.49564821054554],[11.341195106506346,44.49561759886438]]]}},{"type":"Feature","properties":{"message":"https://www.bolognawelcome.com/home/scopri/luoghi/cultura-e-storia/passato-presente/universita-di-bologna/"},"geometry":{"type":"Polygon","coordinates":[[[11.350250244140625,44.496229829434164],[11.351966857910154,44.494178831723815],[11.356644630432129,44.49393393165396],[11.356172561645508,44.498158313714924],[11.350464820861816,44.49971942094208],[11.349177360534668,44.49766854597149],[11.348662376403809,44.49754610339286],[11.350250244140625,44.496229829434164]]]}},{"type":"Feature","properties":{"message":"https://www.bolognawelcome.com/home/scopri/luoghi/architettura-e-monumenti/edifici-religiosi/complesso-di-santo-stefano/"},"geometry":{"type":"Polygon","coordinates":[[[11.34638786315918,44.492954321089925],[11.34638786315918,44.491515488240836],[11.347718238830566,44.48842340818461],[11.351323127746582,44.492403282916335],[11.34763240814209,44.493780868588026],[11.34638786315918,44.492954321089925]]]}},{"type":"Feature","properties":{"message":"https://www.bolognawelcome.com/home/scopri/luoghi/architettura-e-monumenti/edifici-religiosi/basilica-di-san-francesco/"},"geometry":{"type":"Polygon","coordinates":[[[11.336860656738281,44.492770642277236],[11.337718963623047,44.49567882221063],[11.334843635559082,44.49647471986228],[11.334199905395508,44.49356657962081],[11.332740783691406,44.493842093862604],[11.332354545593262,44.49274002908554],[11.336088180541992,44.491760398466674],[11.336860656738281,44.492770642277236]]]}},{"type":"Feature","properties":{"message":"https://www.bolognawelcome.com/home/scopri/luoghi/cultura-e-storia/musei-e-gallerie-arte/mambo-museo-darte-moderna-di-bologna/"},"geometry":{"type":"Polygon","coordinates":[[[11.3358736038208,44.49846441646551],[11.338791847229004,44.49904600726441],[11.339607238769531,44.501341703772546],[11.337246894836426,44.50219874062979],[11.334757804870605,44.50210691585485],[11.333599090576172,44.50186204908127],[11.3358736038208,44.49846441646551]]]}},{"type":"Feature","properties":{"message":"https://www.bolognawelcome.com/home/scopri/luoghi/architettura-e-monumenti/edifici-religiosi/oratorio-san-filippo-neri/"},"geometry":{"type":"Polygon","coordinates":[[[11.342997550964355,44.49630635780343],[11.343812942504883,44.499122531937665],[11.342289447784424,44.4990613122071],[11.339757442474363,44.49892356757832],[11.339178085327148,44.49671960926192],[11.342997550964355,44.49630635780343]]]}},{"type":"Feature","properties":{"message":"https://www.bolognawelcome.com/home/find-book/le-due-torri-torre-degli-asinelli/"},"geometry":{"type":"Polygon","coordinates":[[[11.347246170043945,44.49396454421894],[11.348791122436523,44.49552576372451],[11.346302032470703,44.49500536186677],[11.345915794372559,44.494301281373104],[11.347246170043945,44.49396454421894]]]}},{"type":"Feature","properties":{"message":"Stazione FS"},"geometry":{"type":"Polygon","coordinates":[[[11.338105201721191,44.50464734794379],[11.340293884277342,44.50213752412924],[11.344456672668457,44.50137231244867],[11.345529556274414,44.5047391687174],[11.340594291687012,44.50541251663796],[11.339778900146484,44.50556554917157],[11.338105201721191,44.50464734794379]]]}},{"type":"Feature","properties":{"message":"https://www.bolognawelcome.com/en/home/discover/places/architecture-and-monuments/religious-places/basilica-of-san-domenico/"},"geometry":{"type":"Polygon","coordinates":[[[11.346323490142822,44.49027561444014],[11.346323490142822,44.49035215062323],[11.343770027160645,44.490688908635725],[11.343619823455809,44.49045930111084],[11.341967582702637,44.49045930111084],[11.340980529785156,44.48996946870339],[11.339778900146484,44.48998477602839],[11.339607238769531,44.48842340818461],[11.345143318176268,44.48796417439378],[11.346323490142822,44.49027561444014]]]}},{"type":"Feature","properties":{"message":"Giardini Margherita"},"geometry":{"type":"Polygon","coordinates":[[[11.356086730957031,44.48441264431511],[11.358060836791992,44.48582103556305],[11.357545852661133,44.489923546704276],[11.355957984924316,44.490505222688135],[11.353340148925781,44.487015079768476],[11.352825164794922,44.48732124101233],[11.351709365844727,44.48609658639522],[11.350593566894531,44.48624966962844],[11.350336074829102,44.48511684418947],[11.356086730957031,44.48441264431511]]]}}]}';

	    
			$decoded = json_decode($dataForMx, true);
			foreach ($decoded['features'] as $item) {
				
				//carico i punti prima
				
				$geoms = $item['geometry'];
				$type  = $item['geometry']['type'];
				$crd   = $item['geometry']['coordinates'][0];
				$mes   = $item['properties']['message'];
			
				//echo json_encode($crd);
				
				$cjson = array();
				$cjson[] = array('type'=> $type, 'coordinates'=> array($crd));
				$data  = json_encode($cjson);	
				
				$data  = trim($data, '[]'); 
			    $insertArea = "INSERT INTO geotrend.walk(message, geometry) 
						 	VALUES ('$mes', '$geoms');";
				echo $insertArea;echo "<br><br>";
				$result     = pg_query($db_connection, $insertArea);


				if($result)
				{
					echo "DB POPULATED <br>";
				}

				

				
			}
		
	
	



?>
<script>

	let data  = '<?php echo $dataForMx; ?>'
	console.log(JSON.parse(data).features)
</script>