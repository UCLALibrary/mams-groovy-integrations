import com.tedial.bpmengine.connectors.utils.AddonContext
import com.tedial.pam.commons.springboot.ApplicationContextAccessor
import com.tedial.bpmengine.controllers.wip.WIPController
import com.tedial.pam.bpmengineclients.ws.entity.RSFile

/*
 * =============================================================================
 * UCLA BATON QC PROFILE LOOKUP KEY
 * =============================================================================
 * Version: v2.4
 * Date: 2026-09-09
 *
 * Purpose
 * -------
 * Read the analysed technical metadata from the WIP, normalise the values used
 * by UCLA's Baton profile naming convention, and create the exact lookup key
 * required to select the correct Baton QC profile.
 *
 * The generated key is saved to:
 *
 * BPM:QC:LOOKUP_KEY
 *
 * The match status is saved to:
 *
 * BPM:QC:MATCH_RESULT
 *
 * The Groovy result returned to the Profile outputs is:
 *
 * MATCH
 *
 * or:
 *
 * NO_MATCH
 *
 * Expected key format
 * -------------------
 * MAMs <container>_<resolution>_<video codec>_<fps>_<aspect ratio>_
 *      <video bit depth>_<audio format>_<sample rate.audio bit depth>_
 *      <audio channels>
 *
 * Example
 * -------
 * MAMs QT_720x486_V210_2997fps_4.3_10bit_PCM_48.24_2.0
 *
 * Maintenance note
 * ----------------
 * Analysis tools may return previously unseen values for new formats. When
 * that happens, UCLA must add the appropriate rule to the normalisation maps
 * or helper functions below.
 */


// =============================================================================
// 1. KNOWN BATON PROFILE NAMES
// =============================================================================

/*
 * The generated lookup key must exactly match one of the Baton profile names
 * listed below.
 *
 * When UCLA adds or renames a Baton profile, the corresponding exact profile
 * name must also be added or updated in this list.
 */
def knownBatonProfiles = [
    "MAMs_QT_7680x4320_4444_24fps_16.9_12bit_PCM_96.16_2.0",
    "MAMs_QT_5120x3840_422HQ_24fps_4.3_10bit_PCM_96.24_2.0",
    "MAMs_QT_5120x3840_422HQ_24fps_4.3_10bit_PCM_48.24_2.0",
    "MAMs_QT_5120x3840_422HQ_24fps_4.3_10bit_MOS",
    "MAMs_QT_5120x3840_422HQ_20fps_4.3_10bit_MOS",
    "MAMs_QT_4096x3112_422HQ_24fps_4.3_10bit_PCM_48.24_2.0",
    "MAMs_QT_4096x3112_422HQ_24fps_4.3_10bit_PCM_48.16_2.0",
    "MAMs_QT_4096x3112_422HQ_24fps_4.3_10bit_PCM_48.16_1.0",
    "MAMs_QT_4096x3112_422HQ_2398fps_4.3_10bit_PCM_48.24_2.0",
    "MAMs_QT_4096x2160_422HQ_24fps_1.89_10bit_PCM_48.24_2.0",
    "MAMs_QT_2560x1920_422HQ_24fps_4.3_10bit_PCM_96.24_2.0",
    "MAMs_QT_2560x1920_422HQ_24fps_4.3_10bit_PCM_48.24_2.0",
    "MAMs_QT_2560x1920_422HQ_24fps_4.3_10bit_MOS",
    "MAMs_QT_2048x1556_4444_18fps_4.3_12bit_MOS",
    "MAMs_QT_2048x1556_422HQ_24fps_4.3_10bit_PCM_48.24_2.0",
    "MAMs_QT_2048x1556_422HQ_24fps_4.3_10bit_PCM_48.16_2.0",
    "MAMs_QT_2048x1556_422HQ_24fps_4.3_10bit_PCM_48.24_1.0",
    "MAMs_QT_2048x1556_422HQ_18fps_4.3_10bit_PCM_48.24_2.0",
    "MAMs_QT_2048x1485_422HQ_24fps_1.379_10bit_PCM_48.24_2.0",
    "MAMs_QT_2048x1485_422HQ_24fps_1.379_10bit_PCM_48.16_2.0",
    "MAMs_QT_2048x1485_422HQ_2398fps_1.379_10bit_PCM_48.16_2.0",
    "MAMs_QT_2048x1080_422HQ_30fps_1.89_10bit_PCM_48.16_2.0",
    "MAMs_AVI_1440x1080_RGB_25fps_4.3_8bit_MOS",
    "MAMs_AVI_1440x1080_RGB_25fps_4.3_24bit_MOS",
    "MAMs_QT_1920x1080_4444_24fps_16.9_12bit_PCM_48.24_2.0",
    "MAMs_QT_1920x1080_422HQ_5994fps_16.9_10bit_PCM_48.16_2.0",
    "MAMs_QT_1920x1080_422HQ_2997fps_16.9_10bit_PCM_48.24_1.0",
    "MAMs_QT_1920x1080_422HQ_24fps_16.9_10bit_PCM_48.24_2.0",
    "MAMs_QT_1920x1080_422HQ_2398fps_16.9_10bit_PCM_48.16_2.0",
    "MAMs_QT_1920x1080_422HQ_2398fps_16.9_10bit_PCM_48.16_1.0",
    "MAMs_QT_720x486_422HQ_2398fps_4.3_10bit_PCM_48.24_2.0",
    "MAMs_QT_720x486_V210_2997fps_4.3_10bit_PCM_48.24_2.0",
    "MAMs_QT_720x486_2VUY_2997fps_4.3_8bit_PCM_48.24_2.0",
    "MAMs_QT_720x486_2VUY_2997fps_3.2_8bit_PCM_48.24_2.0",
    "MAMs_QT_720x486_2VUY_2997fps_3.2_8bit_PCM_48.24_1.0",
    "MAMs_QT_720x480_422HQ_2997fps_4.3_10bit_PCM_48.16_2.0",
    "MAMs_AVI_720x480_DV_2997fps_4.3_8bit_PCM_48.16_2.0",
    "MAMs_QT_720x480_DV_2997fps_4.3_8bit_PCM_48.16_1.0",
    "MAMs_WAV_PCM_96.32_2.0",
    "MAMs_WAV_PCM_96.24_1.0",
    "MAMs_WAV_PCM_48.24_1.0",
    "MAMs_MP4_1920x1080_AVC_2398fps_16.9_8bit_AACLC_48.16_2.0",
    "MAMs_MXF_4096x2160_JPEG2000_24fps_1.9_12bit_PCM_48.24_6.0",
    "MAMs_MXF_2048x1080_JPEG2000_24fps_1.9_12bit_PCM_48.24_6.0"
] as Set


// =============================================================================
// 2. SAVE THE LOOKUP RESULT FOR LATER WORKFLOW STEPS
// =============================================================================

/*
 * Save both values as BPM metadata and return the simple result required by
 * the two Profile output conditions.
 *
 * Returned values:
 *
 * MATCH
 * NO_MATCH
 */
def saveLookupResult = { lookupKey, matchResult ->

    def safeLookupKey = lookupKey != null ? lookupKey.toString() : ""
    def safeMatchResult = matchResult == "Match" ? "Match" : "No Match"

    def metadataMap = [:]
    metadataMap["BPM:QC:LOOKUP_KEY"] = safeLookupKey
    metadataMap["BPM:QC:MATCH_RESULT"] = safeMatchResult

    BPM.saveMetadata(metadataMap)

    return safeMatchResult == "Match" ? "MATCH" : "NO_MATCH"
}


// =============================================================================
// 3. GET THE CURRENT WIP CONTEXT
// =============================================================================

String ai = WIPUtils.activity.id

AddonContext addonContext = AddonContext.getContextByActInst(ai)
WIPController wipController =
    ApplicationContextAccessor.getBean(WIPController.class)


// =============================================================================
// 4. FIND AND READ THE TECHNICAL XML
// =============================================================================

// Different workflows or product versions may use different technical XML
// filenames, so check the known alternatives in order.
def possibleTechnicalFiles = [
    "technical.xml",
    "WIP_TECHNICAL.xml",
    "wip_technical.xml"
]

RSFile technicalFile = null

possibleTechnicalFiles.each { fileName ->

    if (technicalFile == null) {
        try {
            technicalFile = wipController.findFileByName(ai, fileName)
        } catch (Exception ignored) {
            // File not found under this name. Continue with the next one.
        }
    }
}

if (technicalFile == null) {
    logger.info(
        "QC lookup key could not be built. No technical XML file found."
    )

    return saveLookupResult("", "No Match")
}

String technicalXmlString = wipController.getFileContentAsString(
    ai,
    technicalFile.getFileId()
)

def technicalXml =
    new XmlSlurper(false, false).parseText(technicalXmlString)


// =============================================================================
// 5. SELECT THE FILE, VIDEO TRACK AND AUDIO TRACK USED FOR THE KEY
// =============================================================================

// UCLA's current profile convention is based on the first analysed FILE,
// first VIDEO_TRACK and first AUDIO_TRACK.
def fileNode = technicalXml.ASSET.TECHNICAL.FILE[0]

if (fileNode == null || fileNode.size() == 0) {
    logger.info(
        "QC lookup key could not be built. No FILE node found in technical XML."
    )

    return saveLookupResult("", "No Match")
}

def videoTrack = fileNode.TRACKS.VIDEO_TRACK[0]
def audioTrack = fileNode.TRACKS.AUDIO_TRACK[0]

if (videoTrack == null || videoTrack.size() == 0) {
    logger.info(
        "QC lookup key could not be built. No VIDEO_TRACK found."
    )

    return saveLookupResult("", "No Match")
}

/*
 * The audio channel value in the profile name represents the first audio
 * track, not the total number of channels across every audio track.
 *
 * For example, an asset containing several mono AUDIO_TRACK elements still
 * generates 1.0 from the first AUDIO_TRACK.
 */


// =============================================================================
// 6. GENERAL XML VALUE HELPER
// =============================================================================

def textValue = { node ->

    return node != null && node.size() > 0
        ? node.text().trim()
        : ""
}


// =============================================================================
// 6A. CONTAINER WRAPPER NORMALISATION
// =============================================================================

/*
 * Technical analysis may describe a QuickTime MOV container using different
 * wrapper values.
 *
 * UCLA's Baton profile naming convention uses QT.
 *
 * QUICKTIME and QT are therefore normalised to QT.
 *
 * Some MOV files are analysed with WRAPPER=MPEG-4. In that case, the FILE
 * NAME attribute is checked. If the analysed source filename ends in .mov,
 * MPEG-4 is normalised to QT.
 *
 * Genuine MPEG-4 files, such as .mp4 files, are left as MPEG-4.
 *
 * Other wrapper values are returned unchanged so unsupported or new values
 * remain visible in the generated No Match lookup key.
 */
def normaliseWrapper = { wrapper, fileName ->

    def cleanWrapper =
        wrapper != null ? wrapper.trim().toUpperCase() : ""

    def cleanFileName =
        fileName != null ? fileName.trim().toLowerCase() : ""

    if (cleanWrapper == "QT" || cleanWrapper == "QUICKTIME") {
        return "QT"
    }

    if (cleanWrapper == "MPEG-4" && cleanFileName.endsWith(".mov")) {
        return "QT"
    }

    if (cleanWrapper == "MXF-ATOM" && cleanFileName.endsWith(".mxf")) {
        return "MXF"
    }

    return cleanWrapper
}

// =============================================================================
// 7. VIDEO CODEC NORMALISATION
// =============================================================================

// Keys are values returned by technical analysis.
// Values are the exact labels used in UCLA's Baton profile names.
def videoCodecMappings = [
    "PRORES_422_HQ": "422HQ",
    "V210"         : "V210",
    "2VUY"         : "2VUY",
    "PRORES_4444"  : "4444",
    "BGR24"        : "RGB",
    "DVCPRO"       : "DV"
]

def normaliseVideoCodec = { codec ->

    def cleanCodec =
        codec != null ? codec.trim().toUpperCase() : ""

    if (cleanCodec == "") {
        return ""
    }

    /*
     * An unmapped codec is returned unchanged. The resulting No Match key
     * will expose the new value so UCLA can add the required mapping.
     */
    return videoCodecMappings.containsKey(cleanCodec)
        ? videoCodecMappings[cleanCodec]
        : cleanCodec
}


// =============================================================================
// 8. FRAME-RATE NORMALISATION
// =============================================================================

def normaliseEditRate = { editRate ->

    if (editRate == null || editRate.trim() == "") {
        return ""
    }

    def cleanRate = editRate.trim().replaceAll("\\s+", " ")
    def parts = cleanRate.split(" ")

    if (parts.size() == 2) {

        def numerator = parts[0]
        def denominator = parts[1]

        // Exact values used by UCLA's Baton profile naming convention.
        if (
            (numerator == "24000" && denominator == "1001") ||
            (numerator == "23976" && denominator == "1000")
        ) {
            return "2398fps"
        }

        if (
            (numerator == "30000" && denominator == "1001") ||
            (numerator == "29970" && denominator == "1000")
        ) {
            return "2997fps"
        }

        if (
            (numerator == "60000" && denominator == "1001") ||
            (numerator == "59940" && denominator == "1000")
        ) {
            return "5994fps"
        }

        if (denominator == "1") {
            return numerator + "fps"
        }

        BigDecimal n = new BigDecimal(numerator)
        BigDecimal d = new BigDecimal(denominator)

        if (d.compareTo(BigDecimal.ZERO) != 0) {

            BigDecimal calculatedFps =
                n.divide(d, 6, BigDecimal.ROUND_HALF_UP)

            def fpsText =
                calculatedFps.stripTrailingZeros().toPlainString()

            if (fpsText == "23.976" || fpsText == "23.98") {
                return "2398fps"
            }

            if (fpsText == "29.97") {
                return "2997fps"
            }

            if (fpsText == "59.94") {
                return "5994fps"
            }

            return fpsText.replace(".", "") + "fps"
        }
    }

    if (cleanRate == "29.97") {
        return "2997fps"
    }

    if (cleanRate == "23.98" || cleanRate == "23.976") {
        return "2398fps"
    }

    if (cleanRate == "59.94") {
        return "5994fps"
    }

    return cleanRate
        .replace(".", "")
        .replaceAll("\\s+", "_") + "fps"
}


// =============================================================================
// 9. ASPECT-RATIO NORMALISATION
// =============================================================================

/*
 * Technical analysis may return aspect ratios in colon notation, while
 * UCLA's Baton profile naming convention uses full stops.
 *
 * Examples:
 *
 * 4:3   -> 4.3
 * 16:9  -> 16.9
 *
 * Some 720x486 2VUY media is analysed as 40:27. UCLA's Baton profile naming
 * convention represents this specific format as 3.2, so it requires an
 * explicit mapping.
 *
 * Any aspect ratio not listed in the mapping is returned with the colon
 * replaced by a full stop.
 */
def aspectRatioMappings = [
    "40:27": "3.2",
    "2048:1485": "1.379"
]

def normaliseAspectRatio = { aspectRatio ->

    def cleanAspectRatio =
        aspectRatio != null ? aspectRatio.trim() : ""

    if (cleanAspectRatio == "") {
        return ""
    }

    if (aspectRatioMappings.containsKey(cleanAspectRatio)) {
        return aspectRatioMappings[cleanAspectRatio]
    }

    return cleanAspectRatio.replace(":", ".")
}


// =============================================================================
// 10. VIDEO BIT-DEPTH NORMALISATION
// =============================================================================

/*
 * BITS_PER_PIXEL cannot always be used directly as video bit depth.
 *
 * In the supplied 2VUY files, analysis reports BITS_PER_PIXEL=16 because the
 * value describes packed pixel storage. UCLA's Baton profile expects 8bit.
 *
 * Codec-specific rules take priority. Other codecs fall back to the analysed
 * BITS_PER_PIXEL value.
 */
def videoBitDepthMappings = [
    "2VUY": "8bit",
    "DVCPRO": "8bit",
    "DV": "8bit"
]

def normaliseVideoBitDepth = { sourceCodec, bitsPerPixel ->

    def cleanCodec =
        sourceCodec != null ? sourceCodec.trim().toUpperCase() : ""

    if (videoBitDepthMappings.containsKey(cleanCodec)) {
        return videoBitDepthMappings[cleanCodec]
    }

    def cleanBits =
        bitsPerPixel != null ? bitsPerPixel.trim() : ""

    return cleanBits != ""
        ? cleanBits + "bit"
        : ""
}


// =============================================================================
// 11. AUDIO SAMPLE-RATE NORMALISATION
// =============================================================================

def normaliseSampleRate = { audioEditRate ->

    if (audioEditRate == null || audioEditRate.trim() == "") {
        return ""
    }

    def parts = audioEditRate.trim().split("\\s+")
    BigDecimal hz = new BigDecimal(parts[0])

    // Baton profile names express 48000 Hz as 48.
    return hz
        .divide(
            new BigDecimal("1000"),
            0,
            BigDecimal.ROUND_HALF_UP
        )
        .toPlainString()
}


// =============================================================================
// 12. AUDIO CHANNEL NORMALISATION
// =============================================================================

def normaliseChannels = { channels ->

    if (channels == null || channels.trim() == "") {
        return ""
    }

    return channels.trim() + ".0"
}


// =============================================================================
// 13. READ AND NORMALISE THE VIDEO VALUES
// =============================================================================

def wrapper = normaliseWrapper(
    textValue(fileNode.WRAPPER),
    fileNode.@NAME.text()
)

def width =
    textValue(videoTrack.VIDEO_SIZE_WIDTH)

def height =
    textValue(videoTrack.VIDEO_SIZE_HEIGHT)

def sourceVideoCodec =
    textValue(videoTrack.VIDEO_CODEC)

def videoCodec =
    normaliseVideoCodec(sourceVideoCodec)

def fps =
    normaliseEditRate(textValue(videoTrack.EDIT_RATE))

def aspectRatio =
    normaliseAspectRatio(textValue(videoTrack.ASPECT_RATIO))

def videoBitDepth = normaliseVideoBitDepth(
    sourceVideoCodec,
    textValue(videoTrack.BITS_PER_PIXEL)
)


// =============================================================================
// 14. READ AND NORMALISE THE AUDIO VALUES
// =============================================================================

def audioCodec
def sampleRate
def audioBitDepth
def channels

def hasAudio =
    audioTrack != null && audioTrack.size() > 0

if (hasAudio) {

    audioCodec =
        textValue(audioTrack.AUDIO_CODEC).toUpperCase()

    sampleRate =
        normaliseSampleRate(textValue(audioTrack.EDIT_RATE))

    audioBitDepth =
        textValue(audioTrack.BITS_PER_AUDIO_SAMPLE)

    channels =
        normaliseChannels(
            textValue(audioTrack.AUDIO_CHANNELS_PER_TRACK)
        )

} else {

    /*
     * UCLA Baton profiles use MOS as the final component when the
     * source media contains no audio.
     *
     * No sample rate, audio bit depth or channel values are appended
     * after MOS.
     *
     * Example:
     * MAMs QT_2048x1556_4444_18fps_4.3_12bit_MOS
     */
    audioCodec = "MOS"
    sampleRate = ""
    audioBitDepth = ""
    channels = ""
}


// =============================================================================
// 15. VALIDATE REQUIRED COMPONENTS BEFORE BUILDING THE KEY
// =============================================================================

def keyComponents = [
    "container"         : wrapper,
    "resolution width"  : width,
    "resolution height" : height,
    "video codec"       : videoCodec,
    "frame rate"        : fps,
    "aspect ratio"      : aspectRatio,
    "video bit depth"   : videoBitDepth,
    "audio format"      : audioCodec
]

/*
 * Audio-specific technical values are only required when the source
 * actually contains an audio track.
 *
 * For media without audio, MOS is sufficient and becomes the final
 * component of the Baton profile name.
 */
if (hasAudio) {
    keyComponents["audio sample rate"] = sampleRate
    keyComponents["audio bit depth"] = audioBitDepth
    keyComponents["audio channels"] = channels
}

def missingComponents = keyComponents.findAll { name, value ->

    value == null || value.toString().trim() == ""

}.keySet()

if (!missingComponents.isEmpty()) {

    logger.info(
        "QC lookup key could not be built. Missing technical values: " +
        missingComponents.join(", ")
    )

    return saveLookupResult("", "No Match")
}


// =============================================================================
// 16. BUILD THE EXACT BATON PROFILE LOOKUP KEY
// =============================================================================

def lookupKey

if (hasAudio) {

    lookupKey = "MAMs_" + [
        wrapper,
        width + "x" + height,
        videoCodec,
        fps,
        aspectRatio,
        videoBitDepth,
        audioCodec,
        sampleRate + "." + audioBitDepth,
        channels
    ].join("_")

} else {

    lookupKey = "MAMs_" + [
        wrapper,
        width + "x" + height,
        videoCodec,
        fps,
        aspectRatio,
        videoBitDepth,
        "MOS"
    ].join("_")
}



// =============================================================================
// 17. MATCH THE GENERATED KEY AGAINST THE KNOWN BATON PROFILES
// =============================================================================

/*
 * The generated lookup key must match a configured profile name exactly.
 */
def matchResult = knownBatonProfiles.contains(lookupKey) ? "Match" : "No Match"

logger.info("QC Lookup Key : " + lookupKey)
logger.info("QC Match Result : " + matchResult)

if (matchResult == "No Match") {
    logger.info(
        "Known Baton profiles: " +
        knownBatonProfiles.join(" | ")
    )
}


// =============================================================================
// 18. SAVE BPM METADATA AND RETURN THE PROFILE OUTPUT RESULT
// =============================================================================

/*
 * This saves:
 *
 * BPM:QC:LOOKUP_KEY
 * BPM:QC:MATCH_RESULT
 *
 * It then returns either:
 *
 * MATCH
 * NO_MATCH
 *
 * The returned value controls the two outputs configured on this Profile.
 */
return saveLookupResult(lookupKey, matchResult)

