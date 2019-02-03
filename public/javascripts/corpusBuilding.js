"use strict";

var mainWin = {};
var responseCache = {};
//var mainWin.map = null;
var layer = null;
var oms = null;
var normalIcon = null;
var altCat = 0;
var submissionCountPerSession = 0;
var geoCoder = null;
var stage = null;
//TODO: do you need a global variable for role?
var role = null;
var documentId = null;
var comments = null;
var activeInputText = null;
var submitAttempted = false;
var cssApplier = null;


$(window).on("load", function () {

    //make the two divs resizable.
    //TODO looks like there is a minimum width on one of the elements of the Map side, that makes things stack. search CSS and style 
    $("#rightPaneDiv").resizable();
    $(window).resize(function () {
        $('#leftPaneDiv').css('width', Math.floor($('.container-fluid').innerWidth() - $("#rightPaneDiv").innerWidth()));
    });

    //This will override the function used when setting jQuery UI dialog titles, allowing it to contain HTML.
    $.widget("ui.dialog", $.extend({}, $.ui.dialog.prototype, {
        _title: function (title) {
            if (!this.options.title) {
                title.html("&#160;");
            } else {
                title.html(this.options.title);
            }
        }
    }));

    $("#globalUnhighlightDivId, #uncertainTagDialog, #advancedSearchDiv").dialog({
        autoOpen: false,
    });

    mainWin.originalAdvancedSearchHtml = $("#advancedSearchDiv").clone();

    mainWin.map = new L.Map('map', {
        center: new L.LatLng(20, 15),
        zoom: 3,
        minZoom: 1,
        name: "OSM"
    });

    mainWin.osmOptions = { nearbyDistance: 35, circleSpiralSwitchover: "infinity", circleFootSeparation: 40, keepSpiderfied: true };
    oms = new OverlappingMarkerSpiderfier(mainWin.map, mainWin.osmOptions);

    normalIcon = new L.divIcon({
        className: 'divNormal'
    });

    mainWin.evalParams = {
        isGeogFocus: "isGeogFocus",
        uncertainLocation: "uncertainLocation",
        uncertainSemantics: "uncertainSemantics",
        impreciseLocation: "impreciseLocation",
        vagueLocation: "vagueLocation",
        representative: "representative"
        //hasSurrogate: "hasSurrogate"
        //surrogates are checked via .surrogate properties.
    };

    mainWin.altEvalParams = {
        isSurrogate: "isSurrogate"
    };

    //var osm = new L.TileLayer('http://tile.osm.org/{z}/{x}/{y}.png');
    var osm = new L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpejY4NXVycTA2emYycXBndHRqcmZ3N3gifQ.rJcFIG214AriISLbB6B5aw', {
        maxZoom: 18,
        attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
        '<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
        'Imagery © <a href="http://mapbox.com">Mapbox</a>',
        id: 'mapbox.light'
    });



    mainWin.map.fitWorld().addLayer(osm);
    //if (map.tap) map.tap.disable();
    //document.getElementById('map').style.cursor = 'default';
    mapManip.initializeAuxMaps();
    $(window).resize(function () {
        fitMapBounds();
        if (!$('#advancedMapBtn').is(":visible")) {//to see if we are in alternative veiew instead of overview.
            mapManip.resetAuxMaps();
            mapManip.initializeAuxMaps();
            if (responseCache.latest) mapManip.drawAuxMaps(responseCache.latest.generatedAlternates[activeInputText]);
        }
    });

    oms.addListener('spiderfy', function (markers) {
        // for (var i = 0, len = markers.length; i < len; i++) {
        //     //markers[i].setIcon(spideredIcon);
        //     //markers[i].closePopup();
        // }
        //mainWin.map.closePopup();
        layer.closePopup();
    });
    //oms.addListener('unspiderfy', function (markers) {
    //    for (var i = 0, len = markers.length; i < len; i++) {
    //        // markers[i].setIcon(new normalIcon());
    //        markers[i].setIcon(normalIcon);
    //    }
    //});
    $(document).ajaxStart(function () {
        $("#loadingImg").show();
        $('body').css('cursor', 'progress');
    });
    $(document).ajaxStop(function () {
        $("#loadingImg").hide();
        $('body').css('cursor', 'auto');
    });
    var historyCtrl = new L.HistoryControl({
        position: 'topleft',
        orientation: 'horizental',
        backImage: 'fa fa-chevron-left',
        forwardImage: 'fa fa-chevron-right',
        backText: '<',
        forwardText: '>'
    }).addTo(mainWin.map);
    responseCache.original = null;
    responseCache.latest = null;
    responseCache.history = [];
    responseCache.historyInput = [];
    responseCache.initialCallCounter = 0;
    responseCache.modifyCount = 0;
    // not used now, but would be good to keep track to see what component has
    // updated the response (in map, manual, text etc).
    responseCache.latestUpdater = null;
    // These are for each place, not for the whole response.
    responseCache.alternates = [];
    responseCache.tempAlternates = null;

    $.getJSON("http://freegeoip.net/json/?callback=?", function (data) {
        responseCache.geoIp = $.extend(true, {}, data);
    });

    // Define alternateController, for rendering 100 alternates // alternate Category (altCat)
    cssApplier = rangy.createClassApplier("selectedPlace", {
        ignoreWhiteSpace: true,
        normalize: false,
        tagNames: ["span", "a"],
        removeEmptyElements: true,
        removeEmptyContainers: true,
        elementProperties: {
            onmouseover: function () {
                var name = this.innerHTML.toString().toLowerCase();
                mainWin.flashMarker(name, true, "Text");
                mainWin.highlightTextOnMapHover(name, true);
            },
            onmouseout: function () {
                var name = this.innerHTML.toString().toLowerCase();
                mainWin.flashMarker(name, false, "Text");
                mainWin.highlightTextOnMapHover(name, false);
            }
        }
    });
    rangy.init();
    $("#rightPaneDiv").children().prop('disabled', true);

    $(document).keydown(utils.checkCtrlZ);
});


mainWin.flashMarker = function (name, isMouseOver, hoveredOnStr) {
    //either going to be in the main map, or one of the aux maps. Check layergroups one by one.
    if (layer !== null) {
        var layerObj = layer.getLayer(name);
    }
    if (typeof (layerObj) === "undefined" && mainWin.unlocatedMapLayerGroup) {
        layerObj = mainWin.unlocatedMapLayerGroup.getLayer(name);
        if (typeof (layerObj) === "undefined" && mainWin.notInGeoNamesMapLayerGroup) {
            console.log("reached here");
            layerObj = mainWin.notInGeoNamesMapLayerGroup.getLayer(name);
        }
    }

    if (layerObj === null || typeof (layerObj) === "undefined") return;

    var name = layerObj.feature.properties.name.toLowerCase();

    if (isMouseOver) {
        //Pan the map to the marker if it's out of view
        if (!mainWin.map.getBounds().contains(layerObj.getLatLng())) {
            //mainWin.map.panTo(layerObj.getLatLng());
            fitMapBounds();
        }
        //Change the marker icon
        layerObj.setIcon(L.divIcon({
            className: utils.getStyleClassName(layerObj.feature) + " divHoveredOn" + hoveredOnStr
        }));
        //Bring up the marker on the map if it's under others
        utils.offsetMarkerZIndex(layerObj);
        //layerObj.bringToFront(); might be a better way. Not tested.
        $(layerObj.label._container).addClass("leaflet-label-hoveredOn" + hoveredOnStr);
    } else {
        layerObj.setIcon(L.divIcon({
            className: utils.getStyleClassName(layerObj.feature)
        }));
        $(layerObj.label._container).removeClass("leaflet-label-hoveredOn" + hoveredOnStr);
    }


}


mainWin.OnToponymHover = function (e) {
    var layerObj = e.target;
    utils.offsetMarkerZIndex(layerObj);
    mainWin.highlightTextOnMapHover(layerObj._leaflet_id, true);
    mainWin.flashMarker(layerObj._leaflet_id, true, "Map");
}


mainWin.OnToponymHoverOut = function (e) {
    var layerObj = e.target;
    mainWin.highlightTextOnMapHover(layerObj._leaflet_id, false);
    mainWin.flashMarker(layerObj._leaflet_id, false, "Map");
    layer.resetStyle(e.target);
}


mainWin.highlightTextOnMapHover = function (name, isMouseOver) {
    $(".selectedPlace").each(function (index) {
        if ($(this).text().toLowerCase() === name) {
            if (isMouseOver) {
                $(this).addClass("hoveredOnMap");
            }
            else {
                $(this).removeClass("hoveredOnMap");
            }
        }
    });
}


// deletes all the features (removes the layer)
function resetMap() {
    //layer is actually a layerGroup, which has the clearLayers method, otherwise basemap would be removed as well.
    if (layer !== null && typeof (layer) !== 'undefined') {
        layer.clearLayers();
    }
    //This would only work if we could somehow do this except for basemap. There doesn't seem to be a way to distinguish that from the rest. 
    // mainWin.map.eachLayer(function (layer) {
    //     mainWin.map.removeLayer(layer);
    // });
    oms.clearMarkers();
    mapManip.resetAuxMaps();
}


function fitMapBounds() {
    //If there is no feature to be rendered on map, then just fitWorld
    var markers = 0;
    if (layer) {
        layer.eachLayer(function (l) {
            //l.bindPopup('Hello');
            markers++;
        });
    }
    if (markers === 0) {
        mainWin.map.fitWorld({ animate: false });
        return;
    }
    // If the bounding box is too small, make a bigger size, something like a
    // mid-sized country. All numbers are experimental
    else if (layer.getBounds().getNorthEast().distanceTo(
        layer.getBounds().getSouthWest()) < 50000) {
        var southWest = L.latLng(layer.getBounds().getSouthWest().lat - 3,
            layer.getBounds().getSouthWest().lng + 3);
        var northEast = L.latLng(layer.getBounds().getNorthEast().lat + 3,
            layer.getBounds().getNorthEast().lng - 3);
        var newBounds = L.latLngBounds(southWest, northEast);
        mainWin.map.fitBounds(newBounds, { padding: [15, 10], animate: false });
        //console.log("The modified zoom logic");
    } else if (layer.getBounds().getNorthEast().distanceTo(
        layer.getBounds().getSouthWest()) >= 50000) {
        mainWin.map.fitBounds(layer.getBounds(), { padding: [21, 15], animate: false });
        //console.log("Default getbounds");
    }
}


function processGeoTxtResponse(response, activeInput, drawMapAndTextDiv, addToHistory) {

    //Only add to history if there are other things done first.    
    if (addToHistory) {
        utils.addToHistory();
    }
    responseCache.latest.generatedAlternates[activeInput] = jQuery.extend(true, {}, response);

    if (!addToHistory) {   // This is an indication that this is the initial annotation. 
        responseCache.initialCallCounter++;
        if (responseCache.initialCallCounter === responseCache.original.inputTexts.length) {
            //todo: this should be handled via deferred and promises. there are 4 assign toponyms within this function, 
            //and one may get completed sooner than the other one. for now, bringing the title first to minimize the chances of clash.
            textManip.callEnrichUponLoad();
        }
    }
    if (drawMapAndTextDiv) { //this is for "make an initial annotation guess"
        resetMap();
        renderMap(response, mainWin.map);
        var elem = document.getElementById(activeInput + "TextDiv");
        textManip.highlightFromGeoJson(elem, response.features, true, rangy, cssApplier, activeInput);
    }
}

// Processes the data received after the aysonchronous HTTP GET call
function processResponse(responseJson) {
    document.getElementById("commentstTxt").value = "";
    responseCache.startTime = new Date();
    //responseJson.inputTexts[0] = he.encode("A Study of Plateaus in the Middle East: Mantle Physical Properties of Plateaus in Iran and Turkey");
    //responseJson.inputTexts[1] = he.encode("In this study we investigate crustal and uppermost mantle physical properties that characterize some of the continental plateaus of the Middle East. This is done as part of a larger effort to map and compare high-frequency wave propagation at regional distances across the earths continental plateaus. Thousands of short-period WWSSN seismograms recorded at stations located in the Middle East and produced by earthquakes with epicentral distances less than about 20° were examined visually in an effort to study lateral variations of high-frequency (0.5-2 Hz) seismic wave propagation across this area, particularly to the north of the zone of continental collision between the Africa-Arabian and Eurasian plates. Variations of frequencies and amplitudes of Sn and Lg relative to P are examined and mapped throughout the region, and this work is supplemented by a study of velocities of Pn, Sn, and Lg. Sn amplitude variations are very striking in this area. An important observation of this study is that Sn propagates efficiently beneath a major part of the Turkish and Iranian plateaus. Sn is strongly attenuated, however, in the northernmost portion of the plateaus south of the Caspian Sea and the Black Sea and in an area between the two seas. These regions are characterized, in general, by active tectonism, including volcanism, faulting, and folding. However, this active tectonism is not restricted to the areas of high Sn attenuation but appears to extend beneath other parts of the Iranian and Turkish plateaus. Patterns of lateral variations in the propagation of Lg are not as consistent as those for Sn. Lg propagates efficiently across Turkey, Iran, and adjacent regions, but the Lg waves that cross the Turkish and Iranian plateaus are weak and have relatively long predominant periods of about 2-5 s. The Lg phase is not observed when the path of propagation crosses the Caspian Sea, consistent with the evidence of oceanic-type crustal structure beneath these seas. Lg is also not observed from subcrustal events located in the proximity of the Hindu Kush Mountains. The velocity of Pn beneath most of the plateau areas, and particularly in the regions of Sn attenuation, ranges between 8.0 and 8.2 km/s. Velocities for Sn and Lg throughout the Middle East are about 4.5 and 3.4 km/s, respectively. It is possible to interpret the efficient Sn propagation and the relatively normal uppermost mantle P and S velocities beneath a major part of the Turkish-Iranian plateau as indicating a partial underthrusting of the Arabian continental plate beneath the Iranian and Turkish continental blocks. Alternatively, the mapped regions of efficient Sn propagation and high Sn attenuation may result from a differential cooling of a past thermal anomaly. In this case the isostatic compensation of the plateaus could be due to an anomalously hot uppermost mantle that developed behind the mid-to-upper Tertiary subduction zone(s) in this region and to a subsequent crustal shortening after the collision of the Arabian and Eurasian plates. Our results cannot uniquely confirm the validity of the above proposed models.");
    //responseJson.inputTexts[0] = "RT @ATT: Our &#x3E;$600M in disaster recovery supports people like the Moore, OK tornado victims #ATTimpact http://t.co/yy7JwNWXUg http://t.co/U&#x2026;";
    //responseJson.inputTexts[0]="This is a test document"
    //responseJson.inputTexts[1] = he.encode("Gaza is tagged with overlapping ambiguous, because the city, and the strip, and the administrative region share the same name and are spatially overlapping. Middle East is a region with boundaries that have different definitions, and therefore is tagged as vague. Eskan Towers do not exist in GeoNames, and therefore, the name is resolved to the containing city and tagged as representative.  Abbasabad is a very frequent place name in Iran, and without enough context, it is impossible to know which one it refers to. Therefore, it is tagged as non-overlapping ambiguous without surrogates (since too many exist). Astara, is the name of two neighboring towns across the border between Iran and Azerbaijan, and without enough context, it is impossible to know which one is referred to. Both are added as surrogates and tagged as non-overlapping ambiguous. Finally, in \"Istanbul protests against the offensive in Gaza\", Istanbul is tagged with uncertain semantics, since it is used as an adjective (noun adjunct).");
    for (var inputTextNum = 0; inputTextNum < responseJson.inputTexts.length; inputTextNum++) {
        var elem = document.getElementById(inputTextNum + "TextDiv");
        while (elem.hasChildNodes()) {
            elem.removeChild(elem.firstChild);
        }
        elem.innerHTML = responseJson.inputTexts[inputTextNum];
    }

    comments = null;
    activeInputText = 0;
    documentId = responseJson.documentId;

    stage = responseJson.allCodings.length + 1;

    responseCache.original = jQuery.extend(true, {}, responseJson);
    responseCache.latest = jQuery.extend(true, {}, responseJson);

    var updateInterface = false;
    for (var i = 0; i < responseCache.latest.generatedAlternates.length; i++) {
        //updateInterface = i === 0 ? true : false;
        if (jQuery.isEmptyObject(responseCache.latest.generatedAlternates[i]) || typeof (responseCache.latest.generatedAlternates[i].features) === "undefined" || responseCache.latest.generatedAlternates[i].features === null) {
            responseCache.latest.generatedAlternates[i] = new FeatureCollection();
            fireXhr(i, updateInterface, false);
        } else {
            //todo: uncomment this
            //responseCache.initialCallCounter++;
            //todo:remove this.
            fireXhr(i, updateInterface, false);
        }
    }

    if (responseCache.initialCallCounter === responseCache.original.inputTexts.length) {
        //todo: this should be handled via deferred and promises. there are two assign toponyms within this function, 
        //and one may get completed sooner than the other one. for now, bringing the title first to minimize the chances of clash.
        textManip.callEnrichUponLoad();
    }

    updateInfoDiv(true);
    $("#rightPaneDiv").children().prop('disabled', false);
}


// Fires an AJAX call to get Tweet from the DB
function fireXhrGetTweet() {

    resetUi();

    var e = document.getElementById("geoCoderDropDown");
    geoCoder = e.options[e.selectedIndex].value;

    if (geoCoder === "none") {
        alert("Select your name under GeoCoder Name: first.");
        return;
    }

    $('#cancelAltBtn, #advancedMapBtn').hide();
    document.getElementById('submitGcResultsButton').hidden = false;
    document.getElementById('skipGcResultsButton').disabled = false;


    var e1 = document.getElementById("roleDropDown");
    role = e1.options[e1.selectedIndex].value;

    submitAttempted = false;

    $.ajax({
        async: true,
        type: "GET",
        url: "tweetsRandom2/exposeCorpus.json?geoCoder="
        + geoCoder + "&role=" + role,
        success: function (results) {
            if (results.success === false) {
                //alert(results.cause.toString());
                console.log(results.cause.toString());
                //resetUi();
            } else {
                processResponse(results);
            }
        },
        error: function () {
            console.log("Get Document AJAX failed.");
            alert("Get Document AJAX failed.");
            resetMap();
            fitMapBounds();
            document.getElementById("tweetText").value = "";
            document.getElementById("commentstTxt").value = "";
        }
    });
}


function fireXhr(activeInput, updateInterface, addToHistory) {
    var e = document.getElementById("engineTextDropDown");
    var m = e.options[e.selectedIndex].value;
    var encodedText = null;
    var heDecoded = he.decode(responseCache.latest.inputTexts[activeInput])
    if (activeInput === undefined) {
        //Not in use
        encodedText = encodeURIComponent(he.decode(document.getElementById('tweetText').value));
    } else {
        //TODO: Should we decode things here or on the server?
        encodedText = encodeURIComponent(heDecoded);
        //TODO note: chanced true to false for the second param...
        resetHighlights(activeInput, false);
    }
    // $.ajax({
    //     async: true,
    //     type: "GET",
    //     url: "../api/geotxt.json?m="
    //     + m
    //     + "&q="
    //     + encodedText,
    //     success: function (results) {
    //         processGeoTxtResponse(results, activeInput);
    //     },
    //     error: function () {
    //         console.log("AJAX failed.");
    //         alert("AJAX failed.");
    //         resetMap();
    //         fitMapBounds();
    //     }
    // });
    var querytData = { m: m, q: heDecoded };
    $.ajax({
        async: true,
        updatedInterfaceProp: updateInterface,
        addToHistoryProp: addToHistory,
        type: "POST",
        url: "../api/geotxtpst.json",
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify(querytData),
        success: function (results) {
            processGeoTxtResponse(results, activeInput, this.updatedInterfaceProp, this.addToHistoryProp);
        },
        error: function () {
            console.log("AJAX failed.");
            alert("AJAX failed.");
            resetMap();
            fitMapBounds();
        }
    });
}


function activateInputItem(activeInput) {
    if (activeInput >= responseCache.original.inputTexts.length) {
        alert("No more items for this document exist, please submit the results if you are done geocoding.");
        return;
    }
    if (activeInput < 0) {
        alert("You are vewing the first document item.");
        return;
    }
    resetMap();
    //activeInput++;
    activeInputText = activeInput;
    document.getElementById("tweetText").value = responseCache.original.inputTexts[activeInput];
    document.getElementById(activeInput + "TextDiv").className = "workingDiv";

    $("#" + activeInput + "TextDiv, #map, #unlocatedMapDiv, #notInGeoNamesMapDiv").animate({
        borderTopColor: "purple",
        borderRightColor: "purple",
        borderBottomColor: "purple",
        borderLeftColor: "purple"
    }, 'slow');
    $("#" + activeInput + "TextDiv, #map, #unlocatedMapDiv, #notInGeoNamesMapDiv").animate({
        borderTopColor: "#8AC007",
        borderRightColor: "#8AC007",
        borderBottomColor: "#8AC007",
        borderLeftColor: "#8AC007"
    }, 'slow');
    //TODO change this logic! It works but what if you have more than two data items.
    //    if (activeInput - 1 < 0) {
    //        document.getElementById((activeInput + 1) + "TextDiv").className = "inactiveDiv";
    //    }
    //    else {
    //        document.getElementById((activeInput - 1) + "TextDiv").className = "inactiveDiv";
    //    }
    if (activeInput === 1) {
        $('#0TextDiv').attr('style', '').removeClass('workingDiv').addClass("inactiveDiv");
        $('#previousInputTextBtn').prop('disabled', false);
        $('#nextInputTextBtn').prop('disabled', true);
        resetHighlights(0, false);
        $('#0TextDiv').disableSelection();
        $('#1TextDiv').enableSelection();
        //document.getElementById("0TextDiv").className = "inactiveDiv";
    }
    else if (activeInput === 0) {
        $('#1TextDiv').attr('style', '').removeClass('workingDiv').addClass("inactiveDiv");
        $('#previousInputTextBtn').prop('disabled', true);
        $('#nextInputTextBtn').prop('disabled', false);
        $('#1TextDiv').disableSelection();
        $('#0TextDiv').enableSelection();
        resetHighlights(1, false);
        //document.getElementById("1TextDiv").className = "inactiveDiv";
    }
    //generatedAlternates[activeInput] will either have .features or an empty array as .features. Made sure in processResponse
    renderMap(responseCache.latest.generatedAlternates[activeInput], mainWin.map);
    var elem = document.getElementById(activeInput + "TextDiv");
    textManip.highlightFromGeoJson(elem, responseCache.latest.generatedAlternates[activeInput].features, true, rangy, cssApplier, activeInput);
    fitMapBounds();
}


function renderMap(fCollection, mapParam, noFitBounds) {

    $('#notInLocDbBtn').hide();
    mapManip.drawAuxMaps(fCollection);

    layer = L
        .geoJson(
        fCollection,
        {
            pointToLayer: function () {
                return utils.pointToLayer(arguments[0], arguments[1]);
            },
            style: function (feature) { //in leaflet1, clickable is renamed 
                if (feature.properties.type === 'aux') { return { color: "#ff7800", fillColor: "#000000", weight: 1, opacity: 1, fillOpacity: 1, clickable: false } }
            },
            onEachFeature: mapManip.mainOnEachFeature
        }).addTo(mapParam);
    if (!noFitBounds) fitMapBounds();

}


mainWin.renderSurrogates = function (positions, geoNameId) {

    var decodedPositions = JSON.parse(decodeURIComponent(positions));
    var decodedGeoNameID = JSON.parse(decodeURIComponent(geoNameId));

    var featureIndex = arrayObjectIndexOf(responseCache.latest.generatedAlternates[activeInputText].features,
        decodedPositions, "properties", "positions");

    var gid = responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties.geoNameId
    if (gid !== decodedGeoNameID) {
        alert("Identified alternate's GeoNameId doesn't match with the picked one. Please report problem to the developer");
        return;
    }

    // TODO make sure that fCollection is defined after this. Also, make sure
    // index is not -1
    var fCollection = jQuery.extend(true, {},
        responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties.surrogates);


    // Get the index of the picked alternate within the alternates object of the parent feature
    var mainFeatureIndex = arrayObjectIndexOf(fCollection.features, decodedGeoNameID, "properties", "geoNameId");

    //This is for interface use only. It doesn't get reocorded in the final object.
    fCollection.features[mainFeatureIndex].properties.isCurrentlyPicked = true;

    renderAlternates(fCollection.features);
};


mainWin.removeAll = function (positions, geoNameId) {
    var decodedPositions = JSON.parse(decodeURIComponent(positions));
    var decodedGeoNameID = JSON.parse(decodeURIComponent(geoNameId));
    utils.addToHistory();
    var featureIndex = arrayObjectIndexOf(responseCache.latest.generatedAlternates[activeInputText].features,
        decodedPositions, "properties", "positions");
    var gid = responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties.geoNameId
    if (gid !== decodedGeoNameID) {
        alert("Identified alternate's GeoNameId doesn't match with the picked one. Please report problem to the developer");
        return;
    }
    for (var i = 0; i < responseCache.latest.generatedAlternates.length; i++) {
        if (i !== activeInputText) {
            utils.removeFromFCollectionByName(responseCache.latest.generatedAlternates[activeInputText].features[featureIndex], responseCache.latest.generatedAlternates[i]);
        }
    }
    responseCache.latest.generatedAlternates[activeInputText].features.splice(featureIndex, 1);
    textManip.highlightFromGeoJson(document.getElementById(activeInputText + "TextDiv"), responseCache.latest.generatedAlternates[activeInputText].features, true, rangy, cssApplier, activeInputText);
    resetMap();
    renderMap(responseCache.latest.generatedAlternates[activeInputText], mainWin.map);
    fitMapBounds();
};


// TODO what if we have fewer than 100 alternates.
function prepareAlternates(positions, geoNameId, altCatParam, noFitBounds, showNotInDbBtn) {

    //showNotInDbBtn comes from mapManip.advancedSearchQuery

    var decodedPositions = JSON.parse(decodeURIComponent(positions));
    var decodedGeoNameID = JSON.parse(decodeURIComponent(geoNameId));

    var featureIndex = arrayObjectIndexOf(responseCache.latest.generatedAlternates[activeInputText].features,
        decodedPositions, "properties", "positions");

    var gid = responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties.geoNameId
    if (gid !== decodedGeoNameID) {
        alert("Identified alternate's GeoNameId doesn't match with the picked one. Please report problem to the developer");
        return;
    }

    $('#advancedMapBtn').click(function () {
        mapManip.advancedSearchDialogue(responseCache.latest.generatedAlternates[activeInputText].features[featureIndex])
    });

    // TODO make sure that fCollection is defined after this. Also, make sure
    // index is not -1
    var fCollection = jQuery.extend(true, {},
        responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties.alternates);

    if (fCollection.features.length !== 100) {
        console.log("Less than 100 alternates were returned for this place. Please beware to report any possible bug if it occures.");
    }



    // Get the index of the picked alternate within the alternates object of the parent feature
    var alternateFeatureIndex = arrayObjectIndexOf(fCollection.features, decodedGeoNameID, "properties", "geoNameId");

    //This is for interface use only. It doesn't get reocorded in the final object.
    fCollection.features[alternateFeatureIndex].properties.isCurrentlyPicked = true;

    for (var i = 0; i < 10; i++) {
        // Or use this JSON.parse(JSON.stringify(obj)) to clone the object
        responseCache.alternates[i] = jQuery.extend(true, {}, fCollection);
    }
    responseCache.tempAlternates = jQuery.extend(true, {}, fCollection);

    //used for "correct candidate" versus "correct candidate with surrogates"
    responseCache.tempAlternates2 = jQuery.extend(true, {}, fCollection);

    // Chunk 100 alternates into 10 categories for easier display. TODO: shoud I change these to jQuery extend?
    responseCache.alternates[0].features = responseCache.tempAlternates.features
        .splice(0, 10);
    responseCache.alternates[1].features = responseCache.tempAlternates.features
        .splice(0, 10);
    responseCache.alternates[2].features = responseCache.tempAlternates.features
        .splice(0, 10);
    responseCache.alternates[3].features = responseCache.tempAlternates.features
        .splice(0, 10);
    responseCache.alternates[4].features = responseCache.tempAlternates.features
        .splice(0, 10);
    responseCache.alternates[5].features = responseCache.tempAlternates.features
        .splice(0, 10);
    responseCache.alternates[6].features = responseCache.tempAlternates.features
        .splice(0, 10);
    responseCache.alternates[7].features = responseCache.tempAlternates.features
        .splice(0, 10);
    responseCache.alternates[8].features = responseCache.tempAlternates.features
        .splice(0, 10);
    responseCache.alternates[9].features = responseCache.tempAlternates.features
        .splice(0, 10);

    if (typeof altCatParam === 'undefined') {
        altCat = 0;
    }
    else {
        //if the function is being called from the mainWin.modifyAlternateProperty
        altCat = altCatParam;
    }
    renderAlternates(responseCache.alternates[altCat], noFitBounds, showNotInDbBtn);
}

// Render next category of alternates
function renderNext() {
    if (altCat >= 9) {
        alert("last page!");
        return;
    }
    altCat++;
    renderAlternates(responseCache.alternates[altCat]);
}


// Render previous category of alternates
function renderPrevious() {
    if (altCat <= 0) {
        alert("first page!");
        return;
    }
    altCat--;
    renderAlternates(responseCache.alternates[altCat]);
}


// render alternates
function renderAlternates(alternates, noFitBounds, showNotInDbBtn) {

    if (!showNotInDbBtn) {
        $('#cancelAltBtn, #advancedMapBtn').show();
        $("#cancelAltBtn, #advancedMapBtn").animate({
            width: '+=10px'
        });
        $("#cancelAltBtn, #advancedMapBtn").animate({
            width: '-=10px'
        });
    } else {
        $('#notInLocDbBtn, #advancedMapBtn').show();
        $("#notInLocDbBtn, #advancedMapBtn").animate({
            width: '+=10px'
        });
        $("#notInLocDbBtn, #advancedMapBtn").animate({
            width: '-=10px'
        });
    }
    document.getElementById('submitGcResultsButton').hidden = true;
    document.getElementById('skipGcResultsButton').disabled = true;
    document.getElementById('nextCatButton').hidden = false;
    document.getElementById('previousCatButton').hidden = false;
    $("#previousCatButton, #nextCatButton").animate({
        width: '+=70px',
        backgroundColor: 'red'
    });
    $("#previousCatButton, #nextCatButton").animate({
        width: '-=70px',
        backgroundColor: '#f0ad4e'
    });


    $("#rightPaneDiv").children().prop('disabled', true);

    var correctCandidate = 'Correct candidate', surrogateCounter = 0, fCollection = alternates;
    ;

    //TODO this if is checked because when coding stage X is picked in coding history, tempAlternates2 is not initialized. Fix this. 
    if (responseCache.hasOwnProperty("tempAlternates2")) {
        for (var i = 0; i < responseCache.tempAlternates2.features.length; i++) {
            if (responseCache.tempAlternates2.features[i].properties.isSurrogate === true) {
                ++surrogateCounter;
            }
        }
    }

    resetMap();


    layer = L
        .geoJson(
        fCollection,
        {
            pointToLayer: function () {
                return utils.pointToLayer(arguments[0], arguments[1]);
            },
            onEachFeature: function (feature, layer) {

                layer.on({
                    mouseover: mainWin.OnToponymHover,
                    mouseout: mainWin.OnToponymHoverOut
                });

                oms.addMarker(layer);

                var hierarchy = '', isSurrogate = '';

                for (var toponymInHierarchy = feature.properties.hierarchy.features.length - 1; toponymInHierarchy >= 0; toponymInHierarchy--) {
                    hierarchy = feature.properties.hierarchy.features[toponymInHierarchy].properties.toponym
                        + ", " + hierarchy;
                }
                hierarchy = "Toponym: " + feature.properties.toponym + ", " + hierarchy;
                hierarchy = hierarchy.substring(0, hierarchy.length - 2);

                if (feature.properties.isSurrogate === undefined
                    || feature.properties.isSurrogate === false) {
                    isSurrogate = "<b>Add surrogate</b>";
                    if (surrogateCounter === 0) {
                        correctCandidate = 'Correct candidate';
                    } else {
                        correctCandidate = 'Correct candidate along with ' + surrogateCounter + ' surrogate(s)';
                    }
                } else if (feature.properties.isSurrogate === true) {

                    correctCandidate = 'Correct candidate along with ' + (surrogateCounter - 1) + ' surrogate(s)';
                    isSurrogate = "<b>Remove surrogate</b>";
                }


                var popUpContent = hierarchy
                    + '<br>'
                    + '<button id="myLink" title="Correct Candidate" href="#"  class="btn btn-warning btn-xs" role="button" onclick="pickCandidateFromMap(\''
                    + encodeURIComponent(JSON
                        .stringify(feature.properties.positions))
                    + "\',\'"
                    + encodeURIComponent(JSON
                        .stringify(feature.properties.geoNameId))
                    + '\'); return false;"><b>' + correctCandidate + '</b></a>'
                    + "</button>"
                    + '<br>'
                    + 'Position: '
                    + feature.properties.positions
                    + '<br><button id="surrogateSel" title="Click if you think this is one of the correct surrogates for the place name." href="#"  class="btn btn-info " onclick="mainWin.modifyAlternateProperty(\''
                    + encodeURIComponent(JSON
                        .stringify(feature.properties.positions))
                    + '\',\''
                    + encodeURIComponent(JSON
                        .stringify(feature.properties.geoNameId))
                    + '\',\''
                    + 'isSurrogate'
                    + '\'); return false;">'
                    + isSurrogate
                    + '</button>';

                if (feature.properties.hasOwnProperty("featureCode")) {
                    popUpContent += '<br> FeatureCode: ' + utils.getFullFeatureCode(feature.properties.featureCode);
                }
                if (feature.properties.hasOwnProperty("geoNameId")) {
                    popUpContent += '<br><a target="_blank" href="http://www.geonames.org/' + feature.properties.geoNameId + '/">See on GeoNames</a> or <a target="_blank" href="http://api.geonames.org/get?geonameId=' + feature.properties.geoNameId + '&username=siddhartha&style=full">Check ID ' + feature.properties.geoNameId + '</a>';
                }

                layer.bindPopup(popUpContent);

                var labelContent = '';

                if (feature.properties.name.toLowerCase() === feature.properties.toponym
                    .toLowerCase()) {
                    labelContent = "<FONT COLOR=\"#000099\">"
                        + feature.properties.toponym
                        + "</FONT>";
                } else {
                    labelContent =
                        // "<FONT COLOR=\"#000099\">"
                        // + feature.properties.name.charAt(0)
                        // .toUpperCase()
                        // + feature.properties.name.slice(1)
                        // + "</FONT>" + "<br>"
                        // +
                        "<FONT COLOR=\"#990033\">"
                        + feature.properties.toponym
                        + "</FONT>";
                }
                layer.bindLabel(labelContent, {
                    noHide: true
                }).showLabel();
            }
        }).addTo(mainWin.map);

    if (noFitBounds === false || typeof noFitBounds === "undefined") {
        fitMapBounds();
    }
}


function modifyProperty(positions, geoNameId, action) {

    var decodedPositions = JSON.parse(decodeURIComponent(positions));
    var decodedGeoNameID = JSON.parse(decodeURIComponent(geoNameId));

    var featureIndex = arrayObjectIndexOf(responseCache.latest.generatedAlternates[activeInputText].features,
        decodedPositions, "properties", "positions");

    var gid = responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties.geoNameId
    if (gid !== decodedGeoNameID) {
        alert("Identified alternate's GeoNameId doesn't match with the picked one. Please report problem to the developer");
        return;
    }

    utils.addToHistory();

    var pickedFeature = jQuery.extend(true, {}, responseCache.latest.generatedAlternates[activeInputText].features[featureIndex]);

    if (responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties[mainWin.evalParams[action]] === false
        || responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties[mainWin.evalParams[action]] === undefined) {
        pickedFeature.properties[mainWin.evalParams[action]] = true;
    } else if (responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties[mainWin.evalParams[action]] === true) {
        delete pickedFeature.properties[mainWin.evalParams[action]];
    }

    // Update the latest responseCache with the pickedFeature
    responseCache.latest.generatedAlternates[activeInputText].features[featureIndex] = jQuery.extend(true, {}, pickedFeature);

    // if (action.toLowerCase() === "representative") {
    //     modifyProperty(positions, geoNameId, "vagueLocation");
    // }

    var tempPickedFeature = $.extend(true, {}, pickedFeature);
    //Multiple: what if there are multiple names? this should be revised, probably.
    for (var i = 0; i < responseCache.latest.generatedAlternates.length; i++) {
        if (i !== activeInputText) {
            var featureIndexByName = utils.getLocFeatureIndexByProperty(responseCache.latest.generatedAlternates[activeInputText].features[featureIndex], "name", responseCache.latest.generatedAlternates[i]);
            if (featureIndexByName >= 0) {
                var tempPos = $.extend(true, [], responseCache.latest.generatedAlternates[i].features[featureIndexByName].properties.positions);
                utils.updateFeaturePos(tempPickedFeature, tempPos);
                responseCache.latest.generatedAlternates[i].features.splice(featureIndexByName, 1);
                responseCache.latest.generatedAlternates[i].features.push(tempPickedFeature);
                utils.sortFeaturesOnFirstPos(responseCache.latest.generatedAlternates[i]);
            }
        }
    }

    //console.log(responseCache.latest);
    mainWin.map.closePopup();
    resetMap();
    renderMap(responseCache.latest.generatedAlternates[activeInputText], mainWin.map, true);
}


mainWin.modifyAlternateProperty = function (positions, geoNameId, action) {

    var decodedPositions = JSON.parse(decodeURIComponent(positions));
    var decodedGeoNameID = JSON.parse(decodeURIComponent(geoNameId));
    // Get the index of the parent feature
    var featureIndex = arrayObjectIndexOf(responseCache.latest.generatedAlternates[activeInputText].features,
        decodedPositions, "properties", "positions");
    // Get the index of the picked alternate within the alternates object of the parent feature
    var alternateFeatureIndex = arrayObjectIndexOf(
        responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties.alternates.features,
        decodedGeoNameID, "properties", "geoNameId");
    // TODO make sure that fCollection is defined after this. Also, make sure
    // index is not -1
    var pickedFeature = responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties.alternates.features[alternateFeatureIndex];
    if (pickedFeature.properties[mainWin.altEvalParams[action]] === false
        || pickedFeature.properties[mainWin.altEvalParams[action]] === undefined) {
        pickedFeature.properties[mainWin.altEvalParams[action]] = true;
    } else if (pickedFeature.properties[mainWin.altEvalParams[action]] === true) {
        delete pickedFeature.properties[mainWin.altEvalParams[action]];
    }
    mainWin.map.closePopup();
    resetMap();
    prepareAlternates(positions, responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties.geoNameId, altCat, true);
};


function pickCandidateFromMap(positions, geoNameId) {

    utils.addToHistory();

    var decodedPositions = JSON.parse(decodeURIComponent(positions));
    var decodedGeoNameID = JSON.parse(decodeURIComponent(geoNameId));

    // Get the index of the parent feature
    var featureIndex = arrayObjectIndexOf(responseCache.latest.generatedAlternates[activeInputText].features,
        decodedPositions, "properties", "positions");

    // Get the index of the picked alternate within the alternates object of the parent feature
    var alternateFeatureIndex = arrayObjectIndexOf(
        responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties.alternates.features,
        decodedGeoNameID, "properties", "geoNameId");

    var alternateFeatures = responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties.alternates.features;

    // TODO make sure that fCollection is defined after this. Also, make sure index is not -1
    var pickedFeature = jQuery.extend(true, {}, alternateFeatures[alternateFeatureIndex]);

    var surrogateFeatures = [], pickedAddedToSurrogates = false, shouldCallForceTags = false;

    //Add surrogates  and remove the pickedCandidate isSurrogate property.
    for (var i = 0; i < alternateFeatures.length; i++) {
        if (typeof (alternateFeatures[i].properties.isSurrogate) !== "undefined" || alternateFeatures[i].properties.isSurrogate === true) {
            surrogateFeatures.push(alternateFeatures[i]);
            if (pickedFeature.properties.geoNameId === alternateFeatures[i].properties.geoNameId) {
                pickedAddedToSurrogates = true;
                delete pickedFeature.properties.isSurrogate;
            }
        }
    }

    if (!pickedAddedToSurrogates && surrogateFeatures.length > 0) {
        responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties.alternates.features[alternateFeatureIndex].properties.isSurrogate = true;
        var pickedFeature2 = jQuery.extend(true, {}, pickedFeature);
        pickedFeature2.properties.isSurrogate = true;
        surrogateFeatures.push(pickedFeature2);
        //todo: is this even necessary?
        //delete pickedFeature.properties.isSurrogate;
    }

    //with the currenct setup, if a cfandidate is tagged as surrogate, the picked feature itself will for sure be added. Therefore, the only case 
    //that length ===1 would be when the surrogate itself is selected as candidate. 
    if (surrogateFeatures.length > 1) {
        pickedFeature.properties.surrogates = new FeatureCollection(surrogateFeatures);
        shouldCallForceTags = true;
    } else if (surrogateFeatures.length === 1 && surrogateFeatures[0].properties.geoNameId === pickedFeature.properties.geoNameId) {
        if (!confirm('Only one candidate is tagged as surrogate which is the same as the picked correct candidate. Surrogate will be removed. Do you want to proceed?')) {
            return;
        }
        delete responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties.alternates.features[alternateFeatureIndex].properties.isSurrogate;
        //at the moment it's redundent, because surrogates are only added to properties if length > 1
        delete pickedFeature.properties.surrogates;
    }

    // Preserve the alternates in the newly picked feature
    pickedFeature.properties.alternates = jQuery.extend(true, {},
        responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties.alternates);

    // Preserve the evalParams such as isGeogFocus if it's already picked
    for (var key in mainWin.evalParams) {
        if (mainWin.evalParams.hasOwnProperty(key)) {
            if (responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties[mainWin.evalParams[key]]) {
                pickedFeature.properties[mainWin.evalParams[key]] = responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties[mainWin.evalParams[key]];
            }
        }
    }

    $('#cancelAltBtn, #advancedMapBtn').hide();
    document.getElementById('submitGcResultsButton').hidden = false;
    document.getElementById('skipGcResultsButton').disabled = false;
    document.getElementById('nextCatButton').hidden = true;
    document.getElementById('previousCatButton').hidden = true;
    $("#rightPaneDiv").children().prop('disabled', false);

    resetMap();

    var tempPickedFeature = $.extend(true, {}, pickedFeature);
    //Multiple: what if there are multiple names? this should be revised, probably.
    for (var i = 0; i < responseCache.latest.generatedAlternates.length; i++) {
        if (i !== activeInputText) {
            var featureIndexByName = utils.getLocFeatureIndexByProperty(responseCache.latest.generatedAlternates[activeInputText].features[featureIndex], "name", responseCache.latest.generatedAlternates[i]);
            if (featureIndexByName >= 0) {
                var tempPos = $.extend(true, [], responseCache.latest.generatedAlternates[i].features[featureIndexByName].properties.positions);
                utils.updateFeaturePos(tempPickedFeature, tempPos);
                responseCache.latest.generatedAlternates[i].features.splice(featureIndexByName, 1);
                responseCache.latest.generatedAlternates[i].features.push(tempPickedFeature);
                utils.sortFeaturesOnFirstPos(responseCache.latest.generatedAlternates[i]);
            }
        }
    }
    responseCache.latest.generatedAlternates[activeInputText].features[featureIndex] = jQuery.extend(true, {}, pickedFeature);
    utils.sortFeaturesOnFirstPos(responseCache.latest.generatedAlternates[activeInputText]);
    //console.log(responseCache.latest);
    renderMap(responseCache.latest.generatedAlternates[activeInputText], mainWin.map);
    if (shouldCallForceTags) {
        mainWin.forceUncertainTag(positions, pickedFeature.properties.geoNameId);
    }
}


mainWin.forceUncertainTag = function (positions, decodedGeoNameId) {

    var decodedPositions = JSON.parse(decodeURIComponent(positions));

    var featureIndex = arrayObjectIndexOf(responseCache.latest.generatedAlternates[activeInputText].features, decodedPositions, "properties", "positions");

    var uncertainParams = {
        uncertainLocation: "uncertainLocation",
        impreciseLocation: "impreciseLocation"
    };

    var mustCallModifyProperty = true;

    for (var key in uncertainParams) {
        if (uncertainParams.hasOwnProperty(key)) {
            //if already uncertainLocation or impreciseLocaiton, no need to force the user to tag
            if (responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties[uncertainParams[key]] === true) {
                mustCallModifyProperty = false;
            }
        }
    }

    if (mustCallModifyProperty) {
        $("#uncertainTagDialog").dialog({
            modal: true,
            //title: 'Continue?',
            closeOnEscape: false,
            //position: [100, 100],
            open: function (event, ui) {
                $(".ui-dialog-titlebar-close", ui.dialog | ui).hide();
            },
            close: function () {
                $(this).dialog('close');
            },
            show: {
                effect: "blind",
                duration: 300
            },
            hide: {
                effect: "explode",
                duration: 300
            },
            buttons: {
                'Overlapping ambiguous': function () {
                    modifyProperty(positions, encodeURIComponent(decodedGeoNameId), 'impreciseLocation');
                    $(this).dialog('close');
                },
                'Non-overlapping ambiguous': function () {
                    modifyProperty(positions, encodeURIComponent(decodedGeoNameId), 'uncertainLocation');
                    $(this).dialog('close');
                }
            }
        });
        $("#uncertainTagDialog").dialog("open");
    }
};

//shouldn't be in use.
function pickNoCandidate(positions) {

    $('#cancelAltBtn, #advancedMapBtn').hide();
    document.getElementById('submitGcResultsButton').hidden = false;
    document.getElementById('skipGcResultsButton').disabled = false;
    document.getElementById('nextCatButton').hidden = true;
    document.getElementById('previousCatButton').hidden = true;
    $("#rightPaneDiv").children().prop('disabled', false);


    // reset Map
    resetMap();

    var decodedPositions = JSON.parse(decodeURIComponent(positions));

    // Get the index of the  feature
    var featureIndex = arrayObjectIndexOf(responseCache.latest.generatedAlternates[activeInputText].features,
        decodedPositions, "properties", "positions");

    // Make sure index is not -1
    var pickedFeature = jQuery
        .extend(
        true,
        {},
        responseCache.latest.generatedAlternates[activeInputText].features[featureIndex]);

    responseCache.latest.generatedAlternates[activeInputText].containsNoToponymPlace = true;
    pickedFeature.geometry = null;
    pickedFeature.noToponym = true;

    pickedFeature.properties = {};
    pickedFeature.properties.name = responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties.name;
    pickedFeature.properties.type = responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties.type;
    pickedFeature.properties.positions = responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties.positions;
    pickedFeature.properties.toponym = "noToponymFound";
    pickedFeature.properties.geoNameId = "noGeonameId";
    //pickedFeature.properties.isGeogFocus = responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties.isGeogFocus;

    // Preserve the evalParams such as isGeogFocus if it's already picked
    for (var key in mainWin.evalParams) {
        if (mainWin.evalParams.hasOwnProperty(key)) {
            if (responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties[mainWin.evalParams[key]]) {
                pickedFeature.properties[mainWin.evalParams[key]] = responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties[mainWin.evalParams[key]];
            }
        }
    }

    //alert('Selected toponym was removed and marked as NoCandidate. You may submit the results if other places are correctly geolocated.');

    // Update the latest responseCache with the pickedFeature
    responseCache.latest.generatedAlternates[activeInputText].features[featureIndex] = jQuery.extend(true, {}, pickedFeature);

    console.log(JSON.stringify(responseCache.latest.generatedAlternates[activeInputText]));

    renderMap(responseCache.latest.generatedAlternates[activeInputText], mainWin.map);
}


mainWin.cancelSelectAlternate = function () {
    $('#cancelAltBtn, #advancedMapBtn, notInLocDbBtn').hide();
    document.getElementById('submitGcResultsButton').hidden = false;
    document.getElementById('skipGcResultsButton').disabled = false;
    document.getElementById('nextCatButton').hidden = true;
    document.getElementById('previousCatButton').hidden = true;
    $("#rightPaneDiv").children().prop('disabled', false);
    resetMap();
    renderMap(responseCache.latest.generatedAlternates[activeInputText], mainWin.map);
};


function subtmitGcResults(isSkipped) {

    var results = {};

    results.timeDif = Math.abs((new Date() - responseCache.startTime) / 1000);
    if (typeof (responseCache.geoIp) === 'undefined') alert("geoIp is undefined!");
    results.geoIp = responseCache.geoIp;
    console.log("GeoIp is: ")
    console.log(results.geoIp);
    //evaluateResults()

    if (activeInputText < responseCache.original.inputTexts.length - 1) {
        alert("Please press the 'See Title On the Map & Activate Title Highlighting' button first, complete geocoding the abstract first and then submit the document.");
        return;
    }


    if (isSkipped) {
        var commented = confirm("Please click on OK only if you intend to skip this document. All GeoCoding results will still be reocrded with a special 'skipped' flag.");
        if (commented === false) {
            return;
        } else if (document.getElementById("commentstTxt").value === "") {
            alert("Please leave a comment explaining the reason why you are skipping this document.");
            return;
        }
        results.skipped = true;
    } else {
        results.skipped = false;
    }


    var geoCodedNoAlternates = jQuery.extend(true, [], responseCache.latest.generatedAlternates);

    var taggedUncertain = false;

    for (var activeIndex = 0; activeIndex < responseCache.original.inputTexts.length; activeIndex++) {
        if (geoCodedNoAlternates[activeIndex].features !== undefined) {
            for (var count = 0; count < geoCodedNoAlternates[activeIndex].features.length; count++) {
                //Uncomment this if you want to remove alternates before submitting.
                //geoCodedNoAlternates[activeIndex].features[count].properties.alternates = {};

                for (var key in mainWin.evalParams) {
                    if (mainWin.evalParams.hasOwnProperty(key) && key !== "isGeogFocus") {
                        if (geoCodedNoAlternates[activeIndex].features[count].properties[mainWin.evalParams[key]] === true) {
                            taggedUncertain = true;
                        }
                    }
                }
            }
        } else {
            geoCodedNoAlternates[activeIndex] = new FeatureCollection([]);
        }
        geoCodedNoAlternates[activeIndex].humanGeoCoded = true;
        //Not in use. Kept for memorizing the overridesAmt property
        // if (activeIndex === 1 && changedActiveInput1 === true)
        // {
        //     geoCodedNoAlternates[activeIndex].overridesAmt = true;
        // }
    }

    if (taggedUncertain === true && document.getElementById("commentstTxt").value === "") {
        alert("You have used a tag. Please leave a comment explaining the reason before attempting to submit or skip the results.");
        return;
    }

    geoCodedNoAlternates = geoCodingEval.consolidateCommonPlaces(geoCodedNoAlternates);

    results.stage = stage;

    results.modifyCount = responseCache.modifyCount;

    results.geoCodedGeoJson = geoCodedNoAlternates;
    // results.geoCoder = geoCoder;
    results.geoCoder = geoCoder;

    results.role = role;

    results.documentId = documentId;

    results.comments = document.getElementById("commentstTxt").value;

    results.browserInfo = utils.getBrowserInfo();

    //results.tweetFinalText = document.getElementById("tweetText").value;

    results.allCodings = jQuery.extend(true, [], responseCache.original.allCodings);

    var tempResults = jQuery.extend(true, {}, results);

    results.allCodings.unshift(tempResults);

    delete results.allCodings[0].allCodings;

    //results.finalGCResults and results.updatedGCStatus are set in this function:
    results = evalSubmissions(results);

    submitAttempted = true;

    if (results === -1) {
        return;
    }

    var postDataJson = { annotation: results };

    console.log(JSON.stringify(results));

    $.ajax({
        async: true,
        type: "POST",
        url: "tweetsRandom2/submitGcResults.json",
        // dataType: "application/x-www-form-urlencoded", //This is the default
        // processData: false, //Default for String
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify(postDataJson),
        success: function (result) {
            if (result.success === true) {
                onSubmitResults();
            } else if (result.success === false) {
                alert("Server could not process the submission.");
            }
        },
        error: function () {

            console.log("Submit (or skip) AJAX failed.");
            alert("Submit (or skip) AJAX failed.");
        }
    });
}


function onSubmitResults() {
    submitAttempted = false;
    resetMap();
    submissionCountPerSession++;
    document.getElementById("commentstTxt").value = "";
    fireXhrGetTweet();
    return true;
}


function resetUi() {
    if (responseCache.latest) {
        activateInputItem(0);
    }
    rangy.getSelection().removeAllRanges();
    resetMap();
    fitMapBounds();
    updateInfoDiv(true, true);
    document.getElementById("tweetText").value = "";
    document.getElementById("commentstTxt").value = "";
    //You may add a parameter to the function that shows if you want to reset the contents as well (objects such as responseCache)
    if (responseCache.latest) {
        for (var inputTextNum = 0; inputTextNum < responseCache.latest.inputTexts.length; inputTextNum++) {
            var elem = document.getElementById(inputTextNum + "TextDiv");
            while (elem.hasChildNodes()) {
                elem.removeChild(elem.firstChild);
            }
        }
    }
    submitAttempted = false;

    var tempGeoIp = $.extend(true, {}, responseCache.geoIp);
    responseCache = {};
    responseCache.geoIp = tempGeoIp;
    responseCache.original = null;
    responseCache.history = [];
    responseCache.historyInput = [];
    responseCache.initialCallCounter = 0;
    responseCache.modifyCount = 0;
    responseCache.latest = null;
    responseCache.latestUpdater = null;
    responseCache.alternates = [];
    responseCache.tempAlternates = null;


    activeInputText = null;

    altCat = 0;

    comments = null;
    activeInputText = 0;
    documentId = null;
    stage = null;
    activeInputText = null;
    $("#rightPaneDiv").children().prop('disabled', true);
    $('#cancelAltBtn, #advancedMapBtn').hide();
    document.getElementById('submitGcResultsButton').hidden = true;
    //$("#submitGcResultsButton").addClass('hidden');
    document.getElementById('skipGcResultsButton').disabled = true;
    document.getElementById('nextCatButton').hidden = true;
    document.getElementById('previousCatButton').hidden = true;

}


