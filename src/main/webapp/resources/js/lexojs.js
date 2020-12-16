/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


function setHeight() {

    var height = $(document.getElementById('content')).height();
    //alert($(document.getElementById('content')).height());
    // alert($(document.getElementById('lexiconCreationTabViewForm:tabViewPanel')).height());
//    var x = document.getElementById("editViewTab").parentNode;
//    //alert(x.offsetHeight);
//    var heightScrollableTab = x.offsetHeight;

    console.log(height);

    $(document.getElementById('editViewTab:editTabScroll')).css('height', height - 150);
    $(document.getElementById('editViewTab:dictViewTabScroll')).css('height', height - 150);
    $(document.getElementById('editViewTab:scrollPaneldetailViewTab')).css('height', height - 150);


//    var y = document.getElementById("lexiconCreationTabViewForm").parentNode;
//  alert(y.offsetHeight);

    //var height = $(document.getElementById('lexiconCreationViewCenter')).height();

    //var bottomheight = $('#foot').height();
    $(document.getElementById('lexiconCreationTabViewForm:tabView:scrollPanelLemmaTree')).css('height', height - 320);
    $(document.getElementById('lexiconCreationTabViewForm:tabView:scrollPanelFormTree')).css('height', height - 270);
    $(document.getElementById('lexiconCreationTabViewForm:tabView:scrollPanelDocTree')).css('height', height - 380);
    $(document.getElementById('lexiconCreationTabViewForm:tabView:scrollPanelAttTree')).css('height', height - 320);
    $(document.getElementById('lexiconCreationTabViewForm:tabView:scrollPanelOntologyTree')).css('height', height - 300);
    $(document.getElementById('lexiconCreationTabViewForm:tabView:scrollPanelSenseTree')).css('height', height);
    // alert(height);
//    $(document.getElementById('lexiconCreationTabViewForm:tabView:scrollPanelLemmaTree')).css('height', height - 275);
//    $(document.getElementById('lexiconCreationTabViewForm:tabView:scrollPanelFormTree')).css('height', height - 220);
//    $(document.getElementById('lexiconCreationTabViewForm:tabView:scrollPanelDocTree')).css('height', height - 340);
//    $(document.getElementById('lexiconCreationTabViewForm:tabView:scrollPanelAttTree')).css('height', height - 275);
//    $(document.getElementById('lexiconCreationTabViewForm:tabView:scrollPanelOntologyTree')).css('height', height - 275);
//    $(document.getElementById('lexiconCreationTabViewForm:tabView:scrollPanelSenseTree')).css('height', height - 275);
    //  $(document.getElementById('editViewTab:layoutEditDictionaryView')).css('height', height);



    //  alert (height);
// alert($(document.getElementById('editViewTab:layoutEditDictionaryView')).css('height'));

    // alert(findFirstDescendant("layoutUnitLeftPanelCenter", "div").height());
    // alert(height * 0.4);
    //$(document.getElementById$('#lexiconCreationTabViewForm:tabView:scrollPanelLemmaTree')).css('paddingBottom', 100);
}
;

//window.addEventListener('resize', setHeight);
window.onresize = setHeight;
window.onload = setHeight;


$(document).ready(function () {
//    // alert($('body').height());
//    setHeight();
//    $(document.getElementById('editViewTab:layoutEditDictionaryView')).addEventListener("load", alert());

//    $(document.getElementById('editViewTab:layoutEditDictionaryView')).bind('mouseover',(function () {
//        alert(  );
//    }));

});
