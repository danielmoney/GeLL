 function show(id)
 {
     document.getElementById(id).style.display="block";
     document.getElementById(id + 'S').style.display="none";
     document.getElementById(id + 'H').style.display="inline";
 }
 function hide(id)
 {
     document.getElementById(id).style.display="none";
     document.getElementById(id + 'S').style.display="inline";
     document.getElementById(id + 'H').style.display="none";
 }