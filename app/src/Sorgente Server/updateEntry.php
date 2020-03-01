<?php
 include "connect.php";

 
 $lat        = (double)$_POST['lat'];
 $lon        = (double)$_POST['lon'];
 $deviceID   = $_POST['did'];
 $token      = $_POST['token'];
 $isPOI      = $_POST['poi']; //not using this

 $isPOICoord      = $_POST['poiC']; //not using this

    $update = "update geotrend.paths
               set 
               poi   = ST_MakePoint($lon, $lat)
               where device_id = '$deviceID' and token = '$token';";
    $result = pg_query($db_connection, $update);

    if($result)
    {
        echo "Correctly updated";
        exit;
    }
    else
    {
        echo pg_result_error($result); exit;
    }
?>