<?php
	
	$username = "********";
	$password = "********";
	$db       = "postgres";

	$db_connection = pg_connect("host=localhost dbname=".$db." user=".$username." password=".$password);

	
?>
