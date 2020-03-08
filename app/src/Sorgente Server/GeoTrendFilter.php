<?php
 include "connect.php";

 $geojson    = $_POST['geojson'];
 $deviceID   = $_POST['deviceID'];
 $token      = $_POST['initial'];
 $parsed     = json_decode($geojson);

 $lat        = $parsed->features[0]->geometry->coordinates[0];//44.498056;
 $lon        = $parsed->features[0]->geometry->coordinates[1];//11.355036;
 $cur_status = "walking"; //$parsed->features[0]->properties->status; other statuses could be implemented

if($lat != null && $lon != null && $cur_status != null)
{
     
     $insertPoint = "insert into geotrend.paths(point, device_id, current_status, token)
                                      values(ST_MakePoint($lon, $lat), '$deviceID', '$cur_status', '$token');";

     $result = pg_query($db_connection, $insertPoint);  


     elabStatus("walking", $lat, $lon);
     if(!$result)
     {
        echo "ERROR: ".pg_result_error($result);
        exit;
     }
}

function elabStatus($status, $lat, $long)
{
     global $db_connection;

     // update geotrend.walk
    //  set expanded = st_buffer(geometry,0.0005)::geometry(Polygon,4326)
     $mix = "";
     if($status == "walking")
     {
        //check if first is inside the buffered area
        $query  = "select st_contains( a.expanded, st_geomfromtext('POINT($long $lat)', 4326)) as isthere, message from geotrend.walk as a";
        $result = pg_query($db_connection, $query);

        $presence_array = array();
        while ($data = pg_fetch_object($result)) {
             
             $presence_array[] = [
                "is"  => $data->isthere,
                "mx"  => $data->message
             ];
        }

      
        

        if($result)
        {
            foreach ($presence_array as $value) {

           

            if($value['is'] === true || $value['is'] === "t")
            { 
                $mix = $value['mx'];

                 //check now if the device is inside the real area
                $queryR      = "select st_contains( a.geometry, st_geomfromtext('POINT($long $lat)', 4326)) as isthere, message from geotrend.walk as a where a.message = '$mix';";
                $resultR     = pg_query($db_connection, $queryR);
                $isInBufferR = pg_fetch_object($resultR);
                $rBufR       = $isInBufferR->isthere;
                    
                    if($rBufR)
                    {
                           
                            //the user is inside so a notification has to be fired
                            $tit      = "Hey there you just got through a Geofence!";
                            $mes      = $mix;
                            $has_link = stristr($mes, 'http://') ?: stristr($mes, 'https://');


                            $message  = array();
                            $message[]= array('title'=> $tit, 'message'=> $mes, 'link_data' => $has_link, 'bp' => "data");


                            //implement a way to send the notification once as long as he is inside the area
                            ob_clean();
                            echo json_encode($message);
                            exit;
                    }
                    else
                    {       ob_clean();
                            echo "MX:".$mix;
                           continue;
                    }
            }
            else
            {
                           ob_clean();
                           echo "MX:".$mix;
                           continue; 
            }


            }
        }
        


    }
    else if($status == "In Vehicle")
    {

    }
    else if($status == "On Bicycle")
    {

    }



}



 
?>
