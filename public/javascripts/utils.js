"use strict";

var utils = {};

utils.offsetMarkerZIndex = function (layerObj) {
    //use of main win global varialbe Layer

    var zIndexes = [];

    // a wiser way of getting each layer than what is below for getting the zIndexes.
    //  layer.eachLayer(function (l) {
    //         l.bindPopup('Hello');
    //     });

    var layersArray = layer.getLayers();
    for (var i = 0; i < layersArray.length; i++) {
        if (layersArray[i].hasOwnProperty("_zIndex")) {
            zIndexes.push(layersArray[i]._zIndex);
        }
    }
    if (zIndexes.length > 0) {
        var maxZIndex = zIndexes.reduce(function (a, b) {
            return Math.max(a, b);
        });
        layerObj.setZIndexOffset(1 + maxZIndex);
    }
};

utils.pointToLayer = function (feature, latlng) {

    var styleClass = utils.getStyleClassName(feature);

    return L.marker(latlng, {
        icon: new L.divIcon({
            className: styleClass
        })
    });
};


utils.getStyleClassName = function (feature) {
    var styleClass = "divNormal";

    if (feature.properties.uncertainLocation) {
        styleClass += " uncertainLoc";
    }
    if (feature.properties.uncertainSemantics) {
        styleClass += " uncertainSemantic";
    }
    if (feature.properties.impreciseLocation) {
        styleClass += " impreciseLoc";
    }
    if (feature.properties.vagueLocation) {
        styleClass += " vagueLoc";
    }
    if (feature.properties.isGeogFocus) {
        styleClass += " divFocus";
    }
    if (typeof feature.properties.surrogates !== 'undefined' && feature.properties.surrogates.features.length > 0) {
        styleClass += " hasSurrogates";
    }

    //These are for Alternates rendering
    if (feature.properties.isCurrentlyPicked) {
        styleClass += " currentlyPicked";
    }

    if (feature.properties.isSurrogate) {
        styleClass += " isSurrogate";
    }

    return styleClass;
};


utils.addPositionsToFeature = function (feature, positionsArray) {

    //todo: make sure there are no duplicated positions.
    for (var i = 0; i < positionsArray.length; i++) {
        if (feature.properties.positions.indexOf(positionsArray[i]) === -1) {
            feature.properties.positions.push(positionsArray[i]);
            feature.properties.positions.sort(function (a, b) {
                return a - b;
            });
        } else {
            alert("WARNING! THIS POSITION ALREADY EXISTS!LET DEVELOPER KNOW");
            console.log("WARNING! THIS POSITION ALREADY EXISTS! ")
        }
    }
    utils.updateFeaturePos(feature, feature.properties.positions);
};


//This function searches for the same names in the fCollection as the feature, and adds the feature to fCollecion
utils.addFeatureToFCollectionByName = function (featureParam, fCollection) {

    var feature = $.extend(true, {}, featureParam);
    feature.properties = $.extend(true, {}, featureParam.properties);

    //TODO: MULTITOPONYM what if there is more than one toponym for the same name? it should probably be dealt with at the mergeNamesWithGeoJson function, or the function that gets called after that to assign toponyms.
    var tempIndex = utils.caselessIndexOf(fCollection.features, feature.properties.name.toLowerCase(), "properties", "name");
    if (tempIndex > -1 && fCollection.features[tempIndex].properties.type === "location") {
        utils.addPositionsToFeature(fCollection.features[tempIndex], feature.properties.positions);
    } else {
        fCollection.features.push(feature);
    }

    fCollection = geoCodingEval.consolidateCommonPlaces([fCollection])[0];
    utils.sortFeaturesOnFirstPos(fCollection);

};


utils.removeFromFCollectionByName = function (featureToRemoveByName, fCollectionByReference) {
    var featureIndexByName = utils.getLocFeatureIndexByProperty(featureToRemoveByName, "name", fCollectionByReference);
    if (featureIndexByName >= 0) {
        fCollectionByReference.features.splice(featureIndexByName, 1);
    }
};

//note: this function works with REGEX. only full word matches... not part of word. Not good for twitter hashtags etc
utils.getIndicesOf = function (searchStr, containingStr, caseSensitive) {
    containingStr = he.decode(containingStr);
    var searchStrLen = searchStr.length;
    if (searchStrLen == 0) {
        return [];
    }
    var match = "", indices = [];
    if (!caseSensitive) {
        containingStr = containingStr.toLowerCase();
        searchStr = searchStr.toLowerCase();
    }
    var regex = new RegExp('\\b' + searchStr + '\\b', 'gi');
    while (match = regex.exec(containingStr)) {
        indices.push(match.index);
        //regex.lastIndex
    }
    return indices;
};


utils.sortFeaturesOnFirstPos = function (fCollectoin) {
    fCollectoin.features.sort(function (a, b) { return a.properties.positions[0] - b.properties.positions[0] });
};


function arrayObjectIndexOf(myArray, searchTerm, properties, property) {

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
}


utils.caselessIndexOf = function (myArray, searchTerm, properties, property) {

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
            if (myArray[i][properties][property].toLowerCase() === searchTerm.toLowerCase())
                return i;
        }
        return -1;
    }
};


utils.onlyUnique = function (value, index, self) {
    return self.indexOf(value) === index;
};


//not in use at the moment?
utils.addFCollectionToHistory = function (fCollectionParam, inputIndexParam) {
    var tempGeneratedAlternates = [];
    for (var i = 0; i < responseCache.latest.generatedAlternates.length; i++) {
        if (i !== inputIndexParam) {
            tempGeneratedAlternates[i] = $.extend(true, {}, responseCache.latest.generatedAlternates[i]);
        }
    }
    tempGeneratedAlternates[inputIndexParam] = $.extend(true, {}, fCollectionParam);
    utils.addToHistory(tempGeneratedAlternates);
};


utils.addToHistory = function (generatedAlternatesArray) {
    if (typeof (generatedAlternatesArray) === "undefined") {
        generatedAlternatesArray = responseCache.latest.generatedAlternates;
    }
    responseCache.modifyCount++;
    console.log("modifyCount is: " + responseCache.modifyCount);
    responseCache.history.push($.extend(true, [], generatedAlternatesArray));
    responseCache.historyInput.push(activeInputText);
};


utils.undoLast = function (activeInputParam) {
    if (responseCache.historyInput.length > 0 && responseCache.historyInput[responseCache.historyInput.length - 1] !== activeInputText) {
        var curInputName = activeInputText === 0 ? "Title" : "Abstract";
        var otherInputName = activeInputText === 1 ? "Title" : "Abstract";
        alert("Nothing to undo for " + curInputName + ". Switch to " + otherInputName + " if you want to continue undoing the last operation.");
        return;
    }
    if (responseCache.history.length > 0) {
        responseCache.latest.generatedAlternates = $.extend(true, [], responseCache.history[responseCache.history.length - 1]);
        responseCache.history.splice(responseCache.history.length - 1, 1);
        responseCache.historyInput.splice(responseCache.historyInput.length - 1, 1);
        resetMap();
        renderMap(responseCache.latest.generatedAlternates[activeInputParam], mainWin.map);
        //todo: highlight both
        // for (var i = 0; responseCache.latest.generatedAlternates.length; i++) {
        textManip.highlightFromGeoJson(document.getElementById(activeInputParam + "TextDiv"), responseCache.latest.generatedAlternates[activeInputParam].features, true, rangy, cssApplier, activeInputParam);
        //}
    } else {
        alert("Nothing to undo.")
    }
};


utils.checkCtrlZ = function (e) {
    var evtobj = window.event ? event : e
    if (evtobj.keyCode == 90 && evtobj.ctrlKey) utils.undoLast(activeInputText);
};


utils.getLocFeatureIndexByProperty = function (featureParam, propertyName, fCollectionParam) {

    var fIndex = -1;
    if (propertyName === "name") {
        fIndex = utils.caselessIndexOf(fCollectionParam.features, featureParam.properties[propertyName].toLowerCase(), "properties", propertyName);
    }
    else {
        fIndex = arrayObjectIndexOf(fCollectionParam.features, fCollectionParam.features, featureParam.properties[propertyName], "properties", propertyName);
    }
    if (fIndex >= 0) {
        if (fCollectionParam.features[fIndex].properties.type.toLowerCase() === "location"
            && fCollectionParam.features[fIndex].properties.geoNameId === featureParam.properties.geoNameId) {
            return fIndex;
        } else {
            return -1;
        }
    } else {
        return -1;
    }
};


utils.updateFeaturePos = function (feature, posArray) {
    var pos = $.extend(true, [], posArray);
    pos.sort(function (a, b) {
        return a - b;
    });
    feature.properties.positions = pos;
    if (typeof (feature.properties.alternates) !== 'undefined' && feature.properties.alternates !== null) {
        for (var a = 0; a < feature.properties.alternates.features.length; a++) {
            feature.properties.alternates.features[a].properties.positions = pos;
        }
    }
    if (typeof (feature.properties.surrogates) !== 'undefined' && feature.properties.surrogates !== null) {
        for (var a = 0; a < feature.properties.surrogates.features.length; a++) {
            feature.properties.surrogates.features[a].properties.positions = pos;
        }
    }
};


utils.getBrowserInfo = function () {

    var info = {};
    //this one cross checkes the info using another function. Edge seems to be using misleading user agent.
    info.NameVersion = utils.getBrowserNameVersion();

    var nVer = navigator.appVersion;
    var nAgt = navigator.userAgent;
    var browserName = navigator.appName;
    var fullVersion = '' + parseFloat(navigator.appVersion);
    var majorVersion = parseInt(navigator.appVersion, 10);
    var nameOffset, verOffset, ix;

    // In Opera, the true version is after "Opera" or after "Version"
    if ((verOffset = nAgt.indexOf("Opera")) !== -1) {
        browserName = "Opera";
        fullVersion = nAgt.substring(verOffset + 6);
        if ((verOffset = nAgt.indexOf("Version")) !== -1)
            fullVersion = nAgt.substring(verOffset + 8);
    }
    // In MSIE, the true version is after "MSIE" in userAgent
    else if ((verOffset = nAgt.indexOf("MSIE")) !== -1) {
        browserName = "Microsoft Internet Explorer";
        fullVersion = nAgt.substring(verOffset + 5);
    }
    // In Chrome, the true version is after "Chrome" 
    else if ((verOffset = nAgt.indexOf("Chrome")) !== -1) {
        browserName = "Chrome";
        fullVersion = nAgt.substring(verOffset + 7);
    }
    // In Safari, the true version is after "Safari" or after "Version" 
    else if ((verOffset = nAgt.indexOf("Safari")) !== -1) {
        browserName = "Safari";
        fullVersion = nAgt.substring(verOffset + 7);
        if ((verOffset = nAgt.indexOf("Version")) !== -1)
            fullVersion = nAgt.substring(verOffset + 8);
    }
    // In Firefox, the true version is after "Firefox" 
    else if ((verOffset = nAgt.indexOf("Firefox")) !== -1) {
        browserName = "Firefox";
        fullVersion = nAgt.substring(verOffset + 8);
    }
    // In most other browsers, "name/version" is at the end of userAgent 
    else if ((nameOffset = nAgt.lastIndexOf(' ') + 1) <
        (verOffset = nAgt.lastIndexOf('/'))) {
        browserName = nAgt.substring(nameOffset, verOffset);
        fullVersion = nAgt.substring(verOffset + 1);
        if (browserName.toLowerCase() == browserName.toUpperCase()) {
            browserName = navigator.appName;
        }
    }
    // trim the fullVersion string at semicolon/space if present
    if ((ix = fullVersion.indexOf(";")) !== -1)
        fullVersion = fullVersion.substring(0, ix);
    if ((ix = fullVersion.indexOf(" ")) !== -1)
        fullVersion = fullVersion.substring(0, ix);

    majorVersion = parseInt('' + fullVersion, 10);
    if (isNaN(majorVersion)) {
        fullVersion = '' + parseFloat(navigator.appVersion);
        majorVersion = parseInt(navigator.appVersion, 10);
    }

    var OSName = "Unknown OS";
    if (navigator.appVersion.indexOf("Win") !== -1)
        OSName = "Windows";
    if (navigator.appVersion.indexOf("Mac") !== -1)
        OSName = "MacOS";
    if (navigator.appVersion.indexOf("X11") !== -1)
        OSName = "UNIX";
    if (navigator.appVersion.indexOf("Linux") !== -1)
        OSName = "Linux";

    info.browserName = browserName;
    info.fullVersion = fullVersion;
    info.majorVersion = majorVersion;
    info.osName = OSName;
    info.appName = navigator.appName;
    info.appCodeName = navigator.appCodeName;
    info.userAgent = navigator.userAgent;
    info.appVersion = navigator.appVersion;
    info.platform = navigator.platform;
    info.language = navigator.language;
    info.product = navigator.product;
    info.cookieEnabled = navigator.cookieEnabled;
    info.currentDateTime = new Date();

    return info;

};


utils.getBrowserNameVersion = function () {
    var ua = navigator.userAgent, tem,
        M = ua.match(/(opera|chrome|safari|firefox|msie|trident(?=\/))\/?\s*(\d+)/i) || [];
    if (/trident/i.test(M[1])) {
        tem = /\brv[ :]+(\d+)/g.exec(ua) || [];
        return 'IE ' + (tem[1] || '');
    }
    if (M[1] === 'Chrome') {
        tem = ua.match(/\b(OPR|Edge)\/(\d+)/);
        if (tem != null) return tem.slice(1).join(' ').replace('OPR', 'Opera');
    }
    M = M[2] ? [M[1], M[2]] : [navigator.appName, navigator.appVersion, '-?'];
    if ((tem = ua.match(/version\/(\d+)/i)) != null) M.splice(1, 1, tem[1]);
    return M.join(' ');
};


//TODO move to a util file. Move feature codes to a seperate text file (or JS file).
utils.getFullFeatureCode = function (featureCode) {

    var featureCodeLookUp = {
        ADM1: "first-order administrative division",
        ADM1H: "historical first-order administrative division",
        ADM2: "second-order administrative division",
        ADM2H: "historical second-order administrative division",
        ADM3: "third-order administrative division",
        ADM3H: "historical third-order administrative division",
        ADM4: "fourth-order administrative division",
        ADM4H: "historical fourth-order administrative division",
        ADM5: "fifth-order administrative division",
        ADMD: "administrative division",
        ADMDH: "historical administrative division",
        LTER: "leased area",
        PCL: "political entity",
        PCLD: "dependent political entity",
        PCLF: "freely associated state",
        PCLH: "historical political entity",
        PCLI: "independent political entity",
        PCLIX: "section of independent political entity",
        PCLS: "semi-independent political entity",
        PRSH: "parish",
        TERR: "territory",
        ZN: "zone",
        ZNB: "buffer zone",
        AIRS: "seaplane landing area",
        ANCH: "anchorage",
        BAY: "bay",
        BAYS: "bays",
        BGHT: "bight(s)",
        BNK: "bank(s)",
        BNKR: "stream bank",
        BNKX: "section of bank",
        BOG: "bog(s)",
        CAPG: "icecap",
        CHN: "channel",
        CHNL: "lake channel(s)",
        CHNM: "marine channel",
        CHNN: "navigation channel",
        CNFL: "confluence",
        CNL: "canal",
        CNLA: "aqueduct",
        CNLB: "canal bend",
        CNLD: "drainage canal",
        CNLI: "irrigation canal",
        CNLN: "navigation canal(s)",
        CNLQ: "abandoned canal",
        CNLSB: "underground irrigation canal(s)",
        CNLX: "section of canal",
        COVE: "cove(s)",
        CRKT: "tidal creek(s)",
        CRNT: "current",
        CUTF: "cutoff",
        DCK: "dock(s)",
        DCKB: "docking basin",
        DOMG: "icecap dome",
        DPRG: "icecap depression",
        DTCH: "ditch",
        DTCHD: "drainage ditch",
        DTCHI: "irrigation ditch",
        DTCHM: "ditch mouth(s)",
        ESTY: "estuary",
        FISH: "fishing area",
        FJD: "fjord",
        FJDS: "fjords",
        FLLS: "waterfall(s)",
        FLLSX: "section of waterfall(s)",
        FLTM: "mud flat(s)",
        FLTT: "tidal flat(s)",
        GLCR: "glacier(s)",
        GULF: "gulf",
        GYSR: "geyser",
        HBR: "harbor(s)",
        HBRX: "section of harbor",
        INLT: "inlet",
        INLTQ: "former inlet",
        LBED: "lake bed(s)",
        LGN: "lagoon",
        LGNS: "lagoons",
        LGNX: "section of lagoon",
        LK: "lake",
        LKC: "crater lake",
        LKI: "intermittent lake",
        LKN: "salt lake",
        LKNI: "intermittent salt lake",
        LKO: "oxbow lake",
        LKOI: "intermittent oxbow lake",
        LKS: "lakes",
        LKSB: "underground lake",
        LKSC: "crater lakes",
        LKSI: "intermittent lakes",
        LKSN: "salt lakes",
        LKSNI: "intermittent salt lakes",
        LKX: "section of lake",
        MFGN: "salt evaporation ponds",
        MGV: "mangrove swamp",
        MOOR: "moor(s)",
        MRSH: "marsh(es)",
        MRSHN: "salt marsh",
        NRWS: "narrows",
        OCN: "ocean",
        OVF: "overfalls",
        PND: "pond",
        PNDI: "intermittent pond",
        PNDN: "salt pond",
        PNDNI: "intermittent salt pond(s)",
        PNDS: "ponds",
        PNDSF: "fishponds",
        PNDSI: "intermittent ponds",
        PNDSN: "salt ponds",
        POOL: "pool(s)",
        POOLI: "intermittent pool",
        RCH: "reach",
        RDGG: "icecap ridge",
        RDST: "roadstead",
        RF: "reef(s)",
        RFC: "coral reef(s)",
        RFX: "section of reef",
        RPDS: "rapids",
        RSV: "reservoir(s)",
        RSVI: "intermittent reservoir",
        RSVT: "water tank",
        RVN: "ravine(s)",
        SBKH: "sabkha(s)",
        SD: "sound",
        SEA: "sea",
        SHOL: "shoal(s)",
        SILL: "sill",
        SPNG: "spring(s)",
        SPNS: "sulphur spring(s)",
        SPNT: "hot spring(s)",
        STM: "stream",
        STMA: "anabranch",
        STMB: "stream bend",
        STMC: "canalized stream",
        STMD: "distributary(-ies)",
        STMH: "headwaters",
        STMI: "intermittent stream",
        STMIX: "section of intermittent stream",
        STMM: "stream mouth(s)",
        STMQ: "abandoned watercourse",
        STMS: "streams",
        STMSB: "lost river",
        STMX: "section of stream",
        STRT: "strait",
        SWMP: "swamp",
        SYSI: "irrigation system",
        TNLC: "canal tunnel",
        WAD: "wadi",
        WADB: "wadi bend",
        WADJ: "wadi junction",
        WADM: "wadi mouth",
        WADS: "wadies",
        WADX: "section of wadi",
        WHRL: "whirlpool",
        WLL: "well",
        WLLQ: "abandoned well",
        WLLS: "wells",
        WTLD: "wetland",
        WTLDI: "intermittent wetland",
        WTRC: "watercourse",
        WTRH: "waterhole(s)",
        AGRC: "agricultural colony",
        AMUS: "amusement park",
        AREA: "area",
        BSND: "drainage basin",
        BSNP: "petroleum basin",
        BTL: "battlefield",
        CLG: "clearing",
        CMN: "common",
        CNS: "concession area",
        COLF: "coalfield",
        CONT: "continent",
        CST: "coast",
        CTRB: "business center",
        DEVH: "housing development",
        FLD: "field(s)",
        FLDI: "irrigated field(s)",
        GASF: "gasfield",
        GRAZ: "grazing area",
        GVL: "gravel area",
        INDS: "industrial area",
        LAND: "arctic land",
        LCTY: "locality",
        MILB: "military base",
        MNA: "mining area",
        MVA: "maneuver area",
        NVB: "naval base",
        OAS: "oasis(-es)",
        OILF: "oilfield",
        PEAT: "peat cutting area",
        PRK: "park",
        PRT: "port",
        QCKS: "quicksand",
        RES: "reserve",
        RESA: "agricultural reserve",
        RESF: "forest reserve",
        RESH: "hunting reserve",
        RESN: "nature reserve",
        RESP: "palm tree reserve",
        RESV: "reservation",
        RESW: "wildlife reserve",
        RGN: "region",
        RGNE: "economic region",
        RGNH: "historical region",
        RGNL: "lake region",
        RNGA: "artillery range",
        SALT: "salt area",
        SNOW: "snowfield",
        TRB: "tribal area",
        PPL: "populated place",
        PPLA: "seat of a first-order administrative division",
        PPLA2: "seat of a second-order administrative division",
        PPLA3: "seat of a third-order administrative division",
        PPLA4: "seat of a fourth-order administrative division",
        PPLC: "capital of a political entity",
        PPLCH: "historical capital of a political entity",
        PPLF: "farm village",
        PPLG: "seat of government of a political entity",
        PPLH: "historical populated place",
        PPLL: "populated locality",
        PPLQ: "abandoned populated place",
        PPLR: "religious populated place",
        PPLS: "populated places",
        PPLW: "destroyed populated place",
        PPLX: "section of populated place",
        STLMT: "israeli settlement",
        CSWY: "causeway",
        OILP: "oil pipeline",
        PRMN: "promenade",
        PTGE: "portage",
        RD: "road",
        RDA: "ancient road",
        RDB: "road bend",
        RDCUT: "road cut",
        RDJCT: "road junction",
        RJCT: "railroad junction",
        RR: "railroad",
        RRQ: "abandoned railroad",
        RTE: "caravan route",
        RYD: "railroad yard",
        ST: "street",
        STKR: "stock route",
        TNL: "tunnel",
        TNLN: "natural tunnel",
        TNLRD: "road tunnel",
        TNLRR: "railroad tunnel",
        TNLS: "tunnels",
        TRL: "trail",
        ADMF: "administrative facility",
        AGRF: "agricultural facility",
        AIRB: "airbase",
        AIRF: "airfield",
        AIRH: "heliport",
        AIRP: "airport",
        AIRQ: "abandoned airfield",
        AMTH: "amphitheater",
        ANS: "ancient site",
        AQC: "aquaculture facility",
        ARCH: "arch",
        ASTR: "astronomical station",
        ASYL: "asylum",
        ATHF: "athletic field",
        ATM: "automatic teller machine",
        BANK: "bank",
        BCN: "beacon",
        BDG: "bridge",
        BDGQ: "ruined bridge",
        BLDG: "building(s)",
        BLDO: "office building",
        BP: "boundary marker",
        BRKS: "barracks",
        BRKW: "breakwater",
        BSTN: "baling station",
        BTYD: "boatyard",
        BUR: "burial cave(s)",
        BUSTN: "bus station",
        BUSTP: "bus stop",
        CARN: "cairn",
        CAVE: "cave(s)",
        CH: "church",
        CMP: "camp(s)",
        CMPL: "logging camp",
        CMPLA: "labor camp",
        CMPMN: "mining camp",
        CMPO: "oil camp",
        CMPQ: "abandoned camp",
        CMPRF: "refugee camp",
        CMTY: "cemetery",
        COMC: "communication center",
        CRRL: "corral(s)",
        CSNO: "casino",
        CSTL: "castle",
        CSTM: "customs house",
        CTHSE: "courthouse",
        CTRA: "atomic center",
        CTRCM: "community center",
        CTRF: "facility center",
        CTRM: "medical center",
        CTRR: "religious center",
        CTRS: "space center",
        CVNT: "convent",
        DAM: "dam",
        DAMQ: "ruined dam",
        DAMSB: "sub-surface dam",
        DARY: "dairy",
        DCKD: "dry dock",
        DCKY: "dockyard",
        DIKE: "dike",
        DIP: "diplomatic facility",
        DPOF: "fuel depot",
        EST: "estate(s)",
        ESTO: "oil palm plantation",
        ESTR: "rubber plantation",
        ESTSG: "sugar plantation",
        ESTT: "tea plantation",
        ESTX: "section of estate",
        FCL: "facility",
        FNDY: "foundry",
        FRM: "farm",
        FRMQ: "abandoned farm",
        FRMS: "farms",
        FRMT: "farmstead",
        FT: "fort",
        FY: "ferry",
        GATE: "gate",
        GDN: "garden(s)",
        GHAT: "ghat",
        GHSE: "guest house",
        GOSP: "gas-oil separator plant",
        GOVL: "local government office",
        GRVE: "grave",
        HERM: "hermitage",
        HLT: "halting place",
        HMSD: "homestead",
        HSE: "house(s)",
        HSEC: "country house",
        HSP: "hospital",
        HSPC: "clinic",
        HSPD: "dispensary",
        HSPL: "leprosarium",
        HSTS: "historical site",
        HTL: "hotel",
        HUT: "hut",
        HUTS: "huts",
        INSM: "military installation",
        ITTR: "research institute",
        JTY: "jetty",
        LDNG: "landing",
        LEPC: "leper colony",
        LIBR: "library",
        LNDF: "landfill",
        LOCK: "lock(s)",
        LTHSE: "lighthouse",
        MALL: "mall",
        MAR: "marina",
        MFG: "factory",
        MFGB: "brewery",
        MFGC: "cannery",
        MFGCU: "copper works",
        MFGLM: "limekiln",
        MFGM: "munitions plant",
        MFGPH: "phosphate works",
        MFGQ: "abandoned factory",
        MFGSG: "sugar refinery",
        MKT: "market",
        ML: "mill(s)",
        MLM: "ore treatment plant",
        MLO: "olive oil mill",
        MLSG: "sugar mill",
        MLSGQ: "former sugar mill",
        MLSW: "sawmill",
        MLWND: "windmill",
        MLWTR: "water mill",
        MN: "mine(s)",
        MNAU: "gold mine(s)",
        MNC: "coal mine(s)",
        MNCR: "chrome mine(s)",
        MNCU: "copper mine(s)",
        MNFE: "iron mine(s)",
        MNMT: "monument",
        MNN: "salt mine(s)",
        MNQ: "abandoned mine",
        MNQR: "quarry(-ies)",
        MOLE: "mole",
        MSQE: "mosque",
        MSSN: "mission",
        MSSNQ: "abandoned mission",
        MSTY: "monastery",
        MTRO: "metro station",
        MUS: "museum",
        NOV: "novitiate",
        NSY: "nursery(-ies)",
        OBPT: "observation point",
        OBS: "observatory",
        OBSR: "radio observatory",
        OILJ: "oil pipeline junction",
        OILQ: "abandoned oil well",
        OILR: "oil refinery",
        OILT: "tank farm",
        OILW: "oil well",
        OPRA: "opera house",
        PAL: "palace",
        PGDA: "pagoda",
        PIER: "pier",
        PKLT: "parking lot",
        PMPO: "oil pumping station",
        PMPW: "water pumping station",
        PO: "post office",
        PP: "police post",
        PPQ: "abandoned police post",
        PRKGT: "park gate",
        PRKHQ: "park headquarters",
        PRN: "prison",
        PRNJ: "reformatory",
        PRNQ: "abandoned prison",
        PS: "power station",
        PSH: "hydroelectric power station",
        PSTB: "border post",
        PSTC: "customs post",
        PSTP: "patrol post",
        PYR: "pyramid",
        PYRS: "pyramids",
        QUAY: "quay",
        RDCR: "traffic circle",
        RECG: "golf course",
        RECR: "racetrack",
        REST: "restaurant",
        RET: "store",
        RHSE: "resthouse",
        RKRY: "rookery",
        RLG: "religious site",
        RLGR: "retreat",
        RNCH: "ranch(es)",
        RSD: "railroad siding",
        RSGNL: "railroad signal",
        RSRT: "resort",
        RSTN: "railroad station",
        RSTNQ: "abandoned railroad station",
        RSTP: "railroad stop",
        RSTPQ: "abandoned railroad stop",
        RUIN: "ruin(s)",
        SCH: "school",
        SCHA: "agricultural school",
        SCHC: "college",
        SCHL: "language school",
        SCHM: "military school",
        SCHN: "maritime school",
        SCHT: "technical school",
        SECP: "State Exam Prep Centre",
        SHPF: "sheepfold",
        SHRN: "shrine",
        SHSE: "storehouse",
        SLCE: "sluice",
        SNTR: "sanatorium",
        SPA: "spa",
        SPLY: "spillway",
        SQR: "square",
        STBL: "stable",
        STDM: "stadium",
        STNB: "scientific research base",
        STNC: "coast guard station",
        STNE: "experiment station",
        STNF: "forest station",
        STNI: "inspection station",
        STNM: "meteorological station",
        STNR: "radio station",
        STNS: "satellite station",
        STNW: "whaling station",
        STPS: "steps",
        SWT: "sewage treatment plant",
        THTR: "theater",
        TMB: "tomb(s)",
        TMPL: "temple(s)",
        TNKD: "cattle dipping tank",
        TOWR: "tower",
        TRANT: "transit terminal",
        TRIG: "triangulation station",
        TRMO: "oil pipeline terminal",
        TWO: "temp work office",
        UNIP: "university prep school",
        UNIV: "university",
        USGE: "united states government establishment",
        VETF: "veterinary facility",
        WALL: "wall",
        WALLA: "ancient wall",
        WEIR: "weir(s)",
        WHRF: "wharf(-ves)",
        WRCK: "wreck",
        WTRW: "waterworks",
        ZNF: "free trade zone",
        ZOO: "zoo",
        ASPH: "asphalt lake",
        ATOL: "atoll(s)",
        BAR: "bar",
        BCH: "beach",
        BCHS: "beaches",
        BDLD: "badlands",
        BLDR: "boulder field",
        BLHL: "blowhole(s)",
        BLOW: "blowout(s)",
        BNCH: "bench",
        BUTE: "butte(s)",
        CAPE: "cape",
        CFT: "cleft(s)",
        CLDA: "caldera",
        CLF: "cliff(s)",
        CNYN: "canyon",
        CONE: "cone(s)",
        CRDR: "corridor",
        CRQ: "cirque",
        CRQS: "cirques",
        CRTR: "crater(s)",
        CUET: "cuesta(s)",
        DLTA: "delta",
        DPR: "depression(s)",
        DSRT: "desert",
        DUNE: "dune(s)",
        DVD: "divide",
        ERG: "sandy desert",
        FAN: "fan(s)",
        FORD: "ford",
        FSR: "fissure",
        GAP: "gap",
        GRGE: "gorge(s)",
        HDLD: "headland",
        HLL: "hill",
        HLLS: "hills",
        HMCK: "hammock(s)",
        HMDA: "rock desert",
        INTF: "interfluve",
        ISL: "island",
        ISLET: "islet",
        ISLF: "artificial island",
        ISLM: "mangrove island",
        ISLS: "islands",
        ISLT: "land-tied island",
        ISLX: "section of island",
        ISTH: "isthmus",
        KRST: "karst area",
        LAVA: "lava area",
        LEV: "levee",
        MESA: "mesa(s)",
        MND: "mound(s)",
        MRN: "moraine",
        MT: "mountain",
        MTS: "mountains",
        NKM: "meander neck",
        NTK: "nunatak",
        NTKS: "nunataks",
        PAN: "pan",
        PANS: "pans",
        PASS: "pass",
        PEN: "peninsula",
        PENX: "section of peninsula",
        PK: "peak",
        PKS: "peaks",
        PLAT: "plateau",
        PLATX: "section of plateau",
        PLDR: "polder",
        PLN: "plain(s)",
        PLNX: "section of plain",
        PROM: "promontory(-ies)",
        PT: "point",
        PTS: "points",
        RDGB: "beach ridge",
        RDGE: "ridge(s)",
        REG: "stony desert",
        RK: "rock",
        RKFL: "rockfall",
        RKS: "rocks",
        SAND: "sand area",
        SBED: "dry stream bed",
        SCRP: "escarpment",
        SDL: "saddle",
        SHOR: "shore",
        SINK: "sinkhole",
        SLID: "slide",
        SLP: "slope(s)",
        SPIT: "spit",
        SPUR: "spur(s)",
        TAL: "talus slope",
        TRGD: "interdune trough(s)",
        TRR: "terrace",
        UPLD: "upland",
        VAL: "valley",
        VALG: "hanging valley",
        VALS: "valleys",
        VALX: "section of valley",
        VLC: "volcano",
        APNU: "apron",
        ARCU: "arch",
        ARRU: "arrugado",
        BDLU: "borderland",
        BKSU: "banks",
        BNKU: "bank",
        BSNU: "basin",
        CDAU: "cordillera",
        CNSU: "canyons",
        CNYU: "canyon",
        CRSU: "continental rise",
        DEPU: "deep",
        EDGU: "shelf edge",
        ESCU: "escarpment (or scarp)",
        FANU: "fan",
        FLTU: "flat",
        FRZU: "fracture zone",
        FURU: "furrow",
        GAPU: "gap",
        GLYU: "gully",
        HLLU: "hill",
        HLSU: "hills",
        HOLU: "hole",
        KNLU: "knoll",
        KNSU: "knolls",
        LDGU: "ledge",
        LEVU: "levee",
        MESU: "mesa",
        MNDU: "mound",
        MOTU: "moat",
        MTU: "mountain",
        PKSU: "peaks",
        PKU: "peak",
        PLNU: "plain",
        PLTU: "plateau",
        PNLU: "pinnacle",
        PRVU: "province",
        RDGU: "ridge",
        RDSU: "ridges",
        RFSU: "reefs",
        RFU: "reef",
        RISU: "rise",
        SCNU: "seachannel",
        SCSU: "seachannels",
        SDLU: "saddle",
        SHFU: "shelf",
        SHLU: "shoal",
        SHSU: "shoals",
        SHVU: "shelf valley",
        SILU: "sill",
        SLPU: "slope",
        SMSU: "seamounts",
        SMU: "seamount",
        SPRU: "spur",
        TERU: "terrace",
        TMSU: "tablemounts (or guyots)",
        TMTU: "tablemount (or guyot)",
        TNGU: "tongue",
        TRGU: "trough",
        TRNU: "trench",
        VALU: "valley",
        VLSU: "valleys",
        BUSH: "bush(es)",
        CULT: "cultivated area",
        FRST: "forest(s)",
        FRSTF: "fossilized forest",
        GRSLD: "grassland",
        GRVC: "coconut grove",
        GRVO: "olive grove",
        GRVP: "palm grove",
        GRVPN: "pine grove",
        HTH: "heath",
        MDW: "meadow",
        OCH: "orchard(s)",
        SCRB: "scrubland",
        TREE: "tree(s)",
        TUND: "tundra",
        VIN: "vineyard",
        VINS: "vineyards",
        ll: "not available"
    };

    return featureCodeLookUp[featureCode];

};

//not used. For advanced search. Div exists in the main window. commented out.
utils.populateCountryDropdown = function () {

    var countries =
        ["Andorra",
            "United Arab Emirates",
            "Afghanistan",
            "Antigua and Barbuda",
            "Anguilla",
            "Albania",
            "Armenia",
            "Netherlands Antilles",
            "Angola",
            "Antarctica",
            "Argentina",
            "American Samoa",
            "Austria",
            "Australia",
            "Aruba",
            "Åland",
            "Azerbaijan",
            "Bosnia and Herzegovina",
            "Barbados",
            "Bangladesh",
            "Belgium",
            "Burkina Faso",
            "Bulgaria",
            "Bahrain",
            "Burundi",
            "Benin",
            "Saint Barthélemy",
            "Bermuda",
            "Brunei",
            "Bolivia",
            "Bonaire",
            "Brazil",
            "Bahamas",
            "Bhutan",
            "Bouvet Island",
            "Botswana",
            "Belarus",
            "Belize",
            "Canada",
            "Cocos [Keeling] Islands",
            "Democratic Republic of the Congo",
            "Central African Republic",
            "Republic of the Congo",
            "Switzerland",
            "Ivory Coast",
            "Cook Islands",
            "Chile",
            "Cameroon",
            "China",
            "Colombia",
            "Costa Rica",
            "Serbia and Montenegro",
            "Cuba",
            "Cape Verde",
            "Curacao",
            "Christmas Island",
            "Cyprus",
            "Czechia",
            "Germany",
            "Djibouti",
            "Denmark",
            "Dominica",
            "Dominican Republic",
            "Algeria",
            "Ecuador",
            "Estonia",
            "Egypt",
            "Western Sahara",
            "Eritrea",
            "Spain",
            "Ethiopia",
            "Finland",
            "Fiji",
            "Falkland Islands",
            "Micronesia",
            "Faroe Islands",
            "France",
            "Gabon",
            "United Kingdom",
            "Grenada",
            "Georgia",
            "French Guiana",
            "Guernsey",
            "Ghana",
            "Gibraltar",
            "Greenland",
            "Gambia",
            "Guinea",
            "Guadeloupe",
            "Equatorial Guinea",
            "Greece",
            "South Georgia and the South Sandwich Islands",
            "Guatemala",
            "Guam",
            "Guinea-Bissau",
            "Guyana",
            "Hong Kong",
            "Heard Island and McDonald Islands",
            "Honduras",
            "Croatia",
            "Haiti",
            "Hungary",
            "Indonesia",
            "Ireland",
            "Israel",
            "Isle of Man",
            "India",
            "British Indian Ocean Territory",
            "Iraq",
            "Iran",
            "Iceland",
            "Italy",
            "Jersey",
            "Jamaica",
            "Jordan",
            "Japan",
            "Kenya",
            "Kyrgyzstan",
            "Cambodia",
            "Kiribati",
            "Comoros",
            "Saint Kitts and Nevis",
            "North Korea",
            "South Korea",
            "Kuwait",
            "Cayman Islands",
            "Kazakhstan",
            "Laos",
            "Lebanon",
            "Saint Lucia",
            "Liechtenstein",
            "Sri Lanka",
            "Liberia",
            "Lesotho",
            "Lithuania",
            "Luxembourg",
            "Latvia",
            "Libya",
            "Morocco",
            "Monaco",
            "Moldova",
            "Montenegro",
            "Saint Martin",
            "Madagascar",
            "Marshall Islands",
            "Macedonia",
            "Mali",
            "Myanmar [Burma]",
            "Mongolia",
            "Macao",
            "Northern Mariana Islands",
            "Martinique",
            "Mauritania",
            "Montserrat",
            "Malta",
            "Mauritius",
            "Maldives",
            "Malawi",
            "Mexico",
            "Malaysia",
            "Mozambique",
            "Namibia",
            "New Caledonia",
            "Niger",
            "Norfolk Island",
            "Nigeria",
            "Nicaragua",
            "Netherlands",
            "Norway",
            "Nepal",
            "Nauru",
            "Niue",
            "New Zealand",
            "Oman",
            "Panama",
            "Peru",
            "French Polynesia",
            "Papua New Guinea",
            "Philippines",
            "Pakistan",
            "Poland",
            "Saint Pierre and Miquelon",
            "Pitcairn Islands",
            "Puerto Rico",
            "Palestine",
            "Portugal",
            "Palau",
            "Paraguay",
            "Qatar",
            "Réunion",
            "Romania",
            "Serbia",
            "Russia",
            "Rwanda",
            "Saudi Arabia",
            "Solomon Islands",
            "Seychelles",
            "Sudan",
            "Sweden",
            "Singapore",
            "Saint Helena",
            "Slovenia",
            "Svalbard and Jan Mayen",
            "Slovakia",
            "Sierra Leone",
            "San Marino",
            "Senegal",
            "Somalia",
            "Suriname",
            "South Sudan",
            "São Tomé and Príncipe",
            "El Salvador",
            "Sint Maarten",
            "Syria",
            "Swaziland",
            "Turks and Caicos Islands",
            "Chad",
            "French Southern Territories",
            "Togo",
            "Thailand",
            "Tajikistan",
            "Tokelau",
            "East Timor",
            "Turkmenistan",
            "Tunisia",
            "Tonga",
            "Turkey",
            "Trinidad and Tobago",
            "Tuvalu",
            "Taiwan",
            "Tanzania",
            "Ukraine",
            "Uganda",
            "U.S. Minor Outlying Islands",
            "United States",
            "Uruguay",
            "Uzbekistan",
            "Vatican City",
            "Saint Vincent and the Grenadines",
            "Venezuela",
            "British Virgin Islands",
            "U.S. Virgin Islands",
            "Vietnam",
            "Vanuatu",
            "Wallis and Futuna",
            "Samoa",
            "Kosovo",
            "Yemen",
            "Mayotte",
            "South Africa",
            "Zambia",
            "Zimbabwe"
        ];



    $.each(countries, function (i, v) {
        var option = $("<option>", { value: v });
        option.append(document.createTextNode(v));
        $("#countryDropId").append(option);
    });

};

