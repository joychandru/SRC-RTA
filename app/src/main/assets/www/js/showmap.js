var SHOW_MYLOCATION={};
SHOW_MYLOCATION.isStatcMap=true;
SHOW_MYLOCATION.mymapBranchView =null;
SHOW_MYLOCATION.isLocationChanged=false;
sessionStorage.changedLat ="";
sessionStorage.changedLng="";
SHOW_MYLOCATION.onLoad=function()
{
}
SHOW_MYLOCATION.btnHome_click = function(e)
{
	if(e) e.preventDefault();
	window.location = COMMON.parentPage;
};
SHOW_MYLOCATION.btnNext_click = function(e)
{
	if(e) e.preventDefault();
	//Go back to prev screen
	//alert(Wrapper.GoBack);
	Wrapper.GoBack();
};
var markersArray = [];
SHOW_MYLOCATION.clearOverlays = function() {
	  if (markersArray) {
	    for (i in markersArray) {
	      markersArray[i].setMap(null);
	    }
	  }
	}
var marker1=null;
SHOW_MYLOCATION.showLocation = function(latitude, longitude)
{
	try
	{
		 mymapBranchView = new google.maps.Map(document.getElementById('mapView2'), {
	           zoom: 16,
	           center: new google.maps.LatLng(latitude, longitude),
	           mapTypeId: google.maps.MapTypeId.ROADMAP
	         });
	         
	         //google.maps.event.trigger(mymapBranchView, 'resize');
	         
	         google.maps.event.addListenerOnce(mymapBranchView, 'idle', function(){
	        	//alert("Location is successfully loaded");
	        	});
	         
	        var viewport = {
				width: jQuery(window).width(),
				height: jQuery(window).height()
			};
	          
	        
			 var iconpath = 'images/email.png';

			 var infowindow = new google.maps.InfoWindow();
			 
	             google.maps.event.addListener(mymapBranchView, "click", function(event,latlng) {
	            	 var jsonStr = JSON.stringify(event);
	            	 var jsonObj = JSON.parse(jsonStr);
	            	
	            	 var latStr = jsonObj.latLng.lat;
	            	 var lngStr = jsonObj.latLng.lng;
	            	 
	            	 if(jsonObj.latLng.G!=undefined && jsonObj.latLng.G!=null)
	            		 {
	            		 latStr = jsonObj.latLng.G;
	            		 }
	            	 if(jsonObj.latLng.K!=undefined && jsonObj.latLng.K!=null)
            		 {
	            		 lngStr = jsonObj.latLng.K;
            		 }
	            	             	 
	            	 var latitude =sessionStorage.changedLat;
	            	 var longitude=sessionStorage.changedLng;
	            	 
	            	 if(latStr!=null && latStr!= undefined && latStr!="")
            		 {
	            		 SHOW_MYLOCATION.isLocationChanged=true;
	            		 latitude = latStr.toString();
            		 }
	            	 if(lngStr!=null && lngStr!= undefined && lngStr!="")
            		 {
	            		 SHOW_MYLOCATION.isLocationChanged=true;
	            		 longitude = lngStr.toString();
            		 }

		        	 if(latitude.length >10)
                     {
                         latitude= latitude.substring(0, 10);
                     }
                     if(longitude.length >10)
                     {
                         longitude =longitude.substring(0, 10);
                     }

		        	 sessionStorage.changedLat =latitude;
		        	 sessionStorage.changedLng=longitude;
		        	 $("#txtSetLat").val(latitude);
		     		 $("#txtSetLong").val(longitude);
	            	 
		     		 SHOW_MYLOCATION.showLocation(latitude, longitude);
	            	 SHOW_MYLOCATION.showStaticLocation(latitude, longitude);
	            	 Wrapper.pushLocation(latitude,longitude);
	            	 
		        	 //Read lat and long values and call map view function
		        	 
		            /* if (true) 
		             {
		            	 SHOW_MYLOCATION.clearOverlays();
		            	 
		            	 if(marker1 !=null && marker1 != 'undefined')
		            	 {
		            		 setTimeout(function(){
		            		 marker1.setMap(null);
		            		 marker.setMap(null);
		            		 },100);
		            		 //mymapBranchView.removeOverlay(marker1);
		            	 }
		            	 marker1 = new google.maps.Marker({position: event.latLng, map: mymapBranchView});
		            	 markersArray.push(marker1);
		            	 
		                google.maps.event.addListener(marker1, "click", function() {
		                	//alert("test")
		                   var html = "<table>" +
		                              "<tr><td>Lat:</td> <td></td></tr>" +
		                              "<tr><td>Long:</td> <td></td> </tr>" +
		                              "<tr><td></td><td><input type='button' value='Save & Close' onclick='saveData()'/></td></tr>";
		                   //marker1.openInfoWindow(html);
		                   infowindow.setContent(html);
			               infowindow.open(mymapBranchView, marker1);
		                 })(marker1);

		              }*/
		             });
	             
	             var marker;
	             marker = new google.maps.Marker({
	             position: new google.maps.LatLng(latitude, longitude),
	             map: mymapBranchView,
	             /*icon: iconpath */
	           });
	             
	             markersArray.push(marker);
	             
	        google.maps.event.addListener(marker, 'click', 
	            (function(marker) 
	            {
	                return function() 
	                {
	                	 infowindow.setContent("<body><h4>My Location </h4></body>");
		                 infowindow.open(mymapBranchView, marker);
	                }
	            })(marker));
	}
	catch(e)
	{
		alert(e);
	}
};
SHOW_MYLOCATION.getDirection = function(fromlatitude, fromlongitude, tolatitude, tolongitude)
{
	try
	{
		var directionsDisplay = new google.maps.DirectionsRenderer();	
		var directionsService = new google.maps.DirectionsService();
	
	    var centerpoint = new google.maps.LatLng(fromlatitude, fromlongitude);
	
	    var start =fromlatitude + "," + fromlongitude;
	    var end = tolatitude + "," + tolongitude;		    
	    
    	var myOptions = {
	     	zoom: 6,
	      center: centerpoint,
	      mapTypeId: google.maps.MapTypeId.ROADMAP,
	      zoomControl: true
	    }
	    mymapBranchView = new google.maps.Map(document.getElementById("mapView"), myOptions);		   
	    directionsDisplay.setMap(mymapBranchView);			    
	    
	    //set Direction
	    var request = {
	      origin:start, 
	      destination:end,
	      travelMode: google.maps.DirectionsTravelMode.DRIVING
	    };
	    directionsService.route(request, function(response, status) 
	    {
	      if (status == google.maps.DirectionsStatus.OK) 
	      {
	    	  directionsDisplay.setDirections(response);	
	      }
	    });
	}
	catch(e)
	{
		
	}
};
SHOW_MYLOCATION.showStaticLocation = function(latitude, longitude)
{
	try
	{
	    //alert("latitude=>" + latitude);
	    //alert("Show Static location");
		var img = document.getElementById("mapView");
		//get height and width
		var width = img.clientWidth;
		var height = img.clientHeight;
		$("#txtSetLat").val(latitude);
		$("#txtSetLong").val(longitude);
		img.src ="";
		img.src = "https://maps.googleapis.com/maps/api/staticmap?center=" + latitude + "," + longitude + "&"+ //40.702147,-74.015794&zoom=13&"+
		//"maptype=roadmap&markers=color:blue%7Clabel:S%7C40.702147,-74.015794";
		"size="+width +"x" + height +"&maptype=roadmap&markers=color:red%7Clabel:S%7C"+latitude+","+longitude;
		
		img.onload = function()
		{
			//alert("Location is successfully loaded");
			var canvas = document.getElementById('mycanvas') // reference to canvas element
			
			var ctx = canvas.getContext('2d'); // get the canvas context;
			ctx.canvas.height = height;
			ctx.canvas.width = width;
			ctx.drawImage(img, 0, 0, width, height); //draw image into canvas;
			var strDataURI = canvas.toDataURL(); 
			Wrapper.saveImage(strDataURI);
		}
	}
	catch(e)
	{
		alert(e);
	}
}
SHOW_MYLOCATION.pushLocation = function(latitude, longitude, incidentID)
{
    //alert("Info: Your previously saved location will be shown by default! \n You can either refresh the current location or manually choose location to update.")
	SHOW_MYLOCATION.isLocationChanged=false;
	sessionStorage.changedLat =latitude;
	sessionStorage.changedLng=longitude;
	sessionStorage.IncidentID = incidentID;
	
	console.log(latitude + "," + longitude);
	
	$("#txtSetLat").val(latitude);
	$("#txtSetLong").val(longitude);
	//alert("Push Location:" + SHOW_MYLOCATION.isStatcMap + "==>" + latitude);

	if(SHOW_MYLOCATION.isStatcMap == true)
	{
		SHOW_MYLOCATION.showStaticLocation(latitude, longitude);
	}
	else
	{
		SHOW_MYLOCATION.showLocation(latitude, longitude);
		
	}
};
SHOW_MYLOCATION.locFailed = function(response)
{
	console.log("Failed" + response);
};
SHOW_MYLOCATION.btnStatic_click =function()
{
	$("#mapView").addClass("displayShow");
	$("#mapView2").removeClass("displayShow");
	
	$(".buttonStatic").addClass("selectedButton");
	$(".buttonDynamic").removeClass("selectedButton");
	
	SHOW_MYLOCATION.isStatcMap = true;

	/*if(SHOW_MYLOCATION.isLocationChanged==false)
	{
		setTimeout(function(){
			Wrapper.getShowLocation();
		}, 1000);
	}
	else 
	{
		SHOW_MYLOCATION.showStaticLocation(
		sessionStorage.changedLat, 
		sessionStorage.changedLng);
	}*/

	//alert(sessionStorage.IncidentID);

               var responseWrapper = Wrapper.getLocation(sessionStorage.IncidentID,
                       		"SHOW_MYLOCATION.getLocation_success",
                       		"NEWINCIDENT.getLocation_failure");



};
SHOW_MYLOCATION.btnDynamic_click =function()
{
	$("#mapView2").addClass("displayShow");
	$("#mapView").removeClass("displayShow");
	
	$(".buttonDynamic").addClass("selectedButton");
	$(".buttonStatic").removeClass("selectedButton");
	
	SHOW_MYLOCATION.isStatcMap = false;
	//alert("Is Location changed dynamic:" + SHOW_MYLOCATION.isLocationChanged);
	if(SHOW_MYLOCATION.isLocationChanged==false)
	{
		setTimeout(function(){
			Wrapper.getShowLocation();
		}, 1000);
	}
	else
	{
	    SHOW_MYLOCATION.showLocation(sessionStorage.changedLat,
                                     		sessionStorage.changedLng);
		/*SHOW_MYLOCATION.showStaticLocation(
		sessionStorage.changedLat, 
		sessionStorage.changedLng);*/
	}
};
SHOW_MYLOCATION.btnSetLocation_click =function()
{
	/*var lat= $("#txtSetLat").val();
	var long= $("#txtSetLong").val();
	SHOW_MYLOCATION.isLocationChanged=true;
	sessionStorage.changedLat =lat;
	sessionStorage.changedLng=long;
	Wrapper.pushLocation(lat,long);
	SHOW_MYLOCATION.showStaticLocation(lat,long);*/


    var responseWrapper = Wrapper.getLocation(sessionStorage.IncidentID,
                   		"SHOW_MYLOCATION.getLocation_success",
                   		"NEWINCIDENT.getLocation_failure");
    //alert(sessionStorage.IncidentID);

};
SHOW_MYLOCATION.getLocation_failure = function(response)
{
    alert("Error:"+ response);
};
SHOW_MYLOCATION.getLocation_success = function(response)
{
    if(response =="OFF")
    	{
    		alert("Switch ON GPS to update Location");
    		$("#txtSetLat").val("No Location");
            $("#txtSetLong").val("No Location");
    	}
    	else if(response=="NO")
    	{
    		 alert("No Location details found.");
    		 $("#txtSetLat").val("No Location");
    		 $("#txtSetLong").val("No Location");
    	}
    	else
    	{
    		//alert(response);
    		//Show Location in Map
    		if(response.split(",").length == 2)
    		{
    			var location=response.split(',');
        		var latitude=location[0];
        		var longitude=location[1];

                $("#txtSetLat").val(latitude);
                $("#txtSetLong").val(longitude);

                SHOW_MYLOCATION.isLocationChanged=true;
                sessionStorage.changedLat =latitude;
                sessionStorage.changedLng=longitude;
                Wrapper.pushLocation(latitude,longitude);
                SHOW_MYLOCATION.showStaticLocation(
                                    sessionStorage.changedLat,
                                    sessionStorage.changedLng);

                /*if(SHOW_MYLOCATION.isLocationChanged==false)
                {
                    setTimeout(function(){
                        Wrapper.getShowLocation();
                    }, 1000);
                }
                else
                {
                    SHOW_MYLOCATION.showStaticLocation(
                    sessionStorage.changedLat,
                    sessionStorage.changedLng);
                }*/
    		}
    	}
};

$(document).ready(function()
{
	SHOW_MYLOCATION.isStatcMap = true;
	SHOW_MYLOCATION.isLocationChanged==false;
	
	setTimeout(function(){
		Wrapper.getShowLocation();
	}, 1000);
	
	//SHOW_MYLOCATION.pushLocation("10","79");
	COMMON.CurrentPage = COMMON.SCREENS.ShowMap;
	$(".header_left").on("click", SHOW_MYLOCATION.btnHome_click);	
	$(".buttonStatic").on("click", SHOW_MYLOCATION.btnStatic_click);	
	$(".buttonDynamic").on("click", SHOW_MYLOCATION.btnDynamic_click);	
	$("#btnSetLocation").on("click", SHOW_MYLOCATION.btnSetLocation_click);

	$(".nav_right").on("click", SHOW_MYLOCATION.btnNext_click);
});
