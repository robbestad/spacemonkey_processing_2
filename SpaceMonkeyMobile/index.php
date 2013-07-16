<?php
if( preg_match('/iphone|ipad|android/i', $_SERVER['HTTP_USER_AGENT']) ) {
    location("http://play.svenardo.com/spacem");
}
else {
    include('index.html');
}
