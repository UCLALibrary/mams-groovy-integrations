/*
 * =============================================================================
 * UCLA BATON QC PROFILE LOOKUP KEY
 * =============================================================================
 * Version: v2.7
 * Date: 2026-09-28
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
 * MAMs_<container>_<resolution>_<video codec>_<fps>_<aspect ratio>_
 *      <video bit depth>_<audio format>_<sample rate.audio bit depth>_
 *      <audio channels>
 *
 * Example
 * -------
 * MAMs_QT_720x486_V210_2997fps_4.3_10bit_PCM_48.24_2.0
 *
 * Maintenance note
 * ----------------
 * Analysis tools may return previously unseen values for new formats. When
 * that happens, UCLA must add the appropriate rule to the normalisation maps
 * or helper functions below.
 */

// =============================================================================
// KNOWN BATON PROFILE NAMES
// =============================================================================

/*
 * The generated lookup key must exactly match one of the Baton profile names
 * listed below.
 *
 * When UCLA adds or renames a Baton profile, the corresponding exact profile
 * name must also be added or updated in this list.
 */
def knownBatonProfiles = [
    'MAMs_QT_7680x4320_4444_24fps_16.9_12bit_PCM_96.16_2.0',
    'MAMs_QT_5120x3840_422HQ_24fps_4.3_10bit_PCM_96.24_2.0',
    'MAMs_QT_5120x3840_422HQ_24fps_4.3_10bit_PCM_48.24_2.0',
    'MAMs_QT_5120x3840_422HQ_24fps_4.3_10bit_MOS',
    'MAMs_QT_5120x3840_422HQ_20fps_4.3_10bit_MOS',
    'MAMs_QT_4096x3112_422HQ_24fps_4.3_10bit_PCM_48.24_2.0',
    'MAMs_QT_4096x3112_422HQ_24fps_4.3_10bit_PCM_48.16_2.0',
    'MAMs_QT_4096x3112_422HQ_24fps_4.3_10bit_PCM_48.16_1.0',
    'MAMs_QT_4096x3112_422HQ_2398fps_4.3_10bit_PCM_48.24_2.0',
    'MAMs_QT_4096x2160_422HQ_24fps_1.89_10bit_PCM_48.24_2.0',
    'MAMs_QT_2560x1920_422HQ_24fps_4.3_10bit_PCM_96.24_2.0',
    'MAMs_QT_2560x1920_422HQ_24fps_4.3_10bit_PCM_48.24_2.0',
    'MAMs_QT_2560x1920_422HQ_24fps_4.3_10bit_MOS',
    'MAMs_QT_2048x1556_4444_18fps_4.3_12bit_MOS',
    'MAMs_QT_2048x1556_422HQ_24fps_4.3_10bit_PCM_48.24_2.0',
    'MAMs_QT_2048x1556_422HQ_24fps_4.3_10bit_PCM_48.16_2.0',
    'MAMs_QT_2048x1556_422HQ_24fps_4.3_10bit_PCM_48.24_1.0',
    'MAMs_QT_2048x1556_422HQ_18fps_4.3_10bit_PCM_48.24_2.0',
    'MAMs_QT_2048x1485_422HQ_24fps_1.379_10bit_PCM_48.24_2.0',
    'MAMs_QT_2048x1485_422HQ_24fps_1.379_10bit_PCM_48.16_2.0',
    'MAMs_QT_2048x1485_422HQ_2398fps_1.379_10bit_PCM_48.16_2.0',
    'MAMs_QT_2048x1080_422HQ_30fps_1.89_10bit_PCM_48.16_2.0',
    'MAMs_AVI_1440x1080_RGB_25fps_4.3_8bit_MOS',
    'MAMs_AVI_1440x1080_RGB_25fps_4.3_24bit_MOS',
    'MAMs_QT_1920x1080_4444_24fps_16.9_12bit_PCM_48.24_2.0',
    'MAMs_QT_1920x1080_422HQ_5994fps_16.9_10bit_PCM_48.16_2.0',
    'MAMs_QT_1920x1080_422HQ_2997fps_16.9_10bit_PCM_48.24_1.0',
    'MAMs_QT_1920x1080_422HQ_24fps_16.9_10bit_PCM_48.24_2.0',
    'MAMs_QT_1920x1080_422HQ_2398fps_16.9_10bit_PCM_48.16_2.0',
    'MAMs_QT_1920x1080_422HQ_2398fps_16.9_10bit_PCM_48.16_1.0',
    'MAMs_QT_720x486_422HQ_2398fps_4.3_10bit_PCM_48.24_2.0',
    'MAMs_QT_720x486_V210_2997fps_4.3_10bit_PCM_48.24_2.0',
    'MAMs_QT_720x486_2VUY_2997fps_4.3_8bit_PCM_48.24_2.0',
    'MAMs_QT_720x486_2VUY_2997fps_3.2_8bit_PCM_48.24_2.0',
    'MAMs_QT_720x486_2VUY_2997fps_3.2_8bit_PCM_48.24_1.0',
    'MAMs_QT_720x480_422HQ_2997fps_4.3_10bit_PCM_48.16_2.0',
    'MAMs_AVI_720x480_DV_2997fps_4.3_8bit_PCM_48.16_2.0',
    'MAMs_QT_720x480_DV_2997fps_4.3_8bit_PCM_48.16_1.0',
    'MAMs_WAV_PCM_96.32_2.0',
    'MAMs_WAV_PCM_96.24_1.0',
    'MAMs_WAV_PCM_48.24_1.0',
    'MAMs_MP4_1920x1080_AVC_2398fps_16.9_8bit_AACLC_48.16_2.0',
    'MAMs_MXF_4096x2160_JPEG2000_24fps_1.9_12bit_PCM_48.24_6.0',
    'MAMs_MXF_2048x1080_JPEG2000_24fps_1.9_12bit_PCM_48.24_6.0',
    'MAMs_QT_1920x1080_422HQ_24fps_16.9_10bit_MOS',
    'MAMs_QT_1920x1080_422HQ_5994fps_16.9_10bit_PCM_48.24_2.0',
    'MAMs_QT_1920x1080_422HQ_2997fps_16.9_10bit_PCM_48.16_2.0'
] as Set

// =============================================================================
// NORMALISATION MAPPINGS
// =============================================================================

/*
 * Add or update mappings here when analysis returns new values. Unmapped
 * values pass through unchanged, so they appear in the No Match lookup key.
 */

// --- Container wrapper -------------------------------------------------------

// Keys are upper-case wrapper values returned by technical analysis.
// Values are the exact container labels used in UCLA's Baton profile names.
def wrapperMappings = [
    'QUICKTIME': 'QT',
    'WAVE'     : 'WAV'
]

// Wrappers that are only normalised for a specific file extension.
// Keys are upper-case wrapper values; values map a lower-case file extension
// (no dot) to the container label.
def wrapperMappingsByFileExt = [
    'MPEG-4'  : ['mov': 'QT'],
    'MXF-ATOM': ['mxf': 'MXF']
]

// --- Video codec -------------------------------------------------------------

// Keys are values returned by technical analysis.
// Values are the exact labels used in UCLA's Baton profile names.
def videoCodecMappings = [
    'PRORES_422_HQ': '422HQ',
    'V210'         : 'V210',
    '2VUY'         : '2VUY',
    'PRORES_4444'  : '4444',
    'BGR24'        : 'RGB',
    'DVCPRO'       : 'DV'
]

// --- Frame rate --------------------------------------------------------------

// Exact "numerator denominator" edit rates used by UCLA's Baton profile naming
// convention.
def editRateMappings = [
    '24000 1001': '2398fps',
    '23976 1000': '2398fps',
    '30000 1001': '2997fps',
    '29970 1000': '2997fps',
    '60000 1001': '5994fps',
    '59940 1000': '5994fps'
]

// Decimal frame rates, whether calculated from an edit rate or supplied as is.
def decimalFrameRateMappings = [
    '23.976': '2398fps',
    '23.98' : '2398fps',
    '29.97' : '2997fps',
    '59.94' : '5994fps'
]

// --- Aspect ratio ------------------------------------------------------------

// Any aspect ratio not listed is returned with the colon replaced by a full
// stop (4:3 -> 4.3).
def aspectRatioMappings = [
    '40:27'    : '3.2',
    '2048:1485': '1.379',
    '256:135'  : '1.89'
]

// Container-specific mappings, which take priority over aspectRatioMappings.
// Keys are normalised container labels, as returned by normaliseWrapper.
def aspectRatioMappingsByWrapper = [
    'MXF': ['256:135': '1.9']
]

// --- Video bit depth ---------------------------------------------------------

// Codec-specific bit depths, which take priority over BITS_PER_PIXEL (packed
// pixel storage can report 16 for 8bit media). Keys are upper-case codecs.
def videoBitDepthMappings = [
    '2VUY'  : '8bit',
    'DVCPRO': '8bit',
    'DV'    : '8bit'
]

// =============================================================================
// SAVE THE LOOKUP RESULT FOR LATER WORKFLOW STEPS
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
    def safeLookupKey = lookupKey != null ? lookupKey.toString() : ''
    def safeMatchResult = matchResult == 'Match' ? 'Match' : 'No Match'

    def metadataMap = [:]
    metadataMap['BPM:QC:LOOKUP_KEY'] = safeLookupKey
    metadataMap['BPM:QC:MATCH_RESULT'] = safeMatchResult

    BPM.saveMetadata(metadataMap)

    return safeMatchResult == 'Match' ? 'MATCH' : 'NO_MATCH'
}

// =============================================================================
// GENERAL XML HELPERS
// =============================================================================

def textValue = { node ->
    return node != null && node.size() > 0
        ? node.text().trim()
        : ''
}

// Returns the lower-case file extension without the dot, or '' if there is none.
def fileExtension = { fileName ->
    def cleanFileName =
        fileName != null ? fileName.trim().toLowerCase() : ''
    def dotIndex = cleanFileName.lastIndexOf('.')

    return dotIndex >= 0 ? cleanFileName.substring(dotIndex + 1) : ''
}

// Returns the first track of the given type in a FILE node, or null if there is none.
def firstTrack = { fileNode, trackName ->
    def tracks = fileNode?.TRACKS?."$trackName"

    return tracks != null && tracks.size() > 0 ? tracks[0] : null
}

// =============================================================================
// CONTAINER WRAPPER NORMALISATION
// =============================================================================

/*
 * Technical analysis may describe a QuickTime MOV container using different
 * wrapper values.
 *
 * UCLA's Baton profile naming convention uses QT, so QUICKTIME is normalised
 * to QT.
 *
 * Some MOV files are analysed with WRAPPER=MPEG-4. In that case, the file
 * extension of the FILE NAME attribute is checked. If it is mov, MPEG-4 is
 * normalised to QT.
 *
 * Genuine MPEG-4 files, such as .mp4 files, are left as MPEG-4.
 *
 * Other wrapper values are returned unchanged so unsupported or new values
 * remain visible in the generated No Match lookup key.
 */

// fileExt is the lower-case extension without the dot, as from fileExtension().
def normaliseWrapper = { wrapper, fileExt ->
    def cleanWrapper =
        wrapper != null ? wrapper.trim().toUpperCase() : ''

    def fileExtMapping = wrapperMappingsByFileExt[cleanWrapper]?.get(fileExt)

    if (fileExtMapping != null) {
        return fileExtMapping
    }

    return wrapperMappings.getOrDefault(cleanWrapper, cleanWrapper)
}

// =============================================================================
// VIDEO CODEC NORMALISATION
// =============================================================================

def normaliseVideoCodec = { codec ->
    def cleanCodec =
        codec != null ? codec.trim().toUpperCase() : ''

    /*
     * An unmapped codec is returned unchanged. The resulting No Match key
     * will expose the new value so UCLA can add the required mapping.
     */
    return videoCodecMappings.getOrDefault(cleanCodec, cleanCodec)
}

// =============================================================================
// FRAME-RATE NORMALISATION
// =============================================================================

def normaliseEditRate = { editRate ->
    def cleanRate =
        editRate != null ? editRate.trim().replaceAll('\\s+', ' ') : ''

    if (cleanRate == '') {
        return ''
    }

    if (editRateMappings.containsKey(cleanRate)) {
        return editRateMappings[cleanRate]
    }

    def parts = cleanRate.split(' ')

    if (parts.size() == 2) {
        def numerator = parts[0]
        def denominator = parts[1]

        if (denominator == '1') {
            return numerator + 'fps'
        }

        BigDecimal n = new BigDecimal(numerator)
        BigDecimal d = new BigDecimal(denominator)

        if (d != 0) {
            BigDecimal calculatedFps =
                n.divide(d, 6, BigDecimal.ROUND_HALF_UP)

            def fpsText =
                calculatedFps.stripTrailingZeros().toPlainString()

            return decimalFrameRateMappings.getOrDefault(
                fpsText,
                fpsText.replace('.', '') + 'fps'
            )
        }
    }

    return decimalFrameRateMappings.getOrDefault(
        cleanRate,
        cleanRate.replace('.', '').replaceAll('\\s+', '_') + 'fps'
    )
}

// =============================================================================
// ASPECT-RATIO NORMALISATION
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
 * Any aspect ratio not listed in the mappings is returned with the colon
 * replaced by a full stop.
 */
def normaliseAspectRatio = { aspectRatio, wrapper ->
    def cleanAspectRatio =
        aspectRatio != null ? aspectRatio.trim() : ''

    if (cleanAspectRatio == '') {
        return ''
    }

    return aspectRatioMappingsByWrapper[wrapper]?.get(cleanAspectRatio) ?:
        aspectRatioMappings[cleanAspectRatio] ?:
        cleanAspectRatio.replace(':', '.')
}

// =============================================================================
// VIDEO BIT-DEPTH NORMALISATION
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
def normaliseVideoBitDepth = { sourceCodec, bitsPerPixel ->
    def cleanCodec =
        sourceCodec != null ? sourceCodec.trim().toUpperCase() : ''

    if (videoBitDepthMappings.containsKey(cleanCodec)) {
        return videoBitDepthMappings[cleanCodec]
    }

    def cleanBits =
        bitsPerPixel != null ? bitsPerPixel.trim() : ''

    return cleanBits != ''
        ? cleanBits + 'bit'
        : ''
}

// =============================================================================
// AUDIO SAMPLE-RATE NORMALISATION
// =============================================================================

def normaliseSampleRate = { audioEditRate ->
    if (audioEditRate == null || audioEditRate.trim() == '') {
        return ''
    }

    def parts = audioEditRate.trim().split('\\s+')
    BigDecimal hz = new BigDecimal(parts[0])

    // Baton profile names express 48000 Hz as 48.
    return hz
        .divide(
            new BigDecimal('1000'),
            0,
            BigDecimal.ROUND_HALF_UP
        )
        .toPlainString()
}

// =============================================================================
// AUDIO CHANNEL NORMALISATION
// =============================================================================

def normaliseChannels = { channels ->
    if (channels == null || channels.trim() == '') {
        return ''
    }

    return channels.trim() + '.0'
}

// =============================================================================
// SELECT THE VIDEO AND AUDIO TRACKS
// =============================================================================

def hasMxfAtomWrapper = { fileNode ->
    return textValue(fileNode.WRAPPER).toUpperCase() == 'MXF-ATOM'
}

// A FILE node is MXF when its wrapper is MXF-Atom and its name ends in .mxf.
def isMxfFile = { fileNode ->
    return hasMxfAtomWrapper(fileNode) &&
        fileExtension(fileNode.@NAME.text()) == 'mxf'
}

/*
 * MXF packages may carry the VIDEO_TRACK and AUDIO_TRACK in different FILE
 * nodes, and must contain both. Each track must be in a FILE node that is
 * MXF. Other FILE nodes, such as proxies, are ignored.
 *
 * All other formats are read from the first FILE node only. It may contain
 * video, audio or both.
 *
 * Returns the selected FILE nodes and tracks, or a message explaining why
 * they could not be selected.
 */
def selectTracks = { fileNodes ->
    // Any FILE node with an MXF-Atom wrapper makes the XML an MXF package.
    def isMxfPackage = fileNodes.any { fileNode -> hasMxfAtomWrapper(fileNode) }

    def videoFileNode
    def audioFileNode

    if (isMxfPackage) {
        videoFileNode = fileNodes.find { fileNode ->
            isMxfFile(fileNode) && firstTrack(fileNode, 'VIDEO_TRACK') != null
        }
        audioFileNode = fileNodes.find { fileNode ->
            isMxfFile(fileNode) && firstTrack(fileNode, 'AUDIO_TRACK') != null
        }
    } else {
        videoFileNode = fileNodes[0]
        audioFileNode = fileNodes[0]
    }

    def videoTrack = firstTrack(videoFileNode, 'VIDEO_TRACK')
    def audioTrack = firstTrack(audioFileNode, 'AUDIO_TRACK')

    if (isMxfPackage) {
        def missingTracks = []

        if (videoTrack == null) {
            missingTracks << 'VIDEO_TRACK'
        }

        if (audioTrack == null) {
            missingTracks << 'AUDIO_TRACK'
        }

        if (!missingTracks.isEmpty()) {
            return [
                message: 'MXF package requires both a VIDEO_TRACK and an ' +
                    'AUDIO_TRACK, each in a FILE node with wrapper MXF-Atom ' +
                    'and a .mxf file name. Missing: ' + missingTracks.join(', ')
            ]
        }
    } else if (videoTrack == null && audioTrack == null) {
        return [message: 'No VIDEO_TRACK or AUDIO_TRACK found in the first FILE node.']
    }

    return [
        videoFileNode: videoFileNode,
        videoTrack   : videoTrack,
        audioFileNode: audioFileNode,
        audioTrack   : audioTrack,
        message      : ''
    ]
}

// =============================================================================
// READ AND NORMALISE THE KEY COMPONENTS
// =============================================================================

/*
 * Each reader returns:
 *
 * required - values that must not be blank, keyed by component name
 * keyParts - the lookup key segments, in Baton profile name order
 */
def readVideoComponents = { videoTrack, wrapper ->
    def width = textValue(videoTrack.VIDEO_SIZE_WIDTH)
    def height = textValue(videoTrack.VIDEO_SIZE_HEIGHT)
    def sourceVideoCodec = textValue(videoTrack.VIDEO_CODEC)
    def videoCodec = normaliseVideoCodec(sourceVideoCodec)
    def fps = normaliseEditRate(textValue(videoTrack.EDIT_RATE))
    def aspectRatio =
        normaliseAspectRatio(textValue(videoTrack.ASPECT_RATIO), wrapper)
    def videoBitDepth = normaliseVideoBitDepth(
        sourceVideoCodec,
        textValue(videoTrack.BITS_PER_PIXEL)
    )

    return [
        required: [
            'resolution width'  : width,
            'resolution height' : height,
            'video codec'       : videoCodec,
            'frame rate'        : fps,
            'aspect ratio'      : aspectRatio,
            'video bit depth'   : videoBitDepth
        ],
        keyParts: [
            width + 'x' + height,
            videoCodec,
            fps,
            aspectRatio,
            videoBitDepth
        ]
    ]
}

def readAudioComponents = { audioTrack ->
    /*
     * UCLA Baton profiles use MOS as the final component when the
     * source media contains no audio.
     *
     * No sample rate, audio bit depth or channel values are appended
     * after MOS.
     *
     * Example:
     * MAMs_QT_2048x1556_4444_18fps_4.3_12bit_MOS
     */
    if (audioTrack == null) {
        return [required: [:], keyParts: ['MOS']]
    }

    /*
     * The audio channel value in the profile name represents the first audio
     * track, not the total number of channels across every audio track.
     *
     * For example, an asset containing several mono AUDIO_TRACK elements still
     * generates 1.0 from the first AUDIO_TRACK.
     */
    def audioCodec = textValue(audioTrack.AUDIO_CODEC).toUpperCase()
    def sampleRate = normaliseSampleRate(textValue(audioTrack.EDIT_RATE))
    def audioBitDepth = textValue(audioTrack.BITS_PER_AUDIO_SAMPLE)
    def channels =
        normaliseChannels(textValue(audioTrack.AUDIO_CHANNELS_PER_TRACK))

    return [
        required: [
            'audio format'      : audioCodec,
            'audio sample rate' : sampleRate,
            'audio bit depth'   : audioBitDepth,
            'audio channels'    : channels
        ],
        keyParts: [
            audioCodec,
            sampleRate + '.' + audioBitDepth,
            channels
        ]
    ]
}

// =============================================================================
// SHARED TECHNICAL XML LOOKUP COMPUTATION
// =============================================================================
def buildLookupResult = { technicalXml ->
    def noMatch = { message ->
        return [lookupKey: '', matchResult: 'No Match', message: message]
    }

    def fileNodes = technicalXml.ASSET?.TECHNICAL?.FILE
    if (fileNodes == null || fileNodes.size() == 0) {
        return noMatch('No FILE node found in technical XML.')
    }

    def tracks = selectTracks(fileNodes)
    if (tracks.message) {
        return noMatch(tracks.message)
    }

    def hasVideo = tracks.videoTrack != null

    // The container comes from the video FILE node, or from the audio FILE
    // node when there is no video.
    def sourceFileNode = hasVideo ? tracks.videoFileNode : tracks.audioFileNode
    def sourceWrapper = textValue(sourceFileNode.WRAPPER)
    def sourceFileExt = fileExtension(sourceFileNode.@NAME.text())
    def wrapper = normaliseWrapper(sourceWrapper, sourceFileExt)

    def videoComponents = hasVideo
        ? readVideoComponents(tracks.videoTrack, wrapper)
        : [required: [:], keyParts: []]
    def audioComponents = readAudioComponents(tracks.audioTrack)

    def requiredValues =
        ['container': wrapper] + videoComponents.required + audioComponents.required

    def missingValues = requiredValues.findAll { name, value ->
        value == null || value.toString().trim() == ''
    }.keySet()

    if (!missingValues.isEmpty()) {
        return noMatch('Missing technical values: ' + missingValues.join(', '))
    }

    def lookupKey = 'MAMs_' + (
        [wrapper] + videoComponents.keyParts + audioComponents.keyParts
    ).join('_')

    // The generated lookup key must match a configured profile name exactly.
    def matchResult = knownBatonProfiles.contains(lookupKey) ? 'Match' : 'No Match'
    return [lookupKey: lookupKey, matchResult: matchResult, message: '']
}

// =============================================================================
// RUN FIXTURE TESTS OR SAVE THE PRODUCTION RESULT
// =============================================================================
def runTests = {
    def tests = [
        [
            fixture: 'fixtures/test_mxf_package.xml',
            expectedLookupKey:
                'MAMs_MXF_2048x1080_JPEG2000_24fps_1.9_12bit_PCM_48.24_6.0',
            expectedMatchResult: 'Match'
        ],
        [
            fixture: 'fixtures/test_audio_only.xml',
            expectedLookupKey: 'MAMs_WAV_PCM_48.24_1.0',
            expectedMatchResult: 'Match'
        ]
    ]

    def failedTests = []
    def recordFailure = { test, message ->
        println "FAIL: ${test.fixture} (${message})"
        failedTests << test.fixture
    }

    tests.each { test ->
        def fixture = java.nio.file.Paths.get(test.fixture)

        if (!java.nio.file.Files.isRegularFile(fixture)) {
            println "FAIL: ${test.fixture} (fixture not found)"
            failedTests << test.fixture
            return
        }

        try {
            def technicalXml = new groovy.xml.XmlSlurper(false, false).parseText(
                java.nio.file.Files.readString(fixture)
            )
            def result = buildLookupResult(technicalXml)

            if (
                result.lookupKey == test.expectedLookupKey &&
                result.matchResult == test.expectedMatchResult
            ) {
                println "PASS: ${test.fixture}"
            } else {
                println "FAIL: ${test.fixture}"
                println "  expected key:    ${test.expectedLookupKey}"
                println "  actual key:      ${result.lookupKey}"
                println "  expected result: ${test.expectedMatchResult}"
                println "  actual result:   ${result.matchResult}"
                failedTests << test.fixture
            }
        } catch (java.io.IOException e) {
            recordFailure(test, e.message)
        } catch (org.xml.sax.SAXException e) {
            recordFailure(test, e.message)
        } catch (NumberFormatException e) {
            recordFailure(test, e.message)
        }
    }

    if (!failedTests.isEmpty()) {
        throw new AssertionError(
            'Fixture tests failed: ' + failedTests.join(', ')
        )
    }
}

def runProduction = { technicalXml ->
    def result = buildLookupResult(technicalXml)

    if (result.message) {
        logger.info(
            'QC lookup key could not be built. ' + result.message
        )
    } else {
        logger.info('QC Lookup Key : ' + result.lookupKey)
        logger.info('QC Match Result : ' + result.matchResult)

        if (result.matchResult == 'No Match') {
            logger.info(
                'Known Baton profiles: ' +
                knownBatonProfiles.join(' | ')
            )
        }
    }

    return saveLookupResult(result.lookupKey, result.matchResult)
}

def technicalXml

try {
    technicalXml = WIPUtils.technical()
} catch (groovy.lang.MissingPropertyException e) {
    if (e.property != 'WIPUtils') {
        throw e
    }

    println 'Production WIPUtils class is unavailable. Running in test mode.'
    return runTests()
}

return runProduction(technicalXml)
