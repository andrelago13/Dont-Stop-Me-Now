<?php
header('Content-Type: application/json; charset=utf-8');

$requestParts = explode('/', rtrim($_GET['request'], '/'));
$method = $_SERVER['REQUEST_METHOD'];

switch ($requestParts[0]) {
	case "events": {
		
	}
	break;
	case "users": {
		
	}
	break;
}

echo json_encode($requestParts);
?>