"use strict";

var textManip = {};

function getSelectedRange(activeInput, action) {

    //    if (activeInput === 1 && action !== "typeIn") {
    //        changedActiveInput1 = true;
    //    }

    var sel = rangy.getSelection();
    var ranges = sel.getAllRanges();
    var elem = document.getElementById(activeInput + "TextDiv");



    if (ranges.length !== 1) {
        alert("Please select a place name in text, one at a time.");
        rangy.getSelection().removeAllRanges();
        return;
    }

    for (var i = 0; i < ranges.length; i++) {
        if (!elem.contains(ranges[i].startContainer) || !elem.contains(ranges[i].endContainer)) {
            alert("Please first select a place name  in text, in the textbox outlined with a green border.");
            rangy.getSelection().removeAllRanges();
            return;
        }
    }

    // //Unhighlight
    // if (action === "unhighlight" && !ranges[0].collapsed) {
    //     cssApplier.normalize = true;
    //     cssApplier.undoToRange(ranges[0]);
    //     getOffsets(activeInput);
    //     //rangy.getSelection().removeAllRanges();
    //     return;
    // }

    var start = ranges[0].startContainer;
    var startOff = ranges[0].startOffset;
    var end = ranges[0].endContainer;
    var endOff = ranges[0].endOffset;

    if (action === "unhighlight") {
        if (start.parentNode.nodeName === "SPAN") {
            //while (!ranges[0].collapsed && startOff < start.textContent.length && (start.textContent[startOff] === ' ' || isPunctuation(start.textContent[startOff]))) {
            while (!ranges[0].collapsed && startOff < start.textContent.length && start.textContent[startOff - 1] === ' ') {
                startOff--;
                ranges[0].setStart(start, startOff);
            }
        }

        if (end.parentNode.nodeName === "SPAN") {
            //while (!ranges[0].collapsed && endOff > 0 && (end.textContent[endOff - 1] === ' ' || isPunctuation(end.textContent[endOff - 1]))) {
            while (!ranges[0].collapsed && endOff > 0 && end.textContent[endOff] === ' ') {
                endOff++;
                ranges[0].setEnd(end, endOff);
            }
        }
    } else {
        if (start.nodeType === 3) {  // text node
            //while (!ranges[0].collapsed && startOff < start.textContent.length && (start.textContent[startOff] === ' ' || isPunctuation(start.textContent[startOff]))) {
            while (!ranges[0].collapsed && startOff < start.textContent.length && start.textContent[startOff] === ' ') {
                startOff++;
                ranges[0].setStart(start, startOff);
            }
        }
        if (end.nodeType === 3) {  // text node
            //while (!ranges[0].collapsed && endOff > 0 && (end.textContent[endOff - 1] === ' ' || isPunctuation(end.textContent[endOff - 1]))) {
            while (!ranges[0].collapsed && endOff > 0 && end.textContent[endOff - 1] === ' ') {
                endOff--;
                ranges[0].setEnd(end, endOff);
            }
        }
    }


    //Unhighlight
    if (action === "unhighlight" && !ranges[0].collapsed) {
        cssApplier.normalize = true;
        cssApplier.undoToRange(ranges[0]);
        getOffsets(activeInput, action, ranges[0]);
        rangy.getSelection().removeAllRanges();
        return;
    }


    if (start.parentNode.nodeName === "SPAN") {
        ranges[0].setStart(start, 0);
    }

    if (end.parentNode.nodeName === "SPAN") {
        ranges[0].setEnd(end, rangy.dom.getNodeLength(end));
    }

    if (!ranges[0].collapsed) {
        if (action === "highlight") {
            cssApplier.normalize = true;
            cssApplier.undoToRange(ranges[0]);
            //cssApplier.normalize = false;
            cssApplier.applyToRange(ranges[0]);

        } else if (action === "typeIn") {
            rangy.getSelection().setSingleRange(ranges[0]);
            //getOffsets(activeInput, action, ranges[0]);
            //getSelectionOffsets(activeInput, action, ranges[0]);
        }
        getOffsets(activeInput, action, ranges[0]);
    }
    rangy.getSelection().removeAllRanges();
}


function getOffsets(activeInput, action, range) {

    if (action === "typeIn" && !cssApplier.isAppliedToRange(range)) {
        alert("The selected  piece of text should be highlighted as a place first.");
        return;
    }

    var elem = document.getElementById(activeInput + "TextDiv");

    $("#" + activeInput + "TextDiv").children('span[class=""]').each(function () {
        $(this).replaceWith(this.childNodes);
    });

    // $('span[class=""]').each(function () {
    //     $(this.childNodes).unwrap();
    // });

    var children = elem.childNodes;
    var offsets = [];
    var l = 0;

    for (var i = 0; i < children.length; i++) {
        if (children[i].nodeType === 3) {  // text node
            l += he.decode(children[i].textContent).length;
        }
        else {
            var s = he.decode(children[i].firstChild.textContent).length;
            offsets.push([l, (l + s)]);

            if (action === "typeIn" && (cssApplier.isAppliedToRange(range) && rangy.getSelection().containsNode(children[i].firstChild, true))) {
                offsets = [];
                offsets.push([l, (l + s)]);
                modifyFeature(offsets, activeInput, action);
                return;
                //modiFyFeature(placeName, offsets, activeInput);
            }
            l += s;
        }
    }

    //utils.addToHistory();

    var comparedJsons = geoCodingEval.compareGeoJsonsNames(responseCache.latest.generatedAlternates[activeInput], textManip.offsetsToGeoJson(offsets, activeInput), activeInput);

    textManip.controlHighlight(comparedJsons, activeInput, action);


    //addFeature(offsets, activeInput, action);
}

textManip.controlHighlight = function (comparedJsons, activeInput, action) {
    //check to see if a full name is highlighted instead of a part of a name.
    var scannedResp = textManip.scanTextForName(comparedJsons.addedFCollection, comparedJsons.purgedFCollection, activeInput, true);
    for (var i = 0; i < scannedResp.enrichedFCollection.features.length; i++) {
        var nameIndices = utils.getIndicesOf(scannedResp.enrichedFCollection.features[i].properties.name, he.decode(responseCache.latest.inputTexts[activeInput]), false);
        if (nameIndices.length === 0) {
            var htmlContent = 'This last operation was cancelled, because you  highlighted <span class="selectedPlace">'
                + scannedResp.enrichedFCollection.features[i].properties.name
                + '</span>, which is only part of a word, or contains punctuations (this may have happened as a result of partial unhighlighting of another word). '
                + 'Please make sure to highlight the full word, if the full word refers to a place and do not include punctuations, unless part of the word such as <span class="toRemovePlaces">U.S.A.</span>';

            $("#cancelledDialogId").html(htmlContent);
            $("#cancelledDialogId").dialog({
                modal: true,
                title: 'Warning! Operation Cancelled.',
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
                    duration: 200
                },
                hide: {
                    effect: "explode",
                    duration: 100
                },
                buttons: {
                    'Ok': function () {
                        $(this).dialog('close');
                    }
                }
            });
            $("#cancelledDialogId").dialog("open");
            activateInputItem(activeInput);
            return;
        }
    }


    var includesPunc = false;
    var htmlContent1 = 'You have highlighted <span class="selectedPlace">'

    for (var i = 0; i < comparedJsons.addedFCollection.features.length; i++) {
        if (/[^a-z\s]/i.test(comparedJsons.addedFCollection.features[i].properties.name)) {
            includesPunc = true;
            htmlContent1 += comparedJsons.addedFCollection.features[i].properties.name + '</span>, and '
        }
        //console.log(/^[a-z\s]+$/i.test(str));
        //console.log(!/[^a-z\s]/i.test(str));
    }
    htmlContent1 = htmlContent1.substring(0, htmlContent1.length - 5);
    htmlContent1 += ' which includes punctuations. <br>'
        + 'Are you sure you want to keep  punctuations in the highlighted name? Please only do so if punctuations are part of the place name, such as dots in <span class="toRemovePlaces">U.S.A.</span>';

    if (includesPunc) {
        $("#cancelledDialogId").html(htmlContent1);
        $("#cancelledDialogId").dialog({
            modal: true,
            title: 'Punctuations detected',
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
                duration: 200
            },
            hide: {
                effect: "explode",
                duration: 100
            },
            buttons: {
                'Yes, these punctuatios are part of the place name. Continue.': function () {
                    $(this).dialog('close');
                    textManip.promptUserOnHighlight(comparedJsons, activeInput, action);
                },
                'No, cancel this highlight.': function () {
                    $(this).dialog('close');
                    activateInputItem(activeInput);
                    return;
                }
            }
        });
        $("#cancelledDialogId").dialog("open");
    } else {
        textManip.promptUserOnHighlight(comparedJsons, activeInput, action);
    }
};


textManip.promptUserOnHighlight = function (comparedJsons, activeInput, action) {
    utils.addToHistory();
    var globalUnghighlight = false;
    if (action === "unhighlight") {
        textManip.checkGlobalUnhighlight(comparedJsons, activeInput);
        //globalUnghighlight = confirm("Do you want to remove all highlights of currentyl highlighted -- -- and highlight all instances of -- -- in title and abstract?");
    } else {
        textManip.modifyHighlights(comparedJsons, activeInput, action, globalUnghighlight);
    }
};


textManip.modifyHighlights = function (comparedJsons, activeInput, action, globalUnghighlight) {

    var scannedResp = {}, mergedJsons = {}, selfMerged = {};

    if (action === "unhighlight" && globalUnghighlight === false) {
        //textManip.checkGlobalUnhighlight(comparedJsons, activeInput, true);
        textManip.assignToponym(comparedJsons.addedFCollection, comparedJsons.purgedFCollection, activeInput, true);
    } else if (action === "unhighlight" && globalUnghighlight === true) {
        for (var i = 0; i < comparedJsons.removedFCollection.features.length; i++) {
            utils.removeFromFCollectionByName(comparedJsons.removedFCollection.features[i], comparedJsons.purgedFCollection);
        }
    }  //highlight and global unhighlight
    scannedResp = textManip.scanTextForName(comparedJsons.addedFCollection, comparedJsons.purgedFCollection, activeInput, true);
    mergedJsons = textManip.mergeNamesWithGeoJson(scannedResp.enrichedFCollection, comparedJsons.purgedFCollection, activeInput, false);
    selfMerged = textManip.mergeNamesWithGeoJson(mergedJsons.addedFCollection, mergedJsons.addedFCollection, activeInput, true);
    textManip.assignToponym(selfMerged.addedFCollection, mergedJsons.baseFCollection, activeInput, true);

    //for all input texts other than the current active input, go through the cycle without rendering the map.
    for (var t = 0; t < responseCache.latest.inputTexts.length; t++) {
        if (t !== activeInput) {
            var otherScannedResp = {}, otherMergedJsons = {}, otherSelfMerged = {};
            if (action === "unhighlight" && globalUnghighlight === true) {
                // otherScannedResp.enrichedFCollection = comparedJsons.addedFCollection;
                //textManip.assignToponym(vv, aan, t, false);
                for (var i = 0; i < comparedJsons.removedFCollection.features.length; i++) {
                    utils.removeFromFCollectionByName(comparedJsons.removedFCollection.features[i], responseCache.latest.generatedAlternates[t]);
                }
            }
            otherScannedResp = textManip.scanTextForName(comparedJsons.addedFCollection, responseCache.latest.generatedAlternates[t], t, false);
            otherMergedJsons = textManip.mergeNamesWithGeoJson(otherScannedResp.enrichedFCollection, responseCache.latest.generatedAlternates[t], t, false);
            otherSelfMerged = textManip.mergeNamesWithGeoJson(otherMergedJsons.addedFCollection, otherMergedJsons.addedFCollection, t, true);
            textManip.assignToponym(otherSelfMerged.addedFCollection, otherMergedJsons.baseFCollection, t, false);
        }
    }
};

//not in use. also delete the div in the main UI
textManip.checkGlobalUnhighlight = function (comparedJsons, activeInput) {
    var htmlContent = '';
    if (comparedJsons.addedFCollection && comparedJsons.addedFCollection.features.length > 0) {
        htmlContent += "Do you want to <b>highlight</b> all instances of ";
        for (var i = 0; i < comparedJsons.addedFCollection.features.length; i++) {
            htmlContent += "<span class=\"selectedPlace\">" + comparedJsons.addedFCollection.features[i].properties.name + "</span> and ";
        }
        htmlContent = htmlContent.substring(0, htmlContent.length - 5);
        htmlContent += ",<br> and <b>unhighlight</b> all instances of "
    }
    else {
        htmlContent += "Do you want to <b>unhiglight</b> all instances of ";
    }
    //This if should be unnecessary, since removedFCollection should be populated by the unhighlight action
    if (comparedJsons.removedFCollection && comparedJsons.removedFCollection.features.length > 0) {
        for (var i = 0; i < comparedJsons.removedFCollection.features.length; i++) {
            htmlContent += '<span class="toRemovePlaces">' + comparedJsons.removedFCollection.features[i].properties.name + '</span>';
        }
    }
    htmlContent += "?"
    $("#globalUnhighlightDivId").html(htmlContent);
    $("#globalUnhighlightDivId").dialog({
        modal: true,
        //title: 'Continue?',
        closeOnEscape: false,
        //position: [100, 100],
        open: function (event, ui) {
            $(".ui-dialog-titlebar-close", ui.dialog | ui).hide();
            //$("#globalUnhighlightDivId").html('hello there');
        },
        close: function () {
            $(this).dialog('close');
        },
        show: {
            effect: "blind",
            duration: 200
        },
        hide: {
            effect: "explode",
            duration: 100
        },
        buttons: {
            'Yes to all': function () {
                textManip.modifyHighlights(comparedJsons, activeInput, "unhighlight", true);
                $(this).dialog('close');
            },
            'No, just this one': function () {
                textManip.modifyHighlights(comparedJsons, activeInput, "unhighlight", false);
                $(this).dialog('close');
            }
        }
    });

    $("#globalUnhighlightDivId").dialog("open");
    //$(this).removeClass('hidden');
};


function modifyFeature(offsets, activeInput) {
    //TODO maybe you can consolidate this with the addFeature function

    document.getElementById('submitGcResultsButton').hidden = false;
    document.getElementById('skipGcResultsButton').disabled = false;
    document.getElementById('nextCatButton').hidden = true;
    document.getElementById('previousCatButton').hidden = true;

    //for the typeIn action, there will be only one offset, enforced already. 
    for (var i = 0; i < offsets.length; i++) {
        var charOff = offsets[i][0];
        var placeName = he.decode(responseCache.latest.inputTexts[activeInput]).slice(offsets[i][0], offsets[i][1]);
        var typedInName = prompt("Please type in a name for this instance of \"" + placeName + "\" (in charachter offset " + offsets[i][0] + ") to query Geonames Solr for:");

        if (typedInName === null || typedInName.replace(/\s/g, '').length === 0) {
            return;
        }

        //console.log(offsets[i][0]);
        //console.log(he.decode(responseCache.latest.inputTexts[activeInput]).slice(offsets[i][0], offsets[i][1]));

        $.ajax({
            async: false,
            type: "GET",
            url: "../api/geotxt.json?m=geocoder"
            + "&q="
            + encodeURIComponent(typedInName),
            success: function (results) {
                if (results.features.length > 1) {
                    alert("Warning: more than one feature was returned for this name. Contact the developer.");
                }

                results.features[0].properties.positions[0] = charOff;
                results.features[0].properties.name = placeName;
                results.features[0].properties.typedIn = typedInName;
                for (var a = 0; a < results.features[0].properties.alternates.features.length; a++) {
                    results.features[0].properties.alternates.features[a].properties.positions[0] = charOff;
                    results.features[0].properties.alternates.features[a].properties.typedIn = typedInName;
                    results.features[0].properties.alternates.features[a].properties.name = placeName;
                }

                //TODO: Why are you using the global activeInputText instead of activeInput?
                //get the index of the feature in generatedAlternates
                var featureIndex = arrayObjectIndexOf(responseCache.latest.generatedAlternates[activeInputText].features, [charOff], "properties", "positions");

                responseCache.latest.generatedAlternates[activeInputText].features[featureIndex] = jQuery.extend(true, {}, results.features[0]);

                resetMap();
                renderMap(responseCache.latest.generatedAlternates[activeInputText], mainWin.map);
                //fitMapBounds();

            }, error: function () {
                console.log("AJAX failed.");
                alert("AJAX failed.");
                resetMap();
                fitMapBounds();
            }
        });
    }
}


textManip.assignToponym = function (addedFCollectionParam, baseFCollectionParam, inputIndexOfbaseFCollection, updateMap) {

    //question: is base fCollection always with geonamesID?
    //for the same inputIndex:

    var addedFCollection = jQuery.extend(true, {}, addedFCollectionParam);
    var baseFCollection = jQuery.extend(true, {}, baseFCollectionParam);
    var callCounter = 0, assignmentCounter = addedFCollection.features.length;

    for (var i = 0; i < addedFCollection.features.length; i++) {
        if (addedFCollection.features[i].properties.type === "location") {
            if (addedFCollection.features[i].hasOwnProperty("geometry") && (addedFCollection.features[i].properties.geoNameId !== 'i'
                || addedFCollection.features[i].properties.geoNameId === 'g')) {

                utils.addFeatureToFCollectionByName(addedFCollection.features[i], baseFCollection);

            } else if (!addedFCollection.features[i].hasOwnProperty("geometry") || addedFCollection.features[i].properties.geoNameId === 'i') {
                var found = false
                //MULTIPLE what if there are multiple features with the same name? Which toponym should be assigned?
                if (responseCache.latest.generatedAlternates[inputIndexOfbaseFCollection].hasOwnProperty("features")) {
                    for (var j = 0; j < responseCache.latest.generatedAlternates[inputIndexOfbaseFCollection].features.length; j++) {
                        if (responseCache.latest.generatedAlternates[inputIndexOfbaseFCollection].features[j].properties.name.toLowerCase() === addedFCollection.features[i].properties.name.toLowerCase()) {
                            var tempFeature = $.extend(true, {}, responseCache.latest.generatedAlternates[inputIndexOfbaseFCollection].features[j]);
                            utils.updateFeaturePos(tempFeature, $.extend(true, [], addedFCollection.features[i].properties.positions));
                            baseFCollection.features.push(tempFeature);
                            baseFCollection = geoCodingEval.consolidateCommonPlaces([baseFCollection])[0];
                            utils.sortFeaturesOnFirstPos(baseFCollection);
                            found = true;
                            assignmentCounter--;
                            break;
                        }
                    }
                }
                //MULTIPLE what if there are multiple features with the same name? Which toponym should be assigned?
                //the current generatedAlternates[inputIndexOfBaseFCollection] has already been searched, but doesn't hurth so didn't complicate.
                if (!found) {
                    loop1:
                    for (var k = 0; k < responseCache.latest.generatedAlternates.length; k++) {
                        if (responseCache.latest.generatedAlternates[k].hasOwnProperty("features")) {
                            for (var j = 0; j < responseCache.latest.generatedAlternates[k].features.length; j++) {
                                if (responseCache.latest.generatedAlternates[k].features[j].properties.name.toLowerCase() === addedFCollection.features[i].properties.name.toLowerCase()) {
                                    var tempFeature = $.extend(true, {}, responseCache.latest.generatedAlternates[k].features[j]);
                                    utils.updateFeaturePos(tempFeature, $.extend(true, [], addedFCollection.features[i].properties.positions));
                                    baseFCollection.features.push(tempFeature);
                                    baseFCollection = geoCodingEval.consolidateCommonPlaces([baseFCollection])[0];
                                    utils.sortFeaturesOnFirstPos(baseFCollection);
                                    found = true;
                                    assignmentCounter--;
                                    break loop1;
                                }
                            }
                        }
                    }
                }
                //var promises = [];
                if (!found) {
                    callCounter++;
                    var request = $.ajax({
                        async: true,
                        type: "GET",
                        posArray: addedFCollection.features[i].properties.positions,
                        url: "../api/geotxt.json?m=geocoder"
                        + "&q="
                        + encodeURIComponent(addedFCollection.features[i].properties.name),
                        success: function (results) {
                            if (results.features.length > 1) {
                                alert("Warning: more than one feature was returned for this name. Contact the developer.");
                            }
                            utils.updateFeaturePos(results.features[0], this.posArray);
                            baseFCollection.features.push(results.features[0]);
                            callCounter--;
                            assignmentCounter--;
                            if (callCounter === 0 && assignmentCounter === 0) {
                                responseCache.latest.generatedAlternates[inputIndexOfbaseFCollection] = $.extend(true, {}, baseFCollection);
                                if (updateMap) {
                                    resetMap();
                                    textManip.highlightFromGeoJson(document.getElementById(inputIndexOfbaseFCollection + "TextDiv"), responseCache.latest.generatedAlternates[inputIndexOfbaseFCollection].features, true, rangy, cssApplier, inputIndexOfbaseFCollection);
                                    renderMap(responseCache.latest.generatedAlternates[inputIndexOfbaseFCollection], mainWin.map);
                                }
                            }
                            // if (baseFCollection.features.length === offsets.length) {
                            //     var fCollection = new FeatureCollection(features);
                            //     responseCache.latest.generatedAlternates[activeInput] = jQuery.extend(true, {}, fCollection);
                            //     responseCache.latest.generatedAlternates = geoCodingEval.consolidateCommonPlaces(responseCache.latest.generatedAlternates);
                            //     resetMap();
                            //     //fitMapBounds();
                            //     renderMap(responseCache.latest.generatedAlternates[activeInput], mainWin.map);
                            // }
                        },
                        error: function () {
                            console.log("AJAX failed.");
                            alert("AJAX failed.");
                            //renderMap instead of reset.
                            resetMap();
                            fitMapBounds();
                        }
                    });
                    //promises.push(request);
                }
                //If geotxt returns, after merge, check for ones without geometry. If there is no geometry, have it rendered on map with floating symbols.
                //once user locates those, assign a "g" to the geonameID.
            }
        }
    }
    if (callCounter === 0 && assignmentCounter === 0) {
        responseCache.latest.generatedAlternates[inputIndexOfbaseFCollection] = $.extend(true, {}, baseFCollection);
        if (updateMap) {
            resetMap();
            textManip.highlightFromGeoJson(document.getElementById(inputIndexOfbaseFCollection + "TextDiv"), responseCache.latest.generatedAlternates[inputIndexOfbaseFCollection].features, true, rangy, cssApplier, inputIndexOfbaseFCollection);
            renderMap(responseCache.latest.generatedAlternates[inputIndexOfbaseFCollection], mainWin.map);
        }
    }
    // $.when.apply(null, promises).done(function () {
    //     responseCache.latest.generatedAlternates[inputIndexOfbaseFCollection] = $.extend(true, {}, baseFCollection);
    //     if (updateMap) {
    //         resetMap();
    //         textManip.highlightFromGeoJson(document.getElementById(inputIndexOfbaseFCollection + "TextDiv"), responseCache.latest.generatedAlternates[inputIndexOfbaseFCollection].features, true, rangy, cssApplier, inputIndexOfbaseFCollection);
    //         renderMap(responseCache.latest.generatedAlternates[inputIndexOfbaseFCollection]);
    //     }
    // })
};


//Gets two fCollections, compares the charOffsets and merges names if necessary. Returns the base and added both modified to reflect this.
//should be called on the same input for the added again with checkSelfOverlap as true.
textManip.mergeNamesWithGeoJson = function (addedFCollectionParam, baseFCollectionParam, inputIndexForMerge, checkSelfOverlap) {

    var addedFCollection = jQuery.extend(true, {}, addedFCollectionParam);
    var baseFCollection = jQuery.extend(true, {}, baseFCollectionParam);

    if (typeof (addedFCollection) === 'undefined' || addedFCollection === null || !addedFCollection.hasOwnProperty("features")) {
        addedFCollection = new FeatureCollection([]);
    }

    if (typeof (baseFCollection) === 'undefined' || baseFCollection === null || !baseFCollection.hasOwnProperty("features")) {
        baseFCollection = new FeatureCollection([]);
    }

    addedFCollection = geoCodingEval.unConsolidateCommonPlaces(addedFCollection);
    baseFCollection = geoCodingEval.unConsolidateCommonPlaces(baseFCollection);

    var deleteCandidatesFromBase = [];
    var deleteCandidatesFromAdded = [];

    for (var i = 0; i < baseFCollection.features.length; i++) {
        for (var j = 0; j < baseFCollection.features[i].properties.positions.length; j++) {

            var a = baseFCollection.features[i].properties.positions[j];
            var b = a + baseFCollection.features[i].properties.name.length;

            for (var k = 0; k < addedFCollection.features.length; k++) {
                for (var l = 0; l < addedFCollection.features[k].properties.positions.length; l++) {

                    var c = addedFCollection.features[k].properties.positions[l];
                    var d = c + addedFCollection.features[k].properties.name.length;

                    if (!(c > b || a > d)) {
                        if (d >= a && d <= b && c < a) { //c-b (delete a-b from base, modify c-d to c-b in added.)
                            addedFCollection.features[k].properties.positions[l] = c;
                            utils.updateFeaturePos(addedFCollection.features[k], addedFCollection.features[k].properties.positions);
                            addedFCollection.features[k].properties.name = he.decode(responseCache.latest.inputTexts[inputIndexForMerge]).slice(c, b);
                            addedFCollection.features[k].properties.geoNameId = 'i';
                            deleteCandidatesFromBase.push(i);
                            break;
                        }
                        if (c >= a && d <= b) { // keep a-b in base, delete c-b from added

                            if (!checkSelfOverlap) {
                                deleteCandidatesFromAdded.push(k);
                                break;
                            } else {//for checking self over lap... we will keep all in the added json and return that in fact.

                            }
                        }
                        if ((a >= c && b < d) || (a > c && b <= d)) { //c-d (delete a-b from base, modify c-d to c-d in added.)
                            addedFCollection.features[k].properties.positions[l] = c;
                            utils.updateFeaturePos(addedFCollection.features[k], addedFCollection.features[k].properties.positions);
                            addedFCollection.features[k].properties.name = he.decode(responseCache.latest.inputTexts[inputIndexForMerge]).slice(c, d);
                            addedFCollection.features[k].properties.geoNameId = 'i';
                            deleteCandidatesFromBase.push(i);
                            break;
                        }
                        if (c >= a && c <= b && d > b) { //a-d (delete a-b from base, modify c-d to a-d in added.)
                            addedFCollection.features[k].properties.positions[l] = a;
                            utils.updateFeaturePos(addedFCollection.features[k], addedFCollection.features[k].properties.positions);
                            addedFCollection.features[k].properties.name = he.decode(responseCache.latest.inputTexts[inputIndexForMerge]).slice(a, d);
                            addedFCollection.features[k].properties.geoNameId = 'i';
                            deleteCandidatesFromBase.push(i);
                            break;
                        }
                    }
                }
            }
        }
    }

    //TODO: this is not necessary. There shouldn't be duplicates in the first place. 
    deleteCandidatesFromBase = deleteCandidatesFromBase.filter(utils.onlyUnique);
    deleteCandidatesFromAdded = deleteCandidatesFromAdded.filter(utils.onlyUnique);


    for (var p = deleteCandidatesFromBase.length - 1; p >= 0; p--) {
        baseFCollection.features.splice(deleteCandidatesFromBase[p], 1);
    }

    //for checkSelfOverlap true this is not going to loop in.
    for (var q = deleteCandidatesFromAdded.length - 1; q >= 0; q--) {
        addedFCollection.features.splice(deleteCandidatesFromAdded[q], 1);
    }

    baseFCollection = geoCodingEval.consolidateCommonPlaces([baseFCollection])[0];
    addedFCollection = geoCodingEval.consolidateCommonPlaces([addedFCollection])[0];

    var response = {};

    response.addedFCollection = addedFCollection;

    if (checkSelfOverlap) {
        return response;
    }

    response.baseFCollection = baseFCollection;

    return response;
};


//This function makes a temporary geoJson with just names and positions, for comparison's sake. 
textManip.offsetsToGeoJson = function (offsets, inputIndex) {

    var tempGeoJsonArray = [];
    if (offsets.length === 0) {
        var fCollection = new FeatureCollection([]);
        tempGeoJsonArray.push(fCollection);
    }

    var features = [];

    for (var i = 0; i < offsets.length; i++) {

        var charOff = offsets[i][0];
        var placeName = he.decode(responseCache.latest.inputTexts[inputIndex]).slice(offsets[i][0], offsets[i][1]);

        var posArray = [];
        posArray.push(charOff);
        //A geoNameId of 'i' to make the consolidate function work. 
        var properties = new FeautreProperties(placeName, 'i', posArray, "location");
        var feature = new Feature(properties);
        features.push(feature);

    }

    if (features.length === offsets.length) {
        var fCollection = new FeatureCollection(features);
        tempGeoJsonArray.push(fCollection);
        tempGeoJsonArray = geoCodingEval.consolidateCommonPlaces(tempGeoJsonArray);
    }
    return tempGeoJsonArray[0];
};


textManip.scanTextForName = function (addedFCollectionParam, baseFCollectionParam, inputIndexForScan, isEnrichingSameInputInex) {

    //if added is the same as base, enrich added positions.
    //if added is different from base, create an added fcollection. 
    var addedFCollection = jQuery.extend(true, {}, addedFCollectionParam);
    var baseFCollection = jQuery.extend(true, {}, baseFCollectionParam);

    var indicesObj = {};
    var response = {};

    for (var i = 0; i < addedFCollection.features.length; i++) {

        var indices = [];
        var searchName = addedFCollection.features[i].properties.name;

        //scan names in text. get their offsets.
        //compaer the offsets agaisnt baseFCollection AND addedFCollection. Adds newly highlighted similar names to the enrichedFCollection in response.
        indices = indices.concat(utils.getIndicesOf(searchName, he.decode(responseCache.latest.inputTexts[inputIndexForScan]), false));

        //remove the same highlighted name from the indices.
        for (var j = 0; j < addedFCollection.features[i].properties.positions.length; j++) {
            var posIndex = indices.indexOf(addedFCollection.features[i].properties.positions[j]);
            if (posIndex !== -1) {
                indices.splice(posIndex, 1);
            }
        }

        //remove indices that are already in the baseFCollection FOR THE SAME NAME
        for (var k = 0; k < baseFCollection.features.length; k++) {
            for (var l = 0; l < baseFCollection.features[k].properties.positions.length; l++) {
                var posIndex = indices.indexOf(baseFCollection.features[k].properties.positions[l]);
                if (posIndex !== -1 && baseFCollection.features[k].properties.name.toLowerCase() === searchName.toLowerCase()) {
                    indices.splice(posIndex, 1);
                }
            }
        }

        //todo... moved this inside the loop. Is this correct?
        indicesObj[searchName] = jQuery.extend(true, [], indices);
    }



    if (isEnrichingSameInputInex) {
        response.enrichedFCollection = jQuery.extend(true, {}, addedFCollection);
    } else {
        response.enrichedFCollection = new FeatureCollection([]);
    }

    for (var strName in indicesObj) {
        if (indicesObj.hasOwnProperty(strName)) {
            //TODO: MULTITOPONYM what if there is more than one toponym for the same name? it should probably be dealt with at the mergeNamesWithGeoJson function, or the function that gets called after that to assign toponyms.
            var tempIndex = utils.caselessIndexOf(response.enrichedFCollection.features, strName, "properties", "name");
            if (tempIndex > -1 && response.enrichedFCollection.features[tempIndex].properties.type.toLowerCase() === "location") { //This is for isEnrichingSameInputInex
                for (var z = 0; z < indicesObj[strName].length; z++) {
                    utils.addPositionsToFeature(response.enrichedFCollection.features[tempIndex], [indicesObj[strName][z]]);
                    // response.enrichedFCollection.features[tempIndex].properties.positions.push(indicesObj[strName][z]);
                    // response.enrichedFCollection.features[tempIndex].properties.positions.sort(function (a, b) {
                    //     return a - b;
                    // });
                }
            } else { //This is for !isEnrichingSameInputInex
                for (var z = 0; z < indicesObj[strName].length; z++) {
                    var posArray = [indicesObj[strName][z]];
                    //A geoNameId of i to make the consolidate function work. 
                    var properties = new FeautreProperties(strName, 'i', posArray, "location");
                    var feature = new Feature(properties);
                    response.enrichedFCollection.features.push(feature);
                    response.enrichedFCollection = geoCodingEval.consolidateCommonPlaces([response.enrichedFCollection])[0];
                }
            }
        }
    }

    return response;

};


textManip.highlightFromGeoJson = function (elem, features, resetFirst, rangy, cssApplier, activeInput) {

    if (resetFirst === true && activeInput !== undefined) {
        resetHighlights(activeInput, false);
    }

    var offsets = {}, startOffsets = [];

    for (var j = 0; j < features.length; j++) {
        //TODO what if it's not a place name? At the moment, if GeoTxt returns no geometry (location type feature) for the place identified, the type doesn't get assigned as location
        if (features[j].properties.type.toLowerCase() === "location") {
            for (var p = 0; p < features[j].properties.positions.length; p++) {
                offsets[features[j].properties.positions[p]] = (features[j].properties.positions[p] + features[j].properties.name.length);
                startOffsets.push(features[j].properties.positions[p]);
            }
        }
    }

    //Sort descending
    startOffsets.sort(function (a, b) {
        return b - a;
    });

    for (var i = 0; i < startOffsets.length; i++) {

        var range = rangy.createRangyRange();
        range.selectNode(elem);

        var startOff = startOffsets[i];
        var endOff = offsets[startOffsets[i]];
        range.setStart(elem.firstChild, startOff);
        range.setEnd(elem.firstChild, endOff);

        cssApplier.normalize = true;
        cssApplier.undoToRange(range);
        //cssApplier.normalize = false;
        cssApplier.applyToRange(range);
    }
};


textManip.callEnrichUponLoad = function () {
    textManip.enrichUponLoading(0, 0, false);
    textManip.enrichUponLoading(0, 1, true);
    textManip.enrichUponLoading(1, 0, false);
    textManip.enrichUponLoading(1, 1, false);
    responseCache.original = $.extend(true, {}, responseCache.latest); //used for resetting annotations
};


textManip.resetAnnotations = function () {
    utils.addToHistory();
    responseCache.latest.generatedAlternates = $.extend(true, [], responseCache.original.generatedAlternates);
    activateInputItem(0);
};


textManip.enrichUponLoading = function (toEnrichIndex, enrichFromIndex, renderMapParam) {
    var selfEnrich = false;
    if (toEnrichIndex === enrichFromIndex) selfEnrich = true;
    var toEnrich = responseCache.latest.generatedAlternates[toEnrichIndex];
    var enrichFrom = responseCache.latest.generatedAlternates[enrichFromIndex];
    var scannedResp = {}, mergedJsons = {}, selfMerged = {};
    scannedResp = textManip.scanTextForName(enrichFrom, toEnrich, toEnrichIndex, selfEnrich);
    mergedJsons = textManip.mergeNamesWithGeoJson(scannedResp.enrichedFCollection, toEnrich, toEnrichIndex, false);
    selfMerged = textManip.mergeNamesWithGeoJson(mergedJsons.addedFCollection, mergedJsons.addedFCollection, toEnrichIndex, true);
    textManip.assignToponym(selfMerged.addedFCollection, mergedJsons.baseFCollection, toEnrichIndex, renderMapParam);
};


function resetHighlights(activeInput, clearGeoJson) {

    if (clearGeoJson === true) {
        utils.addToHistory();
        responseCache.latest.generatedAlternates[activeInput] = new FeatureCollection();
        resetMap();
        fitMapBounds();
    }

    var elem = document.getElementById(activeInput + "TextDiv");
    //TODO: do you even need these? You are removing all children elements
    //    var range = rangy.createRangyRange();
    //    range.selectNode(elem);
    //    cssApplier.undoToRange(range);

    while (elem.hasChildNodes()) {
        elem.removeChild(elem.firstChild);
    }
    elem.innerHTML = responseCache.latest.inputTexts[activeInput];

}

//TODO not used at the moment?
function isPunctuation(c) {
    var cc = c.charCodeAt(0);
    if ((cc >= 20 && cc <= 0x2F) ||
        (cc >= 0x3A && cc <= 0x40) ||
        (cc >= 0x5B && cc <= 0x60) ||
        (cc >= 0x7B && cc <= 0x7E)) {

        return true;
    }
    return false;
}


function updateInfoDiv(resetInfo, removeTable) {
    var table = document.getElementById("infoTable");

    if (resetInfo === true) {
        while (table.hasChildNodes()) {
            table.removeChild(table.firstChild);
        }
        if (removeTable === true) {
            return;
        }
    }

    var r = 0;

    if (responseCache.original.metaData.hasOwnProperty("resolutionType")) {

        var startingTag = "", endingTag = "</span>", resType = "";

        if (responseCache.original.metaData.resolutionType === "PIContainsUnresolved") {
            startingTag = '<span style="color:red">';
            resType = "Contains unresolved";
        } else if (responseCache.original.metaData.resolutionType === "PIManualResolution") {
            startingTag = '<span style="color:orange">';
            resType = "Manually resolved";
        }

        row = table.insertRow(r++);
        cell1 = row.insertCell(0);
        cell2 = row.insertCell(1);
        cell1.innerHTML = startingTag + "Resolution Type" + endingTag;
        cell2.innerHTML = startingTag + "<b>" + resType + "</b>" + endingTag;
    }

    var row = table.insertRow(r++);
    var cell1 = row.insertCell(0);
    var cell2 = row.insertCell(1);
    cell1.innerHTML = "Tweet  ID: ";
    cell2.innerHTML = "<a href='https://twitter.com/" + responseCache.original.metaData.screenName + "/status/" + documentId + "' target='_blank'>" + documentId + " (click to see on Twitter)</a>";

    row = table.insertRow(r++);
    cell1 = row.insertCell(0);
    cell2 = row.insertCell(1);
    cell1.innerHTML = "This tweet was created at: ";
    cell2.innerHTML = responseCache.original.metaData.createdAt;

    row = table.insertRow(r++);
    cell1 = row.insertCell(0);
    cell2 = row.insertCell(1);
    cell1.innerHTML = "Screen Name ";
    cell2.innerHTML = "<a href='https://twitter.com/" + responseCache.original.metaData.screenName + "' target='_blank'>" + responseCache.original.metaData.name + " (" + responseCache.original.metaData.screenName + ")</a>";

    row = table.insertRow(r++);
    cell1 = row.insertCell(0);
    cell2 = row.insertCell(1);
    cell1.innerHTML = "User Description";
    cell2.innerHTML = responseCache.original.metaData.description;

    row = table.insertRow(r++);
    cell1 = row.insertCell(0);
    cell2 = row.insertCell(1);
    cell1.innerHTML = "--------------";
    cell2.innerHTML = "--------------";

    row = table.insertRow(r++);
    cell1 = row.insertCell(0);
    cell2 = row.insertCell(1);
    cell1.innerHTML = "Identified Places by AMT workers: ";
    cell2.innerHTML = responseCache.original.identifiedPlaces;

    row = table.insertRow(r++);
    cell1 = row.insertCell(0);
    cell2 = row.insertCell(1);
    cell1.innerHTML = "Stage of this document:";
    cell2.innerHTML = (stage).toString() + " (It's the " + (stage).toString() + "th time this document is visited by its coders).";


    for (var i = 0; i < responseCache.original.allCodings.length; i++) {
        if (responseCache.original.allCodings[i].skipped === true) {
            row = table.insertRow(r++);
            cell1 = row.insertCell(0);
            cell2 = row.insertCell(1);
            cell1.innerHTML = responseCache.original.allCodings[i].geoCoder + " skipped this document ";
            cell2.innerHTML = "in role " + responseCache.original.allCodings[i].role + " at stage " + responseCache.original.allCodings[i].stage;
        }
    }

    row = table.insertRow(r++);
    cell1 = row.insertCell(0);
    cell2 = row.insertCell(1);
    cell1.innerHTML = "Your total doc submissions:";
    cell2.innerHTML = (submissionCountPerSession).toString() + " in this session";

}

//TODO move to a util file
function FeatureCollection(features) {
    this.type = "FeatureCollection";
    if (typeof (features) === "undefined" || features === null) {
        this.features = [];
    } else {
        this.features = features;
    }
}

//geometry is optional
function Feature(properties, geometry) {
    this.properties = properties;
    this.type = 'Feature';
    if (typeof (geometry) !== 'undefined') {
        this.geometry = geometry;
    }
}


function FeautreProperties(name, geoNameId, positions, type) {
    this.name = name;
    this.geoNameId = geoNameId;
    this.positions = $.extend(true, [], positions);
    this.type = type
}


textManip.FeautrePointGeometry = function (lng, lat) {
    this.type = 'Point';
    this.coordinates = [lng, lat];
};


textManip.getExcerpt = function (positions, name, inputText) {

    var excerpt = '', exerpts = [];
    var charPadNum = 15;

    for (var i = 0; i < positions.length; i++) {
        excerpt = '';
        excerpt += '<br><span class="excerptBorder">';
        //TODO get the inputTexts through parameters, don't use global variables.
        if (he.decode(inputText).substring(positions[i] - charPadNum + 1, positions[i] - charPadNum).indexOf(' ') === -1
            && he.decode(inputText).substring(positions[i] - charPadNum + 1, positions[i] - charPadNum)) {
            excerpt += '..';
        }
        excerpt += he.encode(he.decode(inputText).substring(positions[i] - charPadNum, positions[i]));
        excerpt += '<span class="selectedPlace">' + he.encode(he.decode(inputText).substring(positions[i], positions[i] + name.length)) + '</span>';
        excerpt += he.encode(he.decode(inputText).substring(positions[i] + name.length, positions[i] + name.length + charPadNum));

        if (he.decode(inputText).substring(name.length + positions[i] + charPadNum, positions[i] + name.length + charPadNum + 1).indexOf(' ') === -1
            && he.decode(inputText).substring(name.length + positions[i] + charPadNum, positions[i] + name.length + charPadNum + 1)) {
            excerpt += '..';
        }
        excerpt += '</span>';
        exerpts.push(excerpt);
    }
    return exerpts;
};


//not in use
// function addFeatureOld(offsets, activeInput) {

//     if (offsets.length === 0) {
//         var fCollection = new FeatureCollection([]);
//         responseCache.latest.generatedAlternates[activeInput] = jQuery.extend(true, {}, fCollection);
//         resetMap();
//         fitMapBounds();
//     }

//     var features = [];

//     for (var i = 0; i < offsets.length; i++) {
//         var charOff = offsets[i][0];
//         var placeName = he.decode(responseCache.latest.inputTexts[activeInput]).slice(offsets[i][0], offsets[i][1]);


//         //console.log(offsets[i][0]);
//         //console.log(he.decode(responseCache.latest.inputTexts[activeInput]).slice(offsets[i][0], offsets[i][1]));

//         //todo: if it's the same place name, just combine them in position [] especially in articles.
//         $.ajax({
//             async: true,
//             type: "GET",
//             charOffParam: charOff,
//             url: "../api/geotxt.json?m=geocoder"
//             + "&q="
//             + encodeURIComponent(placeName),
//             success: function (results) {
//                 if (results.features.length > 1) {
//                     alert("Warning: more than one feature was returned for this name. Contact the developer.");
//                 }

//                 results.features[0].properties.positions[0] = this.charOffParam;
//                 for (var a = 0; a < results.features[0].properties.alternates.features.length; a++) {
//                     results.features[0].properties.alternates.features[a].properties.positions[0] = this.charOffParam;
//                     //add the feature to fcollection while checking for others 
//                 }

//                 features.push(results.features[0]);

//                 if (features.length === offsets.length) {
//                     var fCollection = new FeatureCollection(features);
//                     responseCache.latest.generatedAlternates[activeInput] = jQuery.extend(true, {}, fCollection);
//                     responseCache.latest.generatedAlternates = geoCodingEval.consolidateCommonPlaces(responseCache.latest.generatedAlternates);
//                     resetMap();
//                     //fitMapBounds();
//                     renderMap(responseCache.latest.generatedAlternates[activeInput], mainWin.map);
//                 }

//             },
//             error: function () {
//                 console.log("AJAX failed.");
//                 alert("AJAX failed.");
//                 resetMap();
//                 fitMapBounds();
//             }
//         });
//     }
// }

