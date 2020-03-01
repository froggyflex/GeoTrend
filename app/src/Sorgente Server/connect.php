<?php
	
	$username = "postgres";
	$password = "f@n357159";
	$db       = "postgres";

	$db_connection = pg_connect("host=localhost dbname=".$db." user=".$username." password=".$password);

	
?>