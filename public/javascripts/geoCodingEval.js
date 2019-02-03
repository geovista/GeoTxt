"use strict";

var codingsWindow = "";

var geoCodingEval = {};


function displayAllCodings() {

    if (stage < 2) {
        alert("No previous coding");
        return;
    }

    if (stage === 2 && submitAttempted === false) {
        alert("At this stage (2), you can only use this functionality after you have tried submitting at least once.");
        return;
    }

    //TODO = check to see if popup is already enabled

    codingsWindow = window.open("codingHistory.html", "CodingHistoryWindow", "width=585, innerWidth=585, scrollbars=yes, resizable=yes, left=800");

    //TODO: chrome doesn't work with this.
    codingsWindow.resizeTo(585, screen.height);

    codingsWindow.focus();

}


function sendCodingsToPopUp() {

    codingsWindow.codingsMod.allCodings = jQuery.extend(true, [], responseCache.original.allCodings);

    codingsWindow.codingsMod.renderCodings(codingsWindow.codingsMod.allCodings, responseCache.original.inputTexts);

}


geoCodingEval.populateFromPickedCoding = function (codingIndex) {

    responseCache.latest.generatedAlternates = jQuery.extend(true, [], responseCache.original.allCodings[codingIndex].geoCodedGeoJson);

    for (var i = 0; i < responseCache.latest.generatedAlternates.length; i++) {
        activateInputItem(i);
    }
    //run activateInput for all actinveInputs so that they get updated too.

    codingsWindow.close();

};


geoCodingEval.consolidateCommonPlaces = function (geoCodedArrayParam) {

    var geoCodedArray = jQuery.extend(true, [], geoCodedArrayParam);

    for (var i = 0; i < geoCodedArray.length; i++) {

        //TODO this was outside the above for loop, potentially a bug? I braught it inside.
        var matchingIndexes = [];

        if (geoCodedArray[i].hasOwnProperty("features")) {

            for (var j = 0; j < geoCodedArray[i].features.length; j++) {

                //Check to see if this feature has already been checked and its index added to matchingIndexes
                if (matchingIndexes.indexOf(j) === -1) {

                    for (var k = 0; k < geoCodedArray[i].features.length; k++) {

                        //todo: should we check for names to be the same to consolidate tags? 
                        //todo: only consolidate them if tags are the same.
                        if (k !== j && geoCodedArray[i].features[j].properties.geoNameId === geoCodedArray[i].features[k].properties.geoNameId
                            && geoCodedArray[i].features[j].properties.name.toLowerCase() === geoCodedArray[i].features[k].properties.name.toLowerCase()
                            && geoCodedArray[i].features[j].properties.uncertainSemantics === geoCodedArray[i].features[k].properties.uncertainSemantics) {


                            // Preserve the evalParams of feature[k] in feature[j]
                            for (var key in mainWin.evalParams) {
                                if (mainWin.evalParams.hasOwnProperty(key)) {
                                    if (geoCodedArray[i].features[k].properties[mainWin.evalParams[key]]) {
                                        geoCodedArray[i].features[j].properties[mainWin.evalParams[key]] = geoCodedArray[i].features[k].properties[mainWin.evalParams[key]];
                                    }
                                }
                            }

                            //todo: implement this as part of tag check.
                            if (typeof geoCodedArray[i].features[k].properties.surrogates !== "undefined" && geoCodedArray[i].features[k].properties.surrogates.length > 0) {
                                geoCodedArray[i].features[j].properties.surrogates = jQuery.extend(true, {}, geoCodedArray[i].features[k].properties.surrogates);
                            }

                            //ignore the same pos.
                            if (matchingIndexes.indexOf(k) === -1) {
                                matchingIndexes.push(k);
                            }
                            // geoCodedArray[i].features[j].properties.positions = geoCodedArray[i].features[j].properties.positions.concat(geoCodedArray[i].features[k].properties.positions);
                            // geoCodedArray[i].features[j].properties.positions.sort(function (a, b) {
                            //     return a - b;
                            // });

                            utils.updateFeaturePos(geoCodedArray[i].features[j], geoCodedArray[i].features[j].properties.positions.concat(geoCodedArray[i].features[k].properties.positions));
                        }
                    }
                }
            }

        }

        matchingIndexes.sort(function (a, b) {
            return a - b;
        });

        for (var a = matchingIndexes.length - 1; a >= 0; a--) {
            geoCodedArray[i].features.splice(matchingIndexes[a], 1);
        }

        //TODO this is probably where you should  implement cross- validation between profile and text.        



    }
    return geoCodedArray;
};


geoCodingEval.unConsolidateCommonPlaces = function (fCollectionParam) {

    var fCollection = jQuery.extend(true, {}, fCollectionParam);
    var respFCollection = new FeatureCollection([]);
    for (var i = 0; i < fCollection.features.length; i++) {
        for (var j = 0; j < fCollection.features[i].properties.positions.length; j++) {
            var tempFeature = jQuery.extend(true, {}, fCollection.features[i]);
            tempFeature.properties = jQuery.extend(true, {}, fCollection.features[i].properties);
            utils.updateFeaturePos(tempFeature, [tempFeature.properties.positions[j]]);
            respFCollection.features.push(tempFeature);
        }
    }
    utils.sortFeaturesOnFirstPos(respFCollection);
    return respFCollection;
};


function evalCoding() {

    if (stage < 2) {
        alert("No previous coding");
        return;
    }

    if (stage === 2 && submitAttempted === false) {
        alert("At this stage (2), you can only use this functionality after you have tried submitting at least once.");
        return;
    }

    var helperObj = {};
    helperObj.geoCodedGeoJson = jQuery.extend(true, [], responseCache.latest.generatedAlternates);

    for (var i = 0; i < helperObj.geoCodedGeoJson.length; i++) {
        if (helperObj.geoCodedGeoJson[i].features === undefined) {
            helperObj.geoCodedGeoJson[i] = new FeatureCollection([]);
        }
    }

    helperObj.geoCodedGeoJson = geoCodingEval.consolidateCommonPlaces(helperObj.geoCodedGeoJson);

    var agreed = compareFeatureCollectionArrays(responseCache.original.allCodings[0].geoCodedGeoJson, helperObj.geoCodedGeoJson);

    if (agreed === true) {
        alert("Congrats! Your coding matches that of the other coder for this document (regardless of skipped or not)!");
    } else {
        alert("Your coding DOES NOT match that of the other coder for this document (regardless of skipped or not)!");
    }
}


function evalSubmissions(resultsParam) {

    var ResultsObj = jQuery.extend(true, {}, resultsParam);

    if (ResultsObj.stage === 1) {
        ResultsObj.finalGCResults = [];
        ResultsObj.updatedGCStatus = "GCInProgress";
        ResultsObj.allCodings[0].updatedGCStatus = "GCInProgress";
        ResultsObj.allCodings[0].finalGCResults = [];
    }

    if (ResultsObj.stage >= 2) {

        var skipCount = 0;
        var agreed = false;

        for (var c = 0; c < 2; c++) {
            if (ResultsObj.allCodings[c].skipped === true) {
                skipCount++;
            }
        }


        if (skipCount === 0) {

            agreed = compareLastTwoCodings(ResultsObj.allCodings.slice(0, 2));

            if (agreed === true) {
                ResultsObj.updatedGCStatus = "GCComplete";
                ResultsObj.finalGCResults = jQuery.extend(true, [], ResultsObj.allCodings[0].geoCodedGeoJson);
                ResultsObj.allCodings[0].updatedGCStatus = "GCComplete";
                ResultsObj.allCodings[0].finalGCResults = jQuery.extend(true, [], ResultsObj.allCodings[0].geoCodedGeoJson);


            } else if (agreed === false) {

                var proceed = confirm("Your coding does NOT agree with the other coder's coding for this document. CLICK ON CANCEL IF YOU WANT TO REVISE.");

                if (proceed === false) {
                    return -1;
                }

                var totalSkipCount = 0;

                for (var c = 0; c < ResultsObj.allCodings.length; c++) {
                    if (ResultsObj.allCodings[c].skipped === true) {
                        totalSkipCount++;
                    }
                }

                if (ResultsObj.allCodings.length - totalSkipCount >= 6) {
                    ResultsObj.updatedGCStatus = "GCUncertain";
                    ResultsObj.finalGCResults = [];
                    ResultsObj.allCodings[0].updatedGCStatus = "GCUncertain";
                    ResultsObj.allCodings[0].finalGCResults = [];

                } else {
                    ResultsObj.updatedGCStatus = "GCInProgress";
                    ResultsObj.finalGCResults = [];
                    ResultsObj.allCodings[0].updatedGCStatus = "GCInProgress";
                    ResultsObj.allCodings[0].finalGCResults = [];

                }
            }

        } else if (skipCount === 2) {
            ResultsObj.updatedGCStatus = "GCPause";
            ResultsObj.finalGCResults = [];
            ResultsObj.allCodings[0].updatedGCStatus = "GCPause";
            ResultsObj.allCodings[0].finalGCResults = [];


        } else if (skipCount === 1) {

            if (ResultsObj.allCodings.length <= 2) {
                agreed = compareLastTwoCodings(ResultsObj.allCodings.slice(0, 2));
                if (agreed === false) {
                    var proceed = confirm("Your coding does NOT agree with the other coder's coding for this document, although one of them is a skipped submission. CLICK ON CANCEL IF YOU WANT TO REVISE. You can check the previous codings before you proceed.");
                    if (proceed === false) {
                        return -1;
                    }
                }
                ResultsObj.updatedGCStatus = "GCInProgress";
                ResultsObj.finalGCResults = [];
                ResultsObj.allCodings[0].updatedGCStatus = "GCInProgress";
                ResultsObj.allCodings[0].finalGCResults = [];
            } else {
                if (ResultsObj.allCodings[0].skipped === true) {
                    if (ResultsObj.allCodings.length >= 3) {
                        if (ResultsObj.allCodings[2].skipped === true) {
                            ResultsObj.updatedGCStatus = "GCPause";
                            ResultsObj.finalGCResults = [];
                            ResultsObj.allCodings[0].updatedGCStatus = "GCPause";
                            ResultsObj.allCodings[0].finalGCResults = [];
                        } else {
                            ResultsObj.updatedGCStatus = "GCInProgress";
                            ResultsObj.finalGCResults = [];
                            ResultsObj.allCodings[0].updatedGCStatus = "GCInProgress";
                            ResultsObj.allCodings[0].finalGCResults = [];
                        }
                    } else { //todo: this is unreachable code.
                        ResultsObj.updatedGCStatus = "GCInProgress";
                        ResultsObj.finalGCResults = [];
                        ResultsObj.allCodings[0].updatedGCStatus = "GCInProgress";
                        ResultsObj.allCodings[0].finalGCResults = [];
                    }
                } else if (ResultsObj.allCodings[1].skipped === true) {
                    if (ResultsObj.allCodings.length >= 4) {
                        if (ResultsObj.allCodings[3].skipped === true) {
                            ResultsObj.updatedGCStatus = "GCPause";
                            ResultsObj.finalGCResults = [];
                            ResultsObj.allCodings[0].updatedGCStatus = "GCPause";
                            ResultsObj.allCodings[0].finalGCResults = [];
                        } else {
                            ResultsObj.updatedGCStatus = "GCInProgress";
                            ResultsObj.finalGCResults = [];
                            ResultsObj.allCodings[0].updatedGCStatus = "GCInProgress";
                            ResultsObj.allCodings[0].finalGCResults = [];
                        }
                    } else {
                        ResultsObj.updatedGCStatus = "GCInProgress";
                        ResultsObj.finalGCResults = [];
                        ResultsObj.allCodings[0].updatedGCStatus = "GCInProgress";
                        ResultsObj.allCodings[0].finalGCResults = [];
                    }

                }
            }
        }
    }
    return ResultsObj;
}


function compareLastTwoCodings(lastTwoCodings) {
    return compareFeatureCollectionArrays(lastTwoCodings[0].geoCodedGeoJson, lastTwoCodings[1].geoCodedGeoJson);
}


function compareFeatureCollectionArrays(array1, array2) {

    var eachAgree = true;

    for (var i = 0; i < array1.length; i++) {
        eachAgree = compareFeatures(array1[i].features, array2[i].features);
        if (eachAgree === false) {
            return false;
        }
        //        if (array1[i].properties.surrogates !== "undefined"){
        //            if ()
        //        }
    }
    return true;
}


function compareFeatures(features1, features2) {

    if (features1.length === features2.length) {

        for (var i = 0; i < features1.length; i++) {
            //for each feature, get the position. Get the feature that shares the same position in feature 2. Compare the names and the geonames id. 
            //TODO what if we have multiple placeNames in the same position.
            var positions = features1[i].properties.positions;

            var feature2Index = arrayObjectIndexOf(features2, positions, "properties", "positions");

            if (feature2Index === -1) {
                return false;
            }

            if (features1[i].properties.name.toLowerCase() !== features2[feature2Index].properties.name.toLowerCase() || features1[i].properties.geoNameId !== features2[feature2Index].properties.geoNameId) {
                return false;
            }

            // compare the evalParams
            for (var key in mainWin.evalParams) {
                if (mainWin.evalParams.hasOwnProperty(key)) {
                    if (features1[i].properties[mainWin.evalParams[key]] !== features2[feature2Index].properties[mainWin.evalParams[key]]) {
                        return false;
                    }
                }
            }

            if (!geoCodingEval.compareSurrogates(features1[i], features2[feature2Index])) {
                return false;
            }

            //            if (typeof features1[i].properties.surrogates !== 'undefined' || typeof features2[feature2Index].properties.surrogates !== 'undefined') {
            //
            //                if (typeof features1[i].properties.surrogates === 'undefined' && typeof features2[feature2Index].properties.surrogates !== 'undefined') {
            //                    return false;
            //                }
            //
            //                if (typeof features1[i].properties.surrogates !== 'undefined' && typeof features2[feature2Index].properties.surrogates === 'undefined') {
            //                    return false;
            //                }
            //
            //                if (features1[i].properties.surrogates.features.length !== features2[feature2Index].properties.surrogates.features.length) {
            //                    return false;
            //                }
            //
            //                var s1 = [], s2 = [];
            //
            //                for (var su = 0; su < features1[i].properties.surrogates.features.length; su++) {
            //                    s1.push(features1[i].properties.surrogates.features[su].properties.geoNameId);
            //                }
            //
            //                for (var su = 0; su < features2[feature2Index].properties.surrogates.features.length; su++) {
            //                    s2.push(features2[feature2Index].properties.surrogates.features[su].properties.geoNameId);
            //                }
            //
            //                if (!($(s1).not(s2).length === 0 && $(s2).not(s1).length === 0)) {
            //                    return false;
            //                }
            //
            //                //see if the surrogates feature arrays are the same.
            //                //This is not working, maybe because of coordinates or other information contained in the GeoJSON.
            ////                if (!($(features1[i].properties.surrogates.features).not(features2[feature2Index].properties.surrogates.features).length === 0
            ////                        && $(features2[feature2Index].properties.surrogates.features).not(features1[i].properties.surrogates.features).length === 0)) {
            ////                    return false;
            ////                }
            //            }
        }
    } else {
        return false;
    }
    return true;
}

//returns true if two features' surrogates are the same
geoCodingEval.compareSurrogates = function (feature1, feature2) {

    //todo: you can parametrize 1 and 2 in something loike [x] so that you eliminate half the code.

    if (typeof feature1.properties.surrogates !== 'undefined' || typeof feature2.properties.surrogates !== 'undefined') {

        if (typeof feature1.properties.surrogates === 'undefined' && typeof feature2.properties.surrogates !== 'undefined') {
            return false;
        }

        if (typeof feature1.properties.surrogates !== 'undefined' && typeof feature2.properties.surrogates === 'undefined') {
            return false;
        }

        if (feature1.properties.surrogates.features.length !== feature2.properties.surrogates.features.length) {
            return false;
        }

        var s1 = [], s2 = [];

        for (var su = 0; su < feature1.properties.surrogates.features.length; su++) {
            s1.push(feature1.properties.surrogates.features[su].properties.geoNameId);
        }

        for (var su = 0; su < feature2.properties.surrogates.features.length; su++) {
            s2.push(feature2.properties.surrogates.features[su].properties.geoNameId);
        }

        if ($(s1).not(s2).length === 0 && $(s2).not(s1).length === 0) {
            return true;
        } else {
            return false;
        }

    } else {
        //there are no surrogates to compare, return true.
        return true;
    }

};



geoCodingEval.compareGeoJsonsNames = function (currentJson, updatedJson) {

    currentJson = jQuery.extend(true, {}, currentJson);
    updatedJson = jQuery.extend(true, {}, updatedJson);

    var response = {};
    response.addedFCollection = {};
    response.removedFCollection = {};
    response.purgedFCollection = {};

    var alreadyCheckedCurrPositions = [];

    if (typeof (currentJson) === 'undefined' || currentJson === null || !currentJson.hasOwnProperty("features")) {
        currentJson = new FeatureCollection([]);
    }

    if (typeof (updatedJson) === 'undefined' || updatedJson === null || !updatedJson.hasOwnProperty("features")) {
        console.log("Alert: the updatedJson fCollection passed to geoCodingEval.compareGeoJsonsNames() is either null, undefined or empty.")
        updatedJson = new FeatureCollection([]);
        //todo: what if the currentJson is also emply? removed would be an empty fcollection
        response.removedFCollection = jQuery.extend(true, {}, currentJson);
        response.purgedFCollection = jQuery.extend(true, {}, updatedJson);
        return response;
    }

    //The initial implementation was based on consolidated places, later added this to remove the bug 
    //in case there are multiple of the same place name in different lines
    //and the user highlights it all at once
    currentJson = geoCodingEval.unConsolidateCommonPlaces(currentJson);
    updatedJson = geoCodingEval.unConsolidateCommonPlaces(updatedJson);

    response.addedFCollection = new FeatureCollection([]);
    response.removedFCollection = new FeatureCollection([]);
    //if nothing is removed, purged is equivalent to current
    response.purgedFCollection = jQuery.extend(true, {}, currentJson);

    for (var i = 0; i < updatedJson.features.length; i++) {
        for (var j = 0; j < updatedJson.features[i].properties.positions.length; j++) {

            var updatedPosition = updatedJson.features[i].properties.positions[j];
            var updatedName = updatedJson.features[i].properties.name;
            var updatedLength = updatedJson.features[i].properties.name.length;
            var onlyInUpdated = true;

            for (var k = 0; k < currentJson.features.length; k++) {
                for (var l = 0; l < currentJson.features[k].properties.positions.length; l++) {

                    var currentPosition = currentJson.features[k].properties.positions[l];
                    var currentName = currentJson.features[k].properties.name;
                    var currentLength = currentJson.features[k].properties.name.length;

                    if (updatedPosition === currentPosition && updatedName.toLowerCase() === currentName.toLowerCase()) {
                        onlyInUpdated = false;
                    }

                    var onlyInCurrent = true;

                    for (var m = 0; m < updatedJson.features.length; m++) {
                        for (var n = 0; n < updatedJson.features[m].properties.positions.length; n++) {
                            var updatedPosition2 = updatedJson.features[m].properties.positions[n];
                            var updatedName2 = updatedJson.features[m].properties.name;
                            var updatedLength2 = updatedJson.features[m].properties.name.length;

                            if (updatedPosition2 === currentPosition && updatedName2.toLowerCase() === currentName.toLowerCase()) {
                                onlyInCurrent = false;
                            }
                        }
                    }


                    if (alreadyCheckedCurrPositions.indexOf(currentJson.features[k].properties.positions[l]) === -1 && onlyInCurrent) {

                        var tempFeature = jQuery.extend(true, {}, currentJson.features[k]);
                        tempFeature.properties = jQuery.extend(true, {}, currentJson.features[k].properties);
                        //tempFeature.properties.positions = jQuery.extend(true, [], [currentJson.features[k].properties.positions[l]]);
                        utils.updateFeaturePos(tempFeature, [currentJson.features[k].properties.positions[l]]);
                        response.removedFCollection.features.push(tempFeature);
                        //response.removedFCollection = geoCodingEval.consolidateCommonPlaces([response.removedFCollection])[0];

                        var purgeFeatureIndex = arrayObjectIndexOf(response.purgedFCollection.features, currentJson.features[k].properties.positions, "properties", "positions");
                        if (purgeFeatureIndex === -1) { alert("En error occured. Please use the reset button to reset the highlights. If you are unhighlighting the same place repeated multiple times in text, use the map to remove that  feature instead, or unhighlight one by one please."); return; }
                        var purgeFeature = response.purgedFCollection.features[purgeFeatureIndex];
                        if (purgeFeature.properties.positions.length === 1) {
                            response.purgedFCollection.features.splice(purgeFeatureIndex, 1);
                        } else { //with unconsolidation, I think this is never reached
                            var posIndex = purgeFeature.properties.positions.indexOf(currentJson.features[k].properties.positions[l]);
                            response.purgedFCollection.features[purgeFeatureIndex].properties.positions.splice(posIndex, 1);
                            utils.updateFeaturePos(response.purgedFCollection.features[purgeFeatureIndex], response.purgedFCollection.features[purgeFeatureIndex].properties.positions);
                        }
                        //response.purgedFCollection = geoCodingEval.consolidateCommonPlaces([response.purgedFCollection])[0];
                        //console.log("Only in Current: " + onlyInCurrent + " " + currentName);
                    }
                    alreadyCheckedCurrPositions.push(currentJson.features[k].properties.positions[l]);
                }
            }
            if (onlyInUpdated) {

                var tempAddFeature = jQuery.extend(true, {}, updatedJson.features[i]);
                //tempAddFeature.properties.positions = jQuery.extend(true, [], [updatedJson.features[i].properties.positions[j]]);
                utils.updateFeaturePos(tempAddFeature, [updatedJson.features[i].properties.positions[j]]);
                response.addedFCollection.features.push(tempAddFeature);
                //response.addedFCollection = geoCodingEval.consolidateCommonPlaces([response.addedFCollection])[0];
                //console.log("Only in updated: " + onlyInUpdated + " " + updatedName);
            }
        }
    }

    response.removedFCollection = geoCodingEval.consolidateCommonPlaces([response.removedFCollection])[0];
    response.addedFCollection = geoCodingEval.consolidateCommonPlaces([response.addedFCollection])[0];
    response.purgedFCollection = geoCodingEval.consolidateCommonPlaces([response.purgedFCollection])[0];
    //console.log("compareGeoJsonNames");
    //console.log(response);
    return response;
};



/* How does 
 
 Each submitted or skipped geocoding results (when you click on submit or skip gets recorded with a number, starting from 1 up for the same tweet with two coders: 1 (Coder A), 2 (Coder B), 3 (Coder A), 4 (Coder B) .....
 
 Each of these will be marked at a separated column with "skipped" in case the submission is a skipped submission.
 
 For evaluation and sending of the tweet to the right coder, the following gets checked:
 
 Compare the last two submissions:
 
 1. If none is skipped, and submissions are equivalent --> mark GCComplete.
 
 2. If both are skipped --> Mark as GCPause (these later will get reviewed, the system will be fixed or decisions will be made and they will be reset to go through the pipeline all over again)
 
 3. If one is skipped, send the document back to the other coder, unless the same person (coder) has skipped the document twice, in which case --> mark GCPause
 
 4. if none are skipped, and the submissions are not equivalent, send back to the coder unless  TotalCount - skippedCount >=4 , in which case --> GCUncertain
 The rational for this one is that if each coder has geocoded the same document twice and there are still disagreements, then these should be discussed and go back to the pipeline again from the beginning.
 
 */