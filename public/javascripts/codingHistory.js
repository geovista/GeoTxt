"use strict";

var codingsMod = {};

//The rendering of Surrogates in thie view poluted some functions with global vairables such as codingsMod.mapReferences and codingsMod.allCodings.
codingsMod.mapReferences = [];
//todo: use the same as the mainWin for osmOptions
codingsMod.osmOptions = {nearbyDistance: 22, circleSpiralSwitchover: "infinity", circleFootSeparation: 35, keepSpiderfied: true};


//Gets populated by sendCodingsToPopUp in the main window - geoCodingEval.
//codingsMod.allCodings = {};
//Gets populated by renderCodings
//codingsMod.inputTexts =[];

codingsMod.opener = window.opener;



$(document).ready(function () {

    rangy.init();

    codingsMod.cssApplier = rangy.createClassApplier("selectedPlace", {
        ignoreWhiteSpace: true,
        normalize: false,
        tagNames: ["span", "a"]
    });


    //When ready, call the function that sends the data.    
    codingsMod.opener.sendCodingsToPopUp();
    // mainWindow.postMessage("Hello", "*");

});



//This is codingsMod.allCoding passed to this function in the main window.
codingsMod.renderCodings = function (allCodings, inputTexts) {

    codingsMod.inputTexts = inputTexts;

    //console.log(JSON.stringify(allCodings));

    for (var i = allCodings.length - 1; i >= 0; i--) {

        var codingDiv = document.createElement('div');
        codingDiv.id = "coding" + allCodings[i].stage + "Div";

        if (allCodings[i].stage % 2 === 0) {
            codingDiv.setAttribute("class", "codingEvenDivClass");

        } else {
            codingDiv.setAttribute("class", "codingOddDivClass");
        }


        //iDiv.className = 'block';
        if (document.getElementById("historyPageDiv").childNodes.length > 0) {
            document.getElementById("historyPageDiv").insertBefore(codingDiv, document.getElementById("historyPageDiv").firstChild);
        } else {
            document.getElementById("historyPageDiv").appendChild(codingDiv);
        }

        var infoDiv = document.createElement('div');
        infoDiv.id = "info" + allCodings[i].stage + "Div";
        document.getElementById("coding" + allCodings[i].stage + "Div").appendChild(infoDiv);


        var isSkipped = allCodings[i].skipped ? "WAS" : "Was NOT";

        //TODO use event handler to assign the onclick instead of inline here.
        var infoString = "<b><button id=\"pick" + allCodings[i].stage + "\" class=\"btn btn-primary\" href=\"#\" onclick=\"codingsMod.opener.geoCodingEval.populateFromPickedCoding(" + i + ");return false;\" title =\"Click if this coding is correct for all inputs and you want to replicate this coding.\">Select Annotation Stage "
                + allCodings[i].stage +
                "</b></button>, by " + allCodings[i].geoCoder + " in role " + allCodings[i].role + ". " + isSkipped + " skipped. <br>";

        if (allCodings[i].comments !== "") {
            infoString += allCodings[i].geoCoder + "'s comments: <i>" + allCodings[i].comments + "</i><br>";
        }


        infoDiv.innerHTML = infoString;

        codingsMod.mapReferences[i] = [];

        for (var j = 0; j < inputTexts.length; j++) {

            var inputItemDiv = document.createElement('div');
            inputItemDiv.id = allCodings[i].stage + "Coding" + j + "InputItemDiv";
            inputItemDiv.setAttribute("class", "codingInputItemClass");
            document.getElementById("coding" + allCodings[i].stage + "Div").appendChild(inputItemDiv);


            var textDiv = document.createElement('div');
            textDiv.id = allCodings[i].stage + "Coding" + j + "TextDiv";
            textDiv.setAttribute("class", "textDivClass")
            document.getElementById(allCodings[i].stage + "Coding" + j + "InputItemDiv").appendChild(textDiv);


            textDiv.innerHTML = inputTexts[j];

            textManip.highlightFromGeoJson(textDiv, allCodings[i].geoCodedGeoJson[j].features, false, rangy, codingsMod.cssApplier);

            var mapDiv = document.createElement('div');

            mapDiv.id = allCodings[i].stage + "Coding" + j + "MapDiv";

            mapDiv.setAttribute("class", "mapClass");

            document.getElementById(allCodings[i].stage + "Coding" + j + "InputItemDiv").appendChild(mapDiv);



            var map = new L.Map(allCodings[i].stage + "Coding" + j + "MapDiv");

            //store a reference to the map object to use for surrogate mapping
            codingsMod.mapReferences[i][j] = map;

            var osm = new L.TileLayer('http://tile.osm.org/{z}/{x}/{y}.png');

            map.fitWorld().addLayer(osm);

            var oms = new OverlappingMarkerSpiderfier(map, codingsMod.osmOptions);

//            var icons = {normalIcon: new L.divIcon({
//                    className: 'divNormal'
//                }),
//                spideredIcon: new L.divIcon({
//                    className: 'divSpidered'
//                }),
//                focusIcon: new L.divIcon({
//                    className: 'divFocus'
//                })
//            };

            oms.addListener('spiderfy', function (markers) {
                for (var i = 0, len = markers.length; i < len; i++) {
                    //markers[i].setIcon(icons.spideredIcon);
                    //markers[i].closePopup();
                }
                //map.closePopup();
                layer.closePopup();
            });
//
//            oms.addListener('unspiderfy', function (markers) {
//                for (var i = 0, len = markers.length; i < len; i++) {
//                    // markers[i].setIcon(new normalIcon());
//                    markers[i].setIcon(icons.normalIcon);
//                }
//            });

            var cancelBtn = document.createElement('button');
            cancelBtn.id = allCodings[i].stage + "Coding" + j + "Btn";
            cancelBtn.innerHTML = "Back";
            cancelBtn.setAttribute("class", "historyCancelBtn");
            cancelBtn.setAttribute("hidden", "true");
            document.getElementById(allCodings[i].stage + "Coding" + j + "MapDiv").appendChild(cancelBtn);
            cancelBtn.setAttribute("onclick", "codingsMod.reRenderMain(" + i + "," + j + ")");

//            <button type="button" id="cancelAltBtn" class="btnStyle"
//						Style="color: red" onclick="{mainWin.cancelSelectAlternate();}"
//						hidden>Cancel</button>

            codingsMod.renderHistoryMap(allCodings[i].geoCodedGeoJson[j], map, oms, i, j);
        }
    }
};


//TODO: AllCodings is used as a global variable here. See if you can change the function to have it as a parameter.
codingsMod.renderSurrogates = function (encodedPositions, encodedGeoNameId, encodedCodingIndex, encodedInputIndex) {


    var positions, geoNameId, codingIndex, inputIndex, map = '';

    positions = JSON.parse(decodeURIComponent(encodedPositions));
    geoNameId = JSON.parse(decodeURIComponent(encodedGeoNameId));
    codingIndex = JSON.parse(decodeURIComponent(encodedCodingIndex));
    inputIndex = JSON.parse(decodeURIComponent(encodedInputIndex));

    document.getElementById(codingsMod.allCodings[codingIndex].stage + "Coding" + inputIndex + "Btn").hidden = false;

    var map = codingsMod.mapReferences[codingIndex][inputIndex];

    var featureIndex = codingsMod.arrayObjectIndexOf(codingsMod.allCodings[codingIndex].geoCodedGeoJson[inputIndex].features,
            positions, "properties", "positions");

    if (codingsMod.allCodings[codingIndex].geoCodedGeoJson[inputIndex].features[featureIndex].properties.geoNameId !== geoNameId) {
        alert("Identified GeoNameId doesn't match with the picked one. Please report problem to the developer");
        return;
    }

    // TODO make sure that fCollection is defined after this. Also, make sure
    // index is not -1
    var fCollection = jQuery.extend(true, {},
            codingsMod.allCodings[codingIndex].geoCodedGeoJson[inputIndex].features[featureIndex].properties.surrogates);

    // Get the index of the picked alternate within the alternates object of the parent feature
    var alternateFeatureIndex = codingsMod.arrayObjectIndexOf(fCollection.features, geoNameId, "properties", "geoNameId");

    //This is for interface use only. It doesn't get reocorded in the final object.
    fCollection.features[alternateFeatureIndex].properties.isCurrentlyPicked = true;


    //var map = new L.Map(codingsMod.allCodings[codingIndex].stage + "Coding" + inputIndex + "MapDiv");

    //var osm = new L.TileLayer('http://tile.osm.org/{z}/{x}/{y}.png');

    //map.fitWorld().addLayer(osm);

    var oms = new OverlappingMarkerSpiderfier(map, codingsMod.osmOptions);

    codingsMod.renderHistoryMap(fCollection, map, oms, codingIndex, inputIndex);


};


codingsMod.reRenderMain = function (codingIndex, inputIndex) {

    var map = codingsMod.mapReferences[codingIndex][inputIndex];
    var oms = new OverlappingMarkerSpiderfier(map, codingsMod.osmOptions);
    var fCollection = jQuery.extend(true, {}, codingsMod.allCodings[codingIndex].geoCodedGeoJson[inputIndex]);
    codingsMod.renderHistoryMap(fCollection, map, oms, codingIndex, inputIndex);
    document.getElementById(codingsMod.allCodings[codingIndex].stage + "Coding" + inputIndex + "Btn").hidden = true;

};


//TODO consolidate this with the renderMap funcion of the map
codingsMod.renderHistoryMap = function (fCollection, map, oms, codingIndex, inputIndex) {

    //TODO: this is using the Leaflet internal _layers which might break if leaflet changes their implementation.
    $.each(map._layers, function (ml) {
        if (typeof map._layers[ml].feature !== 'undefined') {
            map.removeLayer(this);
            //map.removeLayer(map._layers[ml]); //this refers to the value, so it should be the same.
        }
    });


    var layer = L
            .geoJson(
                    fCollection,
                    {
                        pointToLayer: function () {
                            return utils.pointToLayer(arguments[0], arguments[1]);
                        },
                        onEachFeature: function (feature, layer) {

                            oms.addMarker(layer);

                            var hierarchy = "", surrogateList = "<br>";

                            if (feature.properties.hasOwnProperty("surrogates")) {
                                for (var s = 0; s < feature.properties.surrogates.features.length; s++)
                                {
                                    surrogateList += '*' + feature.properties.surrogates.features[s].properties.toponym
                                            + ' '
                                            + '<a target="_blank" href="http://www.geonames.org/' + feature.properties.surrogates.features[s].properties.geoNameId
                                            + '/">See on GeoNames</a> or <a target="_blank" href="http://api.geonames.org/get?geonameId=' + feature.properties.surrogates.features[s].properties.geoNameId + '&username=siddhartha&style=full">ID ' + feature.properties.surrogates.features[s].properties.geoNameId + '</a><br>';
                                }
                            }

                            for (var toponymInHierarchy = feature.properties.hierarchy.features.length - 1; toponymInHierarchy >= 0; toponymInHierarchy--) {
                                hierarchy = feature.properties.hierarchy.features[toponymInHierarchy].properties.toponym
                                        + ", " + hierarchy;
                            }
                            hierarchy = "Toponym: " + feature.properties.toponym + ", " + hierarchy;
                            hierarchy = hierarchy.substring(0, hierarchy.length - 2);

                            var popUpContent = "";



                            popUpContent = hierarchy
                                    + '<br>'
                                    + 'Position: '
                                    + feature.properties.positions;

                            if (feature.properties.isGeogFocus) {
                                popUpContent += '<br><b><i>Geographic Focus</i></b>';
                            }
                            if (feature.properties.uncertainSemantics) {
                                popUpContent += '<br> <span style="color:#669933"><i>Uncertain Semantics</i></span>';
                            }
                            if (feature.properties.vagueLocation) {
                                popUpContent += '<br> <span style="color:#808080"><i>Vague Location</i></span>';
                            }
                            if (feature.properties.uncertainLocation) {
                                popUpContent += '<br><span style="color:#FF0000"><i>Non-overlapping Ambiguous</i></span>';
                            }
                            if (feature.properties.impreciseLocation) {
                                popUpContent += '<br><span style="color:#800080"><i>Overlapping Ambiguous</i></span>';
                            }
                            if (feature.properties.representative) {
                                popUpContent += '<br><span style="color:#000000"><i>Representative (only for coordinates)</i></span>';
                            }

                            if (feature.properties.hasOwnProperty("featureCode")) {
                                popUpContent += '<br> FeatureCode: ' + utils.getFullFeatureCode(feature.properties.featureCode);
                            }

                            if (feature.properties.hasOwnProperty("geoNameId")) {
                                popUpContent += '<br><a target="_blank" href="http://www.geonames.org/' + feature.properties.geoNameId + '/">See on GeoNames</a> or <a target="_blank" href="http://api.geonames.org/get?geonameId=' + feature.properties.geoNameId + '&username=siddhartha&style=full">Check ID ' + feature.properties.geoNameId + '</a>';
                            }
                            if (feature.properties.hasOwnProperty("surrogates")) {
                                popUpContent += '<br> <b>Surrogate list:</b> ' + surrogateList;
                            }

                            if (feature.properties.hasOwnProperty("surrogates") && feature.properties.surrogates.features !== undefined && feature.properties.surrogates.features.length > 0) {

                                popUpContent += "<a id='surrogateLink' title='Click to see the surrogates for this place on the map' href='#' onclick='codingsMod.renderSurrogates (\""
                                        + encodeURIComponent(JSON
                                                .stringify(feature.properties.positions))
                                        + "\",\""
                                        + encodeURIComponent(JSON
                                                .stringify(feature.properties.geoNameId))
                                        + "\",\""
                                        + encodeURIComponent(JSON
                                                .stringify(codingIndex))
                                        + "\",\""
                                        + encodeURIComponent(JSON
                                                .stringify(inputIndex))
                                        + "\"); return false;'><span style='color:#800080'><b>Surrogates </b>("
                                        + (feature.properties.surrogates.features.length - 1)
                                        + " + this candidate)</span></a>";
                            }

                            //TODO: This is dependent on a global variable codingsMod.inputTexts. It can easily be passed as a param, but decided to leave
                            //it as is since there seems to be no need or harm at the moment.
                            var exerpts = textManip.getExcerpt(feature.properties.positions, feature.properties.name, codingsMod.inputTexts[inputIndex]);

                            for (var e = 0; e < exerpts.length; e++) {
                                popUpContent += exerpts[e];
                            }

                            layer.bindPopup(popUpContent);

                            var labelContent = '', isRep = '', isRepClose = '';

                            if (feature.properties.hasOwnProperty("representative")) {
                                isRep = '<s>';
                                isRepClose = '</s>';
                            }

                            if (feature.properties.name.toLowerCase() === feature.properties.toponym
                                    .toLowerCase() && !feature.properties.hasOwnProperty("representative")) {
                                labelContent = "<FONT COLOR=\"#000099\">"
                                        + feature.properties.toponym
                                        + "</FONT>";
                            } else {
                                labelContent = "<FONT COLOR=\"#000099\">"
                                        + feature.properties.name.charAt(0)
                                        .toUpperCase()
                                        + feature.properties.name.slice(1)
                                        + "</FONT>" + "<br>"
                                        + isRep
                                        + "<FONT COLOR=\"#990033\">"
                                        + feature.properties.toponym
                                        + "</FONT>"
                                        + isRepClose;
                            }

                            if (feature.properties.isGeogFocus) {
                                labelContent = '<b><span class="focusLabel">'
                                        + labelContent + '</span></b>';
                            }

                            layer.bindLabel(labelContent, {
                                noHide: true
                            }).addTo(map).showLabel();
                        }
                    }).addTo(map);

    codingsMod.fitMapBounds(map, layer);
};


//TODO consolidate this with the fitMapBounds on the actual map.
codingsMod.fitMapBounds = function (map, layer) {
    // If there is no feature to be rendered on map, then just fitWorld
    if ((layer === null || layer === undefined)
            || (layer.getBounds() === undefined || layer.getBounds()
                    .getNorthEast() === undefined)) {
        map.fitWorld();
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
        map.fitBounds(newBounds, {padding: [8, 8]});
        console.log("The modified zoom logic");
    } else if (layer.getBounds().getNorthEast().distanceTo(
            layer.getBounds().getSouthWest()) >= 50000) {
        map.fitBounds(layer.getBounds(), {padding: [10, 10]});
        console.log("Default getbounds");
    }
};


//TODO consolidate this with the arrayObjectIndexOf in corpusBuilding.js.
codingsMod.arrayObjectIndexOf = function (myArray, searchTerm, properties, property) {

    // See if the object being passed is an array, like the positions field.
    if (Array.isArray(searchTerm)) {
        for (var i = 0, len = myArray.length; i < len; i++) {
            if ((myArray[i][properties][property].length === searchTerm.length)
                    && myArray[i][properties][property].every(function (element,
                    index) {
                return element === searchTerm[index];
            }))
                return i;
        }
        return -1;
    } else {
        for (var i = 0, len = myArray.length; i < len; i++) {
            if (myArray[i][properties][property] === searchTerm)
                return i;
        }
        return -1;
    }
};
