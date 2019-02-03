"use strict";

var mapManip = {};

mapManip.drawAuxMaps = function (fCollectionParam) {

    var fCollection = $.extend(true, {}, fCollectionParam);
    if (!$.isArray(fCollection.features) || !fCollection.features.length > 0) {
        return;
    }
    var unlocatedFCollection = new FeatureCollection();

    var geomNeedCount = 0;
    $.each(fCollection.features, function (i, v) {
        if ((v.properties.type === "location" && (typeof (v.geometry) === 'undefined' || v.geometry === null)) && !v.properties.notInGeoNames) {
            geomNeedCount++;
            v.properties.geoNameId = "g"; //to make map functions and consolidation work.
            utils.addFeatureToFCollectionByName($.extend(true, {}, v), unlocatedFCollection);
        }
    });
    if (unlocatedFCollection.features.length > 0) {
        var auxFCollection = mapManip.generateTempGeometries(unlocatedFCollection, geomNeedCount);
        $('#unlocatedMapDiv').css("background-color", "#f1948a");
        mapManip.renderAuxMap(auxFCollection, mainWin.unlocatedMap, "unlocatedMapLayerGroup");
    } else {
        $('#unlocatedMapDiv').css("background-color", "#ddd");
    }

    var notInDbGeomNeedCount = 0;
    var notInGeoNamesFCollection = new FeatureCollection();
    $.each(fCollection.features, function (i, v) {
        if (v.properties.notInGeoNames) {
            notInDbGeomNeedCount++;
            v.properties.geoNameId = "n";
            utils.addFeatureToFCollectionByName($.extend(true, {}, v), notInGeoNamesFCollection);
        }
    });
    if (notInGeoNamesFCollection.features.length > 0) {
        var aux2FCollection = mapManip.generateTempGeometries(notInGeoNamesFCollection, notInDbGeomNeedCount);
        $('#notInGeoNamesMapDiv').css("background-color", "#f5cba7 ");
        mapManip.renderAuxMap(aux2FCollection, mainWin.notInGeoNamesMap, "notInGeoNamesMapLayerGroup");
    } else {
        $('#notInGeoNamesMapDiv').css("background-color", "#ddd");
    }
};


mapManip.resetAuxMaps = function () {
    mainWin.unlocatedMap.eachLayer(function (layer) {
        mainWin.unlocatedMap.removeLayer(layer);
    });
    mainWin.notInGeoNamesMap.eachLayer(function (layer) {
        mainWin.notInGeoNamesMap.removeLayer(layer);
    });
    mainWin.unlocatedMapLayerGroup && mainWin.unlocatedMapLayerGroup.clearLayers();
    mainWin.notInGeoNamesMapLayerGroup && mainWin.notInGeoNamesMapLayerGroup.clearLayers();
    $('#unlocatedMapDiv, #notInGeoNamesMapDiv').css("background-color", "#ddd");
};

//not used
mapManip.getGeoJsonBounds = function (geoJsonFCollection) {
    var leafLayer = L.geoJson(geoJsonFCollection);
    return mapManip.getLayerBounds(leafLayer);
};

//not used
mapManip.getLayerBounds = function (leafLayer) {
    //you should unity this with the fitmapbounds in the main win and coding history window
    var respBounds = {};
    if (leafLayer.getBounds().getNorthEast().distanceTo(leafLayer.getBounds().getSouthWest()) < 50000) {
        var southWest = L.latLng(leafLayer.getBounds().getSouthWest().lat - 3, leafLayer.getBounds().getSouthWest().lng + 3);
        var northEast = L.latLng(leafLayer.getBounds().getNorthEast().lat + 3, leafLayer.getBounds().getNorthEast().lng - 3);
        respBounds = L.latLngBounds(southWest, northEast);
    } else if (leafLayer.getBounds().getNorthEast().distanceTo(leafLayer.getBounds().getSouthWest()) >= 50000) {
        respBounds = leafLayer.getBounds();
    }
    return respBounds;
};


mapManip.generateTempGeometries = function (fCollectionParam, geomNeedCount) {
    var fCollection = $.extend(true, {}, fCollectionParam);
    var b = mainWin.unlocatedMap.getBounds();
    var respFCollection = new FeatureCollection();
    // var boundCorners = [b.getNorthWest(), b.getNorthEast(), b.getSouthEast(), b.getSouthWest()];
    // var jsonCoor = [];
    // $.each(boundCorners, function (index, value) {
    //     jsonCoor.push([value.lng, value.lat]);
    // });
    // var rect = {
    //     "type": "Feature",
    //     "properties": {
    //         "type": "aux",
    //         "name": "Feature",
    //         "toponym": "Feature",
    //         "alternates": new FeatureCollection(),
    //         "positions": [0]
    //     },
    //     "geometry": {
    //         "type": "Polygon",
    //         "coordinates": [jsonCoor]
    //     }
    // }
    // respFCollection.features.push(rect);
    var latRef = b.getSouthEast().lat + (b.getNorthEast().lat - b.getSouthEast().lat) * 0.1;
    var lngRef = b.getNorthWest().lng;
    var lngFirstIncrement = (b.getNorthEast().lng - b.getNorthWest().lng) * 0.13;
    //var lngIncrement = ((b.getNorthEast().lng - b.getNorthWest().lng) - lngFirstIncrement) / (geomNeedCount + 1);
    var lngIncrement = ((b.getNorthEast().lng - b.getNorthWest().lng)) / (geomNeedCount + 1);
    var counter = 0;
    $.each(fCollection.features, function (i, v) {
        lngRef = counter === 0 ? lngRef + lngFirstIncrement : lngRef + lngIncrement
        //lngRef += lngIncrement;
        v.geometry = new textManip.FeautrePointGeometry(lngRef, latRef);
        utils.addFeatureToFCollectionByName($.extend(true, {}, v), respFCollection);
        counter++;
    });
    return respFCollection;
};

//uses global activeInputText
mapManip.invokeAdvancedDialogue = function (encodedPosArray, encodedGeoNameId) {
    var decodedPositions = JSON.parse(decodeURIComponent(encodedPosArray));
    var decodedGeoNameID = JSON.parse(decodeURIComponent(encodedGeoNameId));
    var featureIndex = arrayObjectIndexOf(responseCache.latest.generatedAlternates[activeInputText].features, decodedPositions, "properties", "positions");
    var gid = responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties.geoNameId
    if (gid !== decodedGeoNameID && decodedGeoNameID !== 'g') {
        alert("Identified alternate's GeoNameId doesn't match with the picked one. Please report problem to the developer");
        return;
    }
    mapManip.advancedSearchDialogue(responseCache.latest.generatedAlternates[activeInputText].features[featureIndex]);
};


mapManip.advancedSearchDialogue = function (featureParam) {
    var feature = $.extend(true, {}, featureParam);
    var originalContent;
    var titleText = 'Advanced Search for <span class="selectedPlace">' + featureParam.properties.name + '</span>';
    utils.populateCountryDropdown();
    $("#advancedSearchDiv").dialog({
        modal: true,
        title: titleText,
        maxWidth: 700,
        maxHeight: 460,
        width: 600,
        height: 435,
        open: function (event, ui) {
            originalContent = $(this).html();
        },
        close: function (event, ui) {
            $(this).html(originalContent);
            $(this).dialog('close');
        }, show: {
            effect: "blind",
            duration: 200
        }, hide: {
            effect: "explode",
            duration: 100
        }, buttons: {
            'Search Location Database': function () {
                mapManip.initiateAdvancedSearchQuery(feature);
                $(this).html(originalContent);
                $(this).dialog('close');
            }, 'Cancel': function () {
                $(this).html(originalContent);
                $(this).dialog('close');
            }
        }, keypress: function (e) {
            if (e.keyCode == $.ui.keyCode.ENTER) {
                mapManip.initiateAdvancedSearchQuery(feature);
                $(this).html(originalContent);
                $(this).dialog('close');
            }
        }
    });
    $('#advancedSearchDiv').keypress(function (e) {
        if (e.keyCode == $.ui.keyCode.ENTER) {
            mapManip.initiateAdvancedSearchQuery(feature);
            $(this).html(originalContent);
            $(this).dialog('close');
        }
    });
    $("#advancedSearchDiv").dialog("open");
};

//uses global activeInputText
mapManip.initiateAdvancedSearchQuery = function (featureParam) {

    //if q is empty or null or all spaces, use the original name.

    var featureClass = $("#fClassDropId").val();
    var continent = $("#continentDropId").val();
    var country = $("#countryDropId").val();

    var queryObj = {};
    //should I URL encode this?!
    queryObj.q = $("#typeInNameId").val();
    //queryObj.m = $("#engineTextDropDown").val()

    queryObj.geoBoost = {};

    if (featureClass !== 'none') {
        queryObj.geoBoost.featureClass[featureClass] = 50;
    }

    // if (continent !== 'none') { queryObj.geoBoost.continent = { [continent]: 50 }; }
    // if (country !== 'none') { queryObj.geoBoost.country = { [country]: 50 }; }
    if ($("#populationBoostCheckId").is(":checked")) { queryObj.geoBoost.numericalBoosts = { "population": 100 }; }

    //console.log(JSON.stringify(queryObj));

    //guery geotxt
    //replace the fclss in the current inputs and others (get by name)
    //render map
    //add to history
    mapManip.advancedSearchQuery(queryObj, featureParam)
};

//uses global activeInputText
mapManip.advancedSearchQuery = function (queryObj, featureParam) {

    $.ajax({
        posArray: featureParam.properties.positions,
        placeName: featureParam.properties.name,
        type: "GET",
        url: "../api/geotxt.json?m=geocoder"
        + "&q="
        + encodeURIComponent(queryObj.q),
        success: function (results) {
            if (results.features.length > 1) {
                alert("Warning: more than one feature was returned for this name. Contact the developer.");
            }
            utils.updateFeaturePos(results.features[0], this.posArray);
            results.features[0].properties.name = this.placeName;
            results.features[0].properties.typedIn = queryObj.q;
            for (var a = 0; a < results.features[0].properties.alternates.features.length; a++) {
                results.features[0].properties.alternates.features[a].properties.typedIn = queryObj.q;
                results.features[0].properties.alternates.features[a].properties.name = this.placeName;
            }
            //get the index of the feature in generatedAlternates
            var featureIndex = arrayObjectIndexOf(responseCache.latest.generatedAlternates[activeInputText].features, this.posArray, "properties", "positions");
            // Preserve the evalParams such as isGeogFocus if it's already picked
            for (var key in mainWin.evalParams) {
                if (mainWin.evalParams.hasOwnProperty(key)) {
                    if (responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties[mainWin.evalParams[key]]) {
                        results.features[0].properties[mainWin.evalParams[key]] = responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties[mainWin.evalParams[key]];
                    }
                }
            }
            var tempRetreivedFeature = $.extend(true, {}, results.features[0]);
            //Multiple: what if there are multiple names? this should be revised, probably.
            for (var i = 0; i < responseCache.latest.generatedAlternates.length; i++) {
                if (i !== activeInputText) {
                    var featureIndexByName = utils.getLocFeatureIndexByProperty(responseCache.latest.generatedAlternates[activeInputText].features[featureIndex], "name", responseCache.latest.generatedAlternates[i]);
                    if (featureIndexByName >= 0) {
                        var tempPos = $.extend(true, [], responseCache.latest.generatedAlternates[i].features[featureIndexByName].properties.positions);
                        utils.updateFeaturePos(tempRetreivedFeature, tempPos);
                        responseCache.latest.generatedAlternates[i].features.splice(featureIndexByName, 1);
                        responseCache.latest.generatedAlternates[i].features.push(tempRetreivedFeature);
                        utils.sortFeaturesOnFirstPos(responseCache.latest.generatedAlternates[i]);
                    }
                }
            }
            responseCache.latest.generatedAlternates[activeInputText].features[featureIndex] = jQuery.extend(true, {}, results.features[0]);
            utils.sortFeaturesOnFirstPos(responseCache.latest.generatedAlternates[activeInputText]);

            var feature = responseCache.latest.generatedAlternates[activeInputText].features[featureIndex];

            prepareAlternates(encodeURIComponent(JSON.stringify(feature.properties.positions)), encodeURIComponent(JSON.stringify(feature.properties.geoNameId)), 0, false, true);
            //resetMap();

            $('#notInLocDbBtn').click(function () {
                mapManip.declareNotInGeoNames(featureParam.properties.positions);
            });
            //make sure they're not in DB in other inputs too

            //renderMap(responseCache.latest.generatedAlternates[activeInputText], mainWin.map);
            //fitMapBounds();


            //remove the "cancel and keep original button". Bind the charoffset to it?!
            //maybe color that green... and color the not in the DB red

            //make the "not in the databse button appear here. Warn the user too."

            //write on the map as to what's being seen

        }, error: function () {
            console.log("AJAX failed.");
            alert("AJAX failed.");
            resetMap();
            fitMapBounds();
        }
    });


};

//uses global active Input Text... or use feature param insetad of positions to change all features named that in all inputs.
mapManip.declareNotInGeoNames = function (posArrayParam) {

    var featureIndex = arrayObjectIndexOf(responseCache.latest.generatedAlternates[activeInputText].features, posArrayParam, "properties", "positions");
    // // Preserve the evalParams such as isGeogFocus if it's already picked
    // for (var key in mainWin.evalParams) {
    //     if (mainWin.evalParams.hasOwnProperty(key)) {
    //         if (responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties[mainWin.evalParams[key]]) {
    //             results.features[0].properties[mainWin.evalParams[key]] = responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties[mainWin.evalParams[key]];
    //         }
    //     }
    // }
    // var tempRetreivedFeature = $.extend(true, {}, results.features[0]);
    // //Multiple: what if there are multiple names? this should be revised, probably.
    // for (var i = 0; i < responseCache.latest.generatedAlternates.length; i++) {
    //     if (i !== activeInputText) {
    //         var featureIndexByName = utils.getLocFeatureIndexByProperty(responseCache.latest.generatedAlternates[activeInputText].features[featureIndex], "name", responseCache.latest.generatedAlternates[i]);
    //         if (featureIndexByName >= 0) {
    //             var tempPos = $.extend(true, [], responseCache.latest.generatedAlternates[i].features[featureIndexByName].properties.positions);
    //             utils.updateFeaturePos(tempRetreivedFeature, tempPos);
    //             responseCache.latest.generatedAlternates[i].features.splice(featureIndexByName, 1);
    //             responseCache.latest.generatedAlternates[i].features.push(tempRetreivedFeature);
    //             utils.sortFeaturesOnFirstPos(responseCache.latest.generatedAlternates[i]);
    //         }
    //     }
    // }
    responseCache.latest.generatedAlternates[activeInputText].features[featureIndex].properties.notInGeoNames = true;
    activateInputItem(activeInputText);

    //other inputs?
    mainWin.cancelSelectAlternate();

};


mapManip.renderAuxMap = function (fCollection, mapParam, nameResolver) {
    mainWin[nameResolver] = L.geoJson(fCollection, {
        pointToLayer: function () {
            return utils.pointToLayer(arguments[0], arguments[1]);
        },
        onEachFeature: mapManip.auxMapOnEachFeature
    }).addTo(mapParam);
};


mapManip.initializeAuxMaps = function () {

    if (mainWin.unlocatedMap != null) {
        mainWin.unlocatedMap.remove();
        mainWin.unlocatedMap = null;
    } if (mainWin.notInGeoNamesMap != null) {
        mainWin.notInGeoNamesMap.remove();
        mainWin.notInGeoNamesMap = null;
    }

    var southWest = L.latLng(39.7, 27.8);
    var northEast = L.latLng(42.2, 62.3);
    var bounds = L.latLngBounds(southWest, northEast);
    mainWin.unlocatedMap = new L.Map('unlocatedMapDiv', {
        //maxBounds: bounds,
        attributionControl: false
    });
    mainWin.notInGeoNamesMap = new L.Map('notInGeoNamesMapDiv', {
        //maxBounds: bounds,
        attributionControl: false
    });
    mainWin.unlocatedMap.fitBounds(bounds);
    mainWin.notInGeoNamesMap.fitBounds(bounds);
    //$('#unlocatedMapDiv, #notInGeoNamesMapDiv').css('cursor', 'default');
    // mainWin.unlocatedMap.addLayer(new L.TileLayer('http://tile.osm.org/{z}/{x}/{y}.png'));
    // mainWin.notInGeoNamesMap.addLayer(new L.TileLayer('http://tile.osm.org/{z}/{x}/{y}.png'));
    mainWin.unlocatedMap.dragging.disable();
    mainWin.unlocatedMap.touchZoom.disable();
    mainWin.unlocatedMap.doubleClickZoom.disable();
    mainWin.unlocatedMap.scrollWheelZoom.disable();
    mainWin.unlocatedMap.boxZoom.disable();
    mainWin.unlocatedMap.keyboard.disable();

    mainWin.notInGeoNamesMap.dragging.disable();
    mainWin.notInGeoNamesMap.touchZoom.disable();
    mainWin.notInGeoNamesMap.doubleClickZoom.disable();
    mainWin.notInGeoNamesMap.scrollWheelZoom.disable();
    mainWin.notInGeoNamesMap.boxZoom.disable();
    mainWin.notInGeoNamesMap.keyboard.disable();

};


mapManip.auxMapOnEachFeature = function (feature, layer) {
    //layer._leaflet_id = feature.properties.geoNameId;
    //layer._leaflet_id = feature.properties.positions;
    layer._leaflet_id = feature.properties.name.toLowerCase();

    layer.on({
        mouseover: mainWin.OnToponymHover,
        mouseout: mainWin.OnToponymHoverOut
    });

    var removeAll = "<button id='removeAllId' title='Click to remove this place and all its highlights from the text' href='#'  class='btn btn-danger btn-xs' onclick='mainWin.removeAll(\""
        + encodeURIComponent(JSON
            .stringify(feature.properties.positions))
        + "\",\""
        + encodeURIComponent(JSON
            .stringify(feature.properties.geoNameId))
        + "\"); return false;'>Delete/Remove Highlights</button>";

    var advancedSearch = "<button id='advancedSearchMapBtn' title='Click to launch advance search' href='#'  class='btn btn-warning btn-xs' onclick='mapManip.invokeAdvancedDialogue(\""
        + encodeURIComponent(JSON
            .stringify(feature.properties.positions))
        + "\",\""
        + encodeURIComponent(JSON
            .stringify(feature.properties.geoNameId))
        + "\"); return false;'>Locate via Advanced Search</button>";



    var popUpContent = removeAll + "<br>" + advancedSearch;

    //popUpContent += '<br>' + feature.properties.name;
    // var exerpts = textManip.getExcerpt(feature.properties.positions, feature.properties.name, responseCache.latest.inputTexts[activeInputText]);
    // for (var e = 0; e < exerpts.length; e++) {
    //     popUpContent += exerpts[e];
    // }
    layer.bindPopup(popUpContent);

    var labelContent = '';

    if (feature.properties.name) {
        labelContent = "<FONT COLOR=\"#000099\">"
            + feature.properties.name
            + "</FONT>"
    } if (feature.properties.isGeogFocus) {
        labelContent = '<b><span class="focusLabel">'
            + labelContent + '</span></b>';
    }

    layer.bindLabel(labelContent, {
        noHide: true
    }).showLabel();

};


mapManip.mainOnEachFeature = function (feature, layer) {

    if (feature.properties.type !== "aux") {

        //layer._leaflet_id = feature.properties.geoNameId;
        //layer._leaflet_id = feature.properties.positions;
        layer._leaflet_id = feature.properties.name.toLowerCase();

        layer.on({
            mouseover: mainWin.OnToponymHover,
            mouseout: mainWin.OnToponymHoverOut
        });

        oms.addMarker(layer);

        var hierarchy = "", alternatives = "", removeAll = "", isFocus = "", representative = "", uncertainLocation = "", uncertainSemantics = "", impreciseLocation = "", vagueLocation = "", hasSurrogate = "", surrogateList = "<br>";

        if (feature.properties.hierarchy) {
            for (var toponymInHierarchy = feature.properties.hierarchy.features.length - 1; toponymInHierarchy >= 0; toponymInHierarchy--) {
                hierarchy = feature.properties.hierarchy.features[toponymInHierarchy].properties.toponym
                    + ", " + hierarchy;
            }
        }
        if (feature.properties.toponym) {
            hierarchy = "Toponym: " + feature.properties.toponym + ", " + hierarchy;
            hierarchy = hierarchy.substring(0, hierarchy.length - 2);
        }

        if (feature.properties.hasOwnProperty("surrogates")) {
            for (var s = 0; s < feature.properties.surrogates.features.length; s++) {
                surrogateList += '*' + feature.properties.surrogates.features[s].properties.toponym
                    + ' '
                    + '<a target="_blank" href="http://www.geonames.org/' + feature.properties.surrogates.features[s].properties.geoNameId
                    + '/">See on GeoNames</a> or <a target="_blank" href="http://api.geonames.org/get?geonameId=' + feature.properties.surrogates.features[s].properties.geoNameId + '&username=siddhartha&style=full">ID ' + feature.properties.surrogates.features[s].properties.geoNameId + '</a><br>';
            }
        }

        removeAll = "<button id='removeAllId' title='Click to remove this place and all its highlights from the text' href='#'  class='btn btn-danger btn-xs' onclick='mainWin.removeAll(\""
            + encodeURIComponent(JSON
                .stringify(feature.properties.positions))
            + "\",\""
            + encodeURIComponent(JSON
                .stringify(feature.properties.geoNameId))
            + "\"); return false;'>Delete/Remove Highlights in Text</button>"

        if (feature.properties.alternates && feature.properties.alternates.features !== undefined
            && feature.properties.alternates.features.length > 1) {
            alternatives = "<button id='myLink' title='Click to see the alternatives for this place on the map' href='#'  class='btn btn-warning btn-xs' onclick='prepareAlternates(\""
                + encodeURIComponent(JSON
                    .stringify(feature.properties.positions))
                + "\",\""
                + encodeURIComponent(JSON
                    .stringify(feature.properties.geoNameId))
                + "\"); return false;'><b>See Alternative Locations </b>("
                + feature.properties.alternates.features.length
                + ")</button>"
        } else if (feature.properties.geoNameId && typeof (feature.properties.geoNameId) === "number") {
            alternatives = "<b>No Alternative</b>";
        }

        if (feature.properties.isGeogFocus === undefined
            || feature.properties.isGeogFocus === false) {
            isFocus = ">Geographic Focus";
        } else if (feature.properties.isGeogFocus === undefined
            || feature.properties.isGeogFocus === true) {
            isFocus = "checked >Geographic Focus";
        }
        if (feature.properties.uncertainSemantics === undefined
            || feature.properties.uncertainSemantics === false) {
            uncertainSemantics = ">Uncertain Semantics";
        } else if (feature.properties.uncertainSemantics === undefined
            || feature.properties.uncertainSemantics === true) {
            uncertainSemantics = "checked >Uncertain Semantics";
        }

        if (!feature.properties.hasOwnProperty("uncertainLocation")) {
            if (!feature.properties.hasOwnProperty("impreciseLocation") && feature.properties.hasOwnProperty("surrogates")) {
                impreciseLocation = ">Overlapping Ambiguous";
            } else if (feature.properties.impreciseLocation === true) {
                impreciseLocation = "checked >Overlapping Ambiguous";
            }
        }

        if (!feature.properties.hasOwnProperty("impreciseLocation")) {
            if (!feature.properties.hasOwnProperty("uncertainLocation")) {
                uncertainLocation = ">Non-overlapping Ambiguous";
            } else if (feature.properties.uncertainLocation === true) {
                uncertainLocation = "checked >Non-overlapping Ambiguous";
            }
        }

        if (feature.properties.vagueLocation === undefined
            || feature.properties.vagueLocation === false) {
            vagueLocation = ">Vague Boundaries";
        } else if (feature.properties.vagueLocation === undefined
            || feature.properties.vagueLocation === true) {
            vagueLocation = "checked >Vague Boundaries";
        }

        if (!feature.properties.hasOwnProperty("representative")) {
            representative = ">Representative";
        } else if (feature.properties.representative === true) {
            representative = "checked >Representative";
        }


        if (feature.properties.surrogates !== undefined && feature.properties.surrogates.features !== undefined && feature.properties.surrogates.features.length > 0) {

            hasSurrogate = "<br><button id='surrogateLink' title='Click to see the surrogates for this place on the map' class='btn btn-info btn-xs' href='#' onclick='mainWin.renderSurrogates (\""
                + encodeURIComponent(JSON
                    .stringify(feature.properties.positions))
                + "\",\""
                + encodeURIComponent(JSON
                    .stringify(feature.properties.geoNameId))
                + "\"); return false;'>Surrogates ("
                + (feature.properties.surrogates.features.length - 1)
                + " + this candidate)</button>";
        }

        var popUpContent = "";

        if (impreciseLocation) {
            impreciseLocation = '<button id="impreciseLoc" title="Click if you are not sure of the geographic level (city versus state or state versus island)" href="#" class="checkbox btn btn-purple btn-xs" onclick="modifyProperty(\''
                + encodeURIComponent(JSON
                    .stringify(feature.properties.positions))
                + '\',\''
                + encodeURIComponent(JSON
                    .stringify(feature.properties.geoNameId))
                + '\',\''
                + 'impreciseLocation'
                + '\'); return false;"><label><input type="checkbox" value="" '
                + impreciseLocation
                + '</label></button>'
        }

        if (uncertainLocation) {
            uncertainLocation = '<button id="uncertainLoc" title="Click if you are not sure of the exact location for this place name." href="#" class="checkbox btn btn-pink btn-xs"  onclick="modifyProperty(\''
                + encodeURIComponent(JSON
                    .stringify(feature.properties.positions))
                + '\',\''
                + encodeURIComponent(JSON
                    .stringify(feature.properties.geoNameId))
                + '\',\''
                + 'uncertainLocation'
                + '\'); return false;"><label><input type="checkbox" value="" '
                + uncertainLocation
                + '</label></button>'
        }

        popUpContent +=
            '<button id="isAbout" title="Click if the document is about this place." href="#"  class="checkbox btn btn-yellow btn-xs" onclick="modifyProperty(\''
            + encodeURIComponent(JSON
                .stringify(feature.properties.positions))
            + '\',\''
            + encodeURIComponent(JSON
                .stringify(feature.properties.geoNameId))
            + '\',\''
            + 'isGeogFocus'
            + '\'); return false;"><label><input type="checkbox" value=""'
            + isFocus
            + '</label></button>'
            + '<button id="represent" title="Click if you want only the coordinates of this feature. Toponym and GeonameID will be ignored " href="#" class="checkbox btn btn-default btn-xs" onclick="modifyProperty(\''
            + encodeURIComponent(JSON
                .stringify(feature.properties.positions))
            + '\',\''
            + encodeURIComponent(JSON
                .stringify(feature.properties.geoNameId))
            + '\',\''
            + 'representative'
            + '\'); return false;"><label><input type="checkbox" value="" '
            + representative
            + '</label></button>'
            + '<button id="uncertainSemantics" title="Click if you are not sure if this qualifies as a place name or not." href="#" class="checkbox btn btn-success btn-xs" onclick="modifyProperty(\''
            + encodeURIComponent(JSON
                .stringify(feature.properties.positions))
            + '\',\''
            + encodeURIComponent(JSON
                .stringify(feature.properties.geoNameId))
            + '\',\''
            + 'uncertainSemantics'
            + '\'); return false;"><label><input type="checkbox" value="" '
            + uncertainSemantics
            + '</label></button>'
            + uncertainLocation
            + impreciseLocation
            + '<button id="vagueLoc" title="Click if you are not sure of the geographic extent, such as California\'s pacific coast or Southern California" href="#" class="checkbox btn btn-greenGray btn-xs" onclick="modifyProperty(\''
            + encodeURIComponent(JSON
                .stringify(feature.properties.positions))
            + '\',\''
            + encodeURIComponent(JSON
                .stringify(feature.properties.geoNameId))
            + '\',\''
            + 'vagueLocation'
            + '\'); return false;"><label><input type="checkbox" value="" '
            + vagueLocation
            + '</label></button>'
            + removeAll
            + '<br>'
            + hierarchy
            + '<br>'
            + alternatives
            + hasSurrogate

        if (feature.properties.featureCode) {
            popUpContent += '<br> FeatureCode: ' + utils.getFullFeatureCode(feature.properties.featureCode);
        }

        if (feature.properties.hasOwnProperty("geoNameId") && typeof (feature.properties.geoNameId) === "number") {
            popUpContent += '<br><a target="_blank" href="http://www.geonames.org/' + feature.properties.geoNameId + '/">See on GeoNames</a> or <a target="_blank" href="http://api.geonames.org/get?geonameId=' + feature.properties.geoNameId + '&username=siddhartha&style=full">Check ID ' + feature.properties.geoNameId + '</a>';

        }

        if (feature.properties.hasOwnProperty("surrogates")) {
            popUpContent += '<br><b> Surrogate list:</b> ' + surrogateList;
        }

        var exerpts = textManip.getExcerpt(feature.properties.positions, feature.properties.name, responseCache.latest.inputTexts[activeInputText]);

        for (var e = 0; e < exerpts.length; e++) {
            popUpContent += exerpts[e];
        }

        layer.bindPopup(popUpContent);

        var labelContent = '', isRep = '', isRepClose = '';

        if (feature.properties.hasOwnProperty("representative")) {
            isRep = '<s>';
            isRepClose = '</s>';
        }

        // if (feature.properties.toponym && feature.properties.name.toLowerCase() === feature.properties.toponym
        //     .toLowerCase() && !feature.properties.hasOwnProperty("representative")) {
        //     labelContent = "<FONT COLOR=\"#000099\">"
        //         + feature.properties.toponym
        //         + "</FONT>";
        // } else
        if (feature.properties.name) {
            labelContent = "<FONT COLOR=\"#000099\">"
                + feature.properties.name
                + "</FONT>" + "<br>"
                + isRep
        } if (feature.properties.toponym) {
            labelContent += "<FONT COLOR=\"#990033\">"
                + feature.properties.toponym
                + "</FONT>"
                + isRepClose;
        } else if (feature.properties.geoNameId === "g") {
            labelContent += "<FONT COLOR=\"#990033\"> ?? </FONT>"
                + isRepClose;
        } else if (feature.properties.source && feature.properties.source === "clk") {
            labelContent += "<FONT COLOR=\"#990033\">"
                + feature.geometry.coordinates[1] + ", "
                + feature.geometry.coordinates[0]
                + "</FONT>"
                + isRepClose;
        }
        if (feature.properties.isGeogFocus) {
            labelContent = '<b><span class="focusLabel">'
                + labelContent + '</span></b>';
        }

        layer.bindLabel(labelContent, {
            noHide: true
        }).showLabel();
    }
};