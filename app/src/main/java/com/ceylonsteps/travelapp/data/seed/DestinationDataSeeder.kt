package com.ceylonsteps.travelapp.data.seed

import com.ceylonsteps.travelapp.data.model.*

object DestinationDataSeeder {

    fun get100PlusDestinations(): List<Destination> = listOf(
        // ================= CENTRAL PROVINCE (15) =================
        Destination(
            "cp_01", "Sigiriya Rock Fortress", "සීගිරිය",
            Province.CENTRAL, "Matale", DestinationCategory.HERITAGE,
            7.9570, 80.7603,
            "Ancient 5th-century rock palace with frescoes and Lion's paw entrance.",
            "https://images.unsplash.com/photo-1586861635167-e5223aadc9fe?w=800"
        ),
        Destination(
            "cp_02", "Temple of the Sacred Tooth Relic", "ශ්‍රී දළදා මාළිගාව",
            Province.CENTRAL, "Kandy", DestinationCategory.CULTURAL,
            7.2936, 80.6413,
            "Venerated Buddhist temple housing the sacred tooth relic of the Buddha.",
            "https://images.unsplash.com/photo-1588598198321-9735fd52455d?w=800"
        ),
        Destination(
            "cp_03", "Pidurangala Rock", "පිදුරංගල",
            Province.CENTRAL, "Matale", DestinationCategory.HIKING_NATURE,
            7.9654, 80.7634,
            "Popular viewpoint summit offering sunrise vistas of Sigiriya.",
            "https://images.unsplash.com/photo-1609137144813-7d9921338f24?w=800"
        ),
        Destination(
            "cp_04", "Knuckles Mountain Range", "නකල්ස් කඳුවැටිය",
            Province.CENTRAL, "Matale/Kandy", DestinationCategory.HIKING_NATURE,
            7.4647, 80.8142,
            "UNESCO biosphere reserve famous for cloud forests and mist-covered peaks.",
            "https://images.unsplash.com/photo-1544735716-392fe2489ffa?w=800"
        ),
        Destination(
            "cp_05", "Horton Plains & World's End", "හෝර්ටන් තැන්න",
            Province.CENTRAL, "Nuwara Eliya", DestinationCategory.HIKING_NATURE,
            6.8096, 80.8005,
            "Plateau plateau ending in a sheer cliff plunge of 880 meters.",
            "https://images.unsplash.com/photo-1578637387939-43c525550085?w=800"
        ),
        Destination(
            "cp_06", "Ramboda Falls", "රම්බොඩ ඇල්ල",
            Province.CENTRAL, "Nuwara Eliya", DestinationCategory.WATERFALL,
            7.0543, 80.6974,
            "Stunning twin cascade waterfall along the Gampola-Nuwara Eliya road.",
            "https://images.unsplash.com/photo-1596707328965-021e1026027a?w=800"
        ),
        Destination(
            "cp_07", "Dambulla Cave Temple", "දඹුල්ල රජමහා විහාරය",
            Province.CENTRAL, "Matale", DestinationCategory.HERITAGE,
            7.8567, 80.6482,
            "Vast cave monastery complex adorned with centuries-old murals and statues.",
            "https://images.unsplash.com/photo-1546708973-b339540b5162?w=800"
        ),
        Destination(
            "cp_08", "Sembuwatta Lake", "සෙම්බුවත්ත වැව",
            Province.CENTRAL, "Matale", DestinationCategory.HIKING_NATURE,
            7.4361, 80.6989,
            "Emerald-hued spring lake surrounded by pine forests and tea estates.",
            "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800"
        ),
        Destination(
            "cp_09", "Ambuluwawa Tower", "අම්බුළුවාව",
            Province.CENTRAL, "Kandy", DestinationCategory.HIKING_NATURE,
            7.1953, 80.5489,
            "Biodiversity complex with a spiral cone-shaped multi-religious tower.",
            "https://images.unsplash.com/photo-1628178875604-0cb859546026?w=800"
        ),
        Destination(
            "cp_10", "St. Clair's Falls", "සෙන්ට් ක්ලෙයාර් ඇල්ල",
            Province.CENTRAL, "Nuwara Eliya", DestinationCategory.WATERFALL,
            6.9461, 80.6384,
            "Known as the Little Niagara of Sri Lanka running through lush tea gardens.",
            "https://images.unsplash.com/photo-1518457607834-6e8d80c183c5?w=800"
        ),
        Destination(
            "cp_11", "Devon Falls", "ඩෙවොන් ඇල්ල",
            Province.CENTRAL, "Nuwara Eliya", DestinationCategory.WATERFALL,
            6.9535, 80.6558,
            "Picturesque 97-meter tier waterfall facing the main highway.",
            "https://images.unsplash.com/photo-1432405972618-c60b0225b8f9?w=800"
        ),
        Destination(
            "cp_12", "Gregory Lake", "ග්‍රෙගරි වැව",
            Province.CENTRAL, "Nuwara Eliya", DestinationCategory.HIKING_NATURE,
            6.9567, 80.7819,
            "Scenic colonial-era recreational water park in the heart of Little England.",
            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800"
        ),
        Destination(
            "cp_13", "Riverston & Pitawala Pathana", "රිවස්ටන්",
            Province.CENTRAL, "Matale", DestinationCategory.HIKING_NATURE,
            7.5255, 80.7383,
            "High-wind ridge gaps with sheer grassland cliffs and pristine nature.",
            "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800"
        ),
        Destination(
            "cp_14", "Victoria Dam & Reservoir", "වික්ටෝරියා ජලාශය",
            Province.CENTRAL, "Kandy", DestinationCategory.HIKING_NATURE,
            7.2418, 80.7937,
            "Tallest double-curvature arch dam overlooking cascading mist-covered hills.",
            "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=800"
        ),
        Destination(
            "cp_15", "Seetha Amman Temple", "සීතා අම්මාන් කෝවිල",
            Province.CENTRAL, "Nuwara Eliya", DestinationCategory.CULTURAL,
            6.9367, 80.8122,
            "Legendary Ashok Vatika temple dedicated to Sita surrounded by streams.",
            "https://images.unsplash.com/photo-1544735716-392fe2489ffa?w=800"
        ),

        // ================= SOUTHERN PROVINCE (15) =================
        Destination(
            "sp_01", "Galle Dutch Fort", "ගාල්ල ලන්දේසි කොටුව",
            Province.SOUTHERN, "Galle", DestinationCategory.HERITAGE,
            6.0270, 80.2170,
            "UNESCO World Heritage living rampart fort built by Portuguese and Dutch.",
            "https://images.unsplash.com/photo-1566837945700-30057527ade0?w=800"
        ),
        Destination(
            "sp_02", "Mirissa Secret Beach & Coconut Tree Hill", "මිරිස්ස",
            Province.SOUTHERN, "Matara", DestinationCategory.BEACH,
            5.9431, 80.4578,
            "Picturesque palm-covered cliff headland overlooking rolling surf breaks.",
            "https://images.unsplash.com/photo-1544644181-1484b3fdfc62?w=800"
        ),
        Destination(
            "sp_03", "Yala National Park", "යාල ජාතික වනෝද්‍යානය",
            Province.SOUTHERN, "Hambantota", DestinationCategory.WILDLIFE,
            6.3712, 81.5204,
            "World-renowned wilderness sanctuary with the highest density of leopards.",
            "https://images.unsplash.com/photo-1516426122078-c23e76319801?w=800"
        ),
        Destination(
            "sp_04", "Hiriketiya Horseshoe Bay", "හිරිකැටිය",
            Province.SOUTHERN, "Matara", DestinationCategory.BEACH,
            5.9592, 80.6612,
            "Sheltered turquoise cove adored by surfers and cafe culture enthusiasts.",
            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800"
        ),
        Destination(
            "sp_05", "Unawatuna Beach & Japanese Peace Pagoda", "උණවටුන",
            Province.SOUTHERN, "Galle", DestinationCategory.BEACH,
            6.0108, 80.2494,
            "Golden sand bay with a hilltop Buddhist stupa offering coastal sunset vistas.",
            "https://images.unsplash.com/photo-1519046904884-53103b34b206?w=800"
        ),
        Destination(
            "sp_06", "Hummanaya Blowhole", "හුම්මානය",
            Province.SOUTHERN, "Matara/Hambantota", DestinationCategory.HIKING_NATURE,
            5.9723, 80.7061,
            "World's second largest marine blowhole shooting ocean spray meters high.",
            "https://images.unsplash.com/photo-1505118380757-91f5f5632de0?w=800"
        ),
        Destination(
            "sp_07", "Dondra Head Lighthouse", "දෙවිනුවර ප්‍රදීපාගාරය",
            Province.SOUTHERN, "Matara", DestinationCategory.HERITAGE,
            5.9238, 80.5898,
            "Tallest lighthouse in Sri Lanka standing on the southernmost tip of the island.",
            "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?w=800"
        ),
        Destination(
            "sp_08", "Bundala National Park", "බූන්දල ජාතික වනෝද්‍යානය",
            Province.SOUTHERN, "Hambantota", DestinationCategory.WILDLIFE,
            6.1772, 81.2291,
            "Ramsar wetland sanctuary home to thousands of migratory flamingos and birds.",
            "https://images.unsplash.com/photo-1547471080-7cc2caa01a7e?w=800"
        ),
        Destination(
            "sp_09", "Koggala Stilt Fishermen & Lake", "කොග්ගල",
            Province.SOUTHERN, "Galle", DestinationCategory.CULTURAL,
            5.9897, 80.3298,
            "Traditional crossbar fishermen practicing an iconic centuries-old coastal craft.",
            "https://images.unsplash.com/photo-1544644181-1484b3fdfc62?w=800"
        ),
        Destination(
            "sp_10", "Weligama Bay Surf Coast", "වැලිගම",
            Province.SOUTHERN, "Matara", DestinationCategory.BEACH,
            5.9729, 80.4287,
            "Gentle sand-bottom bay celebrated as the premiere beginner surf destination.",
            "https://images.unsplash.com/photo-1502680390469-be75c86b636f?w=800"
        ),
        Destination(
            "sp_11", "Kataragama Sacred City", "කතරගම පුදබිම",
            Province.SOUTHERN, "Hambantota", DestinationCategory.CULTURAL,
            6.4137, 81.3328,
            "Multifaith pilgrimage sanctuary honoring Lord Skanda by the Menik Ganga.",
            "https://images.unsplash.com/photo-1588598198321-9735fd52455d?w=800"
        ),
        Destination(
            "sp_12", "Madu Ganga Mangrove Lagoon", "මාදු ගඟ",
            Province.SOUTHERN, "Galle", DestinationCategory.HIKING_NATURE,
            6.2996, 80.0526,
            "Sprawling estuary with 64 islands, cinnamon peeling sheds, and fish therapy.",
            "https://images.unsplash.com/photo-1544735716-392fe2489ffa?w=800"
        ),
        Destination(
            "sp_13", "Tangalle Silent Beach", "තංගල්ල",
            Province.SOUTHERN, "Hambantota", DestinationCategory.BEACH,
            6.0242, 80.7941,
            "Pristine expanse of wild golden coastline with azure crashing surf.",
            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800"
        ),
        Destination(
            "sp_14", "Ridiyagama Safari Park", "රිදියගම සෆාරි උද්‍යානය",
            Province.SOUTHERN, "Hambantota", DestinationCategory.WILDLIFE,
            6.2235, 80.9702,
            "500-acre open-air drive-thru animal safari sanctuary.",
            "https://images.unsplash.com/photo-1516426122078-c23e76319801?w=800"
        ),
        Destination(
            "sp_15", "Sinharaja Southern Entrance (Pitadeniya)", "සිංහරාජය (පිටදෙණිය)",
            Province.SOUTHERN, "Galle/Matara", DestinationCategory.HIKING_NATURE,
            6.3833, 80.5000,
            "Southern gateway to Sri Lanka's pristine primary lowland tropical rainforest.",
            "https://images.unsplash.com/photo-1448375240586-882707db888b?w=800"
        ),

        // ================= UVA PROVINCE (12) =================
        Destination(
            "up_01", "Nine Arches Bridge", "ආරුක්කු නමයේ පාලම",
            Province.UVA, "Badulla", DestinationCategory.HERITAGE,
            6.8768, 81.0608,
            "Colonial stone viaduct tucked amidst emerald tea fields near Demodara.",
            "https://images.unsplash.com/photo-1546708973-b339540b5162?w=800"
        ),
        Destination(
            "up_02", "Little Adam's Peak (Punchi Sri Pada)", "පුංචි ශ්‍රී පාදය",
            Province.UVA, "Badulla", DestinationCategory.HIKING_NATURE,
            6.8622, 81.0632,
            "Pyramidal peak walk in Ella offering panoramic views of Ella Gap.",
            "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800"
        ),
        Destination(
            "up_03", "Diyaluma Falls", "දියලුම ඇල්ල",
            Province.UVA, "Badulla", DestinationCategory.WATERFALL,
            6.7328, 81.0319,
            "220-meter drop with natural infinity plunge pools at its upper rim.",
            "https://images.unsplash.com/photo-1432405972618-c60b0225b8f9?w=800"
        ),
        Destination(
            "up_04", "Dunhinda Falls", "දුන්හිඳ ඇල්ල",
            Province.UVA, "Badulla", DestinationCategory.WATERFALL,
            7.0270, 81.0658,
            "Roaring Smoky Falls named after the perpetual curtain of mist it creates.",
            "https://images.unsplash.com/photo-1596707328965-021e1026027a?w=800"
        ),
        Destination(
            "up_05", "Ravana Falls & Cave", "රාවණා ඇල්ල",
            Province.UVA, "Badulla", DestinationCategory.WATERFALL,
            6.8409, 81.0543,
            "Cascading cliff waterfall steeped in Ramayana mythology.",
            "https://images.unsplash.com/photo-1518457607834-6e8d80c183c5?w=800"
        ),
        Destination(
            "up_06", "Bambarakanda Falls", "බඹරකන්ද ඇල්ල",
            Province.UVA, "Badulla", DestinationCategory.WATERFALL,
            6.7739, 80.8322,
            "Sri Lanka's tallest waterfall plummeting 263 meters through a pine gorge.",
            "https://images.unsplash.com/photo-1432405972618-c60b0225b8f9?w=800"
        ),
        Destination(
            "up_07", "Ella Rock", "ඇල්ල රොක්",
            Province.UVA, "Badulla", DestinationCategory.HIKING_NATURE,
            6.8550, 81.0475,
            "Challenging summit trek through eucalyptus groves and railway tracks.",
            "https://images.unsplash.com/photo-1544735716-392fe2489ffa?w=800"
        ),
        Destination(
            "up_08", "Lipton's Seat", "ලිප්ටන්ස් සීට්",
            Province.UVA, "Badulla", DestinationCategory.HIKING_NATURE,
            6.7797, 81.0147,
            "High viewpoint where Sir Thomas Lipton surveyed his Dambatenne tea empire.",
            "https://images.unsplash.com/photo-1578637387939-43c525550085?w=800"
        ),
        Destination(
            "up_09", "Madulsima Mini World's End", "මඩුල්සිම",
            Province.UVA, "Badulla", DestinationCategory.HIKING_NATURE,
            7.0503, 81.1872,
            "Dramatic mountain plateau where clouds drift right beneath your feet.",
            "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800"
        ),
        Destination(
            "up_10", "Muthiyangana Raja Maha Viharaya", "මුතියංගණ රජමහා විහාරය",
            Province.UVA, "Badulla", DestinationCategory.CULTURAL,
            6.9897, 81.0583,
            "Ancient Solosmasthana temple sanctified by the Buddha's visit in Badulla.",
            "https://images.unsplash.com/photo-1588598198321-9735fd52455d?w=800"
        ),
        Destination(
            "up_11", "Gal Oya National Park", "ගල්ඔය ජාතික වනෝද්‍යානය",
            Province.UVA, "Monaragala", DestinationCategory.WILDLIFE,
            7.2181, 81.4789,
            "Sole national park offering boat safaris to witness swimming elephants.",
            "https://images.unsplash.com/photo-1516426122078-c23e76319801?w=800"
        ),
        Destination(
            "up_12", "Buduruwagala Rock Sculptures", "බුදුරුවගල",
            Province.UVA, "Monaragala", DestinationCategory.HERITAGE,
            6.6908, 81.0803,
            "Seven colossal Mahayana Buddhist statues carved directly into a cliff rock face.",
            "https://images.unsplash.com/photo-1546708973-b339540b5162?w=800"
        ),

        // ================= EASTERN PROVINCE (12) =================
        Destination(
            "ep_01", "Arugam Bay Surf Point", "ආරුගම්බේ",
            Province.EASTERN, "Ampara", DestinationCategory.BEACH,
            6.8419, 81.8344,
            "World-class right-hand point break rated among the globe's top surf havens.",
            "https://images.unsplash.com/photo-1502680390469-be75c86b636f?w=800"
        ),
        Destination(
            "ep_02", "Pigeon Island National Park", "පරවි දූපත",
            Province.EASTERN, "Trincomalee", DestinationCategory.WILDLIFE,
            8.7208, 81.2033,
            "Marine sanctuary vibrant with coral reefs, reef sharks, and sea turtles.",
            "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800"
        ),
        Destination(
            "ep_03", "Nilaveli Beach", "නිලාවැලි වෙරළ",
            Province.EASTERN, "Trincomalee", DestinationCategory.BEACH,
            8.6917, 81.1969,
            "Pristine white sand shoreline with calm crystal-clear shallow seas.",
            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800"
        ),
        Destination(
            "ep_04", "Koneswaram Temple & Swami Rock", "කෝනේශ්වරම් කෝවිල",
            Province.EASTERN, "Trincomalee", DestinationCategory.CULTURAL,
            8.5772, 81.2442,
            "Ancient cliff-top Hindu shrine perched high over Trincomalee harbor bay.",
            "https://images.unsplash.com/photo-1588598198321-9735fd52455d?w=800"
        ),
        Destination(
            "ep_05", "Pasikuda & Kalkudah Bay", "පාසිකුඩා",
            Province.EASTERN, "Batticaloa", DestinationCategory.BEACH,
            7.9256, 81.5642,
            "Calm shallow bay allowing swimmers to walk hundreds of meters into the sea.",
            "https://images.unsplash.com/photo-1519046904884-53103b34b206?w=800"
        ),
        Destination(
            "ep_06", "Batticaloa Lighthouse & Singing Fish Lagoon", "මඩකලපුව ප්‍රදීපාගාරය",
            Province.EASTERN, "Batticaloa", DestinationCategory.HERITAGE,
            7.7533, 81.7011,
            "Historic lagoon lighthouse famous for mythical singing fish on full moon nights.",
            "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?w=800"
        ),
        Destination(
            "ep_07", "Kumana National Park", "කුමන ජාතික වනෝද්‍යානය",
            Province.EASTERN, "Ampara", DestinationCategory.WILDLIFE,
            6.5367, 81.7083,
            "Famed bird breeding ground and wild safari park bordering the Indian Ocean.",
            "https://images.unsplash.com/photo-1547471080-7cc2caa01a7e?w=800"
        ),
        Destination(
            "ep_08", "Kanniya Hot Springs", "කන්නියා උණුදිය උල්පත්",
            Province.EASTERN, "Trincomalee", DestinationCategory.CULTURAL,
            8.5997, 81.1772,
            "Seven square hot water thermal wells with therapeutic and spiritual folklore.",
            "https://images.unsplash.com/photo-1505118380757-91f5f5632de0?w=800"
        ),
        Destination(
            "ep_09", "Marble Beach", "මාබල් බීච්",
            Province.EASTERN, "Trincomalee", DestinationCategory.BEACH,
            8.5178, 81.2178,
            "Glassy tranquil inlet nestled in a forested naval cove.",
            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800"
        ),
        Destination(
            "ep_10", "Deegawapiya Stupa", "දීඝවාපිය",
            Province.EASTERN, "Ampara", DestinationCategory.HERITAGE,
            7.2189, 81.7483,
            "Sacred 3rd-century BC monastery of great antiquity in the eastern quadrant.",
            "https://images.unsplash.com/photo-1546708973-b339540b5162?w=800"
        ),
        Destination(
            "ep_11", "Kudumbigala Monastery", "කුඩුම්බිගල ආරණ්‍ය සේනාසනය",
            Province.EASTERN, "Ampara", DestinationCategory.HERITAGE,
            6.6694, 81.7469,
            "Meditative hermitage set atop isolated granite rocks in the wilderness.",
            "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800"
        ),
        Destination(
            "ep_12", "Fort Frederick", "ෆෙඩ්‍රික් කොටුව",
            Province.EASTERN, "Trincomalee", DestinationCategory.HERITAGE,
            8.5808, 81.2403,
            "Historical coastal fort built in 1624 home to friendly spotted deer.",
            "https://images.unsplash.com/photo-1566837945700-30057527ade0?w=800"
        ),

        // ================= NORTHERN PROVINCE (12) =================
        Destination(
            "np_01", "Nallur Kandaswamy Kovil", "නල්ලූර් කෝවිල",
            Province.NORTHERN, "Jaffna", DestinationCategory.CULTURAL,
            9.6744, 80.0294,
            "Architectural Dravidian jewel celebrated for its annual vibrant chariot festival.",
            "https://images.unsplash.com/photo-1588598198321-9735fd52455d?w=800"
        ),
        Destination(
            "np_02", "Jaffna Fort", "යාපනය කොටුව",
            Province.NORTHERN, "Jaffna", DestinationCategory.HERITAGE,
            9.6617, 80.0089,
            "Vast pentagonal limestone fortress fronting the Jaffna lagoon.",
            "https://images.unsplash.com/photo-1566837945700-30057527ade0?w=800"
        ),
        Destination(
            "np_03", "Point Pedro & Lighthouse", "පේදුරු තුඩුව",
            Province.NORTHERN, "Jaffna", DestinationCategory.HERITAGE,
            9.8356, 80.2472,
            "The absolute northernmost geographic point of Sri Lanka.",
            "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?w=800"
        ),
        Destination(
            "np_04", "Nagadeepa Purana Viharaya & Kovil", "නාගදීපය",
            Province.NORTHERN, "Jaffna", DestinationCategory.CULTURAL,
            9.6139, 79.7736,
            "Sacred island sanctified by the Buddha's second legendary visit.",
            "https://images.unsplash.com/photo-1546708973-b339540b5162?w=800"
        ),
        Destination(
            "np_05", "Delft Island (Neduntheevu)", "ඩෙල්ෆ්ට් දූපත",
            Province.NORTHERN, "Jaffna", DestinationCategory.HERITAGE,
            9.5167, 79.6833,
            "Remote island famous for wild ponies, coral-stone walls, and giant Baobab trees.",
            "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800"
        ),
        Destination(
            "np_06", "Casuarina Beach", "කැසුරිනා වෙරළ",
            Province.NORTHERN, "Jaffna (Karainagar)", DestinationCategory.BEACH,
            9.7614, 79.8886,
            "Silky white sand beach sheltered by whispers of coastal casuarina trees.",
            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800"
        ),
        Destination(
            "np_07", "Keerimalai Sacred Water Spring", "කීරිමලෙයි පොකුණ",
            Province.NORTHERN, "Jaffna", DestinationCategory.CULTURAL,
            9.8122, 80.0139,
            "Natural freshwater sea pool renowned for curative mineral properties.",
            "https://images.unsplash.com/photo-1505118380757-91f5f5632de0?w=800"
        ),
        Destination(
            "np_08", "Mannar Baobab Tree & Fort", "මන්නාරම බයෝබැබ් ගස",
            Province.NORTHERN, "Mannar", DestinationCategory.HERITAGE,
            8.9839, 79.9142,
            "Colossal 700-year-old tree planted by Arab sea traders alongside historic fort.",
            "https://images.unsplash.com/photo-1448375240586-882707db888b?w=800"
        ),
        Destination(
            "np_09", "Adam's Bridge (Rama Setu)", "ආදම්ගේ පාලම",
            Province.NORTHERN, "Mannar", DestinationCategory.HIKING_NATURE,
            9.0736, 79.5317,
            "Chain of limestone sandbanks connecting the tip of Mannar towards Rameswaram.",
            "https://images.unsplash.com/photo-1519046904884-53103b34b206?w=800"
        ),
        Destination(
            "np_10", "Thiruketheeswaram Kovil", "තිරුකේතීස්වරම් කෝවිල",
            Province.NORTHERN, "Mannar", DestinationCategory.CULTURAL,
            8.9483, 79.9897,
            "One of the Pancha Ishwarams dedicated to Lord Shiva since ancient times.",
            "https://images.unsplash.com/photo-1588598198321-9735fd52455d?w=800"
        ),
        Destination(
            "np_11", "Jaffna Public Library", "යාපනය පුස්තකාලය",
            Province.NORTHERN, "Jaffna", DestinationCategory.HERITAGE,
            9.6619, 80.0136,
            "Iconic Indo-Saracenic cultural landmark and historic center of Asian literature.",
            "https://images.unsplash.com/photo-1566837945700-30057527ade0?w=800"
        ),
        Destination(
            "np_12", "Kantharodai Ancient Stupas (Kandarodai)", "කඳුරුගොඩ විහාරය",
            Province.NORTHERN, "Jaffna (Chunnakam)", DestinationCategory.HERITAGE,
            9.7461, 80.0117,
            "Cluster of miniature coral-stone stupas dating back to early Buddhist antiquity.",
            "https://images.unsplash.com/photo-1546708973-b339540b5162?w=800"
        ),

        // ================= NORTH CENTRAL PROVINCE (12) =================
        Destination(
            "ncp_01", "Jaya Sri Maha Bodhi", "ජය ශ්‍රී මහා බෝධිය",
            Province.NORTH_CENTRAL, "Anuradhapura", DestinationCategory.HERITAGE,
            8.3448, 80.3964,
            "Oldest historically authenticated living tree in the world planted in 288 BC.",
            "https://images.unsplash.com/photo-1546708973-b339540b5162?w=800"
        ),
        Destination(
            "ncp_02", "Ruwanwelisaya Stupa", "රුවන්වැලි සෑය",
            Province.NORTH_CENTRAL, "Anuradhapura", DestinationCategory.HERITAGE,
            8.3500, 80.3961,
            "Gleaming white architectural wonder built by King Dutugemunu.",
            "https://images.unsplash.com/photo-1586861635167-e5223aadc9fe?w=800"
        ),
        Destination(
            "ncp_03", "Mihintale Sacred Peak", "මිහින්තලේ",
            Province.NORTH_CENTRAL, "Anuradhapura", DestinationCategory.HERITAGE,
            8.3508, 80.5097,
            "The cradle of Buddhism in Sri Lanka adorned with 1,840 granite steps.",
            "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800"
        ),
        Destination(
            "ncp_04", "Polonnaruwa Vatadage & Quadrangle", "පොළොන්නරුව වටදාගෙය",
            Province.NORTH_CENTRAL, "Polonnaruwa", DestinationCategory.HERITAGE,
            7.9400, 81.0003,
            "Exquisitely preserved circular relic house with detailed moonstone carvings.",
            "https://images.unsplash.com/photo-1566837945700-30057527ade0?w=800"
        ),
        Destination(
            "ncp_05", "Gal Vihara Rock Statues", "ගල් විහාරය",
            Province.NORTH_CENTRAL, "Polonnaruwa", DestinationCategory.HERITAGE,
            7.9656, 81.0044,
            "Masterpiece of monolithic granite carving featuring seated and reclining Buddhas.",
            "https://images.unsplash.com/photo-1546708973-b339540b5162?w=800"
        ),
        Destination(
            "ncp_06", "Minneriya National Park (The Gathering)", "මින්නේරිය ජාතික වනෝද්‍යානය",
            Province.NORTH_CENTRAL, "Polonnaruwa", DestinationCategory.WILDLIFE,
            8.0336, 80.8986,
            "Host of the legendary Elephant Gathering around the ancient tank bed.",
            "https://images.unsplash.com/photo-1516426122078-c23e76319801?w=800"
        ),
        Destination(
            "ncp_07", "Parakrama Samudra", "පරාක්‍රම සමුද්‍රය",
            Province.NORTH_CENTRAL, "Polonnaruwa", DestinationCategory.HIKING_NATURE,
            7.9142, 80.9856,
            "Colossal reservoir built by King Parakramabahu I spanning ocean-like proportions.",
            "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=800"
        ),
        Destination(
            "ncp_08", "Abhayagiri & Jethawanaramaya Stupas", "ජේතවනාරාමය",
            Province.NORTH_CENTRAL, "Anuradhapura", DestinationCategory.HERITAGE,
            8.3517, 80.4031,
            "One of the tallest brick structures of the ancient world.",
            "https://images.unsplash.com/photo-1546708973-b339540b5162?w=800"
        ),
        Destination(
            "ncp_09", "Kaudulla National Park", "කවුඩුල්ල ජාතික වනෝද්‍යානය",
            Province.NORTH_CENTRAL, "Polonnaruwa", DestinationCategory.WILDLIFE,
            8.1481, 80.9169,
            "Key elephant corridor park offering pristine open grassland safaris.",
            "https://images.unsplash.com/photo-1516426122078-c23e76319801?w=800"
        ),
        Destination(
            "ncp_10", "Ritigala Strict Nature Reserve", "රිටිගල ආරණ්‍ය සේනාසනය",
            Province.NORTH_CENTRAL, "Anuradhapura", DestinationCategory.HERITAGE,
            8.1189, 80.6558,
            "Isolated mountain monastery lost deep inside a dense jungle microclimate.",
            "https://images.unsplash.com/photo-1448375240586-882707db888b?w=800"
        ),
        Destination(
            "ncp_11", "Kala Wewa & Avukana Buddha", "අවුකන බුදුපිළිමය",
            Province.NORTH_CENTRAL, "Anuradhapura", DestinationCategory.HERITAGE,
            8.0169, 80.5131,
            "40-foot standing Buddha statue sculpted in supreme perfection from a rock cliff.",
            "https://images.unsplash.com/photo-1546708973-b339540b5162?w=800"
        ),
        Destination(
            "ncp_12", "Isurumuniya Lovers & Rock Temple", "ඉසුරුමුණිය",
            Province.NORTH_CENTRAL, "Anuradhapura", DestinationCategory.HERITAGE,
            8.3333, 80.3906,
            "Famed rock monastery celebrated for the iconic 5th-century Lovers carving.",
            "https://images.unsplash.com/photo-1588598198321-9735fd52455d?w=800"
        ),

        // ================= WESTERN PROVINCE (12) =================
        Destination(
            "wp_01", "Galle Face Green & Colombo Lighthouse", "ගාලු මුවදොර",
            Province.WESTERN, "Colombo", DestinationCategory.CULTURAL,
            6.9249, 79.8436,
            "Historic seaside urban promenade overlooking blazing Indian Ocean sunsets.",
            "https://images.unsplash.com/photo-1578637387939-43c525550085?w=800"
        ),
        Destination(
            "wp_02", "Gangaramaya Temple & Seema Malaka", "ගංගාරාමය",
            Province.WESTERN, "Colombo", DestinationCategory.CULTURAL,
            6.9167, 79.8569,
            "Vibrant lakeside Buddhist complex featuring eclectic antique treasures.",
            "https://images.unsplash.com/photo-1588598198321-9735fd52455d?w=800"
        ),
        Destination(
            "wp_03", "Lotus Tower (Nelum Kuluna)", "නෙළුම් කුළුණ",
            Province.WESTERN, "Colombo", DestinationCategory.CULTURAL,
            6.9298, 79.8578,
            "Tallest self-supported structure in South Asia offering 360-degree skyline views.",
            "https://images.unsplash.com/photo-1628178875604-0cb859546026?w=800"
        ),
        Destination(
            "wp_04", "Kelaniya Raja Maha Viharaya", "කැලණිය රජමහා විහාරය",
            Province.WESTERN, "Gampaha", DestinationCategory.HERITAGE,
            6.9525, 79.9219,
            "Sacred riverside shrine famed for Solias Mendis murals and Duruthu Perahera.",
            "https://images.unsplash.com/photo-1546708973-b339540b5162?w=800"
        ),
        Destination(
            "wp_05", "Mount Lavinia Beach", "ගල්කිස්ස වෙරළ",
            Province.WESTERN, "Colombo", DestinationCategory.BEACH,
            6.8353, 79.8631,
            "Historic colonial beach promenade famous for dining and golden coastal sunsets.",
            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800"
        ),
        Destination(
            "wp_06", "Negombo Dutch Canal & Lagoon", "මීගමුව කළපුව",
            Province.WESTERN, "Gampaha", DestinationCategory.CULTURAL,
            7.2083, 79.8358,
            "Bustling fishing haven filled with catamaran sails and historic waterways.",
            "https://images.unsplash.com/photo-1544644181-1484b3fdfc62?w=800"
        ),
        Destination(
            "wp_07", "Kalutara Bodhiya & Chaitya", "කළුතර බෝධිය",
            Province.WESTERN, "Kalutara", DestinationCategory.CULTURAL,
            6.5828, 79.9608,
            "World's only hollow Buddhist stupa standing at the mouth of Kalu Ganga.",
            "https://images.unsplash.com/photo-1588598198321-9735fd52455d?w=800"
        ),
        Destination(
            "wp_08", "Independence Memorial Hall", "නිදහස් චතුරශ්‍රය",
            Province.WESTERN, "Colombo", DestinationCategory.HERITAGE,
            6.9042, 79.8672,
            "Imposing national monument inspired by the royal audience hall of Kandy.",
            "https://images.unsplash.com/photo-1566837945700-30057527ade0?w=800"
        ),
        Destination(
            "wp_09", "Richmond Castle", "රිච්මන්ඩ් කාසල්",
            Province.WESTERN, "Kalutara", DestinationCategory.HERITAGE,
            6.5919, 79.9889,
            "Majestic Edwardian mansion built on a 42-acre estate with Indian-British design.",
            "https://images.unsplash.com/photo-1546708973-b339540b5162?w=800"
        ),
        Destination(
            "wp_10", "Seethawaka Botanical Garden", "සීතාවක උද්භිද උද්‍යානය",
            Province.WESTERN, "Colombo", DestinationCategory.HIKING_NATURE,
            6.9083, 80.2194,
            "Lush 106-acre wet zone botanical park nestled against the Awissawella hills.",
            "https://images.unsplash.com/photo-1448375240586-882707db888b?w=800"
        ),
        Destination(
            "wp_11", "Beddagana Wetland Park", "බැද්දගාන තෙත්බිම් උද්‍යානය",
            Province.WESTERN, "Colombo (Kotte)", DestinationCategory.HIKING_NATURE,
            6.8925, 79.9042,
            "Urban biodiversity sanctuary with wooden boardwalks over Diyawanna wetlands.",
            "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800"
        ),
        Destination(
            "wp_12", "Brief Garden by Bevis Bawa", "බ්‍රීෆ් ගාර්ඩන්",
            Province.WESTERN, "Kalutara (Beruwala)", DestinationCategory.HERITAGE,
            6.4389, 80.0136,
            "Magical tropical landscaped garden estate crafted by landscape artist Bevis Bawa.",
            "https://images.unsplash.com/photo-1544735716-392fe2489ffa?w=800"
        ),

        // ================= NORTH WESTERN PROVINCE (12) =================
        Destination(
            "nwp_01", "Yapahuwa Rock Fortress", "යාපහුව බලකොටුව",
            Province.NORTH_WESTERN, "Kurunegala", DestinationCategory.HERITAGE,
            7.8183, 80.3061,
            "13th-century capital with a dramatic steep ornamental stone stairway.",
            "https://images.unsplash.com/photo-1546708973-b339540b5162?w=800"
        ),
        Destination(
            "nwp_02", "Kalpitiya Dolphin & Kite Lagoon", "කල්පිටිය",
            Province.NORTH_WESTERN, "Puttalam", DestinationCategory.BEACH,
            8.2292, 79.7656,
            "Premier kitesurfing destination and sanctuary to mega-pods of spinner dolphins.",
            "https://images.unsplash.com/photo-1502680390469-be75c86b636f?w=800"
        ),
        Destination(
            "nwp_03", "Wilpattu National Park", "විල්පත්තුව ජාතික වනෝද්‍යානය",
            Province.NORTH_WESTERN, "Puttalam", DestinationCategory.WILDLIFE,
            8.4558, 80.0092,
            "Sri Lanka's largest national park world-famed for its natural rainwater 'Villus'.",
            "https://images.unsplash.com/photo-1516426122078-c23e76319801?w=800"
        ),
        Destination(
            "nwp_04", "Athugala (Elephant Rock)", "ඇතුගල",
            Province.NORTH_WESTERN, "Kurunegala", DestinationCategory.CULTURAL,
            7.4878, 80.3664,
            "Elephant-shaped monolithic rock crowned by a giant 88-foot seated white Buddha.",
            "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800"
        ),
        Destination(
            "nwp_05", "Panduwasnuwara Ancient Kingdom", "පඬුවස්නුවර",
            Province.NORTH_WESTERN, "Kurunegala", DestinationCategory.HERITAGE,
            7.6694, 80.1839,
            "12th-century moated royal city with the circular foundation of the Chithralatha tower.",
            "https://images.unsplash.com/photo-1566837945700-30057527ade0?w=800"
        ),
        Destination(
            "nwp_06", "Munneswaram Temple", "මුන්නේශ්වරම් කෝවිල",
            Province.NORTH_WESTERN, "Puttalam", DestinationCategory.CULTURAL,
            7.5833, 79.8500,
            "Venerated Shiva shrine existing since at least 1000 CE in Chilaw.",
            "https://images.unsplash.com/photo-1588598198321-9735fd52455d?w=800"
        ),
        Destination(
            "nwp_07", "Bathalagoda Tank & Lake", "බතලගොඩ වැව",
            Province.NORTH_WESTERN, "Kurunegala", DestinationCategory.HIKING_NATURE,
            7.5256, 80.4439,
            "Serene ancient lake flanked by paddy fields and forest reserves in Ibbagamuwa.",
            "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=800"
        ),
        Destination(
            "nwp_08", "Ridi Viharaya (Silver Temple)", "රිදී විහාරය",
            Province.NORTH_WESTERN, "Kurunegala", DestinationCategory.HERITAGE,
            7.5458, 80.4789,
            "2nd-century BC cave monastery where silver was discovered for Ruwanwelisaya.",
            "https://images.unsplash.com/photo-1546708973-b339540b5162?w=800"
        ),
        Destination(
            "nwp_09", "Puttalam Dutch Fort & Salt Pans", "පුත්තලම ලුණු ලේවාය",
            Province.NORTH_WESTERN, "Puttalam", DestinationCategory.HERITAGE,
            8.0322, 79.8272,
            "Vast salt production pans and colonial trade posts along the Puttalam lagoon.",
            "https://images.unsplash.com/photo-1505118380757-91f5f5632de0?w=800"
        ),
        Destination(
            "nwp_10", "Aluvihare Rock Temple", "අලුවිහාරය",
            Province.NORTH_WESTERN, "Matale border", DestinationCategory.HERITAGE,
            7.5097, 80.6247,
            "Historic cave shrine where the Buddhist Tripitaka was committed to writing in 1st century BC.",
            "https://images.unsplash.com/photo-1588598198321-9735fd52455d?w=800"
        ),
        Destination(
            "nwp_11", "Dambadeniya Ancient Kingdom", "දඹදෙණිය",
            Province.NORTH_WESTERN, "Kurunegala", DestinationCategory.HERITAGE,
            7.3639, 80.1444,
            "13th-century cliff stronghold and historic royal kingdom of King Vijayabahu III.",
            "https://images.unsplash.com/photo-1546708973-b339540b5162?w=800"
        ),
        Destination(
            "nwp_12", "Bar Reef Marine Sanctuary", "බාර් රීෆ් කොරල් පරය",
            Province.NORTH_WESTERN, "Puttalam (Kalpitiya)", DestinationCategory.WILDLIFE,
            8.3833, 79.7333,
            "Sri Lanka's largest and most pristine offshore coral reef biodiversity system.",
            "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800"
        ),

        // ================= SABARAGAMUWA PROVINCE (12) =================
        Destination(
            "sab_01", "Sri Pada (Adam's Peak)", "ශ්‍රී පාදස්ථානය",
            Province.SABARAGAMUWA, "Ratnapura", DestinationCategory.HIKING_NATURE,
            6.8096, 80.4994,
            "Conical sacred summit bearing the sacred footprint surrounded by cloud seas.",
            "https://images.unsplash.com/photo-1544735716-392fe2489ffa?w=800"
        ),
        Destination(
            "sab_02", "Sinharaja Forest Reserve (Kudawa/Waddagala)", "සිංහරාජ වැසි වනාන්තරය",
            Province.SABARAGAMUWA, "Ratnapura", DestinationCategory.HIKING_NATURE,
            6.4167, 80.4167,
            "UNESCO biosphere rainforest packed with endemic birds, reptiles, and canopy trees.",
            "https://images.unsplash.com/photo-1448375240586-882707db888b?w=800"
        ),
        Destination(
            "sab_03", "Udawalawe National Park", "උඩවලව ජාතික වනෝද්‍යානය",
            Province.SABARAGAMUWA, "Ratnapura", DestinationCategory.WILDLIFE,
            6.4744, 80.8986,
            "Savannah-like sanctuary renowned for guaranteed wild elephant sightings.",
            "https://images.unsplash.com/photo-1516426122078-c23e76319801?w=800"
        ),
        Destination(
            "sab_04", "Bopath Ella Falls", "බෝපත් ඇල්ල",
            Province.SABARAGAMUWA, "Ratnapura", DestinationCategory.WATERFALL,
            6.7967, 80.3664,
            "Waterfall cascading through a narrow cleft resembling the leaf of a Sacred Bo tree.",
            "https://images.unsplash.com/photo-1432405972618-c60b0225b8f9?w=800"
        ),
        Destination(
            "sab_05", "Pinnawala Elephant Orphanage", "පින්නවල අලි අනාථාගාරය",
            Province.SABARAGAMUWA, "Kegalle", DestinationCategory.WILDLIFE,
            7.3014, 80.3858,
            "Pioneering sanctuary where elephant herds bathe daily in the Maha Oya river.",
            "https://images.unsplash.com/photo-1516426122078-c23e76319801?w=800"
        ),
        Destination(
            "sab_06", "Kitulgala White Water Rafting", "කිතුල්ගල",
            Province.SABARAGAMUWA, "Kegalle", DestinationCategory.HIKING_NATURE,
            6.9939, 80.4128,
            "Adventure capital nestled along the roaring Kelani River rapids.",
            "https://images.unsplash.com/photo-1502680390469-be75c86b636f?w=800"
        ),
        Destination(
            "sab_07", "Kirindi Ella Falls", "කිරිඳි ඇල්ල",
            Province.SABARAGAMUWA, "Ratnapura", DestinationCategory.WATERFALL,
            6.7417, 80.5056,
            "116-meter picturesque waterfall descending into a deep pool near Pelmadulla.",
            "https://images.unsplash.com/photo-1596707328965-021e1026027a?w=800"
        ),
        Destination(
            "sab_08", "Belilena Prehistoric Caves", "බෙලිලෙන ගුහාව",
            Province.SABARAGAMUWA, "Kegalle", DestinationCategory.HERITAGE,
            6.9833, 80.4333,
            "Famed archaeological cave revealing 30,000-year-old Balangoda Man remains.",
            "https://images.unsplash.com/photo-1546708973-b339540b5162?w=800"
        ),
        Destination(
            "sab_09", "Maha Saman Devalaya", "මහා සමන් දේවාලය",
            Province.SABARAGAMUWA, "Ratnapura", DestinationCategory.CULTURAL,
            6.6872, 80.3800,
            "Majestic 13th-century shrine dedicated to deity Sumana Saman, guardian of Sri Lanka.",
            "https://images.unsplash.com/photo-1588598198321-9735fd52455d?w=800"
        ),
        Destination(
            "sab_10", "Wavulpane Bat Cave & Calcium Fall", "වවුල්පනේ හුණුගල් ගුහාව",
            Province.SABARAGAMUWA, "Ratnapura", DestinationCategory.HIKING_NATURE,
            6.4442, 80.7481,
            "Prehistoric limestone cavern housing hundreds of thousands of bats and underground cascades.",
            "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800"
        ),
        Destination(
            "sab_11", "Surathali Falls", "සුරතලී ඇල්ල",
            Province.SABARAGAMUWA, "Ratnapura (Belihuloya)", DestinationCategory.WATERFALL,
            6.7333, 80.8167,
            "Graceful 60-meter tiered cascade sliding down a sheer rock face along the Colombo-Badulla highway.",
            "https://images.unsplash.com/photo-1432405972618-c60b0225b8f9?w=800"
        ),
        Destination(
            "sab_12", "Batadombalena Prehistoric Cave", "බටදොඹලෙන",
            Province.SABARAGAMUWA, "Ratnapura (Kuruwita)", DestinationCategory.HERITAGE,
            6.7667, 80.4000,
            "Important prehistoric cave habitation yielding fossil records and microliths from 36,000 BP.",
            "https://images.unsplash.com/photo-1546708973-b339540b5162?w=800"
        )
    )
}
