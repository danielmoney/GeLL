 function show(id)
 {
     //document.getElementById("standard").style.display="none";
     document.getElementById(showing).style.display="none";
     document.getElementById(id).style.display="block";
     showing = id;
 }
 function hide(id)
 {
     //document.getElementById(id).style.display="none";
     //document.getElementById("standard").style.display="block";
 }
 var showing = "standard";